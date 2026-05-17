package com.mindmatrix.skillexchange.repository

import com.mindmatrix.skillexchange.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface SkillExchangeRepository {
    fun getCurrentUser(): Flow<User>
    fun getNeedPosts(): Flow<List<NeedPost>>
    fun getOffersForPost(postId: String): Flow<List<SwapOffer>>
    suspend fun addNeedPost(description: String, skillRequired: String)
    suspend fun makeOffer(postId: String, message: String, hours: Int)
    suspend fun confirmSwap(offerId: String)
    suspend fun login(name: String, password: String): Boolean
    suspend fun signup(name: String, password: String, skill: String): Boolean
    suspend fun logout()
}

class MockSkillExchangeRepository : SkillExchangeRepository {
    
    companion object {
        // Shared mock data across all instances to persist across logins
        private val currentUser = MutableStateFlow<User?>(null)
        private val allUsers = MutableStateFlow(listOf(
            User("u1", "Raju (Plumber)", "123", "Plumber", trustScore = 15, skillPoints = 5)
        ))
        
        private val posts = MutableStateFlow(listOf(
            NeedPost("p1", "u2", "Ramesh (Electrician)", "Need help with leaking roof.", "Plumber", PostStatus.OPEN, System.currentTimeMillis() - 100000),
            NeedPost("p2", "u3", "Suresh (Carpenter)", "Need someone to fix my ceiling fan.", "Electrician", PostStatus.OPEN, System.currentTimeMillis() - 50000)
        ))
        
        private val offers = MutableStateFlow(listOf<SwapOffer>())
    }

    override fun getCurrentUser(): Flow<User> = currentUser.map { it ?: User() } // Fallback for UI if null

    override fun getNeedPosts(): Flow<List<NeedPost>> = posts

    override fun getOffersForPost(postId: String): Flow<List<SwapOffer>> {
        return offers.map { allOffers -> allOffers.filter { it.postId == postId } }
    }

    override suspend fun addNeedPost(description: String, skillRequired: String) {
        val user = currentUser.value ?: return
        val newPost = NeedPost(
            postId = UUID.randomUUID().toString(),
            authorId = user.userId,
            authorName = user.name,
            description = description,
            skillRequired = skillRequired
        )
        posts.value = listOf(newPost) + posts.value
    }

    override suspend fun makeOffer(postId: String, message: String, hours: Int) {
        val user = currentUser.value ?: return
        val newOffer = SwapOffer(
            offerId = UUID.randomUUID().toString(),
            postId = postId,
            offererId = user.userId,
            offererName = user.name,
            offeredSkill = user.skill,
            hours = hours,
            message = message
        )
        offers.value = offers.value + newOffer
    }

    override suspend fun confirmSwap(offerId: String) {
        val offer = offers.value.find { it.offerId == offerId } ?: return
        
        // Update Offer Status
        val updatedOffers = offers.value.map { 
            if (it.offerId == offerId) it.copy(status = OfferStatus.ACCEPTED) else it 
        }
        offers.value = updatedOffers
        
        // Update Post Status
        val updatedPosts = posts.value.map {
            if (it.postId == offer.postId) it.copy(status = PostStatus.COMPLETED) else it
        }
        posts.value = updatedPosts
        
        // Update User Trust Score and Skill Points in allUsers
        val post = updatedPosts.find { it.postId == offer.postId }
        val authorId = post?.authorId
        val offererId = offer.offererId

        allUsers.value = allUsers.value.map { user ->
            when (user.userId) {
                authorId -> user.copy(trustScore = user.trustScore + 10, skillPoints = user.skillPoints + offer.hours)
                offererId -> user.copy(trustScore = user.trustScore + 10, skillPoints = user.skillPoints + offer.hours)
                else -> user
            }
        }

        // Update current user if they were part of the swap
        if (currentUser.value?.userId == authorId || currentUser.value?.userId == offererId) {
            currentUser.value = allUsers.value.find { it.userId == currentUser.value?.userId }
        }
    }

    override suspend fun login(name: String, password: String): Boolean {
        val user = allUsers.value.find { it.name.contains(name, ignoreCase = true) && it.password == password }
        if (user != null) {
            currentUser.value = user
            return true
        }
        return false
    }

    override suspend fun signup(name: String, password: String, skill: String): Boolean {
        val newUser = User(
            userId = UUID.randomUUID().toString(),
            name = name,
            password = password,
            skill = skill
        )
        allUsers.value = allUsers.value + newUser
        currentUser.value = newUser
        return true
    }

    override suspend fun logout() {
        currentUser.value = null
    }
}

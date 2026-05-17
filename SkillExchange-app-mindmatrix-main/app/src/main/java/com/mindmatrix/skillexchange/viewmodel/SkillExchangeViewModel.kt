package com.mindmatrix.skillexchange.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.skillexchange.model.NeedPost
import com.mindmatrix.skillexchange.model.SwapOffer
import com.mindmatrix.skillexchange.model.User
import com.mindmatrix.skillexchange.repository.SkillExchangeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SkillExchangeViewModel(private val repository: SkillExchangeRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _needPosts = MutableStateFlow<List<NeedPost>>(emptyList())
    val needPosts: StateFlow<List<NeedPost>> = _needPosts.asStateFlow()

    private val _offersForCurrentPost = MutableStateFlow<List<SwapOffer>>(emptyList())
    val offersForCurrentPost: StateFlow<List<SwapOffer>> = _offersForCurrentPost.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCurrentUser().collectLatest { user ->
                _currentUser.value = user
            }
        }

        viewModelScope.launch {
            repository.getNeedPosts().collectLatest { posts ->
                _needPosts.value = posts
            }
        }
    }

    fun addNeedPost(description: String, skillRequired: String) {
        viewModelScope.launch {
            repository.addNeedPost(description, skillRequired)
        }
    }

    fun loadOffersForPost(postId: String) {
        viewModelScope.launch {
            repository.getOffersForPost(postId).collectLatest { offers ->
                _offersForCurrentPost.value = offers
            }
        }
    }

    fun makeOffer(postId: String, message: String, hours: Int) {
        viewModelScope.launch {
            repository.makeOffer(postId, message, hours)
        }
    }

    fun confirmSwap(offerId: String) {
        viewModelScope.launch {
            repository.confirmSwap(offerId)
        }
    }

    fun login(name: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.login(name, password)
            onResult(success)
        }
    }

    fun signup(name: String, password: String, skill: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.signup(name, password, skill)
            onResult(success)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}

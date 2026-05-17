package com.mindmatrix.skillexchange.model

data class User(
    val userId: String = "",
    val name: String = "",
    val password: String = "123", // For mock auth
    val skill: String = "",
    val trustScore: Int = 0,
    val skillPoints: Int = 0
)

data class NeedPost(
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val description: String = "",
    val skillRequired: String = "",
    val status: PostStatus = PostStatus.OPEN,
    val timestamp: Long = System.currentTimeMillis()
)

enum class PostStatus {
    OPEN, NEGOTIATING, COMPLETED
}

data class SwapOffer(
    val offerId: String = "",
    val postId: String = "",
    val offererId: String = "",
    val offererName: String = "",
    val offeredSkill: String = "",
    val hours: Int = 1,
    val message: String = "",
    val status: OfferStatus = OfferStatus.PENDING
)

enum class OfferStatus {
    PENDING, ACCEPTED, REJECTED
}

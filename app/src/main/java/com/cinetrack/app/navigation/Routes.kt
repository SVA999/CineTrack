package com.cinetrack.app.navigation

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Register = "register"
    const val ForgotPassword = "forgot_password"
    const val Explore = "explore"
    const val Search = "search"
    const val MyList = "my_list"
    const val Profile = "profile"
    const val EditProfile = "profile/edit"
    const val Settings = "settings"
    const val DeleteAccount = "account/delete"
    const val Credits = "credits"

    const val DetailBase = "detail"
    const val ReviewBase = "review"
    const val PublicProfileBase = "public_profile"
    const val SharedReviewBase = "shared_review"
    const val SharedMediaBase = "shared_media"

    const val Detail = "$DetailBase/{mediaId}"
    const val Review = "$ReviewBase/{mediaId}"
    const val PublicProfile = "$PublicProfileBase/{userId}"
    const val SharedReview = "$SharedReviewBase/{reviewId}"
    const val SharedMedia = "$SharedMediaBase/{mediaType}/{tmdbId}"

    fun detail(mediaId: String) = "$DetailBase/$mediaId"
    fun review(mediaId: String) = "$ReviewBase/$mediaId"
    fun publicProfile(userId: String) = "$PublicProfileBase/$userId"
    fun sharedReview(reviewId: String) = "$SharedReviewBase/$reviewId"
    fun sharedMedia(mediaType: String, tmdbId: Int) = "$SharedMediaBase/$mediaType/$tmdbId"
}

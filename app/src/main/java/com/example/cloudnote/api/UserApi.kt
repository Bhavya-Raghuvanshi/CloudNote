package com.example.cloudnote.api

import com.example.cloudnote.models.ProfileDetails
import com.example.cloudnote.models.ProfileImage
import com.example.cloudnote.models.User
import com.google.gson.Gson

class UserApi
{
    companion object
    {
        fun login(user: User): ApiResponse = Api.post("auth/login.php", Gson().toJson(user))
        fun register(user: User): ApiResponse = Api.post("auth/signup.php", Gson().toJson(user))

        fun profile(profileDetails: ProfileDetails,accessToken: String): ApiResponse = Api.post("users/profile.php",Gson().toJson(profileDetails),accessToken)
        fun profileImage(profileImage: ProfileImage,accessToken: String): ApiResponse = Api.post("users/avatar.php",Gson().toJson(profileImage),accessToken)
        fun getProfile(accessToken: String):ApiResponse = Api.get("users/get_profile.php",accessToken)

        fun ApiResponse.asUser(): User = Gson().fromJson(this.json, User::class.java)
    }
}
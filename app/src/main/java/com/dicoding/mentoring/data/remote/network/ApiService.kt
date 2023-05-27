package com.dicoding.mentoring.data.remote.network

import com.dicoding.mentoring.data.local.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("auth/register")
    fun postRegister(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("post-feedback")
    fun postFeedback(
        @Header("Authorization") token: String,
        @Field("mentoring_id") mentoringId: String,
        @Field("rating") rating: Float,
        @Field("feedback") feedback: String
    ): Call<FeedbackResponse>

    @GET("mentor/all")
    fun getMentors(
        @Header("Authorization") token: String,
    ): Call<MentorsResponse>

    @GET("mentors-by-interest")
    fun getMentorsByInterest(
        @Header("Authorization") token: String, @Query("list_interest") listInterest: List<String>
    ): Call<MentorsResponse>

//    @GET("schedules")
//    fun getSchedules(
//        @Header("Authorization") token: String,
//        @Query("from_date") fromDate: String,
//    ): Call<ScheduleResponse>

    @GET("user")
    fun getUserProfile(
        @Header("Authorization") token: String,
    ): Call<GetUserProfileResponse>

    @FormUrlEncoded
    @PUT("user")
    fun updateUserProfile(
        @Header("Authorization") token: String?,
        @Field("name") name : String?,
        @Field("gender_id") gender_id : Int?,
        @Field("phone") phone : String?,
        @Field("bio") bio : String?,
        @Field("email") email : String?
    ) : Call<PostUserProfileResponse>

    @GET("user/interest")
    fun getUserInterest(
        @Header("Authorization") token: String
    ) : Call<InterestResponse>

    @FormUrlEncoded
    @PUT("user/interest")
    fun updateUserInterest(
        @Header("Authorization") token: String,
        @Field("is_path_android") is_path_android : Boolean?,
        @Field("is_path_ios") is_path_ios :  Boolean?,
        @Field("is_path_flutter") is_path_flutter:  Boolean?,
        @Field("is_path_ml") is_path_ml :  Boolean?,
        @Field("is_path_fe") is_path_fe:  Boolean?,
        @Field("is_path_be") is_path_be :  Boolean?,
        @Field("is_path_react") is_path_react:  Boolean?,
        @Field("is_path_devops") is_path_devops:  Boolean?,
        @Field("is_path_gcp") is_path_gcp :  Boolean?
    ) : Call<PostUserProfileResponse>

    @FormUrlEncoded
    @POST("user/avatar")
    fun updateUserProfilePicture(
        @Header("Authorization") token:String
    ) : Callback<GetUserProfileResponse>
}
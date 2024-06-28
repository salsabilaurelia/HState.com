package com.example.h_state.API

import com.example.h_state.Modal.LoginResponse
import com.example.h_state.Modal.RoomHome
import com.example.h_state.Modal.User
import com.example.h_state.Modal.UserDetailsResponse
import com.example.h_state.Modal.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @GET("api-get-email.php")
    fun getUserByEmail(@Query("email") email: String): Call<UserResponse>

    @FormUrlEncoded
    @POST("api-register.php")
    fun register(
        @Field("post_fullname") fullname: String,
        @Field("post_email") email: String,
        @Field("post_password") password: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api-login.php")
    fun login(
        @Field("post_email") email: String,
        @Field("post_password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("api-post-reservasi.php")
    fun booking(
        @Field("post_fullname") fullname: String,
        @Field("post_noHp") noHp: String,
        @Field("post_tglCheckin") tglCheckin: String,
        @Field("post_tglCheckout") tglCheckout: String,
        @Field("post_id_room") id_room: String
    ): Call<ResponseBody>

    @GET("api-reservasi-data.php")
    fun getReservasi(): Call<ResponseBody>

    @GET("api-room.php")
    fun data(): Call<ResponseBody>

    @GET("api-user.php")
    fun getUserDetails(@Query("email") email: String): Call<ResponseBody>

    @DELETE("api-delete-reservasi.php")
    fun deleteReservasi(@Query("id") id: String): Call<ResponseBody>

    @Multipart
    @POST("api-upload-image.php")
    fun uploadImage(
        @Part("email") email: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api-edit-profil.php")
    fun updateUserAccount(
        @Field("original_email") originalEmail: String,
        @Field("full_name") fullname: String,
        @Field("new_email") newEmail: String,
        @Field("password") password: String
    ): Call<ResponseBody>

    @GET("api-get-image.php")
    fun getProfileImage(@Query("email") email: String): Call<ResponseBody>

}


interface OnRoomImageClickListener {
    fun onRoomImageClick(room: RoomHome)
}
interface OnButtonClickListener {
    fun onButtonClick(room: RoomHome)
}






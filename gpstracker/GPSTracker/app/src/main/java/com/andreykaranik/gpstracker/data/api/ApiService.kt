package com.andreykaranik.gpstracker.data.api

import com.andreykaranik.gpstracker.data.api.request.CreateGroupRequest
import com.andreykaranik.gpstracker.data.api.request.GetPeriodClustersRequest
import com.andreykaranik.gpstracker.data.api.request.GetPeriodLocationsRequest
import com.andreykaranik.gpstracker.data.api.request.JoinGroupRequest
import com.andreykaranik.gpstracker.data.api.request.LoginRequest
import com.andreykaranik.gpstracker.data.api.request.Mode11Request
import com.andreykaranik.gpstracker.data.api.request.Mode12Request
import com.andreykaranik.gpstracker.data.api.request.RefreshTokenRequest
import com.andreykaranik.gpstracker.data.api.request.RegisterRequest
import com.andreykaranik.gpstracker.data.api.request.SendDataRequest
import com.andreykaranik.gpstracker.data.api.response.CreateGroupResponse
import com.andreykaranik.gpstracker.data.api.response.GetGroupDataResponse
import com.andreykaranik.gpstracker.data.api.response.GetGroupMembersResponse
import com.andreykaranik.gpstracker.data.api.response.GetPeriodClustersResponse
import com.andreykaranik.gpstracker.data.api.response.GetPeriodLocationsResponse
import com.andreykaranik.gpstracker.data.api.response.JoinGroupResponse
import com.andreykaranik.gpstracker.data.api.response.LeaveGroupResponse
import com.andreykaranik.gpstracker.data.api.response.LoginResponse
import com.andreykaranik.gpstracker.data.api.response.Mode11Response
import com.andreykaranik.gpstracker.data.api.response.Mode12Response
import com.andreykaranik.gpstracker.data.api.response.RefreshTokenResponse
import com.andreykaranik.gpstracker.data.api.response.RegisterResponse
import com.andreykaranik.gpstracker.data.api.response.SendDataResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/register")
    @Headers("Content-Type: application/json")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("api/auth/login")
    @Headers("Content-Type: application/json")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/auth/refresh-token")
    @Headers("Content-Type: application/json")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<RefreshTokenResponse>

    @GET("api/groups/group")
    @Headers("Content-Type: application/json")
    fun getGroupData(
        @Header("Authorization") accessToken: String
    ): Call<GetGroupDataResponse>

    @POST("api/groups/create")
    @Headers("Content-Type: application/json")
    fun createGroup(
        @Header("Authorization") accessToken: String,
        @Body request: CreateGroupRequest
    ): Call<CreateGroupResponse>

    @POST("api/groups/join")
    @Headers("Content-Type: application/json")
    fun joinGroup(
        @Header("Authorization") accessToken: String,
        @Body request: JoinGroupRequest
    ): Call<JoinGroupResponse>

    @DELETE("api/groups/leave")
    @Headers("Content-Type: application/json")
    fun leaveGroup(
        @Header("Authorization") accessToken: String
    ): Call<LeaveGroupResponse>

    @GET("api/groups/members")
    @Headers("Content-Type: application/json")
    fun getGroupMembers(
        @Header("Authorization") accessToken: String
    ): Call<GetGroupMembersResponse>

    @POST("api/data/send-data")
    @Headers("Content-Type: application/json")
    fun sendData(
        @Header("Authorization") accessToken: String,
        @Body request: SendDataRequest
    ): Call<SendDataResponse>

    @POST("api/data/mode1-1")
    @Headers("Content-Type: application/json")
    fun mode11(
        @Header("Authorization") accessToken: String,
        @Body request: Mode11Request
    ): Call<Mode11Response>

    @POST("api/data/mode1-2")
    @Headers("Content-Type: application/json")
    fun mode12(
        @Header("Authorization") accessToken: String,
        @Body request: Mode12Request
    ): Call<Mode12Response>

    @POST("api/data/get-period-locations")
    @Headers("Content-Type: application/json")
    fun getPeriodLocations(
        @Header("Authorization") accessToken: String,
        @Body request: GetPeriodLocationsRequest
    ): Call<GetPeriodLocationsResponse>

    @POST("api/data/get-period-clusters")
    @Headers("Content-Type: application/json")
    fun getPeriodClusters(
        @Header("Authorization") accessToken: String,
        @Body request: GetPeriodClustersRequest
    ): Call<GetPeriodClustersResponse>
}
package com.andreykaranik.gpstracker.data.repository

import android.content.Context
import com.andreykaranik.gpstracker.data.api.ApiService
import com.andreykaranik.gpstracker.data.api.request.CreateGroupRequest
import com.andreykaranik.gpstracker.data.api.request.JoinGroupRequest
import com.andreykaranik.gpstracker.domain.model.GroupData
import com.andreykaranik.gpstracker.domain.model.GroupMember
import com.andreykaranik.gpstracker.domain.model.result.CreateGroupResult
import com.andreykaranik.gpstracker.domain.model.result.GetGroupDataResult
import com.andreykaranik.gpstracker.domain.model.result.GetGroupMembersResult
import com.andreykaranik.gpstracker.domain.model.result.JoinGroupResult
import com.andreykaranik.gpstracker.domain.model.result.LeaveGroupResult
import com.andreykaranik.gpstracker.domain.repository.GroupRepository

class GroupRepositoryImpl(
    private val context: Context,
    private val apiService: ApiService
) : GroupRepository {
    override fun getGroupData(accessToken: String): GetGroupDataResult {
        try {
            val response =
                apiService.getGroupData(
                    accessToken = "Bearer $accessToken"
                ).execute()

            when (response.code()) {
                200 -> {
                    val body = response.body()!!
                    return GetGroupDataResult.Success(
                        groupData = GroupData(
                            id = body.id!!,
                            name = body.name!!,
                            type = body.type!!
                        )
                    )
                }

                404 -> {
                    return GetGroupDataResult.IsNotInGroup
                }

                401 -> {
                    return GetGroupDataResult.Unauthorized
                }

                else -> {
                    return GetGroupDataResult.Failure
                }
            }
        } catch (e: Exception) {
            return GetGroupDataResult.Failure
        }
    }

    override fun createGroup(
        accessToken: String,
        name: String,
        type: Int,
        joinCode: String
    ): CreateGroupResult {
        try {
            val response =
                apiService.createGroup(
                    "Bearer $accessToken",
                    CreateGroupRequest(
                        name = name,
                        type = type,
                        joinCode = joinCode
                    )
                ).execute()

            when (response.code()) {
                200 -> {
                    return CreateGroupResult.Success
                }

                400 -> {
                    return CreateGroupResult.IsAlreadyInGroup
                }

                401 -> {
                    return CreateGroupResult.Unauthorized
                }

                else -> {
                    return CreateGroupResult.Failure
                }
            }
        } catch (e: Exception) {
            return CreateGroupResult.Failure
        }
    }

    override fun joinGroup(
        accessToken: String,
        id: Int,
        joinCode: String
    ): JoinGroupResult {
        try {
            val response =
                apiService.joinGroup(
                    "Bearer $accessToken",
                    JoinGroupRequest(
                        id = id,
                        joinCode = joinCode
                    )
                ).execute()

            when (response.code()) {
                200 -> {
                    return JoinGroupResult.Success
                }

                400 -> {
                    return JoinGroupResult.IsAlreadyInGroup
                }

                401 -> {
                    return JoinGroupResult.Unauthorized
                }

                404 -> {
                    return JoinGroupResult.InvalidGroupIdOrJoinCode
                }

                else -> {
                    return JoinGroupResult.Failure
                }
            }
        } catch (e: Exception) {
            return JoinGroupResult.Failure
        }
    }

    override fun leaveGroup(accessToken: String): LeaveGroupResult {
        try {
            val response =
                apiService.leaveGroup(
                    "Bearer $accessToken"
                ).execute()

            when (response.code()) {
                200 -> {
                    return LeaveGroupResult.Success
                }

                400 -> {
                    return LeaveGroupResult.IsNotInGroup
                }

                401 -> {
                    return LeaveGroupResult.Unauthorized
                }

                else -> {
                    return LeaveGroupResult.Failure
                }
            }
        } catch (e: Exception) {
            return LeaveGroupResult.Failure
        }
    }

    override fun getGroupMembers(accessToken: String): GetGroupMembersResult {
        try {
            val response =
                apiService.getGroupMembers(
                    "Bearer $accessToken"
                ).execute()

            when (response.code()) {
                200 -> {
                    val body = response.body()!!
                    val members = body.members!!
                    return GetGroupMembersResult.Success(
                        groupMembers = members.map {
                            GroupMember(
                                id = it.id,
                                name = it.name,
                                email = it.email
                            )
                        }
                    )
                }

                400 -> {
                    return GetGroupMembersResult.IsNotInGroup
                }

                401 -> {
                    return GetGroupMembersResult.Unauthorized
                }

                else -> {
                    return GetGroupMembersResult.Failure
                }
            }
        } catch (e: Exception) {
            return GetGroupMembersResult.Failure
        }
    }
}
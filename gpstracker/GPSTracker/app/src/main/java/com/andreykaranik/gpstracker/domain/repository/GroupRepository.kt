package com.andreykaranik.gpstracker.domain.repository

import com.andreykaranik.gpstracker.domain.model.result.CreateGroupResult
import com.andreykaranik.gpstracker.domain.model.result.GetGroupDataResult
import com.andreykaranik.gpstracker.domain.model.result.GetGroupMembersResult
import com.andreykaranik.gpstracker.domain.model.result.JoinGroupResult
import com.andreykaranik.gpstracker.domain.model.result.LeaveGroupResult

interface GroupRepository {
    fun getGroupData(accessToken: String): GetGroupDataResult
    fun createGroup(accessToken: String, name: String, type: Int, joinCode: String): CreateGroupResult
    fun joinGroup(accessToken: String, id: Int, joinCode: String): JoinGroupResult
    fun leaveGroup(accessToken: String): LeaveGroupResult
    fun getGroupMembers(accessToken: String): GetGroupMembersResult
}


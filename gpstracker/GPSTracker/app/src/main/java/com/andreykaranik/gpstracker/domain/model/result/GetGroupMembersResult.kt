package com.andreykaranik.gpstracker.domain.model.result

import com.andreykaranik.gpstracker.domain.model.GroupMember

sealed class GetGroupMembersResult {
    data class Success(
        val groupMembers: List<GroupMember>
    ) : GetGroupMembersResult()
    object Unauthorized : GetGroupMembersResult()
    object IsNotInGroup : GetGroupMembersResult()
    object Failure : GetGroupMembersResult()
    object Pending : GetGroupMembersResult()
    object None : GetGroupMembersResult()
}

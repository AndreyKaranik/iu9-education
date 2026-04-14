package com.andreykaranik.gpstracker.data.api.response

data class GetGroupMembersResponse(
    val members: List<Member>?,
    val message: String?,
    val error: String?
) {
    data class Member(
        val id: Int,
        val name: String,
        val email: String
    )
}
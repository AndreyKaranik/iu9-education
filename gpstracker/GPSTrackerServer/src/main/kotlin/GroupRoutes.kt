import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt

fun Route.groupRoutes() {
    authenticate("auth-jwt") {
        get("/group") {
            val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
            val conn = Database.connection

            val group = conn.prepareStatement(
                """
        SELECT g.id, g.name, g.type
        FROM group_members gm
        JOIN groups g ON gm.group_id = g.id
        WHERE gm.user_id = ?
        """
            ).use { stmt ->
                stmt.setInt(1, userId)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    GroupInfo(
                        id = rs.getInt("id"),
                        name = rs.getString("name"),
                        type = rs.getInt("type")
                    )
                } else null
            }

            if (group == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("User is not in a group"))
            } else {
                call.respond(HttpStatusCode.OK, group)
            }
        }

        post("/create") {
            val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
            val request = call.receive<CreateGroupRequest>()
            val conn = Database.connection

            conn.autoCommit = false

            val hashedJoinCode = hash(request.joinCode)

            try {
                val existing = conn.prepareStatement(
                    "SELECT 1 FROM group_members WHERE user_id = ?"
                ).use {
                    it.setInt(1, userId)
                    it.executeQuery().next()
                }

                if (existing) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("User already in a group"))
                    return@post
                }

                val groupId = conn.prepareStatement(
                    "INSERT INTO groups (name, type, join_code_hash, owner_id) VALUES (?, ?, ?, ?) RETURNING id"
                ).use {
                    it.setString(1, request.name)
                    it.setInt(2, request.type)
                    it.setString(3, hashedJoinCode)
                    it.setInt(4, userId)
                    val rs = it.executeQuery()
                    if (rs.next()) rs.getInt("id") else throw Exception("Group creation failed")
                }

                conn.prepareStatement(
                    "INSERT INTO group_members (user_id, group_id) VALUES (?, ?)"
                ).use {
                    it.setInt(1, userId)
                    it.setInt(2, groupId)
                    it.executeUpdate()
                }

                conn.commit()
                call.respond(HttpStatusCode.OK, MessageResponse("Group created"))
            } catch (e: Exception) {
                conn.rollback()
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error creating group"))
            } finally {
                conn.autoCommit = true
            }
        }

        post("/join") {
            val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
            val request = call.receive<JoinGroupRequest>()
            val conn = Database.connection

            val inGroup = conn.prepareStatement(
            "SELECT 1 FROM group_members WHERE user_id = ?"
            ).use {
                it.setInt(1, userId)
                it.executeQuery().next()
            }

            if (inGroup) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Already in a group"))
                return@post
            }

            val joinCodeHash: String? = conn.prepareStatement(
            "SELECT join_code_hash FROM groups WHERE id = ?"
            ).use {
                it.setInt(1, request.id)
                val rs = it.executeQuery()
                if (rs.next()) rs.getString("join_code_hash") else null
            }

            if (joinCodeHash == null || !BCrypt.checkpw(request.joinCode, joinCodeHash)) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Invalid group ID or join code"))
                return@post
            }

            conn.prepareStatement(
            "INSERT INTO group_members (user_id, group_id) VALUES (?, ?)"
            ).use {
                it.setInt(1, userId)
                it.setInt(2, request.id)
                it.executeUpdate()
            }

            call.respond(HttpStatusCode.OK, MessageResponse("Joined group"))
        }

        delete("/leave") {
            val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
            val conn = Database.connection

            conn.autoCommit = false
            try {
                val groupId = conn.prepareStatement(
                    "SELECT group_id FROM group_members WHERE user_id = ?"
                ).use {
                    it.setInt(1, userId)
                    val rs = it.executeQuery()
                    if (rs.next()) rs.getInt("group_id") else null
                }

                if (groupId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("User not in a group"))
                    return@delete
                }

                val isOwner = conn.prepareStatement(
                    "SELECT 1 FROM groups WHERE id = ? AND owner_id = ?"
                ).use {
                    it.setInt(1, groupId)
                    it.setInt(2, userId)
                    it.executeQuery().next()
                }

                if (isOwner) {
                    conn.prepareStatement("DELETE FROM groups WHERE id = ?").use {
                        it.setInt(1, groupId)
                        it.executeUpdate()
                    }
                } else {
                    conn.prepareStatement("DELETE FROM group_members WHERE user_id = ?").use {
                        it.setInt(1, userId)
                        it.executeUpdate()
                    }
                }

                conn.commit()
                call.respond(HttpStatusCode.OK, MessageResponse("Left the group"))
            } catch (e: Exception) {
                conn.rollback()
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error leaving group"))
            } finally {
                conn.autoCommit = true
            }
        }

        get("/members") {
            val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
            val conn = Database.connection

            val groupId = conn.prepareStatement(
                "SELECT group_id FROM group_members WHERE user_id = ?"
            ).use {
                it.setInt(1, userId)
                val rs = it.executeQuery()
                if (rs.next()) rs.getInt("group_id") else null
            }

            if (groupId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("User is not in a group"))
                return@get
            }

            val members = conn.prepareStatement(
                """
            SELECT u.id, u.name, u.email 
            FROM users u
            JOIN group_members gm ON u.id = gm.user_id
            WHERE gm.group_id = ?
            """
            ).use {
                it.setInt(1, groupId)
                val rs = it.executeQuery()
                val list = mutableListOf<UserBasicInfo>()
                while (rs.next()) {
                    list.add(
                        UserBasicInfo(
                            id = rs.getInt("id"),
                            name = rs.getString("name"),
                            email = rs.getString("email")
                        )
                    )
                }
                list
            }

            call.respond(HttpStatusCode.OK, mapOf("members" to members))
        }
    }
}

@Serializable
data class CreateGroupRequest(
    val name: String,
    val type: Int,
    @SerialName("join_code")
    val joinCode: String
)

@Serializable
data class JoinGroupRequest(
    val id: Int,
    @SerialName("join_code")
    val joinCode: String
)

@Serializable
data class UserBasicInfo(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class GroupInfo(
    val id: Int,
    val name: String,
    val type: Int
)
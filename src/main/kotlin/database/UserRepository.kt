package com.apppillar.database

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class UserRepository {

    fun registerUser(username: String, email: String, password: String): Boolean {
        return transaction {
            // Проверка, существует ли уже пользователь
            val userExists = Users.select { Users.email eq email }.count() > 0
            if (userExists) return@transaction false

            // Хеширование пароля
            val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())

            // Вставка пользователя
            Users.insert {
                it[Users.username] = username
                it[Users.email] = email
                it[Users.passwordHash] = passwordHash
            }

            return@transaction true
        }
    }

    fun checkCredentials(email: String, password: String): String? {
        return transaction {
            val user = Users.select { Users.email eq email }.singleOrNull()
            if (user != null) {
                val hash = user[Users.passwordHash]
                if (BCrypt.checkpw(password, hash)) {
                    return@transaction user[Users.username] // вернём имя
                }
            }
            return@transaction null
        }
    }

    fun createPasswordResetToken(email: String): String {
        val token = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        transaction {
            // Удалим старые токены этого email
            PasswordResetTokens.deleteWhere { PasswordResetTokens.email eq email }

            PasswordResetTokens.insert {
                it[PasswordResetTokens.email] = email
                it[PasswordResetTokens.token] = token
                it[PasswordResetTokens.createdAt] = timestamp
            }
        }

        return token
    }

    fun resetPassword(token: String, newPassword: String): Boolean {
        return transaction {
            val row = PasswordResetTokens.select { PasswordResetTokens.token eq token }.singleOrNull()
                ?: return@transaction false

            val email = row[PasswordResetTokens.email]
            val passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())

            Users.update({ Users.email eq email }) {
                it[Users.passwordHash] = passwordHash
            }

            // Удалим токен, чтобы он не сработал снова
            PasswordResetTokens.deleteWhere { PasswordResetTokens.token eq token }

            true
        }
    }

    fun isUserExists(email: String): Boolean {
        return transaction {
            Users.select { Users.email eq email }.count() > 0
        }
    }
}
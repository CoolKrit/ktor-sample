package com.apppillar.database

import org.jetbrains.exposed.sql.Table

object PasswordResetTokens : Table() {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 100)
    val token = varchar("token", 128)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}
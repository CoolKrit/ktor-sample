package com.apppillar.database

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50)
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password", 64)

    override val primaryKey = PrimaryKey(id)
}
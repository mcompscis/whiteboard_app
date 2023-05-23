/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.persistence

import cs.shared.project.persistence.model.UserData
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


object UserDataDao {
    fun validate(name: String, password: String): UUID? {
        var result: ResultRow? = null
        transaction {
            result = UserData.select((UserData.username eq name) and (UserData.password eq password)).firstOrNull()
        }
        return result?.get(UserData.id)?.value
    }

    fun insert(name: String, password: String): Boolean {
        transaction {
            UserData.insertAndGetId {
                it[username] = name
                it[UserData.password] = password
            }
        }
        return true
    }
}
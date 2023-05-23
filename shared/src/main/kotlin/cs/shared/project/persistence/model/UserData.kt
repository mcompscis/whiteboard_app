/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.persistence.model

import org.jetbrains.exposed.dao.id.UUIDTable

object UserData : UUIDTable("User") {
    val username = varchar("username", 50)
    val password = varchar("password", 50)
}
/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.persistence

import cs.shared.project.persistence.model.ClientPersistence
import cs.shared.project.persistence.model.LastSessionPersistence
import cs.shared.project.persistence.model.UserData
import cs.shared.project.persistence.model.WhiteboardData
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Paths

class Connect {

    fun dbConnect() {
        // create database if it doesn't already exist
        Files.createDirectories(Paths.get("database/"))
        Database.connect("jdbc:sqlite:database/whiteboardapp.db")

        // everything happens in a single transaction context
        transaction {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            // create a table that reflects our table structure
            SchemaUtils.create(ClientPersistence)
            SchemaUtils.create(WhiteboardData)
            SchemaUtils.create(UserData)
            SchemaUtils.create(LastSessionPersistence)

            if (UserDataDao.validate("test", "test") == null) {
                UserDataDao.insert("test", "test")
            }
        }
    }
}

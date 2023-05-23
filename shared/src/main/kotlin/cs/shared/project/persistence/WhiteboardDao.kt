/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.persistence

import cs.shared.project.persistence.model.Whiteboard
import cs.shared.project.persistence.model.WhiteboardData
import cs.shared.project.persistence.model.WhiteboardData.toWhiteboard
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


object WhiteboardDao {

    fun getWhiteboardById(id: Int): Whiteboard? {
        return transaction {
            WhiteboardData.select { WhiteboardData.id eq id }
                .map { it.toWhiteboard() }.firstOrNull()
        }
    }

    fun getAll(): List<Whiteboard> {
        return transaction {
            WhiteboardData.selectAll().map { it.toWhiteboard() }
        }
    }

    fun addOrUpdateWhiteboard(whiteboard: Whiteboard): Whiteboard {
        return transaction {
            var id = whiteboard.id
            if (id == null) {
                id = WhiteboardData.insertAndGetId {
                    it[jsonString] = whiteboard.exportDataToJson()
                    it[name] = whiteboard.name
                }.value

                WhiteboardData.update({ WhiteboardData.id eq id }) {
                    it[name] = whiteboard.name + "_" + id
                    it[jsonString] = whiteboard.exportDataToJson()
                }.run { WhiteboardData.select { WhiteboardData.id eq id }.first() }.toWhiteboard()
            } else {
                WhiteboardData.update({ WhiteboardData.id eq id }) {
                    it[name] = whiteboard.name
                    it[jsonString] = whiteboard.exportDataToJson()
                }.run { WhiteboardData.select { WhiteboardData.id eq id }.first() }.toWhiteboard()
            }
        }
    }
}
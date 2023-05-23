/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.persistence.model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow

object WhiteboardData : IntIdTable("Whiteboard") {
    val name = varchar("name", 50)
    val jsonString = text("jsonString")

    fun ResultRow.toWhiteboard(): Whiteboard {
        val whiteboard = Whiteboard()
        whiteboard.id = this[id].value
        whiteboard.name = this[name]
        whiteboard.importDataFromJson(this[jsonString])
        return whiteboard
    }
}

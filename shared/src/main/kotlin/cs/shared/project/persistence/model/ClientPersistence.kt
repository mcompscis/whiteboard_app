/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.persistence.model

import cs.shared.project.presentation.model.DisplayModel
import javafx.scene.paint.Color
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.util.*

object ClientPersistence : Table() {
    val id = integer("id").autoIncrement()
    val selectedToolType = varchar("tool_type", length = 50)
    val selectedLineWidth = varchar("width_type", length = 50)
    val selectedLineColor = varchar("line_color", length = 50)
    val selectedWindowHeight = double("selected_height")
    val selectedWindowWidth = double("selected_width")
    val selectedWindowPositionX = double("selected_x")
    val selectedWindowPositionY = double("selected_y")
    val selectedWhiteboardIds = varchar("selected_whiteboards", length = 200)
    val ownerId = varchar("owner_id", length = 36)
    override val primaryKey = PrimaryKey(id)

    fun ResultRow.toDisplayModel() {
        val selectedToolType = this[selectedToolType].let { toolType ->
            Whiteboard.ToolType.valueOf(toolType)
        }
        val selectedLineWidth = this[selectedLineWidth]
        val selectedLineColor = this[selectedLineColor].let { lineColor ->
            Color.web(lineColor)
        }
        DisplayModel.loadData(
            selectedToolType,
            selectedLineWidth,
            selectedLineColor,
            this[selectedWindowHeight],
            this[selectedWindowWidth],
            this[selectedWindowPositionX],
            this[selectedWindowPositionY],
            UUID.fromString(this[ownerId]),
            this[selectedWhiteboardIds]
        )
    }
}

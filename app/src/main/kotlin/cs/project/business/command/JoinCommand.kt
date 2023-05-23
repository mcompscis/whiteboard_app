/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.business.command

import cs.project.business.interfaces.ICommand
import cs.project.presentation.controller.DrawingPaneController
import cs.shared.project.model.JoinMessage
import cs.shared.project.persistence.model.ShapeDataModel
import cs.shared.project.presentation.model.DisplayModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JoinCommand(private val args: Array<Any>?) : ICommand {
    override fun execute() {
        DisplayModel.socketSession?.send(
            "/app/join", JoinMessage(
                id = args?.get(0) as Int?,
                userId = DisplayModel.currentUserId,
                cursorString = Json.encodeToString(DrawingPaneController.createCursor() as ShapeDataModel),
                author = DisplayModel.socketSession!!.sessionId,
            )
        )
    }
}
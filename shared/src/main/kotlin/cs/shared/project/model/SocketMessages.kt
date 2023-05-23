/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.model

import java.util.*

// for testing purposes
data class StringMessage(val data: String? = null)

// clients sends this message when their cursors change
data class CursorUpdateMessage(val author: String? = null, val x: Double? = null, val y: Double? = null)

// server broadcasts this to clients when a client sends in their cursor update
data class CursorUpdateBroadcastMessage(
    val shapeId: String? = null,
    val x: Double? = null,
    val y: Double? = null,
    val author: String? = null,
)

// for a new user joining a whiteboard
data class JoinMessage(
    val id: Int? = null,
    val userId: UUID? = null,
    val cursorString: String? = null,
    val author: String? = null
)

// for a user leaving a whiteboard
data class LeaveMessage(val author: String? = null)

// to remove cursor for a user leaving a whiteboard
data class DeleteCursorMessage(val shapeId: String? = null, val author: String? = null)

// request to fetch list of whiteboards
data class ListWhiteboardsRequest(val author: String? = null)

// response with fetched list of whiteboards
data class ListWhiteboardsResponse(val author: String? = null, val whiteboardsString: String? = null)

// rename whiteboard message
data class RenameWhiteboardMessage(val author: String? = null, val newName: String? = null)

// WhiteboardDataMessage -> for initial join's return
data class WhiteboardMessage(
    val whiteboardString: String? = null,
    val cursorMapString: String? = null,
    val author: String? = null,
)

data class NewJoinerMessage(
    val author: String? = null,
    val newCursorString: String? = null,
)

// for a user creating a new shape in a whiteboard
data class NewShapeMessage(val author: String? = null, val shapeString: String? = null)

// for a user deleting a shape from a whiteboard
data class EraseShapeMessage(val author: String? = null, val shapeId: String? = null)

// for a user editing a shape from a whiteboard
data class EditShapeMessage(val author: String? = null, val shapeId: String? = null, val shapeString: String? = null)

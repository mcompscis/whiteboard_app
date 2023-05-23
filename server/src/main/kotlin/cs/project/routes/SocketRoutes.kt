/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.routes

import cs.project.ServerDataStore
import cs.shared.project.model.*
import cs.shared.project.persistence.WhiteboardDao
import cs.shared.project.persistence.model.CircleDataModel
import cs.shared.project.persistence.model.ShapeDataModel
import cs.shared.project.persistence.model.Whiteboard
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.messaging.handler.annotation.*
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.RestController

@RestController
class SocketRoutes(private val messagingTemplate: SimpMessagingTemplate) {
    private val lock = Any()

    @MessageMapping("/join")
    @SendTo("/topic/join")
    @Synchronized
    fun handleJoin(@Payload message: JoinMessage, @Header("simpSessionId") sessionId: String) {
        synchronized(lock) {
            println("join request from client with session: $sessionId for whiteboard: ${message.id}")

            var whiteboardId: Int? = message.id
            if (message.id == null) {
                val whiteboard =
                    WhiteboardDao.addOrUpdateWhiteboard(Whiteboard(name = "new_whiteboard"))
                whiteboardId = whiteboard.id!!
                ServerDataStore.whiteboardIdToWhiteboard[whiteboard.id!!] = whiteboard
                ServerDataStore.whiteboardIdToCursorMap[whiteboard.id!!] = HashMap()
            } else if (!ServerDataStore.whiteboardIdToWhiteboard.contains(message.id)) {
                ServerDataStore.whiteboardIdToWhiteboard[message.id!!] = WhiteboardDao.getWhiteboardById(message.id!!)!!
                ServerDataStore.whiteboardIdToCursorMap[message.id!!] = HashMap()
            }

            // assigning random colored cursor sent by client to new joiner
            ServerDataStore.whiteboardIdToCursorMap[whiteboardId]!![message.author!!] =
                Json.decodeFromString(message.cursorString!!)

            val whiteboardMessage = WhiteboardMessage(
                whiteboardString = Json.encodeToString(ServerDataStore.whiteboardIdToWhiteboard[whiteboardId]!!),
                cursorMapString = Json.encodeToString(ServerDataStore.whiteboardIdToCursorMap[whiteboardId]!!),
                author = message.author,
            )

            // broadcast to other users on the whiteboard that a new user joined
            val newJoinerMessage = NewJoinerMessage(
                author = message.author,
                newCursorString = Json.encodeToString(ServerDataStore.whiteboardIdToCursorMap[whiteboardId]!![message.author!!]),
            )
            messagingTemplate.convertAndSend("/topic/new-joiner/${whiteboardId}", newJoinerMessage)

            // returning the current whiteboard state to the new user
            val headerAccessor: SimpMessageHeaderAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE)
            headerAccessor.sessionId = sessionId
            headerAccessor.setLeaveMutable(true)
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/topic/join",
                whiteboardMessage,
                headerAccessor.messageHeaders
            )
        }
    }

    @MessageMapping("/list-whiteboards")
    @SendTo("/topic/list-whiteboards")
    @Synchronized
    fun handleListWhiteboards(@Payload message: ListWhiteboardsRequest, @Header("simpSessionId") sessionId: String) {
        synchronized(lock) {
            val whiteboards = WhiteboardDao.getAll()
            val headerAccessor: SimpMessageHeaderAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE)
            headerAccessor.sessionId = sessionId
            headerAccessor.setLeaveMutable(true)
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/topic/list-whiteboards",
                ListWhiteboardsResponse(author = message.author, whiteboardsString = Json.encodeToString(whiteboards)),
                headerAccessor.messageHeaders
            )
        }
    }

    @MessageMapping("/leave/{whiteboardId}")
    @SendTo("/topic/leave/{whiteboardId}")
    @Synchronized
    fun handleLeave(@DestinationVariable whiteboardId: String, @Payload message: LeaveMessage) {
        synchronized(lock) {
            println("Leaving whiteboard $whiteboardId")
            // save whiteboard to DB
            WhiteboardDao.addOrUpdateWhiteboard(ServerDataStore.whiteboardIdToWhiteboard[whiteboardId.toInt()]!!)

            // remove cursor from whiteboard and relay to other users on the whiteboard
            val cursorDataModel =
                ServerDataStore.whiteboardIdToCursorMap[whiteboardId.toInt()]!![message.author!!] as CircleDataModel

            messagingTemplate.convertAndSend(
                "/topic/leave/$whiteboardId",
                DeleteCursorMessage(shapeId = cursorDataModel.id, author = message.author)
            )

            ServerDataStore.whiteboardIdToCursorMap[whiteboardId.toInt()]!!.remove(message.author!!)
        }
    }

    @MessageMapping("/new-shape/{whiteboardId}")
    @SendTo("/topic/new-shape/{whiteboardId}")
    @Synchronized
    fun handleNewShape(@DestinationVariable whiteboardId: String, @Payload message: NewShapeMessage) {
        synchronized(lock) {
            val shape = Json.decodeFromString<ShapeDataModel>(message.shapeString!!)
            ServerDataStore.whiteboardIdToWhiteboard[whiteboardId.toInt()]!!.shapes.add(shape)
            messagingTemplate.convertAndSend("/topic/new-shape/$whiteboardId", message)
        }
    }

    @MessageMapping("/erase-shape/{whiteboardId}")
    @SendTo("/topic/erase-shape/{whiteboardId}")
    @Synchronized
    fun handleEraseShape(@DestinationVariable whiteboardId: String, @Payload message: EraseShapeMessage) {
        synchronized(lock) {
            ServerDataStore.whiteboardIdToWhiteboard[whiteboardId.toInt()]!!.shapes.removeIf { it.id == message.shapeId }
            messagingTemplate.convertAndSend("/topic/erase-shape/$whiteboardId", message)
        }
    }

    @MessageMapping("/edit-shape/{whiteboardId}")
    @SendTo("/topic/edit-shape/{whiteboardId}")
    @Synchronized
    fun handleEraseShape(@DestinationVariable whiteboardId: String, @Payload message: EditShapeMessage) {
        synchronized(lock) {
            // delete old version of shape
            ServerDataStore.whiteboardIdToWhiteboard[whiteboardId.toInt()]!!.shapes.removeIf { it.id == message.shapeId }
            // add new version of shape
            val shape = Json.decodeFromString<ShapeDataModel>(message.shapeString!!)
            ServerDataStore.whiteboardIdToWhiteboard[whiteboardId.toInt()]!!.shapes.add(shape)
            // broadcast
            messagingTemplate.convertAndSend("/topic/edit-shape/$whiteboardId", message)
        }
    }


    @MessageMapping("/cursor/{whiteboardId}")
    @SendTo("/topic/cursor/{whiteboardId}")
    @Synchronized
    fun handleCursor(
        @DestinationVariable whiteboardId: String,
        @Payload message: CursorUpdateMessage,
        @Header("simpSessionId") sessionId: String
    ) {
        synchronized(lock) {
            val cursorDataModel =
                ServerDataStore.whiteboardIdToCursorMap[whiteboardId.toInt()]!![message.author!!] as CircleDataModel
            cursorDataModel.centerX = message.x!!
            cursorDataModel.centerY = message.y!!
            messagingTemplate.convertAndSend(
                "/topic/cursor/$whiteboardId",
                CursorUpdateBroadcastMessage(
                    shapeId = cursorDataModel.id,
                    x = message.x,
                    y = message.y,
                    author = message.author
                )
            )
        }
    }

    @MessageMapping("/rename-whiteboard/{whiteboardId}")
    @SendTo("/topic/rename-whiteboard/{whiteboardId}")
    @Synchronized
    fun handleRenameWhiteboard(
        @DestinationVariable whiteboardId: String,
        @Payload message: RenameWhiteboardMessage,
        @Header("simpSessionId") sessionId: String
    ) {
        synchronized(lock) {
            // update whiteboard name
            ServerDataStore.whiteboardIdToWhiteboard[whiteboardId.toInt()]!!.name = message.newName!!

            // save whiteboard to DB
            WhiteboardDao.addOrUpdateWhiteboard(ServerDataStore.whiteboardIdToWhiteboard[whiteboardId.toInt()]!!)

            // broadcast to other users
            messagingTemplate.convertAndSend("/topic/rename-whiteboard/$whiteboardId", message)
        }
    }
}
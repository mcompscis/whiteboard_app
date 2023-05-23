/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.business

import cs.shared.project.model.*
import cs.shared.project.persistence.model.ShapeDataModel
import cs.shared.project.persistence.model.Whiteboard
import cs.shared.project.presentation.model.DisplayModel
import javafx.application.Platform
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.lang.Nullable
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.*
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type

class ClientSocket {
    init {
        val client = WebSocketStompClient(StandardWebSocketClient())

        client.messageConverter = MappingJackson2MessageConverter()
        client.connect(
            "ws://${serverUrl}:8080/whiteboard/websocket", object : StompSessionHandlerAdapter() {
                override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
                    println("Connected to server")

                    // broadcast listener
                    session.subscribe("/topic/join", object : StompFrameHandler {
                        override fun handleFrame(headers: StompHeaders, payload: Any?) {
                            println("join message relayed from server: ${payload?.toString()}")
                            println("whiteboard ID: ${(payload as JoinMessage).id}")
                        }

                        override fun getPayloadType(headers: StompHeaders): Type {
                            return JoinMessage::class.java
                        }
                    })

                    // broadcast listener for list of whiteboards vs. single user response to save fetching delays
                    session.subscribe("/user/topic/list-whiteboards", object : StompFrameHandler {
                        override fun handleFrame(headers: StompHeaders, payload: Any?) {
                            DisplayModel.setListWhiteboards(Json.decodeFromString((payload as ListWhiteboardsResponse).whiteboardsString!!))
                        }

                        override fun getPayloadType(headers: StompHeaders): Type {
                            return ListWhiteboardsResponse::class.java
                        }
                    })

                    // user specific listener
                    session.subscribe("/user/topic/join", object : StompFrameHandler {
                        override fun handleFrame(headers: StompHeaders, payload: Any?) {
                            val whiteboard =
                                Json.decodeFromString<Whiteboard>((payload as WhiteboardMessage).whiteboardString!!)
                            val cursorMap =
                                Json.decodeFromString<HashMap<String, ShapeDataModel>>(payload.cursorMapString!!)
                            Platform.runLater { // to let javafx thread run this
                                DisplayModel.joinTab(
                                    whiteboard = whiteboard,
                                    cursorMap = cursorMap,
                                    author = payload.author!!
                                )
                            }

                            // new shape topic for this whiteboard
                            session.subscribe("/topic/new-shape/" + whiteboard.id, object : StompFrameHandler {
                                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                                    Platform.runLater {
                                        val shapeDataModel =
                                            Json.decodeFromString<ShapeDataModel>((payload as NewShapeMessage).shapeString!!)
                                        if (payload.author != session.sessionId) {
                                            DisplayModel.addShape(
                                                whiteboardId = whiteboard.id!!,
                                                shapeDataModel = shapeDataModel
                                            )
                                        }
                                    }
                                }

                                override fun getPayloadType(headers: StompHeaders): Type {
                                    return NewShapeMessage::class.java
                                }
                            })

                            // erase shape topic for this whiteboard
                            session.subscribe("/topic/erase-shape/" + whiteboard.id, object : StompFrameHandler {
                                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                                    Platform.runLater {
                                        if ((payload as EraseShapeMessage).author != session.sessionId) {
                                            DisplayModel.removeShape(
                                                whiteboardId = whiteboard.id!!,
                                                shapeId = payload.shapeId!!
                                            )
                                        }
                                    }
                                }

                                override fun getPayloadType(headers: StompHeaders): Type {
                                    return EraseShapeMessage::class.java
                                }
                            })

                            // edit shape topic for this whiteboard
                            session.subscribe("/topic/edit-shape/" + whiteboard.id, object : StompFrameHandler {
                                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                                    Platform.runLater {
                                        val shapeDataModel =
                                            Json.decodeFromString<ShapeDataModel>((payload as EditShapeMessage).shapeString!!)
                                        val shapeId = payload.shapeId!!
                                        if (payload.author != session.sessionId) {
                                            DisplayModel.editShape(
                                                whiteboardId = whiteboard.id!!,
                                                shapeId = shapeId,
                                                shapeDataModel = shapeDataModel
                                            )
                                        }
                                    }
                                }

                                override fun getPayloadType(headers: StompHeaders): Type {
                                    return EditShapeMessage::class.java
                                }
                            })


                            // user cursor update topic for this whiteboard
                            session.subscribe("/topic/cursor/" + whiteboard.id, object : StompFrameHandler {
                                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                                    Platform.runLater {
                                        if ((payload as CursorUpdateBroadcastMessage).author != session.sessionId) {
                                            DisplayModel.updateCursor(
                                                whiteboardId = whiteboard.id!!,
                                                shapeId = payload.shapeId!!,
                                                x = payload.x!!,
                                                y = payload.y!!,
                                            )
                                        }
                                    }
                                }

                                override fun getPayloadType(headers: StompHeaders): Type {
                                    return CursorUpdateBroadcastMessage::class.java
                                }
                            })

                            // new joiner topic for this whiteboard
                            session.subscribe("/topic/new-joiner/" + whiteboard.id, object : StompFrameHandler {
                                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                                    Platform.runLater {
                                        val cursorShapeModel =
                                            Json.decodeFromString<ShapeDataModel>((payload as NewJoinerMessage).newCursorString!!)
                                        if (payload.author != session.sessionId) {
                                            DisplayModel.addNewCursor(
                                                whiteboardId = whiteboard.id!!,
                                                newCursorShapeModel = cursorShapeModel
                                            )
                                        }
                                    }
                                }

                                override fun getPayloadType(headers: StompHeaders): Type {
                                    return NewJoinerMessage::class.java
                                }
                            })

                            // delete cursor for leaving user for this whiteboard
                            session.subscribe("/topic/leave/" + whiteboard.id, object : StompFrameHandler {
                                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                                    Platform.runLater {
                                        if ((payload as DeleteCursorMessage).author != session.sessionId) {
                                            DisplayModel.deleteCursor(
                                                whiteboardId = whiteboard.id!!,
                                                shapeId = payload.shapeId!!,
                                            )
                                        }
                                    }
                                }

                                override fun getPayloadType(headers: StompHeaders): Type {
                                    return DeleteCursorMessage::class.java
                                }
                            })

                            // rename the given whiteboard if you are not the author
                            session.subscribe("/topic/rename-whiteboard/" + whiteboard.id, object : StompFrameHandler {
                                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                                    Platform.runLater {
                                        if ((payload as RenameWhiteboardMessage).author != session.sessionId) {
                                            DisplayModel.renameWhiteboard(
                                                whiteboardId = whiteboard.id!!,
                                                newName = payload.newName!!
                                            )
                                        }
                                    }
                                }

                                override fun getPayloadType(headers: StompHeaders): Type {
                                    return RenameWhiteboardMessage::class.java
                                }
                            })
                        }

                        override fun getPayloadType(headers: StompHeaders): Type {
                            return WhiteboardMessage::class.java
                        }
                    })

                    // storing socket session in display model
                    DisplayModel.socketSession = session
                }

                override fun handleException(
                    session: StompSession,
                    @Nullable command: StompCommand?,
                    headers: StompHeaders,
                    payload: ByteArray,
                    exception: Throwable
                ) {
                    println("Error: ${exception.message}")
                }

                override fun handleTransportError(session: StompSession, exception: Throwable) {
                    println("Error: ${exception.message}")
                }

                override fun handleFrame(headers: StompHeaders, @Nullable payload: Any?) {
                    println("Received unknown message from server: ${payload?.toString()}")
                }
            })

    }

    companion object {
        const val serverUrl = "ec2-3-90-219-152.compute-1.amazonaws.com"
        fun sendEditShapeMessage(shapeId: String, updatedShapeDataModel: ShapeDataModel) {
            DisplayModel.socketSession?.send(
                "/app/edit-shape/" + DisplayModel.currSelectedWhiteboard.id,
                EditShapeMessage(
                    author = DisplayModel.socketSession?.sessionId,
                    shapeId = shapeId,
                    shapeString = Json.encodeToString(updatedShapeDataModel),
                )
            )
        }

        fun sendEraseShapeMessage(shapeId: String) {
            DisplayModel.socketSession?.send(
                "/app/erase-shape/" + DisplayModel.currSelectedWhiteboard.id,
                EraseShapeMessage(
                    author = DisplayModel.socketSession?.sessionId,
                    shapeId = shapeId,
                )
            )
        }

        fun sendNewShapeMessage(shapeDataModel: ShapeDataModel) {
            DisplayModel.socketSession?.send(
                "/app/new-shape/" + DisplayModel.currSelectedWhiteboard.id,
                NewShapeMessage(
                    author = DisplayModel.socketSession?.sessionId,
                    shapeString = Json.encodeToString(shapeDataModel),
                )
            )
        }

        // helps the client gracefully leave all whiteboards
        fun gracefulQuit() {
            try { // wrapping within try-catch block so that connection-refused or server errors don't prevent closing app
                DisplayModel.whiteboards.forEach {
                    DisplayModel.socketSession?.send(
                        "/app/leave/${it.id}",
                        LeaveMessage(author = DisplayModel.socketSession!!.sessionId)
                    )
                }
            } catch (e: Exception) {
                println(e.stackTraceToString())
            }
        }

        fun sendWhiteboardRenameMessage(whiteboardId: Int, newName: String) {
            DisplayModel.socketSession?.send(
                "/app/rename-whiteboard/$whiteboardId",
                RenameWhiteboardMessage(
                    author = DisplayModel.socketSession?.sessionId,
                    newName = newName,
                )
            )
        }
    }
}
/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project

import cs.shared.project.model.JoinMessage
import cs.shared.project.model.StringMessage
import cs.shared.project.model.WhiteboardMessage
import cs.shared.project.persistence.model.Whiteboard
import org.springframework.lang.Nullable
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.*
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type

fun main() {
    val client = WebSocketStompClient(StandardWebSocketClient())

    client.messageConverter = MappingJackson2MessageConverter()
    client.connect("ws://localhost:8080/whiteboard/websocket", object : StompSessionHandlerAdapter() {
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

            // user specific listener
            session.subscribe("/user/topic/join", object : StompFrameHandler {
                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    println("unique join message relayed from server: ${payload?.toString()}")
                    val whiteboard = Whiteboard()
                    whiteboard.importDataFromJson((payload as WhiteboardMessage).whiteboardString!!)
                    println("whiteboard tool: ${whiteboard.currSelectedShape}")
                    println("whiteboard list of shapes: ${whiteboard.shapes}")
                }

                override fun getPayloadType(headers: StompHeaders): Type {
                    return WhiteboardMessage::class.java
                }
            })

            session.subscribe("/topic/new-shape", object : StompFrameHandler {
                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    println("new shape handler - message from server: ${payload?.toString()}")
                    println("message data: ${(payload as StringMessage).data}")
                }

                override fun getPayloadType(headers: StompHeaders): Type {
                    return StringMessage::class.java
                }
            })

            session.subscribe("/topic/move-shape", object : StompFrameHandler {
                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    println("move shape handler - message from server: ${payload?.toString()}")
                    println("message data: ${(payload as StringMessage).data}")
                }

                override fun getPayloadType(headers: StompHeaders): Type {
                    return StringMessage::class.java
                }
            })

            session.subscribe("/topic/erase-shape", object : StompFrameHandler {
                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    println("erase shape handler - message from server: ${payload?.toString()}")
                    println("message data: ${(payload as StringMessage).data}")
                }

                override fun getPayloadType(headers: StompHeaders): Type {
                    return StringMessage::class.java
                }
            })

            println("sending messages to server")
            session.send("/app/join", JoinMessage(1))
            session.send("/app/new-shape", StringMessage("Hello, server! - I am drawing a new shape"))
            session.send("/app/move-shape", StringMessage("Hello, server - pls move this shape"))
            session.send("/app/erase-shape", StringMessage("Hello, server - pls erase this shape"))
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

    // Wait for the user to press Enter before disconnecting
    readlnOrNull()

    client.stop()
}

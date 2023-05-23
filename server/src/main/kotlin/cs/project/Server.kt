/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project

import cs.shared.project.persistence.Connect
import cs.shared.project.persistence.model.ShapeDataModel
import cs.shared.project.persistence.model.Whiteboard
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class Server

// TODO: handle logic for removing whiteboard from store and persisting
//  it to db when the last user leaves
object ServerDataStore {
    // mapping whiteboard IDs to whiteboard data models
    val whiteboardIdToWhiteboard = HashMap<Int, Whiteboard>()

    // mapping whiteboard IDs to cursor map (Socket ID String -> CircleDataModel for the cursor)
    val whiteboardIdToCursorMap = HashMap<Int, HashMap<String, ShapeDataModel>>()
}

object ServerLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val connect = Connect()
        connect.dbConnect()

        runApplication<Server>(*args)
    }
}

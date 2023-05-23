/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.DaoTests

import cs.shared.project.persistence.Connect
import cs.shared.project.persistence.WhiteboardDao
import cs.shared.project.persistence.model.CircleDataModel
import cs.shared.project.persistence.model.RectangleDataModel
import cs.shared.project.persistence.model.Whiteboard
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class WhiteboardTest {

    @BeforeEach
    fun setup() {
        val connect = Connect()
        connect.dbConnect()
    }

    @AfterEach
    fun cleanup() {
        val file = File("database/whiteboardapp.db")
        if (file.exists()) {
            file.delete()
        }
    }

    @Test
    fun canAddWhiteboard() {
        val returnedWhiteboard = WhiteboardDao.addOrUpdateWhiteboard(Whiteboard(name = "my whiteboard"))
        // returned whiteboards when creating a new whiteboard have whiteboard_name + "_id"
        assert(returnedWhiteboard.name == "my whiteboard_" + returnedWhiteboard.id)
    }


    @Test
    fun canGetCreatedWhiteboard() {
        val returnedWhiteboard = WhiteboardDao.addOrUpdateWhiteboard(
            Whiteboard(
                name = "created whiteboard",
                shapes = arrayListOf(CircleDataModel(radius = 10.0), RectangleDataModel(width = 10.0, height = 10.0))
            )
        )
        val getWhiteboard = WhiteboardDao.getWhiteboardById(returnedWhiteboard.id!!)
        assert(getWhiteboard!!.name == returnedWhiteboard.name)
        assert(getWhiteboard.id == returnedWhiteboard.id)
        assert(getWhiteboard!!.shapes.size == 2)
        assert(getWhiteboard!!.shapes[0].shapeType == Whiteboard.ToolType.CIRCLE)
        assert(getWhiteboard!!.shapes[1].shapeType == Whiteboard.ToolType.RECTANGLE)
    }

    @Test
    fun canUpdateCreatedWhiteboard() {
        val returnedWhiteboard = WhiteboardDao.addOrUpdateWhiteboard(
            Whiteboard(
                name = "created whiteboard",
                shapes = arrayListOf(CircleDataModel(radius = 10.0), RectangleDataModel(width = 10.0, height = 10.0))
            )
        )
        val updatedWhiteboard =
            WhiteboardDao.addOrUpdateWhiteboard(
                Whiteboard(
                    id = returnedWhiteboard.id, name = "edited whiteboard",
                    shapes = arrayListOf(CircleDataModel(radius = 10.0))
                )
            )
        assert(updatedWhiteboard!!.name == "edited whiteboard")
        assert(updatedWhiteboard.id == returnedWhiteboard.id)
        assert(updatedWhiteboard.shapes.size == 1)
    }

    @Test
    fun canGetWhiteboardAfterUpdatingWhiteboard() {
        val returnedWhiteboard = WhiteboardDao.addOrUpdateWhiteboard(
            Whiteboard(
                name = "created whiteboard",
                shapes = arrayListOf(CircleDataModel(radius = 10.0), RectangleDataModel(width = 10.0, height = 10.0))
            )
        )
        val updatedWhiteboard =
            WhiteboardDao.addOrUpdateWhiteboard(
                Whiteboard(
                    id = returnedWhiteboard.id, name = "edited whiteboard",
                    shapes = arrayListOf(CircleDataModel(radius = 10.0))
                )
            )
        val getWhiteboard = WhiteboardDao.getWhiteboardById(updatedWhiteboard.id!!)
        assert(getWhiteboard!!.name == "edited whiteboard")
        assert(getWhiteboard.id == returnedWhiteboard.id)
        assert(getWhiteboard.shapes.size == 1)

    }


}
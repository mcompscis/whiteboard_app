/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.presentation.model

import cs.shared.project.persistence.model.ShapeDataModel
import cs.shared.project.persistence.model.Whiteboard
import cs.shared.project.presentation.interfaces.EventType
import cs.shared.project.presentation.interfaces.IObserver
import javafx.scene.paint.Color
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.messaging.simp.stomp.StompSession
import java.util.*

object DisplayModel {

    var selectedToolType: Whiteboard.ToolType = Whiteboard.ToolType.PEN
        set(value) {
            field = value
            notifyObservers(EventType.TOOL_SELECTED)
        }

    var selectedLineWidth: String = "thin"
        set(value) {
            field = value
            notifyObservers(EventType.LINE_WIDTH_CHANGED)
        }

    var selectedLineColor: Color = Color.BLACK
        set(value) {
            field = value
            notifyObservers(EventType.COLOR_CHANGED)
        }

    var currentUserId: UUID? = null
    var selectedFontFamily: String? = null
        set(value) {
            field = value
            notifyObservers(EventType.FONT_FAMILY_CHANGED)
        }

    var selectedFontSize: Double? = null
        set(value) {
            field = value
            notifyObservers(EventType.FONT_SIZE_CHANGED)
        }

    var selectedBold: Boolean? = null
        set(value) {
            field = value
            notifyObservers(EventType.FONT_BOLDED)
        }

    var selectedItalics: Boolean? = null
        set(value) {
            field = value
            notifyObservers(EventType.FONT_ITALICIZED)
        }

    lateinit var currSelectedWhiteboard: Whiteboard

    var whiteboards = ArrayList<Whiteboard>()
    var selectedWindowWidth: Double = 700.0
    var selectedWindowHeight: Double = 700.0
    var selectedWindowPositionX: Double = 100.0
    var selectedWindowPositionY: Double = 300.0
    var loadUpX: Double = 100.0
    var loadUpY: Double = 300.0

    private val tabObservers: ArrayList<IObserver> = ArrayList()
    private val dataObservers: ArrayList<IObserver> = ArrayList()
    var socketSession: StompSession? = null

    fun resetData() {
        selectedToolType = Whiteboard.ToolType.PEN
        selectedLineWidth = "thin"
        selectedLineColor = Color.BLACK
        selectedWindowHeight = 700.0
        selectedWindowWidth = 700.0
        selectedWindowPositionX = 100.0
        selectedWindowPositionY = 300.0
        currentUserId = null
        whiteboards.clear()

        notifyObservers(EventType.TOOL_SELECTED)
        notifyObservers(EventType.RESET_DATA)
    }

    fun loadData(
        toolType: Whiteboard.ToolType,
        lineWidth: String,
        lineColor: Color,
        selectedHeight: Double,
        selectedWidth: Double,
        selectedX: Double,
        selectedY: Double,
        currenUser: UUID,
        selectedWhiteboards: String
    ) {
        selectedToolType = toolType
        selectedLineWidth = lineWidth
        selectedLineColor = lineColor
        selectedWindowPositionX = selectedX
        selectedWindowPositionY = selectedY
        loadUpX = selectedX
        loadUpY = selectedY
        selectedWindowHeight = selectedHeight
        selectedWindowWidth = selectedWidth
        currentUserId = currenUser

        notifyObservers(EventType.TOOL_SELECTED)
        notifyObservers(EventType.LOAD_DATA, arrayOf(selectedWhiteboards))
    }

    fun setup() {
        notifyObservers(EventType.LOAD_DATA, arrayOf(""))
    }

    fun addView(observer: IObserver, isDataObserver: Boolean) {
        if (isDataObserver) {
            dataObservers.add(observer)
        } else {
            tabObservers.add(observer)
        }
    }

    private fun notifyObservers(eventType: EventType, args: Array<Any>? = null) {
        val dataObserverEvents = arrayOf(
            EventType.TOOL_SELECTED,
            EventType.COLOR_CHANGED,
            EventType.LINE_WIDTH_CHANGED,
            EventType.DELETE_CURR_SELECTED_SHAPE,
            EventType.COPY_CURR_SELECTED_SHAPE,
            EventType.PASTE_CURR_COPIED_SHAPE,
            EventType.SHAPE_ADDED,
            EventType.SHAPE_EDITED,
            EventType.SHAPE_REMOVED,
            EventType.CURSOR_MAP,
            EventType.CURSOR_UPDATE,
            EventType.DELETE_CURSOR,
            EventType.NEW_JOINER,
            EventType.FONT_FAMILY_CHANGED,
            EventType.FONT_SIZE_CHANGED,
            EventType.FONT_BOLDED,
            EventType.FONT_ITALICIZED,
            EventType.LIST_WHITEBOARDS,
        )
        var observers = if (eventType in dataObserverEvents) {
            dataObservers
        } else {
            tabObservers
        }

        for (observer in observers) {
            observer.update(eventType, args)
        }
    }

    fun joinTab(whiteboard: Whiteboard, cursorMap: HashMap<String, ShapeDataModel>, author: String) {
        notifyObservers(EventType.JOIN_TAB, arrayOf(whiteboard))
        notifyObservers(EventType.CURSOR_MAP, arrayOf(whiteboard.id!!, cursorMap, author))
    }

    fun focusTab(whiteboardId: Int) {
        notifyObservers(EventType.FOCUS_TAB, arrayOf(whiteboardId))
    }

    fun addShape(whiteboardId: Int, shapeDataModel: ShapeDataModel) {
        println("Add Shape RECEIVED:")
        println(Json.encodeToString(shapeDataModel))
        notifyObservers(EventType.SHAPE_ADDED, arrayOf(whiteboardId, shapeDataModel))
    }

    fun editShape(whiteboardId: Int, shapeId: String, shapeDataModel: ShapeDataModel) {
        notifyObservers(EventType.SHAPE_EDITED, arrayOf(whiteboardId, shapeId, shapeDataModel))
    }


    fun removeShape(whiteboardId: Int, shapeId: String) {
        notifyObservers(EventType.SHAPE_REMOVED, arrayOf(whiteboardId, shapeId))
    }

    fun addNewCursor(whiteboardId: Int, newCursorShapeModel: ShapeDataModel) {
        notifyObservers(EventType.NEW_JOINER, arrayOf(whiteboardId, newCursorShapeModel))
    }

    fun updateCursor(whiteboardId: Int, shapeId: String, x: Double, y: Double) {
        notifyObservers(EventType.CURSOR_UPDATE, arrayOf(whiteboardId, shapeId, x, y))
    }

    fun deleteCursor(whiteboardId: Int, shapeId: String) {
        notifyObservers(EventType.DELETE_CURSOR, arrayOf(whiteboardId, shapeId))
    }

    fun removeTab() {
        notifyObservers(EventType.REMOVE_TAB)
    }

    fun deleteCurrentSelectedShape() {
        notifyObservers(EventType.DELETE_CURR_SELECTED_SHAPE)
    }

    fun copyCurrentSelectedShape() {
        notifyObservers(EventType.COPY_CURR_SELECTED_SHAPE)
    }

    fun pasteCurrCopiedShape() {
        notifyObservers(EventType.PASTE_CURR_COPIED_SHAPE)
    }

    fun renameTab() {
        notifyObservers(EventType.RENAME_TAB)
    }

    fun setListWhiteboards(whiteboards: List<Whiteboard>) {
        notifyObservers(EventType.LIST_WHITEBOARDS, arrayOf(whiteboards))
    }

    fun renameWhiteboard(whiteboardId: Int, newName: String) {
        notifyObservers(EventType.RENAME_WHITEBOARD, arrayOf(whiteboardId, newName))
    }
}

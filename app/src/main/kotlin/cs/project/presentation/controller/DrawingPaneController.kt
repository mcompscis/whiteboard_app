/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import cs.project.business.ClientSocket
import cs.project.presentation.controller.ShapeDrawingHandler.Companion.getStrokeWidthFromLineWidth
import cs.project.presentation.controller.TextDrawingHandler.Companion.updateFontBold
import cs.project.presentation.controller.TextDrawingHandler.Companion.updateFontColor
import cs.project.presentation.controller.TextDrawingHandler.Companion.updateFontFamily
import cs.project.presentation.controller.TextDrawingHandler.Companion.updateFontItalic
import cs.project.presentation.controller.TextDrawingHandler.Companion.updateFontSize
import cs.shared.project.model.CursorUpdateMessage
import cs.shared.project.persistence.model.*
import cs.shared.project.presentation.interfaces.EventType
import cs.shared.project.presentation.interfaces.IObserver
import cs.shared.project.presentation.model.DisplayModel
import javafx.fxml.FXML
import javafx.scene.Cursor
import javafx.scene.control.TextArea
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Shape
import java.util.*
import kotlin.random.Random


interface DrawingPaneControllerDependencies {
    fun getMainPane(): Pane
}

class DrawingPaneController : DrawingPaneControllerDependencies, IObserver {

    private val whiteboardShapeEditEvents = arrayOf(
        EventType.COLOR_CHANGED,
        EventType.LINE_WIDTH_CHANGED,
        EventType.FONT_FAMILY_CHANGED,
        EventType.FONT_SIZE_CHANGED,
        EventType.FONT_BOLDED,
        EventType.FONT_ITALICIZED,
    )


    private val whiteboardShapeChangeEvents =
        whiteboardShapeEditEvents.plusElement(EventType.DELETE_CURR_SELECTED_SHAPE)
    private val whiteboardShapeCopyPasteEvents =
        arrayOf(EventType.COPY_CURR_SELECTED_SHAPE, EventType.PASTE_CURR_COPIED_SHAPE)

    @FXML
    private lateinit var pane: Pane

    private lateinit var whiteboard: Whiteboard

    private var toolTypeToHandler: MutableMap<Whiteboard.ToolType, ShapeDrawingHandler> = mutableMapOf(
        Whiteboard.ToolType.CIRCLE to CircleDrawingHandler(),
        Whiteboard.ToolType.RECTANGLE to RectangleDrawingHandler(),
        Whiteboard.ToolType.LINE to LineDrawingHandler(),
        Whiteboard.ToolType.PEN to FreeformDrawingHandler(),
        Whiteboard.ToolType.TEXT to TextDrawingHandler(),
        Whiteboard.ToolType.ERASER to EraserToolHandler(),
    )

    companion object {
        private val listOfShapes = listOf(
            Whiteboard.ToolType.CIRCLE,
            Whiteboard.ToolType.RECTANGLE,
            Whiteboard.ToolType.LINE,
            Whiteboard.ToolType.PEN,
            Whiteboard.ToolType.TEXT,
        )

        // creates a circular cursor with random color used for new users joining a whiteboard
        fun createCursor(): CircleDataModel {
            val cursor = Circle(0.0, 0.0, 5.0)
            cursor.id = UUID.randomUUID().toString()
            cursor.fill = Color.rgb(
                Random.nextInt(0, 256),
                Random.nextInt(0, 256),
                Random.nextInt(0, 256)
            )
            cursor.stroke = Color.BLACK
            cursor.isMouseTransparent = true
            return CircleDataModel(cursor)
        }
    }

    constructor()

    @FXML
    fun initialize() {
        // Need to start full drag from pane so that drag events during erase
        // can be detected by the shape nodes (since the drag can originate from outside the shape node)
        pane.addEventHandler(MouseEvent.DRAG_DETECTED) { _ ->
            this.pane.startFullDrag()
        }

        // deselect current selected shape if a mouse press happens in pane that doesn't press on any child shape nodes
        pane.addEventHandler(MouseEvent.MOUSE_PRESSED) { event ->
            if (DisplayModel.selectedToolType == Whiteboard.ToolType.SELECT) {
                for (child in pane.children) {
                    if (child.contains(child.sceneToLocal(event.sceneX, event.sceneY))) {
                        return@addEventHandler
                    }
                }
                deselectCurrSelectedShape()
            }
        }

        pane.addEventHandler(MouseEvent.MOUSE_MOVED) { event ->
            // sending message to server with this client's live cursor position
            sendCursorUpdateMessage(event)

            if (DisplayModel.selectedToolType == Whiteboard.ToolType.SELECT) {
                for (child in pane.children) {
                    if (child.contains(child.sceneToLocal(event.sceneX, event.sceneY))) {
                        return@addEventHandler
                    }
                }
                pane.cursor = Cursor.DEFAULT
            }
        }

        pane.addEventHandler(MouseEvent.MOUSE_DRAGGED) { event ->
            // sending message to server with this client's live cursor position
            sendCursorUpdateMessage(event)
        }
    }

    private fun sendCursorUpdateMessage(event: MouseEvent) {
        // TODO: remove this outer check once each whiteboard is a joined whiteboard
        if (DisplayModel.currSelectedWhiteboard.id != null) {
            DisplayModel.socketSession?.send(
                "/app/cursor/" + DisplayModel.currSelectedWhiteboard.id,
                CursorUpdateMessage(author = DisplayModel.socketSession!!.sessionId, x = event.x, y = event.y)
            )
        }
    }


    override fun getMainPane(): Pane {
        return pane
    }

    fun setWhiteboardViewModel(whiteboardModel: Whiteboard, render: Boolean = false) {
        whiteboard = whiteboardModel
        toolTypeToHandler.values.forEach { handler ->
            handler.setWhiteboardViewModel(whiteboardModel)
            handler.setDrawingPane(this.pane)
        }

        if (render) {
            for (shape in whiteboard.shapes) {
                val s = shape.fxNode
                toolTypeToHandler[shape.shapeType]?.renderNode(s)
            }
        }
    }

    @FXML
    fun handleMousePressed(event: MouseEvent) {
        toolTypeToHandler[DisplayModel.selectedToolType]?.startDrawing(this, event)
    }

    @FXML
    fun handleMouseDragged(event: MouseEvent) {
        toolTypeToHandler[DisplayModel.selectedToolType]?.continueDrawing(event)
    }

    @FXML
    fun handleMouseReleased() {
        toolTypeToHandler[DisplayModel.selectedToolType]?.endDrawing()
    }

    private fun deselectCurrSelectedShape() {
        val currSelectedShape = pane.children.find { node -> node.id == whiteboard.currSelectedShape?.id }
        currSelectedShape?.effect = null
        whiteboard.currSelectedShape = null
    }

    private fun deleteCurrSelectedShape() {
        whiteboard.currSelectedShape?.let { toolTypeToHandler[it.shapeType]!!.eraseNode(it.fxNode) }
        whiteboard.currSelectedShape = null
    }

    private fun copyCurrSelectedShape() {
        whiteboard.currCopiedShape = whiteboard.currSelectedShape
    }

    private fun pasteCurrCopiedShape() {
        whiteboard.currCopiedShape ?: return
        val clonedShapeDataModel = whiteboard.currCopiedShape!!.clone()
        clonedShapeDataModel.translateX = clonedShapeDataModel.translateX + 15.0
        clonedShapeDataModel.translateY = clonedShapeDataModel.translateY + 15.0
        whiteboard.shapes.add(clonedShapeDataModel)
        toolTypeToHandler[clonedShapeDataModel.shapeType]?.renderNode(clonedShapeDataModel.fxNode)
        ClientSocket.sendNewShapeMessage(clonedShapeDataModel)
    }


    // TODO: clean this code
    override fun update(eventType: EventType, args: Array<Any>?) {
        if (eventType == EventType.SHAPE_ADDED && whiteboard.id == args!![0]) {
            val shapeDataModel = (args[1] as ShapeDataModel)
            toolTypeToHandler[shapeDataModel.shapeType]?.renderNode(shapeDataModel.fxNode)
            whiteboard.shapes.add(shapeDataModel)
        } else if (eventType == EventType.SHAPE_EDITED && whiteboard.id == args!![0]) {
            // remove old shape
            val shapeId = args[1] as String
            pane.children.removeIf { shapeId == it.id }
            whiteboard.shapes.removeIf { shapeId == it.id }

            // add updated shape
            val shapeDataModel = (args[2] as ShapeDataModel)
            toolTypeToHandler[shapeDataModel.shapeType]?.renderNode(shapeDataModel.fxNode)
            whiteboard.shapes.add(shapeDataModel)
        } else if (eventType == EventType.SHAPE_REMOVED && whiteboard.id == args!![0]) {
            val shapeId = args[1] as String
            pane.children.removeIf { shapeId == it.id }
            whiteboard.shapes.removeIf { shapeId == it.id }
        } else if (eventType == EventType.CURSOR_MAP && whiteboard.id == args!![0]) {
            val cursorMap = args[1] as HashMap<String, ShapeDataModel>
            val author = args[2] as String
            cursorMap.filter { it.key != author }.forEach { (_, cursorShape) ->
                // since these cursors don't have any click handlers, we don't use renderNode
                pane.children.add(cursorShape.fxNode)
            }
        } else if (eventType == EventType.CURSOR_UPDATE && whiteboard.id == args!![0]) {
            // update cursor for given cursor shape ID sent by server
            val cursor = pane.children.find { it.id == (args[1] as String) } as Circle
            cursor.centerX = (args[2] as Double)
            cursor.centerY = (args[3] as Double)
        } else if (eventType == EventType.NEW_JOINER && whiteboard.id == args!![0]) {
            val shapeDataModel = (args[1] as ShapeDataModel)
            pane.children.add(shapeDataModel.fxNode)
        } else if (eventType == EventType.DELETE_CURSOR && whiteboard.id == args!![0]) {
            // remove cursor for of user who left
            pane.children.removeIf { it.id == (args[1] as String) }
        } else if (eventType == EventType.TOOL_SELECTED) {
            when (DisplayModel.selectedToolType) {
                Whiteboard.ToolType.SELECT -> pane.cursor = Cursor.DEFAULT
                in listOfShapes -> {
                    deselectCurrSelectedShape()
                    pane.cursor = Cursor.CROSSHAIR
                }

                else -> {
                    deselectCurrSelectedShape()
                    pane.cursor = Cursor.DEFAULT
                }
            }
        } else if (eventType in whiteboardShapeChangeEvents
            && DisplayModel.selectedToolType == Whiteboard.ToolType.SELECT
            && DisplayModel.currSelectedWhiteboard === whiteboard
        ) {
            val currSelectedShape =
                (pane.children.find { node -> node.id == whiteboard.currSelectedShape?.id }) ?: return

            if (eventType in whiteboardShapeEditEvents) {
                if (currSelectedShape is Shape) {
                    handleEventsForShape(currSelectedShape, eventType)
                } else if (currSelectedShape is TextArea) {
                    handleEventsForText(currSelectedShape, eventType)
                }
                // update the server, so it can update clients to edit the shape in their whiteboards
                ClientSocket.sendEditShapeMessage(whiteboard.currSelectedShape!!.id, whiteboard.currSelectedShape!!)
            } else if (eventType == EventType.DELETE_CURR_SELECTED_SHAPE) {
                deleteCurrSelectedShape()
            }
        } else if (eventType in whiteboardShapeCopyPasteEvents
            && DisplayModel.selectedToolType == Whiteboard.ToolType.SELECT
            && DisplayModel.currSelectedWhiteboard === whiteboard
        ) {
            if (eventType == EventType.COPY_CURR_SELECTED_SHAPE) {
                whiteboard.currSelectedShape ?: return
                copyCurrSelectedShape()
            } else if (eventType == EventType.PASTE_CURR_COPIED_SHAPE) {
                whiteboard.currCopiedShape ?: return
                pasteCurrCopiedShape()
            }
        }
    }

    private fun handleEventsForText(textArea: TextArea, eventType: EventType) {
        when (eventType) {
            EventType.COLOR_CHANGED -> {
                updateFontColor(textArea, DisplayModel.selectedLineColor)
                (whiteboard.currSelectedShape as TextDataModel).style = textArea.style
            }

            EventType.FONT_FAMILY_CHANGED -> {
                updateFontFamily(textArea, DisplayModel.selectedFontFamily!!)
                (whiteboard.currSelectedShape as TextDataModel).style = textArea.style
            }

            EventType.FONT_SIZE_CHANGED -> {
                updateFontSize(textArea, DisplayModel.selectedFontSize!!)
                (whiteboard.currSelectedShape as TextDataModel).style = textArea.style
            }

            EventType.FONT_BOLDED -> {
                updateFontBold(textArea, DisplayModel.selectedBold!!)
                (whiteboard.currSelectedShape as TextDataModel).style = textArea.style
            }

            EventType.FONT_ITALICIZED -> {
                updateFontItalic(textArea, DisplayModel.selectedItalics!!)
                (whiteboard.currSelectedShape as TextDataModel).style = textArea.style
            }

            else -> {}
        }
    }

    private fun handleEventsForShape(currSelectedShape: Shape, eventType: EventType) {
        when (eventType) {
            EventType.LINE_WIDTH_CHANGED -> {
                currSelectedShape.strokeWidth = getStrokeWidthFromLineWidth()
                whiteboard.currSelectedShape?.strokeWidth = currSelectedShape.strokeWidth
            }

            EventType.COLOR_CHANGED -> {
                currSelectedShape.stroke = DisplayModel.selectedLineColor
                whiteboard.currSelectedShape?.stroke = SerializableColor(DisplayModel.selectedLineColor)
            }

            else -> {}
        }
    }
}
/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import cs.project.business.ClientSocket
import cs.project.business.command.CommandFactory
import cs.project.business.command.CommandTypes
import cs.project.presentation.controller.TextDrawingHandler.Companion.getRGBCodeFromColor
import cs.shared.project.persistence.model.ShapeDataModel
import cs.shared.project.persistence.model.TextDataModel
import cs.shared.project.persistence.model.Whiteboard
import cs.shared.project.presentation.model.DisplayModel
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.scene.effect.DropShadow
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseDragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Polyline
import javafx.scene.shape.Shape
import java.util.*

abstract class ShapeDrawingHandler {
    var shape: Shape? = null
    lateinit var shapeDataModel: ShapeDataModel
    private var isResizing = false

    // Below properties used for dragging shapes
    private var originalSceneX = 0.0
    private var originalSceneY = 0.0
    private var originalTranslateX = 0.0
    private var originalTranslateY = 0.0

    protected lateinit var whiteboard: Whiteboard
    protected lateinit var pane: Pane

    // below enum tells us which anchor we are resizing a shape from
    enum class ResizingAnchor {
        // below 8 are for rectangle
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        TOP_LEFT,
        BOTTOM_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,

        // below two are for line
        START,
        END,

        // below one is for circle
        CIRCLE_EDGE,

        // below used for no resizing anchor
        NONE
    }

    protected var resizingAnchor: ResizingAnchor = ResizingAnchor.NONE
    fun setDrawingPane(drawingPane: Pane) {
        pane = drawingPane
    }

    fun setWhiteboardViewModel(whiteboardModel: Whiteboard) {
        whiteboard = whiteboardModel
    }

    fun renderNode(node: Node) {
        pane.children.add(node)
        setNodeDefaultFunctionalities(node)
    }

    fun setShapeDefaultProperties() {
        shape!!.fill = Color.TRANSPARENT
        shape!!.stroke = DisplayModel.selectedLineColor
        shape!!.strokeWidth = getStrokeWidthFromLineWidth()
        setNodeDefaultProperties(shape!!)
    }

    fun setTextDefaultProperties(textArea: TextArea) {
        setTextPropertiesBasedOnDisplayModel(textArea)
        setNodeDefaultProperties(textArea)
    }

    private fun setTextPropertiesBasedOnDisplayModel(textArea: TextArea) {
        var style = ""
        style += "-fx-font-family: ${DisplayModel.selectedFontFamily};"
        style += " -fx-text-fill: ${getRGBCodeFromColor(DisplayModel.selectedLineColor)};"
        if (DisplayModel.selectedItalics == true) {
            style += " -fx-font-style: italic;"
        }
        if (DisplayModel.selectedBold == true) {
            style += " -fx-font-weight: bold;"
        }
        style += " -fx-font-size: ${DisplayModel.selectedFontSize}px"
        textArea.style = style.trimIndent()
    }

    companion object {
        fun getStrokeWidthFromLineWidth(): Double {
            return when (DisplayModel.selectedLineWidth) {
                "thin" -> 1.0
                "medium" -> 3.0
                "thick" -> 6.0
                else -> 1.0
            }
        }
    }

    private fun setNodeDefaultProperties(node: Node) {
        node.id = UUID.randomUUID().toString()
        setNodeDefaultFunctionalities(node)
    }

    private fun setNodeDefaultFunctionalities(node: Node) {
        if (node is TextArea) {
            setTextDefaultFunctionalities(node)
        }
        makeNodeDraggable(node)
        makeNodeErasable(node)
        highlightShapeOnClick(node)
        if (node !is Polyline && node !is TextArea) { // these two are not resizable
            makeNodeResizable(node)
        }
    }

    private fun setTextDefaultFunctionalities(textArea: TextArea) {
        setTextChangeListener(textArea)
        setTextAreaSelectListener(textArea)
        setTextAreaDeleteListener(textArea)
    }

    private fun setTextChangeListener(textArea: TextArea) {
        textArea.textProperty().addListener { _, _, newVal ->
            var textAreaDataModel =
                (whiteboard.shapes.find { whiteboardShape -> whiteboardShape.id == textArea.id } as TextDataModel)
            textAreaDataModel.textVal = newVal
            // update the server, so it can update clients to edit the shape in their whiteboards
            ClientSocket.sendEditShapeMessage(textAreaDataModel.id, textAreaDataModel)
        }
    }

    private fun setTextAreaSelectListener(textArea: TextArea) {
        // event filter to intercept mouse events before they reach the TextArea to handle selection logic for textarea
        //  if SELECT tool, single click on text area puts text area in selected state and double click leads to
        //  edit mode (which is default behaviour otherwise)
        textArea.addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
            if (DisplayModel.selectedToolType == Whiteboard.ToolType.SELECT) {
                if (event.clickCount == 1) {
                    textArea.isEditable = false
                } else if (event.clickCount == 2) {
                    textArea.isEditable = true
                }
            }
        }
    }

    private fun setTextAreaDeleteListener(textArea: TextArea) {
        textArea.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            // only delete text area if it's in selected state (in non-editable state)
            if (event.code == KeyCode.BACK_SPACE && !textArea.isEditable) {
                CommandFactory.createFromArgs(CommandTypes.DELETE).execute()
            }
        }
    }

    open fun isMouseOnNodeEdge(node: Node, event: MouseEvent): Boolean {
        // return false by default for shapes like pen and text box which aren't resizable
        // and don't override this function
        return false
    }

    // this super class method should be called at the end of the subclass method
    private fun resizeShapeOnMouseEvent(shape: Shape, event: MouseEvent) {
        doResizeShapeOnMouseEvent(shape, event)
        // update the server, so it can update clients to edit the shape in their whiteboards
        ClientSocket.sendEditShapeMessage(shapeDataModel.id, shapeDataModel)
    }

    open fun doResizeShapeOnMouseEvent(shape: Shape, event: MouseEvent) {
    }

    private fun makeNodeResizable(node: Node) {

        // below event handler is cursor logic for resizability
        node.addEventHandler(MouseEvent.MOUSE_MOVED) { event ->
            if (DisplayModel.selectedToolType == Whiteboard.ToolType.SELECT) {
                if (isMouseOnNodeEdge(node, event)) {
                    when (resizingAnchor) {
                        ResizingAnchor.TOP -> pane.cursor = Cursor.N_RESIZE
                        ResizingAnchor.BOTTOM -> pane.cursor = Cursor.S_RESIZE
                        ResizingAnchor.RIGHT -> pane.cursor = Cursor.E_RESIZE
                        ResizingAnchor.LEFT -> pane.cursor = Cursor.W_RESIZE
                        ResizingAnchor.TOP_LEFT -> pane.cursor = Cursor.NW_RESIZE
                        ResizingAnchor.TOP_RIGHT -> pane.cursor = Cursor.NE_RESIZE
                        ResizingAnchor.BOTTOM_LEFT -> pane.cursor = Cursor.SW_RESIZE
                        ResizingAnchor.BOTTOM_RIGHT -> pane.cursor = Cursor.SE_RESIZE
                        else -> pane.cursor = Cursor.E_RESIZE
                    }
                } else {
                    pane.cursor = Cursor.OPEN_HAND
                }
            }
        }

        // below event handlers perform actual resizing logic
        node.addEventHandler(MouseEvent.MOUSE_PRESSED) { event ->
            if (DisplayModel.selectedToolType == Whiteboard.ToolType.SELECT && isMouseOnNodeEdge(node, event)) {
                onStartResizing(node, event)
                isResizing = true
            }
        }

        node.addEventHandler(MouseEvent.MOUSE_DRAGGED) { event ->
            if (DisplayModel.selectedToolType == Whiteboard.ToolType.SELECT && isResizing) {
                resizeShapeOnMouseEvent(node as Shape, event)
            }
        }

        node.addEventHandler(MouseEvent.MOUSE_RELEASED) { _ ->
            isResizing = false
            resizingAnchor = ResizingAnchor.NONE
        }
    }

    private fun handleSelectNode(node: Node) {
        if (DisplayModel.selectedToolType == Whiteboard.ToolType.SELECT) {
            if (whiteboard.currSelectedShape != null) {
                val oldSelectedShape = pane.children.find { it.id == whiteboard.currSelectedShape?.id }
                oldSelectedShape?.effect = null
            }
            whiteboard.currSelectedShape = whiteboard.shapes.find { shape -> node.id == shape.id }
            val dropShadow = DropShadow()
            dropShadow.color = Color.BLUE
            node.effect = dropShadow
        }
    }

    private fun highlightShapeOnClick(node: Node) {
        // clicking on text areas does not trigger MOUSE_PRESSED, so we need a CLICK handler for that
        if (node is Shape) {
            node.addEventHandler(MouseEvent.MOUSE_PRESSED) { _ -> handleSelectNode(node) }
        } else if (node is TextArea) {
            node.addEventHandler(MouseEvent.MOUSE_CLICKED) { _ -> handleSelectNode(node) }
        }
    }

    private fun makeNodeDraggable(node: Node) {
        if (node is TextArea) {
            // For TextArea:
            //  The event handlers to move it around need to be attached on the TextArea's content and the
            //      transformations need to be applied on the TextArea itself
            //  And for the content to be available, initialization needs to be complete. Thus, we attach a
            //      change listener to widthProperty() as that is only set when the initialization is completed
            node.widthProperty().addListener { _, _, _ ->
                val textAreaContent = node.lookup(".content")
                addMakeDraggableEventHandlers(nodeToAttachHandlersTo = textAreaContent, nodeToApplyDragsOn = node)
            }
        } else {
            addMakeDraggableEventHandlers(nodeToAttachHandlersTo = node, nodeToApplyDragsOn = node)
        }
    }

    private fun addMakeDraggableEventHandlers(nodeToAttachHandlersTo: Node, nodeToApplyDragsOn: Node) {
        // NOTE:  Use addEventHandler(MouseEvent.{MOUSE_EVENT}) instead of setOn{MouseEvent}
        // With setOn{MouseEvent}, you can only attach a single event.
        // Whenever you add a new setOn{MouseEvent}, it replaces the previous attached functionality
        // Whereas, addEventHandler can attach multiple handlers for the same mouse event.
        nodeToAttachHandlersTo.addEventHandler(MouseEvent.MOUSE_PRESSED) { event ->
            if (DisplayModel.selectedToolType == Whiteboard.ToolType.SELECT && !isResizing) {
                originalTranslateX = nodeToApplyDragsOn.translateX
                originalTranslateY = nodeToApplyDragsOn.translateY
                originalSceneX = event.sceneX
                originalSceneY = event.sceneY
            }
        }

        nodeToAttachHandlersTo.addEventHandler(MouseEvent.MOUSE_DRAGGED) { event ->
            if (DisplayModel.selectedToolType == Whiteboard.ToolType.SELECT && !isResizing) {
                nodeToApplyDragsOn.translateX = event.sceneX - originalSceneX + originalTranslateX
                nodeToApplyDragsOn.translateY = event.sceneY - originalSceneY + originalTranslateY

                // Set updated shape data in model
                val movedShape = whiteboard.shapes.find { shape -> shape.id == nodeToApplyDragsOn.id }
                movedShape!!.translateX = nodeToApplyDragsOn.translateX
                movedShape.translateY = nodeToApplyDragsOn.translateY

                // update the server, so it can update clients to edit the shape in their whiteboards
                ClientSocket.sendEditShapeMessage(movedShape.id, movedShape)
            }
        }
    }

    fun eraseNode(node: Node) {
        // remove node from drawing pane
        pane.children.removeIf { it.id == node.id }
        // remove shape from data model
        whiteboard.shapes.removeIf { shape -> shape.id == node.id }
        // update the server, so it can update clients to remove the shape from their whiteboards
        ClientSocket.sendEraseShapeMessage(node.id)
    }

    private fun makeNodeErasable(node: Node) {
        node.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (DisplayModel.selectedToolType == Whiteboard.ToolType.ERASER) {
                eraseNode(node)
            }
        }
        node.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED) {
            if (DisplayModel.selectedToolType == Whiteboard.ToolType.ERASER) {
                eraseNode(node)
            }
        }
    }

    fun startDrawing(drawingPaneControllerDependencies: DrawingPaneControllerDependencies, event: MouseEvent) {
        pane = drawingPaneControllerDependencies.getMainPane()
        doStartDrawing(event)
    }

    fun addDataModelToWhiteboardAndSendNewShapeMsg() {
        whiteboard.shapes.add(shapeDataModel)
        ClientSocket.sendNewShapeMessage(shapeDataModel)
    }

    open fun doStartDrawing(event: MouseEvent) {
    }

    open fun continueDrawing(event: MouseEvent) {
    }

    fun sendEditShapeMessage() {
        ClientSocket.sendEditShapeMessage(shapeDataModel.id, shapeDataModel)
    }

    fun endDrawing() {
        doEndDrawing()
        shape = null
    }

    open fun doEndDrawing() {
    }

    // optional method to override that executes at the start of resizing
    open fun onStartResizing(node: Node, event: MouseEvent) {
    }
}

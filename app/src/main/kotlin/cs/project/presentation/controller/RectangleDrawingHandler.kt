/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import cs.shared.project.persistence.model.RectangleDataModel
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import kotlin.math.abs

class RectangleDrawingHandler() : ShapeDrawingHandler() {
    private var startX: Double = 0.0
    private var startY: Double = 0.0

    override fun isMouseOnNodeEdge(node: Node, event: MouseEvent): Boolean {
        var rectangle = (node as Rectangle)
        val width = rectangle.width
        val height = rectangle.height
        val threshold = 6.0

        resizingAnchor = if ((abs(event.x - rectangle.x) <= threshold) && (abs(event.y - rectangle.y) <= threshold)) {
            ResizingAnchor.TOP_LEFT
        } else if ((abs(event.x - rectangle.x) <= threshold)
            && (abs(event.y - (rectangle.y + rectangle.height)) <= threshold)
        ) {
            ResizingAnchor.BOTTOM_LEFT
        } else if ((abs(event.y - rectangle.y) <= threshold)
            && (abs(event.x - (rectangle.x + rectangle.width)) <= threshold)
        ) {
            ResizingAnchor.TOP_RIGHT
        } else if ((abs(event.y - (rectangle.y + rectangle.height)) <= threshold)
            && (abs(event.x - (rectangle.x + rectangle.width)) <= threshold)
        ) {
            ResizingAnchor.BOTTOM_RIGHT
        } else if ((event.x >= rectangle.x && event.x <= rectangle.x + width)
            && (abs(event.y - rectangle.y) <= threshold)
        ) {
            ResizingAnchor.TOP
        } else if ((event.x >= rectangle.x && event.x <= rectangle.x + width)
            && (abs(event.y - (rectangle.y + rectangle.height)) <= threshold)
        ) {
            ResizingAnchor.BOTTOM
        } else if ((event.y >= rectangle.y && event.y <= rectangle.y + height)
            && (abs(event.x - rectangle.x) <= threshold)
        ) {
            ResizingAnchor.LEFT
        } else if ((event.y >= rectangle.y && event.y <= rectangle.y + height)
            && (abs(event.x - (rectangle.x + width)) <= threshold)
        ) {
            ResizingAnchor.RIGHT
        } else {
            ResizingAnchor.NONE
        }
        return resizingAnchor != ResizingAnchor.NONE
    }

    override fun doStartDrawing(event: MouseEvent) {
        startX = event.x
        startY = event.y
        shape = Rectangle(event.x, event.y, 0.0, 0.0)
        setShapeDefaultProperties()
        pane.children.add(shape)
        shapeDataModel = RectangleDataModel(shape as Rectangle)
        addDataModelToWhiteboardAndSendNewShapeMsg()
    }

    override fun continueDrawing(event: MouseEvent) {
        val rectangle = shape as Rectangle
        val width = abs(event.x - startX)
        val height = abs(event.y - startY)
        rectangle.x = if (event.x < startX) event.x else startX
        rectangle.y = if (event.y < startY) event.y else startY
        rectangle.width = width
        rectangle.height = height
        (shapeDataModel as RectangleDataModel).x = rectangle.x
        (shapeDataModel as RectangleDataModel).y = rectangle.y
        (shapeDataModel as RectangleDataModel).width = rectangle.width
        (shapeDataModel as RectangleDataModel).height = rectangle.height
        sendEditShapeMessage()
    }

    override fun doResizeShapeOnMouseEvent(shape: Shape, event: MouseEvent) {
        val rectangle = (shape as Rectangle)
        val deltaX = event.x - startX
        val deltaY = event.y - startY
        startX = event.x
        startY = event.y
        when (resizingAnchor) {
            ResizingAnchor.RIGHT -> {
                if (rectangle.width + deltaX <= 0) {
                    resizeFromLeftAnchor(rectangle, deltaX)
                } else {
                    resizeFromRightAnchor(rectangle, deltaX)
                }
            }

            ResizingAnchor.LEFT -> {
                if (rectangle.width - deltaX <= 0) {
                    resizeFromRightAnchor(rectangle, deltaX)
                } else {
                    resizeFromLeftAnchor(rectangle, deltaX)
                }
            }

            ResizingAnchor.TOP -> {
                if (rectangle.height - deltaY <= 0) {
                    resizeFromBottomAnchor(rectangle, deltaY)
                } else {
                    resizeFromTopAnchor(rectangle, deltaY)
                }
            }

            ResizingAnchor.BOTTOM -> {
                if (rectangle.height + deltaY <= 0) {
                    resizeFromTopAnchor(rectangle, deltaY)
                } else {
                    resizeFromBottomAnchor(rectangle, deltaY)
                }
            }

            ResizingAnchor.BOTTOM_LEFT -> {
                if (rectangle.width - deltaX <= 0 && rectangle.height + deltaY <= 0) {
                    resizeFromTopRightAnchor(rectangle, deltaX, deltaY)
                } else if (rectangle.width - deltaX <= 0) {
                    resizeFromBottomRightAnchor(rectangle, deltaX, deltaY)
                } else if (rectangle.height + deltaY <= 0) {
                    resizeFromTopLeftAnchor(rectangle, deltaX, deltaY)
                } else {
                    resizeFromBottomLeftAnchor(rectangle, deltaX, deltaY)
                }
            }

            ResizingAnchor.TOP_LEFT -> {
                if (rectangle.width - deltaX <= 0 && rectangle.height - deltaY <= 0) {
                    resizeFromBottomRightAnchor(rectangle, deltaX, deltaY)
                } else if (rectangle.width - deltaX <= 0) {
                    resizeFromTopRightAnchor(rectangle, deltaX, deltaY)
                } else if (rectangle.height - deltaY <= 0) {
                    resizeFromBottomLeftAnchor(rectangle, deltaX, deltaY)
                } else {
                    resizeFromTopLeftAnchor(rectangle, deltaX, deltaY)
                }
            }

            ResizingAnchor.BOTTOM_RIGHT -> {
                if (rectangle.width + deltaX <= 0 && rectangle.height + deltaY <= 0) {
                    resizeFromTopLeftAnchor(rectangle, deltaX, deltaY)
                } else if (rectangle.width + deltaX <= 0) {
                    resizeFromBottomLeftAnchor(rectangle, deltaX, deltaY)
                } else if (rectangle.height + deltaY <= 0) {
                    resizeFromTopRightAnchor(rectangle, deltaX, deltaY)
                } else {
                    resizeFromBottomRightAnchor(rectangle, deltaX, deltaY)
                }
            }

            ResizingAnchor.TOP_RIGHT -> {
                if (rectangle.width + deltaX <= 0 && rectangle.height - deltaY <= 0) {
                    resizeFromBottomLeftAnchor(rectangle, deltaX, deltaY)
                } else if (rectangle.width + deltaX <= 0) {
                    resizeFromTopLeftAnchor(rectangle, deltaX, deltaY)
                } else if (rectangle.height - deltaY <= 0) {
                    resizeFromBottomRightAnchor(rectangle, deltaX, deltaY)
                } else {
                    resizeFromTopRightAnchor(rectangle, deltaX, deltaY)
                }
            }

            else -> {}
        }

        // save changes to data model
        shapeDataModel =
            (whiteboard.shapes.find { whiteboardShape -> whiteboardShape.id == rectangle.id }!!)
        (shapeDataModel as RectangleDataModel).x = rectangle.x
        (shapeDataModel as RectangleDataModel).y = rectangle.y
        (shapeDataModel as RectangleDataModel).width = rectangle.width
        (shapeDataModel as RectangleDataModel).height = rectangle.height
    }

    private fun resizeFromBottomAnchor(rectangle: Rectangle, deltaY: Double) {
        rectangle.height = rectangle.height + deltaY
        resizingAnchor = ResizingAnchor.BOTTOM
    }

    private fun resizeFromTopAnchor(rectangle: Rectangle, deltaY: Double) {
        rectangle.y = rectangle.y + deltaY
        rectangle.height = rectangle.height - deltaY
        resizingAnchor = ResizingAnchor.TOP
    }

    private fun resizeFromLeftAnchor(rectangle: Rectangle, deltaX: Double) {
        rectangle.x = rectangle.x + deltaX
        rectangle.width = rectangle.width - deltaX
        resizingAnchor = ResizingAnchor.LEFT
    }

    private fun resizeFromRightAnchor(rectangle: Rectangle, deltaX: Double) {
        rectangle.width = rectangle.width + deltaX
        resizingAnchor = ResizingAnchor.RIGHT
    }

    private fun resizeFromBottomRightAnchor(rectangle: Rectangle, deltaX: Double, deltaY: Double) {
        rectangle.width = rectangle.width + deltaX
        rectangle.height = rectangle.height + deltaY
        resizingAnchor = ResizingAnchor.BOTTOM_RIGHT
    }

    private fun resizeFromBottomLeftAnchor(rectangle: Rectangle, deltaX: Double, deltaY: Double) {
        rectangle.x = rectangle.x + deltaX
        rectangle.width = rectangle.width - deltaX
        rectangle.height = rectangle.height + deltaY
        resizingAnchor = ResizingAnchor.BOTTOM_LEFT
    }

    private fun resizeFromTopRightAnchor(rectangle: Rectangle, deltaX: Double, deltaY: Double) {
        rectangle.width = rectangle.width + deltaX
        rectangle.y = rectangle.y + deltaY
        rectangle.height = rectangle.height - deltaY
        resizingAnchor = ResizingAnchor.TOP_RIGHT
    }

    private fun resizeFromTopLeftAnchor(rectangle: Rectangle, deltaX: Double, deltaY: Double) {
        rectangle.x = rectangle.x + deltaX
        rectangle.width = rectangle.width - deltaX
        rectangle.y = rectangle.y + deltaY
        rectangle.height = rectangle.height - deltaY
        resizingAnchor = ResizingAnchor.TOP_LEFT
    }

    override fun onStartResizing(node: Node, event: MouseEvent) {
        startX = event.x
        startY = event.y
    }
}
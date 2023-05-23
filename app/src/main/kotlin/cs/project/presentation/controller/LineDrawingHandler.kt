/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import cs.shared.project.persistence.model.LineDataModel
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Line
import javafx.scene.shape.Shape

class LineDrawingHandler : ShapeDrawingHandler() {

    override fun isMouseOnNodeEdge(node: Node, event: MouseEvent): Boolean {
        var line = (node as Line)
        val threshold = 2
        val mousePoint = Point2D(event.x, event.y)
        val lineStartPoint = Point2D(line.startX, line.startY)
        val lineEndPoint = Point2D(line.endX, line.endY)
        resizingAnchor = if (lineStartPoint.distance(mousePoint) <= threshold) {
            ResizingAnchor.START
        } else if (lineEndPoint.distance(mousePoint) <= threshold) {
            ResizingAnchor.END
        } else {
            ResizingAnchor.NONE
        }
        return resizingAnchor != ResizingAnchor.NONE
    }

    override fun doStartDrawing(event: MouseEvent) {
        shape = Line(event.x, event.y, event.x, event.y)
        setShapeDefaultProperties()
        pane.children.add(shape)
        shapeDataModel = LineDataModel(shape as Line)
        addDataModelToWhiteboardAndSendNewShapeMsg()
    }

    override fun continueDrawing(event: MouseEvent) {
        val line = shape as Line
        line.endX = event.x
        line.endY = event.y
        (shapeDataModel as LineDataModel).endX = line.endX
        (shapeDataModel as LineDataModel).endY = line.endY
        sendEditShapeMessage()
    }

    override fun doResizeShapeOnMouseEvent(shape: Shape, event: MouseEvent) {
        val line = shape as Line
        if (resizingAnchor == ResizingAnchor.START) {
            line.startX = event.x
            line.startY = event.y
        } else if (resizingAnchor == ResizingAnchor.END) {
            line.endX = event.x
            line.endY = event.y
        }

        // save changes to data model
        shapeDataModel =
            (whiteboard.shapes.find { whiteboardShape -> whiteboardShape.id == line.id }!!)
        (shapeDataModel as LineDataModel).startX = line.startX
        (shapeDataModel as LineDataModel).startY = line.startY
        (shapeDataModel as LineDataModel).endX = line.endX
        (shapeDataModel as LineDataModel).endY = line.endY
    }
}
/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import cs.shared.project.persistence.model.CircleDataModel
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Circle
import javafx.scene.shape.Shape
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class CircleDrawingHandler : ShapeDrawingHandler() {
    override fun doStartDrawing(event: MouseEvent) {
        shape = Circle(event.x, event.y, 0.0)
        setShapeDefaultProperties()
        pane.children.add(shape)
        shapeDataModel = CircleDataModel(shape as Circle)
        addDataModelToWhiteboardAndSendNewShapeMsg()
    }

    override fun isMouseOnNodeEdge(node: Node, event: MouseEvent): Boolean {
        var circle = (node as Circle)
        val threshold = 2
        val mousePoint = Point2D(event.x, event.y)
        val circleCenter = Point2D(circle.centerX, circle.centerY)
        val distance = circleCenter.distance(mousePoint)
        resizingAnchor = if (abs(distance - circle.radius) <= threshold) {
            ResizingAnchor.CIRCLE_EDGE
        } else {
            ResizingAnchor.NONE
        }
        return resizingAnchor != ResizingAnchor.NONE
    }

    override fun doResizeShapeOnMouseEvent(shape: Shape, event: MouseEvent) {
        val circle = shape as Circle
        var newRadius = Point2D(event.x, event.y).distance(circle.centerX, circle.centerY)
        circle.radius = newRadius

        // save changes to data model
        shapeDataModel =
            (whiteboard.shapes.find { whiteboardShape -> whiteboardShape.id == circle.id }!!)
        (shapeDataModel as CircleDataModel).radius = circle.radius
    }

    override fun continueDrawing(event: MouseEvent) {
        val circle = (shape as Circle)
        val radius = sqrt(
            (event.x - circle.centerX).pow(2) + (event.y - circle.centerY).pow(2)
        )
        circle.radius = radius
        (shapeDataModel as CircleDataModel).radius = circle.radius
        sendEditShapeMessage()
    }
}
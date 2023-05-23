/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import cs.shared.project.persistence.model.PolylineDataModel
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Polyline

class FreeformDrawingHandler : ShapeDrawingHandler() {

    override fun doStartDrawing(event: MouseEvent) {
        shape = Polyline()
        (shape as Polyline).points.addAll(event.x, event.y)
        setShapeDefaultProperties()
        pane.children.add(shape)
        shapeDataModel = PolylineDataModel(shape as Polyline)
        addDataModelToWhiteboardAndSendNewShapeMsg()
    }

    override fun continueDrawing(event: MouseEvent) {
        (shape as Polyline).points.addAll(event.x, event.y)
        (shapeDataModel as PolylineDataModel).addPoint(event.x, event.y)
        sendEditShapeMessage()
    }
}
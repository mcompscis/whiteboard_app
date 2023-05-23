/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle

class EraserToolHandler : ShapeDrawingHandler() {
    override fun doStartDrawing(event: MouseEvent) {
        // add temporary circle to act as eraser cursor
        shape = Circle(event.x, event.y, 5.0)
        shape!!.fill = Color.TRANSPARENT
        shape!!.stroke = Color.BLACK
        shape!!.isMouseTransparent = true

        // disable default cursor and add eraser cursor to drawing pane
        pane.cursor = Cursor.NONE
        pane.children.add(shape)
    }

    override fun continueDrawing(event: MouseEvent) {
        val circle = (shape as Circle)
        circle.centerX = event.x
        circle.centerY = event.y
    }

    override fun doEndDrawing() {
        // enable default cursor and remove temporary circle for eraser cursor
        pane.cursor = Cursor.DEFAULT
        pane.children.remove(shape)
    }
}

/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import cs.shared.project.persistence.model.Whiteboard
import cs.shared.project.presentation.interfaces.EventType
import cs.shared.project.presentation.interfaces.IObserver
import cs.shared.project.presentation.model.DisplayModel
import javafx.fxml.FXML
import javafx.scene.control.Button


class ToolBarController : IObserver {
    lateinit var penButton: Button
    lateinit var rectangleButton: Button
    lateinit var circleButton: Button
    lateinit var lineButton: Button
    lateinit var textBoxButton: Button
    lateinit var selectButton: Button
    lateinit var eraseButton: Button
    private var selectedButton: Button? = null

    fun initialize() {
        update(EventType.TOOL_SELECTED)
    }

    @FXML
    fun onPenButtonPressed() {
        DisplayModel.selectedToolType = Whiteboard.ToolType.PEN
    }

    @FXML
    fun onRectangleButtonPressed() {
        DisplayModel.selectedToolType = Whiteboard.ToolType.RECTANGLE
    }

    @FXML
    fun onCircleButtonPressed() {
        DisplayModel.selectedToolType = Whiteboard.ToolType.CIRCLE
    }

    fun onLineButtonPressed() {
        DisplayModel.selectedToolType = Whiteboard.ToolType.LINE
    }

    fun onTextBoxButtonPressed() {
        DisplayModel.selectedToolType = Whiteboard.ToolType.TEXT
    }

    fun onSelectButtonPressed() {
        DisplayModel.selectedToolType = Whiteboard.ToolType.SELECT
    }

    fun onEraseButtonPressed() {
        DisplayModel.selectedToolType = Whiteboard.ToolType.ERASER
    }

    override fun update(eventType: EventType, args: Array<Any>?) {
        if (eventType == EventType.TOOL_SELECTED) {
            // Clear the style of the previously selected button
            selectedButton?.style = ""

            selectedButton = when (DisplayModel.selectedToolType) {
                Whiteboard.ToolType.PEN -> penButton
                Whiteboard.ToolType.RECTANGLE -> rectangleButton
                Whiteboard.ToolType.CIRCLE -> circleButton
                Whiteboard.ToolType.LINE -> lineButton
                Whiteboard.ToolType.TEXT -> textBoxButton
                Whiteboard.ToolType.SELECT -> selectButton
                Whiteboard.ToolType.ERASER -> eraseButton
            }

            // Set the style of the currently selected button
            selectedButton?.style = "-fx-background-color: lightgray;"
        }
    }
}
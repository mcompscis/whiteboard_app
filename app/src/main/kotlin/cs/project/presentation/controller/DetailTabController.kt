/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import cs.shared.project.presentation.interfaces.EventType
import cs.shared.project.presentation.interfaces.IObserver
import cs.shared.project.presentation.model.DisplayModel
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage


class DetailTabController : IObserver {

    @FXML
    private lateinit var colorPicker: ColorPicker

    @FXML
    private lateinit var lineWidthGroup: ToggleGroup

    @FXML
    private lateinit var thin: RadioButton

    @FXML
    private lateinit var medium: RadioButton

    @FXML
    private lateinit var thick: RadioButton

    @FXML
    private lateinit var fontFamilySelector: ChoiceBox<String>

    @FXML
    private lateinit var fontSizeSelector: Spinner<Double>

    @FXML
    lateinit var boldButton: ToggleButton

    @FXML
    lateinit var italicButton: ToggleButton

    @FXML
    private fun initialize() {

        Platform.runLater {
            lineWidthGroup.selectedToggleProperty().addListener { _, _, newVal ->
                DisplayModel.selectedLineWidth = getRadioButtonId(newVal)
            }

            fontFamilySelector.selectionModel.selectedItemProperty().addListener { _, _, newVal ->
                DisplayModel.selectedFontFamily = newVal
            }

            fontSizeSelector.valueProperty().addListener { _, _, newVal ->
                DisplayModel.selectedFontSize = newVal
            }
        }

        loadData(isReset = false)
    }

    @FXML
    fun onColorPickerAction() {
        DisplayModel.selectedLineColor = colorPicker.value
    }

    @FXML
    fun onBoldAction() {
        DisplayModel.selectedBold = boldButton.isSelected
    }

    @FXML
    fun onItalicAction() {
        DisplayModel.selectedItalics = italicButton.isSelected
    }

    private fun getRadioButtonId(radioButton: Toggle?): String {
        val id = radioButton?.toString() ?: "RadioButton[id=thin, styleClass=radio-button]'Thin'"
        val startIndex = id.indexOf("id=") + 3
        val endIndex = id.indexOf(",", startIndex)
        return id.substring(startIndex, endIndex)
    }

    private fun loadData(isReset: Boolean) {
        when (DisplayModel.selectedLineWidth) {
            "thin" -> thin.isSelected = true
            "medium" -> medium.isSelected = true
            "thick" -> thick.isSelected = true
        }

        colorPicker.value = DisplayModel.selectedLineColor
        DisplayModel.selectedLineColor = colorPicker.value

        fontFamilySelector.value = DisplayModel.selectedFontFamily ?: "Arial"
        DisplayModel.selectedFontFamily = fontFamilySelector.value

        fontSizeSelector.valueFactory.value = DisplayModel.selectedFontSize ?: 13.0
        DisplayModel.selectedFontSize = fontSizeSelector.value

        boldButton.isSelected = DisplayModel.selectedBold ?: false
        DisplayModel.selectedBold = boldButton.isSelected

        italicButton.isSelected = DisplayModel.selectedItalics ?: false
        DisplayModel.selectedItalics = italicButton.isSelected

        if (isReset) {
            setStageDimensions(isReset)
        } else {
            // Run after next scene is loaded
            Platform.runLater { setStageDimensions(isReset) }
        }
    }

    private fun setStageDimensions(isReset: Boolean) {
        if (this::colorPicker.isInitialized && colorPicker.scene != null && colorPicker.scene.window != null) {
            val currentStage = colorPicker.scene.window as Stage
            currentStage.width = DisplayModel.selectedWindowWidth
            currentStage.height = DisplayModel.selectedWindowHeight
            if (!isReset) {
                currentStage.x = DisplayModel.loadUpX
                currentStage.y = DisplayModel.loadUpY
            }
        }
    }

    override fun update(eventType: EventType, args: Array<Any>?) {
        when (eventType) {
            EventType.LOAD_DATA -> loadData(isReset = false)
            EventType.RESET_DATA -> loadData(isReset = true)
            else -> {}
        }
    }
}

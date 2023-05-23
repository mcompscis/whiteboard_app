/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import cs.shared.project.persistence.model.TextDataModel
import javafx.scene.control.TextArea
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle


class TextDrawingHandler : ShapeDrawingHandler() {
    override fun doStartDrawing(event: MouseEvent) {
        shape = Rectangle(event.x, event.y, 0.0, 0.0)
        setShapeDefaultProperties()
        pane.children.add(shape)
    }

    override fun continueDrawing(event: MouseEvent) {
        val rectangle = shape as Rectangle
        rectangle.width = event.x - rectangle.x
        rectangle.height = event.y - rectangle.y
    }

    override fun doEndDrawing() {
        val rectangle = shape as Rectangle
        val textArea = TextArea("Enter text here.")
        textArea.isWrapText = true
        textArea.prefWidth = rectangle.width
        textArea.prefHeight = rectangle.height
        textArea.layoutX = rectangle.x
        textArea.layoutY = rectangle.y

        setTextDefaultProperties(textArea)
        pane.children.add(textArea)
        pane.children.remove(shape)
        shapeDataModel = TextDataModel(textArea)
        addDataModelToWhiteboardAndSendNewShapeMsg()
    }

    companion object {
        private fun getMutableStyleMap(textArea: TextArea): MutableMap<String, String> {
            val styleString = textArea.style

            return styleString
                .split(";")
                .map { it.trim() }
                .filter { it.isNotBlank() }.associate {
                    val (property, value) = it.split(":")
                    property.trim() to value.trim()
                }.toMutableMap()
        }

        private fun getStyleString(styleMap: MutableMap<String, String>): String {
            return styleMap
                .map { "${it.key}: ${it.value}" }
                .joinToString("; ")
        }

        fun getRGBCodeFromColor(color: Color): String {
            return String.format(
                "#%02X%02X%02X",
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            )
        }

        fun updateFontColor(textArea: TextArea, color: Color) {
            val styleMap = getMutableStyleMap(textArea)
            styleMap["-fx-text-fill"] = getRGBCodeFromColor(color)
            textArea.style = getStyleString(styleMap)
        }

        fun updateFontFamily(textArea: TextArea, fontFamily: String) {
            val styleMap = getMutableStyleMap(textArea)
            styleMap["-fx-font-family"] = fontFamily
            textArea.style = getStyleString(styleMap)
        }

        fun updateFontSize(textArea: TextArea, size: Double) {
            val styleMap = getMutableStyleMap(textArea)
            styleMap["-fx-font-size"] = "${size}px"
            textArea.style = getStyleString(styleMap)
        }

        fun updateFontBold(textArea: TextArea, isBold: Boolean) {
            val styleMap = getMutableStyleMap(textArea)
            if (!isBold) {
                styleMap.remove("-fx-font-weight")
            } else if (!styleMap.contains("-fx-font-weight")) {
                styleMap["-fx-font-weight"] = "bold"
            }
            textArea.style = getStyleString(styleMap)
        }

        fun updateFontItalic(textArea: TextArea, isItalics: Boolean) {
            val styleMap = getMutableStyleMap(textArea)
            if (!isItalics) {
                styleMap.remove("-fx-font-style")
            } else if (!styleMap.contains("-fx-font-style")) {
                styleMap["-fx-font-style"] = "italic"
            }
            textArea.style = getStyleString(styleMap)
        }
    }
}

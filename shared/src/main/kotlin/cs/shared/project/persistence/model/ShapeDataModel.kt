/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.persistence.model

import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.scene.paint.Color
import javafx.scene.shape.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
sealed class ShapeDataModel {
    abstract val shapeType: Whiteboard.ToolType
    abstract var id: String
    abstract var fill: SerializableColor
    abstract var stroke: SerializableColor?
    abstract var strokeWidth: Double
    abstract var translateX: Double
    abstract var translateY: Double

    constructor(
        id: String,
        fill: SerializableColor,
        stroke: SerializableColor?,
        strokeWidth: Double,
        translateX: Double,
        translateY: Double
    ) {
        this.id = id
        this.fill = fill
        this.stroke = stroke
        this.strokeWidth = strokeWidth
        this.translateX = translateX
        this.translateY = translateY
    }

    protected fun populateShapeData(shape: Shape) {
        shape.id = id
        shape.fill = fill.fXColor
        shape.stroke = stroke?.fXColor
        shape.strokeWidth = strokeWidth
        shape.translateX = translateX
        shape.translateY = translateY
    }

    abstract val fxNode: Node

    fun clone(): ShapeDataModel {
        val encodedObj = Json.encodeToString(this)
        val shapeDataModelCopy = Json.decodeFromString<ShapeDataModel>(encodedObj)
        shapeDataModelCopy.id = UUID.randomUUID().toString()
        return shapeDataModelCopy
    }
}

@Serializable
data class CircleDataModel(
    override val shapeType: Whiteboard.ToolType = Whiteboard.ToolType.CIRCLE,
    override var id: String = UUID.randomUUID().toString(),
    override var fill: SerializableColor = SerializableColor(Color.BLACK),
    override var stroke: SerializableColor? = SerializableColor(Color.BLACK),
    override var strokeWidth: Double = 0.0,
    override var translateX: Double = 0.0,
    override var translateY: Double = 0.0,
    var centerX: Double = 0.0,
    var centerY: Double = 0.0,
    var radius: Double = 0.0
) : ShapeDataModel(id, fill, stroke, strokeWidth, translateX, translateY) {
    constructor(circle: Circle) : this(
        id = circle.id,
        fill = SerializableColor(circle.fill as Color),
        stroke = SerializableColor(circle.stroke as Color),
        strokeWidth = circle.strokeWidth,
        translateX = circle.translateX,
        translateY = circle.translateY
    ) {
        centerX = circle.centerX
        centerY = circle.centerY
        radius = circle.radius
    }

    override val fxNode: Node
        get() {
            val circle = Circle(centerX, centerY, radius)
            populateShapeData(circle)
            return circle
        }
}

@Serializable
data class PolylineDataModel(
    override val shapeType: Whiteboard.ToolType = Whiteboard.ToolType.PEN,
    override var id: String = UUID.randomUUID().toString(),
    override var fill: SerializableColor = SerializableColor(Color.BLACK),
    override var stroke: SerializableColor? = SerializableColor(Color.BLACK),
    override var strokeWidth: Double = 0.0,
    override var translateX: Double = 0.0,
    override var translateY: Double = 0.0,
    var points: ArrayList<Double> = ArrayList()
) : ShapeDataModel(id, fill, stroke, strokeWidth, translateX, translateY) {
    constructor(polyline: Polyline) : this(
        id = polyline.id,
        fill = SerializableColor(polyline.fill as Color),
        stroke = SerializableColor(polyline.stroke as Color),
        strokeWidth = polyline.strokeWidth,
        translateX = polyline.translateX,
        translateY = polyline.translateY,
    ) {
        points = ArrayList(polyline.points)
    }

    fun addPoint(x: Double, y: Double) {
        points.add(x)
        points.add(y)
    }

    override val fxNode: Node
        get() {
            val polyline = Polyline()
            polyline.points.addAll(points)
            populateShapeData(polyline)
            return polyline
        }
}

@Serializable
data class LineDataModel(
    override val shapeType: Whiteboard.ToolType = Whiteboard.ToolType.LINE,
    override var id: String = UUID.randomUUID().toString(),
    override var fill: SerializableColor = SerializableColor(Color.BLACK),
    override var stroke: SerializableColor? = SerializableColor(Color.BLACK),
    override var strokeWidth: Double = 0.0,
    override var translateX: Double = 0.0,
    override var translateY: Double = 0.0,
    var startX: Double = 0.0,
    var startY: Double = 0.0,
    var endX: Double = 0.0,
    var endY: Double = 0.0
) : ShapeDataModel(id, fill, stroke, strokeWidth, translateX, translateY) {
    constructor(line: Line) : this(
        id = line.id,
        fill = SerializableColor(line.fill as Color),
        stroke = SerializableColor(line.stroke as Color),
        strokeWidth = line.strokeWidth,
        translateX = line.translateX,
        translateY = line.translateY,
    ) {
        startX = line.startX
        startY = line.startY
        endX = line.endX
        endY = line.endY
    }

    override val fxNode: Node
        get() {
            val line = Line(startX, startY, endX, endY)
            populateShapeData(line)
            return line
        }
}

@Serializable
data class RectangleDataModel(
    override val shapeType: Whiteboard.ToolType = Whiteboard.ToolType.RECTANGLE,
    override var id: String = UUID.randomUUID().toString(),
    override var fill: SerializableColor = SerializableColor(Color.BLACK),
    override var stroke: SerializableColor? = SerializableColor(Color.BLACK),
    override var strokeWidth: Double = 0.0,
    override var translateX: Double = 0.0,
    override var translateY: Double = 0.0,
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 0.0,
    var height: Double = 0.0
) : ShapeDataModel(id, fill, stroke, strokeWidth, translateX, translateY) {
    constructor(rectangle: Rectangle) : this(
        id = rectangle.id,
        fill = SerializableColor(rectangle.fill as Color),
        stroke = SerializableColor(rectangle.stroke as Color),
        strokeWidth = rectangle.strokeWidth,
        translateX = rectangle.translateX,
        translateY = rectangle.translateY,
    ) {
        x = rectangle.x
        y = rectangle.y
        width = rectangle.width
        height = rectangle.height
    }

    override val fxNode: Node
        get() {
            val rectangle = Rectangle(x, y, width, height)
            populateShapeData(rectangle)
            return rectangle
        }
}

@Serializable
data class TextDataModel(
    override val shapeType: Whiteboard.ToolType = Whiteboard.ToolType.TEXT,
    override var id: String = UUID.randomUUID().toString(),
    override var fill: SerializableColor = SerializableColor(Color.BLACK),
    override var stroke: SerializableColor? = null,
    override var strokeWidth: Double = 0.0,
    override var translateX: Double = 0.0,
    override var translateY: Double = 0.0,
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 0.0,
    var height: Double = 0.0,
    var textVal: String = "",
    var isWrapText: Boolean = true,
    var style: String = "",
) : ShapeDataModel(id, fill, stroke, strokeWidth, translateX, translateY) {
    constructor(textArea: TextArea) : this(
        id = textArea.id,
        fill = SerializableColor(Color.BLACK),
        stroke = null,
        strokeWidth = 0.0,
        translateX = textArea.translateX,
        translateY = textArea.translateY,
    ) {
        x = textArea.layoutX
        y = textArea.layoutY
        textVal = textArea.text
        width = textArea.prefWidth
        height = textArea.prefHeight
        isWrapText = textArea.isWrapText
        style = textArea.style
    }

    override val fxNode: Node
        get() {
            val textArea = TextArea(textVal)
            textArea.id = id
            textArea.layoutX = x
            textArea.layoutY = y
            textArea.translateX = translateX
            textArea.translateY = translateY
            textArea.prefWidth = width
            textArea.prefHeight = height
            textArea.isWrapText = isWrapText
            textArea.style = style
            return textArea
        }
}
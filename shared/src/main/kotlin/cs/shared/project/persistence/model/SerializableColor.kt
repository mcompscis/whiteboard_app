/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.persistence.model

import javafx.scene.paint.Color

@kotlinx.serialization.Serializable
class SerializableColor(
    private var red: Double = 0.0,
    private var green: Double = 0.0,
    private var blue: Double = 0.0,
    private var alpha: Double = 0.0
) {

    constructor(color: Color) : this() {
        red = color.red
        green = color.green
        blue = color.blue
        alpha = color.opacity
    }

    val fXColor: Color
        get() = Color(red, green, blue, alpha)
}
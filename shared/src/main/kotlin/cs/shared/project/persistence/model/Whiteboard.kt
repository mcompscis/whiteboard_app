/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.persistence.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Whiteboard(
    var id: Int? = null,
    var name: String = "",
    var shapes: ArrayList<ShapeDataModel> = ArrayList(),
    var currSelectedShape: ShapeDataModel? = null,
    var currCopiedShape: ShapeDataModel? = null
) {

    enum class ToolType {
        CIRCLE,
        LINE,
        PEN,
        RECTANGLE,
        SELECT,
        ERASER,
        TEXT
    }

    fun exportDataToJson(): String {
        return Json.encodeToString(shapes)
    }

    fun importDataFromJson(jsonString: String) {
        shapes = Json.decodeFromString(jsonString)
    }
}
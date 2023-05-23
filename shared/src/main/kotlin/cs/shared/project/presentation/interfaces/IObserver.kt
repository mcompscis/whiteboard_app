/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.presentation.interfaces

enum class EventType {
    SHAPE_ADDED,
    SHAPE_EDITED,
    SHAPE_REMOVED,
    NEW_JOINER,
    CURSOR_MAP,
    CURSOR_UPDATE,
    DELETE_CURSOR,
    JOIN_TAB,
    REMOVE_TAB,
    RENAME_TAB, // used for non-socket observer event to start renaming
    FOCUS_TAB,
    TOOL_SELECTED,
    COLOR_CHANGED,
    LOAD_DATA,
    RESET_DATA,
    LINE_WIDTH_CHANGED,
    FONT_FAMILY_CHANGED,
    FONT_SIZE_CHANGED,
    FONT_BOLDED,
    FONT_ITALICIZED,
    DELETE_CURR_SELECTED_SHAPE,
    COPY_CURR_SELECTED_SHAPE,
    PASTE_CURR_COPIED_SHAPE,
    LIST_WHITEBOARDS,
    RENAME_WHITEBOARD, // used for socket broadcast handler of new name
}

interface IObserver {

    fun update(eventType: EventType, args: Array<Any>? = null)
}
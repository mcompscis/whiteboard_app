/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import cs.project.business.ClientSocket
import cs.project.business.command.CommandFactory
import cs.project.business.command.CommandTypes
import cs.shared.project.model.LeaveMessage
import cs.shared.project.persistence.model.Whiteboard
import cs.shared.project.presentation.interfaces.EventType
import cs.shared.project.presentation.interfaces.IObserver
import cs.shared.project.presentation.model.DisplayModel
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane

class WhiteboardController : IObserver {
    lateinit var addTab: Tab
    lateinit var whiteboardTabPane: TabPane
    private lateinit var tabSelectionModel: SelectionModel<Tab>
    private lateinit var tabList: ObservableList<Tab>

    private var isLoading = false

    @FXML
    fun initialize() {
        tabSelectionModel = whiteboardTabPane.selectionModel
        tabList = whiteboardTabPane.tabs

        tabSelectionModel.selectedItemProperty().addListener { _, _, newTab ->
            if (newTab === addTab && !isLoading) {
                CommandFactory.createFromArgs(CommandTypes.JOIN).execute()
            } else {
                if (tabSelectionModel.selectedIndex >= 0 && tabSelectionModel.selectedIndex < DisplayModel.whiteboards.size) {
                    DisplayModel.currSelectedWhiteboard = DisplayModel.whiteboards[tabSelectionModel.selectedIndex]
                }
            }
        }
    }

    private fun createNewTab(whiteboard: Whiteboard, render: Boolean = false): Tab {
        val newTab = Tab()

        newTab.setOnCloseRequest {
            removeCurrentTab()
            it.consume()
        }

        val label = Label(whiteboard.name)
        newTab.graphic = label
        val textField = TextField()

        // bring text field to focus on double click
        label.onMouseClicked = EventHandler {
            if (it.clickCount == 2) {
                textField.text = label.text
                newTab.graphic = textField
                textField.selectAll()
                textField.requestFocus()
            }
        }

        // update label when text field is changed
        textField.onAction = EventHandler {
            // if new name is different, fire a rename event
            if (label.text != textField.text) {
                ClientSocket.sendWhiteboardRenameMessage(whiteboardId = whiteboard.id!!, newName = textField.text)
            }
            label.text = textField.text
            newTab.graphic = label
        }

        // update and bring label to front when text field is out of focus
        textField.focusedProperty()
            .addListener { _: ObservableValue<out Boolean?>, _: Boolean?, newValue: Boolean? ->
                if (!newValue!!) {
                    label.text = textField.text
                    DisplayModel.whiteboards[tabSelectionModel.selectedIndex].name = textField.text
                    newTab.graphic = label
                }
            }

        // set drawing controller to whiteboard model
        val loader = FXMLLoader(javaClass.getResource("/fxml/DrawingPane.fxml"))
        val node = loader.load<AnchorPane>()
        val drawingPaneController = loader.getController<DrawingPaneController>()
        DisplayModel.addView(observer = drawingPaneController, isDataObserver = true)
        // unique ID for drawing pane
        // TODO: dont allow whiteboards to have duplicate names for sanity
        node.children[0].id = "pane_${whiteboard.name.replace(' ', '_')}"
        drawingPaneController.setWhiteboardViewModel(whiteboard, render)

        newTab.content = node
        newTab.userData = drawingPaneController
        return newTab
    }

    private fun joinNewTab(whiteboard: Whiteboard) {
        val newTab = createNewTab(whiteboard, render = true)
        if (!this::whiteboardTabPane.isInitialized) return
        DisplayModel.whiteboards.add(whiteboard)
        tabList.add(tabList.size - 1, newTab) // Adding new tab before "add" tab
        tabSelectionModel.select(tabList.size - 2) // Select new tab
    }


    private fun removeCurrentTab() {
        val selectedTabIndex = tabSelectionModel.selectedIndex
        tabSelectionModel.clearSelection()
        tabList.removeAt(selectedTabIndex)

        leaveWhiteboard(DisplayModel.whiteboards[selectedTabIndex].id!!)
        DisplayModel.whiteboards.removeAt(selectedTabIndex)
    }

    // tell the server that you are leaving so persistence and removal of cursor can be handled
    private fun leaveWhiteboard(whiteboardId: Int) {
        DisplayModel.socketSession?.send(
            "/app/leave/$whiteboardId",
            LeaveMessage(author = DisplayModel.socketSession!!.sessionId)
        )
    }

    private fun focusWhiteboard(whiteboardId: Int) {
        tabSelectionModel.select(DisplayModel.whiteboards.indexOfFirst { whiteboard -> whiteboard.id == whiteboardId })
    }

    private fun handleRenameCurrentTab() {
        // fire double click event at tab's label
        Event.fireEvent(
            tabSelectionModel.selectedItem.graphic, MouseEvent(
                MouseEvent.MOUSE_CLICKED,
                0.0,
                0.0,
                0.0,
                0.0,
                MouseButton.PRIMARY,
                2,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                null
            )
        )
    }

    private fun loadTabs(whiteboardIdsToLoad: String) {
        isLoading = true
        tabList.remove(0, tabList.size - 1)

        if (whiteboardIdsToLoad == "") {
            CommandFactory.createFromArgs(CommandTypes.JOIN).execute()
        } else {
            whiteboardIdsToLoad.split(",").forEach {
                CommandFactory.createFromArgs(CommandTypes.JOIN, arrayOf(it.toInt())).execute()
            }
        }
        isLoading = false
    }

    override fun update(eventType: EventType, args: Array<Any>?) {
        when (eventType) {
            EventType.JOIN_TAB -> {
                var whiteboard = args?.get(0) as Whiteboard
                joinNewTab(whiteboard)
            }

            EventType.LOAD_DATA -> {
                var whiteboardIdsToLoad = args?.get(0) as String
                loadTabs(whiteboardIdsToLoad)
            }

            EventType.REMOVE_TAB -> removeCurrentTab()

            EventType.RENAME_TAB -> handleRenameCurrentTab()

            EventType.RENAME_WHITEBOARD -> {
                val whiteboardId = args?.get(0) as Int
                val newName = args[1] as String
                tabList[DisplayModel.whiteboards.indexOfFirst { it.id == whiteboardId }].graphic = Label(newName)
            }

            EventType.FOCUS_TAB -> focusWhiteboard(args?.get(0) as Int)

            else -> {}
        }
    }
}
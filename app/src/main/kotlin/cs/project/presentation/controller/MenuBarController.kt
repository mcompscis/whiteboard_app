/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.presentation.controller

import cs.project.business.command.CommandFactory
import cs.project.business.command.CommandTypes
import cs.shared.project.model.ListWhiteboardsRequest
import cs.shared.project.persistence.model.Whiteboard
import cs.shared.project.presentation.interfaces.EventType
import cs.shared.project.presentation.interfaces.IObserver
import cs.shared.project.presentation.model.DisplayModel
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

class MenuBarController : IObserver {
    @FXML
    lateinit var openMenu: Menu

    @FXML
    lateinit var menuBar: MenuBar
    private lateinit var loginScene: Scene

    @FXML
    lateinit var deleteMenuItem: MenuItem

    @FXML
    lateinit var newWhiteboardMenuItem: MenuItem

    @FXML
    lateinit var renameWhiteboardMenuItem: MenuItem

    @FXML
    lateinit var removeWhiteboardMenuItem: MenuItem

    @FXML
    lateinit var closeAppMenuItem: MenuItem

    @FXML
    lateinit var maximizeMenuItem: MenuItem

    @FXML
    lateinit var minimizeMenuItem: MenuItem

    @FXML
    lateinit var logOutMenuItem: MenuItem

    @FXML
    lateinit var copyMenuItem: MenuItem

    @FXML
    lateinit var pasteMenuItem: MenuItem


    fun setLoginScene(scene: Scene) {
        loginScene = scene
    }

    @FXML
    fun initialize() {
        deleteMenuItem.accelerator = KeyCodeCombination(KeyCode.BACK_SPACE)
        newWhiteboardMenuItem.accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)
        renameWhiteboardMenuItem.accelerator = KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN)
        removeWhiteboardMenuItem.accelerator = KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN)
        closeAppMenuItem.accelerator = KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)
        maximizeMenuItem.accelerator = KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)
        minimizeMenuItem.accelerator = KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN)
        logOutMenuItem.accelerator = KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN)
        copyMenuItem.accelerator = KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN)
        pasteMenuItem.accelerator = KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN)
    }

    @FXML
    fun onNewWhiteboardAction() {
        CommandFactory.createFromArgs(CommandTypes.JOIN).execute()
    }

    @FXML
    fun onRemoveWhiteboardAction() {
        CommandFactory.createFromArgs(CommandTypes.REMOVE).execute()
    }

    @FXML
    fun onRenameAction() {
        CommandFactory.createFromArgs(CommandTypes.RENAME).execute()
    }

    @FXML
    fun onDeleteAction() {
        CommandFactory.createFromArgs(CommandTypes.DELETE).execute()
    }

    @FXML
    fun onCloseAction() {
        CommandFactory.createFromArgs(CommandTypes.CLOSE).execute()
    }

    @FXML
    fun onMinimizeAction() {
        CommandFactory.createFromArgs(CommandTypes.MINIMIZE, arrayOf(menuBar.scene.window)).execute()
    }

    @FXML
    fun onMaximizeAction() {
        CommandFactory.createFromArgs(CommandTypes.MAXIMIZE, arrayOf(menuBar.scene.window)).execute()
    }

    @FXML
    fun onCopyAction() {
        CommandFactory.createFromArgs(CommandTypes.COPY).execute()
    }

    @FXML
    fun onPasteAction() {
        CommandFactory.createFromArgs(CommandTypes.PASTE).execute()
    }

    @FXML
    fun onfileMenuClicked() {
        DisplayModel.socketSession?.send(
            "/app/list-whiteboards",
            ListWhiteboardsRequest(author = DisplayModel.socketSession!!.sessionId)
        )
    }

    override fun update(eventType: EventType, args: Array<Any>?) {
        if (eventType == EventType.LIST_WHITEBOARDS) {
            val whiteboards = args?.get(0) as List<Whiteboard>
            val selectionList = ArrayList<MenuItem>()
            for (whiteboard in whiteboards) {
                val menuItem = MenuItem(whiteboard.name)
                menuItem.onAction = EventHandler {
                    if (DisplayModel.whiteboards.find { board -> board.id == whiteboard.id } != null) {
                        DisplayModel.focusTab(whiteboard.id as Int)
                    } else {
                        CommandFactory.createFromArgs(CommandTypes.JOIN, arrayOf(whiteboard.id as Int)).execute()
                    }
                }
                selectionList.add(menuItem)
            }
            openMenu.items.setAll(selectionList)

            // reopen the menu to show the updated items
            Platform.runLater { openMenu.show() }
        }
    }

    fun onLogOutAction() {
        CommandFactory.createFromArgs(CommandTypes.LOG_OUT, arrayOf(menuBar, loginScene)).execute()
    }
}
/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.business.command

import cs.project.business.ClientSocket
import cs.project.business.interfaces.ICommand
import cs.shared.project.persistence.ClientPersistenceDao
import cs.shared.project.presentation.model.DisplayModel
import javafx.scene.Scene
import javafx.scene.control.MenuBar
import javafx.stage.Stage

class LogOutCommand(val args: Array<Any>?) : ICommand {
    override fun execute() {
        val menuBar = args!![0] as MenuBar
        val loginScene = args!![1] as Scene
        ClientSocket.gracefulQuit()
        ClientPersistenceDao.insertOrUpdate(DisplayModel.currentUserId!!)
        DisplayModel.resetData()
        ClientPersistenceDao.resetLastLoggedInUser()
        (menuBar.scene.window as Stage).scene = loginScene
    }
}

/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.business.command

import cs.project.business.interfaces.ICommand
import javafx.application.Platform

class CloseCommand(args: Array<Any>?) : ICommand {
    override fun execute() {
        Platform.exit()
    }
}

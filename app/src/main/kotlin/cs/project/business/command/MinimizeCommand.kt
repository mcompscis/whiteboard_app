/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.business.command

import cs.project.business.interfaces.ICommand
import javafx.stage.Stage

class MinimizeCommand(private val args: Array<Any>?) : ICommand {
    override fun execute() {
        (args?.get(0) as Stage).isIconified = true
    }

}

/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.business.command

import cs.project.business.interfaces.ICommand
import cs.shared.project.presentation.model.DisplayModel

class RenameCommand(args: Array<Any>?) : ICommand {
    override fun execute() {
        DisplayModel.renameTab()
    }

}

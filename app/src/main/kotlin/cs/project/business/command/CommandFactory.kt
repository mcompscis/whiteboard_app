/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.business.command

import cs.project.business.interfaces.ICommand

enum class CommandTypes { JOIN, REMOVE, DELETE, RENAME, CLOSE, CUT, COPY, PASTE, MAXIMIZE, MINIMIZE, LOG_OUT }
object CommandFactory {
    fun createFromArgs(type: CommandTypes, args: Array<Any>? = null): ICommand =
        when (type) {
            // JOIN command is used for creating new whiteboards and joining existing whiteboards as well
            CommandTypes.JOIN -> JoinCommand(args)
            CommandTypes.REMOVE -> RemoveCommand(args)
            CommandTypes.DELETE -> DeleteCommand(args)
            CommandTypes.RENAME -> RenameCommand(args)
            CommandTypes.CLOSE -> CloseCommand(args)
            CommandTypes.LOG_OUT -> LogOutCommand(args)
//                CommandTypes.CUT -> CutCommand(args)
            CommandTypes.COPY -> CopyCommand(args)
            CommandTypes.PASTE -> PasteCommand(args)
            CommandTypes.MAXIMIZE -> MaximizeCommand(args)
            CommandTypes.MINIMIZE -> MinimizeCommand(args)
            else -> ErrorCommand(args)
        }
}

class ErrorCommand(val args: Array<Any>?) : ICommand {
    override fun execute() {
        println("Command not recognized")
    }
}

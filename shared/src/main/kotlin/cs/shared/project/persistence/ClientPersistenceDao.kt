/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.persistence

import cs.shared.project.persistence.model.ClientPersistence
import cs.shared.project.persistence.model.ClientPersistence.toDisplayModel
import cs.shared.project.persistence.model.LastSessionPersistence
import cs.shared.project.presentation.model.DisplayModel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


//TODO convert to interface:
// interface ModelDao {
//    fun insert(data: T)
//    fun update(data: T)
//    fun delete(data: T)
//    fun getById(id: Int): T?
//    fun getAll(): List<T>
//}
object ClientPersistenceDao {
    fun insertOrUpdate(userId: UUID, selectedWhiteboards: String = "") {
        var selectedWhiteboards = selectedWhiteboards
        if (selectedWhiteboards.isEmpty()) {
            DisplayModel.whiteboards.forEach {
                selectedWhiteboards += (it.id.toString() + ",")
            }
            selectedWhiteboards = selectedWhiteboards.substring(0, selectedWhiteboards.length - 1)
        }

        transaction {
            var result = ClientPersistence.select(ClientPersistence.ownerId eq userId.toString()).firstOrNull()

            if (result != null) {
                ClientPersistence.update({ ClientPersistence.ownerId eq userId.toString() }) {
                    it[selectedToolType] = DisplayModel.selectedToolType.name
                    it[selectedLineWidth] = DisplayModel.selectedLineWidth
                    it[selectedLineColor] = DisplayModel.selectedLineColor.toString()
                    it[selectedWindowWidth] = DisplayModel.selectedWindowWidth
                    it[selectedWindowHeight] = DisplayModel.selectedWindowHeight
                    it[selectedWindowPositionX] = DisplayModel.selectedWindowPositionX
                    it[selectedWindowPositionY] = DisplayModel.selectedWindowPositionY
                    it[selectedWhiteboardIds] = selectedWhiteboards
                }
            } else {
                ClientPersistence.insert {
                    it[selectedToolType] = DisplayModel.selectedToolType.name
                    it[selectedLineWidth] = DisplayModel.selectedLineWidth
                    it[selectedLineColor] = DisplayModel.selectedLineColor.toString()
                    it[selectedWindowWidth] = DisplayModel.selectedWindowWidth
                    it[selectedWindowHeight] = DisplayModel.selectedWindowHeight
                    it[selectedWindowPositionX] = DisplayModel.selectedWindowPositionX
                    it[selectedWindowPositionY] = DisplayModel.selectedWindowPositionY
                    it[selectedWhiteboardIds] = selectedWhiteboards
                    it[ownerId] = userId.toString()
                }
            }
        }
    }

    fun setUpDisplayModel(lastLoggedInUserId: UUID) {
        val result = transaction {
            ClientPersistence.select { ClientPersistence.ownerId eq lastLoggedInUserId.toString() }
                .firstOrNull()
        }

        if (result == null) {
            DisplayModel.setup()
        } else {
            result.toDisplayModel()
        }
    }

    fun getLastLoggedInUser(): UUID? {
        var result = transaction {
            LastSessionPersistence.selectAll().firstOrNull()
        }
        return result?.get(LastSessionPersistence.loggedInUser)?.value
    }

    fun insertLastLoggedInUser(userId: UUID) {
        transaction {
            LastSessionPersistence.deleteAll()
            LastSessionPersistence.insert {
                it[loggedInUser] = userId
            }
        }
    }

    fun resetLastLoggedInUser() {
        transaction {
            LastSessionPersistence.deleteAll()
        }
    }
}
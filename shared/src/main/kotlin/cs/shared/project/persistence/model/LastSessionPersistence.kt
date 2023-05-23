/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.persistence.model

import cs.shared.project.persistence.model.UserData
import org.jetbrains.exposed.dao.id.IntIdTable

object LastSessionPersistence : IntIdTable() {
    val loggedInUser = reference("logged_in_user", UserData.id)
}
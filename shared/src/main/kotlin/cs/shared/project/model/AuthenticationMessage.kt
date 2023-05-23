/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationMessage(val username: String, val password: String)

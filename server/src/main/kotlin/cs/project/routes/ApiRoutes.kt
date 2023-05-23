/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.routes

import cs.shared.project.model.AuthenticationMessage
import cs.shared.project.persistence.UserDataDao
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ApiRoutes {
    @GetMapping("/greeting")
    fun greeting(): String {
        println("sending greeting")
        return "Hello, world!"
    }

    @PostMapping("/login")
    fun login(@RequestBody message: AuthenticationMessage): String {
       val result = UserDataDao.validate(message.username, message.password)
        return result.toString()
    }

    @PostMapping("/signup")
    fun signup(@RequestBody message: AuthenticationMessage): Boolean {
        return UserDataDao.insert(message.username, message.password)
    }
}
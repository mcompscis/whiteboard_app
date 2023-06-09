/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.shared.project

import org.junit.jupiter.api.Test

class SharedTest {
    @Test
    fun appHasAGreeting() {
        val sysInfo = Shared.SysInfo
        assert(sysInfo.userName == System.getProperty("user.name"))
    }
}

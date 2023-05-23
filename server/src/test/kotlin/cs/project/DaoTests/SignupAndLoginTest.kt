/**
 * Copyright (c) 2023, CS 346 Whiteboard App Team 210 Winter 2022.
 * All rights reserved.
 *
 * This code is licensed under the MIT License.
 */

package cs.project.DaoTests

import cs.shared.project.persistence.Connect
import cs.shared.project.persistence.UserDataDao
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

class SignupAndLoginTest {

    @BeforeEach
    fun setup() {
        val connect = Connect()
        connect.dbConnect()
    }

    @AfterEach
    fun cleanup() {
        val file = File("database/whiteboardapp.db")
        if (file.exists()) {
            file.delete()
        }
    }

    @Test
    fun canSignUp() {
        assert(UserDataDao.insert("test", "testpass"))
    }

    @Test
    fun canSignUpAndLogin() {
        val userName = "test2"
        val password = "testpass1"
        UserDataDao.insert(userName, password)
        val result = UserDataDao.validate(userName, password)
        assert(result is UUID)
    }

    @Test
    fun cannotLoginWithoutSigningUp() {
        val userName = "test3"
        val password = "testpass1"
        val result = UserDataDao.validate(userName, password)
        assert(result == null)
    }

    @Test
    fun cannotLoginWithWrongPassword() {
        val userName = "test4"
        val password = "testpass1"
        val wrongPassword = "testpass"
        UserDataDao.insert(userName, password)
        val result = UserDataDao.validate(userName, wrongPassword)
        assert(result == null)
    }

}
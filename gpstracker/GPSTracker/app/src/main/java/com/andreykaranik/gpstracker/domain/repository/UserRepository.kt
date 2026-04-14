package com.andreykaranik.gpstracker.domain.repository

import com.andreykaranik.gpstracker.domain.model.UserData
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.LoginResult
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.model.result.RegisterResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult

interface UserRepository {
    fun register(name: String, email: String, password: String): RegisterResult
    fun login(email: String, password: String): LoginResult
    fun refreshToken(userData: UserData): RefreshTokenResult
    fun saveUserData(userData: UserData): SaveUserDataResult
    fun getUserData(): GetUserDataResult
}
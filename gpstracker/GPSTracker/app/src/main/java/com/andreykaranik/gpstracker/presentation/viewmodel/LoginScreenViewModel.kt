package com.andreykaranik.gpstracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreykaranik.gpstracker.domain.model.UserData
import com.andreykaranik.gpstracker.domain.model.result.LoginResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.usecase.LoginUseCase
import com.andreykaranik.gpstracker.domain.usecase.SaveUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val saveUserDataUseCase: SaveUserDataUseCase
) : ViewModel() {
    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.None)
    val loginResult: StateFlow<LoginResult> = _loginResult

    private val _saveUserDataResult = MutableStateFlow<SaveUserDataResult>(SaveUserDataResult.None)
    val saveUserDataResult: StateFlow<SaveUserDataResult> = _saveUserDataResult

    fun login(email: String, password: String) {
        _loginResult.value = LoginResult.Pending
        viewModelScope.launch(Dispatchers.IO) {
            _loginResult.value = loginUseCase.execute(email = email, password = password)
        }
    }

    fun saveUserData(email: String, loginResult: LoginResult.Success) {
        _saveUserDataResult.value = SaveUserDataResult.Pending
        viewModelScope.launch(Dispatchers.IO) {
            _saveUserDataResult.value = saveUserDataUseCase.execute(
                userData = UserData(
                    name = loginResult.name,
                    email = email,
                    accessToken = loginResult.accessToken,
                    refreshToken = loginResult.refreshToken
                )
            )
        }
    }

    fun clearLoginResult() {
        _loginResult.value = LoginResult.None
    }

    fun clearSaveUserDataResult() {
        _saveUserDataResult.value = SaveUserDataResult.None
    }

}
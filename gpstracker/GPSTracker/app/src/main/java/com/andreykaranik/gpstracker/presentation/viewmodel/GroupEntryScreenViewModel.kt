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

@HiltViewModel
class GroupEntryScreenViewModel @Inject constructor(
    private val saveUserDataUseCase: SaveUserDataUseCase,
) : ViewModel() {

    private val _saveUserDataResult = MutableStateFlow<SaveUserDataResult>(SaveUserDataResult.None)
    val saveUserDataResult: StateFlow<SaveUserDataResult> = _saveUserDataResult

    fun leave() {
        _saveUserDataResult.value = SaveUserDataResult.Pending
        viewModelScope.launch(Dispatchers.IO) {
            _saveUserDataResult.value = saveUserDataUseCase.execute(
                userData = UserData(
                    name = "",
                    email = "",
                    accessToken = "",
                    refreshToken = ""
                )
            )
        }
    }

    fun clearSaveUserDataResult() {
        _saveUserDataResult.value = SaveUserDataResult.None
    }

}
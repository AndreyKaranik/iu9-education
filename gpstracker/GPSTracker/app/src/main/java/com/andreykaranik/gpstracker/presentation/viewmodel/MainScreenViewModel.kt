package com.andreykaranik.gpstracker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreykaranik.gpstracker.domain.model.GroupData
import com.andreykaranik.gpstracker.domain.model.GroupMember
import com.andreykaranik.gpstracker.domain.model.Mode12Data
import com.andreykaranik.gpstracker.domain.model.ModeParameters
import com.andreykaranik.gpstracker.domain.model.UserData
import com.andreykaranik.gpstracker.domain.model.result.CreateGroupResult
import com.andreykaranik.gpstracker.domain.model.result.GetGroupDataResult
import com.andreykaranik.gpstracker.domain.model.result.GetGroupMembersResult
import com.andreykaranik.gpstracker.domain.model.result.GetPeriodClustersResult
import com.andreykaranik.gpstracker.domain.model.result.GetPeriodLocationsResult
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.LeaveGroupResult
import com.andreykaranik.gpstracker.domain.model.result.Mode11Result
import com.andreykaranik.gpstracker.domain.model.result.Mode12Result
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.usecase.CreateGroupUseCase
import com.andreykaranik.gpstracker.domain.usecase.GetGroupDataUseCase
import com.andreykaranik.gpstracker.domain.usecase.GetGroupMembersUseCase
import com.andreykaranik.gpstracker.domain.usecase.GetPeriodClustersUseCase
import com.andreykaranik.gpstracker.domain.usecase.GetPeriodLocationsUseCase
import com.andreykaranik.gpstracker.domain.usecase.GetUserDataUseCase
import com.andreykaranik.gpstracker.domain.usecase.LeaveGroupUseCase
import com.andreykaranik.gpstracker.domain.usecase.Mode11UseCase
import com.andreykaranik.gpstracker.domain.usecase.Mode12UseCase
import com.andreykaranik.gpstracker.domain.usecase.SaveUserDataUseCase
import com.andreykaranik.gpstracker.presentation.view.ModeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val getGroupDataUseCase: GetGroupDataUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val getGroupMembersUseCase: GetGroupMembersUseCase,
    private val saveUserDataUseCase: SaveUserDataUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val mode11UseCase: Mode11UseCase,
    private val mode12UseCase: Mode12UseCase,
    private val getPeriodLocationsUseCase: GetPeriodLocationsUseCase,
    private val getPeriodClustersUseCase: GetPeriodClustersUseCase

) : ViewModel() {

    private val _getGroupDataResult = MutableStateFlow<GetGroupDataResult>(GetGroupDataResult.None)
    val getGroupDataResult: StateFlow<GetGroupDataResult> = _getGroupDataResult

    private val _groupData = MutableStateFlow<GroupData?>(null)
    val groupData: StateFlow<GroupData?> = _groupData

    private val _leaveGroupResult = MutableStateFlow<LeaveGroupResult>(LeaveGroupResult.None)
    val leaveGroupResult: StateFlow<LeaveGroupResult> = _leaveGroupResult

    private val _getGroupMembersResult = MutableStateFlow<GetGroupMembersResult>(GetGroupMembersResult.None)
    val getGroupMembersResult: StateFlow<GetGroupMembersResult> = _getGroupMembersResult

    private val _groupMembers = MutableStateFlow<List<GroupMember>>(listOf())
    val groupMembers: StateFlow<List<GroupMember>> = _groupMembers

    private val _getUserDataResult = MutableStateFlow<GetUserDataResult>(GetUserDataResult.None)
    val getUserDataResult: StateFlow<GetUserDataResult> = _getUserDataResult

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _selectedUserId = MutableStateFlow<Int?>(null)
    val selectedUserId: StateFlow<Int?> = _selectedUserId

    private val _selectedMode = MutableStateFlow(ModeType.CURRENT_LOCATION)
    val selectedMode: StateFlow<ModeType> = _selectedMode

    private val _parameters = MutableStateFlow(ModeParameters())
    val parameters: StateFlow<ModeParameters> = _parameters

    private val _locationPointsFlow = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val locationPointsFlow: StateFlow<List<Pair<Double, Double>>> = _locationPointsFlow

    private val _isUnauthorized = MutableStateFlow<Boolean>(false)
    val isUnauthorized: StateFlow<Boolean> = _isUnauthorized

    private val _bottomSheetVisible = MutableStateFlow<Boolean>(false)
    val bottomSheetVisible: StateFlow<Boolean> = _bottomSheetVisible

    private val _mode12Data = MutableStateFlow<Mode12Data?>(null)
    val mode12Data: StateFlow<Mode12Data?> = _mode12Data

    private var mode12UseCaseJob: Job? = null

    fun onMarkerClick() {
        _bottomSheetVisible.value = true
        startMode12UseCase()
    }

    fun onBottomSheetDismiss() {
        _bottomSheetVisible.value = false
        mode12UseCaseJob?.cancel()
    }

    private fun startMode12UseCase() {
        mode12UseCaseJob?.cancel()
        mode12UseCaseJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val result = mode12UseCase.execute(
                    userId = selectedUserId.value!!,
                    kalmanEnabled = parameters.value.kalmanEnabled
                )
                if (result is Mode12Result.Success) {
                    _mode12Data.value = result.mode12Data
                }
                if (result is Mode12Result.Unauthorized) {
                    setUnauthorized(true)
                }
                delay(1_000L)
            }
        }
    }

    init {
        viewModelScope.launch {
            combine(_selectedUserId.filterNotNull(), _selectedMode, _parameters) { userId, mode, params ->
                Triple(userId, mode, params)
            }.collectLatest { (userId, mode, params) ->
                when (mode) {
                    ModeType.CURRENT_LOCATION -> startCurrentLocationMode(userId, params.kalmanEnabled)
                    ModeType.TIME_INTERVAL -> startPeriodLocationsMode(userId, params)
                    ModeType.DBSCAN -> startPeriodClustersMode(userId, params)
                    else -> {}
                }
            }
        }
    }

    fun setUnauthorized(isUnauthorized: Boolean) {
        _isUnauthorized.value = isUnauthorized
    }

    private var currentJob: Job? = null

    private fun startCurrentLocationMode(userId: Int, kalman: Boolean) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                when (val result = mode11UseCase.execute(userId, kalman)) {
                    is Mode11Result.Success -> {
                        _locationPointsFlow.emit(listOf(result.latitude to result.longitude))
                    }
                    is Mode11Result.Unauthorized -> {
                        setUnauthorized(false)
                    }
                    else -> {}
                }
                delay(1000L)
            }
        }
    }

    fun convertLocalToUtc(
        localDateTimeStr: String,
        inputPattern: String = "yyyy-MM-dd HH:mm:ss.SSS",
        outputPattern: String = "yyyy-MM-dd HH:mm:ss.SSS",
        localZoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val inputFormatter = DateTimeFormatter.ofPattern(inputPattern)
        val outputFormatter = DateTimeFormatter.ofPattern(outputPattern)

        val localDateTime = LocalDateTime.parse(localDateTimeStr, inputFormatter)

        val zonedDateTime = localDateTime.atZone(localZoneId)
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC)

        return utcDateTime.format(outputFormatter)
    }

    private fun startPeriodLocationsMode(userId: Int, params: ModeParameters) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val result = getPeriodLocationsUseCase.execute(
                        userId,
                        convertLocalToUtc(params.timeFrom),
                        convertLocalToUtc(params.timeTo),
                        minIntervalMinutes = params.minIntervalMinutes.toInt()
                    )
                    if (result is GetPeriodLocationsResult.Success) {
                        if (!params.kalmanEnabled) {
                            _locationPointsFlow.emit(result.locations.map {
                                Pair(
                                    it.latitude,
                                    it.longitude
                                )
                            })
                        } else {
                            _locationPointsFlow.emit(result.locations.map {
                                Pair(
                                    it.kalmanLatitude,
                                    it.kalmanLongitude
                                )
                            })
                        }
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                delay(10_000L)
            }
        }
    }

    private fun startPeriodClustersMode(userId: Int, params: ModeParameters) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val result = getPeriodClustersUseCase.execute(
                        userId,
                        convertLocalToUtc(params.timeFrom),
                        convertLocalToUtc(params.timeTo),
                        kalmanEnabled = params.kalmanEnabled,
                        eps = params.eps.toDouble(),
                        minPts = params.minPts.toInt()
                    )
                    if (result is GetPeriodClustersResult.Success) {
                        _locationPointsFlow.emit(result.centers.map { Pair(it.latitude, it.longitude) })
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                delay(10_000L)
            }
        }
    }


    fun selectMode(mode: ModeType) {
        _selectedMode.value = mode
    }

    fun updateParameters(params: ModeParameters) {
        _parameters.value = params
    }

    fun getGroupData() {
        _getGroupDataResult.value = GetGroupDataResult.Pending
        viewModelScope.launch(Dispatchers.IO) {
            _getGroupDataResult.value = getGroupDataUseCase.execute()
            if (_getGroupDataResult.value is GetGroupDataResult.Success) {
                _groupData.value = (_getGroupDataResult.value as GetGroupDataResult.Success).groupData
            }
        }
    }

    fun leaveGroup() {
        _leaveGroupResult.value = LeaveGroupResult.Pending
        viewModelScope.launch(Dispatchers.IO) {
            _leaveGroupResult.value = leaveGroupUseCase.execute()
        }
    }

    fun getGroupMembers() {
        _getGroupMembersResult.value = GetGroupMembersResult.Pending
        _groupMembers.value = listOf()
        viewModelScope.launch(Dispatchers.IO) {
            _getGroupMembersResult.value = getGroupMembersUseCase.execute()
            if (_getGroupMembersResult.value is GetGroupMembersResult.Success) {
                _groupMembers.value = (_getGroupMembersResult.value as GetGroupMembersResult.Success).groupMembers
            }
        }
    }

    fun clearGetGroupDataResult() {
        _getGroupDataResult.value = GetGroupDataResult.None
    }

    fun clearLeaveGroupResult() {
        _leaveGroupResult.value = LeaveGroupResult.None
    }

    fun clearGetGroupMembersResult() {
        _getGroupMembersResult.value = GetGroupMembersResult.None
    }

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

    fun getUserData() {
        _getUserDataResult.value = GetUserDataResult.Pending
        viewModelScope.launch(Dispatchers.IO) {
            _getUserDataResult.value = getUserDataUseCase.execute()
            if (_getUserDataResult.value is GetUserDataResult.Success) {
                _userData.value = (_getUserDataResult.value as GetUserDataResult.Success).userData
            }
            clearGetUserDataResult()
        }
    }

    fun clearGetUserDataResult() {
        _getUserDataResult.value = GetUserDataResult.None
    }

    fun selectUser(id: Int?) {
        if (_selectedUserId.value == id) {
            _selectedUserId.value = null
        } else {
            _selectedUserId.value = id
        }
    }

}
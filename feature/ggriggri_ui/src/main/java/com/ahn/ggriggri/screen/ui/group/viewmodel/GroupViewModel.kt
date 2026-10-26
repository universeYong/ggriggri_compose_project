package com.ahn.ggriggri.screen.ui.group.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.Group
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // SnackBar 기능을 직접 구현
    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    fun showSnackBar(message: String) {
        viewModelScope.launch {
            _snackBarMessage.emit(message)
        }
    }

    private val _createResult = MutableStateFlow<DataResourceResult<Unit>?>(null)
    val createResult: StateFlow<DataResourceResult<Unit>?> = _createResult

    private val _isGroupCodeDuplicate = MutableStateFlow<DataResourceResult<Boolean>?>(null)
    val isGroupCodeDuplicate: StateFlow<DataResourceResult<Boolean>?> = _isGroupCodeDuplicate


    /** Loading을 건너뛰고 첫 번째 Success/Failure 결과만 받는다. */
    private suspend fun <T> Flow<DataResourceResult<T>>.awaitResult(): DataResourceResult<T> =
        filter { it is DataResourceResult.Success || it is DataResourceResult.Failure }.first()

    /** Firebase에서 최신 사용자 정보를 가져와 SessionManager를 갱신한다. 실패해도 흐름은 계속된다. */
    private suspend fun refreshSessionUser(userId: String, tag: String) {
        try {
            when (val updatedUserResult = userRepository.getUserById(userId).awaitResult()) {
                is DataResourceResult.Success -> {
                    updatedUserResult.data?.let { sessionManager.loginUser(it) }
                }
                is DataResourceResult.Failure -> {
                    Log.e(tag, "최신 사용자 정보 가져오기 실패", updatedUserResult.exception)
                }
                else -> Unit
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(tag, "사용자 정보 업데이트 실패", e)
        }
    }

    private suspend fun fail(exception: Throwable, message: String) {
        _createResult.value = DataResourceResult.Failure(exception)
        _snackBarMessage.emit(message)
    }

    fun createGroupAndUpdateUser(
        userId: String,
        groupName: String,
        groupCode: String,
        groupPw: String,
    ) {
        viewModelScope.launch {
            _createResult.value = DataResourceResult.Loading
            try {
                // 1. 그룹 생성
                val createResult = groupRepository.create(
                    Group(
                        groupName = groupName,
                        groupCode = groupCode,
                        groupPw = groupPw
                    )
                ).awaitResult()
                val groupId = when (createResult) {
                    is DataResourceResult.Success -> createResult.data
                    is DataResourceResult.Failure -> {
                        Log.e("GroupVM_Create", "그룹 생성 실패", createResult.exception)
                        fail(createResult.exception, "그룹 생성에 실패했습니다.")
                        return@launch
                    }
                    else -> return@launch // awaitResult는 Success/Failure만 반환한다
                }

                // 2. 사용자 문서에 그룹 ID 기록
                val updateResult = userRepository.updateUserGroupDocumentId(userId, groupId).awaitResult()
                if (updateResult is DataResourceResult.Failure) {
                    Log.e("GroupVM_Create", "사용자 그룹 ID 업데이트 실패", updateResult.exception)
                    fail(updateResult.exception, "사용자 정보 업데이트에 실패했습니다.")
                    return@launch
                }

                // 3. 그룹 멤버 목록에 사용자 추가
                val addResult = groupRepository.addUserToGroup(groupId, userId).awaitResult()
                if (addResult is DataResourceResult.Failure) {
                    Log.e("GroupVM_Create", "Failed to add user $userId to group $groupId members list.", addResult.exception)
                    fail(addResult.exception, "그룹 생성 중 오류가 발생했습니다.")
                    return@launch
                }

                // 4. 세션 갱신 후 성공 처리
                refreshSessionUser(userId, "GroupVM_Create")
                _createResult.value = DataResourceResult.Success(Unit)
                _snackBarMessage.emit("그룹이 성공적으로 생성되었습니다!")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GroupVM_Create", "그룹 생성 처리 중 예외", e)
                fail(e, "그룹 생성 중 오류가 발생했습니다.")
            }
        }
    }

    fun checkGroupCodeDuplicate(
        groupCode: String
    ) {
        if (groupCode.isBlank()){
            _isGroupCodeDuplicate.value = null
            return
        }
        viewModelScope.launch {
            _isGroupCodeDuplicate.value = DataResourceResult.Loading
            Log.d("GroupVM_CheckCode", "Checking duplicate for group code: $groupCode")

            groupRepository.isGroupCodeExist(groupCode)
                .collect { result ->
                    _isGroupCodeDuplicate.value = result
                    when (result) {
                        is DataResourceResult.Success -> {
                            if (result.data == true) {
                                Log.i("GroupVM_CheckCode", "Group code '$groupCode' already exists.")
                                showSnackBar("이미 사용중인 코드입니다")
                            } else {
                                Log.i("GroupVM_CheckCode", "Group code '$groupCode' is available.")
                                showSnackBar("사용 가능한 코드입니다.")
                            }
                        }
                        is DataResourceResult.Failure -> {
                            Log.e("GroupVM_CheckCode", "Failed to check group code: $groupCode", result.exception)
                            showSnackBar("사용할 수 없는 코드입니다.")
                        }
                        else -> {}
                    }
                }
        }
    }

    fun clearDuplicateCheckResult() {
        _isGroupCodeDuplicate.value = null
    }

    fun joinGroup(
        userId: String,
        groupCode: String,
        groupPw: String
    ) {
        viewModelScope.launch {
            _createResult.value = DataResourceResult.Loading
            try {
                // 1. 그룹 조회 및 검증
                val group = when (val groupResult = groupRepository.getGroupByCode(groupCode).awaitResult()) {
                    is DataResourceResult.Success -> groupResult.data
                    is DataResourceResult.Failure -> {
                        Log.e("GroupVM_Join", "Failed to get group by code: $groupCode", groupResult.exception)
                        fail(groupResult.exception, "그룹 정보를 가져오는데 실패했습니다.")
                        return@launch
                    }
                    else -> return@launch // awaitResult는 Success/Failure만 반환한다
                }
                val groupDocumentId = group?.groupDocumentId
                if (group == null || groupDocumentId == null) {
                    fail(Exception("그룹을 찾을 수 없습니다."), "그룹을 찾을 수 없습니다.")
                    return@launch
                }
                if (group.groupPw != groupPw) {
                    fail(Exception("비밀번호가 일치하지 않습니다."), "비밀번호가 일치하지 않습니다.")
                    return@launch
                }

                // 2. 사용자 문서에 그룹 ID 기록
                val updateResult = userRepository.updateUserGroupDocumentId(userId, groupDocumentId).awaitResult()
                if (updateResult is DataResourceResult.Failure) {
                    Log.e("GroupVM_Join", "Failed to update user $userId with group ID: $groupDocumentId", updateResult.exception)
                    fail(updateResult.exception, "사용자 정보 업데이트에 실패했습니다.")
                    return@launch
                }

                // 3. 그룹 멤버 목록에 사용자 추가
                val addResult = groupRepository.addUserToGroup(groupDocumentId, userId).awaitResult()
                if (addResult is DataResourceResult.Failure) {
                    Log.e("GroupVM_Join", "Failed to add user $userId to group $groupDocumentId members list.", addResult.exception)
                    fail(addResult.exception, "그룹 가입 중 오류가 발생했습니다.")
                    return@launch
                }

                // 4. 세션 갱신 후 성공 처리
                refreshSessionUser(userId, "GroupVM_Join")
                _createResult.value = DataResourceResult.Success(Unit)
                _snackBarMessage.emit("그룹에 성공적으로 가입되었습니다!")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GroupVM_Join", "그룹 가입 처리 중 예외", e)
                fail(e, "그룹 가입 중 오류가 발생했습니다.")
            }
        }
    }
}

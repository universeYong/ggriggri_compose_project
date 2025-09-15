package com.ahn.ggriggri.screen.ui.group.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // SnackBar 기능을 직접 구현
    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    protected fun showSnackBar(message: String) {
        viewModelScope.launch {
            _snackBarMessage.emit(message)
        }
    }

    private val _createResult = MutableStateFlow<DataResourceResult<Unit>?>(null)
    val createResult: StateFlow<DataResourceResult<Unit>?> = _createResult

    private val _isGroupCodeDuplicate = MutableStateFlow<DataResourceResult<Boolean>?>(null)
    val isGroupCodeDuplicate: StateFlow<DataResourceResult<Boolean>?> = _isGroupCodeDuplicate

    fun createGroupAndUpdateUser(
        userId: String,
        groupName: String,
        groupCode: String,
        groupPw: String,
    ) {
        viewModelScope.launch {
            _createResult.value = DataResourceResult.Loading
            
            groupRepository.create(
                Group(
                    groupName = groupName,
                    groupCode = groupCode,
                    groupPw = groupPw
                )
            ).collect { result ->
                when (result) {
                    is DataResourceResult.Loading -> {
                        _createResult.value = DataResourceResult.Loading
                    }

                    is DataResourceResult.Success -> {
                        val groupId = result.data
                        userRepository.updateUserGroupDocumentId(userId, groupId)
                            .collect { updateResult ->
                                when (updateResult) {
                                    is DataResourceResult.Success -> {
                                        Log.d("MakeGroup", "사용자 그룹 ID 업데이트 성공")
                                        
                                        groupRepository.addUserToGroup(groupId, userId)
                                            .filter { it !is DataResourceResult.Loading }
                                            .collect { groupUserAddResult ->
                                                when (groupUserAddResult) {
                                                    is DataResourceResult.Success -> {
                                                        Log.i("GroupVM_Create", "User $userId successfully added to group $groupId members list.")
                                                        _createResult.value = DataResourceResult.Success(Unit)
                                                        showSnackBar("그룹이 성공적으로 생성되었습니다!")
                                                    }
                                                    is DataResourceResult.Failure -> {
                                                        Log.e("GroupVM_Create", "Failed to add user $userId to group $groupId members list.", groupUserAddResult.exception)
                                                        _createResult.value = DataResourceResult.Failure(groupUserAddResult.exception)
                                                        showSnackBar("그룹 생성 중 오류가 발생했습니다.")
                                                    }
                                                    else -> {}
                                                }
                                            }
                                    }
                                    is DataResourceResult.Failure -> {
                                        Log.e("MakeGroup", "사용자 그룹 ID 업데이트 실패", updateResult.exception)
                                        _createResult.value = DataResourceResult.Failure(updateResult.exception)
                                        showSnackBar("사용자 정보 업데이트에 실패했습니다.")
                                    }
                                    else -> {}
                                }
                            }
                    }
                    is DataResourceResult.Failure -> {
                        Log.e("MakeGroup", "그룹 생성 실패", result.exception)
                        _createResult.value = DataResourceResult.Failure(result.exception)
                        showSnackBar("그룹 생성에 실패했습니다.")
                    }

                    else -> { }
                }
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

    fun joinGroup(
        userId: String,
        groupCode: String,
        groupPw: String
    ){
      viewModelScope.launch {
          _createResult.value = DataResourceResult.Loading
          Log.d("GroupVM_Join", "Attempting to join group. User: $userId, Code: $groupCode")

          // 그룹 정보 가져오기
          groupRepository.getGroupByCode(groupCode)
              .filter { it !is DataResourceResult.Loading }
              .collect { groupResult ->
                  when (groupResult) {
                      is DataResourceResult.Success -> {
                          val group = groupResult.data
                          if (group == null || group.groupDocumentId == null){
                              Log.w("GroupVM_Join", "Group not found or documentId is null for code: $groupCode")
                              _createResult.value = DataResourceResult.Failure(Exception("그룹을 찾을 수 없습니다."))
                              showSnackBar("그룹을 찾을 수 없습니다.")
                              return@collect
                          }
                          Log.d("GroupVM_Join", "Group found: ${group.groupName}, ID: ${group.groupDocumentId}")

                          if (group.groupPw != groupPw) {
                              Log.w("GroupVM_Join", "Password mismatch for group: ${group.groupDocumentId}")
                              _createResult.value = DataResourceResult.Failure(Exception("비밀번호가 일치하지 않습니다."))
                              showSnackBar("비밀번호가 일치하지 않습니다.")
                              return@collect
                          }
                          Log.d("GroupVM_Join", "Password matched for group: ${group.groupDocumentId}")

                          val actualGroupDocumentId = group.groupDocumentId
                          userRepository.updateUserGroupDocumentId(userId, actualGroupDocumentId)
                              .filter {it !is DataResourceResult.Loading}
                              .collect { userUpdateResult ->
                                  when(userUpdateResult) {
                                      is DataResourceResult.Success -> {
                                          Log.d("GroupVM_Join", "User $userId successfully updated with group ID: $actualGroupDocumentId")
                                          groupRepository.addUserToGroup(actualGroupDocumentId,userId)
                                              .filter { it !is DataResourceResult.Loading }
                                              .collect { groupUserAddResult ->
                                                  when (groupUserAddResult) {
                                                      is DataResourceResult.Success -> {
                                                          Log.i("GroupVM_Join", "User $userId successfully added to group $actualGroupDocumentId members list.")
                                                          _createResult.value = DataResourceResult.Success(Unit)
                                                          showSnackBar("그룹에 성공적으로 가입되었습니다!")
                                                      }
                                                      is DataResourceResult.Failure -> {
                                                          Log.e("GroupVM_Join", "Failed to add user $userId to group $actualGroupDocumentId members list.", groupUserAddResult.exception)
                                                          _createResult.value = DataResourceResult.Failure(groupUserAddResult.exception)
                                                          showSnackBar("그룹 가입 중 오류가 발생했습니다.")
                                                      }
                                                      else -> {}
                                                  }
                                              }
                                      }
                                      is DataResourceResult.Failure -> {
                                          Log.e("GroupVM_Join", "Failed to update user $userId with group ID: $actualGroupDocumentId", userUpdateResult.exception)
                                          _createResult.value = DataResourceResult.Failure(userUpdateResult.exception)
                                          showSnackBar("사용자 정보 업데이트에 실패했습니다.")
                                      }
                                      else -> {}
                                  }
                              }
                      }
                      is DataResourceResult.Failure -> {
                          Log.e("GroupVM_Join", "Failed to get group by code: $groupCode", groupResult.exception)
                          _createResult.value = DataResourceResult.Failure(groupResult.exception)
                          showSnackBar("그룹 정보를 가져오는데 실패했습니다.")
                      }
                      else -> {}
                  }
              }
      }
    }


}

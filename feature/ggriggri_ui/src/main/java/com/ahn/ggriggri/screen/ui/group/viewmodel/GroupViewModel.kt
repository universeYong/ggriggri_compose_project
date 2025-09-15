package com.ahn.ggriggri.screen.ui.group.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ahn.common_ui.components.SnackBarViewModel
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) : SnackBarViewModel() {

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
            groupRepository.create(
                Group(
                    groupName = groupName,
                    groupCode = groupCode,
                    groupPw = groupPw
                )
            ).collect { result ->
                when (result) {
                    is DataResourceResult.Loading -> {

                    }

                    is DataResourceResult.Success -> {
                        val groupId = result.data
                        userRepository.updateUserGroupDocumentId(userId, groupId)
                            .collect { updateResult ->
                                // 업데이트 결과 처리
                            }
                        Log.d("MakeGroup", "그룹 생성 성공, 그룹 ID: ${groupId}")

                        groupRepository.addUserToGroup(groupId,userId)
                            .filter { it !is DataResourceResult.Loading }
                            .collect { groupUserAddResult ->
                                if (groupUserAddResult is DataResourceResult.Success) {
                                    Log.i("GroupVM_Create", "User $userId successfully added to group $groupId members list.")
                                } else if (groupUserAddResult is DataResourceResult.Failure) {
                                    Log.e("GroupVM_Create", "Failed to add user $userId to group $groupId members list.", groupUserAddResult.exception)
                                }
                            }
                    }
                    // 생성된 그룹 ID로 사용자 정보 업데이트
                    is DataResourceResult.Failure -> Log.e(
                        "MakeGroup",
                        "사용자 그룹 ID 업데이트 실패",
                        result.exception
                    )

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
                              return@collect
                          }
                          Log.d("GroupVM_Join", "Group found: ${group.groupName}, ID: ${group.groupDocumentId}")

                          if (group.groupPw != groupPw) {
                              Log.w("GroupVM_Join", "Password mismatch for group: ${group.groupDocumentId}")
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
                                                  if (groupUserAddResult is DataResourceResult.Success) {
                                                      Log.i("GroupVM_Join", "User $userId successfully added to group $actualGroupDocumentId members list.")
                                                  } else if (groupUserAddResult is DataResourceResult.Failure) {
                                                      Log.e("GroupVM_Join", "Failed to add user $userId to group $actualGroupDocumentId members list.", groupUserAddResult.exception)
                                                  }
                                              }
                                      }
                                      is DataResourceResult.Failure -> {
                                          Log.e("GroupVM_Join", "Failed to update user $userId with group ID: $actualGroupDocumentId", userUpdateResult.exception)
                                      }
                                      else -> {}
                                  }
                              }
                      }
                      is DataResourceResult.Failure -> {
                          Log.e("GroupVM_Join", "Failed to get group by code: $groupCode", groupResult.exception)
                      }
                      else -> {}
                  }
              }
      }
    }


}

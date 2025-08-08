package com.ahn.ggriggri.screen.ui.group.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.data.datasource.GroupDataSource
import com.ahn.data.datasource.UserDataSource
import com.ahn.data.remote.firebase.FirestoreGroupDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreUserDataSourceImpl
import com.ahn.data.repository.FirestoreGroupRepositoryImpl
import com.ahn.data.repository.FirestoreUserRepositoryImpl
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group

import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class GroupViewModel : ViewModel() {

    private val _createResult = MutableStateFlow<DataResourceResult<Unit>?>(null)
    val createResult: StateFlow<DataResourceResult<Unit>?> = _createResult

    private val remoteGroupDataSource: GroupDataSource = FirestoreGroupDataSourceImpl()
    private val groupRepository: GroupRepository =
        FirestoreGroupRepositoryImpl(remoteGroupDataSource)

    private val remoteUserDataSource: UserDataSource = FirestoreUserDataSourceImpl()
    private val userRepository: UserRepository = FirestoreUserRepositoryImpl(remoteUserDataSource)

//    private val _uiState = MutableStateFlow(Group())
//    val uiState: StateFlow<Group> = _uiState
//
//    // 그룹 코드 중복 체크
//    fun checkGroupCodeDuplicate(checkCode: String){
//
//    }

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
                    }
                    // 생성된 그룹 ID로 사용자 정보 업데이트
                    is DataResourceResult.Failure -> Log.e(
                        "MakeGroup",
                        "사용자 그룹 ID 업데이트 실패",
                        result.exception
                    )

                    else -> { /* loading 또는 기타 상태 */
                    }
                }
            }
        }
    }
}


//    // 그룹 생성
//    fun createGroup(groupName: String, groupCode: String, groupPw: String) {
//
//    viewModelScope.launch {
//        groupRepository.create(group).collect { result ->
//            when(result) {
//                is DataResourceResult.Loading -> {
//
//                }
//                is DataResourceResult.Success -> {
//                    _createResult.value = result
//                    Log.d("MakeGroup", "그룹 생성 성공")
//                }
//                is DataResourceResult.Failure -> {
//
//                }
//                else -> {}
//            }
//        }
//    }
//
//
//    }
//    // 그룹 가입
//    fun joinGroup() {
//
//    }
//    // 입력값 업데이트
//    fun onGroupNameChange(value: String) {
//
//    }

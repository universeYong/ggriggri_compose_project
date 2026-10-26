package com.ahn.ggriggri.testutil

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Request
import com.ahn.domain.repository.RequestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart

/** 요청 문서를 MutableStateFlow로 들고 있어 실시간 갱신을 흉내 내는 RequestRepository. */
class FakeRequestRepository : RequestRepository {
    /** requestId별 단건 구독 결과. 없으면 Success(null)이 흐른다. */
    val requestResults = mutableMapOf<String, MutableStateFlow<DataResourceResult<Request?>>>()

    val getRequestByIdCalls = mutableListOf<String>()

    fun put(request: Request) {
        resultFlow(request.requestId).value = DataResourceResult.Success(request)
    }

    fun resultFlow(requestId: String): MutableStateFlow<DataResourceResult<Request?>> =
        requestResults.getOrPut(requestId) { MutableStateFlow(DataResourceResult.Success(null)) }

    override suspend fun getRequestById(requestId: String): Flow<DataResourceResult<Request?>> {
        getRequestByIdCalls += requestId
        return resultFlow(requestId).onStart { emit(DataResourceResult.Loading) }
    }

    override suspend fun create(request: Request): Flow<DataResourceResult<Unit>> = notUsed()
    override suspend fun read(groupId: String): Flow<DataResourceResult<List<Request>>> = notUsed()
    override suspend fun readAllRequests(groupId: String): Flow<DataResourceResult<List<Request>>> = notUsed()
    override suspend fun update(request: Request): Flow<DataResourceResult<Unit>> = notUsed()
    override suspend fun delete(requestId: String): Flow<DataResourceResult<Unit>> = notUsed()

    private fun notUsed(): Nothing = throw UnsupportedOperationException("이 테스트에서는 쓰지 않음")
}

package com.ahn.ggriggri.testutil

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Response
import com.ahn.domain.repository.ResponseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** requestId별 응답 목록을 지정하는 ResponseRepository. 기본은 Loading 다음 Success(빈 목록). */
class FakeResponseRepository : ResponseRepository {
    val responses = mutableMapOf<String, List<Response>>()

    /** 지정하면 read가 이 결과를 Success 대신 흘린다. */
    var readFailure: Throwable? = null

    override suspend fun read(requestDocumentId: String): Flow<DataResourceResult<List<Response>>> {
        val failure = readFailure
        return if (failure != null) {
            flowOf(DataResourceResult.Loading, DataResourceResult.Failure(failure))
        } else {
            flowOf(DataResourceResult.Loading, DataResourceResult.Success(responses[requestDocumentId].orEmpty()))
        }
    }

    override suspend fun create(requestDocumentId: String, response: Response): Flow<DataResourceResult<Response>> = notUsed()
    override suspend fun update(requestDocumentId: String, response: Response): Flow<DataResourceResult<Response>> = notUsed()
    override suspend fun delete(requestDocumentId: String, responseId: String): Flow<DataResourceResult<Unit>> = notUsed()
    override suspend fun getResponseById(requestDocumentId: String, responseId: String): Flow<DataResourceResult<Response?>> = notUsed()

    private fun notUsed(): Nothing = throw UnsupportedOperationException("이 테스트에서는 쓰지 않음")
}

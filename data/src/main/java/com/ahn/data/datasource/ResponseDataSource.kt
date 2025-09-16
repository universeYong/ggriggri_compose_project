package com.ahn.data.datasource

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Response

interface ResponseDataSource {
    suspend fun create(requestDocumentId: String, response: Response): DataResourceResult<Response>
    suspend fun read(requestDocumentId: String): DataResourceResult<List<Response>>
    suspend fun update(requestDocumentId: String, response: Response): DataResourceResult<Response>
    suspend fun delete(requestDocumentId: String, responseId: String): DataResourceResult<Unit>
    suspend fun getResponseById(requestDocumentId: String, responseId: String): DataResourceResult<Response?>
}

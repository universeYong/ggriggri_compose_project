package com.ahn.domain.model

data class  Request(
    var requestId: String = "",
    val requestTime: Long = 0L,
    val requestUserDocumentID: String = "",
    val requestMessage: String = "",
    val requestImage: String = "",
    val requestGroupDocumentID: String = "",
    val answerDeadline: Long = 0L,
    val hasAnswer: Boolean = false
){
    fun isQuestioner(currentUserId: String?): Boolean {
        val result = requestUserDocumentID == currentUserId
        return result
    }

    fun getRemainingMinutes(): Int {
        val currentTime = System.currentTimeMillis()
        val remaining = maxOf(0, answerDeadline - currentTime)
        return (remaining / (60 * 1000)).toInt()
    }

    fun isAnswerable(): Boolean {
        return System.currentTimeMillis() < answerDeadline
    }

    fun getRemainingTimeText(): String {
        val currentTime = System.currentTimeMillis()
        val remaining = maxOf(0, answerDeadline - currentTime)
        val remainingMinutes = (remaining / (60 * 1000)).toInt()
        val remainingSeconds = ((remaining % (60 * 1000)) / 1000).toInt()
        
        return when {
            remaining <= 0 -> "답변 마감"
            remainingMinutes < 60 -> "${remainingMinutes}:${remainingSeconds.toString().padStart(2, '0')}"
            else -> "${remainingMinutes / 60}:${(remainingMinutes % 60).toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}"
        }
    }
}
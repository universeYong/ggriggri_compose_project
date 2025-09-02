package com.ahn.ggrigggri.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AppSnackBarManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _messages = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _messages.asSharedFlow()

    fun showMessage(message: String) {
        scope.launch {
            _messages.emit(message)
        }
    }
}
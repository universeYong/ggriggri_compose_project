package com.ahn.ggriggri.screen.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Activity 범위로 쓰는 ViewModel. 앱을 새로 켰을 때(Activity 생성당) 자동 로그인을 한 번만 시도하게 하는 플래그를 가진다.
 * 화면 회전에는 살아남고, 앱을 종료했다 다시 켜면 새로 만들어진다.
 * (그룹 화면 등에서 로그인 화면으로 돌아온 경우에는 자동 로그인으로 다시 튕겨 나가지 않게 하기 위함)
 */
@HiltViewModel
class AppStartViewModel @Inject constructor() : ViewModel() {

    private var autoLoginConsumed = false

    /** 처음 호출될 때만 true. 자동 로그인을 시도할 차례인지 알려 준다. */
    fun consumeAutoLogin(): Boolean {
        val first = !autoLoginConsumed
        autoLoginConsumed = true
        return first
    }
}

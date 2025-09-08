package com.ahn.ggriggri.screen.ui.main.worker

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object DateChangeNotifier {

    // Unit 타입의 이벤트를 발행하는 MutableSharedFlow
    // replay = 0: 새로운 구독자는 구독 이후에 발행된 이벤트만 받음 (필요에 따라 replay 값 조절)
    // extraBufferCapacity = 1: 동시에 여러 이벤트가 빠르게 발생할 경우 일부 버퍼링
    private val _dateChangedEvents = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)

    /**
     * 날짜 변경 또는 데이터 새로고침 필요 이벤트를 구독할 수 있는 SharedFlow.
     * ViewModel 등에서 이 Flow를 collect하여 알림을 받을 수 있습니다.
     */
    val dateChangedEvents = _dateChangedEvents.asSharedFlow()

    /**
     * 날짜 변경 또는 데이터 새로고침이 필요함을 알리는 함수.
     * 예: DailyQuestionWorker가 성공적으로 실행된 후, 또는 사용자가 수동으로 새로고침 버튼을 눌렀을 때 호출.
     */
    fun notifyDateChanged() {
        // GlobalScope는 일반적으로 권장되지 않지만, Application 생명주기와 관련된
        // 간단한 이벤트 발행에는 사용될 수 있습니다.
        // 더 나은 방법은 ViewModel의 CoroutineScope나 Application 레벨의 CoroutineScope를 사용하는 것입니다.
        // 여기서는 간단히 GlobalScope를 사용합니다.
        GlobalScope.launch { // 또는 적절한 CoroutineScope 사용
            _dateChangedEvents.tryEmit(Unit) // tryEmit은 버퍼가 꽉 찼을 때 이벤트를 버릴 수 있음
            // emit은 버퍼가 찰 때까지 suspend됨
        }
    }
}
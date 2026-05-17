# FCM(푸시 알림) 기능 코드 설명

프로젝트의 FCM 관련 코드가 **무슨 역할을 하고, 어떤 순서로 동작하는지** 정리한 문서입니다.

---

## 1. 전체 흐름 한눈에 보기

```
[서버/콘솔] 푸시 발송
       ↓
[Android] GgriggriFirebaseMessagingService.onMessageReceived()
       ↓
[앱] NotificationHandler.handleNotification()  ← "이 알림 보여줄까?" 판단
       ↓
       ├─ 설정 OFF → 아무것도 안 함
       └─ 설정 ON → NotificationDisplayManager.showNotification()
                         ↓
                    [시스템] 알림 표시 (제목, 내용, 클릭 시 MainActivity)
```

**토큰 갱신 흐름:**

```
[Firebase] 새 FCM 토큰 발급
       ↓
GgriggriFirebaseMessagingService.onNewToken(token)
       ↓
FCMTokenManager.saveTokenToServer(token)  ← 서버에 "이 기기 = 이 토큰" 저장
```

---

## 2. 각 파일이 하는 일

### 2-1. GgriggriFirebaseMessagingService (알림 수신의 진입점)

**위치:** `data/.../fcm/GgriggriFirebaseMessagingService.kt`

**역할:**
- Firebase가 푸시를 보내면 **Android가 이 서비스의 메서드를 호출**합니다.
- 우리는 두 가지만 처리합니다.
  1. **메시지 받았을 때** → `onMessageReceived(remoteMessage)`  
     → `notificationHandler.handleNotification(remoteMessage)` 호출 (포그라운드에서도 알림 띄우기)
  2. **토큰이 새로 발급되었을 때** → `onNewToken(token)`  
     → `fcmTokenManager.saveTokenToServer(token)` 호출 (서버에 최신 토큰 저장)

**왜 이렇게 나눴는지:**
- `FirebaseMessagingService`는 Android가 만드는 객체라서, 여기에 알림 표시/설정 조회/토큰 전송 같은 로직을 전부 넣으면 테스트하기 어렵고 복잡해집니다.
- 그래서 **“메시지 왔다” / “토큰 바뀌었다”만 받고, 나머지는 `NotificationHandler`, `FCMTokenManager`에 맡깁니다.**

**참고:** 이 서비스는 Hilt가 생성하는 게 아니라 시스템이 생성합니다. 
따라서 `@Inject`로 넣어둔 `notificationHandler`, `fcmTokenManager`는 **그대로 두면 null 상태로 남을 수 있습니다.** 
실제로 알림/토큰이 동작하게 하려면 **아래 “5. FirebaseMessagingService에서 Hilt 사용하기”** 처럼 EntryPoint로 가져와야 합니다.

---

### 2-2. NotificationHandler (보여줄지 말지 결정)

**위치:** `data/.../fcm/NotificationHandler.kt`

**역할:**
1. FCM 메시지에서 **알림 종류**를 꺼냅니다.  
   - `remoteMessage.data["type"]` → `"question"`, `"request"`, `"response"` 등
2. **그 종류에 대해 사용자가 알림을 켜뒀는지** 확인합니다.  
   - `notificationPreferenceManager.isNotificationEnabled(notificationType)`  
   - DataStore에 저장된 설정(전체 알림 ON/OFF, 질문/요청/응답별 ON/OFF)을 봅니다.
3. **설정이 ON일 때만** 실제 알림을 띄웁니다.  
   - `notificationDisplayManager.showNotification(title, body, data)`

**왜 이렇게 나눴는지:**
- “알림을 띄울지 말지”는 **비즈니스 규칙**(설정 조회)입니다.
- “알림을 어떻게 띄울지”는 **Android UI**(NotificationCompat, 채널 등)입니다.
- 이 둘을 한 클래스에서 처리하면 나중에 설정 추가/변경 시 복잡해지므로, Handler는 “판단만” 하고 표시는 `NotificationDisplayManager`에 맡깁니다.

---

### 2-3. NotificationPreferenceManager (알림 설정 저장/조회)

**위치:** `data/.../local/NotificationPreferenceManager.kt`

**역할:**
- **DataStore**에 알림 설정을 저장하고 읽습니다.
  - 전체 알림 ON/OFF
  - 오늘의 질문 알림 ON/OFF
  - 요청 알림 ON/OFF
  - 응답 알림 ON/OFF
- `isNotificationEnabled(type)`  
  - `type`이 `"question"`/`"daily_question"` / `"request"` / `"response"`일 때 해당 설정을 보고 true/false 반환.
  - 전체가 꺼져 있으면 무조건 false.

**왜 DataStore인지:**
- 앱을 껐다 켜도 설정이 유지되어야 하고, 가벼운 키-값 저장이면 DataStore가 적합합니다.

---

### 2-4. NotificationDisplayManagerImpl (실제 알림 띄우기)

**위치:** `feature/.../notification/NotificationDisplayManagerImpl.kt`

**역할:**
1. **알림 권한** 확인 (Android 13+ 에서는 `POST_NOTIFICATIONS` 필요).  
   - 없으면 로그만 남기고 표시하지 않음.
2. **알림 클릭 시 열 화면** 준비  
   - `createNotificationIntent(data)`  
   - `MainActivity`를 띄우고, FCM `data` 맵을 그대로 Intent extra로 넣어둠.  
   - 나중에 “이 알림이 질문인지, 요청인지” 등으로 화면을 나누려면 이 extra를 쓰면 됩니다.
3. **채널 선택**  
   - `data["type"]`에 따라 채널 ID 선택 (오늘의 질문 / 요청 / 응답).
4. **NotificationCompat**으로 알림 생성 후  
   - `NotificationManagerCompat.notify(notificationId, notification)` 로 표시.

**채널을 나누는 이유:**
- Android 8부터 “알림 채널” 단위로 사용자가 소리/진동/중요도를 따로 설정할 수 있습니다.  
- 질문/요청/응답을 서로 다른 채널로 두면, 나중에 “요청 알림만 무음”처럼 세밀하게 설정할 수 있습니다.

---

### 2-5. NotificationChannelManagerImpl (채널 등록)

**위치:** `feature/.../notification/NotificationChannelManagerImpl.kt`

**역할:**
- 앱에서 사용할 **알림 채널 3개**를 한 번만 등록합니다.
  - 오늘의 질문 (고중요도, 진동 패턴 있음)
  - 새로운 요청
  - 응답 알림  
- `ggriggriAplication.onCreate()` 에서 `notificationChannelManager.createNotificationChannels()` 를 호출해 앱 시작 시 한 번 실행됩니다.

**채널을 미리 만드는 이유:**
- Android는 “한 번 등록된 채널”에만 알림을 보낼 수 있고, 채널 ID는 `NotificationDisplayManagerImpl`에서 타입별로 사용합니다.

---

### 2-6. FCMTokenManager (토큰 발급·서버 전송)

**위치:** `data/.../fcm/FCMTokenManager.kt`

**역할:**
1. **현재 FCM 토큰** 가져오기  
   - `FirebaseMessaging.getInstance().token.await()`
2. **서버에 토큰 저장**  
   - `sendTokenToServer()`: 로그인 후 등에 호출해 “지금 이 기기의 토큰”을 서버에 보냄.  
   - `saveTokenToServer(token)`: `onNewToken`에서 호출해 **새 토큰**을 서버에 반영.  
   - 둘 다 `sessionManager.currentUserFlow`로 “지금 로그인한 사용자”를 알아내서, `userRepository.updateFcmToken(userId, token)` 으로 전송합니다.

**왜 서버에 보내는지:**
- 푸시를 “누구에게 보낼지”는 보통 서버가 결정합니다.  
- 서버가 “이 사용자 = 이 FCM 토큰”을 알고 있어야, 나중에 해당 사용자에게만 푸시를 보낼 수 있습니다.

---

### 2-7. PermissionManager (알림 권한 요청)

**위치:** `data/.../fcm/PermissionManager.kt`

**역할:**
- Android 13(TIRAMISU) 이상에서 **알림 권한**(`POST_NOTIFICATIONS`)을 요청하고 결과를 처리합니다.
  - `hasNotificationPermission()`: 이미 허용됐는지
  - `shouldRequestNotificationPermission()`: 요청이 필요한지 (버전 + 미허용)
  - `requestNotificationPermission(activity)`: 시스템 권한 요청 다이얼로그 띄우기
  - `handlePermissionResult(...)`: 사용자가 허용/거부한 결과 처리
  - `shouldShowRequestPermissionRationale(activity)`: “이전에 거부했을 때 설명 보여줄지” 판단

**왜 별도 클래스인지:**
- 권한 로직을 한 곳에 두면, 로그인 화면·설정 화면 등 여러 곳에서 같은 방식으로 “알림 권한 요청”을 재사용할 수 있습니다.

---

### 2-8. FCMModule (Hilt 의존성 제공)

**위치:** `data/.../di/FCMModule.kt`

**역할:**
- FCM 관련 객체들을 **싱글톤으로 만들어 주입**합니다.
  - `FCMTokenManager`
  - `NotificationHandler`
  - `NotificationPreferenceManager`
  - `PermissionManager`  
- `NotificationDisplayManager`는 UI 모듈의 `NotificationModule`에서 제공하고, `NotificationHandler`가 그걸 받아서 사용합니다.

---

## 3. FCM 메시지 형식 (서버/콘솔에서 보낼 때)

앱이 기대하는 구조는 대략 다음과 같습니다.

- **notification (선택)**  
  - `title`, `body` → 알림에 그대로 표시됩니다.
- **data**  
  - `type`: `"question"` / `"daily_question"` / `"request"` / `"response"`  
    → 설정 조회 및 채널 선택에 사용됩니다.  
  - 그 외 키/값은 Intent extra로 전달되므로, 상세 화면으로 이동할 때 사용할 수 있습니다.

---

## 4. 알림 클릭 시 동작

- `NotificationDisplayManagerImpl.createNotificationIntent()` 에서  
  `MainActivity`를 띄우고, `data` 맵 전체를 Intent extra로 넣습니다.
- 따라서 **MainActivity(또는 네비게이션)** 에서 Intent extra를 읽어  
  `type`이나 다른 값에 따라 “질문 상세”, “요청 상세” 등으로 라우팅하면 됩니다.

---

## 5. FirebaseMessagingService에서 Hilt 사용하기 (필수 수정)

`GgriggriFirebaseMessagingService`는 **Android 시스템**이 생성하기 때문에 `@Inject`만으로는 `notificationHandler`, `fcmTokenManager`가 주입되지 않습니다.  
그래서 **Hilt EntryPoint**로 Application에서 인스턴스를 가져와야 합니다.

**예시:**

1. **EntryPoint 인터페이스 정의** (예: `data` 모듈 또는 `app` 모듈)

```kotlin
@EntryPoint
@InstallIn(SingletonComponent::class)
interface FcmEntryPoint {
    fun notificationHandler(): NotificationHandler
    fun fcmTokenManager(): FCMTokenManager
}
```

2. **서비스에서 사용**

```kotlin
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    val handler = EntryPointAccessors.fromApplication(
        applicationContext,
        FcmEntryPoint::class.java
    ).notificationHandler()
    serviceScope.launch {
        handler.handleNotification(remoteMessage)
    }
}

override fun onNewToken(token: String) {
    super.onNewToken(token)
    val tokenManager = EntryPointAccessors.fromApplication(
        applicationContext,
        FcmEntryPoint::class.java
    ).fcmTokenManager()
    CoroutineScope(Dispatchers.IO).launch {
        tokenManager.saveTokenToServer(token)
    }
}
```

이렇게 하면 FCM 메시지/토큰 수신 시 실제로 `NotificationHandler`와 `FCMTokenManager`가 사용되어 알림과 토큰 저장이 동작합니다.

---

이 문서는 “FCM 기능들이 왜 이렇게 나뉘어 있고, 각각 무슨 일을 하는지” 이해하는 데 초점을 맞춰 두었습니다.  
특정 클래스나 함수를 더 깊게 보고 싶으면 그 이름을 알려주면 됩니다.

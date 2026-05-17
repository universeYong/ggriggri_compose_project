# 끼리끼리(ggrigggri) 프로젝트 코드 리뷰 및 개선 가이드 (2026-04-14)

오늘 진행한 프로젝트 코드 분석 결과를 바탕으로, 주요 문제점과 향후 개선 방향을 정리한 문서입니다.

---

## 🏗️ 1. 아키텍처 및 모듈화 구조

### 1-1. 모듈 경계 위반 (중요도: 상)
현재 `:feature:ggriggri_ui` 모듈이 `:data` 모듈에 직접 의존하고 있습니다. 이로 인해 ViewModel에서 데이터 레이어의 구현체(`FCMTokenManager`, `PermissionManager`, `StorageRepository` 등)를 직접 참조하고 있습니다.
*   **문제점:** 데이터 레이어의 변경이 UI 레이어에 직접적인 영향을 주며, 계층 간 결합도가 높아져 단위 테스트 작성이 어려워집니다.
*   **개선 방안:** 모든 비즈니스 로직은 `domain` 모듈의 인터페이스를 통하거나 `UseCase`를 거쳐 접근하도록 수정해야 합니다.

### 1-2. 고립된 모듈 및 버전 불일치 (중요도: 중)
*   **미사용 모듈:** `core`와 `app/common` 디렉토리가 존재하지만 `settings.gradle.kts`에 포함되어 있지 않아 실제 빌드에는 사용되지 않고 있습니다.
*   **SDK 버전 불일치:** `app`, `data` 모듈은 `compileSdk 36`을 쓰지만, `feature` 모듈은 `35`를 사용하는 등 설정이 혼재되어 있습니다. 프로젝트 전체의 SDK 버전을 하나로 통일하는 것이 좋습니다.

---

## ⚙️ 2. ViewModel 및 코루틴 사용성

### 2-1. `runBlocking` 사용 및 메인 스레드 차단 (중요도: 매우 상)
`HomeViewModel.kt`에서 `runBlocking`을 사용해 `getUserName`을 호출하고 있으며, 이를 `ActiveRequestCard.kt`의 컴포즈 `derivedStateOf` 안에서 실행하고 있습니다.
*   **문제점:** 컴포지션 과정에서 UI 스레드를 직접 차단하여 앱이 심하게 버벅이거나 ANR(App Not Responding)이 발생할 수 있는 치명적인 원인입니다.
*   **개선 방안:** `getUserName`을 비동기적으로 처리하거나, 애초에 `requests`나 `profiles` 데이터를 가져올 때 이름 정보를 포함한 UI 모델로 가공하여 `StateFlow`로 노출해야 합니다.

### 2-2. 명령형 상태 업데이트 및 Flow 오용 (중요도: 중)
*   **중첩 `collect`:** `HomeViewModel`의 `init` 블록에서 `collect` 내부에서 다시 `launch`를 호출하여 작업을 수행하고 있습니다. 이는 `groupId`가 변경될 때 이전 작업이 취소되지 않고 중첩될 위험이 있습니다.
*   **개선 방안:** `flatMapLatest`나 `combine`을 활용하여 소스 데이터로부터 최종 UI 상태를 합성해내는 **선언적(Reactive)** 방식으로 전환해야 합니다.

### 2-3. ViewModel의 플랫폼 의존성 (중요도: 하)
`OAuthViewModel.kt`에서 `Activity` 객체를 함수의 파라미터로 직접 전달받고 있습니다.
*   **문제점:** ViewModel이 특정 플랫폼(Android Activity)에 종속되어 순수 유닛 테스트가 어려워집니다.
*   **개선 방안:** UI에서 권한 요청을 담당하는 별도의 `Controller`를 두거나, ViewModel은 "권한이 필요함"이라는 **상태(Effect)**만 노출하고 실제 처리는 UI(Activity)에서 수행해야 합니다.

---

## 🎨 3. Jetpack Compose UI 및 최적화

### 3-1. 하위 컴포넌트로의 ViewModel 전달 (중요도: 중)
`ActiveRequestCard`, `DefaultRequestCard` 등 하위 컴포넌트에 ViewModel 전체를 넘기고 있습니다.
*   **문제점:** 컴포넌트 간의 결합도가 높아져 재사용성이 떨어지고, Compose Preview를 작성하기가 매우 까다로워집니다.
*   **개선 방안:** 필요한 데이터 객체와 클릭 이벤트 리스너(Lambda)만 전달하도록 수정하세요.

### 3-2. 과도한 로그 및 중복 로드 (중요도: 하)
*   **로그 과다:** `HomeScreen.kt` 내부에 수많은 `Log.d`와 이를 위한 `LaunchedEffect`가 존재하여 가독성을 해칩니다.
*   **중복 호출:** ViewModel `init`과 UI `LaunchedEffect` 양쪽에서 동일한 데이터(`loadRequests`)를 중복으로 요청하고 있습니다.

---

## ✅ 향후 개선을 위한 우선순위 가이드

1.  **최우선:** `HomeViewModel`의 `runBlocking` 제거 및 `ActiveRequestCard`의 `userName` 로드 로직 비동기화.
2.  **우선:** `:feature` 모듈에서 `:data` 모듈 의존성 제거 및 `domain` 모듈 중심의 의존성 재정렬.
3.  **차선:** ViewModel 내부의 복잡한 로직을 `UseCase`로 분리하여 코드 가독성 및 유지보수성 확보.

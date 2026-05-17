package com.ahn.core.common

/**
 * Data resource result
 *
 * @param T
 * @constructor Create empty Data resource result
 *
 * 존재 이유(왜 이런 구조를 쓰는가?)
 * 명확한 상태 표현:
 * 데이터를 불러오는 작업의 결과를 딱 네 가지(초기, 로딩, 성공, 실패) 상태로 깔끔하게 표현할 수 있습니다.
 *
 * when 구문 사용에 유리:
 * sealed class 특성상, when(expression) 구문에서 각 경우(객체/클래스)를 빠짐없이 체크할 수 있어 안전합니다.
 *
 * 에러 처리 쉬움:
 * 성공/실패/로딩/기본값 등을 한 번에 관리할 수 있어 코드의 일관성과 가독성이 높아집니다.
 *
 * 확장성:
 * 추후 필요 시 새로운 상태(예를 들어 "Empty" 등)를 편리하게 추가할 수 있습니다.
 */
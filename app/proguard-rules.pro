# 릴리스 빌드(R8 코드 축소·난독화) 규칙. 라이브러리가 자체 규칙(consumer rules)을 싣고 오는 경우는 여기에 적지 않는다.

# 크래시 스택 트레이스에서 줄 번호를 읽을 수 있게 한다(원본 파일명은 숨김).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Firestore DTO ---------------------------------------------------------
# toObject()/set()이 필드·getter·setter 이름으로 문서 필드와 매핑하고 기본 생성자를 리플렉션으로 부른다.
# 이름이 바뀌거나 지워지면 읽은 값이 전부 기본값이 되거나 저장이 깨진다.
-keep class com.ahn.data.remote.dto.** { *; }

# --- Moshi (SessionManagerImpl이 User를 JSON으로 저장·복원) -------------------------
# KotlinJsonAdapterFactory는 kotlin.Metadata와 프로퍼티 이름을 리플렉션으로 읽는다.
# 이름이 바뀌면 저장된 세션을 못 읽어 자동 로그인이 풀린다.
-keepattributes RuntimeVisibleAnnotations,Signature,InnerClasses,EnclosingMethod
-keep class kotlin.Metadata { *; }
-keep class com.ahn.domain.model.User { *; }

# --- 카카오 SDK -------------------------------------------------------------
# SDK 내부 모델이 Gson으로 (역)직렬화된다. 카카오 개발자 문서의 권장 규칙.
-keep class com.kakao.sdk.**.model.* { <fields>; }
-keep class * extends com.google.gson.TypeAdapter

# SDK가 내부에서 쓰는 Retrofit 2.9.0에는 R8 full mode용 규칙이 없다. 없으면 Call<T>·Response<T>의
# 제네릭 정보가 지워져 앱 시작 때 UserApi 호출이 "Call return type must be parameterized"로 죽는다.
# (Retrofit 2.10 이상이 자체 규칙으로 싣고 있는 것과 같은 내용)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Devain APICall

Devain APICall은 Devain 프로젝트에서의 API 통신을 담당하는 라이브러리입니다.

REST 및 XML을 통해 직접 클래스를 확장하여 사용하는 방법으로 대부분의 웹 API 호출에 적합하게 사용할 수 있습니다.

## 종속성 추가
build.gradle에 다음과 같이 추가합니다 :

```groovy
repositories {
   maven {
     url "https://repo.trinarywolf.net/releases" 
   }
}

depdendencies {
    implementation "skywolf46:devain-api-call:1.3.2"
}
```

## 사용법

### REST 호출

1. APICall 라이브러리를 초기화합니다. 기본 설정으로는 CIO를 사용하는 Ktor 프레임워크의 HttpClient로 초기화됩니다.

```kotlin
  APICallLib.init()
```

2. REST 호출을 위한 클래스를 생성합니다.
   이 예제에서는 https://localhost:8080/ 에 POST 요청을 보낼 수 있다고 가정합니다.<br>

3. 보낼 요청 클래스를 생성합니다. 모든 요청 클래스는 Request를 상속해야 합니다.

```kotlin
// 이 요청 객체는 JSONObject를 반환합니다.
class LocalhostRequest : Request<JSONObject> {
    // 제너릭스가 JSONObject임으로, 반환값은 Throwable 혹은 JSONObject 중 하나가 됩니다.
    override fun serialize(): Either<Throwable, JSONObject> {
        // 해당 예제에서는 요청 파라미터가 { test: string, test2: int } 형태인것으로 가정합니다.
        return JSONObject().apply {
            // 모든 데이터를 지정합니다.
            put("test", "test")
            put("test2", 1)
            // right()로 이 데이터가 정상 반환되었음을 알립니다.
            // left()로 예외를 통해 입력된 데이터에서 조건에 맞지 않음을 알릴수 있습니다.
            // 해당 라이브러리에서는 직접 예외를 발생시키는것을 권장하지 않습니다.
        }.right()
    }
    
}
```

4. 받을 요청 클래스를 생성합니다. 모든 요청 클래스는 Response를 상속해야 합니다.

```kotlin
// 해당 예제서는 반환이 { test3: string } 형태인 것 으로 가정합니다.
class LocalhostResponse(val test3: String) : Response {
    // 해당 라이브러리에서는 companion object를 통해 deserialize를 정의하는것을 권장합니다.
    companion object {
        fun fromJson(json: JSONObject): Either<Throwable, LocalhostResponse> {
            // 해당 예제에서는 JSONObject에서 test3를 가져와 반환합니다.
            return LocalhostResponse(json.getString("test3")).right()
        }
    }
}
```

5. RESTAPICall을 상속합니다.

```kotlin
class LocalhostAPICall : RESTAPICall<LocalhostRequest, LocalhostResponse>({ request ->
    // 이 부분에서는 Request 객체를 받아 API 호출에서 사용될 1회성 URL을 반환합니다.
    // 이 예제에서는 https://localhost:8080/을 사용함으로, 주소가 고정적입니다.
    // GET 요청을 보내는 경우, 이 부분에서 URL을 변경하여 반환할 수 있습니다.
    "https://localhost:8080/"
}) {
    // 만약 인증과 같은 헤더 설정이 필요한 경우, 이 부분에서 설정할 수 있습니다.
    override fun HeadersBuilder.applyCredential() {
        append("Authorization", "Bearer sk-abcdefghijklmnop123456")
    }

    // API가 호출된 후, 성공적으로 호출된 경우에 데이터를 파싱합니다.
    // "성공적" 호출은 일반적으로 200번대 코드를 받은 경우를 의미합니다.
    // RESTAPICall은 모든 응답을 JSONObject로 파싱하여 되돌려줍니다.
    override suspend fun parseResult(
        request: LocalhostRequest,
        response: JSONObject
    ): Either<APIError, LocalhostResponse> {
        return LocalhostResponse.fromJson(response)
    }

    // API가 실패한 경우에 데이터를 처리합니다.
    override suspend fun parseHttpError(
        request: OpenAIGPTRequest,
        response: HttpResponse,
        errorCode: Int
    ): APIError {
        println(response.headers)
        return super.parseHttpError(request, response, errorCode)
    }
}
```

6. API를 호출합니다.

```kotlin
val api = LocalhostAPICall()
val result = api.call(LocalhostRequest())
```
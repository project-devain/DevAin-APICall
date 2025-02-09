package skywolf46.devain.apicall

import arrow.core.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.koin.core.component.get
import org.koin.core.component.inject
import skywolf46.devain.apicall.errors.PreconditionError
import skywolf46.devain.apicall.errors.StandardRestAPIError
import skywolf46.devain.apicall.errors.UnexpectedError
import skywolf46.devain.apicall.networking.Request
import skywolf46.devain.apicall.networking.Response

abstract class RESTAPICall<REQUEST : Request<JSONObject>, RESPONSE : Response>(
    val endpointProvider: (REQUEST) -> String,
    client: Option<HttpClient> = None,
    val method: HttpMethod = HttpMethod.Get,
    jsonParser : Option<JSONParser> = None
) : APICall<REQUEST, RESPONSE> {

    private val client by lazy {
        client.getOrElse { get<HttpClient>() }
    }

    private val jsonParser by lazy {
        jsonParser.getOrElse { get<JSONParser>() }
    }

    override suspend fun call(request: REQUEST): Either<APIError, RESPONSE> {
        val prebuiltRequest = request.serialize().getOrElse { return PreconditionError(it).left() }
        return Either.catch {
            val result = client.request(endpointProvider(request)) {
                method = this@RESTAPICall.method
                contentType(ContentType.Application.Json)
                headers {
                    applyCredential()
                }
                setBody(prebuiltRequest.toJSONString())
            }
            if (result.status.value in 200..299) {
                if (result.status.value == 204) {
                    // No content
                    parseResult(request, JSONObject())  
                } else {
                    parseResult(request, jsonParser.parse(result.bodyAsText()) as JSONObject)
                }
            } else {
                parseHttpError(request, result, result.status.value).left()
            }
        }.getOrElse {
            onError(it).left()
        }
    }

    protected abstract suspend fun parseResult(request: REQUEST, response: JSONObject): Either<APIError, RESPONSE>

    protected open suspend fun parseHttpError(request: REQUEST, response: HttpResponse, errorCode: Int): APIError {
        return StandardRestAPIError(errorCode, response.bodyAsText())
    }

    protected open suspend fun onError(throwable: Throwable): APIError {
        return UnexpectedError(throwable)
    }

    protected open fun HeadersBuilder.applyCredential() {
        // Do nothing
    }
}
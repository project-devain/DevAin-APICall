package skywolf46.devain.apicall

import arrow.core.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.koin.core.component.get
import org.w3c.dom.Document
import org.xml.sax.InputSource
import skywolf46.devain.apicall.errors.PreconditionError
import skywolf46.devain.apicall.errors.StandardRestAPIError
import skywolf46.devain.apicall.errors.UnexpectedError
import skywolf46.devain.apicall.networking.Request
import skywolf46.devain.apicall.networking.Response
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

abstract class XMLRESTAPICall <REQUEST : Request<Any>, RESPONSE : Response>(
    val endpointProvider: (REQUEST) -> String,
    client: Option<HttpClient> = None,
    val apiMethod: HttpMethod = HttpMethod.Get
) : APICall<REQUEST, RESPONSE> {

    private val client by lazy {
        client.getOrElse { get<HttpClient>() }
    }

    private val xmlParserFactory  = DocumentBuilderFactory.newInstance()

    override suspend fun call(request: REQUEST): Either<APIError, RESPONSE> {
        val prebuiltRequest = request.serialize().getOrElse { return PreconditionError(it).left() }
        return Either.catch {
            val result = client.request(endpointProvider(request)) {
                method = apiMethod
                contentType(ContentType.Application.Json)
                headers {
                    applyCredential()
                }
                setBody(prebuiltRequest)
            }
            if (result.status.value != 200) {
                parseHttpError(request, result, result.status.value).left()
            } else {
                parseResult(request, xmlParserFactory.newDocumentBuilder().parse(InputSource(StringReader(result.bodyAsText()))))
            }
        }.getOrElse {
            onError(it).left()
        }
    }

    protected abstract suspend fun parseResult(request: REQUEST, response: Document): Either<APIError, RESPONSE>

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
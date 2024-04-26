package skywolf46.devain.apicall.networking

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import org.json.simple.JSONObject
data class GetRequest<T : Any>(val key: String, val section: Option<String> = None) : Request<JSONObject> {
    override fun serialize(): Either<Throwable, JSONObject> {
        return JSONObject().right()
    }
}
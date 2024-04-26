package skywolf46.devain.apicall.networking

import arrow.core.Either
import arrow.core.right
import org.json.simple.JSONObject

open class EmptyJSONRequest : Request<JSONObject> {
    override fun serialize(): Either<Throwable, JSONObject> {
        return JSONObject().right()
    }
}
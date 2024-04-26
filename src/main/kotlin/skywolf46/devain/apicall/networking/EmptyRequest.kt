package skywolf46.devain.apicall.networking

import arrow.core.Either
import arrow.core.right

open class EmptyRequest : Request<Unit> {
    override fun serialize(): Either<Throwable, Unit> {
        return Unit.right()
    }
}

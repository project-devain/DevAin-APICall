package skywolf46.devain.apicall.networking

import arrow.core.Either
import org.koin.core.component.KoinComponent

interface Request<T : Any> : KoinComponent{
    fun serialize(): Either<Throwable, T>
}
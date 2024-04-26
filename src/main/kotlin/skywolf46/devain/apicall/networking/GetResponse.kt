package skywolf46.devain.apicall.networking


data class GetResponse<T : Any>(val value: T) : Response
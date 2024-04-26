package skywolf46.devain.apicall.networking

class GenericMapResponse<K, V> : LinkedHashMap<K, V>(), Response, Cloneable {
    override fun clone(): GenericMapResponse<K, V> {
        return GenericMapResponse<K, V>().also {
            it.putAll(this)
        }
    }
}
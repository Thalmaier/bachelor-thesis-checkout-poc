package core.domain.common

sealed class ModifiedResult<out T>(val modified: Boolean, val payload: T) {

    abstract fun <K> setPayload(payload: K): ModifiedResult<K>

    class Refreshed<T>(modifiedObject: T) : ModifiedResult<T>(true, modifiedObject) {
        override fun <K> setPayload(payload: K) = Refreshed(payload)
    }

    class Updated<T>(modifiedObject: T) : ModifiedResult<T>(true, modifiedObject) {
        override fun <K> setPayload(payload: K) = Updated(payload)
    }

    class Unchanged<T>(unchangedObject: T) : ModifiedResult<T>(false, unchangedObject) {
        override fun <K> setPayload(payload: K) = Unchanged(payload)
    }

    fun <K> toRefreshResult(newPayload: K) = this.modified.refreshResult(newPayload)
    fun <K> toUpdateResult(newPayload: K) = this.modified.toUpdateResult(newPayload)

}

fun <T> Boolean.toUpdateResult(payload: T) = when {
    this -> ModifiedResult.Updated(payload)
    else -> ModifiedResult.Unchanged(payload)
}

fun <T> Boolean.refreshResult(payload: T) = when {
    this -> ModifiedResult.Refreshed(payload)
    else -> ModifiedResult.Unchanged(payload)
}

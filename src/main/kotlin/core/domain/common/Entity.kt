package core.domain.common

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * Class representing a entity with implemented [equals] and [hashCode] function
 */
abstract class Entity(@JsonIgnore private val _entityId: Any) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != this.javaClass) return false

        other as Entity

        if (this._entityId != other._entityId) return false

        return true
    }

    override fun hashCode(): Int {
        return _entityId.hashCode()
    }
}

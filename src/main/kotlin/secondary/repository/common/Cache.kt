package secondary.repository.common

/**
 * Interface for a class responsible for caching objects
 */
interface Cache<K, E> {

    /**
     * Get an entity if it is in the cache
     */
    fun get(key: K): E?

    /**
     * Get an entity based on a key if it is in the cache and [isValid] returns true,
     * else returns the entity returned by the [fallback] function and saves it in the cache.
     */
    fun getAndUpdateIfInvalid(key: K, fallback: () -> E): E

    /**
     * Returns true based on certain attributes like age of the entry
     */
    fun isValid(cachedEntity: Cacheable<K, E>): Boolean

    /**
     * Adds and entity to the cache
     */
    fun put(entity: Cacheable<K, E>)
}



package core.domain.common


/**
 * Chains two objects together, returning the right most
 */
infix fun <T> T?.and(other: T?): T? {
    return other
}

/**
 * Throws a exception if the [condition] is true
 */
fun <T> T.throwIf(condition: Boolean, execute: () -> Exception): T = this.apply {
    if (condition) {
        throw execute()
    }
}

/**
 * Returns true if any element in the list is not null
 */
fun anyNotNull(vararg args: Any?): Boolean = args.firstOrNull { it != null }?.let { true } ?: false

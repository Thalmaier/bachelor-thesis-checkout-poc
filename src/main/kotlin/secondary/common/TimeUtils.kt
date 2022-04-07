package secondary.common

import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Util object for all operations on a [LocalDateTime]
 */
object TimeUtils {

    /**
     * Returns the current time
     */
    fun dateTimeNow(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

    /**
     * Returns true if the passed [time] is older than seconds passed by [epochSeconds]
     */
    fun olderThan(time: LocalDateTime, epochSeconds: Long): Boolean {
        return epochSeconds(dateTimeNow()) - epochSeconds(time) - epochSeconds > 0
    }

    /**
     * Returns the epoch time in seconds based on a [LocalDateTime]
     */
    private fun epochSeconds(time: LocalDateTime) = time.toEpochSecond(ZoneOffset.UTC)
}
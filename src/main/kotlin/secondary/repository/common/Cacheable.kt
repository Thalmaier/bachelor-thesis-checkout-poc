package secondary.repository.common

import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.bson.codecs.pojo.annotations.BsonId
import secondary.common.TimeUtils
import java.time.LocalDateTime

/**
 * Extend this class if the subtype should be [Cacheable]
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
abstract class Cacheable<K, E>(
    @BsonId val id: K,
    val payload: E,
    val insertedAt: LocalDateTime = TimeUtils.dateTimeNow(),
)

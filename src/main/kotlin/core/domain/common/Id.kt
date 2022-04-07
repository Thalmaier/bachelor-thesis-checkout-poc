package core.domain.common

import java.util.*

/**
 * Abstract class for an Id containing a [UUID]
 */
abstract class Id(open val id: UUID = UUID.randomUUID())

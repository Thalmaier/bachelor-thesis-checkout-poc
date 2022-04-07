package core.application.metric

data class MetricsDTO(
    val combinedWaitTime: Long = 0L,
    val externalApiCalls: Map<String, Long> = mapOf(),
    val combinedDatabaseWrite: Long = 0L,
    val combinedDatabaseRead: Long = 0L,
    val databaseReadWriteCalls: Map<String, ReadWriteCallDTO> = mapOf(),
)

data class ReadWriteCallDTO(
    var reads: Long = 0L,
    var writes: Long = 0L,
)
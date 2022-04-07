package core.application.metric

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryMetricOperator : MetricOperator {
    private val combinedWaitTime: AtomicLong = AtomicLong()
    private val externalApiCalls: ConcurrentMap<String, AtomicLong> = ConcurrentHashMap()
    private val combinedDatabaseWrite: AtomicLong = AtomicLong()
    private val combinedDatabaseRead: AtomicLong = AtomicLong()
    private val databaseReadWriteCalls: ConcurrentMap<String, ReadWriteCall> = ConcurrentHashMap()

    override fun read(name: String) {
        combinedDatabaseRead.incrementAndGet()
        databaseReadWriteCalls.getOrPut(name) { ReadWriteCall() }.incReadAndGet()
    }

    override fun write(name: String) {
        combinedDatabaseWrite.incrementAndGet()
        databaseReadWriteCalls.getOrPut(name) { ReadWriteCall() }.incWriteAndGet()
    }

    override fun apiCall(name: String, waitTime: Long?) {
        externalApiCalls.getOrPut(name) { AtomicLong() }.incrementAndGet()
        if (waitTime != null && waitTime > 0) {
            combinedWaitTime.addAndGet(waitTime)
        }
    }

    override fun getMetrics(): MetricsDTO {
        return MetricsDTO(
            combinedWaitTime.get(),
            externalApiCalls.mapValues { it.value.get() },
            combinedDatabaseWrite.get(),
            combinedDatabaseRead.get(),
            databaseReadWriteCalls.mapValues {
                ReadWriteCallDTO(it.value.reads.get(), it.value.writes.get())
            }
        )
    }
}

data class ReadWriteCall(
    var reads: AtomicLong = AtomicLong(),
    var writes: AtomicLong = AtomicLong(),
) {
    fun incReadAndGet() = reads.incrementAndGet()
    fun incWriteAndGet() = writes.incrementAndGet()
}


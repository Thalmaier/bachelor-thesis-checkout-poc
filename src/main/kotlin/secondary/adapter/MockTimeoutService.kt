package secondary.adapter

import config.Config
import core.application.metric.Metric
import mu.KotlinLogging

object MockTimeoutService {

    private val logger = KotlinLogging.logger {}
    private val simulateWaitTime by lazy { Config().application.simulateApiCalls }

    fun timeout(ms: Long, name: String) {
        if (simulateWaitTime) {
            logger.info { "Waiting for $ms ms to simulate $name api call" }
            Metric.apiCall(name, ms)
            Thread.sleep(ms)
        } else {
            Metric.apiCall(name)
        }
    }

}
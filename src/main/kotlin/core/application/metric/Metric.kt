package core.application.metric

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Metric : KoinComponent, MetricOperator {

    private val operator: MetricOperator by inject()

    override fun read(name: String) = operator.read(name)

    override fun write(name: String) = operator.write(name)

    override fun apiCall(name: String, waitTime: Long?) = operator.apiCall(name, waitTime)

    override fun getMetrics(): MetricsDTO = operator.getMetrics()

}



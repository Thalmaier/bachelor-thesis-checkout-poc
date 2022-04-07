package core.application.metric

class NoOpsMetricOperator : MetricOperator {

    override fun read(name: String) = Unit

    override fun write(name: String) = Unit

    override fun apiCall(name: String, waitTime: Long?) = Unit

    override fun getMetrics(): MetricsDTO = MetricsDTO()

}
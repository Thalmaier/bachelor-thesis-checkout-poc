package core.application.metric

interface MetricOperator {

    fun read(name: String)

    fun write(name: String)

    fun apiCall(name: String, waitTime: Long? = null)

    fun getMetrics(): MetricsDTO

}
package secondary.repository.common

import config.DatabaseConfig
import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.Defaults
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.config.RuntimeConfig
import de.flapdoodle.embed.process.config.process.ProcessOutput
import de.flapdoodle.embed.process.io.Processors
import de.flapdoodle.embed.process.runtime.Network
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import java.io.Closeable

object InMemoryDatabaseProcess : Closeable {

    private var inMemoryDatabase: Closeable? = null

    fun startInMemoryDatabaseIfNecessary(config: DatabaseConfig) {
        if (useInMemoryPostgreDB(config)) {
            inMemoryDatabase = startInMemoryPostgres(config.relational.inMemoryPostgresPort)
        } else if (usesInMemoryMongoDB(config)) {
            val mongod = startInMemoryMongoDB(config.documentOriented.inMemoryMongodbPort)
            inMemoryDatabase = Closeable { mongod?.stop() }
        }
    }

    fun usesInMemoryMongoDB(config: DatabaseConfig): Boolean {
        return !config.useRelationalDatabase && config.documentOriented.useInMemoryMongodb
    }

    fun useInMemoryPostgreDB(config: DatabaseConfig): Boolean {
        return config.useRelationalDatabase && config.relational.useInMemoryPostgres
    }

    private fun startInMemoryPostgres(port: Int): EmbeddedPostgres {
        return EmbeddedPostgres.builder()
            .setPort(port)
            .start()
    }

    private fun startInMemoryMongoDB(port: Int): MongodProcess? {
        val runtimeConfig: RuntimeConfig = Defaults
            .runtimeConfigFor(Command.MongoD)
            .processOutput(
                ProcessOutput.builder()
                    .output(Processors.silent())
                    .error(Processors.silent())
                    .commands(Processors.silent())
                    .build())
            .build()

        val processConfig = MongodConfig.builder()
            .net(Net(port, Network.localhostIsIPv6()))
            .version(Version.Main.PRODUCTION)
            .build()

        return MongodStarter.getInstance(runtimeConfig).prepare(processConfig).start()
    }

    override fun close() {
        inMemoryDatabase?.close()
    }


}

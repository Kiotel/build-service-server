package buildService.di

import buildService.features.contactors.ContractorRepository
import buildService.features.contactors.ContractorRepositoryImpl
import buildService.features.users.UserRepository
import buildService.features.users.UserRepositoryImpl
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDi(config: ApplicationConfig) {
    val dbModule = module {
        single<Database> {
            Database.connect(
                url = config.property("storage.jdbcURL").getString(),
                user = config.property("storage.user").getString(),
                password = config.property("storage.password").getString()
            )
        }
        singleOf(::UserRepositoryImpl) {
            bind<UserRepository>()
            createdAtStart()
        }
        singleOf(::ContractorRepositoryImpl) {
            bind<ContractorRepository>()
            createdAtStart()
        }
    }

    install(Koin) {
        slf4jLogger()
        modules(dbModule)
    }
}

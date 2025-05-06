package buildService.di

import buildService.features.contactors.ContractorRepository
import buildService.features.contactors.ContractorRepositoryImpl
import buildService.features.contactors.comments.ContractorCommentsRepository
import buildService.features.contactors.comments.ContractorCommentsRepositoryImpl
import buildService.features.users.UserRepository
import buildService.features.users.UserRepositoryImpl
import buildService.features.users.WorkingSiteRepository
import buildService.features.users.WorkingSiteRepositoryImpl
import io.ktor.server.application.*
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDi() {
    val dbModule = module {
        singleOf(::UserRepositoryImpl) {
            bind<UserRepository>()
            createdAtStart()
        }
        singleOf(::ContractorRepositoryImpl) {
            bind<ContractorRepository>()
            createdAtStart()
        }
        singleOf(::WorkingSiteRepositoryImpl) {
            bind<WorkingSiteRepository>()
            createdAtStart()
        }
        singleOf(::ContractorCommentsRepositoryImpl) {
            bind<ContractorCommentsRepository>()
            createdAtStart()
        }
    }

    install(Koin) {
        slf4jLogger()
        modules(dbModule)
    }
}

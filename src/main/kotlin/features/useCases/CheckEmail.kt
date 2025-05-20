package buildService.features.useCases

import buildService.features.contactors.ContractorRepository
import buildService.features.users.UserRepository

class CheckEmail(
    private val userRepository: UserRepository,
    private val contractorRepository: ContractorRepository
) {
    suspend operator fun invoke(email: String): Boolean {
        val userExists = userRepository.findByEmail(email) != null
        val contractorExists = contractorRepository.findByEmail(email) != null
        val result = userExists || contractorExists
        return result
    }
}



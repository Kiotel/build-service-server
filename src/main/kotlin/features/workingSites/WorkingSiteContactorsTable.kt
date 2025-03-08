package buildService.features.workingSites

import buildService.features.contactors.ContractorsTable
import org.jetbrains.exposed.sql.Table

object WorkingSiteContactorsTable : Table("working_site_contractors") {
    val workingSite = reference("working_site_id", WorkingSitesTable)
    val contractor = reference("contractor_id", ContractorsTable)
    override val primaryKey = PrimaryKey(workingSite, contractor)
}
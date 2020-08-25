package com.kutzlerstudios.onboardtrackers.repositories

import com.kutzlerstudios.onboardtrackers.models.company.Company
import com.kutzlerstudios.onboardtrackers.models.company.Credential
import org.springframework.data.repository.CrudRepository

interface CredentialRepository : CrudRepository<Credential, Int> {

    fun findAllByCompanyAndType(company: Int, type: String = "Drug"): Credential
}
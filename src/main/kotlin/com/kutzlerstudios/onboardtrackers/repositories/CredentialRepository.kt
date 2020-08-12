package com.kutzlerstudios.onboardtrackers.repositories

import com.kutzlerstudios.onboardtrackers.models.company.Credential
import org.springframework.data.repository.CrudRepository

interface CredentialRepository : CrudRepository<Credential, Long> {
}
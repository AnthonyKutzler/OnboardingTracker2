package com.kutzlerstudios.onboardtrackers.controllers.credentials

import com.kutzlerstudios.onboardtrackers.models.company.Credential
import com.kutzlerstudios.onboardtrackers.repositories.CredentialRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cred")
class CredentialController(private var credentialRepository: CredentialRepository) {

    @PostMapping
    @RequestMapping("/post")
    fun newCreds(creds: Credential): String{
        credentialRepository.save(creds)

        return HttpStatus.ACCEPTED.toString()
    }
}
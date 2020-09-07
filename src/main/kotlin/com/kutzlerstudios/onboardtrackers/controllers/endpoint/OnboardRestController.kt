package com.kutzlerstudios.onboardtrackers.controllers.endpoint

import com.kutzlerstudios.onboardtrackers.controllers.Emailer
import com.kutzlerstudios.onboardtrackers.controllers.MasterController
import com.kutzlerstudios.onboardtrackers.repositories.CompanyRepository
import com.kutzlerstudios.onboardtrackers.repositories.CredentialRepository
import com.kutzlerstudios.onboardtrackers.repositories.PeopleRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/onboard")
class OnboardRestController(private val peopleRepository: PeopleRepository, private val companyRepository: CompanyRepository, private val credentialRepository: CredentialRepository) {

    @GetMapping
    @RequestMapping("/run")
    fun runOnboarding(){
            MasterController(peopleRepository, companyRepository, credentialRepository).run()
    }

    @GetMapping("/recap")
    fun sendRecap(){
        MasterController(peopleRepository, companyRepository, credentialRepository).daRecapEmail()
    }

    @GetMapping("/breakdown")
    fun sendBreakdownEmail(){
        for(company in companyRepository.findAllBy())
        Emailer("anthonykutzler@gmail.com", peopleRepository, companyRepository).sendBreakdownEmail(company.pk!!)
    }
}
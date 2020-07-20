package com.kutzlerstudios.onboardtrackers.controllers.endpoint

import com.kutzlerstudios.onboardtrackers.controllers.Master
import com.kutzlerstudios.onboardtrackers.repositories.CompanyRepository
import com.kutzlerstudios.onboardtrackers.repositories.PeopleRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/onboard")
class OnboardRestController(private val peopleRepository: PeopleRepository, private val companyRepository: CompanyRepository) {

    @GetMapping
    @RequestMapping("/onboard/run")
    fun runOnboarding(){
        for(x in z){
            Master(peopleRepository, companyRepository, x).run()
        }
    }
}
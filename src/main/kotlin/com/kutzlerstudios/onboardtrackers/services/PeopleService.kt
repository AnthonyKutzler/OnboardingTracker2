package com.kutzlerstudios.onboardtrackers.services

import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.repositories.PeopleRepository

class PeopleService(private val repository: PeopleRepository) {

    var cortex = mutableListOf<Person>()
    var drug = mutableListOf<Person>()

    fun setupLists(company : Int){
        val z = repository.getAllByCompanyAndOnboardGreaterThanEqual(company)
        cortex = z.filter { (it.onboard.background in 0..1) || (it.onboard.background == 2 && !it.onboard.videos) }.toMutableList()
        drug = z.filter { it.onboard.drug in 1..3 }.toMutableList()
    }
}
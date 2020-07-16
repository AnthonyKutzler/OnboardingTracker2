package com.kutzlerstudios.onboardtrackers.repositories

import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.models.cortex.Onboard
import org.springframework.data.repository.CrudRepository

interface PeopleRepository : CrudRepository<Person, Long> {

    fun getAllByCompanyAndOnboardGreaterThanEqual(company: Int, onboard : Int = 77) : MutableList<Person>
}
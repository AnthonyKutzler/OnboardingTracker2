package com.kutzlerstudios.onboardtrackers.controllers.drugTests.`interface`

import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.models.company.Credential
import com.kutzlerstudios.onboardtrackers.models.drug.PersonList

interface DrugTest {


    fun runDt()

    fun runDrugTest() {
        val list = PersonList
        if(login(getCredentials())) {
            var person = list.drug.poll()
            while (person != null) {
                val person1 = checkResults(person)
                when {
                    person1.drug == 0 -> {
                        person1.drug = 1
                        list.newDt.add(person1)
                    }
                    person1.drug < 0 -> {
                        person1.status = -1
                    }
                    else -> list.add(person1, false, false)
                }
                person = list.drug.poll()
            }
            setupNewTests()
        } else loginError()
    }

    fun checkResults(person: Person) : Person

    fun setupNewTests()

    fun login(credentials: Credential) : Boolean

    fun loginError()

    fun getCredentials() : Credential

    fun sendReminder(person: Person)

}
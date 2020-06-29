package com.kutzlerstudios.onboardtrackers.controllers.drugTests.`interface`

import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.models.drug.Credentials

interface DrugTest {



    fun runDrugTest(people : List<Person>){
        if(login(getCredentials())) {
            for (person in people) {
                val person1 = checkResults(person)
                if (person1.onboard.drug == 0)
                    addToNewList(person1)
            }
            setupNewTests(getCreateList())
        } else loginError()
    }

    fun checkResults(person: Person) : Person

    fun addToNewList(person: Person)

    fun getCreateList(): Any

    fun setupNewTests(people: Any)

    fun login(credentials: Credentials) : Boolean

    fun loginError()

    fun getCredentials() : Credentials
}
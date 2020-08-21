package com.kutzlerstudios.onboardtrackers.controllers.drugTests.`interface`

import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.models.company.Credential
import com.kutzlerstudios.onboardtrackers.models.drug.PersonList

interface DrugTest {



    fun runDrugTest(people : List<Person>) {
        val list = PersonList
        if(login(getCredentials())) {
            for (person in people) {
                val person1 = checkResults(person)
                if (person1.onboard.drug == 0)
                    list.dtList.add(person1)
                else
                    list.list.add(person1)
            }
            setupNewTests(list.dtList)
        } else loginError()
    }

    fun checkResults(person: Person) : Person

    fun setupNewTests(people: List<Person>)

    fun login(credentials: Credential) : Boolean

    fun loginError()

    fun getCredentials() : Credential

    fun sendReminder(person: Person)

}
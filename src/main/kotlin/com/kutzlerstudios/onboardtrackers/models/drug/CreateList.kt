package com.kutzlerstudios.onboardtrackers.models.drug

import com.kutzlerstudios.onboardtrackers.models.Person

object CreateList {

    var list = mutableListOf<Person>()

    fun add(person: Person){
        list.add(person)
    }
}
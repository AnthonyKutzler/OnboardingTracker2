package com.kutzlerstudios.onboardtrackers.models.drug

import com.kutzlerstudios.onboardtrackers.models.Person

object PersonList {

    var dtList = mutableListOf<Person>()
    var list = mutableListOf<Person>()
    get() {
        for(person in dtList){
            person.onboard.drug = 1
            list.add(person)
        }
        return list
    }

    fun add(person: Person){
        list.add(person)
    }

    fun addAll(people : List<Person>){
        list.addAll(people)
    }

    fun clear(){
        list.clear()
    }

    fun addDtToList(){
        for (person in dtList){
            person.onboard.drug = 1
            list.add(person)
        }
    }
}
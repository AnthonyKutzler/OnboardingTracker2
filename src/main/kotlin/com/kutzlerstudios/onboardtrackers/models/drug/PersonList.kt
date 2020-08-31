package com.kutzlerstudios.onboardtrackers.models.drug

import com.kutzlerstudios.onboardtrackers.models.Person

object PersonList {

    var drug = mutableListOf<Person>()
    var cortex = mutableListOf<Person>()
    var newDt = mutableListOf<Person>()
    var complete = mutableListOf<Person>()
    var failed = mutableListOf<Person>()

    fun clearAll(){
        drug.clear()
        cortex.clear()
        newDt.clear()
        complete.clear()
    }

    fun add(person: Person){
        if(person.background == 2 && person.drug == 4 && person.videos) {
            person.status = 2
            complete.add(person)
        }
        else if(person.background == 2 && person.drug in 0..3)
            drug.add(person)
        else if(person.background in 0..1 || !person.videos)
            cortex.add(person)
        else{
            person.status = -1
            failed.add(person)
        }
    }

    fun addAll(people : List<Person>){
        for(person in people)
            add(person)
    }

    fun getAll(): List<Person>{
        val list = mutableListOf<Person>()
        list.addAll(complete)
        list.addAll(drug)
        list.addAll(cortex)
        list.addAll(failed)
        return list
    }

    fun reorder(){
        val list = getAll()
        clearAll()
        /*for(person in list) {
            person.change = person.changeBuilder.toString()
            add(person)
        }*/
    }


}
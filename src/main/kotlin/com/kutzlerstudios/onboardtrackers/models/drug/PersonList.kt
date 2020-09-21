package com.kutzlerstudios.onboardtrackers.models.drug

import com.kutzlerstudios.onboardtrackers.models.Person
import java.util.*

object PersonList {


    var drug : Queue<Person> = LinkedList<Person>()
    var cortex : Queue<Person> = LinkedList<Person>()
    var cortexCom = mutableListOf<Person>()
    var drugCom = mutableListOf<Person>()
    var newDt = mutableListOf<Person>()
    var complete = mutableListOf<Person>()
    var failed = mutableListOf<Person>()

    fun clearAll(){
        drug.clear()
        drugCom.clear()
        cortex.clear()
        cortexCom.clear()
        newDt.clear()
        complete.clear()

    }

    fun add(person: Person, cInit : Boolean, dInit : Boolean){
        if(person.background == 2 && person.drug == 4 && person.videos) {
            person.status = 2
            complete.add(person)
        }
        else if(person.background == 2 && person.drug in 0..3) {
            if(dInit)
                drug.add(person)
            else
                drugCom.add(person)

        }
        else if(person.background in 0..1 || !person.videos) {
            if(cInit)
                cortex.add(person)
            else
                cortexCom.add(person)

        }
        else{
            person.status = -1
            failed.add(person)
        }
    }

    fun addAll(people : List<Person>, cInit : Boolean, dInit: Boolean){
        for(person in people)
            add(person, cInit, dInit)
    }

    fun getAll(): List<Person>{
        val list = mutableListOf<Person>()
        list.addAll(complete)
        list.addAll(drugCom)
        list.addAll(cortexCom)
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
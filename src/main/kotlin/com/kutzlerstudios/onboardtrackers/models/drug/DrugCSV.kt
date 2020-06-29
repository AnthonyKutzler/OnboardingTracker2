package com.kutzlerstudios.onboardtrackers.models.drug

import java.util.*

class DrugCSV private constructor() {
    private val list: MutableList<Array<String>>
    fun add(array: Array<String>) {
        list.add(array)
    }

    fun getList(): List<Array<String>> {
        return list
    }

    companion object {
        private val lock = Any()

        @get:Synchronized
        var instance: DrugCSV? = null
            get() {
                var l = field
                if (l == null) {
                    synchronized(lock) {
                        l = field
                        if (l == null) {
                            l = DrugCSV()
                            field = l
                        }
                    }
                }
                return l
            }
            private set
    }

    init {
        list = ArrayList()
    }
}
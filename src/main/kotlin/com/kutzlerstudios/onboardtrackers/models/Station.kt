package com.kutzlerstudios.onboardtrackers.models

import org.jetbrains.annotations.NotNull
import javax.persistence.*


@Entity
@Table(name = "Station")
class Station (@Id @GeneratedValue(strategy= GenerationType.IDENTITY) var pk : Int? = null,
               @NotNull var code : String,
               @NotNull var phone: String,
               @NotNull var email: String,
               @NotNull @Column(name = "quest_local") var questLocal: String){


    fun stationStyle() : String{
        when(code){
            "DIN1" -> return "background: #b8d1f3"
            "DCH3" -> return "background: #b8d1f3"
            "DCH4" -> return "background: #dae5f4"
        }
        return ""
    }
}
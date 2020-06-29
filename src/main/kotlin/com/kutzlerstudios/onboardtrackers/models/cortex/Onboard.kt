package com.kutzlerstudios.onboardtrackers.models.cortex

import org.jetbrains.annotations.NotNull
import javax.persistence.*

@Entity
@Table(name = "Onboarding")
class Onboard(@Id @GeneratedValue(strategy= GenerationType.IDENTITY) var pk : Int? = null,
              @NotNull var background : Int,
              @NotNull var drug : Int,
              @NotNull var videos : Boolean,
              @NotNull var mentor : Boolean){

    fun bgString(): String {
        when(background){
            0 -> return ""
            1 -> return "Failed"
            2 -> return "Pending"
            3 -> return "Passed"
        }
        return ""
    }

    fun dtString():String{
        when(drug){
            0 -> return ""
            1 -> return "Positive"
            2 -> return "Expired"
            3 -> return "Scheduled"
            4 -> return "Collected"
            5 -> return "MRO/ At Lab"
            6 -> return "Passed"
        }

        return ""
    }
    fun vidString(): String{
        return if(videos) "Completed" else ""
    }

    fun bgStyle() : String{
        val z = "background: "
        when(background){
            2 -> return "$z yellow"
            3 -> return "$z chartreuse"
        }
        return ""
    }

    fun dtStyle(): String{
        val z = "background: "
        when(drug){
            0 -> return ""
            1 -> return "$z red"
            2 -> return "$z red"
            3 -> return "$z orange"
            4 -> return "$z yellow"
            5 -> return "$z greenyellow"
            6 -> return "$z chartreuse"
        }
        return ""
    }

    fun vidStyle(): String{
        return if(videos) "background: chartreuse" else ""
    }

    fun cumulativeStyle() : String{
        if((background == 3 && drug >= 4) || (background == 3 && videos))
            return "background: green"
        else if(background == 3 && drug <= 3 && !videos)
            return "background: yellow"
        else if(background == 2)
            return "background: orange"
        return ""
    }
}
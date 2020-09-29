package com.kutzlerstudios.onboardtrackers.models

import com.kutzlerstudios.onboardtrackers.models.company.Company
import org.jetbrains.annotations.NotNull
import java.lang.StringBuilder
import java.sql.Timestamp
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "People")
class Person(@Id @GeneratedValue(strategy= GenerationType.IDENTITY) var pk : Int? = null,
             @NotNull @ManyToOne @JoinColumn(name = "company", referencedColumnName = "pk") var company: Company,
             @NotNull @Column(name = "first_name") var firstName: String,
             @NotNull @Column(name = "last_name") var lastName: String,
             var phone: String,
             @NotNull var email: String,
             @NotNull var referral: String,
             var note: String? = "Nothing",
             var status: Int? = 0,
             @NotNull var paused : Boolean = false,
             @NotNull var background : Int,
             @NotNull var drug : Int,
             @NotNull var videos : Boolean,
             @NotNull var mentor : Boolean,
             @NotNull var bg : Int,
             @NotNull var dt : Int,
             @NotNull var vids : Boolean,
             @NotNull var bgc : Boolean? = false,
             @NotNull var dtc : Boolean? = false,
             @NotNull var statC : Boolean = false,
             var onboardS : LocalDate,
             var onboardE: LocalDate){


/*
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
    }*/
}
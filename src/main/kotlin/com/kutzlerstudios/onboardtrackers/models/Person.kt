package com.kutzlerstudios.onboardtrackers.models

import com.kutzlerstudios.onboardtrackers.models.cortex.Contacted
import com.kutzlerstudios.onboardtrackers.models.cortex.Onboard
import org.jetbrains.annotations.NotNull
import javax.persistence.*

@Entity
@Table(name = "People")
class Person(@Id @GeneratedValue(strategy= GenerationType.IDENTITY) var pk : Int? = null,
             @NotNull @ManyToOne @JoinColumn(name = "station", referencedColumnName = "pk") var station : Station,
             @NotNull @Column(name = "first_name") var firstName: String,
             @NotNull @Column(name = "last_name") var lastName: String,
             @NotNull var phone: String,
             @NotNull var email: String,
             @NotNull var referral: String,
             var note: String? = "Nothing",
             @NotNull @ManyToOne @JoinColumn(name = "onboard", referencedColumnName = "pk") var onboard : Onboard,
             @NotNull @ManyToOne @JoinColumn(name = "contacted", referencedColumnName = "pk") var contacted : Contacted,
             @NotNull var paused : Boolean,
             @NotNull var company : Int){

}
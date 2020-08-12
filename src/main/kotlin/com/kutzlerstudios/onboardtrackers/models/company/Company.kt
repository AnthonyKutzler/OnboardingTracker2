package com.kutzlerstudios.onboardtrackers.models.company

import org.jetbrains.annotations.NotNull
import javax.persistence.*

@Entity
@Table(name = "Company")
class Company(@Id @GeneratedValue(strategy = GenerationType.IDENTITY) var pk : Int? = null,
              @NotNull var name: String? = null,
              @NotNull var email: String? = null,
              @NotNull var phone: String? = null,
              @NotNull @ManyToOne @JoinColumn(name = "preferences", referencedColumnName = "pk") var prefs: Preferences? = null) {
}
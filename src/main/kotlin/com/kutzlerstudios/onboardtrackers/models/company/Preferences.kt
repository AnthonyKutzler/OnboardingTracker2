package com.kutzlerstudios.onboardtrackers.models.company

import org.jetbrains.annotations.NotNull
import javax.persistence.*

@Entity
@Table(name = "Preferences")
class Preferences(@Id @GeneratedValue(strategy = GenerationType.IDENTITY) var pk : Int? = null,
                  @NotNull var accurate : Boolean = true,
                  @NotNull var drug : Boolean = true,
                  @NotNull var contact : String = "phone") {
}
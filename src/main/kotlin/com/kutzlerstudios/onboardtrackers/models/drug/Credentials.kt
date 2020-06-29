package com.kutzlerstudios.onboardtrackers.models.drug

import org.jetbrains.annotations.NotNull
import javax.persistence.*


@Entity
@Table(name = "creds")
class Credentials(@Id @GeneratedValue(strategy= GenerationType.IDENTITY) var pk : Int? = null,
                  @NotNull var user : String,
                  @NotNull var pass : String) {
}
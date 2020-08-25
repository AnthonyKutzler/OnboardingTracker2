package com.kutzlerstudios.onboardtrackers.models.company

import org.jetbrains.annotations.NotNull
import javax.persistence.*

@Entity
@Table(name = "Credentials")
class Credential(@Id @GeneratedValue(strategy = GenerationType.IDENTITY) var pk : Int? = null,
                 @NotNull var type : String,
                 @NotNull var company : Int? = null,
                 @NotNull var user : String? = null,
                 @NotNull var pass : String?= null,
                 @NotNull var provider: String? = null,
                 @NotNull var additional: String) {
}
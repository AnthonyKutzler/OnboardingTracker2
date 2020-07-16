package com.kutzlerstudios.onboardtrackers.models

import javax.persistence.*

@Entity
@Table(name = "someName")//TODO
class Company(@Id @GeneratedValue(strategy= GenerationType.IDENTITY) var pk : Int? = null, somevalue : Int)//TODO: FINISH {
}
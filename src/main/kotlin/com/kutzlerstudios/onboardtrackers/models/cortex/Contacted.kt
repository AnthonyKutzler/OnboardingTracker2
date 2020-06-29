package com.kutzlerstudios.onboardtrackers.models.cortex

import javax.persistence.*

@Entity
@Table(name = "Contacted")
class Contacted(@Id @GeneratedValue(strategy= GenerationType.IDENTITY) var pk : Int? = null,
                @Column(name = "contacted") var bg : Int = 0,
                @Column(name = "drug_contacted") var dt : Int = 0)
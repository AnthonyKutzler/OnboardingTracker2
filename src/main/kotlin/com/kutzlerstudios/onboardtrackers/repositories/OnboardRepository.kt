package com.kutzlerstudios.onboardtrackers.repositories

import com.kutzlerstudios.onboardtrackers.models.cortex.Onboard
import org.springframework.data.repository.CrudRepository
import java.util.*

interface OnboardRepository : CrudRepository<Onboard, Int> {

    fun findByPk(pk: Int): Onboard
}
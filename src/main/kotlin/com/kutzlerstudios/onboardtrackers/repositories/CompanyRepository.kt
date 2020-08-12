package com.kutzlerstudios.onboardtrackers.repositories

import com.kutzlerstudios.onboardtrackers.models.company.Company
import org.springframework.data.repository.CrudRepository

interface CompanyRepository : CrudRepository<Company, Long> {

    fun getByPk(pk : Int): Company
}
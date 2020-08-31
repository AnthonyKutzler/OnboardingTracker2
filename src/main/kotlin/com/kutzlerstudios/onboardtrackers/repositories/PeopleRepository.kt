package com.kutzlerstudios.onboardtrackers.repositories

import com.kutzlerstudios.onboardtrackers.models.Person
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PeopleRepository : CrudRepository<Person, Long> {

    @Query("SELECT p FROM Person p WHERE p.company.pk = ?1 AND p.status IN(0,1)")
    fun getAlByCompanyAndStatus(company: Int) : MutableList<Person>

    @Query("SELECT p FROM Person p WHERE p.company.pk = ?1 AND (p.background <> p.bg OR p.drug <> p.dt OR p.videos <> p.vids) ORDER BY p.videos DESC, p.drug DESC, p.background DESC")
    fun getAlByCompanyAndChanges(company: Int) : MutableList<Person>

}
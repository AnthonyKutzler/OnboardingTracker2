package com.kutzlerstudios.onboardtrackers.repositories

import com.kutzlerstudios.onboardtrackers.models.Person
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PeopleRepository : CrudRepository<Person, Long> {

    @Query("SELECT p FROM Person p WHERE p.company.pk = ?1 AND p.status IN(0,1)")
    fun getAlByCompanyAndStatus(company: Int) : MutableList<Person>

    @Query("SELECT p FROM Person p WHERE p.company.pk = ?1 AND (p.background <> p.bg OR p.drug <> p.dt OR p.videos <> p.vids) ORDER BY p.videos DESC, p.drug DESC, p.background DESC")
    fun getAlByCompanyAndChanges(company: Int) : MutableList<Person>

    @Query("SELECT COUNT(p) FROM Person p WHERE p.background = ?1 AND p.company.pk = ?2")
    fun countBackgrounds(bg : Int, company: Int): Int

    @Query("SELECT COUNT(p) FROM Person p WHERE p.background = 2 AND p.drug = ?1 AND p.company.pk = ?2")
    fun countDrug(dt: Int, company: Int) : Int

    @Query("SELECT COUNT(p) FROM Person p WHERE p.background = 2 AND p.drug = 4 AND p.videos = false AND p.company.pk = ?1")
    fun countVideos(company: Int) : Int

    @Query("SELECT COUNT(p) FROM Person p WHERE p.status > 0 AND p.company.pk = ?1")
    fun countFailed(company: Int) : Int

    @Query("SELECT COUNT(p) FROM Person p WHERE p.status = 2 AND p.company.pk = ?1")
    fun countPassed(company: Int) : Int

    @Query("SELECT COUNT(p) FROM Person p WHERE p.company.pk = ?1 AND p.onboard_date < DATE_SUB(NOW(), INTERVAL 7 DAY)")
    fun countNewInLast7(company: Int): Int

}
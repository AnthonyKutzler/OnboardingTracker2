package com.kutzlerstudios.onboardtrackers.controllers

import com.kutzlerstudios.onboardtrackers.controllers.cortex.Onboarding
import com.kutzlerstudios.onboardtrackers.controllers.drugTests.`interface`.DrugTest
import com.kutzlerstudios.onboardtrackers.controllers.drugTests.providers.Quest
import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.models.drug.PersonList
import com.kutzlerstudios.onboardtrackers.repositories.CompanyRepository
import com.kutzlerstudios.onboardtrackers.repositories.PeopleRepository
import com.kutzlerstudios.onboardtrackers.services.PeopleService
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

class MasterController(var repository: PeopleRepository, var companyRepository: CompanyRepository, var threads: Int = 3) {

    private var drivers = mutableListOf<WebDriver>()
    fun run(){
        val peopleService = PeopleService(repository)
        for(company in companyRepository.findAll()) {
            peopleService.setupLists(company.pk!!)
            val people = peopleService.cortex
            val onboardList = mutableListOf<Onboarding>()
            for ((index, driver) in drivers.withIndex()) {
                val thread = if (index == 0)
                    Onboarding(people.subList(0, people.size / threads), driver)
                else
                    Onboarding(people.subList((people.size / threads) * index, (people.size / threads) * (index + 1)), driver)
                //TODO: TRY ME
                thread.start()
                thread.join()
                onboardList.add(thread)
            }
            people.clear()
            for ((index, thread) in onboardList.withIndex()) {
                people.addAll(thread.peopleOut)
                if (index > 0) {
                    drivers[index].close()
                }
            }
            val drugFiltered = people.filter { it.onboard.background == 2 && it.onboard.drug in 0..3 }.toMutableList()
            drugFiltered.addAll(peopleService.drug)
            when (companyRepository.getByPk(company.pk!!).prefs!!.drugProvider!!) {
                "QUEST" -> Quest(drugFiltered, company.pk!!, com.kutzlerstudios.onboardtrackers.models.company.Credential(1, "1",1,"KutzlerA001", "OrderM0r3Drug73575Please2!", "1"), drivers[0])
            }
            people.addAll(PersonList.list)
            val completeFilter = people.filter { it.onboard.background == 2 && it.onboard.drug == 4 && it.onboard.videos }.toMutableList()
            for(person in completeFilter){
                person.status = 2
                repository.save(person)
            }
            //Onboarding(completeFilter, drivers[0])
            drivers[0].close()
            Emailer("cncdin1@cnclogistics.biz", repository, companyRepository).sendDaRecap()
        }
    }




    private fun setup(){
        for(z in threads downTo 1){
            startDriver()
        }
    }

    private fun startDriver() {
        System.setProperty("webdriver.chrome.driver", "/home/gob/Onboard/chromedriver")
        val options = ChromeOptions()
        options.addArguments("start-maximized")
        options.addArguments("enable-automation")
        options.addArguments("--no-sandbox")
        options.addArguments("--disable-infobars")
        options.addArguments("--disable-dev-shm-usage")
        options.addArguments("--disable-browser-side-navigation")
        options.addArguments("--disable-gpu")
        drivers.add(ChromeDriver(options))
    }
}
package com.kutzlerstudios.onboardtrackers.controllers

import com.kutzlerstudios.onboardtrackers.controllers.cortex.Onboarding
import com.kutzlerstudios.onboardtrackers.controllers.drugTests.`interface`.DrugTest
import com.kutzlerstudios.onboardtrackers.controllers.drugTests.providers.Quest
import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.models.drug.PersonList
import com.kutzlerstudios.onboardtrackers.repositories.CompanyRepository
import com.kutzlerstudios.onboardtrackers.repositories.CredentialRepository
import com.kutzlerstudios.onboardtrackers.repositories.PeopleRepository
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

class MasterController(var repository: PeopleRepository, var companyRepository: CompanyRepository, var credentialRepository: CredentialRepository) {

    private var threads = 3

    private var drivers = mutableListOf<WebDriver>()
    fun run(){
        setup()
        val pLists = PersonList
        for(company in companyRepository.findAllBy()) {
            pLists.clearAll()
            val emailer = Emailer(company.email!!, repository, companyRepository)
            pLists.addAll(repository.getAlByCompanyAndStatus(company.pk!!), true, true)
            if(pLists.cortex.isNotEmpty()) {
                val onboardList = mutableListOf<Onboarding>()
                for (driver in drivers) {
                    val thread = Onboarding(driver, credentialRepository)
                    thread.start()
                    onboardList.add(thread)
                }
                for(thread in onboardList)
                    thread.join()
                when (credentialRepository.findAllByCompanyAndType(company.pk!!).provider!!.toLowerCase()) {
                        else -> Quest(company.pk!!, drivers[0], emailer, credentialRepository)
                }
                repository.saveAll(pLists.getAll())
            }
        }
        for(driver in drivers)
            driver.close()

    }

    fun daRecapEmail(){
            Emailer("AnthonyKutzler@gmail.com", repository, companyRepository).sendDaRecap()
    }


    private fun setup(){
        startDriver()
        //for(z in threads downTo 1){
        //    startDriver()
        //}
    }

    private fun startDriver() {
        for(z in 1..3) {
            System.setProperty("webdriver.chrome.driver", "/home/gob/Onboard/chromedriver")
            val options = ChromeOptions()
            options.addArguments("start-maximized")
            options.addArguments("enable-automation")
            options.addArguments("--no-sandbox")
            options.addArguments("--disable-infobars")
            options.addArguments("--disable-dev-shm-usage")
            options.addArguments("--disable-browser-side-navigation")
            options.addArguments("--disable-gpu")
            /*driver =*/ drivers.add(ChromeDriver(options))
        }
    }
}
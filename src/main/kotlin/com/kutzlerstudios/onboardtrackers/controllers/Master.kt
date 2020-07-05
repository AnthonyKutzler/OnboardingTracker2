package com.kutzlerstudios.onboardtrackers.controllers

import com.kutzlerstudios.onboardtrackers.controllers.cortex.Onboarding
import com.kutzlerstudios.onboardtrackers.controllers.drugTests.`interface`.DrugTest
import com.kutzlerstudios.onboardtrackers.controllers.drugTests.providers.Quest
import com.kutzlerstudios.onboardtrackers.models.Person
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

class Master(private val threads: Int = 3) {
    private var drivers = mutableListOf<WebDriver>()
    private lateinit var onboardList: MutableList<Onboarding>
    private lateinit var drugList: MutableList<Quest>
    private lateinit var people: MutableList<Person>

    fun run(){
        for((index, driver) in drivers.withIndex()){
            val thread = if(index == 0)
                Onboarding(people.subList(0, people.size/threads), driver)
            else
                Onboarding(people.subList((people.size/threads)*index, (people.size/threads)*(index+1)), driver)
            //FIXME: TRY ME
            thread.start()
            thread.join()
            onboardList.add(thread)
        }
        people.clear()
        for(thread in onboardList){
            people.addAll(thread.peopleOut)
        }
        val drugFiltered = people.filter { it.onboard.background == 2 && it.onboard.drug in 0..3 }.toMutableList()
        drugFiltered.addAll(getMoreFromRepo)
        for((index, driver) in drivers.withIndex()){
            val thread = when(1){
                else -> if(index == 0)
                    Quest(people.subList(0, people.size/threads), driver)
                else
                    Quest(people.subList((people.size/threads)*index, (people.size/threads)*(index+1)), driver)
            }
            //FIXME: TRY ME
            thread.start()
            thread.join()
            drugList.add(thread)
        }
        val updatePeople = people.filter { it.onboard.background != 2 && it.onboard.drug !in 0..3 }.toMutableList()
        for(thread in drugList)
            updatePeople.addAll(thread.peopleIn)
        drugList[0].setupNewTests(drugList[0].getCreateList())

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
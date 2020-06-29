package com.kutzlerstudios.onboardtrackers.controllers.drugTests.providers

import com.kutzlerstudios.onboardtrackers.controllers.drugTests.`interface`.DrugTest
import com.kutzlerstudios.onboardtrackers.models.drug.DrugCSV
import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.models.drug.Credentials
import org.openqa.selenium.*
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

class Quest(var peopleIn : List<Person>, var driver : WebDriver) : Thread(), DrugTest {

    var csv = DrugCSV.instance
    var passwordEmailSent = false
    var attemptCount = 0

    override fun run() {

        try {
            runDrugTest(peopleIn)
        } catch (ignored: Exception) {}
    }

    override fun checkResults(person: Person): Person {
        if(attemptCount > 5)
            return person
        val wait = WebDriverWait(driver, 40)
        try {
            driver["https://esp.employersolutions.com/Results/Summary"]
            wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//*[@id=\"search-boxes\"]/div/input[1]"))))
        } catch (e: TimeoutException) {
            attemptCount++
            return checkResults(person)
        }
        try {
            driver.findElement(By.xpath("//*[@id=\"flash\"]"))
            val text = driver.findElement(By.xpath("//*[@id=\"flash\"]/span")).text
            driver.findElement(By.xpath("//*[@id=\"flash\"]")).click()
            if (!passwordEmailSent) {
                loginError()
            }
        } catch (ignored: NoSuchElementException) { }
        return try {
            driver.findElement(By.xpath("//*[@id=\"search-boxes\"]/div/input[1]")).sendKeys(person.phone.replace("[\D+]".toRegex(), ""))
            driver.findElement(By.xpath("//*[@id=\"search-boxes\"]/div/input[14]")).click()
            wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//*[@id=\"results-table\"]"))))
            val elements = driver.findElements(By.xpath("//*[@id=\"table-items\"]/*"))
            for (element in elements) {
                val name = element.findElements(By.tagName("td"))[2].text.toLowerCase().split(",").toTypedArray()
                if (!(name[0].trim { it <= ' ' } == person.lastName.toLowerCase().replace("[-]".toRegex(), " ") && name[1].trim { it <= ' ' } == person.firstName.toLowerCase().replace("[-]".toRegex(), " "))) continue
                val resultText = element.findElements(By.tagName("td"))[7].text.toLowerCase()
                val result = person.onboard.drug
                if (result < 6) {
                    person.onboard.drug = if (resultText.contains("neg")) 4
                    else if (resultText.contains("mro") || resultText.contains("lab")) 3
                    else if (resultText.contains("collec")) 2
                    else if (resultText.contains("order") || resultText.contains("suspend") && result < 4) 1
                    else if ((resultText.contains("expir"))) -1
                    else if((resultText.contains("cancel") || resultText.contains("unable") || resultText.contains("refus")) && result < 3) -2
                    else if (resultText.contains("pos") && result < 3) -3
                    else 0
                }
            }
            person
        } catch (e1: ElementClickInterceptedException) {
            checkResults(person)
        } catch (e: TimeoutException) {
            attemptCount++
            checkResults(person)
        }
    }

    override fun login(credentials: Credentials) : Boolean {
        if (attemptCount > 5) return false
        try {
            driver["https://esp.employersolutions.com/"]
            driver.findElement(By.xpath("//*[@id=\"UserName\"]")).sendKeys(credentials.user)
            driver.findElement(By.xpath("//*[@id=\"Password\"]")).sendKeys(credentials.pass)
            driver.findElement(By.xpath("//*[@id=\"loginContainer\"]/div/div/form/fieldset/button")).click()
        } catch (e: java.lang.Exception) {
            ++attemptCount
            login(credentials)
        }
        return true
    }

    override fun addToNewList(person: Person) {
        TODO("Not yet implemented")
    }

    override fun getCreateList(): Any {
        TODO("Not yet implemented")
    }

    override fun setupNewTests(people: Any) {
        TODO("Not yet implemented")
    }

    override fun loginError() {
        if(!passwordEmailSent) {


            passwordEmailSent = true
        }
    }

    override fun getCredentials(): Credentials {
        TODO("Not yet implemented")
    }

}


//checkResults(), login(), setupNewTests(), loginError()
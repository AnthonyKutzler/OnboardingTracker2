package com.kutzlerstudios.onboardtrackers.controllers.drugTests.providers

import com.kutzlerstudios.onboardtrackers.controllers.Emailer
import com.kutzlerstudios.onboardtrackers.controllers.drugTests.`interface`.DrugTest
import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.models.company.Company
import com.kutzlerstudios.onboardtrackers.models.company.Credential
import com.kutzlerstudios.onboardtrackers.models.drug.PersonList
import com.kutzlerstudios.onboardtrackers.repositories.CredentialRepository
import com.kutzlerstudios.onboardtrackers.services.GmailService
import com.kutzlerstudios.onboardtrackers.services.TwilioHelper
import com.opencsv.CSVWriter
import org.openqa.selenium.*
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Autowired
import java.awt.Robot
import java.awt.event.KeyEvent
import java.io.File
import java.lang.Exception
import java.lang.Thread.sleep
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class Quest(var company: Int, var driver : WebDriver, private val emailer: Emailer, private val credentialRepository: CredentialRepository) : DrugTest {

    var people = mutableListOf<Person>()
    var list = PersonList
    private var passwordEmailSent = false
    private var attemptCount = 0

    override fun runDt(){
        super.runDrugTest()
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
        }catch (ex : Exception){
            login(getCredentials())
            checkResults(person)
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
            driver.findElement(By.xpath("//*[@id=\"search-boxes\"]/div/input[1]")).sendKeys(person.phone.replace("[\\D+]".toRegex(), ""))
            driver.findElement(By.xpath("//*[@id=\"search-boxes\"]/div/input[14]")).click()
            wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//*[@id=\"results-table\"]"))))
            val elements = driver.findElements(By.xpath("//*[@id=\"table-items\"]/*"))
            val preResult = person.drug
            person.drug = 0
            for (element in elements) {
                val name = element.findElements(By.tagName("td"))[2].text.toLowerCase().split(",").toTypedArray()
                if ((name[0].trim() == person.lastName.toLowerCase().replace("[^a-zA-Z]+".toRegex(), " ").trim() &&
                                name[1].trim() == person.firstName.toLowerCase().replace("[^a-zA-Z]+".toRegex(), " ").trim())) {
                    var resultText = element.findElements(By.tagName("td"))[7].text.toLowerCase()
                    var result = person.drug
                    if (result < 6) {
                        person.drug = checkResultText(person, resultText, result, preResult)
                    }
                }else
                    person.drug = checkResultText(person,"", person.drug, preResult)
            }
            person
        } catch (e1: ElementClickInterceptedException) {
            checkResults(person)
        } catch (e: TimeoutException) {
            attemptCount++
            checkResults(person)
        }
    }

    private fun checkResultText(person: Person, resultText : String, result : Int, preResult : Int) : Int{
        return (if (resultText.contains("neg")) 4
        else if (resultText.contains("mro") || resultText.contains("lab")) 3
        else if (resultText.contains("collec")){
            if(preResult != 2){
                emailer.sendCollectedEmail(person)
            }
            2}
        else if (resultText.contains("order") || resultText.contains("suspend") && result < 2) 1
        else if ((resultText.contains("expir"))) -1
        else if ((resultText.contains("cancel") || resultText.contains("unable") || resultText.contains("refus")) && result < 1) -2
        else if (resultText.contains("pos") && result < 1) -3
                else result)
    }

    override fun login(credentials: Credential) : Boolean {
        if (attemptCount > 5) return false
        try {
            driver["https://esp.employersolutions.com/"]
            driver.findElement(By.xpath("//*[@id=\"UserName\"]")).sendKeys(credentials.user)
            driver.findElement(By.xpath("//*[@id=\"Password\"]")).sendKeys(credentials.pass)
            driver.findElement(By.xpath("//*[@id=\"loginContainer\"]/div/div/form/fieldset/button")).click()
        } catch (e: Exception) {
            ++attemptCount
            login(credentials)
        }
        return true
    }

    @Throws(java.io.IOException::class)
    override fun setupNewTests() {
        if(PersonList.newDt.isEmpty()) return
        val writer = CSVWriter(Files.newBufferedWriter(Paths.get("/home/gob/dt.csv")),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)
        writer.writeNext(arrayOf("Primary ID", "First Name", "Last Name", "Primary Phone", "Date of Birth", "Account Number", "Modality",
                "Client Site Location", "Order Code(s)", "Collection Type", "Reason for Test", "Order Expiration Date",
                "Order Expiration Time", "Collection Site Code", "Observed", "Email(s)"))
        for(person in PersonList.newDt){
            writer.writeNext(arrayOf(person.phone.replace("\\D+".toRegex(), ""),
                    person.firstName.replace("[^a-zA-Z]+".toRegex(), " "),
                    person.lastName.replace("[^a-zA-Z]+".toRegex(), " "),
                    person.phone, "",
                    getCredentials().additional,
                    "FMCSA", "", "65304N", "Split", "Pre-Employment",
                    LocalDate.now().plus(2, ChronoUnit.WEEKS).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                    "1100", "", "No", person.email))
            //try catch ApiException
            sendReminder(person)
        }
        writer.close()
        try {
            if (login(getCredentials())) {
                driver["https://esp.employersolutions.com/ImportOrder/Index"]
                sleep(1600)
                driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/div[1]/div[3]/form/div[2]/div[1]/div/input[2]")).click()
                sleep(2000)
                driver.findElement(By.xpath("//*[@id=\"ImportFileName\"]")).sendKeys("/home/gob/dt.csv")
                //driver.findElement(By.xpath("//*[@id=\"ImportFileName\"]")).click();
                //driver.switchTo().activeElement().sendKeys("/home/gob/dt.csv");
                driver.findElement(By.xpath("//*[@id=\"ui-id-4\"]/input[2]")).click()
                driver.findElement(By.xpath("//*[@id=\"import-order-form\"]/div[2]/div[4]/div/input")).click()
                driver.findElement(By.xpath("//*[@id=\"Import\"]")).click()
                sleep(10000)
            }
        } catch (ignored: java.lang.Exception) { }
        File("/home/gob/dt.csv").delete()
    }

    override fun loginError() {
        if(!passwordEmailSent) {
            emailer.from = "anthonykutzler@gmail.com"
            emailer.sendQuestEmail(PersonList.getAll()[0].company.email!!, "QUEST Password is Expiring")
            passwordEmailSent = true
        }
    }

    override fun getCredentials(): Credential {
        return credentialRepository.findAllByCompanyAndType(company)
    }

    override fun sendReminder(person: Person) {
        TwilioHelper(person.company.phone!!, person.phone, 1)
    }
}


//checkResults(), login(), setupNewTests(), loginError()

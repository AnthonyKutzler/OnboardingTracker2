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

class Quest(var peopleIn : List<Person>, var company: Int, var driver : WebDriver, val emailer: Emailer, val credentialRepository: CredentialRepository) : DrugTest {

    var people = mutableListOf<Person>()
    var list = PersonList
    private var passwordEmailSent = false
    private var attemptCount = 0
//    private var robot = Robot()

    override fun runDt(){
        super.runDrugTest(peopleIn)
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
            /*val passE = */driver.findElement(By.xpath("//*[@id=\"Password\"]")).sendKeys(credentials.pass)
            /*var letters = credentials.pass!!.toCharArray()
            for(letter in letters) {
                passE.sendKeys(letter.toString())
                sleep((Math.random() + .5).toLong() * 1200)
            }*/
            //println("${credentials.user}, ${credentials.pass}")
            //println(driver.findElement(By.xpath("//*[@id=\"Password\"]")).text)
            //type(credentials.pass!!)
            driver.findElement(By.xpath("//*[@id=\"loginContainer\"]/div/div/form/fieldset/button")).click()
        } catch (e: java.lang.Exception) {
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
            emailer.sendQuestEmail(peopleIn[0].company.email!!, "QUEST Password is Expiring")
            passwordEmailSent = true
        }
    }

    override fun getCredentials(): Credential {
        return credentialRepository.findAllByCompanyAndType(company)
    }

    override fun sendReminder(person: Person) {
        TwilioHelper(person.company.phone!!, person.phone, 1)
    }


    fun type(characters: CharSequence) {
        val length = characters.length
        for (i in 0 until length) {
            val character = characters[i]
            type(character)
        }
    }

    fun type(character: Char) {
        when (character) {
            'a' -> doType(KeyEvent.VK_A)
            'b' -> doType(KeyEvent.VK_B)
            'c' -> doType(KeyEvent.VK_C)
            'd' -> doType(KeyEvent.VK_D)
            'e' -> doType(KeyEvent.VK_E)
            'f' -> doType(KeyEvent.VK_F)
            'g' -> doType(KeyEvent.VK_G)
            'h' -> doType(KeyEvent.VK_H)
            'i' -> doType(KeyEvent.VK_I)
            'j' -> doType(KeyEvent.VK_J)
            'k' -> doType(KeyEvent.VK_K)
            'l' -> doType(KeyEvent.VK_L)
            'm' -> doType(KeyEvent.VK_M)
            'n' -> doType(KeyEvent.VK_N)
            'o' -> doType(KeyEvent.VK_O)
            'p' -> doType(KeyEvent.VK_P)
            'q' -> doType(KeyEvent.VK_Q)
            'r' -> doType(KeyEvent.VK_R)
            's' -> doType(KeyEvent.VK_S)
            't' -> doType(KeyEvent.VK_T)
            'u' -> doType(KeyEvent.VK_U)
            'v' -> doType(KeyEvent.VK_V)
            'w' -> doType(KeyEvent.VK_W)
            'x' -> doType(KeyEvent.VK_X)
            'y' -> doType(KeyEvent.VK_Y)
            'z' -> doType(KeyEvent.VK_Z)
            'A' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_A)
            'B' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_B)
            'C' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_C)
            'D' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_D)
            'E' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_E)
            'F' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_F)
            'G' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_G)
            'H' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_H)
            'I' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_I)
            'J' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_J)
            'K' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_K)
            'L' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_L)
            'M' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_M)
            'N' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_N)
            'O' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_O)
            'P' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_P)
            'Q' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Q)
            'R' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_R)
            'S' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_S)
            'T' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_T)
            'U' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_U)
            'V' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_V)
            'W' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_W)
            'X' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_X)
            'Y' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Y)
            'Z' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Z)
            '`' -> doType(KeyEvent.VK_BACK_QUOTE)
            '0' -> doType(KeyEvent.VK_0)
            '1' -> doType(KeyEvent.VK_1)
            '2' -> doType(KeyEvent.VK_2)
            '3' -> doType(KeyEvent.VK_3)
            '4' -> doType(KeyEvent.VK_4)
            '5' -> doType(KeyEvent.VK_5)
            '6' -> doType(KeyEvent.VK_6)
            '7' -> doType(KeyEvent.VK_7)
            '8' -> doType(KeyEvent.VK_8)
            '9' -> doType(KeyEvent.VK_9)
            '-' -> doType(KeyEvent.VK_MINUS)
            '=' -> doType(KeyEvent.VK_EQUALS)
            '~' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE)
            '!' -> doType(KeyEvent.VK_EXCLAMATION_MARK)
            '@' -> doType(KeyEvent.VK_AT)
            '#' -> doType(KeyEvent.VK_NUMBER_SIGN)
            '$' -> doType(KeyEvent.VK_DOLLAR)
            '%' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_5)
            '^' -> doType(KeyEvent.VK_CIRCUMFLEX)
            '&' -> doType(KeyEvent.VK_AMPERSAND)
            '*' -> doType(KeyEvent.VK_ASTERISK)
            '(' -> doType(KeyEvent.VK_LEFT_PARENTHESIS)
            ')' -> doType(KeyEvent.VK_RIGHT_PARENTHESIS)
            '_' -> doType(KeyEvent.VK_UNDERSCORE)
            '+' -> doType(KeyEvent.VK_PLUS)
            '\t' -> doType(KeyEvent.VK_TAB)
            '\n' -> doType(KeyEvent.VK_ENTER)
            '[' -> doType(KeyEvent.VK_OPEN_BRACKET)
            ']' -> doType(KeyEvent.VK_CLOSE_BRACKET)
            '\\' -> doType(KeyEvent.VK_BACK_SLASH)
            '{' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET)
            '}' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET)
            '|' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH)
            ';' -> doType(KeyEvent.VK_SEMICOLON)
            ':' -> doType(KeyEvent.VK_COLON)
            '\'' -> doType(KeyEvent.VK_QUOTE)
            '"' -> doType(KeyEvent.VK_QUOTEDBL)
            ',' -> doType(KeyEvent.VK_COMMA)
            '<' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA)
            '.' -> doType(KeyEvent.VK_PERIOD)
            '>' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD)
            '/' -> doType(KeyEvent.VK_SLASH)
            '?' -> doType(KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH)
            ' ' -> doType(KeyEvent.VK_SPACE)
            else -> throw IllegalArgumentException("Cannot type character $character")
        }
    }

    private fun doType(vararg keyCodes: Int) {
        doType(keyCodes, 0, keyCodes.size)
    }

    private fun doType(keyCodes: IntArray, offset: Int, length: Int) {
        if (length == 0) {
            return
        }
        //robot.keyPress(keyCodes[offset])
        doType(keyCodes, offset + 1, length - 1)
        //robot.keyRelease(keyCodes[offset])
    }
}


//checkResults(), login(), setupNewTests(), loginError()

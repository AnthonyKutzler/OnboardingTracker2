package com.kutzlerstudios.onboardtrackers.controllers.cortex

import com.deathbycaptcha.Captcha
import com.deathbycaptcha.SocketClient
import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.models.company.Credential
import com.kutzlerstudios.onboardtrackers.models.drug.PersonList
import com.kutzlerstudios.onboardtrackers.repositories.CredentialRepository
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

class Onboarding(var peopleIn: MutableList<Person>, var driver: WebDriver, val credentialRepository: CredentialRepository) : Thread() {


    var peopleOut : MutableList<Person> = mutableListOf()
    var drugList : MutableList<Person> = mutableListOf()

    private lateinit var credential: Credential

    private var captchaCounter = 0

    override fun run() {
        try {
            //startDriver()
            runCortex()
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
    }

    @Throws(java.lang.Exception::class)
    private fun runCortex() {
        setupCredentials()
        var lists = PersonList
        for (person in peopleIn) {
            var personA = checkCortex(person)
            lists.add(personA)

        }
    }

    private fun checkCortex(personA: Person): Person {
        var person = personA
        try{
            while (!cortexLogin()){}
        }catch (e: org.openqa.selenium.NoSuchElementException) {
            try {
                return checkProfile(person)
            } catch (nse: Exception) {
                try {
                    return checkProfile(person)
                } catch (nse2: Exception) {
                    person = checkDetails(person)
                }
            }
        }
        return person
    }

    private fun checkProfile(person: Person) : Person{
        setupCortex(person.email)
        person.background = checkBackground()
        if(person.background > 0)
            person.status = 1
        if(person.background < 0)
            person.status = -1
        person.videos = checkVideos()
        if (person.phone == "")
            person.phone = driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div/div[1]/div/div[2]/div[2]/div[1]/div[3]/span")).text
        return person
    }


    private fun checkBackground(): Int {
        try {
            val status = driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div/div[1]/div/div[2]/div[1]/div/span[1]")).text.toLowerCase()
            if (status.contains("offboard")) return -1
            val wait = WebDriverWait(driver, 20)
            wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div/div[2]/div/div[2]/div/div[2]/div[8]/div[1]"))))
            val bgClass = driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div/div[2]/div/div[2]/div/div[2]/div[8]/div[1]")).getAttribute("class")
            return if (bgClass.toLowerCase().contains("error")) -2
            else if (bgClass.toLowerCase().contains("completed")) 2
            else if (bgClass.toLowerCase().contains("in-progress")) 1
            else if(driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div/div[1]/div/div[2]/div[1]/div/span[1]")).text.toLowerCase().contains("offboard")) -1
            else 0
        } catch (ignored: java.lang.Exception) {
        }
        return 0
    }

    private fun checkVideos(): Boolean {
        return driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div/div[2]/div/div[2]/div/div[2]/div[5]/div[1]")).getAttribute("class").toLowerCase().contains("complete")
    }

    private fun checkDetails(person: Person) : Person{
        driver["https://logistics.amazon.com/account-management/delivery-associates?providerType=DA&email=&searchStart=0&searchSize=100"]
        val wait = WebDriverWait(driver, 15)
        wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//*[@id=\"name-input\"]"))))
        driver.findElement(By.xpath("//*[@id=\"name-input\"]")).sendKeys(person.lastName)
        wait.until(ExpectedConditions.textToBePresentInElement(driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div[3]/div[1]/div[1]/button")), "Search"))
        driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div[3]/div[1]/div[1]/button")).sendKeys(Keys.SPACE)
        Thread.sleep(3000)
        var x = 0
        person.background = -1
        val elements = driver.findElements(By.xpath("/html/body/div[1]/main/div/div/div/main/div[3]/div[2]/div[1]/table/tbody/*"))
        for(element in elements){
            val elementName = element.findElement(By.xpath("./td[1]/a")).text.split(" ")
            if (element.findElement(By.xpath("./td[1]/a")).text.split(" ")[0].trim() == person.firstName &&
                    element.findElement(By.xpath("./td[7]")).text.contains("Onboard")) {
                person.background = 0
                val y = element.findElement(By.xpath("./td[6]")).text.split("/")[0].trim().toInt()
                if (y > x) {
                    x = y
                    person.email = element.findElement(By.xpath("./td[4]")).text.trim()
                }
            }
        }
        return person
    }

    @Throws(TimeoutException::class, org.openqa.selenium.NoSuchElementException::class)
    private fun setupCortex(email: String) {
        val wait = WebDriverWait(driver, 15)
        wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//*[@id=\"email-input\"]"))))
        driver.findElement(By.xpath("//*[@id=\"email-input\"]")).clear()
        driver.findElement(By.xpath("//*[@id=\"email-input\"]")).sendKeys(email)
        wait.until(ExpectedConditions.textToBePresentInElement(driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div[3]/div[1]/div[1]/button")), "Search")) //elementToBeClickable(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div[3]/div[1]/button")));
        driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div[3]/div[1]/div[1]/button")).sendKeys(Keys.SPACE)
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div[3]/div[2]/div[1]/table/tbody/tr/td[1]/a")))
        driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div[3]/div[2]/div[1]/table/tbody/tr/td[1]/a")).click()
        driver.close()
        driver.switchTo().window(ArrayList(driver.windowHandles)[0])
        //driver.switchTo().window(ArrayList(driver.windowHandles)[1])
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div/div[2]/div/div[1]")))
        driver.findElement(By.xpath("//*[@id=\"dsp-onboarding\"]/div/main/div/div[2]/div/div[1]")).click()
        //driver.close()
        //driver.switchTo().window(ArrayList(driver.windowHandles)[0])
    }

    @Throws(NoSuchElementException::class)
    private fun cortexLogin(): Boolean {
        if (!login()) captchaLogin()
        return true
    }

    @Throws(NoSuchElementException::class)
    private fun login(): Boolean {
        return try {
            val url = "https://logistics.amazon.com/account-management/delivery-associates?providerType=DA&email=&searchStart=0&searchSize=100"
            driver.get(url)
            val wait = WebDriverWait(driver, 20)
            wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.id("ap_email"))))
            var element: WebElement = driver.findElement(By.id("ap_email"))
            element.clear()
            element.sendKeys(credential.user)
            element = driver.findElement(By.id("ap_password"))
            element.clear()
            element.sendKeys(credential.pass)
            element = driver.findElement(By.id("signInSubmit"))
            element.click()
            wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.id("ap_email"))))
            false
        } catch (e: TimeoutException) {
            true
        }
    }

    @Throws(NoSuchElementException::class)
    private fun captchaLogin(): Boolean {
        if (captchaCounter > 3) {
            driver.close()
        }
        try {
            captchaCounter++
            val cap: WebElement = driver.findElement(By.xpath("//*[@id=\"auth-captcha-image\"]"))
            val arrScreen: ByteArray = (driver as TakesScreenshot).getScreenshotAs(OutputType.BYTES)
            val imageScreen = ImageIO.read(ByteArrayInputStream(arrScreen))
            val capDimension: Dimension = cap.size
            val capLocation: Point = cap.location
            val imgCap = imageScreen.getSubimage(capLocation.x, capLocation.y, capDimension.width, capDimension.height)
            val os = ByteArrayOutputStream()
            ImageIO.write(imgCap, "png", os)
            val client = SocketClient("ghostrev", "Wearedead66")
            val res: Captcha = client.decode(ByteArrayInputStream(os.toByteArray()))
            if (res.isSolved && res.isCorrect) {
                println("Captcha Correct")
                driver.findElement(By.id("ap_password")).sendKeys(credential.pass)
                val captcha: WebElement = driver.findElement(By.xpath("//*[@id=\"auth-captcha-guess\"]"))
                val letters: CharArray = res.text.toCharArray()
                for (letter in letters) {
                    captcha.sendKeys(letter.toString())
                    Thread.sleep((Math.random() + .5).toLong() * 1200)
                }
                driver.findElement(By.xpath("//*[@id=\"signInSubmit\"]")).click()
            }
        } catch (e: IOException) {
            return true
        } catch (e: com.deathbycaptcha.Exception) {
            return true
        } catch (e: InterruptedException) {
            return true
        }
        return captchaLogin()
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
        driver = ChromeDriver(options)
    }

    private fun setupCredentials(){
        credential = credentialRepository!!.findAllByCompanyAndType(peopleIn[0].company.pk!!, "Cortex")
    }
}
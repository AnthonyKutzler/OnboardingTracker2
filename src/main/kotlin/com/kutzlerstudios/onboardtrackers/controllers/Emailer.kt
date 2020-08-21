package com.kutzlerstudios.onboardtrackers.controllers

import com.google.api.services.gmail.model.Message
import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.repositories.CompanyRepository
import com.kutzlerstudios.onboardtrackers.repositories.OnboardRepository
import com.kutzlerstudios.onboardtrackers.repositories.PeopleRepository
import com.kutzlerstudios.onboardtrackers.services.GmailService
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Autowired
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.time.LocalDate
import java.util.*
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.security.auth.Subject

class Emailer(var from: String, @Autowired var peopleRepository: PeopleRepository, @Autowired var companyRepository: CompanyRepository) {

    @Autowired
    private lateinit var onboardRepository: OnboardRepository

    private var subject: String = ""

    private val drugMessage = """Congratulations, your background passed. Look out for an email from Quest Diagnostics(any QUEST location will suffice), this will have the information you need for your pre-employment drug screen. Also be sure to login to the Amazon portal(where you started the background) and watch your seven onboarding videos. When both of these are completed, please reach reply to this email to schedule a date for you to start your paid classroom training.

We look forward to having you be apart of our team.

Sincerely, """

    fun sendQuestEmail(email : String, msg: String) {
        try {
            sendEmail(email, "*****************CHECK QUEST*****************", "QUEST: $msg")
        } catch (ignored: Exception) {
        }
    }

    fun sendDaEmail(person: Person){
        try {
            sendEmail(person.email, "Onboarding Update ${person.firstName} ${person.lastName}", "$drugMessage ${person.company.name} Management.")
        } catch (ignored: Exception) {
        }
    }

    fun sendDaRecap(){
        var people = buildPeopleLists()
        subject = "Onboarding Daily Recap ${LocalDate.now()}"
        for(peopleList in people){
            var to = ""
            var sBuilder = StringBuilder("Changes to DAs: \n")
            for(person in peopleList){
                if(person.onboard.pk != person.checked){
                    sBuilder.append(getChanges(person)).append("\n")
                    person.checked = person.onboard.pk
                    peopleRepository.save(person)
                    to = person.company.email!!
                }
            }
            sendEmail(to, subject, sBuilder.toString())
        }
    }

    fun getChanges(person: Person) : String{
        val onboard = onboardRepository.findByPk(person.checked!!)
        val builder = StringBuilder("${person.firstName} ${person.lastName}\n")
        if(person.onboard.background != onboard.background){
            builder.append("\t")
            when (person.onboard.background){
                -1 -> builder.append("Background: Needs Review(equivalent to Failed")
                1 -> builder.append("Background: Pending")
                2 -> builder.append("Background: Meets Requirements")
            }
            builder.append("\n")
        }
        if(person.onboard.drug != onboard.drug){
            builder.append("\t")
            when (person.onboard.drug){
                -3 -> builder.append("Drug: Failed(Positive)")
                -2 -> builder.append("Drug: Failed(Refusal)")
                -1 -> builder.append("Drug: Failed(Expired)")
                1 -> builder.append("Drug: Scheduled")
                2 -> builder.append("Drug: Collected")
                3 -> builder.append("Drug: At Lab/Mro")
                4 -> builder.append("Drug: Passed(Negative)")
            }
            builder.append("\n")
        }
        if(person.onboard.videos != onboard.videos){
            builder.append("\t Videos: Completed\n")
        }
        return builder.toString()
    }

    private fun buildPeopleLists(): List<List<Person>>{
        var parent = mutableListOf<List<Person>>()
        for(company in companyRepository.findAll()){
            parent.add(peopleRepository.getAllByCompanyAndStatusLessThan(company.pk!!))
        }
        return parent
    }

    @Throws(java.lang.Exception::class)
    private fun sendEmail(to: String, subject: String, body: String) {
        val email: MimeMessage = createMessage(to, subject, body)
        val buffer = ByteArrayOutputStream()
        email.writeTo(buffer)
        val bytes = buffer.toByteArray()
        val encodedEmail = Base64.encodeBase64URLSafeString(bytes)
        val message = Message()
        message.raw = encodedEmail
        GmailService().getService().users().messages().send("me", message).execute()
    }

    @Throws(MessagingException::class)
    private fun createMessage(to: String, subject: String, body: String): MimeMessage {
        val props = Properties()
        val session = Session.getDefaultInstance(props, null)
        val email = MimeMessage(session)
        email.setFrom(InternetAddress(from))
        email.addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(to))
        email.subject = subject
        email.setText(body)
        return email
    }
}
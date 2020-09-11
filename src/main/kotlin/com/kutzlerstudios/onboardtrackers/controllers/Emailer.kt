package com.kutzlerstudios.onboardtrackers.controllers

import com.google.api.services.gmail.model.Message
import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.repositories.CompanyRepository
import com.kutzlerstudios.onboardtrackers.repositories.PeopleRepository
import com.kutzlerstudios.onboardtrackers.services.GmailService
import org.apache.commons.codec.binary.Base64
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.time.LocalDate
import java.util.*
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class Emailer(var from: String, private var peopleRepository: PeopleRepository, private var companyRepository: CompanyRepository) {


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
            if(peopleList.isNotEmpty()) {
                var to = peopleList[0].company.email!!
                var sBuilder = StringBuilder("Changes to DAs: \n")
                for (person in peopleList) {
                    sBuilder.append(getChanges(person)).append("\n")
                    person.bg = person.background
                    person.dt = person.drug
                    person.vids = person.videos
                    peopleRepository.save(person)
                    to = person.company.email!!

                }
                sendEmail(to, subject, sBuilder.toString())
            }
        }
    }

    fun getChanges(person: Person) : String{
        val builder = StringBuilder("${person.firstName} ${person.lastName}\n")
        if(person.status!! != -1) {
            if (person.background != person.bg) {
                builder.append("\t")
                when (person.background) {
                    -1 -> builder.append("Background: Needs Review(equivalent to Failed)")
                    1 -> builder.append("Background: Pending")
                    2 -> builder.append("Background: Meets Requirements")
                }
                builder.append("\n")
            }
            if (person.drug != person.dt) {
                builder.append("\t")
                when (person.drug) {
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
            if (person.videos != person.vids) {
                builder.append("\t Videos: Completed\n")
            }
        }else
            builder.append("\t").append("Offboarded").append("\n")
        return builder.toString()
    }

    private fun buildPeopleLists(): List<List<Person>>{//TODO BUILD EXTERNALLY
        var parent = mutableListOf<List<Person>>()
        for(company in companyRepository.findAll()){
            parent.add(peopleRepository.getAlByCompanyAndChanges(company.pk!!))
        }
        return parent
    }

    fun sendBreakdownEmail(co: Int){
        val company = companyRepository.getByPk(co)
        //if(peopleRepository.countNewInLast7(co) >= 7) {
            val builder = StringBuilder("Onboarding Breakdown(at/waiting on)\n")
            builder.append("\tPassed: ").append(peopleRepository.countPassed(co)).append("\n")
            builder.append("\t\tVideos: ").append(peopleRepository.countVideos(co)).append("\n")
            builder.append("\tDrug Test").append("\n")
            builder.append("\t\tAt Lab: ").append(peopleRepository.countDrug(3, co)).append("\n")
            builder.append("\t\tCollected: ").append(peopleRepository.countDrug(2, co)).append("\n")
            builder.append("\t\tScheduled: ").append(peopleRepository.countDrug(1, co)).append("\n")
            builder.append("\tBackground").append("\n")
            builder.append("\t\tPending: ").append(peopleRepository.countBackgrounds(1, co)).append("\n")
            builder.append("\t\tNot Started: ").append(peopleRepository.countBackgrounds(0, co)).append("\n")
            builder.append("\n\n").append("Failure Stats").append("\n")
            builder.append("\tFailed/Offboarded: ").append(peopleRepository.countFailed(co))
            sendEmail(company.email!!, "Onboard Breakdown", builder.toString())
        //}

    }

    fun sendCollectedEmail(person: Person){
        sendEmail(person.company.email!!, "DT Collected ${person.firstName} ${person.lastName}", "${person.firstName} ${person.lastName}, has taken their drug test")
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
        email.setFrom(InternetAddress("anthonykutzler@gmail.com"))
        println(to)
        email.addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(to))
        email.subject = subject
        email.setText(body)
        return email
    }
}
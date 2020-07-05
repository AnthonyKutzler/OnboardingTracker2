package com.kutzlerstudios.onboardtrackers.controllers

import com.google.api.services.gmail.model.Message
import com.kutzlerstudios.onboardtrackers.models.Person
import com.kutzlerstudios.onboardtrackers.services.GmailService
import org.apache.commons.codec.binary.Base64
import java.io.ByteArrayOutputStream
import java.util.*
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class Emailer {

    private val message: String = ""
    private val subject: String = ""

    fun sendQuestEmail(msg: String) {
        try {
            sendEmail("anthonykutzler@cnclogistics.biz", "*****************CHECK QUEST*****************", "QUEST: $msg")
        } catch (ignored: Exception) {
        }
    }

    fun sendDaRecap(people : List<Person>){



        //TODO: FIXME: IMPLEMENT ME
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
        email.setFrom(InternetAddress("AnthonyKutzler@gmail.com"))
        email.addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(to))
        email.subject = subject
        email.setText(body)
        return email
    }
}
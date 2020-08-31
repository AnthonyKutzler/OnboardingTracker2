package com.kutzlerstudios.onboardtrackers.services

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber

class TwilioHelper(stationPhone: String, to: String, messageInt: Int) {
    companion object {
        private const val SID = "AC590d3ddefda92206401845aefb1e88a2" //"AC60a0be21708305433a35dc37e3a5fbb4";
        private const val TOKEN = "161910628ea65fd0cf7e598e5920f499" //"b9b7a347e510d338964e90e0ab052375";
    }

    init {
        val message: String
        var text = ""
        when (messageInt) {
            0 -> {
                message = """PLEASE DO NOT REPLY TO THIS MESSAGE(This is an automated messaging service)

If your still interested in the position look for the email from Amazon Logistics, to start your background check. If your having any trouble, reach out to us at $stationPhone for additional help.
Thank you!"""
                text = "BG Start "
            }
            1 -> {
                message = """PLEASE DO NOT REPLY TO THIS MESSAGE(This is an automated messaging service)

Congratulations, your background passed. Look out for an email from Quest Diagnostics(any QUEST location will suffice, be sure to check your spam folder), this will have the information you need for your pre-employment drug screen. Also be sure to login to the Amazon portal(where you started the background) and watch your seven onboarding videos. When both of these are completed, please reach reply to this email to schedule a date for you to start your paid classroom training.

We look forward to having you be apart of our team.

Sincerely, CNC Management"""
                text = "DT Start "
            }
            2 -> {
                message = """PLEASE DO NOT REPLY TO THIS MESSAGE(This is an automated messaging service)

This is a reminder that your background has come back and about 7 days ago we set you up with a drug test, with Quest Diagnostics. Also if you haven't already, be sure to log back into the Amazon portal(where you started the background) and watch your seven onboarding videos. When both of these are completed, please reach out to us at $stationPhone to schedule a date for you to start your paid classes.

We look forward to having you be apart of our team.

Sincerely, CNC Management"""
                text = "DT Reminder "
            }
            3 -> {
                message = """
                    PLEASE DO NOT REPLY TO THIS MESSAGE(This is an automated messaging service)
                    
                    Now that you have completed, or are about to complete the classroom training, look out for an email, from Mentor, and follow those directions to setup your Mentor password. You don't need to install the app, we will supply a device with the app on it, and you will use this daily along side the rabbit app.
                    """.trimIndent()
                text = "Mentor"
            }
            else -> {
                message = ""
                text = ""
            }
        }
        try {
            Twilio.init(SID, TOKEN)
            //Message.creator(PhoneNumber("+1" + to.replace("\\D+".toRegex(), "")), PhoneNumber("+16308668965") /*new PhoneNumber("+15005550006")*/, message).create()
            println("$text text Sent to $to")
        } catch (ignored: Exception) {
        }
    }
}
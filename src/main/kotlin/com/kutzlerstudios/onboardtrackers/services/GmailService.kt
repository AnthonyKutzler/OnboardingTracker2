package com.kutzlerstudios.onboardtrackers.services

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import java.io.*

class GmailService {
    private val TOKENS_DIRECTORY_PATH = pathToJarDir() + "/gtokens"
    private val CREDENTIALS = pathToJarDir().trim { it <= ' ' } + "/resources/akutzGmailCreds.json".trim { it <= ' ' }
    private val service: Gmail

    @Throws(IOException::class)
    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        val CREDENTIALS_FILE_PATH = CREDENTIALS
        val `in` = FileInputStream(CREDENTIALS_FILE_PATH)
                ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
        val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))
        val flow = GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    fun getService(): Gmail {
        return service
    }

    private fun pathToJarDir(): String {
        try {
            return File(GmailService::class.java.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile().getPath()
        } catch (ignored: Exception) {
        }
        return "/home/gob/Onboard"
    }

    companion object {
        private const val APPLICATION_NAME = "Onboard Tracker"
        private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
        private val SCOPES = listOf<String>(GmailScopes.MAIL_GOOGLE_COM)
    }

    init {
        val HTTP_TRANSPORT: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        service = Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build()
    }
}
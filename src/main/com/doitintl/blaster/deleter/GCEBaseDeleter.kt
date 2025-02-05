package com.doitintl.blaster.deleter

import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.compute.Compute
import com.google.api.services.compute.model.Operation
import java.lang.System.currentTimeMillis


abstract class GCEBaseDeleter : BaseDeleter() {

    companion object {
        private const val twoMin = 1000 * 60 * 2

        fun getComputeService(): Compute {
            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            var credential = GoogleCredential.getApplicationDefault()
            if (credential.createScopedRequired()) {
                credential = credential.createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
            }

            return Compute.Builder(httpTransport, jsonFactory, credential).setApplicationName(CLOUD_BLASTER)
                .build()
        }


        fun waitOnZonalOperation(
            project: String,
            location: String,
            operation: Operation
        ) {
            val start = currentTimeMillis()

            val timeout = start + twoMin
            while (currentTimeMillis() < timeout) {
                val currentOperation: Operation = getComputeService()
                    .zoneOperations()
                    .get(project, location, operation.name)
                    .execute()
                if (currentOperation.status == "DONE") {
                    return
                }
                Thread.sleep(SLEEP_IN_LOOPS_MS)
            }
        }

        fun waitOnRegionalOperation(project: String, location: String, operation: Operation) {
            val start = currentTimeMillis()
            val timeout = start + twoMin
            while (currentTimeMillis() < timeout) {
                val currentOperation: Operation = getComputeService()
                    .regionOperations().get(project, location, operation.name).execute()
                if (currentOperation.status == "DONE") {
                    return
                }
                Thread.sleep(SLEEP_IN_LOOPS_MS)
            }
        }

        fun waitOnGlobalOperation(project: String, operation: Operation) {
            val start = currentTimeMillis()

            val timeout = start + twoMin
            while (currentTimeMillis() < timeout) {
                val currentOperation: Operation = getComputeService()
                    .globalOperations().get(project, operation.name).execute()
                if (currentOperation.status == "DONE") {
                    return
                }
                Thread.sleep(SLEEP_IN_LOOPS_MS)
            }
        }
    }
}

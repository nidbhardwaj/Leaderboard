package com.example

import com.example.leaderboard.fetchLeaderboardCsv
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
}
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/leaderboard") {
            // Run the fetch in a coroutine with a timeout
            val csv = withTimeoutOrNull(5000L) {
                fetchLeaderboardCsv()
            } ?: "Request timed out."
            println(csv)

            call.respondText(csv, ContentType.Text.Plain)
        }
    }
}

// --- Your Shopify fetching logic ---

const val shopUrl = "https://interview-00x037s7zm.myshopify.com/admin/api/2025-01/graphql.json"
const val apiToken = "" // Add the api token here

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

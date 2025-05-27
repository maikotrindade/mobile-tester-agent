package io.github.maikotrindade

import kotlinx.coroutines.runBlocking
import io.github.cdimascio.dotenv.dotenv

fun main() = runBlocking {
    val dotenv = dotenv()
    val geminiToken = dotenv["GEMINI_API_KEY"]
}
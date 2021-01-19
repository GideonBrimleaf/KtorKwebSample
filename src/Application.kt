package com.example

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.state.KVar
import kweb.state.render
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(DefaultHeaders)
    install(Compression)
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(10)
        timeout = Duration.ofSeconds(30)
    }
    install(Kweb)


    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/kweb-async-response") {
            call.respondKweb {
                doc.body {
                    val name = KVar("Loading")
                    GlobalScope.launch {
                        delay(3000L)
                        name.value = "Han Solo"
                    }
                    h1().text(name.map{ "The name is $it" })
                }
            }
        }

        get("/freemarker-async-response") {
            val name = KVar("Loading")
            GlobalScope.launch {
                delay(3000L)
                name.value = "Han Solo"
            }
//            val element = Element(jsExpression = """document.getElementById("header-name")""")
            call.respond(FreeMarkerContent("index.ftl", mapOf("name" to name.map { "The name is $it" })))
        }

        get("/dynamic-input") {
            call.respondKweb {
                doc.body {
                    h1().text("I'm Mary Poppins Y'All!")
                    p().text("What is your name?")
                    val textField = input()
                    val name = KVar("")
                    textField.value = name
                    render(name) {
                        if (name.value == "") {
                            p().text("Enter a name")
                        } else {
                            p().text(name.map { "Hello $it" })
                        }
                    }
                }
            }
        }
    }
}


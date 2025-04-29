package com.apppillar

import com.apppillar.auth.JwtConfig
import com.apppillar.database.UserRepository
import com.apppillar.models.*
import com.apppillar.services.EmailService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepo = UserRepository()
    val emailService = EmailService(
        smtpHost = "smtp.gmail.com",
        smtpPort = "587",
        username = "amir.makhmudov1990@gmail.com",        // заменишь на свой email
        password = "fxlb zaum epwr hueg"           // не обычный пароль! нужен app password
    )

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/register") {
            val registerRequest = call.receive<RegisterRequest>()
            val wasCreated =
                userRepo.registerUser(registerRequest.name, registerRequest.email, registerRequest.password)

            if (wasCreated) {
                call.respondText("Пользователь зарегистрирован")
            } else {
                call.respondText("Email уже используется", status = io.ktor.http.HttpStatusCode.Conflict)
            }
        }
        post("/login") {
            val request = call.receive<LoginRequest>()

            val name = userRepo.checkCredentials(request.email, request.password)
            if (name != null) {
                val token = JwtConfig.generateToken(request.email)
                call.respond(LoginResponse(token = token, name = name))
            } else {
                call.respondText("Неверный email или пароль", status = HttpStatusCode.Unauthorized)
            }
        }
        post("/forgot-password") {
            val request = call.receive<ForgotPasswordRequest>()
            val exists = userRepo.isUserExists(request.email)

            if (!exists) {
                call.respondText("Пользователь не найден", status = HttpStatusCode.NotFound)
                return@post
            }

            val token = userRepo.createPasswordResetToken(request.email)

            // Ссылка на восстановление пароля (тестовый вариант)
            val resetLink = "http://192.168.1.150:8080/reset-password?token=$token"

            emailService.sendResetEmail(request.email, resetLink)
        }
        post("/reset-password") {
            val request = call.receive<ResetPasswordRequest>()

            val success = userRepo.resetPassword(request.token, request.newPassword)
            if (success) {
                call.respondText("Пароль успешно обновлён")
            } else {
                call.respondText("Невалидный или просроченный токен", status = HttpStatusCode.BadRequest)
            }
        }
        // 1. Открываем HTML по токену
        get("/reset-password") {
            val token = call.parameters["token"]
            if (token.isNullOrBlank()) {
                call.respondText("Токен отсутствует", status = HttpStatusCode.BadRequest)
                return@get
            }

            call.respondText(
                """
                <html>
                <head>
                    <title>Сброс пароля</title>
                    <style>
                        body { font-family: sans-serif; padding: 40px; background: #f9f9f9; }
                        form { max-width: 400px; margin: auto; background: white; padding: 20px; border-radius: 8px; }
                        input { width: 100%; padding: 10px; margin: 10px 0; }
                        button { padding: 10px 20px; }
                    </style>
                </head>
                <body>
                    <form method="POST" action="/reset-password-submit">
                        <h2>Введите новый пароль</h2>
                        <input type="hidden" name="token" value="$token" />
                        <input type="password" name="newPassword" placeholder="Новый пароль" required />
                        <button type="submit">Сбросить пароль</button>
                    </form>
                </body>
                </html>
                """.trimIndent(),
                ContentType.Text.Html
            )
        }
        // 2. Обработка отправки формы
        post("/reset-password-submit") {
            val params = call.receiveParameters()
            val token = params["token"]
            val newPassword = params["newPassword"]

            if (token.isNullOrBlank() || newPassword.isNullOrBlank()) {
                call.respondText("Неверные данные", status = HttpStatusCode.BadRequest)
                return@post
            }

            val success = userRepo.resetPassword(token, newPassword)
            if (success) {
                call.respondText("<h2>Пароль успешно обновлён 🎉</h2>", ContentType.Text.Html)
            } else {
                call.respondText("<h2>Невалидный или просроченный токен</h2>", ContentType.Text.Html)
            }
        }
    }
}

package com.example.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.net.URL
import java.util.concurrent.TimeUnit

fun Application.configureAuth() {
    val jwkProvider = JwkProviderBuilder(
        URL("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com")
    )
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val firebaseProjectId = "electronichome-6ed77"

    install(Authentication) {
        jwt("resident") {
            verifier(jwkProvider) {
                withIssuer("https://securetoken.google.com/electronichome-6ed77")
                withAudience(firebaseProjectId)
            }
            validate { credential ->
                val issuer = credential.payload.issuer
                val audience = credential.payload.audience

                val uid = credential.payload.getClaim("user_id").asString() ?:
                credential.payload.getClaim("uid").asString() ?:
                credential.payload.getClaim("sub").asString()



                if (uid != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }


        bearer("management") {
            authenticate { tokenCredential ->
                val mgmtToken = System.getenv("MANAGEMENT_TOKEN") ?: "dev-mgmt-secret"
                if (tokenCredential.token == mgmtToken) {
                    UserIdPrincipal("management")
                } else null
            }
        }
    }
}
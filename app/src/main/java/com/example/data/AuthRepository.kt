package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

data class UserSession(
    val uid: String,
    val email: String?
)

class AuthRepository {
    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    private var localUserSession: UserSession? = null

    val currentUserSession: UserSession?
        get() {
            if (firebaseAuth != null) {
                try {
                    val fbUser = firebaseAuth.currentUser
                    if (fbUser != null) {
                        return UserSession(fbUser.uid, fbUser.email)
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
            return localUserSession
        }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserSession> {
        if (email.isBlank() || pass.isBlank()) {
            return Result.failure(Exception("Por favor, preencha o e-mail e a senha."))
        }
        if (firebaseAuth == null) {
            return Result.failure(Exception("Serviço de autenticação Firebase indisponível. Verifique a configuração do projeto ou sua conexão."))
        }

        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user
            if (user != null) {
                val session = UserSession(user.uid, user.email)
                localUserSession = session
                Result.success(session)
            } else {
                Result.failure(Exception("Usuário não encontrado no banco de dados."))
            }
        } catch (e: Exception) {
            // Se a conta não existir no Firebase Auth, executa o fluxo real de cadastramento do usuário
            if (e is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                e.message?.contains("user-not-found", ignoreCase = true) == true ||
                e.message?.contains("no user record", ignoreCase = true) == true) {
                signUpWithEmail(email.trim(), pass)
            } else {
                val msg = when {
                    e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Senha incorreta para esta conta de e-mail."
                    e.message?.contains("password", ignoreCase = true) == true -> "Senha incorreta."
                    else -> e.localizedMessage ?: "Falha ao realizar login no Firebase."
                }
                Result.failure(Exception(msg))
            }
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<UserSession> {
        if (email.isBlank() || pass.isBlank()) {
            return Result.failure(Exception("Por favor, preencha e-mail e senha."))
        }
        if (pass.length < 6) {
            return Result.failure(Exception("Para cadastrar uma nova conta no Firebase, a senha deve ter pelo menos 6 caracteres."))
        }
        if (firebaseAuth == null) {
            return Result.failure(Exception("Serviço de autenticação Firebase indisponível."))
        }

        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user
            if (user != null) {
                val session = UserSession(user.uid, user.email)
                localUserSession = session
                Result.success(session)
            } else {
                Result.failure(Exception("Não foi possível registrar o usuário no Firebase."))
            }
        } catch (e: Exception) {
            val msg = when {
                e is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Este e-mail já possui uma conta cadastrada. Verifique a senha informada."
                e is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "A senha é muito fraca. Utilize no mínimo 6 caracteres."
                e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "O e-mail digitado possui um formato inválido."
                else -> e.localizedMessage ?: "Erro ao realizar cadastro no Firebase."
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): Result<UserSession> {
        if (firebaseAuth == null) {
            return Result.failure(Exception("Serviço de autenticação Firebase não configurado no aplicativo."))
        }
        if (idToken.isBlank() || idToken == "google_user_credential_token" || idToken == "mock_google_id_token") {
            return Result.failure(Exception("Credencial do Google inválida ou não selecionada."))
        }

        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                val session = UserSession(user.uid, user.email)
                localUserSession = session
                Result.success(session)
            } else {
                Result.failure(Exception("Não foi possível autenticar o usuário Google no Firebase."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Falha na autenticação via Google no Firebase: ${e.localizedMessage ?: "Verifique sua conta e conexão com a internet."}"))
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            // ignore
        }
        localUserSession = null
    }
}


package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import android.util.Log

data class UserSession(
    val uid: String,
    val email: String?
)

class AuthRepository {
    private val firebaseAuth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("AuthRepository", "FirebaseAuth não disponível ainda: ${e.message}")
            null
        }

    private var localUserSession: UserSession? = null

    val currentUserSession: UserSession?
        get() {
            val auth = firebaseAuth
            if (auth != null) {
                try {
                    val fbUser = auth.currentUser
                    if (fbUser != null && !fbUser.isAnonymous && !fbUser.email.isNullOrBlank()) {
                        return UserSession(fbUser.uid, fbUser.email)
                    } else if (fbUser != null && (fbUser.isAnonymous || fbUser.email.isNullOrBlank())) {
                        auth.signOut()
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
        val auth = firebaseAuth
            ?: return Result.failure(Exception("Serviço de autenticação Firebase indisponível. Verifique a configuração do projeto ou sua conexão."))

        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
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
                    e.message?.contains("API key not valid", ignoreCase = true) == true ||
                    e.message?.contains("API key", ignoreCase = true) == true -> "A chave da API do Firebase precisa estar ativa e com a API 'Identity Toolkit' habilitada no Google Cloud Console."
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
        val auth = firebaseAuth
            ?: return Result.failure(Exception("Serviço de autenticação Firebase indisponível."))

        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
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
                e.message?.contains("API key not valid", ignoreCase = true) == true ||
                e.message?.contains("API key", ignoreCase = true) == true -> "A chave da API do Firebase precisa estar ativa e com a API 'Identity Toolkit' habilitada no Google Cloud Console."
                e is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Este e-mail já possui uma conta cadastrada. Verifique a senha informada."
                e is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "A senha é muito fraca. Utilize no mínimo 6 caracteres."
                e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "O e-mail digitado possui um formato inválido."
                else -> e.localizedMessage ?: "Erro ao realizar cadastro no Firebase."
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): Result<UserSession> {
        Log.d("AuthRepository", "signInWithGoogleCredential chamado com token tamanho: ${idToken.length}")
        val auth = firebaseAuth
        if (auth == null) {
            Log.e("AuthRepository", "firebaseAuth é nulo.")
            return Result.failure(Exception("Serviço de autenticação Firebase não configurado no aplicativo."))
        }
        if (idToken.isBlank() || idToken == "google_user_credential_token" || idToken == "mock_google_id_token") {
            Log.e("AuthRepository", "Token inválido fornecido: $idToken")
            return Result.failure(Exception("Credencial do Google inválida ou não selecionada."))
        }

        return try {
            Log.d("AuthRepository", "Criando GoogleAuthProvider credential...")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            Log.d("AuthRepository", "Executando firebaseAuth.signInWithCredential...")
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                Log.d("AuthRepository", "Usuário autenticado com sucesso no Firebase: ${user.email} (${user.uid})")
                val session = UserSession(user.uid, user.email)
                localUserSession = session
                Result.success(session)
            } else {
                Log.e("AuthRepository", "signInWithCredential retornou usuário nulo.")
                Result.failure(Exception("Não foi possível autenticar o usuário Google no Firebase."))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exceção ao autenticar com credencial Google no Firebase: ${e.message}", e)
            val msg = when {
                e.message?.contains("API key not valid", ignoreCase = true) == true ||
                e.message?.contains("API key", ignoreCase = true) == true -> "A chave da API do Firebase precisa estar ativa e com a API 'Identity Toolkit' habilitada no Google Cloud Console."
                e.message?.contains("DEVELOPER_ERROR", ignoreCase = true) == true ||
                e.message?.contains("Developer error", ignoreCase = true) == true -> "Configuração do Google Cloud necessária: cadastre a chave SHA-1 e habilite o provedor Google no console do Firebase."
                else -> "Falha na autenticação via Google no Firebase: ${e.localizedMessage ?: "Verifique sua conta e conexão com a internet."}"
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Firebase Auth não está inicializado."))
        if (email.isBlank()) {
            return Result.failure(Exception("Por favor, informe seu e-mail cadastrado."))
        }
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Log.d("AuthRepository", "E-mail de redefinição de senha enviado para: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Erro ao enviar e-mail de redefinição: ${e.message}", e)
            val msg = when {
                e.message?.contains("user-not-found", ignoreCase = true) == true ||
                e.message?.contains("There is no user record", ignoreCase = true) == true -> "Nenhum usuário encontrado com este e-mail."
                e.message?.contains("invalid-email", ignoreCase = true) == true -> "Formato de e-mail inválido."
                else -> "Erro ao enviar link de redefinição: ${e.localizedMessage ?: "Verifique sua conexão."}"
            }
            Result.failure(Exception(msg))
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


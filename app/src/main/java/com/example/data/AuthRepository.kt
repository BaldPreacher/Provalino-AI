package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import android.util.Log

data class UserAccountItem(
    val uid: String = "",
    val email: String = "",
    val nome: String = "",
    val status: String = "ativo",
    val ultimoLogin: Long = System.currentTimeMillis(),
    val dataCadastro: Long = System.currentTimeMillis()
)

data class UserSession(
    val uid: String,
    val email: String?,
    val userName: String? = null,
    val escolaPadrao: String? = null
)

class AuthRepository {
    private val firebaseAuth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("AuthRepository", "FirebaseAuth não disponível ainda: ${e.message}")
            null
        }

    private val firestore: com.google.firebase.firestore.FirebaseFirestore?
        get() = FirebaseConfig.getFirestore()

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
                syncUserAccount(user.uid, user.email ?: "", user.displayName ?: "")
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
                syncUserAccount(user.uid, user.email ?: "", user.displayName ?: "")
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
                syncUserAccount(user.uid, user.email ?: "", user.displayName ?: "")
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

    suspend fun syncUserAccount(uid: String, email: String, displayName: String = "") {
        val fs = firestore ?: return
        try {
            withTimeoutOrNull(3000L) {
                val cleanEmail = email.trim().lowercase()
                val defaultUserName = if (displayName.isNotBlank()) displayName else cleanEmail.substringBefore("@")
                val now = System.currentTimeMillis()

                val userDoc = fs.collection("users").document(uid).get().await()
                val existingUserName = userDoc.getString("user_name") ?: userDoc.getString("userName")
                val existingEscola = userDoc.getString("escola_padrao") ?: userDoc.getString("escolaPadrao")

                val userMap = mutableMapOf<String, Any>(
                    "uid" to uid,
                    "email" to cleanEmail,
                    "nome" to (existingUserName ?: defaultUserName),
                    "user_name" to (existingUserName ?: defaultUserName),
                    "status" to "ativo",
                    "tipo_usuario" to "professor",
                    "ultimoLogin" to now,
                    "app_version" to "45.0",
                    "app_version_code" to 45L,
                    "plataforma" to "android"
                )
                if (existingEscola != null) {
                    userMap["escola_padrao"] = existingEscola
                }
                fs.collection("users").document(uid).set(userMap, com.google.firebase.firestore.SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Falha ao sincronizar conta no Firestore: ${e.message}")
        }
    }

    suspend fun fetchUserProfile(uid: String): Result<Map<String, String>> {
        val fs = firestore ?: return Result.failure(Exception("Firestore indisponível"))
        return try {
            val doc = fs.collection("users").document(uid).get().await()
            val userName = doc.getString("user_name") ?: doc.getString("nome") ?: ""
            val escolaPadrao = doc.getString("escola_padrao") ?: doc.getString("escolaPadrao") ?: ""
            Result.success(mapOf("user_name" to userName, "escola_padrao" to escolaPadrao))
        } catch (e: Exception) {
            Log.w("AuthRepository", "Aviso ao buscar perfil do professor: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(uid: String, userName: String, escolaPadrao: String): Result<Unit> {
        val fs = firestore ?: return Result.failure(Exception("Firestore indisponível"))
        return try {
            val data = mutableMapOf<String, Any>(
                "user_name" to userName.trim(),
                "nome" to userName.trim(),
                "escola_padrao" to escolaPadrao.trim()
            )
            fs.collection("users").document(uid).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Erro ao atualizar perfil do professor: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllRegisteredAccounts(): Result<List<UserAccountItem>> {
        val fs = firestore ?: return Result.failure(Exception("Firestore indisponível"))
        return try {
            val snapshot = fs.collection("users").get().await()
            val list = mutableListOf<UserAccountItem>()
            for (doc in snapshot.documents) {
                val emailVal = doc.getString("email") ?: doc.getString("mail") ?: ""
                val nomeVal = doc.getString("nome")
                    ?: doc.getString("name")
                    ?: doc.getString("displayName")
                    ?: if (emailVal.isNotBlank()) emailVal.substringBefore("@") else ""

                list.add(
                    UserAccountItem(
                        uid = doc.getString("uid") ?: doc.id,
                        email = if (emailVal.isNotBlank()) emailVal else "Sem e-mail",
                        nome = nomeVal,
                        status = doc.getString("status") ?: "ativo",
                        ultimoLogin = doc.getLong("ultimoLogin")
                            ?: doc.getLong("lastLogin")
                            ?: doc.getLong("last_login")
                            ?: System.currentTimeMillis(),
                        dataCadastro = doc.getLong("dataCadastro")
                            ?: doc.getLong("createdAt")
                            ?: doc.getLong("created_at")
                            ?: System.currentTimeMillis()
                    )
                )
            }
            Result.success(list.sortedByDescending { it.ultimoLogin })
        } catch (e: Exception) {
            Log.w("AuthRepository", "Aviso ao buscar usuários do Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteOrDeactivateUserAccount(uid: String, email: String): Result<Unit> {
        val fs = firestore ?: return Result.failure(Exception("Firestore indisponível"))
        return try {
            // Marca como desativado no Firestore e remove dados de sessão
            fs.collection("users").document(uid).delete().await()
            // Envia e-mail de notificação/redefinição para segurança caso email seja válido
            if (email.isNotBlank() && email.contains("@")) {
                try {
                    firebaseAuth?.sendPasswordResetEmail(email.trim())?.await()
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Aviso no envio de e-mail de encerramento: ${e.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Erro ao remover conta de usuário: ${e.message}", e)
            Result.failure(e)
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


package com.example.ui

import android.app.Application
import android.content.Context
import timber.log.Timber
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlunoNecessidadeEspecial
import com.example.data.NotaAluno
import com.example.data.Prova
import com.example.data.ProvalinoDatabase
import com.example.data.ProvalinoRepository
import com.example.data.Questao
import com.example.data.Turma
import com.example.data.AuthRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.AppUpdateState
import com.example.data.AppVersionChecker
import com.example.data.AnalyticsRepository
import com.example.data.CardCaa
import com.example.data.CardCaaRepository
import com.example.data.CaaImportProgress
import com.example.data.CaaJsonImporter
import com.example.data.PlatformMetrics
import com.example.data.UserAccountItem

data class OfflineNoQuestionsDialogState(
    val subject: String,
    val grade: String,
    val count: Int,
    val type: String = "ANY",
    val profile: String = "REGULAR",
    val aluno: AlunoNecessidadeEspecial? = null,
    val isRetry: Boolean = false
)

class ProvalinoViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val prefs = application.getSharedPreferences("provalino_prefs", Context.MODE_PRIVATE)

    private val _offlineNoQuestionsState = MutableStateFlow<OfflineNoQuestionsDialogState?>(null)
    val offlineNoQuestionsState: StateFlow<OfflineNoQuestionsDialogState?> = _offlineNoQuestionsState.asStateFlow()

    fun dismissOfflineDialogAndRefund() {
        val state = _offlineNoQuestionsState.value
        if (state != null && !state.isRetry) {
            adicionarMoedas(2) // Reembolso automático de 2 moedas
        }
        _offlineNoQuestionsState.value = null
    }

    fun retryOfflineGeneration() {
        val state = _offlineNoQuestionsState.value ?: return
        _offlineNoQuestionsState.value = null
        if (state.aluno != null) {
            generateAndCreateProvaForAluno(
                aluno = state.aluno,
                subject = state.subject,
                grade = state.grade,
                count = state.count,
                skipCoinDeduction = true
            )
        } else {
            generateQuestionsWithAI(
                subject = state.subject,
                grade = state.grade,
                count = state.count,
                type = state.type,
                profile = state.profile,
                skipCoinDeduction = true
            )
        }
    }

    private val repository: ProvalinoRepository = ProvalinoRepository(ProvalinoDatabase.getDatabase(application).dao())

    private val _appUpdateState = MutableStateFlow(AppUpdateState())
    val appUpdateState: StateFlow<AppUpdateState> = _appUpdateState.asStateFlow()

    private val _cardsCaa = MutableStateFlow<List<CardCaa>>(emptyList())
    val cardsCaa: StateFlow<List<CardCaa>> = _cardsCaa.asStateFlow()

    private val _isLoadingCardsCaa = MutableStateFlow(false)
    val isLoadingCardsCaa: StateFlow<Boolean> = _isLoadingCardsCaa.asStateFlow()

    private val _caaImportProgress = MutableStateFlow(CaaImportProgress())
    val caaImportProgress: StateFlow<CaaImportProgress> = _caaImportProgress.asStateFlow()

    private val _registeredAccounts = MutableStateFlow<List<UserAccountItem>>(emptyList())
    val registeredAccounts: StateFlow<List<UserAccountItem>> = _registeredAccounts.asStateFlow()

    private val _isLoadingAccounts = MutableStateFlow(false)
    val isLoadingAccounts: StateFlow<Boolean> = _isLoadingAccounts.asStateFlow()

    val platformMetrics: StateFlow<PlatformMetrics> = AnalyticsRepository.metrics

    fun isNetworkAvailable(): Boolean {
        return try {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    fun checkNetworkStatus() {
        _isOffline.value = !isNetworkAvailable()
    }

    fun refreshPlatformMetrics() {
        viewModelScope.launch {
            AnalyticsRepository.refreshMetrics()
        }
    }

    fun loadRegisteredAccounts() {
        viewModelScope.launch {
            _isLoadingAccounts.value = true
            val result = authRepository.getAllRegisteredAccounts()
            if (result.isSuccess) {
                _registeredAccounts.value = result.getOrDefault(emptyList())
            }
            _isLoadingAccounts.value = false
        }
    }

    fun sendAdminUserPasswordReset(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                onResult(true, "E-mail de redefinição de senha enviado com sucesso para $email!")
            } else {
                onResult(false, result.exceptionOrNull()?.localizedMessage ?: "Erro ao disparar e-mail de redefinição.")
            }
        }
    }

    fun deleteOrDeactivateAdminUserAccount(uid: String, email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.deleteOrDeactivateUserAccount(uid, email)
            if (result.isSuccess) {
                loadRegisteredAccounts()
                onResult(true, "Conta encerrada com sucesso no sistema. Um e-mail de segurança foi emitido.")
            } else {
                onResult(false, result.exceptionOrNull()?.localizedMessage ?: "Erro ao encerrar conta.")
            }
        }
    }

    fun importCaaJsonPipeline(jsonText: String, autoDownloadImages: Boolean = true, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _caaImportProgress.value = CaaImportProgress(isRunning = true, message = "Iniciando leitura do JSON...")
            val result = CaaJsonImporter.importJsonPipeline(
                jsonText = jsonText,
                autoDownloadImages = autoDownloadImages,
                onProgress = { progress ->
                    _caaImportProgress.value = progress
                }
            )
            if (result.isSuccess) {
                loadCardsCaa(forceRefresh = true)
                onComplete(true, "Sucesso: ${result.getOrNull()} cartões importados/sincronizados na coleção card_caa!")
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Falha na importação do JSON."
                _caaImportProgress.value = _caaImportProgress.value.copy(isRunning = false, message = "Erro: $err")
                onComplete(false, err)
            }
        }
    }

    fun loadCardsCaa(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoadingCardsCaa.value = true
            val result = CardCaaRepository.getAllCards(forceRefresh)
            if (result.isSuccess) {
                _cardsCaa.value = result.getOrDefault(emptyList())
            }
            _isLoadingCardsCaa.value = false
        }
    }

    fun searchCardsCaa(query: String = "", categoria: String? = null, nivelCognitivo: String? = null) {
        viewModelScope.launch {
            _isLoadingCardsCaa.value = true
            val result = CardCaaRepository.searchCards(query, categoria, nivelCognitivo)
            if (result.isSuccess) {
                _cardsCaa.value = result.getOrDefault(emptyList())
            }
            _isLoadingCardsCaa.value = false
        }
    }

    fun saveCardCaa(card: CardCaa, onComplete: (Result<String>) -> Unit = {}) {
        viewModelScope.launch {
            val result = CardCaaRepository.saveCard(card)
            if (result.isSuccess) {
                loadCardsCaa(forceRefresh = true)
            }
            onComplete(result)
        }
    }

    fun deleteCardCaa(cardId: String, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = CardCaaRepository.deleteCard(cardId)
            if (result.isSuccess) {
                loadCardsCaa(forceRefresh = true)
            }
            onComplete(result)
        }
    }

    fun checkAppVersion(onFinished: ((AppUpdateState) -> Unit)? = null) {
        viewModelScope.launch {
            val state = AppVersionChecker.checkForUpdates(getApplication())
            _appUpdateState.value = state
            onFinished?.invoke(state)
        }
    }

    fun simulateUpdateDialog(versionCode: Long = 52, force: Boolean = false) {
        _appUpdateState.value = AppUpdateState(
            isUpdateAvailable = true,
            isForceUpdate = force,
            installedVersionCode = AppVersionChecker.getInstalledVersionCode(getApplication()),
            latestVersionCode = versionCode,
            latestVersionName = "$versionCode.0",
            updateTitle = "Nova Versão do Provalino! 🚀",
            updateMessage = "Uma nova versão com melhorias pedagógicas e correções está disponível na Google Play Store. Atualize agora para continuar aproveitando!",
            releaseNotes = "• Melhorias no algoritmo de geração DUA\n• Aprimoramento da compatibilidade de faixas etárias\n• Estabilidade e correções gerais."
        )
    }

    fun publishAppVersionToFirebase(
        latestCode: Long,
        latestName: String,
        minCode: Long,
        forceUpdate: Boolean,
        releaseNotes: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = AppVersionChecker.saveVersionConfig(
                latestCode = latestCode,
                latestName = latestName,
                minCode = minCode,
                forceUpdate = forceUpdate,
                releaseNotes = releaseNotes
            )
            if (result.isSuccess) {
                // Atualiza o estado local imediatamente
                checkAppVersion()
                onResult(true, "Configuração de versão v$latestCode publicada com sucesso no Firebase!")
            } else {
                val err = result.exceptionOrNull()?.message ?: "Erro desconhecido ao salvar no Firestore."
                onResult(false, "Falha ao publicar no Firebase: $err")
            }
        }
    }

    fun dismissUpdateDialog() {
        _appUpdateState.value = _appUpdateState.value.copy(isUpdateAvailable = false)
    }

    private val _currentUser = MutableStateFlow(authRepository.currentUserSession)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authError.value = "Preencha e-mail e senha."
            return
        }
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = authRepository.signInWithEmail(email, pass)
            _authLoading.value = false
            if (result.isSuccess) {
                val session = authRepository.currentUserSession
                _currentUser.value = session
                session?.uid?.let { uid ->
                    if (uid.isNotBlank()) {
                        prefs.edit().putString("cached_teacher_id", uid).apply()
                    }
                }
                AnalyticsRepository.logLoginSuccess("email")
            } else {
                _authError.value = result.exceptionOrNull()?.localizedMessage ?: "Erro ao realizar login."
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authError.value = "Preencha e-mail e senha."
            return
        }
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = authRepository.signUpWithEmail(email, pass)
            _authLoading.value = false
            if (result.isSuccess) {
                val session = authRepository.currentUserSession
                _currentUser.value = session
                session?.uid?.let { uid ->
                    if (uid.isNotBlank()) {
                        prefs.edit().putString("cached_teacher_id", uid).apply()
                    }
                }
                AnalyticsRepository.logLoginSuccess("email_signup")
            } else {
                _authError.value = result.exceptionOrNull()?.localizedMessage ?: "Erro ao criar conta."
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = authRepository.signInWithGoogleCredential(idToken)
            _authLoading.value = false
            if (result.isSuccess) {
                val session = authRepository.currentUserSession
                _currentUser.value = session
                session?.uid?.let { uid ->
                    if (uid.isNotBlank()) {
                        prefs.edit().putString("cached_teacher_id", uid).apply()
                    }
                }
                AnalyticsRepository.logLoginSuccess("google")
            } else {
                _authError.value = result.exceptionOrNull()?.localizedMessage ?: "Erro no login com Google."
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Por favor, digite seu e-mail cadastrado.")
            return
        }
        viewModelScope.launch {
            _authLoading.value = true
            val result = authRepository.sendPasswordResetEmail(email)
            _authLoading.value = false
            if (result.isSuccess) {
                onResult(true, "E-mail de redefinição enviado com sucesso! Verifique sua caixa de entrada e spam.")
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Falha ao enviar e-mail de redefinição."
                onResult(false, err)
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _lastGeneratedProvaId.value = null
    }


    // --- COINS & ADS SYSTEM ---
    private val _moedas = MutableStateFlow(10) // 10 moedas iniciais
    val moedas: StateFlow<Int> = _moedas.asStateFlow()

    private val _showAdDialog = MutableStateFlow(false)
    val showAdDialog: StateFlow<Boolean> = _showAdDialog.asStateFlow()

    private val _adType = MutableStateFlow("REWARDED") // "REWARDED" (+2 moedas) or "INTERSTITIAL" (0 moedas - rentabilização)
    val adType: StateFlow<String> = _adType.asStateFlow()

    // Anti-bot & Anti-abuse Daily and Hourly rate limiting for rewarded ads
    // Limite diário estrito: Máximo de 10 moedas ganhas por dia com anúncios premiados
    private var rewardedCoinsEarnedToday = 0
    private var lastDayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
    private var lastHourTimestamp = System.currentTimeMillis()
    private var rewardedAdsWatchedThisHour = 0
    private val maxRewardedAdsPerHour = 5
    private val maxDailyRewardedCoins = 10

    private val _adLimitMessage = MutableStateFlow<String?>(null)
    val adLimitMessage: StateFlow<String?> = _adLimitMessage.asStateFlow()

    private fun checkDailyAndHourlyLimits() {
        val currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        if (currentDay != lastDayOfYear) {
            rewardedCoinsEarnedToday = 0
            lastDayOfYear = currentDay
            rewardedAdsWatchedThisHour = 0
            lastHourTimestamp = System.currentTimeMillis()
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastHourTimestamp > 3600000) {
            rewardedAdsWatchedThisHour = 0
            lastHourTimestamp = currentTime
        }
    }

    fun deduzirMoedas(quantidade: Int = 2): Boolean {
        if (_moedas.value >= quantidade) {
            _moedas.value -= quantidade
            return true
        } else {
            checkDailyAndHourlyLimits()

            if (rewardedCoinsEarnedToday >= maxDailyRewardedCoins) {
                _adLimitMessage.value = "Você atingiu o limite diário de 10 moedas gratuitas com anúncios hoje ($rewardedCoinsEarnedToday/$maxDailyRewardedCoins). Volte amanhã para resgatar mais moedas!"
            } else if (rewardedAdsWatchedThisHour >= maxRewardedAdsPerHour) {
                _adLimitMessage.value = "Limite de anúncios por hora atingido (máx. 5/hora). Aguarde para evitar bloqueio anti-bot."
            } else {
                _adLimitMessage.value = null
            }

            _adType.value = "REWARDED"
            _showAdDialog.value = true
            return false
        }
    }

    fun adicionarMoedas(quantidade: Int) {
        _moedas.value += quantidade
    }

    fun openAdModal(type: String = "REWARDED") {
        checkDailyAndHourlyLimits()

        if (type == "REWARDED") {
            if (rewardedCoinsEarnedToday >= maxDailyRewardedCoins) {
                _adLimitMessage.value = "Você atingiu o limite diário de 10 moedas gratuitas hoje ($rewardedCoinsEarnedToday/$maxDailyRewardedCoins). Volte amanhã para resgatar mais!"
            } else if (rewardedAdsWatchedThisHour >= maxRewardedAdsPerHour) {
                _adLimitMessage.value = "Você atingiu o limite de ${maxRewardedAdsPerHour} anúncios por hora (Proteção Anti-Bot)."
            } else {
                _adLimitMessage.value = null
            }
        } else {
            _adLimitMessage.value = null
        }

        _adType.value = type
        _showAdDialog.value = true
    }

    fun closeAdModal(grantReward: Boolean = false) {
        _showAdDialog.value = false
        if (grantReward && _adType.value == "REWARDED") {
            checkDailyAndHourlyLimits()

            if (rewardedCoinsEarnedToday >= maxDailyRewardedCoins) {
                _adLimitMessage.value = "Limite diário de 10 moedas já foi atingido hoje ($rewardedCoinsEarnedToday/$maxDailyRewardedCoins)."
            } else if (rewardedAdsWatchedThisHour < maxRewardedAdsPerHour) {
                val coinsToAdd = minOf(2, maxDailyRewardedCoins - rewardedCoinsEarnedToday)
                if (coinsToAdd > 0) {
                    rewardedAdsWatchedThisHour++
                    rewardedCoinsEarnedToday += coinsToAdd
                    adicionarMoedas(coinsToAdd)
                    _adLimitMessage.value = null
                }
            } else {
                _adLimitMessage.value = "Limite horário de resgate atingido. Nenhuma moeda adicionada (Proteção Anti-Bot)."
            }
        }
    }

    // --- STATE FLOWS ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val turmas: StateFlow<List<Turma>> = currentUser.flatMapLatest { user ->
        val effectiveUid = user?.uid?.ifBlank { null } ?: prefs.getString("cached_teacher_id", "") ?: ""
        repository.getTurmas(effectiveUid)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val questoes: StateFlow<List<Questao>> = currentUser.flatMapLatest { user ->
        val effectiveUid = user?.uid?.ifBlank { null } ?: prefs.getString("cached_teacher_id", "") ?: ""
        repository.getQuestoes(effectiveUid)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val provas: StateFlow<List<Prova>> = currentUser.flatMapLatest { user ->
        val effectiveUid = user?.uid?.ifBlank { null } ?: prefs.getString("cached_teacher_id", "") ?: ""
        repository.getProvas(effectiveUid)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentProvasCache: StateFlow<List<Prova>> = repository.getRecentProvas(15).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val alunosInclusao: StateFlow<List<AlunoNecessidadeEspecial>> = currentUser.flatMapLatest { user ->
        val effectiveUid = user?.uid?.ifBlank { null } ?: prefs.getString("cached_teacher_id", "") ?: ""
        repository.getAlunosInclusao(effectiveUid)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- AI GENERATION STATUS ---
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    private val _showTour = MutableStateFlow(false)
    val showTour: StateFlow<Boolean> = _showTour.asStateFlow()

    private val _tourStep = MutableStateFlow(0)
    val tourStep: StateFlow<Int> = _tourStep.asStateFlow()

    private val _teacherUserProfile = MutableStateFlow<Map<String, String>>(emptyMap())
    val teacherUserProfile: StateFlow<Map<String, String>> = _teacherUserProfile.asStateFlow()

    // Memória da última prova gerada nesta sessão (isenta de exibição de anúncio imediato)
    private val _lastGeneratedProvaId = MutableStateFlow<Int?>(null)
    val lastGeneratedProvaId: StateFlow<Int?> = _lastGeneratedProvaId.asStateFlow()

    fun setLastGeneratedProvaId(id: Int?) {
        _lastGeneratedProvaId.value = id
    }

    init {
        Timber.tag("ProvalinoLifecycle").i("ProvalinoViewModel.init started")
        try {
            AnalyticsRepository.initialize(application)
            checkAppVersion()
            loadCardsCaa()
            checkFirstRunOnboarding()
            checkNetworkStatus()
            authRepository.currentUserSession?.uid?.let { uid ->
                if (uid.isNotBlank()) {
                    prefs.edit().putString("cached_teacher_id", uid).apply()
                }
            }
            loadTeacherProfile()
            Timber.tag("ProvalinoLifecycle").i("ProvalinoViewModel.init completed successfully")
        } catch (e: Throwable) {
            Timber.tag("ProvalinoLifecycle").e(e, "Error in ProvalinoViewModel.init: %s", e.message)
        }
    }

    private fun checkFirstRunOnboarding() {
        val hasSeen = prefs.getBoolean("has_seen_onboarding_tour_v2", false)
        if (!hasSeen) {
            _tourStep.value = 0
            _showTour.value = true
        }
    }

    fun startTour() {
        _tourStep.value = 0
        _showTour.value = true
    }

    fun nextTourStep() {
        if (_tourStep.value < 2) {
            _tourStep.value += 1
        } else {
            completeTour()
        }
    }

    fun prevTourStep() {
        if (_tourStep.value > 0) {
            _tourStep.value -= 1
        }
    }

    fun dismissTour() {
        completeTour()
    }

    fun completeTour() {
        _showTour.value = false
        prefs.edit().putBoolean("has_seen_onboarding_tour_v2", true).apply()
    }

    // --- ACTIVE SCREEN STATE ---
    private val _currentScreen = MutableStateFlow("home") // "home", "turmas", "questoes", "provas", "nova_prova", "corrigir_prova"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // --- ACTIVE SELECTIONS FOR CREATING PROVA ---
    private val _selectedQuestions = MutableStateFlow<List<Int>>(emptyList())
    val selectedQuestions: StateFlow<List<Int>> = _selectedQuestions.asStateFlow()

    private val _activeProvaForGrades = MutableStateFlow<Prova?>(null)
    val activeProvaForGrades: StateFlow<Prova?> = _activeProvaForGrades.asStateFlow()

    private val _gradesForActiveProva = MutableStateFlow<List<NotaAluno>>(emptyList())
    val gradesForActiveProva: StateFlow<List<NotaAluno>> = _gradesForActiveProva.asStateFlow()

    fun setScreen(screen: String) {
        _currentScreen.value = screen
        AnalyticsRepository.logScreenView(screen)
    }

    // --- DEVELOPER PANEL (marcio.moura2708@gmail.com) ---
    private val _showDeveloperPanel = MutableStateFlow(false)
    val showDeveloperPanel: StateFlow<Boolean> = _showDeveloperPanel.asStateFlow()

    fun toggleDeveloperPanel(show: Boolean) {
        _showDeveloperPanel.value = show
    }

    // --- OPERATIONS ---

    // 1. TURMAS
    fun addTurma(nome: String, materia: String) {
        viewModelScope.launch {
            if (nome.isNotBlank() && materia.isNotBlank()) {
                repository.insertTurma(Turma(nome = nome, materia = materia, teacherId = currentUser.value?.uid ?: ""))
            }
        }
    }

    fun deleteTurma(id: Int) {
        viewModelScope.launch {
            repository.deleteTurma(id)
        }
    }

    // 2. QUESTOES
    fun addQuestaoManual(
        enunciado: String,
        tipo: String,
        opcaoA: String = "",
        opcaoB: String = "",
        opcaoC: String = "",
        opcaoD: String = "",
        respostaCorreta: String = "",
        assunto: String = "",
        anoEscolar: String = "",
        perfilAdaptacao: String = "REGULAR"
    ) {
        viewModelScope.launch {
            if (enunciado.isNotBlank()) {
                val q = Questao(
                    enunciado = enunciado,
                    tipo = tipo,
                    opcaoA = opcaoA,
                    opcaoB = opcaoB,
                    opcaoC = opcaoC,
                    opcaoD = opcaoD,
                    respostaCorreta = respostaCorreta,
                    assunto = assunto,
                    anoEscolar = anoEscolar,
                    perfilAdaptacao = perfilAdaptacao,
                    teacherId = currentUser.value?.uid ?: ""
                )
                repository.insertQuestao(q)
            }
        }
    }

    fun deleteQuestao(id: Int) {
        viewModelScope.launch {
            repository.deleteQuestao(id)
            // Remove from current selection if selected
            _selectedQuestions.value = _selectedQuestions.value.filter { it != id }
        }
    }

    // AI Generation (Costs 2 Moedas)
    fun generateQuestionsWithAI(
        subject: String,
        grade: String,
        count: Int,
        type: String,
        profile: String = "REGULAR",
        skipCoinDeduction: Boolean = false
    ) {
        if (!skipCoinDeduction && !deduzirMoedas(2)) return // Stops if insufficient coins and opens Ad modal
        viewModelScope.launch {
            _isGenerating.value = true
            _aiError.value = null
            openAdModal("INTERSTITIAL")
            try {
                val tId = currentUser.value?.uid ?: ""
                val savedQuestions = repository.generateAndSaveAIQuestions(subject, grade, count, type, profile, teacherId = tId)
                if (savedQuestions.isNotEmpty()) {
                    AnalyticsRepository.logActivityGenerated(profile, subject, grade, savedQuestions.size)
                    val idsString = savedQuestions.joinToString(",") { it.id.toString() }
                    val newProva = Prova(
                        titulo = "Avaliação de $subject ($grade)",
                        turmaId = null,
                        descricao = "Matéria: $subject | Série: $grade | Perfil: $profile",
                        questoesIds = idsString,
                        teacherId = tId
                    )
                    val provaId = repository.insertProva(newProva)
                    val createdProva = newProva.copy(id = provaId.toInt())
                    _lastGeneratedProvaId.value = createdProva.id
                    _activeProvaForGrades.value = createdProva
                    _currentScreen.value = "provas"
                } else {
                    _offlineNoQuestionsState.value = OfflineNoQuestionsDialogState(
                        subject = subject,
                        grade = grade,
                        count = count,
                        type = type,
                        profile = profile,
                        aluno = null,
                        isRetry = skipCoinDeduction
                    )
                }
            } catch (e: Exception) {
                _offlineNoQuestionsState.value = OfflineNoQuestionsDialogState(
                    subject = subject,
                    grade = grade,
                    count = count,
                    type = type,
                    profile = profile,
                    aluno = null,
                    isRetry = skipCoinDeduction
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // AI Generation and Prova Creation for Student (Costs 2 Moedas)
    fun generateAndCreateProvaForAluno(
        aluno: AlunoNecessidadeEspecial,
        subject: String,
        grade: String,
        count: Int,
        docType: String = "Atividade",
        onComplete: (Prova) -> Unit = {},
        skipCoinDeduction: Boolean = false
    ) {
        if (!skipCoinDeduction && !deduzirMoedas(2)) return
        viewModelScope.launch {
            _isGenerating.value = true
            _aiError.value = null
            openAdModal("INTERSTITIAL")
            try {
                val tId = currentUser.value?.uid ?: ""
                val savedQuestions = repository.generateAndSaveAIQuestions(
                    subject = subject,
                    grade = grade,
                    count = count,
                    type = "ANY",
                    profile = aluno.necessidade,
                    teacherId = tId,
                    nivelAutonomia = aluno.nivelAutonomiaLeitura
                )
                if (savedQuestions.isNotEmpty()) {
                    AnalyticsRepository.logActivityGenerated(aluno.necessidade, subject, grade, savedQuestions.size)
                    val idsString = savedQuestions.joinToString(",") { it.id.toString() }
                    val docPrefix = if (docType.contains("avaliação", ignoreCase = true) || docType.contains("avaliacao", ignoreCase = true)) "Avaliação" else "Atividade"
                    val newProva = Prova(
                        titulo = "$docPrefix Adaptada: ${aluno.nome} - $subject",
                        turmaId = null,
                        descricao = "Aluno: ${aluno.nome} | Perfil: ${aluno.necessidade} (${aluno.nivelSuporte}) | Autonomia: ${aluno.nivelAutonomiaLeitura} | Matéria: $subject ($grade)",
                        questoesIds = idsString,
                        teacherId = tId
                    )
                    val provaId = repository.insertProva(newProva)
                    val createdProva = newProva.copy(id = provaId.toInt())
                    _lastGeneratedProvaId.value = createdProva.id
                    _activeProvaForGrades.value = createdProva
                    _currentScreen.value = "detalhes_prova"
                    onComplete(createdProva)
                } else {
                    _offlineNoQuestionsState.value = OfflineNoQuestionsDialogState(
                        subject = subject,
                        grade = grade,
                        count = count,
                        type = "ANY",
                        profile = aluno.necessidade,
                        aluno = aluno,
                        isRetry = skipCoinDeduction
                    )
                }
            } catch (e: Exception) {
                _offlineNoQuestionsState.value = OfflineNoQuestionsDialogState(
                    subject = subject,
                    grade = grade,
                    count = count,
                    type = "ANY",
                    profile = aluno.necessidade,
                    aluno = aluno,
                    isRetry = skipCoinDeduction
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // AI Generation and Prova Creation (Custom / Direct from Provas Screen)
    fun generateAndCreateProvaCustom(
        titulo: String,
        subject: String,
        grade: String,
        profile: String,
        count: Int,
        turmaId: Int? = null,
        alunoNome: String? = null,
        docType: String = "Atividade",
        onComplete: (Prova) -> Unit = {},
        skipCoinDeduction: Boolean = false,
        nivelAutonomia: String = ""
    ) {
        if (!skipCoinDeduction && !deduzirMoedas(2)) return
        viewModelScope.launch {
            _isGenerating.value = true
            _aiError.value = null
            openAdModal("INTERSTITIAL")
            try {
                val tId = currentUser.value?.uid ?: ""
                val savedQuestions = repository.generateAndSaveAIQuestions(
                    subject = subject,
                    grade = grade,
                    count = count,
                    type = "ANY",
                    profile = profile,
                    teacherId = tId,
                    nivelAutonomia = nivelAutonomia
                )
                if (savedQuestions.isNotEmpty()) {
                    AnalyticsRepository.logActivityGenerated(profile, subject, grade, savedQuestions.size)
                    val idsString = savedQuestions.joinToString(",") { it.id.toString() }
                    val desc = if (!alunoNome.isNullOrBlank()) {
                        "Aluno: $alunoNome | Perfil: $profile | Matéria: $subject ($grade)"
                    } else {
                        "Perfil Pedagógico: $profile | Matéria: $subject ($grade)"
                    }
                    val docPrefix = if (docType.contains("avaliação", ignoreCase = true) || docType.contains("avaliacao", ignoreCase = true)) "Avaliação" else "Atividade"
                    val defaultTitle = if (!alunoNome.isNullOrBlank()) "$docPrefix Adaptada: $alunoNome - $subject" else "$docPrefix: $subject"
                    val newProva = Prova(
                        titulo = titulo.ifBlank { defaultTitle },
                        turmaId = turmaId,
                        descricao = desc,
                        questoesIds = idsString,
                        teacherId = tId
                    )
                    val provaId = repository.insertProva(newProva)
                    val createdProva = newProva.copy(id = provaId.toInt())
                    _lastGeneratedProvaId.value = createdProva.id
                    _activeProvaForGrades.value = createdProva
                    _currentScreen.value = "provas"
                    onComplete(createdProva)
                } else {
                    _offlineNoQuestionsState.value = OfflineNoQuestionsDialogState(
                        subject = subject,
                        grade = grade,
                        count = count,
                        type = "ANY",
                        profile = profile,
                        aluno = null,
                        isRetry = skipCoinDeduction
                    )
                }
            } catch (e: Exception) {
                _offlineNoQuestionsState.value = OfflineNoQuestionsDialogState(
                    subject = subject,
                    grade = grade,
                    count = count,
                    type = "ANY",
                    profile = profile,
                    aluno = null,
                    isRetry = skipCoinDeduction
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // AI Adaptation of an existing question (Costs 2 Moedas)
    fun adaptQuestionWithAI(
        id: Int,
        targetProfile: String,
        onFinished: (Boolean) -> Unit = {}
    ) {
        if (!deduzirMoedas(2)) {
            onFinished(false)
            return // Stops if insufficient coins
        }
        viewModelScope.launch {
            _isGenerating.value = true
            _aiError.value = null
            try {
                val result = repository.adaptAndSaveQuestion(id, targetProfile, teacherId = currentUser.value?.uid ?: "")
                if (result != null) {
                    onFinished(true)
                } else {
                    adicionarMoedas(2) // Refund moedas
                    _aiError.value = "Sem conexão com a internet ou erro na API. Não foi possível adaptar a questão."
                    onFinished(false)
                }
            } catch (e: Exception) {
                adicionarMoedas(2) // Refund moedas
                _aiError.value = "Sem conexão com a internet ou erro na API. Não foi possível adaptar a questão."
                onFinished(false)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // 2. CARTEIRA ALUNO INCLUSÃO (Máximo 9 Carteiras)
    fun addAlunoInclusao(
        nome: String,
        necessidade: String,
        nivelSuporte: String,
        observacoesPedagogicas: String,
        avatarEmoji: String,
        serieAno: String,
        nivelAutonomiaLeitura: String = "Em processo de alfabetização"
    ): Boolean {
        return saveOrUpdateAlunoInclusao(0, nome, necessidade, nivelSuporte, observacoesPedagogicas, avatarEmoji, serieAno, nivelAutonomiaLeitura)
    }

    fun saveOrUpdateAlunoInclusao(
        id: Int = 0,
        nome: String,
        necessidade: String,
        nivelSuporte: String,
        observacoesPedagogicas: String,
        avatarEmoji: String,
        serieAno: String,
        nivelAutonomiaLeitura: String = "Em processo de alfabetização"
    ): Boolean {
        if (id == 0 && alunosInclusao.value.size >= 9) {
            return false // Max limit reached!
        }
        viewModelScope.launch {
            if (nome.isNotBlank()) {
                val aluno = AlunoNecessidadeEspecial(
                    id = id,
                    nome = nome,
                    necessidade = necessidade,
                    nivelSuporte = nivelSuporte,
                    nivelAutonomiaLeitura = nivelAutonomiaLeitura.ifBlank { "Em processo de alfabetização" },
                    observacoesPedagogicas = observacoesPedagogicas,
                    avatarEmoji = avatarEmoji.ifBlank { "🧩" },
                    serieAno = serieAno.ifBlank { "3º Ano Fundamental" },
                    teacherId = currentUser.value?.uid ?: ""
                )
                repository.insertAlunoInclusao(aluno)
            }
        }
        return true
    }

    fun deleteAlunoInclusao(id: Int) {
        viewModelScope.launch {
            repository.deleteAlunoInclusao(id)
        }
    }

    fun clearAIError() {
        _aiError.value = null
    }

    // 3. PROVAS
    fun toggleQuestionSelection(id: Int, maxLimit: Int = 10): Boolean {
        val currentList = _selectedQuestions.value.toMutableList()
        if (currentList.contains(id)) {
            currentList.remove(id)
            _selectedQuestions.value = currentList
            return true
        } else {
            if (currentList.size >= maxLimit) {
                return false
            }
            currentList.add(id)
            _selectedQuestions.value = currentList
            return true
        }
    }

    fun clearQuestionSelection() {
        _selectedQuestions.value = emptyList()
    }

    fun createProva(titulo: String, descricao: String, turmaId: Int?) {
        viewModelScope.launch {
            if (titulo.isNotBlank() && _selectedQuestions.value.isNotEmpty()) {
                val idsString = _selectedQuestions.value.joinToString(",")
                val newProva = Prova(
                    titulo = titulo,
                    descricao = descricao,
                    turmaId = turmaId,
                    questoesIds = idsString,
                    teacherId = currentUser.value?.uid ?: ""
                )
                repository.insertProva(newProva)
                clearQuestionSelection()
                _currentScreen.value = "provas"
            }
        }
    }

    fun deleteProva(id: Int) {
        viewModelScope.launch {
            repository.deleteProva(id)
            if (_activeProvaForGrades.value?.id == id) {
                _activeProvaForGrades.value = null
                _gradesForActiveProva.value = emptyList()
            }
        }
    }

    fun updateProva(prova: Prova) {
        viewModelScope.launch {
            repository.insertProva(prova)
            if (_activeProvaForGrades.value?.id == prova.id) {
                _activeProvaForGrades.value = prova
            }
        }
    }

    // 4. NOTAS / CORRECAO
    fun selectProvaForGrades(prova: Prova) {
        _activeProvaForGrades.value = prova
        viewModelScope.launch {
            repository.getNotasForProva(prova.id).collect {
                _gradesForActiveProva.value = it
            }
        }
    }

    suspend fun getQuestoesForProva(prova: Prova): List<Questao> {
        if (prova.questoesIds.isBlank()) return emptyList()
        val ids = prova.questoesIds.split(",").mapNotNull { it.toIntOrNull() }
        return repository.getQuestoesByIds(ids)
    }

    fun salvarNotaAluno(
        provaId: Int,
        nomeAluno: String,
        respostasMap: Map<Int, String>, // QuestaoID -> AlunoAnswer
        questoes: List<Questao>
    ) {
        viewModelScope.launch {
            if (nomeAluno.isBlank()) return@launch

            // Calculate grade automatically!
            // Grade is out of 10.0
            var correctCount = 0
            var scorableQuestionsCount = 0

            for (q in questoes) {
                val studentAnswer = respostasMap[q.id]?.trim()?.uppercase()
                val correctAnswer = q.respostaCorreta.trim().uppercase()

                if (q.tipo == "MULTIPLE_CHOICE" || q.tipo == "TRUE_FALSE") {
                    scorableQuestionsCount++
                    if (studentAnswer == correctAnswer) {
                        correctCount++
                    }
                } else {
                    // For discursive, we count it as a scorable question and default to full score,
                    // or teachers can adjust it if they want.
                    // For extreme simplicity, let's assume multiple choice/TF are automatically graded,
                    // and discursive can be marked as "Correto" or "Incorreto" by the teacher.
                    // Let's treat it as scorable if the teacher checked it.
                    scorableQuestionsCount++
                    // Discursive answer in responses map can be "CORRETO" or "INCORRETO"
                    if (studentAnswer == "C" || studentAnswer == "CORRETO") {
                        correctCount++
                    }
                }
            }

            val finalGrade = if (scorableQuestionsCount > 0) {
                (correctCount.toDouble() / scorableQuestionsCount.toDouble()) * 10.0
            } else {
                10.0
            }

            // Serialize answers to String format: "id1:ans1|id2:ans2"
            val respostasString = respostasMap.entries.joinToString("|") { "${it.key}:${it.value}" }

            val notaObj = NotaAluno(
                provaId = provaId,
                nomeAluno = nomeAluno,
                respostas = respostasString,
                nota = finalGrade
            )

            repository.insertNotaAluno(notaObj)

            // Refresh grades
            repository.getNotasForProva(provaId).collect {
                _gradesForActiveProva.value = it
            }
        }
    }

    fun deleteNotaAluno(id: Int, provaId: Int) {
        viewModelScope.launch {
            repository.deleteNotaAluno(id)
            // Refresh grades
            repository.getNotasForProva(provaId).collect {
                _gradesForActiveProva.value = it
            }
        }
    }

    fun loadTeacherProfile() {
        val uid = authRepository.currentUserSession?.uid ?: prefs.getString("cached_teacher_id", null) ?: return
        viewModelScope.launch {
            val result = authRepository.fetchUserProfile(uid)
            result.onSuccess { data ->
                _teacherUserProfile.value = data
                val savedEscola = data["escola_padrao"]
                if (!savedEscola.isNullOrBlank()) {
                    prefs.edit().putString("saved_escola_padrao", savedEscola).apply()
                }
                val savedUser = data["user_name"]
                if (!savedUser.isNullOrBlank()) {
                    prefs.edit().putString("saved_teacher_user_name", savedUser).apply()
                }
            }
        }
    }

    fun updateTeacherProfile(userName: String, escolaPadrao: String, onResult: (Boolean, String) -> Unit) {
        val uid = authRepository.currentUserSession?.uid ?: prefs.getString("cached_teacher_id", null)
        if (uid.isNullOrBlank()) {
            onResult(false, "Usuário não autenticado.")
            return
        }
        viewModelScope.launch {
            val result = authRepository.updateUserProfile(uid, userName, escolaPadrao)
            result.fold(
                onSuccess = {
                    val updated = mapOf("user_name" to userName.trim(), "escola_padrao" to escolaPadrao.trim())
                    _teacherUserProfile.value = updated
                    prefs.edit()
                        .putString("saved_teacher_user_name", userName.trim())
                        .putString("saved_escola_padrao", escolaPadrao.trim())
                        .apply()
                    onResult(true, "Perfil atualizado com sucesso!")
                },
                onFailure = { error ->
                    onResult(false, "Erro ao salvar perfil: ${error.localizedMessage ?: "Verifique sua conexão."}")
                }
            )
        }
    }

    fun appendQuestaoToProva(provaId: Int, questaoId: Int) {
        viewModelScope.launch {
            val prova = repository.getProvaById(provaId) ?: return@launch
            val ids = prova.questoesIds.split(",").mapNotNull { it.toIntOrNull() }.toMutableList()
            if (!ids.contains(questaoId)) {
                ids.add(questaoId)
                val updatedIdsString = ids.joinToString(",")
                val updatedProva = prova.copy(questoesIds = updatedIdsString)
                repository.insertProva(updatedProva)
                if (_activeProvaForGrades.value?.id == provaId) {
                    _activeProvaForGrades.value = updatedProva
                }
            }
        }
    }
}

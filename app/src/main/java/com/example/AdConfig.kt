package com.example.ads

/**
 * Configuração centralizada dos blocos de anúncio do Google AdMob.
 *
 * Permite alternar facilmente entre modo de TESTE (com IDs oficiais da Google)
 * e modo de PRODUÇÃO (com os IDs reais da sua conta AdMob).
 */
object AdConfig {
    // Habilita ou desabilita anúncios globalmente no app
    const val ADS_ENABLED = true

    // Alterne para 'true' somente quando for gerar o pacote final (AAB) para produção
    const val IS_PRODUCTION = false

    // =========================================================================
    // 1. IDs OFICIAIS DE TESTE HOMOLOGADOS PELA GOOGLE (Risco Zero de Bloqueio)
    // =========================================================================
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    // =========================================================================
    // 2. IDs DE PRODUÇÃO (CRIADOS NO SEU PAINEL ADMOB)
    // Preencha aqui com os IDs que você criar em: AdMob -> Aplicativos -> Blocos de anúncios
    // =========================================================================
    var PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-7600465943265150/YYYYYYYYYY"
    var PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7600465943265150/ZZZZZZZZZZ"
    var PROD_BANNER_AD_UNIT_ID = "ca-app-pub-7600465943265150/WWWWWWWWWW"

    // ID do bloco de Recompensado ativo
    val REWARDED_AD_UNIT_ID: String
        get() = if (IS_PRODUCTION) PROD_REWARDED_AD_UNIT_ID else TEST_REWARDED_AD_UNIT_ID

    // ID do bloco de Intersticial ativo
    val INTERSTITIAL_AD_UNIT_ID: String
        get() = if (IS_PRODUCTION) PROD_INTERSTITIAL_AD_UNIT_ID else TEST_INTERSTITIAL_AD_UNIT_ID

    // ID do bloco de Banner ativo
    val BANNER_AD_UNIT_ID: String
        get() = if (IS_PRODUCTION) PROD_BANNER_AD_UNIT_ID else TEST_BANNER_AD_UNIT_ID
}

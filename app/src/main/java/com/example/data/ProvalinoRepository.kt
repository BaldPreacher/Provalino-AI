package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ProvalinoRepository(private val dao: ProvalinoDao) {

    // --- TURMAS ---
    fun getTurmas(teacherId: String): Flow<List<Turma>> = dao.getAllTurmas(teacherId)

    suspend fun insertTurma(turma: Turma): Long {
        return dao.insertTurma(turma)
    }

    suspend fun deleteTurma(id: Int) {
        dao.deleteTurmaById(id)
    }

    // --- QUESTOES ---
    fun getQuestoes(teacherId: String): Flow<List<Questao>> = dao.getAllQuestoes(teacherId)

    suspend fun insertQuestao(questao: Questao): Long {
        return dao.insertQuestao(questao)
    }

    suspend fun deleteQuestao(id: Int) {
        dao.deleteQuestaoById(id)
    }

    suspend fun getQuestoesByIds(ids: List<Int>): List<Questao> {
        return dao.getQuestoesByIds(ids)
    }

    /**
     * Generates questions using the Gemini API and inserts them into the local database automatically
     * to keep them for the teacher. Returns the list of inserted Questao items.
     */
    suspend fun generateAndSaveAIQuestions(
        subject: String,
        grade: String,
        count: Int,
        type: String,
        profile: String = "REGULAR",
        teacherId: String = ""
    ): List<Questao> {
        val aiQuestions = GeminiClient.generateQuestions(subject, grade, count, type, profile)
        val savedQuestions = mutableListOf<Questao>()
        for (aiQ in aiQuestions) {
            val dbQuestao = Questao(
                enunciado = aiQ.enunciado,
                tipo = aiQ.tipo,
                opcaoA = aiQ.opcaoA,
                opcaoB = aiQ.opcaoB,
                opcaoC = aiQ.opcaoC,
                opcaoD = aiQ.opcaoD,
                respostaCorreta = aiQ.respostaCorreta,
                assunto = aiQ.assunto,
                anoEscolar = aiQ.anoEscolar,
                perfilAdaptacao = profile,
                codigoBNCC = aiQ.codigoBNCC,
                pictogramasSuporte = aiQ.pictogramasSuporte,
                teacherId = teacherId
            )
            val generatedId = dao.insertQuestao(dbQuestao)
            savedQuestions.add(dbQuestao.copy(id = generatedId.toInt()))
        }
        return savedQuestions
    }

    /**
     * Adapts an existing question for a specific target profile using AI.
     * Inserts the new adapted question into the database and returns it.
     */
    suspend fun adaptAndSaveQuestion(originalId: Int, targetProfile: String, teacherId: String = ""): Questao? {
        val original = dao.getQuestoesByIds(listOf(originalId)).firstOrNull() ?: return null
        val adaptedAI = GeminiClient.adaptExistingQuestion(
            enunciado = original.enunciado,
            tipo = original.tipo,
            opcaoA = original.opcaoA,
            opcaoB = original.opcaoB,
            opcaoC = original.opcaoC,
            opcaoD = original.opcaoD,
            profile = targetProfile
        ) ?: return null

        val dbQuestao = Questao(
            enunciado = adaptedAI.enunciado,
            tipo = adaptedAI.tipo,
            opcaoA = adaptedAI.opcaoA,
            opcaoB = adaptedAI.opcaoB,
            opcaoC = adaptedAI.opcaoC,
            opcaoD = adaptedAI.opcaoD,
            respostaCorreta = adaptedAI.respostaCorreta,
            assunto = original.assunto,
            anoEscolar = original.anoEscolar,
            perfilAdaptacao = targetProfile,
            codigoBNCC = adaptedAI.codigoBNCC.ifBlank { original.codigoBNCC },
            pictogramasSuporte = adaptedAI.pictogramasSuporte,
            teacherId = teacherId
        )
        val generatedId = dao.insertQuestao(dbQuestao)
        return dbQuestao.copy(id = generatedId.toInt())
    }

    // --- CARTEIRAS ALUNOS INCLUSÃO ---
    fun getAlunosInclusao(teacherId: String): Flow<List<AlunoNecessidadeEspecial>> = dao.getAllAlunosInclusao(teacherId)

    suspend fun insertAlunoInclusao(aluno: AlunoNecessidadeEspecial): Long {
        return dao.insertAlunoInclusao(aluno)
    }

    suspend fun deleteAlunoInclusao(id: Int) {
        dao.deleteAlunoInclusaoById(id)
    }

    // --- PROVAS ---
    fun getProvas(teacherId: String): Flow<List<Prova>> = dao.getAllProvas(teacherId)

    suspend fun getProvaById(id: Int): Prova? {
        return dao.getProvaById(id)
    }

    suspend fun insertProva(prova: Prova): Long {
        return dao.insertProva(prova)
    }

    suspend fun deleteProva(id: Int) {
        dao.deleteProvaById(id)
    }

    // --- NOTAS ALUNOS ---
    fun getNotasForProva(provaId: Int): Flow<List<NotaAluno>> {
        return dao.getNotasForProva(provaId)
    }

    suspend fun insertNotaAluno(notaAluno: NotaAluno): Long {
        return dao.insertNotaAluno(notaAluno)
    }

    suspend fun deleteNotaAluno(id: Int) {
        dao.deleteNotaAlunoById(id)
    }
}

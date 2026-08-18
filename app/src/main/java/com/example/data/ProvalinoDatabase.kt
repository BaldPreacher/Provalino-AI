package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- ENTITIES ---

@Entity(tableName = "turmas")
data class Turma(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val materia: String,
    val teacherId: String = ""
)

@Entity(tableName = "questoes")
data class Questao(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val enunciado: String,
    val tipo: String, // "MULTIPLE_CHOICE" or "TRUE_FALSE" or "DISCURSIVE"
    val opcaoA: String = "",
    val opcaoB: String = "",
    val opcaoC: String = "",
    val opcaoD: String = "",
    val respostaCorreta: String = "", // "A", "B", "C", "D", "V", "F", or sample text
    val assunto: String = "",
    val anoEscolar: String = "",
    val perfilAdaptacao: String = "REGULAR", // "REGULAR", "TEA", "TDAH", "DISLEXIA", "SUPORTE_COGNITIVO"
    val codigoBNCC: String = "", // BNCC skill code e.g. "EF01MA01"
    val pictogramasSuporte: String = "", // Pictograms / emoticons e.g. "🍎 ➕ 🍌"
    val teacherId: String = ""
)

@Entity(tableName = "provas")
data class Prova(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descricao: String = "",
    val turmaId: Int? = null,
    val questoesIds: String = "", // Comma-separated list of Questao IDs, e.g. "1,4,12"
    val dataCriacao: Long = System.currentTimeMillis(),
    val teacherId: String = ""
)

@Entity(tableName = "notas_alunos")
data class NotaAluno(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val provaId: Int,
    val nomeAluno: String,
    val respostas: String = "", // Key-value separated list, e.g. "1:A|2:B|3:V"
    val nota: Double,
    val data: Long = System.currentTimeMillis()
)

@Entity(tableName = "alunos_inclusao")
data class AlunoNecessidadeEspecial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val necessidade: String, // "TEA", "TDAH", "DISLEXIA", "SUPORTE_COGNITIVO", "ACESSIBILIDADE_VISUAL", "ACESSIBILIDADE_LINGUISTICA", "SUPORTE_MULTISSENSORIAL", "ALTAS_HABILIDADES"
    val nivelSuporte: String = "Nível 1 (Leve)",
    val observacoesPedagogicas: String = "",
    val avatarEmoji: String = "🧩",
    val serieAno: String = "3º Ano Fundamental",
    val teacherId: String = ""
)

// --- DAOS ---

@Dao
interface ProvalinoDao {
    // Turmas
    @Query("SELECT * FROM turmas WHERE teacherId = :teacherId ORDER BY nome ASC")
    fun getAllTurmas(teacherId: String): Flow<List<Turma>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurma(turma: Turma): Long

    @Query("DELETE FROM turmas WHERE id = :id")
    suspend fun deleteTurmaById(id: Int)

    // Questoes
    @Query("SELECT * FROM questoes WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getAllQuestoes(teacherId: String): Flow<List<Questao>>

    @Query("SELECT * FROM questoes WHERE id IN (:ids)")
    suspend fun getQuestoesByIds(ids: List<Int>): List<Questao>

    @Query("SELECT * FROM questoes WHERE teacherId = :teacherId AND (assunto LIKE :query OR enunciado LIKE :query)")
    fun searchQuestoes(teacherId: String, query: String): Flow<List<Questao>>

    @Query("SELECT * FROM questoes WHERE (teacherId = :teacherId OR teacherId = '') AND (assunto LIKE '%' || :subject || '%' OR :subject LIKE '%' || assunto || '%' OR enunciado LIKE '%' || :subject || '%') ORDER BY id DESC")
    suspend fun getMatchingQuestionsForFallback(teacherId: String, subject: String): List<Questao>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestao(questao: Questao): Long

    @Query("DELETE FROM questoes WHERE id = :id")
    suspend fun deleteQuestaoById(id: Int)

    // Provas
    @Query("SELECT * FROM provas WHERE teacherId = :teacherId ORDER BY dataCriacao DESC")
    fun getAllProvas(teacherId: String): Flow<List<Prova>>

    @Query("SELECT * FROM provas WHERE id = :id")
    suspend fun getProvaById(id: Int): Prova?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProva(prova: Prova): Long

    @Query("DELETE FROM provas WHERE id = :id")
    suspend fun deleteProvaById(id: Int)

    // Notas Alunos
    @Query("SELECT * FROM notas_alunos WHERE provaId = :provaId ORDER BY nomeAluno ASC")
    fun getNotasForProva(provaId: Int): Flow<List<NotaAluno>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotaAluno(notaAluno: NotaAluno): Long

    @Query("DELETE FROM notas_alunos WHERE id = :id")
    suspend fun deleteNotaAlunoById(id: Int)

    // Carteira Alunos Inclusao
    @Query("SELECT * FROM alunos_inclusao WHERE teacherId = :teacherId ORDER BY nome ASC")
    fun getAllAlunosInclusao(teacherId: String): Flow<List<AlunoNecessidadeEspecial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlunoInclusao(aluno: AlunoNecessidadeEspecial): Long

    @Query("DELETE FROM alunos_inclusao WHERE id = :id")
    suspend fun deleteAlunoInclusaoById(id: Int)
}

// --- DATABASE ---

@Database(
    entities = [Turma::class, Questao::class, Prova::class, NotaAluno::class, AlunoNecessidadeEspecial::class],
    version = 3,
    exportSchema = false
)
abstract class ProvalinoDatabase : RoomDatabase() {
    abstract fun dao(): ProvalinoDao

    companion object {
        @Volatile
        private var INSTANCE: ProvalinoDatabase? = null

        fun getDatabase(context: Context): ProvalinoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProvalinoDatabase::class.java,
                    "provalino_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

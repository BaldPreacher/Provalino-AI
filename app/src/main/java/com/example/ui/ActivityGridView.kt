package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityGridHelper

@Composable
fun WordSearchComposable(
    data: ActivityGridHelper.WordSearchData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "🔍 Caça-Palavras Inclusivo",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF0F766E)
        )

        // Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            data.grid.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    row.forEach { ch ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF64748B), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$ch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }

        // Word Bank
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Palavras para encontrar:",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF334155)
            )
            Text(
                text = data.words.joinToString("  •  "),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                color = Color(0xFF0284C7)
            )
        }
    }
}

@Composable
fun CrosswordComposable(
    data: ActivityGridHelper.CrosswordData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "🧩 Palavras Cruzadas Inclusivas",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF0F766E)
        )

        // Crossword Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (r in 0 until data.size) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (c in 0 until data.size) {
                        val letter = data.grid[r][c]
                        val num = data.cellNumbers[r][c]

                        if (letter != null) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White, RoundedCornerShape(4.dp))
                                    .border(1.2.dp, Color(0xFF1E293B), RoundedCornerShape(4.dp))
                            ) {
                                if (num != null) {
                                    Text(
                                        text = "$num",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier.padding(start = 2.dp, top = 1.dp)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFF334155), RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }

        // Dicas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (data.horizontais.isNotEmpty()) {
                Text("👉 Horizontais:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E293B))
                data.horizontais.forEach {
                    Text("${it.number}. ${it.clue} (${it.word.length} letras)", fontSize = 10.5.sp, color = Color(0xFF334155))
                }
            }
            if (data.verticais.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text("👇 Verticais:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E293B))
                data.verticais.forEach {
                    Text("${it.number}. ${it.clue} (${it.word.length} letras)", fontSize = 10.5.sp, color = Color(0xFF334155))
                }
            }
        }
    }
}

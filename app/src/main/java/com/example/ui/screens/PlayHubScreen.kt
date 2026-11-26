package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlayedGameEntity
import com.example.domain.bot.BotPersona
import com.example.domain.bot.BotRoster
import com.example.domain.bot.PlayProfile
import com.example.ui.components.BotMonogram
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.InkButton
import com.example.ui.components.SectionLabel
import com.example.ui.components.tr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LocalIsEnglish

@Composable
fun PlayHubScreen(
    profile: PlayProfile,
    playedGames: List<PlayedGameEntity>,
    showBackButton: Boolean,
    onStartGame: (botId: String, colorChoice: Int, minutes: Int) -> Unit,
    onSetStartingElo: (Int) -> Unit,
    onAnalyzeGame: (String) -> Unit,
    onDeleteGame: (Long) -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    var selected by remember { mutableStateOf<BotPersona?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showBackButton) {
            GlassTopBar(title = tr("Jouer", "Play"), onBack = onBack)
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!showBackButton) Modifier.statusBarsPadding() else Modifier)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                if (!showBackButton) {
                    Text(
                        text = tr("Jouer", "Play"),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                }
                RatingCard(profile = profile, onSetStartingElo = onSetStartingElo)
            }

            item {
                SectionLabel(tr("Entraînement adaptatif", "Adaptive sparring"), Modifier.padding(top = 10.dp))
            }
            item {
                SparringCard(
                    persona = BotRoster.sparring,
                    elo = profile.botElo(BotRoster.sparring),
                    onClick = { selected = BotRoster.sparring }
                )
            }

            item {
                SectionLabel(tr("L'échelle", "The ladder"), Modifier.padding(top = 10.dp))
            }
            items(BotRoster.ladder, key = { it.id }) { persona ->
                val unlocked = profile.isUnlocked(persona)
                LadderRow(
                    persona = persona,
                    elo = profile.botElo(persona),
                    record = profile.record(persona),
                    unlocked = unlocked,
                    previous = BotRoster.ladder.getOrNull(BotRoster.ladder.indexOf(persona) - 1),
                    onClick = { if (unlocked) selected = persona }
                )
            }

            if (playedGames.isNotEmpty()) {
                item { SectionLabel(tr("Dernières parties", "Recent games"), Modifier.padding(top = 10.dp)) }
                items(playedGames.take(6), key = { it.id }) { g ->
                    RecentGameRow(g, onAnalyzeGame, onDeleteGame)
                }
            }
        }
    }

    selected?.let { persona ->
        NewGameDialog(
            persona = persona,
            elo = profile.botElo(persona),
            onDismiss = { selected = null },
            onStart = { color, minutes ->
                selected = null
                onStartGame(persona.id, color, minutes)
            }
        )
    }
}

@Composable
private fun RatingCard(profile: PlayProfile, onSetStartingElo: (Int) -> Unit) {
    val palette = LocalGlassPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.glassSurface)
            .border(1.dp, palette.paperTaupe.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        SectionLabel(
            if (profile.isProvisional) tr("Ton Elo · provisoire", "Your rating · provisional")
            else tr("Ton Elo", "Your rating")
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = profile.playerElo.toString(),
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 56.sp,
                color = palette.textPrimary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                Text(
                    text = tr("Record ${profile.peakElo}", "Peak ${profile.peakElo}"),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
                Text(
                    text = tr("${profile.ratedGames} parties classées", "${profile.ratedGames} rated games"),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
            }
        }
        if (profile.ratedGames == 0) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = tr(
                    "Ton Elo de départ est une estimation. Choisis le plus proche de toi, il se corrigera tout seul.",
                    "Your starting rating is a guess. Pick the closest, it will correct itself."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassFilterChip(tr("Débutant", "Beginner"), profile.playerElo == 600, { onSetStartingElo(600) })
                GlassFilterChip(tr("Moyen", "Intermediate"), profile.playerElo == 1000, { onSetStartingElo(1000) })
                GlassFilterChip(tr("Avancé", "Advanced"), profile.playerElo == 1400, { onSetStartingElo(1400) })
            }
        }
    }
}

@Composable
private fun SparringCard(persona: BotPersona, elo: Int, onClick: () -> Unit) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.textPrimary)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Sparring · $elo",
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = palette.bgTop
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isEn) persona.bioEn else persona.bioFr,
                style = MaterialTheme.typography.bodySmall,
                color = palette.bgTop.copy(alpha = 0.78f)
            )
        }
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = palette.bgTop
        )
    }
}

@Composable
private fun LadderRow(
    persona: BotPersona,
    elo: Int,
    record: com.example.domain.bot.BotRecord,
    unlocked: Boolean,
    previous: BotPersona?,
    onClick: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    val drift = elo - persona.baseElo
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (unlocked) 1f else 0.5f)
            .clip(RoundedCornerShape(16.dp))
            .background(palette.glassSurface)
            .border(1.dp, palette.paperTaupe.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .clickable(enabled = unlocked, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotMonogram(persona)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isEn) persona.nameEn else persona.nameFr,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            Text(
                text = if (unlocked) {
                    (if (isEn) persona.styleEn else persona.styleFr) +
                        if (record.games > 0) "  ·  ${record.wins}V ${record.draws}N ${record.losses}D" else ""
                } else {
                    tr(
                        "Bats ${previous?.nameFr ?: "le précédent"} pour débloquer",
                        "Beat ${previous?.nameEn ?: "the previous one"} to unlock"
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = elo.toString(),
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = palette.textPrimary
            )
            if (drift != 0 && unlocked) {
                Text(
                    text = if (drift > 0) "▲ $drift" else "▼ ${-drift}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = if (drift > 0) palette.error else palette.success
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = if (unlocked) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.Default.Lock,
            contentDescription = null,
            tint = palette.textSecondary
        )
    }
}

@Composable
private fun RecentGameRow(g: PlayedGameEntity, onAnalyze: (String) -> Unit, onDelete: (Long) -> Unit) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.glassSurface)
            .border(1.dp, palette.paperTaupe.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${g.whiteName} – ${g.blackName}", fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
            Text(
                text = "${g.result}  ·  ${g.moveCount} ${tr("coups", "moves")}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = palette.textSecondary
            )
        }
        IconButton(onClick = { onAnalyze(g.pgn) }) {
            Icon(Icons.Default.Analytics, contentDescription = tr("Analyser", "Analyze"), tint = palette.textPrimary)
        }
        IconButton(onClick = { onDelete(g.id) }) {
            Icon(Icons.Default.Delete, contentDescription = tr("Supprimer", "Delete"), tint = palette.textSecondary)
        }
    }
}

@Composable
private fun NewGameDialog(
    persona: BotPersona,
    elo: Int,
    onDismiss: () -> Unit,
    onStart: (color: Int, minutes: Int) -> Unit
) {
    val palette = LocalGlassPalette.current
    val isEn = LocalIsEnglish.current
    var color by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.glassSurface,
        shape = RoundedCornerShape(22.dp),
        title = {
            Column {
                Text(
                    text = if (isEn) persona.nameEn else persona.nameFr,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Text(
                    text = "Elo $elo · " + if (isEn) persona.styleEn else persona.styleFr,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
            }
        },
        text = {
            Column(modifier = Modifier.widthIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isEn) persona.bioEn else persona.bioFr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary
                )
                SectionLabel(tr("Couleur", "Color"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassFilterChip(tr("Blancs", "White"), color == 0, { color = 0 })
                    GlassFilterChip(tr("Noirs", "Black"), color == 1, { color = 1 })
                    GlassFilterChip(tr("Hasard", "Random"), color == 2, { color = 2 })
                }
                SectionLabel(tr("Cadence", "Time"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassFilterChip("∞", minutes == 0, { minutes = 0 })
                    GlassFilterChip("5", minutes == 5, { minutes = 5 })
                    GlassFilterChip("10", minutes == 10, { minutes = 10 })
                    GlassFilterChip("15", minutes == 15, { minutes = 15 })
                }
                Text(
                    text = tr(
                        "Partie classée : pas d'indice, pas de coup repris. Sinon l'Elo ne bouge pas.",
                        "Rated game: no hints, no takebacks. Otherwise your rating stays put."
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }
        },
        confirmButton = {
            InkButton(text = tr("Jouer", "Play"), onClick = { onStart(color, minutes) })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Annuler", "Cancel"), color = palette.textSecondary)
            }
        }
    )
}

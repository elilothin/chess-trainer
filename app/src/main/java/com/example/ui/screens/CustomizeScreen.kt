package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.chess.ChessEngine
import com.example.domain.model.BoardState
import com.example.domain.model.UserSettings
import com.example.ui.components.ChessBoardView
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.components.GlassTopBar
import com.example.ui.components.LineButton
import com.example.ui.components.SectionLabel
import com.example.ui.components.tr
import com.example.ui.feedback.LocalGameFeedback
import com.example.ui.feedback.MoveKind
import com.example.ui.feedback.SoundPack
import com.example.ui.feedback.Sfx
import com.example.ui.theme.BoardTheme
import com.example.ui.theme.LocalBoardTheme
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.PieceStyle

private const val PREVIEW_FEN = "r1bq1rk1/pp2bppp/2n1pn2/2pp4/3P1B2/2PBPN2/PP1N1PPP/R2QK2R w KQ - 0 9"

@Composable
fun CustomizeScreen(
    settings: UserSettings,
    onBoardTheme: (String) -> Unit,
    onPieceStyle: (String) -> Unit,
    onSoundPack: (String) -> Unit,
    onVolume: (Float) -> Unit,
    onSoundEnabled: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val feedback = LocalGameFeedback.current
    val isEn = com.example.ui.theme.LocalIsEnglish.current
    val board = remember { ChessEngine.fromFen(PREVIEW_FEN) }
    val theme = BoardTheme.fromId(settings.boardTheme)
    val style = PieceStyle.fromId(settings.pieceStyle)
    val pack = SoundPack.fromId(settings.soundPack)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        GlassTopBar(
            title = tr("Personnaliser", "Customize"),
            subtitle = tr("Plateau, pièces, sons", "Board, pieces, sounds"),
            onBack = onBack
        )
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChessBoardView(board = board, showCoordinates = true)

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionLabel(tr("Plateau", "Board"))
                Spacer(Modifier.height(10.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BoardTheme.values().forEach { t ->
                        ThemeSwatch(t, selected = t == theme, label = if (isEn) t.en else t.fr) { onBoardTheme(t.id) }
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionLabel(tr("Pièces", "Pieces"))
                Spacer(Modifier.height(10.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PieceStyle.values().forEach { st ->
                        GlassFilterChip(label = if (isEn) st.en else st.fr, selected = st == style, onClick = { onPieceStyle(st.id) })
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SectionLabel(tr("Sons", "Sounds"))
                Spacer(Modifier.height(10.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoundPack.values().forEach { p ->
                        GlassFilterChip(
                            label = if (isEn) p.en else p.fr,
                            selected = p == pack,
                            onClick = {
                                onSoundPack(p.id)
                                feedback?.setPack(p)
                                feedback?.play(Sfx.CAPTURE)
                            }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(tr("Volume", "Volume"), color = palette.textPrimary, modifier = Modifier.padding(end = 12.dp))
                    Slider(
                        value = settings.soundVolume,
                        onValueChange = { onVolume(it); feedback?.volume = it },
                        onValueChangeFinished = { feedback?.play(Sfx.MOVE) },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${(settings.soundVolume * 100).toInt()}%", color = palette.textSecondary, modifier = Modifier.padding(start = 10.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tr("Sons activés", "Sounds on"), color = palette.textPrimary)
                    Switch(checked = settings.soundEnabled, onCheckedChange = onSoundEnabled)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tr("Vibrations", "Vibrations"), color = palette.textPrimary)
                    Switch(checked = settings.hapticsEnabled, onCheckedChange = onHaptics)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    LineButton(text = tr("Écouter un coup", "Hear a move"), onClick = { feedback?.onMove(MoveKind.QUIET) }, modifier = Modifier.weight(1f))
                    LineButton(text = tr("Écouter un échec", "Hear a check"), onClick = { feedback?.onMove(MoveKind.CHECK) }, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ThemeSwatch(theme: BoardTheme, selected: Boolean, label: String, onClick: () -> Unit) {
    val palette = LocalGlassPalette.current
    val light = theme.light ?: palette.boardLightSquare
    val dark = theme.dark ?: palette.boardDarkSquare
    val shape = RoundedCornerShape(12.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(shape)
                .border(if (selected) 2.dp else 1.dp, if (selected) palette.textPrimary else palette.paperTaupe, shape)
                .androidClickable(onClick)
        ) {
            Column {
                for (r in 0..1) Row(Modifier.weight(1f)) {
                    for (c in 0..1) Box(Modifier.weight(1f).fillMaxWidth().height(29.dp).background(if ((r + c) % 2 == 0) light else dark))
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = if (selected) palette.textPrimary else palette.textSecondary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
    }
}

private fun Modifier.androidClickable(onClick: () -> Unit): Modifier = this.then(Modifier.clickable(onClick = onClick))

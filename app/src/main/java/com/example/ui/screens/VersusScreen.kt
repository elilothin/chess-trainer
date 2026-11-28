package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.bot.Advantage
import com.example.domain.bot.PositionEvaluator
import com.example.domain.chess.rememberChessEngine
import com.example.domain.model.GameOutcome
import com.example.domain.model.PieceColor
import com.example.domain.online.HttpRoomTransport
import com.example.domain.online.JoinResult
import com.example.domain.online.OnlineRoom
import com.example.ui.components.AdvantageBadge
import com.example.ui.components.BoardWithGauge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTopBar
import com.example.ui.components.InkButton
import com.example.ui.components.KChessBoard
import com.example.ui.components.LineButton
import com.example.ui.components.SectionLabel
import com.example.ui.components.tr
import com.example.ui.feedback.LocalGameFeedback
import com.example.ui.feedback.Sfx
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalGlassPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private enum class Phase { SETUP, LOBBY, WAITING, PLAYING }

/**
 * Duel entre deux humains : [online] = false pour deux joueurs sur le même téléphone,
 * true pour une salle en ligne identifiée par un code à 5 caractères.
 */
@Composable
fun VersusScreen(
    online: Boolean,
    dbUrl: String,
    showCoordinates: Boolean,
    onSaveDbUrl: (String) -> Unit,
    onExit: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val scope = rememberCoroutineScope()
    val feedback = LocalGameFeedback.current
    val engine = rememberChessEngine()
    val engineState by engine.state.collectAsStateWithLifecycle()
    val board = engineState.board
    val n = engineState.moveHistoryUci.size

    var phase by remember { mutableStateOf(if (!online) Phase.PLAYING else if (OnlineRoom.normalizeDbUrl(dbUrl) == null) Phase.SETUP else Phase.LOBBY) }
    var result by remember { mutableStateOf<String?>(null) }
    var advantage by remember { mutableStateOf<Advantage?>(null) }
    var autoFlip by rememberSaveable { mutableStateOf(false) }
    var manualFlip by rememberSaveable { mutableStateOf(false) }
    var showGauge by rememberSaveable { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // --- en ligne ---
    val uid = remember { UUID.randomUUID().toString().take(8) }
    var code by rememberSaveable { mutableStateOf("") }
    var codeInput by rememberSaveable { mutableStateOf("") }
    var urlInput by rememberSaveable { mutableStateOf(dbUrl) }
    var myColor by remember { mutableStateOf(PieceColor.WHITE) }
    val room = remember(dbUrl) {
        OnlineRoom(HttpRoomTransport(OnlineRoom.normalizeDbUrl(dbUrl) ?: "https://invalid.example"))
    }

    fun endGame(res: String, publish: Boolean) {
        if (result != null) return
        result = res
        val score = when (res) {
            "1-0" -> if (!online || myColor == PieceColor.WHITE) 1.0 else 0.0
            "0-1" -> if (!online || myColor == PieceColor.BLACK) 1.0 else 0.0
            else -> 0.5
        }
        feedback?.let { fb ->
            scope.launch {
                delay(450)
                fb.onGameOver(if (!online) Sfx.WIN else if (score > 0.75) Sfx.WIN else if (score < 0.25) Sfx.LOSE else Sfx.DRAW)
            }
        }
        if (online && publish) scope.launch { runCatching { room.finish(code, res) } }
    }

    LaunchedEffect(engineState.fen) {
        val fen = engineState.fen
        advantage = withContext(Dispatchers.Default) { PositionEvaluator.evaluate(fen) }
    }

    LaunchedEffect(engineState.fen, phase) {
        if (phase != Phase.PLAYING || result != null) return@LaunchedEffect
        val out = engineState.outcome
        if (out != GameOutcome.ONGOING) {
            endGame(
                when (out) {
                    GameOutcome.CHECKMATE_WHITE_WINS -> "1-0"
                    GameOutcome.CHECKMATE_BLACK_WINS -> "0-1"
                    else -> "1/2-1/2"
                },
                publish = myColor == PieceColor.WHITE
            )
        }
    }

    // Sondage de la salle : arrivée de l'adversaire, coups reçus, abandon
    LaunchedEffect(phase, code) {
        if (!online || (phase != Phase.WAITING && phase != Phase.PLAYING) || code.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(1000)
            val snap = runCatching { room.poll(code) }.getOrNull()
            if (snap == null) { error = tr("Connexion instable… nouvelle tentative", "Unstable connection… retrying"); continue }
            error = null
            if (phase == Phase.WAITING && snap.black.isNotEmpty()) phase = Phase.PLAYING
            if (phase == Phase.PLAYING) {
                val have = engine.state.value.moveHistoryUci.size
                if (snap.moves.size > have) {
                    for (i in have until snap.moves.size) engine.playUci(snap.moves[i])
                }
                if (snap.result.isNotEmpty() && result == null) endGame(snap.result, publish = false)
            }
        }
    }

    val flipped = when {
        online -> myColor == PieceColor.BLACK
        autoFlip -> board.turn == PieceColor.BLACK
        else -> manualFlip
    }
    val myTurn = !online || board.turn == myColor
    val interactive = phase == Phase.PLAYING && result == null && myTurn

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        GlassTopBar(
            title = if (online) tr("Duel en ligne", "Online duel") else tr("Deux joueurs", "Two players"),
            subtitle = if (online) tr("Salle par code", "Room code") else tr("Sur le même téléphone", "Same phone"),
            onBack = onExit
        )
        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (phase) {
                Phase.SETUP -> GlassCard(modifier = Modifier.fillMaxWidth()) {
                    SectionLabel(tr("Configuration (une seule fois)", "Setup (one time)"))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        tr(
                            "Le jeu en ligne a besoin d'une petite base de données gratuite :\n1. Crée un projet sur console.firebase.google.com\n2. Ajoute une « Realtime Database » en mode test\n3. Colle ci-dessous son adresse (https://…firebaseio.com)",
                            "Online play needs a small free database:\n1. Create a project at console.firebase.google.com\n2. Add a Realtime Database in test mode\n3. Paste its address below (https://…firebaseio.com)"
                        ),
                        color = palette.textPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("URL") }
                    )
                    Spacer(Modifier.height(10.dp))
                    InkButton(
                        text = tr("Enregistrer", "Save"),
                        onClick = {
                            val ok = OnlineRoom.normalizeDbUrl(urlInput)
                            if (ok == null) error = tr("Adresse invalide : elle doit commencer par https://", "Invalid address: must start with https://")
                            else { error = null; onSaveDbUrl(ok); phase = Phase.LOBBY }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Phase.LOBBY -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        SectionLabel(tr("Créer une salle", "Create a room"))
                        Spacer(Modifier.height(6.dp))
                        Text(tr("Tu joues les Blancs. Donne le code à ton adversaire.", "You play White. Give the code to your opponent."), color = palette.textSecondary)
                        Spacer(Modifier.height(10.dp))
                        InkButton(
                            text = tr("Créer", "Create"),
                            onClick = {
                                scope.launch {
                                    val c = OnlineRoom.newCode()
                                    runCatching { room.create(c, uid) }
                                        .onSuccess { code = c; myColor = PieceColor.WHITE; engine.reset(); result = null; error = null; phase = Phase.WAITING }
                                        .onFailure { error = tr("Impossible de joindre la base. Vérifie l'adresse et ta connexion.", "Can't reach the database. Check the address and your connection.") }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        SectionLabel(tr("Rejoindre une salle", "Join a room"))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = codeInput,
                            onValueChange = { codeInput = OnlineRoom.normalizeCode(it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text(tr("Code à 5 caractères", "5-character code")) }
                        )
                        Spacer(Modifier.height(10.dp))
                        LineButton(
                            text = tr("Rejoindre", "Join"),
                            enabled = codeInput.length == 5,
                            onClick = {
                                scope.launch {
                                    runCatching { room.join(codeInput, uid) }
                                        .onSuccess { r ->
                                            when (r) {
                                                JoinResult.OK -> { code = codeInput; myColor = PieceColor.BLACK; engine.reset(); result = null; error = null; phase = Phase.PLAYING }
                                                JoinResult.NOT_FOUND -> error = tr("Salle introuvable.", "Room not found.")
                                                JoinResult.FULL -> error = tr("Cette salle est déjà complète.", "This room is already full.")
                                            }
                                        }
                                        .onFailure { error = tr("Impossible de joindre la base.", "Can't reach the database.") }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    LineButton(
                        text = tr("Changer l'adresse de la base", "Change database address"),
                        onClick = { phase = Phase.SETUP },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Phase.WAITING -> {
                    val clipboard = LocalClipboardManager.current
                    GlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                        SectionLabel(tr("Code de la salle", "Room code"))
                        Spacer(Modifier.height(6.dp))
                        Text(code, fontFamily = JetBrainsMonoFontFamily, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary, letterSpacing = 6.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(tr("En attente de ton adversaire…", "Waiting for your opponent…"), color = palette.textSecondary)
                        Spacer(Modifier.height(10.dp))
                        LineButton(text = tr("Copier le code", "Copy code"), onClick = { clipboard.setText(AnnotatedString(code)) }, modifier = Modifier.fillMaxWidth())
                    }
                }

                Phase.PLAYING -> {
                    TurnStrip(
                        text = when {
                            result != null -> tr("Partie terminée", "Game over")
                            online && !myTurn -> tr("L'adversaire réfléchit…", "Opponent is thinking…")
                            board.turn == PieceColor.WHITE -> tr("Trait aux Blancs", "White to move")
                            else -> tr("Trait aux Noirs", "Black to move")
                        },
                        advantage = advantage
                    )
                    val boardContent: @Composable () -> Unit = {
                        KChessBoard(
                            engine = engine,
                            flipped = flipped,
                            showCoordinates = showCoordinates,
                            interactive = interactive,
                            showStatusHeader = false,
                            onMovePlayed = { _, st ->
                                if (online) scope.launch { runCatching { room.pushMoves(code, st.moveHistoryUci) } }
                            }
                        )
                    }
                    if (showGauge) BoardWithGauge(advantage = advantage, flipped = flipped) { boardContent() } else boardContent()

                    if (result == null) {
                        if (!online) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                ToggleRow(tr("Pivoter le plateau à chaque coup", "Rotate board every move"), autoFlip) { autoFlip = it }
                                ToggleRow(tr("Afficher la jauge d'avantage", "Show advantage gauge"), showGauge) { showGauge = it }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                LineButton(text = tr("Retourner", "Flip"), onClick = { manualFlip = !manualFlip }, modifier = Modifier.weight(1f))
                                LineButton(text = tr("Annuler le coup", "Undo move"), enabled = n > 0, onClick = { engine.undoMove() }, modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                LineButton(text = tr("Nulle", "Draw"), onClick = { endGame("1/2-1/2", false) }, modifier = Modifier.weight(1f))
                                LineButton(
                                    text = if (board.turn == PieceColor.WHITE) tr("Les Blancs abandonnent", "White resigns") else tr("Les Noirs abandonnent", "Black resigns"),
                                    onClick = { endGame(if (board.turn == PieceColor.WHITE) "0-1" else "1-0", false) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            LineButton(
                                text = tr("Abandonner", "Resign"),
                                onClick = { endGame(if (myColor == PieceColor.WHITE) "0-1" else "1-0", true) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        GlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                            SectionLabel(tr("Partie terminée · $result", "Game over · $result"))
                            Spacer(Modifier.height(4.dp))
                            val msg = when (result) {
                                "1-0" -> if (online) (if (myColor == PieceColor.WHITE) tr("Tu as gagné", "You won") else tr("Tu as perdu", "You lost")) else tr("Les Blancs gagnent", "White wins")
                                "0-1" -> if (online) (if (myColor == PieceColor.BLACK) tr("Tu as gagné", "You won") else tr("Tu as perdu", "You lost")) else tr("Les Noirs gagnent", "Black wins")
                                else -> tr("Match nul", "Draw")
                            }
                            Text(msg, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                            Spacer(Modifier.height(10.dp))
                            if (!online) {
                                InkButton(text = tr("Rejouer", "Play again"), onClick = { engine.reset(); result = null }, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(8.dp))
                            }
                            LineButton(text = tr("Quitter", "Leave"), onClick = onExit, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            error?.let { Text(it, color = palette.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TurnStrip(text: String, advantage: Advantage?) {
    val palette = LocalGlassPalette.current
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text, color = palette.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            AdvantageBadge(advantage)
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = palette.textPrimary, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

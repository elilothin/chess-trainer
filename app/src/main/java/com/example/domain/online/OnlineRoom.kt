package com.example.domain.online

import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

/** Transport minimal vers une base type Firebase Realtime Database (API REST, JSON). */
interface RoomTransport {
    /** Corps JSON brut du nœud, ou null s'il n'existe pas. */
    suspend fun get(path: String): String?
    suspend fun put(path: String, json: String)
}

/** JSON plat {"clé":"valeur"} : tout ce dont ce protocole a besoin, sans dépendance. */
object FlatJson {
    fun quote(s: String): String {
        val sb = StringBuilder("\"")
        for (c in s) when (c) {
            '"' -> sb.append("\\\"")
            '\\' -> sb.append("\\\\")
            '\n' -> sb.append("\\n")
            else -> sb.append(c)
        }
        return sb.append('"').toString()
    }

    fun encode(map: Map<String, String>): String =
        map.entries.joinToString(",", "{", "}") { quote(it.key) + ":" + quote(it.value) }

    /** Décode un objet plat ; accepte chaînes, nombres, booléens et null (null devient ""). */
    fun decode(json: String?): Map<String, String>? {
        if (json == null) return null
        val s = json.trim()
        if (s == "null" || s.isEmpty() || s[0] != '{') return null
        val out = LinkedHashMap<String, String>()
        var i = 1
        fun skipWs() { while (i < s.length && s[i].isWhitespace()) i++ }
        fun readString(): String {
            val sb = StringBuilder()
            i++ // guillemet ouvrant
            while (i < s.length && s[i] != '"') {
                if (s[i] == '\\' && i + 1 < s.length) {
                    i++
                    sb.append(when (s[i]) { 'n' -> '\n'; 't' -> '\t'; else -> s[i] })
                } else sb.append(s[i])
                i++
            }
            i++ // guillemet fermant
            return sb.toString()
        }
        while (i < s.length) {
            skipWs()
            if (i >= s.length || s[i] == '}') break
            if (s[i] == ',') { i++; continue }
            if (s[i] != '"') return out
            val key = readString()
            skipWs()
            if (i < s.length && s[i] == ':') i++
            skipWs()
            if (i >= s.length) break
            if (s[i] == '"') out[key] = readString()
            else {
                val start = i
                while (i < s.length && s[i] != ',' && s[i] != '}') i++
                val raw = s.substring(start, i).trim()
                out[key] = if (raw == "null") "" else raw
            }
        }
        return out
    }
}

data class RoomSnapshot(
    val white: String,
    val black: String,
    val moves: List<String>,
    val status: String,
    val result: String
)

enum class JoinResult { OK, NOT_FOUND, FULL }

/**
 * Salle de jeu en ligne : un nœud `rooms/CODE` contenant les joueurs, la liste des coups (UCI séparés
 * par des espaces), l'état et le résultat. Chaque champ est écrit séparément (PUT) pour rester
 * compatible avec n'importe quel client HTTP.
 */
class OnlineRoom(private val transport: RoomTransport) {

    suspend fun create(code: String, uid: String) {
        val body = FlatJson.encode(
            mapOf("white" to uid, "black" to "", "moves" to "", "status" to "waiting", "result" to "")
        )
        transport.put("rooms/$code", body)
    }

    suspend fun join(code: String, uid: String): JoinResult {
        val room = FlatJson.decode(transport.get("rooms/$code")) ?: return JoinResult.NOT_FOUND
        val black = room["black"].orEmpty()
        if (black.isNotEmpty() && black != uid) return JoinResult.FULL
        transport.put("rooms/$code/black", FlatJson.quote(uid))
        transport.put("rooms/$code/status", FlatJson.quote("playing"))
        return JoinResult.OK
    }

    suspend fun poll(code: String): RoomSnapshot? {
        val room = FlatJson.decode(transport.get("rooms/$code")) ?: return null
        return RoomSnapshot(
            white = room["white"].orEmpty(),
            black = room["black"].orEmpty(),
            moves = room["moves"].orEmpty().split(' ').filter { it.isNotBlank() },
            status = room["status"].orEmpty(),
            result = room["result"].orEmpty()
        )
    }

    suspend fun pushMoves(code: String, moves: List<String>) {
        transport.put("rooms/$code/moves", FlatJson.quote(moves.joinToString(" ")))
    }

    suspend fun finish(code: String, result: String) {
        transport.put("rooms/$code/result", FlatJson.quote(result))
        transport.put("rooms/$code/status", FlatJson.quote("over"))
    }

    companion object {
        private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        fun newCode(rnd: Random = Random.Default): String =
            (1..5).map { ALPHABET[rnd.nextInt(ALPHABET.length)] }.joinToString("")

        fun normalizeCode(raw: String): String =
            raw.trim().uppercase().filter { it in ALPHABET }.take(5)

        /** Accepte "https://xxx.firebaseio.com/" ou sans slash final ; refuse le reste. */
        fun normalizeDbUrl(raw: String): String? {
            val u = raw.trim().trimEnd('/')
            return if (u.startsWith("https://") && u.length > 12 && !u.contains(' ')) u else null
        }
    }
}

/** Transport HTTP réel (Firebase Realtime Database en mode REST, règles de lecture/écriture ouvertes). */
class HttpRoomTransport(baseUrl: String) : RoomTransport {
    private val base = baseUrl.trimEnd('/')

    override suspend fun get(path: String): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val c = open("$base/$path.json")
        try {
            c.requestMethod = "GET"
            if (c.responseCode !in 200..299) error("HTTP ${c.responseCode}")
            c.inputStream.bufferedReader().use { it.readText() }
        } finally { c.disconnect() }
    }

    override suspend fun put(path: String, json: String) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val c = open("$base/$path.json")
            try {
                c.requestMethod = "PUT"
                c.doOutput = true
                c.setRequestProperty("Content-Type", "application/json")
                c.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                if (c.responseCode !in 200..299) error("HTTP ${c.responseCode}")
                c.inputStream.use { it.readBytes() }
            } finally { c.disconnect() }
        }
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 6000
            readTimeout = 6000
        }
}

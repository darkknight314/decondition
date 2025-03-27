package com.social.media.decondition

enum class ContentType {
    APP,
    DOMAIN
}


class BlockingStrategy(private val preferencesManager: PreferencesManager) {
    private fun createPreferenceFlag(prefix: String, contentType: ContentType, identifier: String): String {
        return listOf(prefix, contentType.toString(), identifier).joinToString("_")
    }

    fun shouldShowPuzzle(contentType: ContentType, identifier: String): Boolean {
        if (!isMonitored(identifier)) return false
        val puzzleSolvedFlag = createPreferenceFlag("PUZZLE_SOLVED", contentType, identifier)
        val sessionActiveFlag = createPreferenceFlag("SESSION_ACTIVE", contentType, identifier)

        val puzzleSolved = preferencesManager.getBoolean(puzzleSolvedFlag, false)
        val sessionActive = preferencesManager.getBoolean(sessionActiveFlag, false)

        return !(puzzleSolved && sessionActive)
    }

    private fun markPreferenceTrue(prefix: String, contentType: ContentType, identifier: String) {
        val flag = createPreferenceFlag(prefix, contentType, identifier)
        preferencesManager.putBoolean(flag, true)
    }

    fun markPuzzleSolved(contentType: ContentType, identifier: String) {
        markPreferenceTrue("PUZZLE_SOLVED", contentType, identifier)
    }

    fun markSessionActive(contentType: ContentType, identifier: String) {
        markPreferenceTrue("SESSION_ACTIVE", contentType, identifier)
    }

    fun isMonitored(identifier: String): Boolean {
        if (identifier == "com.social.media.decondition") return false

        val monitoredDomainFlag = preferencesManager.getStringSet("monitoredDomains").any {it: String-> it.contains(identifier, ignoreCase = true) }
        val monitoredAppFlag = preferencesManager.getStringSet("selectedApps").any {it: String-> it.contains(identifier, ignoreCase = true) }
        return monitoredDomainFlag || monitoredAppFlag
    }
}

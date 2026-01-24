package com.elseeker.game.adapter.input.api.request

import java.time.Instant

data class BibleTypingSessionEndRequest(
    val endedAt: Instant
)

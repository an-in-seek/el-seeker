package com.elseeker.game.domain.event

import com.elseeker.game.domain.vo.GameType

data class GameCompletedEvent(
    val memberId: Long,
    val gameType: GameType
)

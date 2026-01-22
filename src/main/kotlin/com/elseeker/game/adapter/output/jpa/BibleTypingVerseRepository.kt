package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.BibleTypingVerse
import com.elseeker.game.domain.model.BibleTypingVerseId
import org.springframework.data.jpa.repository.JpaRepository

interface BibleTypingVerseRepository : JpaRepository<BibleTypingVerse, BibleTypingVerseId>

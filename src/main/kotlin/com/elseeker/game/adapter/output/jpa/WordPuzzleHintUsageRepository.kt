package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.WordPuzzleHintUsage
import org.springframework.data.jpa.repository.JpaRepository

interface WordPuzzleHintUsageRepository : JpaRepository<WordPuzzleHintUsage, Long>

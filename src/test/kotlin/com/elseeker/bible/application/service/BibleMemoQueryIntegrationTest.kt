package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleBookMemoRepository
import com.elseeker.bible.adapter.output.jpa.BibleBookRepository
import com.elseeker.bible.adapter.output.jpa.BibleChapterMemoRepository
import com.elseeker.bible.adapter.output.jpa.BibleMemoRepository
import com.elseeker.bible.adapter.output.jpa.BibleTranslationRepository
import com.elseeker.bible.domain.model.BibleBook
import com.elseeker.bible.domain.model.BibleBookMemo
import com.elseeker.bible.domain.model.BibleChapterMemo
import com.elseeker.bible.domain.model.BibleTranslation
import com.elseeker.bible.domain.model.BibleVerseMemo
import com.elseeker.bible.domain.vo.BibleBookKey
import com.elseeker.bible.domain.vo.BibleTestamentType
import com.elseeker.bible.domain.vo.BibleTranslationType
import com.elseeker.common.IntegrationTest
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.neovisionaries.i18n.LanguageCode
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

@DisplayName("Bible Memo 조회 통합테스트")
class BibleMemoQueryIntegrationTest @Autowired constructor(
    private val bibleMemoService: BibleMemoService,
    private val bibleChapterMemoService: BibleChapterMemoService,
    private val bibleBookMemoService: BibleBookMemoService,
    private val bibleTranslationRepository: BibleTranslationRepository,
    private val bibleBookRepository: BibleBookRepository,
    private val bibleMemoRepository: BibleMemoRepository,
    private val bibleChapterMemoRepository: BibleChapterMemoRepository,
    private val bibleBookMemoRepository: BibleBookMemoRepository,
    private val memberRepository: MemberRepository,
) : IntegrationTest() {

    @Test
    fun `구절 메모 목록은 책 이름을 조인으로 함께 조회하고 totalCount를 유지한다`() {
        val fixture = createBibleFixture()

        bibleMemoRepository.save(
            BibleVerseMemo(
                member = member,
                translationId = fixture.translationId,
                bookOrder = 1,
                chapterNumber = 1,
                verseNumber = 1,
                content = "창세기 메모"
            )
        )
        bibleMemoRepository.save(
            BibleVerseMemo(
                member = member,
                translationId = fixture.translationId,
                bookOrder = 2,
                chapterNumber = 2,
                verseNumber = 3,
                content = "출애굽기 메모"
            )
        )

        val firstPage = bibleMemoService.getMyMemos(
            memberUid = member.uid,
            pageable = PageRequest.of(0, 1),
            translationId = fixture.translationId
        )
        val filtered = bibleMemoService.getMyMemos(
            memberUid = member.uid,
            pageable = PageRequest.of(0, 10),
            translationId = fixture.translationId,
            bookOrder = 2
        )

        firstPage.totalCount shouldBe 2
        firstPage.hasNext shouldBe true
        firstPage.content shouldHaveSize 1
        setOf("창세기", "출애굽기").contains(firstPage.content.first().bookName) shouldBe true
        filtered.content.single().bookName shouldBe "출애굽기"
    }

    @Test
    fun `장 메모 목록은 책 이름을 추가 조회하지 않고 반환한다`() {
        val fixture = createBibleFixture()

        bibleChapterMemoRepository.save(
            BibleChapterMemo(
                member = member,
                translationId = fixture.translationId,
                bookOrder = 1,
                chapterNumber = 3,
                content = "장 메모"
            )
        )

        val result = bibleChapterMemoService.getMyChapterMemos(
            memberUid = member.uid,
            pageable = PageRequest.of(0, 10),
            translationId = fixture.translationId
        )

        result.content shouldHaveSize 1
        result.content.single().bookName shouldBe "창세기"
        result.totalCount shouldBe 1
    }

    @Test
    fun `책 메모 목록은 책 이름을 조인 조회한다`() {
        val fixture = createBibleFixture()

        bibleBookMemoRepository.save(
            BibleBookMemo(
                member = member,
                translationId = fixture.translationId,
                bookOrder = 2,
                content = "책 메모"
            )
        )

        val result = bibleBookMemoService.getMyBookMemos(
            memberUid = member.uid,
            pageable = PageRequest.of(0, 10),
            translationId = fixture.translationId
        )

        result.content shouldHaveSize 1
        result.content.single().bookName shouldBe "출애굽기"
        result.totalCount shouldBe 1
    }

    @Test
    fun `메모 개수 집계는 단일 쿼리로 세 탭 개수를 함께 반환한다`() {
        val fixture = createBibleFixture()

        bibleBookMemoRepository.save(
            BibleBookMemo(
                member = member,
                translationId = fixture.translationId,
                bookOrder = 1,
                content = "책 메모"
            )
        )
        bibleChapterMemoRepository.save(
            BibleChapterMemo(
                member = member,
                translationId = fixture.translationId,
                bookOrder = 1,
                chapterNumber = 1,
                content = "장 메모 1"
            )
        )
        bibleChapterMemoRepository.save(
            BibleChapterMemo(
                member = member,
                translationId = fixture.translationId,
                bookOrder = 2,
                chapterNumber = 1,
                content = "장 메모 2"
            )
        )
        bibleMemoRepository.save(
            BibleVerseMemo(
                member = member,
                translationId = fixture.translationId,
                bookOrder = 1,
                chapterNumber = 1,
                verseNumber = 1,
                content = "절 메모 1"
            )
        )
        bibleMemoRepository.save(
            BibleVerseMemo(
                member = member,
                translationId = fixture.translationId,
                bookOrder = 1,
                chapterNumber = 1,
                verseNumber = 2,
                content = "절 메모 2"
            )
        )
        bibleMemoRepository.save(
            BibleVerseMemo(
                member = member,
                translationId = fixture.translationId,
                bookOrder = 2,
                chapterNumber = 1,
                verseNumber = 1,
                content = "절 메모 3"
            )
        )

        val counts = memberRepository.findMemoCountsByUid(member.uid)

        counts?.book shouldBe 1
        counts?.chapter shouldBe 2
        counts?.verse shouldBe 3
    }

    private fun createBibleFixture(): BibleFixture {
        val translation = bibleTranslationRepository.save(
            BibleTranslation(
                translationType = BibleTranslationType.KRV,
                name = "테스트 번역본",
                translationOrder = 1,
                languageCode = LanguageCode.ko
            )
        )

        bibleBookRepository.saveAll(
            listOf(
                BibleBook(
                    translationId = translation.id!!,
                    bookKey = BibleBookKey.GEN,
                    bookOrder = 1,
                    name = "창세기",
                    abbreviation = "창",
                    testamentType = BibleTestamentType.OLD
                ),
                BibleBook(
                    translationId = translation.id!!,
                    bookKey = BibleBookKey.EXO,
                    bookOrder = 2,
                    name = "출애굽기",
                    abbreviation = "출",
                    testamentType = BibleTestamentType.OLD
                )
            )
        )

        return BibleFixture(translationId = translation.id!!)
    }

    private data class BibleFixture(
        val translationId: Long
    )
}

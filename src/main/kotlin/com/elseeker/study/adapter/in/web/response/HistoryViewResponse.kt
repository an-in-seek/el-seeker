package com.elseeker.study.adapter.`in`.web.response

data class EraSummary(
    val slug: String,
    val label: String,
    val period: String,
    val keywords: List<String>
)

data class BookLink(
    val label: String,
    val url: String
)

data class BookGroup(
    val groupName: String,
    val books: List<BookLink>
)

data class HistoryEventSummary(
    val id: String,
    val eraSlug: String,
    val title: String,
    val description: String,
    val scriptureRange: String
)

data class HistoryEventDetail(
    val id: String,
    val eraSlug: String,
    val eraLabel: String,
    val title: String,
    val timeline: String,
    val summary: String,
    val background: String,
    val references: List<HistoryReference>
)

data class HistoryReference(
    val label: String,
    val url: String
)

data class EraTimelineBlock(
    val era: EraSummary,
    val eventHighlights: List<HistoryEventSummary>,
    val bookGroups: List<BookGroup>
)

object HistoryDummyData {
    val eras: List<EraSummary> = listOf(
        EraSummary("patriarchs", "창조와 족장 시대", "연대 미상", listOf("창조", "언약")),
        EraSummary("exodus", "출애굽과 광야", "B.C 1500-1300", listOf("해방", "광야")),
        EraSummary("conquest", "가나안 정복", "B.C 1400-1200", listOf("여호수아", "정복")),
        EraSummary("judges", "사사 시대", "B.C 1200-1050", listOf("사사", "순환")),
        EraSummary("united-kingdom", "통일 왕국 시대", "B.C 1050-930", listOf("다윗", "솔로몬")),
        EraSummary("divided-kingdom", "분열 왕국 시대", "B.C 930-586", listOf("북이스라엘", "남유다")),
        EraSummary("exile", "바벨론 포로", "B.C 586-539", listOf("바벨론", "포로")),
        EraSummary("return", "귀환과 재건", "B.C 538-430", listOf("예루살렘", "성전")),
        EraSummary("intertestamental", "중간사 시대", "B.C 400-4", listOf("헬라", "로마")),
        EraSummary("jesus", "예수 시대", "B.C 4-A.D 30", listOf("복음", "구원")),
        EraSummary("early-church", "초대 교회", "A.D 33-100", listOf("사도", "선교"))
    )


    val bookCategories: List<String> = listOf(
        "율법서",
        "역사서",
        "시가서",
        "예언서",
        "복음서",
        "서신서",
        "배경"
    )

    private val eraLabelBySlug = eras.associate { it.slug to it.label }
    private fun bookLink(label: String) = BookLink(label, "#")
    private val eraBookGroups: Map<String, List<BookGroup>> = mapOf(
        "patriarchs" to listOf(
            BookGroup("율법서", listOf(bookLink("창세기"))),
            BookGroup("시가서", listOf(bookLink("욥기")))
        ),
        "exodus" to listOf(
            BookGroup("율법서", listOf(bookLink("출애굽기"), bookLink("레위기"), bookLink("민수기"), bookLink("신명기")))
        ),
        "conquest" to listOf(
            BookGroup("역사서", listOf(bookLink("여호수아")))
        ),
        "judges" to listOf(
            BookGroup("역사서", listOf(bookLink("사사기"), bookLink("룻기")))
        ),
        "united-kingdom" to listOf(
            BookGroup("역사서", listOf(bookLink("사무엘상"), bookLink("사무엘하"), bookLink("열왕기상"))),
            BookGroup("시가서", listOf(bookLink("시편"), bookLink("아가")))
        ),
        "divided-kingdom" to listOf(
            BookGroup("역사서", listOf(bookLink("열왕기하"), bookLink("역대상"), bookLink("역대하"))),
            BookGroup("예언서", listOf(bookLink("이사야"), bookLink("예레미야"), bookLink("호세아"), bookLink("아모스")))
        ),
        "exile" to listOf(
            BookGroup("예언서", listOf(bookLink("예레미야"), bookLink("예레미야애가"), bookLink("에스겔"), bookLink("다니엘")))
        ),
        "return" to listOf(
            BookGroup("역사서", listOf(bookLink("에스라"), bookLink("느헤미야"), bookLink("에스더"))),
            BookGroup("예언서", listOf(bookLink("학개"), bookLink("스가랴"), bookLink("말라기")))
        ),
        "intertestamental" to listOf(
            BookGroup("배경", listOf(bookLink("헬라 시대"), bookLink("로마 시대")))
        ),
        "jesus" to listOf(
            BookGroup("복음서", listOf(bookLink("마태복음"), bookLink("마가복음"), bookLink("누가복음"), bookLink("요한복음")))
        ),
        "early-church" to listOf(
            BookGroup("역사서", listOf(bookLink("사도행전"))),
            BookGroup("서신서", listOf(bookLink("로마서"), bookLink("고린도전서"), bookLink("갈라디아서"), bookLink("에베소서")))
        )
    )

    val eventSummaries: List<HistoryEventSummary> = listOf(
        HistoryEventSummary(
            "event-creation",
            "patriarchs",
            "창조",
            "하나님이 세상을 창조하신 사건.",
            "창 1-2"
        ),
        HistoryEventSummary(
            "event-covenant-abraham",
            "patriarchs",
            "아브라함 언약",
            "믿음의 조상 아브라함에게 주어진 언약.",
            "창 12, 15, 17"
        ),
        HistoryEventSummary(
            "event-exodus",
            "exodus",
            "출애굽",
            "애굽에서 해방되어 광야로 나아간 사건.",
            "출 1-15"
        ),
        HistoryEventSummary(
            "event-sinai-covenant",
            "exodus",
            "시내산 언약",
            "율법이 주어지고 언약이 갱신된 사건.",
            "출 19-24"
        ),
        HistoryEventSummary(
            "event-jericho",
            "conquest",
            "여리고 함락",
            "가나안 정복의 첫 전투로 여리고성이 무너짐.",
            "수 6"
        ),
        HistoryEventSummary(
            "event-gideon",
            "judges",
            "기드온의 승리",
            "적은 군대로 미디안을 이긴 사건.",
            "삿 6-7"
        ),
        HistoryEventSummary(
            "event-david-kingdom",
            "united-kingdom",
            "다윗 왕국",
            "예루살렘을 중심으로 통일 왕국이 세워짐.",
            "삼하 5-7"
        ),
        HistoryEventSummary(
            "event-temple",
            "united-kingdom",
            "솔로몬 성전 봉헌",
            "첫 성전이 완공되어 봉헌됨.",
            "왕상 8"
        ),
        HistoryEventSummary(
            "event-elijah",
            "divided-kingdom",
            "엘리야의 갈멜 산 대결",
            "바알 선지자와의 대결로 하나님을 증명.",
            "왕상 18"
        ),
        HistoryEventSummary(
            "event-fall-israel",
            "divided-kingdom",
            "북이스라엘 멸망",
            "앗수르에 의해 북이스라엘이 멸망함.",
            "왕하 17"
        ),
        HistoryEventSummary(
            "event-exile-start",
            "exile",
            "바벨론 포로 시작",
            "유다 백성이 바벨론으로 끌려감.",
            "왕하 24"
        ),
        HistoryEventSummary(
            "event-temple-destruction",
            "exile",
            "예루살렘 성전 파괴",
            "바벨론에 의해 성전이 파괴됨.",
            "왕하 25"
        ),
        HistoryEventSummary(
            "event-return",
            "return",
            "스룹바벨 1차 귀환",
            "포로에서 돌아와 예배를 회복함.",
            "스 1-3"
        ),
        HistoryEventSummary(
            "event-nehemiah",
            "return",
            "느헤미야 성벽 재건",
            "예루살렘 성벽이 재건됨.",
            "느 1-6"
        ),
        HistoryEventSummary(
            "event-maccabees",
            "intertestamental",
            "마카비 혁명",
            "헬라 통치에 맞서 유대 독립을 회복.",
            "외경 배경"
        ),
        HistoryEventSummary(
            "event-nativity",
            "jesus",
            "예수 탄생",
            "메시아의 탄생과 복음의 시작.",
            "마 1-2, 눅 1-2"
        ),
        HistoryEventSummary(
            "event-resurrection",
            "jesus",
            "십자가와 부활",
            "예수님의 죽음과 부활로 구원이 완성.",
            "마 27-28"
        ),
        HistoryEventSummary(
            "event-pentecost",
            "early-church",
            "오순절 성령 강림",
            "성령이 임하여 교회가 탄생함.",
            "행 2"
        ),
        HistoryEventSummary(
            "event-paul-mission",
            "early-church",
            "바울 1차 선교",
            "복음이 이방으로 확장됨.",
            "행 13-14"
        )
    )

    val eventDetails: List<HistoryEventDetail> = eventSummaries.map { summary ->
        HistoryEventDetail(
            id = summary.id,
            eraSlug = summary.eraSlug,
            eraLabel = eraLabelBySlug[summary.eraSlug] ?: "알 수 없는 시대",
            title = summary.title,
            timeline = "기원전/후 연대(더미)",
            summary = summary.description,
            background = "정치·문화적 배경 설명 텍스트를 여기에 제공합니다.",
            references = listOf(
                HistoryReference(summary.scriptureRange, "#"),
                HistoryReference("관련 본문 더보기", "#")
            )
        )
    }

    val timelineBlocks: List<EraTimelineBlock> = eras.map { era ->
        EraTimelineBlock(
            era = era,
            eventHighlights = eventSummaries.filter { it.eraSlug == era.slug }.take(3),
            bookGroups = eraBookGroups[era.slug].orEmpty()
        )
    }

    fun findEra(slug: String?): EraSummary? {
        if (slug.isNullOrBlank()) {
            return null
        }
        return eras.firstOrNull { it.slug == slug }
    }

    fun eventsForEra(slug: String?): List<HistoryEventSummary> {
        if (slug.isNullOrBlank()) {
            return emptyList()
        }
        return eventSummaries.filter { it.eraSlug == slug }
    }

    fun findEventDetail(id: String?): HistoryEventDetail? {
        if (id.isNullOrBlank()) {
            return null
        }
        return eventDetails.firstOrNull { it.id == id }
    }
}

package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.vo.BibleBookKey
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class BibleBookKeyConverter : AttributeConverter<BibleBookKey, String> {

    override fun convertToDatabaseColumn(attribute: BibleBookKey?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): BibleBookKey? {
        return dbData?.let { BibleBookKey.Companion.fromCode(it) }
    }
}
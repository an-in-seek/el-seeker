package com.elseeker.bible.domain.vo

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class BibleBookKeyConverter : AttributeConverter<BibleBookKey, String> {

    override fun convertToDatabaseColumn(attribute: BibleBookKey?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): BibleBookKey? {
        return dbData?.let { BibleBookKey.fromCode(it) }
    }
}

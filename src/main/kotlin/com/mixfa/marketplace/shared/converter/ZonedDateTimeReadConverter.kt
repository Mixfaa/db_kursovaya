package com.mixfa.marketplace.shared.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@Component
class ZonedDateTimeReadConverter : Converter<Date, ZonedDateTime> {
    override fun convert(date: Date): ZonedDateTime {
        return date.toInstant().atZone(ZoneOffset.UTC)
    }
}

@Component
class ZonedDateTimeWriteConverter :
    Converter<ZonedDateTime, Date> {
    override fun convert(zonedDateTime: ZonedDateTime): Date {
        return Date.from(zonedDateTime.toInstant())
    }
}
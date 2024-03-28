package com.mixfa.marketplace.shared

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

interface WithDto<TDto> {
    val asDto: TDto
}

class WithDtoSerializer : StdSerializer<WithDto<*>>(WithDto::class.java) {
    override fun serialize(obj: WithDto<*>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeObject(obj.asDto)
    }
}
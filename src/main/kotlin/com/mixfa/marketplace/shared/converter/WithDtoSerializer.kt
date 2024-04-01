package com.mixfa.marketplace.shared.converter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.mixfa.marketplace.shared.model.WithDto

class WithDtoSerializer : StdSerializer<WithDto>(WithDto::class.java) {
    override fun serialize(obj: WithDto, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeObject(obj.asDto)
    }
}
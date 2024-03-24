package com.mixfa.marketplace

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.readValues
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mixfa.marketplace.shared.AbstractQueryCriteria
import com.mixfa.marketplace.shared.QueryConstructor
import com.mixfa.marketplace.shared.SortConstructor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DbKursovayaApplicationTests {
    companion object {
        val mapper = ObjectMapper()
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .registerKotlinModule()
    }

    @Test
    fun contextLoads() {
    }

    @Test
    fun sortConstructorTest() {
        val sortJson = """
{
    "orders": {
        "price": "ASC",
        "length": "DESC"
    }
}           
        """.trimIndent()

        val sort = mapper.readValue<SortConstructor>(sortJson)
        val mongoSort = sort.makeSort()

        Assertions.assertTrue(true)

        println(mongoSort)
    }

    @Test
    fun queryConstructorTest() {
        val queryJson = """
            {
                "criterias": [
                    {
                        "field": "length",
                        "type": "In",
                        "values": [
                            "long",
                            "short"
                        ]
                    },
                    {
                        "field": "price",
                        "type": "Is",
                        "value": 15.0
                    }
                ]
            }
        """.trimIndent()

        val query = mapper.readValue<QueryConstructor>(queryJson)
        val mongoQuery = query.makeQuery()

        Assertions.assertTrue(
            query.criterias.get(0) is AbstractQueryCriteria.CriteriaIn
        )
        Assertions.assertTrue(
            query.criterias.get(1) is AbstractQueryCriteria.CriteriaIs
        )

        println(mongoQuery)
    }

}

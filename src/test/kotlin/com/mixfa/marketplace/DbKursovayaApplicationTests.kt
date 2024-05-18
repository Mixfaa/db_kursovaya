package com.mixfa.marketplace

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mixfa.marketplace.shared.model.AbstractQueryCriteria
import com.mixfa.marketplace.shared.model.QueryConstructor
import com.mixfa.marketplace.shared.model.AssembleableSort
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
open class DbKursovayaApplicationTests {
    @Autowired
    lateinit var mapper: ObjectMapper

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

        val sort = mapper.readValue<AssembleableSort>(sortJson)
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

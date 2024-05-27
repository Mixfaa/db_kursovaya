package com.mixfa.shared.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.TextCriteria

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    visible = false,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AssembleableQueryCriteria.CriteriaIn::class, name = "In"),
    JsonSubTypes.Type(value = AssembleableQueryCriteria.CriteriaIs::class, name = "Is"),
    JsonSubTypes.Type(value = AssembleableQueryCriteria.CriteriaAll::class, name = "All"),
    JsonSubTypes.Type(value = AssembleableQueryCriteria.CriteriaNin::class, name = "Nin"),
    JsonSubTypes.Type(value = AssembleableQueryCriteria.CriteriaText::class, name = "Text"),
)
sealed interface AssembleableQueryCriteria {
    fun makeCriteria(): CriteriaDefinition?

    data class CriteriaIn @JsonCreator constructor(
        val field: String,
        val values: List<*>
    ) : AssembleableQueryCriteria {
        override fun makeCriteria(): CriteriaDefinition = Criteria.where(field).`in`(values)
    }

    data class CriteriaIs @JsonCreator constructor(
        val field: String,
        val value: Any?
    ) : AssembleableQueryCriteria {
        override fun makeCriteria(): CriteriaDefinition = Criteria.where(field).`is`(value)
    }

    data class CriteriaAll @JsonCreator constructor(
        val field: String,
        val values: List<*>
    ) : AssembleableQueryCriteria {
        override fun makeCriteria(): CriteriaDefinition = Criteria.where(field).all(values)
    }

    data class CriteriaNin @JsonCreator constructor(
        val field: String,
        val values: List<*>
    ) : AssembleableQueryCriteria {
        override fun makeCriteria(): CriteriaDefinition = Criteria.where(field).nin(values)
    }

    data class CriteriaText @JsonCreator constructor(
        val text: String
    ) : AssembleableQueryCriteria {
        override fun makeCriteria(): CriteriaDefinition? =
            if (text.isBlank()) null else TextCriteria.forDefaultLanguage().matching(text)

    }
}


data class QueryConstructor(
    val criterias: List<AssembleableQueryCriteria>
) {
    fun makeQuery(): Query {
        val mongoQuery = Query()

        for (criteria in criterias)
            criteria.makeCriteria()?.let(mongoQuery::addCriteria)

        return mongoQuery
    }
}
package com.mixfa.shared.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import kotlin.reflect.KClass

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CUSTOM,
    include = JsonTypeInfo.As.PROPERTY,
    visible = true, // important
    property = "type"
)
@JsonTypeIdResolver(AbstractQueryCriteria.JsonSerializationTypeIdResolver::class)
sealed class AbstractQueryCriteria(
    val field: String,
    val type: CriteriaType,
) {
    enum class CriteriaType(val klass: KClass<*>) {
        In(CriteriaIn::class),
        Is(CriteriaIs::class),
        All(CriteriaAll::class),
        Nin(CriteriaNin::class);

        companion object {
            fun fromCriteriaClass(value: Any): CriteriaType {
                return entries.first { it.klass == value::class }
            }

            fun forValue(value: String): CriteriaType {
                val ordinal = value.toIntOrNull()
                return if (ordinal != null)
                    entries[ordinal]
                else try {
                    valueOf(value)
                } catch (ex: IllegalArgumentException) {
                    valueOf(value.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }

    abstract fun apply(mongoCriteria: Criteria): Criteria

    class CriteriaIn(
        field: String,
          val values: List<*>
    ) : AbstractQueryCriteria(field, CriteriaType.In) {
        override fun apply(mongoCriteria: Criteria) = mongoCriteria.`in`(values)
    }

    class CriteriaIs(
        field: String,
        private val value: Any?
    ) : AbstractQueryCriteria(field, CriteriaType.Is) {
        override fun apply(mongoCriteria: Criteria) = mongoCriteria.`is`(value)
    }

    class CriteriaAll(
        field: String,
        private val values: List<*>
    ) : AbstractQueryCriteria(field, CriteriaType.All) {
        override fun apply(mongoCriteria: Criteria) = mongoCriteria.all(values)
    }

    class CriteriaNin(
        field: String,
        private val values: List<*>
    ) : AbstractQueryCriteria(field, CriteriaType.Nin) {
        override fun apply(mongoCriteria: Criteria) = mongoCriteria.nin(values)
    }

    class JsonSerializationTypeIdResolver : TypeIdResolverBase() {
        private lateinit var baseType: JavaType

        override fun init(bt: JavaType?) {
            baseType = bt!!
        }

        override fun idFromValue(value: Any?): String {
            if (value !is AbstractQueryCriteria) throw Exception("Can`t resolve type")
            return CriteriaType.fromCriteriaClass(value).ordinal.toString()
        }

        override fun idFromValueAndType(value: Any?, suggestedType: Class<*>?): String {
            TODO("Not yet implemented")
        }

        override fun typeFromId(context: DatabindContext?, id: String?): JavaType {
            val type = CriteriaType.forValue(id!!)

            return context!!.constructSpecializedType(
                baseType,
                type.klass.java
            )
        }

        override fun getMechanism(): JsonTypeInfo.Id {
            return JsonTypeInfo.Id.CUSTOM
        }
    }
}

data class QueryConstructor(
    val criterias: List<AbstractQueryCriteria>
) {
    fun makeQuery(): Query {
        val mongoQuery = Query()

        for (criteria in criterias)
            mongoQuery.addCriteria(
                criteria.apply(Criteria.where(criteria.field))
            )

        return mongoQuery
    }
}
package com.elseeker.common

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Table
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DatabaseCleaner(
    @PersistenceContext private val entityManager: EntityManager
) : InitializingBean {

    private lateinit var tableNames: Set<String>

    override fun afterPropertiesSet() {
        tableNames = entityManager.metamodel.entities
            .filter { it.javaType.isAnnotationPresent(Table::class.java) }
            .mapNotNull { it.javaType.getAnnotation(Table::class.java)?.name }
            .toSet()
    }

    @Transactional
    fun execute() {
        entityManager.flushAndClear()
        disableTriggers()
        truncateTables()
        enableTriggers()
    }

    private fun disableTriggers() {
        executeForAllTables { tableName ->
            entityManager.createNativeQuery("ALTER TABLE $tableName DISABLE TRIGGER ALL").executeUpdate()
        }
    }

    private fun truncateTables() {
        executeForAllTables { tableName ->
            entityManager.createNativeQuery("TRUNCATE TABLE $tableName RESTART IDENTITY CASCADE").executeUpdate()
        }
    }

    private fun enableTriggers() {
        executeForAllTables { tableName ->
            entityManager.createNativeQuery("ALTER TABLE $tableName ENABLE TRIGGER ALL").executeUpdate()
        }
    }

    private fun executeForAllTables(action: (String) -> Unit) {
        tableNames.forEach(action)
    }

    private fun EntityManager.flushAndClear() {
        this.flush()
        this.clear()
    }
}
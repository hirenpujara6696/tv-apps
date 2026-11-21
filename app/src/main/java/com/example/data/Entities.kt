package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_domain_rules",
    indices = [Index(value = ["domain"], unique = true)]
)
data class CustomDomainRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val isAllowlist: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dns_query_logs")
data class DnsQueryLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val blocked: Boolean,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

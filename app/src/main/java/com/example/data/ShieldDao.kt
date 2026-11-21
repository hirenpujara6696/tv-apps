package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShieldDao {
    @Query("SELECT * FROM custom_domain_rules ORDER BY createdAt DESC")
    fun observeRules(): Flow<List<CustomDomainRule>>

    @Query("SELECT * FROM custom_domain_rules")
    suspend fun getAllRulesOnce(): List<CustomDomainRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: CustomDomainRule)

    @Query("DELETE FROM custom_domain_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)

    @Query("DELETE FROM custom_domain_rules WHERE domain = :domain")
    suspend fun deleteRuleByDomain(domain: String)

    @Query("SELECT * FROM dns_query_logs ORDER BY timestamp DESC LIMIT 250")
    fun observeRecentLogs(): Flow<List<DnsQueryLog>>

    @Query("SELECT COUNT(*) FROM dns_query_logs WHERE blocked = 1")
    fun observeTotalBlockedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM dns_query_logs")
    fun observeTotalQueryCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM dns_query_logs WHERE blocked = 1 AND timestamp >= :sinceMillis")
    fun observeBlockedSince(sinceMillis: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: DnsQueryLog)

    @Query(
        "DELETE FROM dns_query_logs WHERE id NOT IN " +
            "(SELECT id FROM dns_query_logs ORDER BY timestamp DESC LIMIT 1000)"
    )
    suspend fun trimOldLogs()

    @Query("DELETE FROM dns_query_logs")
    suspend fun clearLogs()
}

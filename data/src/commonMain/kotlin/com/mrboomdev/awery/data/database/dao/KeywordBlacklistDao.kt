package com.mrboomdev.awery.data.database.dao

import androidx.room.*
import com.mrboomdev.awery.data.database.entity.DBBlacklistedKeyword

@Dao
interface KeywordBlacklistDao {
    /**
     * Adds a keyword to the blacklisted keywords list.
     * If the keyword already exists in the list, this operation will be ignored.
     * @param media The keyword to add to the list
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(media: DBBlacklistedKeyword)

    /**
     * Deletes a keyword from the blacklisted keywords list.
     * @param media The keyword to delete from the list
     */
    @Delete
    suspend fun delete(media: DBBlacklistedKeyword)

    /**
     * Retrieves all blacklisted keywords from the database.
     * This operation is performed synchronously and will block the calling thread until the operation is complete.
     * @return A list of all blacklisted keywords in the database.
     */
    @Query("SELECT * FROM blacklisted_keyword")
    suspend fun getAll(): List<DBBlacklistedKeyword>
}
package com.mrboomdev.awery.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class DBList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val orderIndex: Int = -1,
    val hidden: Boolean = false,
    val autoUpdateItems: Boolean = true
)

@Entity(
    primaryKeys = ["mediaExtensionId", "mediaId", "listId"],
    foreignKeys = [
        ForeignKey(
            entity = DBMedia::class,
            parentColumns = ["extensionId", "id"],
            childColumns = ["mediaExtensionId", "mediaId"],
            onDelete = ForeignKey.CASCADE
        ),

        ForeignKey(
            entity = DBList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("mediaExtensionId", "mediaId", "listId")
    ]
)
data class DBListMediaCrossRef(
    val mediaExtensionId: String,
    val mediaId: String,
    val listId: Long
)
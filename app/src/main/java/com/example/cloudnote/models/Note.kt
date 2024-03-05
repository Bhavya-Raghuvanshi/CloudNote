package com.example.cloudnote.models

data class Note
(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val isArchived: Int = 0,
    val isDeleted: Int = 0,
    val preArchived: Int = 0,
    val createTime: String = "",
    val editedTime: String = "",
    val noteColor: String? = ""
)

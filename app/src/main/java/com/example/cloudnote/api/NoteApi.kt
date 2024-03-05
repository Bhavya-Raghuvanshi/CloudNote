package com.example.cloudnote.api

import com.example.cloudnote.models.Note
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException

class NoteApi {
    companion object {
        fun add(note: Note, accessToken: String): ApiResponse =
            Api.post("notes/add.php", Gson().toJson(note), accessToken)

        fun getAll(accessToken: String): ApiResponse = Api.get("notes/get.php", accessToken)

        fun getAllArchive(accessToken: String): ApiResponse = Api.get("notes/archive.php", accessToken)
        fun getAllRecycle(accessToken: String): ApiResponse = Api.get("notes/recycle.php", accessToken)

        fun getNote(noteId: Int): ApiResponse = Api.get("notes/get_note.php?noteId=$noteId")
        fun update(note: Note): ApiResponse = Api.post("notes/update.php", Gson().toJson(note))

        fun archiveNote(archivedIds: List<Int>): ApiResponse {
            val archivedId = JsonObject().apply {
                add("archivedIds", Gson().toJsonTree(archivedIds))
            }
            return Api.post("notes/archived.php", Gson().toJson(archivedId))
        }

        fun unarchiveNote(unarchivedIds: List<Int>): ApiResponse {
            val unarchivedId = JsonObject().apply {
                add("unarchivedIds", Gson().toJsonTree(unarchivedIds))
            }
            return Api.post("notes/unarchived.php", Gson().toJson(unarchivedId))
        }

        fun recycleNote(recycledIds: List<Int>, accessToken: String?): ApiResponse {
            val recycledId = JsonObject().apply {
                add("recycledIds", Gson().toJsonTree(recycledIds))
            }
            return Api.post("notes/recycled.php", Gson().toJson(recycledId), accessToken)
        }

        fun restoreNote(restoredIds: List<Int>, accessToken: String?): ApiResponse {
            val restoredId = JsonObject().apply {
                add("restoredIds", Gson().toJsonTree(restoredIds))
            }
            return Api.post("notes/restore.php", Gson().toJson(restoredId), accessToken)
        }

        fun deleteNote(deletedIds: List<Int>): ApiResponse {
            val deletedId = JsonObject().apply {
                add("deletedIds", Gson().toJsonTree(deletedIds))
            }
            return Api.post("notes/delete.php", Gson().toJson(deletedId))
        }

        fun ApiResponse.asNoteArray(): Array<Note>? {
            return try {
                Gson().fromJson(json, Array<Note>::class.java)
            } catch (e: JsonSyntaxException) {
                null
            }
        }
    }
}
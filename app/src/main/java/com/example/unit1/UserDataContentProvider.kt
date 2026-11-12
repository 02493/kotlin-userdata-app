package com.example.unit1

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class UserDataContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.unit1.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/user_data")

        private const val USER_DATA = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "user_data", USER_DATA)
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        if (uriMatcher.match(uri) != USER_DATA) return null

        val cursor = MatrixCursor(arrayOf("user_name", "user_age"))
        val ctx = context ?: return cursor

        val (name, age) = UserDataStore(ctx).readUserDataSync()
        cursor.addRow(arrayOf(name ?: "", age?.toString() ?: ""))
        return cursor
    }

    override fun getType(uri: Uri): String? = when (uriMatcher.match(uri)) {
        // одна строка → cursor.item, а не cursor.dir
        USER_DATA -> "vnd.android.cursor.item/vnd.com.example.unit1.user_data"
        else -> null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? =
        throw UnsupportedOperationException("Insert not supported")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("Delete not supported")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = throw UnsupportedOperationException("Update not supported")
}
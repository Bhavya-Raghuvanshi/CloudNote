package com.example.cloudnote

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.cloudnote.api.NoteApi
import com.example.cloudnote.models.Note
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import com.example.cloudnote.api.ApiResponse
import com.example.cloudnote.fragments.BottomColorMenuFragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddNoteActivity : AppCompatActivity() {
    private lateinit var txtTitle: EditText

    private lateinit var txtDescription: EditText

    private lateinit var createDate: TextView

    private var bottomSheetDialog: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        txtTitle = findViewById(R.id.txtNoteTitle)
        txtDescription = findViewById(R.id.txtNoteDescription)
        createDate = findViewById(R.id.createDate)

        val currentDate = LocalDate.now()
        val formattedDate = currentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))

        createDate.text = formattedDate

        findViewById<MaterialButton>(R.id.colorBtn).setOnClickListener { showBottomColorMenu() }

        findViewById<MaterialButton>(R.id.backBtn).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.saveBtn).setOnClickListener(::addNote)

    }

    private fun showBottomColorMenu() {
        if (bottomSheetDialog == null || !bottomSheetDialog!!.isShowing) {
            val bottomColorMenuFragment = BottomColorMenuFragment()
            supportFragmentManager.beginTransaction()
                .add(bottomColorMenuFragment, "bottomColorMenuFragmentTag")
                .commitNow()

            bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog!!.setContentView(bottomColorMenuFragment.requireView())
            bottomSheetDialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
            bottomSheetDialog!!.window?.setDimAmount(0.6f)

            bottomSheetDialog!!.setOnDismissListener {
                bottomSheetDialog = null
            }
            bottomSheetDialog!!.show()
        } else {
            bottomSheetDialog!!.dismiss()
            bottomSheetDialog = null
        }
    }


    private fun addNote(view: View) {
        val title = txtTitle.text.toString()
        val description = txtDescription.text.toString()

        val token = getSharedPreferences("token", MODE_PRIVATE)
        val accessToken = token.getString("accessToken", "")

        val selectedColorName = getSharedPreferences("color_prefs", MODE_PRIVATE)
        val noteColor = selectedColorName.getString("selected_color_name", "defaultNoteColor")

        if (title.isEmpty() || description.isEmpty()) {
            Snackbar.make(view, "Empty values not allowed!", Snackbar.LENGTH_LONG).show()
            return
        }

        val note = Note(title = title, description = description, noteColor = noteColor)

        CoroutineScope(Dispatchers.IO).launch {
            val response = if (accessToken.isNullOrEmpty()) {
                ApiResponse(401, "Unauthorized")
            } else {
                NoteApi.add(note, accessToken)
            }

            if (response?.statusCode != HttpURLConnection.HTTP_OK) {
                val error = response?.getError()
                error?.let {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(view, it.messages, Snackbar.LENGTH_LONG).show()
                    }
                }
                return@launch
            }

            val responseData = JSONObject(response.json).optJSONObject("note")
            val id = responseData.optInt("id")
            val getTitle = responseData.optString("title")
            val getDescription = responseData.optString("description")
            val getNoteColor = responseData.optString("noteColor")
            val getCreateTime = responseData.optString("createTime")

            val newNote = Note(id = id, title = getTitle, description = getDescription, noteColor = getNoteColor, createTime = getCreateTime)

            withContext(Dispatchers.Main)
            {
                val editor = getSharedPreferences("color_prefs", Context.MODE_PRIVATE).edit()
                editor.remove("selected_color")
                editor.apply()

                    val intent = Intent()
                intent.putExtra("note", Gson().toJson(newNote))
                setResult(Activity.RESULT_OK, intent)

                finish()
            }
        }
    }
}
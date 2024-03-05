package com.example.cloudnote

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.cloudnote.api.NoteApi
import com.example.cloudnote.fragments.BottomColorMenuFragment
import com.example.cloudnote.models.Note
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection

class UpdateNoteActivity : AppCompatActivity()
{

    private lateinit var mainLayout: ConstraintLayout

    private lateinit var txtNoteTitle: EditText
    private lateinit var txtNoteDescription: EditText
    private lateinit var lastEditedDate: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private var bottomSheetDialog: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_note)

        val noteId = intent.getIntExtra("noteId", -1)

        mainLayout = findViewById(R.id.updateNoteLayout)
        txtNoteTitle = findViewById(R.id.txtNoteTitle)
        txtNoteDescription = findViewById(R.id.txtNoteDescription)
        lastEditedDate = findViewById(R.id.lastEdited)

        fetchNote(noteId)

        sharedPreferences = getSharedPreferences("color_prefs", Context.MODE_PRIVATE)

        findViewById<MaterialButton>(R.id.colorBtn).setOnClickListener { showBottomColorMenu() }

        findViewById<MaterialButton>(R.id.backBtn).setOnClickListener { updateNote(noteId) }
        findViewById<MaterialButton>(R.id.saveBtn).setOnClickListener { updateNote(noteId) }
    }

    override fun onBackPressed()
    {
        // Clear shared preferences related to color
        val sharedPreferences = getSharedPreferences("color_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        super.onBackPressed()
    }

    private fun fetchNote(noteId: Int)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val response = NoteApi.getNote(noteId)

            if (response?.statusCode != HttpURLConnection.HTTP_OK) {
                val error = response?.getError()
                error?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UpdateNoteActivity, it.messages, Toast.LENGTH_LONG).show()
                    }
                }

                return@launch
            }

            val responseData = JSONObject(response.json).optJSONObject("note")
            val noteTitle = responseData?.optString("title")
            val noteDescription = responseData?.optString("description")
            val noteEditedDate = "Edited " + responseData?.optString("editedTime")
            val color = responseData?.optString("noteColor")
            val colorResId = this@UpdateNoteActivity.resources.getIdentifier(color, "color", this@UpdateNoteActivity.packageName)
            val noteColor = ContextCompat.getColor(this@UpdateNoteActivity, colorResId)


            withContext(Dispatchers.Main) {
                mainLayout.setBackgroundColor(noteColor)
                txtNoteTitle.setText(noteTitle)
                txtNoteDescription.setText(noteDescription)
                lastEditedDate.setText(noteEditedDate)

                sharedPreferences.edit().putInt("selected_color", noteColor).putString("selected_color_name", color).apply()
            }
        }
    }

    private fun showBottomColorMenu()
    {
        if (bottomSheetDialog == null || !bottomSheetDialog!!.isShowing)
        {
            val bottomColorMenuFragment = BottomColorMenuFragment()
            supportFragmentManager.beginTransaction()
                .add(bottomColorMenuFragment, "updateBottomColorMenuFragmentTag")
                .commitNow()

            bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog!!.setContentView(bottomColorMenuFragment.requireView())
            bottomSheetDialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
            bottomSheetDialog!!.window?.setDimAmount(0.6f)

            bottomSheetDialog!!.setOnDismissListener {
                bottomSheetDialog = null
            }
            bottomSheetDialog!!.show()
        }
        else
        {
            bottomSheetDialog!!.dismiss()
            bottomSheetDialog = null
        }
    }

    private fun updateNote(noteId: Int)
    {
        val title = txtNoteTitle.text.toString()
        val description = txtNoteDescription.text.toString()
        val noteColor = sharedPreferences.getString("selected_color_name", "transparent")

        val note = Note(id = noteId,title = title, description = description, noteColor = noteColor)

        CoroutineScope(Dispatchers.IO).launch {

            val response = NoteApi.update(note)

            if (response?.statusCode != HttpURLConnection.HTTP_OK) {
                val error = response?.getError()
                error?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UpdateNoteActivity, it.messages, Toast.LENGTH_LONG).show()
                    }
                }
                return@launch
            }

            withContext(Dispatchers.Main)
            {
                finish()
                val editor = getSharedPreferences("color_prefs", Context.MODE_PRIVATE).edit()
                editor.remove("selected_color")
                editor.apply()
            }

        }

    }
}


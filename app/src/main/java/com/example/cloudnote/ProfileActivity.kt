package com.example.cloudnote

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.cloudnote.api.ApiResponse
import com.example.cloudnote.api.UserApi
import com.example.cloudnote.models.ProfileDetails
import com.example.cloudnote.models.ProfileImage
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.time.Instant

class ProfileActivity : AppCompatActivity() {
    private lateinit var profileImage: ImageView
    private lateinit var uploadBtn: FloatingActionButton

    private lateinit var backBtn: MaterialButton
    private lateinit var editBtn: MaterialButton

    private lateinit var bitmap: Bitmap

    private lateinit var nameEditText: EditText
    private lateinit var occupationEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var aboutEditText: EditText

    private lateinit var nameTextView: TextView
    private lateinit var occupationTextView: TextView
    private lateinit var contactTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var aboutTextView: TextView
    private lateinit var emailTextView: TextView

    private lateinit var accessToken: String

    private var isEditing: Boolean = false

    private lateinit var editTextPairs: List<Pair<TextView, EditText>>

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImage = findViewById(R.id.profileImage)
        uploadBtn = findViewById(R.id.uploadBtn)
        uploadBtn.setOnClickListener { showUploadImage() }

        backBtn = findViewById(R.id.backBtn)
        editBtn = findViewById(R.id.editBtn)

        nameTextView = findViewById(R.id.nameTextView)
        occupationTextView = findViewById(R.id.occupationTextView)
        contactTextView = findViewById(R.id.contactTextView)
        locationTextView = findViewById(R.id.locationTextView)
        aboutTextView = findViewById(R.id.aboutTextView)
        emailTextView = findViewById(R.id.emailTextView)

        nameEditText = findViewById(R.id.nameEditText)
        occupationEditText = findViewById(R.id.occupationEditText)
        contactEditText = findViewById(R.id.contactEditText)
        locationEditText = findViewById(R.id.locationEditText)
        aboutEditText = findViewById(R.id.aboutEditText)

        editTextPairs = listOf(
            Pair(nameTextView, nameEditText),
            Pair(occupationTextView, occupationEditText),
            Pair(contactTextView, contactEditText),
            Pair(locationTextView, locationEditText),
            Pair(aboutTextView, aboutEditText)
        )

        backBtn.setOnClickListener { finish() }
        editBtn.setOnClickListener { toggleEditMode() }

        val token = getSharedPreferences("token", MODE_PRIVATE)
        accessToken = token.getString("accessToken", "").toString()

        getProfile()
    }

    private fun showUploadImage()
    {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage!!, filePathColumn, null, null, null)

            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(filePathColumn[0])
                    val picturePath = it.getString(columnIndex)

                    if (isImageFile(picturePath)) {
                        bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage))
                        val extension = File(picturePath).extension.toLowerCase()
                        uploadImage(bitmap, extension)
                        profileImage.setImageBitmap(bitmap)
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Invalid image type", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    private fun isImageFile(filePath: String): Boolean
    {
        val allowedExtensions = arrayOf("jpg", "jpeg", "png")
        val extension = File(filePath).extension.toLowerCase()
        return allowedExtensions.contains(extension)
    }

    @SuppressWarnings("deprecation")
    private fun uploadImage(bitmap: Bitmap, imageType: String)
    {
        val byteArrayOutputStream = ByteArrayOutputStream()

        val compressFormat = when (imageType) {
            "png" -> Bitmap.CompressFormat.PNG
            "jpeg" -> Bitmap.CompressFormat.JPEG
            else -> Bitmap.CompressFormat.JPEG
        }
        bitmap.compress(compressFormat, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val timestamp = Instant.now().toEpochMilli().toString()
        val fileName = "image_$timestamp.$imageType"
        val encodedImage = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)

        val profileImage = ProfileImage(imageName = fileName, image = encodedImage)
        CoroutineScope(Dispatchers.IO).launch {
            val response = if (accessToken.isNullOrEmpty()) {
                ApiResponse(401, "Unauthorized")
            } else {
                UserApi.profileImage(profileImage, accessToken)
            }

            if (response?.statusCode != HttpURLConnection.HTTP_OK) {
                val error = response?.getError()
                error?.let {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(findViewById(androidx.appcompat.R.id.content), it.messages, Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
                return@launch
            }
            withContext(Dispatchers.Main)
            {
                val intent = Intent()
                intent.putExtra("image", fileName)
                setResult(Activity.RESULT_OK, intent)

                finish()
            }
        }
    }

    private fun toggleEditMode()
    {
        if (editBtn.text == "edit") {
            isEditing = true

            for ((textView, editText) in editTextPairs) {
                editText.setText(textView.text)
                editText.setSelection(editText.text.length)
                textView.visibility = View.GONE
                editText.visibility = View.VISIBLE
            }
            editBtn.text = "save"
            editBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_save)
        } else {
            saveProfile()
            isEditing = false

            for ((textView, editText) in editTextPairs) {
                textView.text = editText.text
                textView.visibility = View.VISIBLE
                editText.visibility = View.GONE
            }
            editBtn.text = "edit"
            editBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_edit)

        }
    }


    private fun saveProfile()
    {
        val name = nameEditText.text.toString()
        val occupation = occupationEditText.text.toString()
        val contact = contactEditText.text.toString()
        val location = locationEditText.text.toString()
        val about = aboutEditText.text.toString()

        val profile = ProfileDetails(
            name = name,
            occupation = occupation,
            contact = contact,
            location = location,
            about = about
        )

        CoroutineScope(Dispatchers.IO).launch {
            val response = if (accessToken.isNullOrEmpty()) {
                ApiResponse(401, "Unauthorized")
            } else {
                UserApi.profile(profile, accessToken)
            }

            if (response?.statusCode != HttpURLConnection.HTTP_OK) {
                val error = response?.getError()
                error?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(findViewById(androidx.appcompat.R.id.content), it.messages, Toast.LENGTH_LONG)
                            .show()
                    }
                }
                return@launch
            }
            withContext(Dispatchers.Main)
            {
                finish()
            }
        }
    }


    private fun getProfile()
    {
        CoroutineScope(Dispatchers.IO).launch {
            val response = accessToken?.let { UserApi.getProfile(it) }

            if (response?.statusCode != HttpURLConnection.HTTP_OK) {
                val error = response?.getError()
                error?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, it.messages, Toast.LENGTH_LONG).show()
                    }
                }

                return@launch
            }

            val responseData = JSONObject(response.json).optJSONObject("profile")
            val name = responseData?.optString("name")
            val occupation = responseData?.optString("occupation")
            val about = responseData?.optString("about")
            val contact = responseData?.optString("contact")
            val location = responseData?.optString("location")
            val avatarFileName = responseData?.optString("avatarFileName")

            val avatarImageUrl = avatarFileName?.let { "http://{URL}/api/public/uploads/avatars/$it" }

            val email = JSONObject(response.json).getString("email")

            withContext(Dispatchers.Main)
            {
                nameTextView.setText(name)
                occupationTextView.setText(occupation)
                aboutTextView.setText(about)
                contactTextView.setText(contact)
                locationTextView.setText(location)
                emailTextView.setText(email)

                if (isEditing)
                {
                    nameEditText.setText(name)
                    occupationEditText.setText(occupation)
                    aboutEditText.setText(about)
                    locationEditText.setText(contact)
                    contactEditText.setText(location)
                }

                Glide.with(this@ProfileActivity)
                    .load(avatarImageUrl)
                    .error(R.drawable.profile)
                    .into(profileImage)
            }
        }
    }

    override fun onBackPressed() {
        if (isEditing) {
            toggleEditMode()
        } else {
            super.onBackPressed()
        }
    }
}
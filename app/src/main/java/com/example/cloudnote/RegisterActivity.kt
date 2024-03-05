package com.example.cloudnote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.cloudnote.api.UserApi
import com.example.cloudnote.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

class RegisterActivity : AppCompatActivity() {
    private lateinit var txtEmail: TextInputEditText
    private lateinit var txtPassword: TextInputEditText
    private lateinit var txtConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword)

        btnRegister = findViewById(R.id.btnRegister)
        btnRegister.setOnClickListener(::registerClicked)

        btnLogin = findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener(::loginClicked)
    }

    private fun registerClicked(view: View) {
        val email = txtEmail.text.toString()
        val password = txtPassword.text.toString()
        val confirmPassword = txtConfirmPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Snackbar.make(view, "Empty values not allowed!", Snackbar.LENGTH_LONG).show()
            return
        }

        if (password != confirmPassword) {
            Snackbar.make(view, "Passwords do not match!", Snackbar.LENGTH_LONG).show()
            return
        }

        val user = User(email = email, password = password)

        CoroutineScope(Dispatchers.IO).launch {
            val response = UserApi.register(user)
            if (response?.statusCode != HttpURLConnection.HTTP_OK) {
                val errorMessage = try {
                    val json = JSONObject(response.json)
                    val messagesArray = json.getJSONArray("messages")
                    val messages = StringBuilder()

                    for (i in 0 until messagesArray.length()) {
                        messages.append(messagesArray.getString(i))
                        if (i < messagesArray.length() - 1) {
                            messages.append("\n") // Add newline separator between messages
                        }
                    }

                    if (messages.isEmpty()) {
                        "Unknown error"
                    } else {
                        messages.toString()
                    }
                } catch (e: JSONException) {
                    "Error parsing response"
                }

                withContext(Dispatchers.Main) {
                    Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG).show()
                }

                return@launch
            }

            withContext(Dispatchers.Main) {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun loginClicked(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}  
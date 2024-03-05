package com.example.cloudnote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.cloudnote.api.UserApi
import com.example.cloudnote.api.UserApi.Companion.asUser
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

class LoginActivity : AppCompatActivity() {
    private lateinit var txtEmail: TextInputEditText
    private lateinit var txtPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)

        btnLogin = findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener(::loginClicked)

        btnRegister = findViewById(R.id.btnRegister)
        btnRegister.setOnClickListener(::registerClicked)
    }

    private fun loginClicked(view: View) {
        val user = User(
            email = txtEmail.text.toString(),
            password = txtPassword.text.toString()
        )

        CoroutineScope(Dispatchers.IO).launch {
            val response = UserApi.login(user)

            if (response?.statusCode != HttpURLConnection.HTTP_OK) {
                val errorMessage = try {
                    val json = JSONObject(response.json)
                    val messages = json.getJSONArray("messages")
                    if (messages.length() > 0) {
                        "${messages}"
                    } else {
                        "Unknown error"
                    }
                } catch (e: JSONException) {
                    "Error parsing response"
                }


                withContext(Dispatchers.Main) {
                    Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG).show()
                }

                return@launch
            }
            val responseJson = JSONObject(response.json)


            withContext(Dispatchers.Main) {

                if (responseJson.has("accessToken")) {
                    val accessToken = responseJson.getString("accessToken")

                    val token = getSharedPreferences("token", MODE_PRIVATE)
                    val editor = token.edit()

                    editor.putString("accessToken", accessToken)
                    editor.apply()

                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)

                    finish()
                }

            }
        }
    }

    private fun registerClicked(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
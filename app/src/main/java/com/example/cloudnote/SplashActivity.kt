package com.example.cloudnote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate

class SplashActivity : AppCompatActivity()
{
    private lateinit var logo: ImageView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        logo = findViewById(R.id.logo)

        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_anim))

        Handler(Looper.getMainLooper()).postDelayed({
            doSomething()
        },3000)
    }

    private fun doSomething()
    {
        startActivity(Intent(this,HomeActivity::class.java))
        finish()
    }
}
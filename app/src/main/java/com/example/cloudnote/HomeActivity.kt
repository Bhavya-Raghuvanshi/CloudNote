package com.example.cloudnote

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.cloudnote.fragments.HomeFragment
import com.example.cloudnote.fragments.ArchiveFragment
import com.example.cloudnote.fragments.RecycleFragment
import com.google.android.material.navigation.NavigationView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.cloudnote.api.ApiResponse
import com.example.cloudnote.api.UserApi
import com.example.cloudnote.models.Note
import com.example.cloudnote.services.Utilitites
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection


class HomeActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var toolbar: Toolbar

    private lateinit var homeFragment: HomeFragment
    private lateinit var archiveFragment: ArchiveFragment
    private lateinit var recycleFragment: RecycleFragment

    private lateinit var changeLayout: ImageButton
    private lateinit var profileLayout: ImageView

    private lateinit var toolbarTitle: TextView

    private var isStaggeredLayout = true

    private lateinit var addProfileActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        toolbar = findViewById(R.id.toolbar)

        changeLayout = findViewById(R.id.changeLayoutButton)
        profileLayout = findViewById(R.id.profileLayoutButton)

        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener(::menuItemSelected)

        changeLayout.setOnClickListener { toggleLayout() }

        profileLayout.setOnClickListener(::showPopupMenu)

        addProfileActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data!!
                    val avatarFileName = intent.getStringExtra("image")
                    val avatarImageUrl = avatarFileName?.let { "http://{URL}/api/public/uploads/avatars/$it" }

                    Glide.with(this@HomeActivity)
                        .load(avatarImageUrl)
                        .circleCrop()
                        .error(R.drawable.ic_profile_pic)
                        .into(profileLayout)
                }
            }

        toolbarTitle = findViewById(R.id.title)

        toolbarTitle.text = "Home"

        homeFragment = HomeFragment()
        archiveFragment = ArchiveFragment()
        recycleFragment = RecycleFragment()
        showFragment(homeFragment)

        val token = getSharedPreferences("token", MODE_PRIVATE)
        val accessToken = token.getString("accessToken", "")

        if (accessToken == "") {
            goToLogin()
            finish()
            return
        }

        getProfileImage()

        val preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val layout = preferences.getBoolean("isStaggeredLayout", true)

        if (layout) {
            changeLayout.setImageResource(R.drawable.ic_list_view)
        } else {
            changeLayout.setImageResource(R.drawable.ic_grid_view)
        }
    }

    private fun showFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.currentFragment, fragment)
        transaction.commit()
    }

    private fun menuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menuHome -> {
                showFragment(homeFragment)
                toolbarTitle.text = "Home"
            }

            R.id.menuArchive -> {
                showFragment(archiveFragment)
                toolbarTitle.text = "Archive"
            }

            R.id.menuRecycle -> {
                showFragment(recycleFragment)
                toolbarTitle.text = "Recycle"
            }
        }

        return true
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun logOut() {
        val token = getSharedPreferences("token", MODE_PRIVATE)
        val editor = token.edit()

        editor.remove("accessToken")
        editor.apply()

        goToLogin()
    }

    private fun toggleLayout() {
        isStaggeredLayout = !isStaggeredLayout
        val preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        preferences.edit().putBoolean("isStaggeredLayout", isStaggeredLayout).apply()

        homeFragment.updateLayout(isStaggeredLayout)
        archiveFragment.updateLayout(isStaggeredLayout)
        recycleFragment.updateLayout(isStaggeredLayout)

        if (isStaggeredLayout) {
            changeLayout.setImageResource(R.drawable.ic_list_view)
        } else {
            changeLayout.setImageResource(R.drawable.ic_grid_view)
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.profile_menu) // Menu Resource
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuProfile -> {
                    addProfileActivityLauncher.launch(Intent(this, ProfileActivity::class.java))
                    true
                }

                R.id.menuLogout -> {
                    logOut()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun getProfileImage() {
        val token = getSharedPreferences("token", MODE_PRIVATE)
        val accessToken = token.getString("accessToken", "")

        CoroutineScope(Dispatchers.IO).launch {
            val response = if (accessToken.isNullOrEmpty()) {
                ApiResponse(401, "Unauthorized")
            } else {
                UserApi.getProfile(accessToken)
            }

            if (response?.statusCode != HttpURLConnection.HTTP_OK) {
                val error = response?.getError()
                error?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeActivity, it.messages, Toast.LENGTH_LONG).show()
                    }
                }
            }

            val responseData = JSONObject(response.json).optJSONObject("profile")
            val avatarFileName = responseData?.optString("avatarFileName")

            val avatarImageUrl = avatarFileName?.let { "http://{URL}/api/public/uploads/avatars/$it" }

            withContext(Dispatchers.Main)
            {
                Glide.with(this@HomeActivity)
                    .load(avatarImageUrl)
                    .circleCrop()
                    .error(R.drawable.profile)
                    .into(profileLayout)
            }
        }

    }
}
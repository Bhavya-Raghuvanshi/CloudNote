package com.example.cloudnote.adapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudnote.R
import com.example.cloudnote.models.NoteColor
import com.google.android.material.card.MaterialCardView

class NoteColorRVAdapter
(
    private val context: Context,
    private val resourceId: Int,
    private val colors: List<NoteColor>,
    private val mainLayout: ViewGroup
)
    : RecyclerView.Adapter<NoteColorRVAdapter.ViewHolder>()
{

    private var selectedColor: Int = Color.TRANSPARENT
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("color_prefs", Context.MODE_PRIVATE)
    private val selectedColorKey = "selected_color"
    private val selectedColorNameKey = "selected_color_name"

    private val modeChangeReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context?, intent: Intent?)
        {
            applySelectedColor()
        }
    }

    init
    {
        selectedColor = sharedPreferences.getInt(selectedColorKey, Color.TRANSPARENT)
        registerModeChangeReceiver()
    }

    private fun registerModeChangeReceiver()
    {
        val filter = IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)
        context.registerReceiver(modeChangeReceiver, filter)
    }

    private fun unregisterModeChangeReceiver()
    {
        context.unregisterReceiver(modeChangeReceiver)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        private val colorItem: MaterialCardView
        private val checkIcon: ImageView

        init
        {
            colorItem = view.findViewById(R.id.colorItem)
            checkIcon = view.findViewById(R.id.checkIcon)
        }

        fun bind(color: NoteColor)
        {
            val colorResId = color.resId
            val colorName = color.name

            colorItem.setCardBackgroundColor(ContextCompat.getColor(context, colorResId))
            colorItem.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorResId))

            itemView.setOnClickListener()
            {
                if (selectedColor != colorResId)
                {
                    selectedColor = colorResId
                    saveSelectedColor(selectedColor, colorName)

                    mainLayout.setBackgroundColor(ContextCompat.getColor(context, colorResId))
                    showCheckmark(true)
                }
                else
                {
                    selectedColor = Color.TRANSPARENT
                    saveSelectedColor(selectedColor, "transparent")

                    mainLayout.setBackgroundColor(Color.TRANSPARENT)
                    showCheckmark(false)
                }

                notifyDataSetChanged()
            }

            if (selectedColor == colorResId)
            {
                showCheckmark(true)
                colorItem.strokeColor =
                    ContextCompat.getColor(context, R.color.selected_stroke_color)
            }
            else
            {
                showCheckmark(false)
                colorItem.strokeColor = ContextCompat.getColor(context, R.color.strokeColor)
            }
        }

        private fun showCheckmark(show: Boolean)
        {
            checkIcon.visibility = if (show) View.VISIBLE else View.GONE
        }

        private fun saveSelectedColor(color: Int, colorName: String)
        {
            sharedPreferences.edit()
                .putInt(selectedColorKey, color)
                .putString(selectedColorNameKey, colorName)
                .apply()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(context).inflate(resourceId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val color = colors[position]
        holder.bind(color)
    }

    override fun getItemCount(): Int = colors.size

    fun applySelectedColor()
    {
        val selectedColorResId = sharedPreferences.getInt(selectedColorKey, Color.TRANSPARENT)
        val selectedColorName = sharedPreferences.getString(selectedColorNameKey, "") ?: ""

        if (selectedColorResId != Color.TRANSPARENT)
        {
            val backgroundColor = if (isDarkModeEnabled())
            {
                getDarkModeColor(selectedColorName)
            }
            else
            {
                selectedColorResId
            }

            mainLayout.setBackgroundColor(backgroundColor)
        }
    }

    private fun isDarkModeEnabled(): Boolean
    {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    private fun getDarkModeColor(colorName: String): Int
    {
        val darkModeColorResId = context.resources.getIdentifier(colorName, "color", context.packageName)
        return ContextCompat.getColor(context, darkModeColorResId)
    }
}

package com.example.cloudnote.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudnote.R
import com.example.cloudnote.UpdateNoteActivity
import com.example.cloudnote.models.Note
import com.google.android.material.card.MaterialCardView

class NoteRVAdapter
(
    private val context: Context,
    private val resourceId: Int,
    private val notes: MutableList<Note>,
    private var showSelectionBar: (Boolean) -> Unit,
    private val showSelectionBarCount: (Int) -> Unit
) : RecyclerView.Adapter<NoteRVAdapter.ViewHolder>()
{
    private var isEnable = false
    private val itemSelectedList = mutableListOf<Int>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        private val txtNoteTitle: TextView = view.findViewById(R.id.txtNoteTitle)
        private val txtNoteDescription: TextView = view.findViewById(R.id.txtNoteDescription)
        private val notesCreatedDate: TextView = view.findViewById(R.id.notesDate)

        fun bind(note: Note, position: Int)
        {
            txtNoteTitle.text = note.title
            txtNoteDescription.text = note.description
            notesCreatedDate.text = note.createTime

            val card = itemView as MaterialCardView

            card.setOnLongClickListener { selectItem(this, position); true }
            card.setOnClickListener()
            {
                if (position >= 0 && position < itemSelectedList.size)
                {
                    if (itemSelectedList.contains(position))
                    {
                        itemSelectedList.removeAt(position)
                        card.setBackgroundResource(android.R.color.transparent)
                        showSelectionBarCount(itemSelectedList.size)
                        if (itemSelectedList.isEmpty())
                        {
                            isEnable = false
                            showSelectionBar(false)
                        }
                        return@setOnClickListener
                    }
                }

                if (isEnable)
                {
                    selectItem(this, position)
                    return@setOnClickListener
                }

                val intent = Intent(context, UpdateNoteActivity::class.java)
                intent.putExtra("noteId", note.id)
                context.startActivity(intent)
            }
            val noteColor = note.noteColor
            if (noteColor != null)
            {
                if (noteColor.isNotBlank())
                {
                    val colorResId = context.resources.getIdentifier(noteColor, "color", context.packageName)
                    card.setCardBackgroundColor(ContextCompat.getColor(context, colorResId))
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(resourceId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notes[position], position)
    }

    private fun selectItem(holder: ViewHolder, position: Int) {
        isEnable = true
        itemSelectedList.add(position)
        val selectedDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.card_stroke_selector)
        holder.itemView.background = selectedDrawable
        showSelectionBar(true)
        showSelectionBarCount(itemSelectedList.size)
    }

    override fun getItemCount(): Int = notes.size

    fun getSelectedPositions(): List<Int>
    {
        return itemSelectedList.toList()
    }

    fun deleteSelectedItem()
    {
        if (itemSelectedList.isNotEmpty()) {
            isEnable = false
            itemSelectedList.clear()
            notifyDataSetChanged()
        }
    }
}
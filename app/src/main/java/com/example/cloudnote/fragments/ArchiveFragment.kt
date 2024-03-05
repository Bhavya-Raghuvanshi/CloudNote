package com.example.cloudnote.fragments

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.cloudnote.*
import com.example.cloudnote.adapters.NoteRVAdapter
import com.example.cloudnote.api.ApiResponse
import com.example.cloudnote.api.NoteApi
import com.example.cloudnote.api.NoteApi.Companion.asNoteArray
import com.example.cloudnote.models.Note
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.net.HttpURLConnection

class ArchiveFragment : Fragment() {
    private lateinit var progress: ProgressBar
    private lateinit var rvNotes: RecyclerView

    private lateinit var notesAdapter: NoteRVAdapter

    private lateinit var appBarLayout: AppBarLayout

    private var isSelectionBarVisible: Boolean = false

    private lateinit var selectionBarLayout: View

    private lateinit var selectedCountTextView: TextView

    private var isStaggeredLayout = true

    private val notes = mutableListOf<Note>()

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var isAttachedToContext = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_archive, container, false)

        progress = view.findViewById(R.id.progress)
        rvNotes = view.findViewById(R.id.rvNotes)
        appBarLayout = requireActivity().findViewById(R.id.appBarLayout)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)

        viewLifecycleOwner.lifecycleScope.launch {
            fetchAllNotes()
        }

        isAttachedToContext = true

        selectionBarLayout = layoutInflater.inflate(R.layout.selection_bar_layout, null)
        selectionBarLayout.visibility = View.GONE

        selectedCountTextView = selectionBarLayout.findViewById(R.id.selectedCount)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            fetchAllNotes()
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchAllNotes()
    {
        val context = context ?: return

        progress.visibility = View.VISIBLE
        rvNotes.visibility = View.INVISIBLE

        val token = requireContext().getSharedPreferences("token", MODE_PRIVATE)
        val accessToken = token.getString("accessToken", "")

        CoroutineScope(Dispatchers.IO).launch {
            val response = if (accessToken.isNullOrEmpty()) {
                ApiResponse(401, "Unauthorized")
            } else {
                NoteApi.getAllArchive(accessToken)
            }

            if (response?.statusCode != HttpURLConnection.HTTP_OK) {
                val error = response?.getError()
                error?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, it.messages, Toast.LENGTH_LONG).show()
                    }
                }
                return@launch
            }
            val allNotes = response?.asNoteArray() ?: return@launch

            withContext(Dispatchers.Main)
            {
                notes.clear()
                notes.addAll(allNotes)

                val noItemsImageView = view?.findViewById<ImageView>(R.id.noItemsImageView)
                val noItemsText = view?.findViewById<TextView>(R.id.noItemsText)

                if (notes.isEmpty()) {
                    noItemsImageView?.visibility = View.VISIBLE
                    noItemsText?.visibility = View.VISIBLE
                    progress.visibility = View.INVISIBLE
                    return@withContext
                }

                noItemsImageView?.visibility = View.GONE
                noItemsText?.visibility = View.GONE

                val preferences = context.getSharedPreferences("MyPreferences", MODE_PRIVATE)
                isStaggeredLayout = preferences.getBoolean("isStaggeredLayout", true)

                notesAdapter = NoteRVAdapter(context, R.layout.note_list_item, notes,
                    { showSelectionBar ->
                        if (showSelectionBar) {
                            showSelectionBar()
                        } else {
                            hideSelectionBar()
                        }
                    },
                    { count ->
                        updateSelectedItemsCount(count)
                    }
                )

                val layoutManager = if (isStaggeredLayout) {
                    StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
                } else {
                    LinearLayoutManager(context)
                }
                rvNotes.layoutManager = layoutManager
                rvNotes.adapter = notesAdapter

                progress.visibility = View.INVISIBLE
                rvNotes.visibility = View.VISIBLE

                notesAdapter.notifyDataSetChanged()
            }
        }
    }

    fun updateLayout(isStaggeredLayout: Boolean)
    {
        if (!isAttachedToContext)
        {
            return
        }

        val layoutManager = if (isStaggeredLayout) {
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        } else {
            LinearLayoutManager(context)
        }
        rvNotes.layoutManager = layoutManager
        rvNotes.adapter?.notifyDataSetChanged()
    }

    private fun updateSelectedItemsCount(count: Int) {
        selectedCountTextView.text = "$count"
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showSelectionBar()
    {
        if (!isSelectionBarVisible)
        {
            val parentLayout = appBarLayout.parent as ViewGroup
            val drawerIndex = parentLayout.indexOfChild(appBarLayout)

            parentLayout.removeViewAt(drawerIndex)
            parentLayout.addView(selectionBarLayout, drawerIndex)

            selectionBarLayout.visibility = View.VISIBLE

            val deselectButton = selectionBarLayout.findViewById<MaterialButton>(R.id.btnDeselect)
            deselectButton.setOnClickListener()
            {
                hideSelectionBar()
            }

            val unarchiveButton = selectionBarLayout.findViewById<MaterialButton>(R.id.btnArchive)
            unarchiveButton.setIconResource(R.drawable.ic_unarchive)
            unarchiveButton.setOnClickListener { unarchivedNote() }

            val recycleButton = selectionBarLayout.findViewById<MaterialButton>(R.id.btnRecycle)
            recycleButton.setOnClickListener { recycledNote() }

            isSelectionBarVisible = true
        }
    }

    private fun hideSelectionBar()
    {
        if (isSelectionBarVisible) {
            val parentLayout = selectionBarLayout.parent as? ViewGroup
            parentLayout?.let {
                val selectionBarIndex = it.indexOfChild(selectionBarLayout)
                if (selectionBarIndex != -1) {
                    it.removeViewAt(selectionBarIndex)
                    it.addView(appBarLayout, selectionBarIndex)
                    isSelectionBarVisible = false
                }
            }
        }
        notesAdapter.deleteSelectedItem()
        fetchAllNotes()
    }

    private fun onActionButtonClicked(): List<Int> {
        val selectedNotePositions: List<Int> = notesAdapter.getSelectedPositions()
        val selectedNotesIds: MutableList<Int> = mutableListOf()

        for (position in selectedNotePositions) {
            if (position in 0 until notes.size) {
                val selectedNote = notes[position]
                selectedNotesIds.add(selectedNote.id)
            }
        }
        return selectedNotesIds
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun unarchivedNote() {
        val selectedNoteIds = onActionButtonClicked()

        CoroutineScope(Dispatchers.IO).launch {
            val response = NoteApi.unarchiveNote(selectedNoteIds)

            if (response.statusCode != HttpURLConnection.HTTP_OK) {
                val error = response.getError()
                withContext(Dispatchers.Main)
                {
                    Toast.makeText(context, error?.messages, Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            withContext(Dispatchers.Main)
            {
                view?.let {
                    hideSelectionBar()
                    Snackbar.make(it, "Unarchived", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun recycledNote() {
        val selectedNoteIds = onActionButtonClicked()
        val token = requireContext().getSharedPreferences("token", MODE_PRIVATE)
        val accessToken = token.getString("accessToken", "")

        CoroutineScope(Dispatchers.IO).launch {
            val response = NoteApi.recycleNote(selectedNoteIds, accessToken)

            if (response.statusCode != HttpURLConnection.HTTP_OK) {
                val error = response.getError()
                withContext(Dispatchers.Main)
                {
                    Toast.makeText(requireContext(), error?.messages, Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            withContext(Dispatchers.Main)
            {
                view?.let {
                    hideSelectionBar()
                    Snackbar.make(it, "Recycled", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
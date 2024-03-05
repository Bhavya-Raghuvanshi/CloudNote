package com.example.cloudnote.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudnote.R
import com.example.cloudnote.adapters.NoteColorRVAdapter
import com.example.cloudnote.models.NoteColor


class BottomColorMenuFragment : Fragment()
{
    private lateinit var colorsAdapter: NoteColorRVAdapter

    private lateinit var noteColorRV: RecyclerView

    private lateinit var mainLayout: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {

        val view = inflater.inflate(R.layout.fragment_bottom_color_menu, container, false)

        super.onViewCreated(view, savedInstanceState)

        noteColorRV = view.findViewById(R.id.recyclerViewColors)
        mainLayout = requireActivity().findViewById(android.R.id.content)

        val colorsList = listOf(
            NoteColor(R.color.transparent,"transparent"),
            NoteColor(R.color.grey,"grey"),
            NoteColor(R.color.purple,"purple"),
            NoteColor(R.color.pink,"pink"),
            NoteColor(R.color.blue,"blue"),
            NoteColor(R.color.orange,"orange"),
            NoteColor(R.color.sky,"sky"),
            NoteColor(R.color.green,"green")
        )

        colorsAdapter = NoteColorRVAdapter(requireContext(), R.layout.note_color_item, colorsList, mainLayout)
        noteColorRV.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        noteColorRV.adapter = colorsAdapter

        return view
    }
}
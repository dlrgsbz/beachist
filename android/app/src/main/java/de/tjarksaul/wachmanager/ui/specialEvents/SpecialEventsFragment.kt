package de.tjarksaul.wachmanager.ui.specialEvents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.ui.base.BaseFragment


class SpecialEventsFragment : BaseFragment() {
    private val specialEventsViewModel: SpecialEventsViewModel by viewModels()

    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_special_events, container, false)
        listView = root.findViewById(R.id.specialEventList)

        specialEventsViewModel.events.observe(viewLifecycleOwner, Observer { items ->
            val adapter = activity?.applicationContext?.let { ctx ->
                SpecialEventsListAdapter(ctx, items, requireActivity())
            }
            listView.adapter = adapter
        })

        val button = root.findViewById<FloatingActionButton>(R.id.add_special_event_fab)
        button.setOnClickListener {
            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(
                android.R.id.content,
                AddSpecialEventFragment(specialEventsViewModel)
            )

            transaction.addToBackStack("ShowAddSpecialEventsView")

            transaction.commit()
        }

        return root
    }
}
package de.tjarksaul.wachmanager.modules.specialEvents

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.dtos.NetworkState
import de.tjarksaul.wachmanager.dtos.SpecialEvent
import de.tjarksaul.wachmanager.dtos.SpecialEventKind


class SpecialEventsListAdapter(
    context: Context,
    items: List<SpecialEvent>,
//    private val itemClickCallback: ((String, Boolean, StateKind?, Int?, String?) -> Unit)?,
    private val parentActivity: Activity
) : ArrayAdapter<SpecialEvent>(context, R.layout.item_special_event, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_special_event, parent, false)

        val item = getItem(position) ?: return view


        val nameView: TextView = view.findViewById(R.id.special_event_item_name)
        val dateView: TextView = view.findViewById(R.id.special_event_item_date)
        val typeView: TextView = view.findViewById(R.id.special_event_item_type)

        nameView.text = item.title
        dateView.text = item.printableDate
        typeView.text = if (item.kind == SpecialEventKind.damage)  "Schadenmeldung" else "Besonderes Vorkommnis"

        addNetworkIndicator(view, item.networkState)

        return view
    }

    private fun addNetworkIndicator(view: View, networkState: NetworkState) {
        val loadingIndicatorContainer =
            view.findViewById<LinearLayout>(R.id.loading_indicator_container)

        loadingIndicatorContainer.removeAllViews()

        lateinit var indicatorView: View
        when (networkState) {
            NetworkState.pending -> {
                val progressBar =
                    ProgressBar(context, null, android.R.attr.progressBarStyleSmall)
                progressBar.isIndeterminate = true
                indicatorView = progressBar
            }
            NetworkState.failed -> {
                val imageView = ImageView(context)
                imageView.setImageResource(R.drawable.ic_baseline_error_outline_24)
                indicatorView = imageView
            }
            NetworkState.successful -> {
                val imageView = ImageView(context)
                imageView.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
                indicatorView = imageView
            }
        }

        indicatorView.visibility = View.VISIBLE
        loadingIndicatorContainer.addView(indicatorView, 100, 100)
    }

//    private fun showNoteInput(itemId: String, resultStateKind: StateKind) {
//        val builder: AlertDialog.Builder = AlertDialog.Builder(parentActivity)
//        builder.setTitle("Bitte das Problem genauer beschreiben")
//
//        val input = EditText(parentActivity)
//        input.inputType = InputType.TYPE_CLASS_TEXT
//        builder.setView(input)
//
//        builder.setPositiveButton(
//            "OK"
//        ) { _, _ ->
//            val note = input.text.toString()
//            itemClickCallback?.invoke(itemId, false, resultStateKind, null, note)
//        }
//        builder.setNegativeButton(
//            "Cancel"
//        ) { dialog, _ -> dialog.cancel() }
//
//        builder.show()
//    }
}
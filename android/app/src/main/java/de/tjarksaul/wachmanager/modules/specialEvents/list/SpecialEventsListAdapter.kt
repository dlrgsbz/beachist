package de.tjarksaul.wachmanager.modules.specialEvents.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.diffableList
import de.tjarksaul.wachmanager.dtos.NetworkState
import de.tjarksaul.wachmanager.dtos.SpecialEvent
import de.tjarksaul.wachmanager.dtos.SpecialEventKind
import de.tjarksaul.wachmanager.util.DateFormatProvider
import kotlinx.android.synthetic.main.item_special_event.view.*


internal class SpecialEventsListAdapter(private val formatProvider: DateFormatProvider) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items: List<SpecialEvent> by diffableList(
        compareContent = { a, b -> a == b },
        compareId = { a, b -> a.id == b.id }
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_special_event, parent, false)

        return SpecialEventHolder(view, formatProvider)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SpecialEventHolder -> holder.bind(
                items[position],
            )
        }
    }
}

internal class SpecialEventHolder(itemView: View, private val formatProvider: DateFormatProvider) :
    RecyclerView.ViewHolder(itemView) {
    fun bind(specialEvent: SpecialEvent) {
        with(specialEvent) {
            val nameView: TextView = itemView.special_event_item_name
            val dateView: TextView = itemView.special_event_item_date
            val typeView: TextView = itemView.special_event_item_type

            nameView.text = this.title
            dateView.text = this.printableDate(formatProvider)
            typeView.text =
                if (this.kind == SpecialEventKind.damage) "Schadenmeldung" else "Besonderes Vorkommnis"

            addNetworkIndicator(itemView, this.networkState)
        }
    }

    private fun addNetworkIndicator(view: View, networkState: NetworkState) {
        val loadingIndicatorContainer = view.loading_indicator_container

        loadingIndicatorContainer.removeAllViews()

        lateinit var indicatorView: View
        when (networkState) {
            NetworkState.pending -> {
                val progressBar =
                    ProgressBar(view.context, null, android.R.attr.progressBarStyleSmall)
                progressBar.isIndeterminate = true
                indicatorView = progressBar
            }
            NetworkState.failed -> {
                val imageView = ImageView(view.context)
                imageView.setImageResource(R.drawable.ic_baseline_error_outline_24)
                indicatorView = imageView
            }
            NetworkState.successful -> {
                val imageView = ImageView(view.context)
                imageView.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
                indicatorView = imageView
            }
        }

        indicatorView.visibility = View.VISIBLE
        loadingIndicatorContainer.addView(indicatorView, 100, 100)
    }
}

private fun SpecialEvent.printableDate(formatProvider: DateFormatProvider): String {
    return formatProvider.getTimeFormatForDate(date).format(date)
}

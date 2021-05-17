package de.tjarksaul.wachmanager.modules.events

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.dtos.NetworkState
import de.tjarksaul.wachmanager.util.formatDateTime
import io.reactivex.Observer
import kotlinx.android.synthetic.main.item_event.view.*
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class EventsListAdapter(
    private val actions: Observer<EventListAction>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<Event> by diffableList(
        compareContent = { a, b -> a == b },
        compareId = { a, b -> a == b }
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)

        return EventHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventHolder -> holder.bind(
                items[position],
                actions
            )
        }
    }


}

internal class EventHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @SuppressLint("SetTextI18n")
    fun bind(event: Event, actions: Observer<EventListAction>) {
        with(event) {
            itemView.event_item_name.text = "Erste Hilfe"
            itemView.event_item_date.text = formatDateTime(this.date)

            setIndicatorView(this.state)
        }
    }

    private fun setIndicatorView(networkState: NetworkState) {
        lateinit var indicatorView: View
        when (networkState) {
            NetworkState.pending -> {
                val progressBar =
                    ProgressBar(itemView.context, null, android.R.attr.progressBarStyleSmall)
                progressBar.isIndeterminate = true
                indicatorView = progressBar
            }
            NetworkState.failed -> {
                val imageView = ImageView(itemView.context)
                imageView.setImageResource(R.drawable.ic_baseline_error_outline_24)
                indicatorView = imageView
            }
            NetworkState.successful -> {
                val imageView = ImageView(itemView.context)
                imageView.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
                indicatorView = imageView
            }
        }

        itemView.loading_indicator_container.removeAllViews()
        indicatorView.visibility = View.VISIBLE
        itemView.loading_indicator_container.addView(indicatorView, 100, 100)
    }
}

// todo: move the following stuff somewhere
fun <T> RecyclerView.Adapter<*>.diffableList(
    compareContent: (T, T) -> Boolean,
    compareId: (T, T) -> Boolean
): ReadWriteProperty<Any?, List<T>> = object : ObservableProperty<List<T>>(emptyList()) {
    override fun afterChange(property: KProperty<*>, oldValue: List<T>, newValue: List<T>) {
        autoNotify(oldValue, newValue, compareContent, compareId)
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T>) {
        super.setValue(thisRef, property, value.map { it })
    }
}

private fun <T> RecyclerView.Adapter<*>.autoNotify(
    old: List<T>, new: List<T>,
    compareContent: (T, T) -> Boolean,
    compareId: (T, T) -> Boolean
) {
    val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return compareId(old[oldItemPosition], new[newItemPosition])
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return compareContent(old[oldItemPosition], new[newItemPosition])
        }

        override fun getOldListSize() = old.size

        override fun getNewListSize() = new.size
    })

    diff.dispatchUpdatesTo(this)
}

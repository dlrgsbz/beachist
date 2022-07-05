package app.beachist.event.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import app.beachist.event.Event
import app.beachist.event.R
import app.beachist.event.databinding.ItemEventBinding
import app.beachist.shared.NetworkState
import app.beachist.shared.recyclerview.diffableList
import app.beachist.shared.date.formatDateTime
import io.reactivex.Observer

internal class EventsListAdapter(
    private val actions: Observer<EventListAction>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<Event> by diffableList(
        compareContent = { a, b -> a == b },
        compareId = { a, b -> a.id == b.id }
    )

    private var _binding: ItemEventBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        _binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventHolder(binding)
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

internal class EventHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(event: Event, actions: Observer<EventListAction>) {
        with(event) {
            binding.eventItemName.text = "Erste Hilfe"
            binding.eventItemDate.text = formatDateTime(this.date)

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

        binding.loadingIndicatorContainer.removeAllViews()
        indicatorView.visibility = View.VISIBLE
        binding.loadingIndicatorContainer.addView(indicatorView, 100, 100)
    }
}

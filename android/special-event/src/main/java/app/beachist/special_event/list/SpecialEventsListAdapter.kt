package app.beachist.special_event.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import app.beachist.shared.NetworkState
import app.beachist.shared.date.DateFormatProvider
import app.beachist.shared.recyclerview.diffableList
import app.beachist.special_event.R
import app.beachist.special_event.databinding.ItemSpecialEventBinding
import app.beachist.special_event.dtos.SpecialEvent
import app.beachist.special_event.dtos.SpecialEventKind


internal class SpecialEventsListAdapter(private val formatProvider: DateFormatProvider) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items: List<SpecialEvent> by diffableList(
        compareContent = { a, b -> a == b },
        compareId = { a, b -> a.id == b.id }
    )

    private var _binding: ItemSpecialEventBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        _binding = ItemSpecialEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return SpecialEventHolder(binding, formatProvider)
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

internal class SpecialEventHolder(
    private var binding: ItemSpecialEventBinding,
    private val formatProvider: DateFormatProvider,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun getString(id: Int) = binding.root.context.getString(id)

    fun bind(specialEvent: SpecialEvent) {
        with(specialEvent) {
            binding.specialEventItemName.text = this.title
            binding.specialEventItemDate.text = this.printableDate(formatProvider)
            binding.specialEventItemType.text =
                if (this.kind == SpecialEventKind.damage) getString(R.string.special_event_damage_title)
                else getString(R.string.special_event_special_title)

            addNetworkIndicator(itemView, this.networkState)
        }
    }

    private fun addNetworkIndicator(view: View, networkState: NetworkState) {
        binding.loadingIndicatorContainer.removeAllViews()

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
        binding.loadingIndicatorContainer.addView(indicatorView, 100, 100)
    }
}

private fun SpecialEvent.printableDate(formatProvider: DateFormatProvider): String {
    return formatProvider.getTimeFormatForDate(date).format(date)
}

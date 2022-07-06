package app.beachist.station_check.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import app.beachist.shared.recyclerview.diffableList
import app.beachist.station_check.R
import app.beachist.station_check.databinding.ItemStationCheckBinding
import app.beachist.station_check.dtos.Field
import app.beachist.station_check.dtos.StateKind
import io.reactivex.Observer


internal class StationCheckListAdapter(private val actions: Observer<StationCheckAction>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items: List<Field> by diffableList(
        compareContent = { a, b -> a == b },
        compareId = { a, b -> a.id == b.id }
    )

    private var _binding: ItemStationCheckBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        _binding = ItemStationCheckBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return StationCheckItemHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StationCheckItemHolder -> holder.bind(items[position], actions)
        }
    }
}

const val CHECKED_PREFIX = "✓ "
const val UNCHECKED_PREFIX = ""

internal class StationCheckItemHolder(private val binding: ItemStationCheckBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(field: Field, actions: Observer<StationCheckAction>) {
        with(field) {
            binding.innerContainer.setPadding(if (parent !== null) 25 else 0, 0, 0, 0)

            binding.stationCheckItemName.text = this.name
            binding.stationCheckItemRequired.text = this.required?.toString() ?: ""

            constraintSet(
                binding.innerContainer,
                binding,
                binding.stationCheckItemNote,
                binding.stationCheckItemName
            ).applyTo(binding.innerContainer)

            setButtonState(this, actions)
        }
    }

    private fun setButtonState(item: Field, actions: Observer<StationCheckAction>) {
        setProblemTypeButtonState(item, actions)

        binding.buttonOkay.setOnClickListener { actions.onNext(StationCheckAction.MarkItemOkay(item.id)) }
        binding.buttonProblem.setOnClickListener { actions.onNext(StationCheckAction.MarkItemNotOkay(item.id)) }

        with(item.entry?.state) {
            markButtons(this)
        }
    }

    private fun setProblemTypeButtonState(
        item: Field,
        actions: Observer<StationCheckAction>,
    ) {
        if (!item.hasProblem()) {
            binding.problemTypeContainer.visibility = View.INVISIBLE
            return
        } else {
            binding.problemTypeContainer.visibility = View.VISIBLE
        }

        binding.buttonTooLittle.setOnClickListener {
            actions.onNext(StationCheckAction.MarkItemTooLittle(item.id))
        }

        binding.buttonBroken.setOnClickListener {
            actions.onNext(StationCheckAction.MarkItemBroken(item.id))
        }

        binding.buttonOther.setOnClickListener {
            actions.onNext(StationCheckAction.MarkItemOther(item.id))
        }

        with(item.entry?.stateKind) {
            markStateKindButtons(this)
        }
    }

    private fun markButtons(state: Boolean?) {
        val okayText = binding.root.context.getString(R.string.station_check_button_okay)
        val notOkayText = binding.root.context.getString(R.string.station_check_button_not_okay)
        val okayPrefix = if (state == true) CHECKED_PREFIX else UNCHECKED_PREFIX
        val notOkayPrefix = if (state == false) CHECKED_PREFIX else UNCHECKED_PREFIX
        binding.buttonOkay.updateText(okayPrefix + okayText)
        binding.buttonProblem.updateText(notOkayPrefix + notOkayText)
    }

    private fun markStateKindButtons(stateKind: StateKind?) {
        val tooLittleText = binding.root.context.getString(R.string.station_check_button_too_little)
        val brokenText = binding.root.context.getString(R.string.station_check_button_broken)
        val otherText = binding.root.context.getString(R.string.station_check_button_other)
        val tooLittlePrefix =
            if (stateKind == StateKind.tooLittle) CHECKED_PREFIX else UNCHECKED_PREFIX
        val otherPrefix = if (stateKind == StateKind.other) CHECKED_PREFIX else UNCHECKED_PREFIX
        val brokenPrefix = if (stateKind == StateKind.broken) CHECKED_PREFIX else UNCHECKED_PREFIX
        binding.buttonTooLittle.updateText(tooLittlePrefix + tooLittleText)
        binding.buttonOther.updateText(otherPrefix + otherText)
        binding.buttonBroken.updateText(brokenPrefix + brokenText)
    }
}

private fun Button.updateText(text: String) {
    this.isPressed = false
    this.text = text
}

private fun Field.hasProblem(): Boolean {
    val entry = this.entry ?: return false

    if (entry.state == null) {
        return false
    }

    return !entry.state
}

private fun Field.constraintSet(
    outerConstraintLayout: ConstraintLayout,
    binding: ItemStationCheckBinding,
    noteView: TextView,
    nameView: TextView,
): ConstraintSet {
    val innerConstraintLayout: ConstraintLayout = binding.buttonContainer

    val constraintSet = ConstraintSet()
    constraintSet.clone(outerConstraintLayout)
    constraintSet.clear(innerConstraintLayout.id, ConstraintSet.TOP)
    constraintSet.clear(innerConstraintLayout.id, ConstraintSet.BOTTOM)

    if (this.note !== null) {
        noteView.visibility = View.VISIBLE
        noteView.text = this.note

        constraintSet.connect(
            innerConstraintLayout.id,
            ConstraintSet.TOP,
            noteView.id,
            ConstraintSet.BOTTOM,
            1
        )
    } else {
        noteView.visibility = View.INVISIBLE
        noteView.text = ""

        constraintSet.connect(
            innerConstraintLayout.id,
            ConstraintSet.TOP,
            nameView.id,
            ConstraintSet.BOTTOM,
            1
        )
    }
    return constraintSet
}

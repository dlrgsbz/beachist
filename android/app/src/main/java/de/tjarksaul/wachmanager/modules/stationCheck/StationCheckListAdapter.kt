package de.tjarksaul.wachmanager.modules.stationCheck

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import de.tjarksaul.wachmanager.R
import app.beachist.shared.recyclerview.diffableList
import de.tjarksaul.wachmanager.dtos.Field
import de.tjarksaul.wachmanager.dtos.StateKind
import io.reactivex.Observer


internal class StationCheckListAdapter(private val actions: Observer<StationCheckAction>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items: List<Field> by diffableList(
        compareContent = { a, b -> a == b },
        compareId = { a, b -> a.id == b.id }
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_station_check_item, parent, false)

        return StationCheckItemHolder(view)
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

internal class StationCheckItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(field: Field, actions: Observer<StationCheckAction>) {
        with(field) {
            val nameView: TextView = itemView.findViewById(R.id.station_check_item_name)
            val requiredView: TextView = itemView.findViewById(R.id.station_check_item_required)
            val noteView: TextView = itemView.findViewById(R.id.station_check_item_note)

            val outerConstraintLayout: ConstraintLayout =
                itemView.findViewById(R.id.inner_container)

            outerConstraintLayout.setPadding(if (parent !== null) 25 else 0, 0, 0, 0)

            nameView.text = this.name
            requiredView.text = this.required?.toString() ?: ""

            constraintSet(outerConstraintLayout, itemView, noteView, nameView)
                .applyTo(outerConstraintLayout)

            setButtonState(itemView, this, actions)
        }
    }

    private fun setButtonState(view: View, item: Field, actions: Observer<StationCheckAction>) {
        setProblemTypeButtonState(view, item, actions)

        val okayButton: Button = view.findViewById(R.id.button_okay)
        val problemButton: Button = view.findViewById(R.id.button_problem)

        okayButton.setOnClickListener { actions.onNext(StationCheckAction.MarkItemOkay(item.id)) }
        problemButton.setOnClickListener { actions.onNext(StationCheckAction.MarkItemNotOkay(item.id)) }

        with(item.entry?.state) {
            markButtons(this, okayButton, problemButton)
        }
    }

    private fun setProblemTypeButtonState(
        view: View,
        item: Field,
        actions: Observer<StationCheckAction>
    ) {
        val problemGroup: LinearLayout = view.findViewById(R.id.problem_type_container)
        if (!item.hasProblem()) {
            problemGroup.visibility = View.INVISIBLE
            return
        } else {
            problemGroup.visibility = View.VISIBLE
        }

        val tooLittleButton: Button = view.findViewById(R.id.button_tooLittle)
        val brokenButton: Button = view.findViewById(R.id.button_broken)
        val otherButton: Button = view.findViewById(R.id.button_other)

        tooLittleButton.setOnClickListener {
            actions.onNext(StationCheckAction.MarkItemTooLittle(item.id))
        }

        brokenButton.setOnClickListener {
            actions.onNext(StationCheckAction.MarkItemBroken(item.id))
        }

        otherButton.setOnClickListener {
            actions.onNext(StationCheckAction.MarkItemOther(item.id))
        }

        with(item.entry?.stateKind) {
            markStateKindButtons(this, tooLittleButton, otherButton, brokenButton)
        }
    }

    private fun markButtons(state: Boolean?, okayButton: Button, problemButton: Button) {
        val okayPrefix = if (state == true) CHECKED_PREFIX else UNCHECKED_PREFIX
        val notOkayPrefix = if (state == false) CHECKED_PREFIX else UNCHECKED_PREFIX
        okayButton.updateText("${okayPrefix}In Ordnung")
        problemButton.updateText("${notOkayPrefix}Problem")
    }

    private fun markStateKindButtons(
        stateKind: StateKind?,
        tooLittleButton: Button,
        otherButton: Button,
        brokenButton: Button
    ) {
        val tooLittlePrefix =
            if (stateKind == StateKind.tooLittle) CHECKED_PREFIX else UNCHECKED_PREFIX
        val otherPrefix = if (stateKind == StateKind.other) CHECKED_PREFIX else UNCHECKED_PREFIX
        val brokenPrefix = if (stateKind == StateKind.broken) CHECKED_PREFIX else UNCHECKED_PREFIX
        tooLittleButton.updateText("${tooLittlePrefix}zu wenig")
        otherButton.updateText("${otherPrefix}anderes")
        brokenButton.updateText("${brokenPrefix}defekt")
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
    itemView: View,
    noteView: TextView,
    nameView: TextView
): ConstraintSet {
    val innerConstraintLayout: ConstraintLayout = itemView.findViewById(R.id.button_container)

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

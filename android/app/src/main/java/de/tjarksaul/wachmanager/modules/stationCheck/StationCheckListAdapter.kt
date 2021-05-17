package de.tjarksaul.wachmanager.modules.stationCheck

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.dtos.Field
import de.tjarksaul.wachmanager.dtos.StateKind


class StationCheckListAdapter(
    context: Context,
    var items: List<Field>,
    private val itemClickCallback: ((String, Boolean, StateKind?, Int?, String?) -> Unit)?,
    val parentActivity: Activity
) : ArrayAdapter<Field>(context, 0, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_station_check_item, parent, false)

        val item = getItem(position) ?: return view


        val nameView: TextView = view.findViewById(R.id.station_check_item_name)
        val requiredView: TextView = view.findViewById(R.id.station_check_item_required)
        val noteView: TextView = view.findViewById(R.id.station_check_item_note)

        val outerConstraintLayout: ConstraintLayout = view.findViewById(R.id.inner_container)
        if (item.parent !== null) {
            outerConstraintLayout.setPadding(25, 0, 0, 0)
        } else {
            outerConstraintLayout.setPadding(0, 0, 0, 0)
        }

        nameView.text = item.name
        requiredView.text = item.required?.toString() ?: ""

        val innerConstraintLayout: ConstraintLayout = view.findViewById(R.id.button_container)

        val constraintSet = ConstraintSet()
        constraintSet.clone(outerConstraintLayout)
        constraintSet.clear(innerConstraintLayout.id, ConstraintSet.TOP)
        constraintSet.clear(innerConstraintLayout.id, ConstraintSet.BOTTOM)

        if (item.note !== null) {
            noteView.visibility = View.VISIBLE
            noteView.text = item.note

            constraintSet.connect(
                innerConstraintLayout.id,
                ConstraintSet.TOP,
                noteView.id,
                ConstraintSet.BOTTOM,
                1
            );
        } else {
            noteView.visibility = View.INVISIBLE
            noteView.text = ""

            constraintSet.connect(
                innerConstraintLayout.id,
                ConstraintSet.TOP,
                nameView.id,
                ConstraintSet.BOTTOM,
                1
            );
        }

        constraintSet.applyTo(outerConstraintLayout)

        setButtonState(view, item)

        return view
    }

    private fun setButtonState(view: View, item: Field) {
        setProblemTypeButtonState(view, item)

        val okayButton: Button = view.findViewById(R.id.button_okay)
        val problemButton: Button = view.findViewById(R.id.button_problem)

        okayButton.setOnClickListener { itemClickCallback?.invoke(item.id, true, null, null, null) }
        problemButton.setOnClickListener {
            itemClickCallback?.invoke(
                item.id,
                false,
                null,
                null,
                null
            )
        }

        if (item.entry == null || item.entry?.state == null) {
            okayButton.isPressed = false
            okayButton.text = "In Ordnung"
            problemButton.isPressed = false
            problemButton.text = "Problem"


            return
        }

        if (item.entry?.state != null && item.entry!!.state!!) {
            okayButton.isPressed = true
            okayButton.text = "✓ In Ordnung"
            problemButton.isPressed = false
            problemButton.text = "Problem"
        } else {
            okayButton.isPressed = false
            okayButton.text = "In Ordnung"
            problemButton.isPressed = true
            problemButton.text = "✓ Problem"
        }
    }

    private fun setProblemTypeButtonState(view: View, item: Field) {
        val problemGroup: LinearLayout = view.findViewById(R.id.problem_type_container)
        if (item.entry == null || (item.entry?.state != null && item.entry!!.state!!)) {
            problemGroup.visibility = View.INVISIBLE
        } else {
            problemGroup.visibility = View.VISIBLE
        }

        val tooLittleButton: Button = view.findViewById(R.id.button_tooLittle)
        val brokenButton: Button = view.findViewById(R.id.button_broken)
        val otherButton: Button = view.findViewById(R.id.button_other)

        tooLittleButton.setOnClickListener {
            // show modal to ask for amount

            if (item.required != null && item.required == 1) {
                itemClickCallback?.invoke(item.id, false, StateKind.tooLittle, 0, null)
            } else {
                val builder: AlertDialog.Builder = AlertDialog.Builder(parentActivity)
                builder.setTitle("Wie viel ist noch vorhanden?")

                val input = EditText(parentActivity)
                input.inputType = InputType.TYPE_CLASS_NUMBER
                input.setText("0")
                builder.setView(input)

                builder.setPositiveButton(
                    "OK"
                ) { _, _ ->
                    val amount = try {
                        input.text.toString().toInt()
                    } catch (e: NumberFormatException) {
                        0
                    }
                    itemClickCallback?.invoke(item.id, false, StateKind.tooLittle, amount, null)
                }
                builder.setNegativeButton(
                    "Cancel"
                ) { dialog, _ -> dialog.cancel() }

                builder.show()
            }
        }

        brokenButton.setOnClickListener {
            showNoteInput(item.id, StateKind.broken)
        }

        otherButton.setOnClickListener {
            showNoteInput(item.id, StateKind.other)
        }

        item.entry?.stateKind?.let {
            when (it) {
                StateKind.broken -> {
                    tooLittleButton.isPressed = false
                    tooLittleButton.text = "zu wenig"
                    otherButton.isPressed = false
                    otherButton.text = "anderes"
                    brokenButton.isPressed = true
                    brokenButton.text = "✓ defekt"
                }
                StateKind.tooLittle -> {
                    tooLittleButton.isPressed = true
                    tooLittleButton.text = "✓ zu wenig"
                    otherButton.isPressed = false
                    otherButton.text = "anderes"
                    brokenButton.isPressed = false
                    brokenButton.text = "defekt"
                }
                StateKind.other -> {
                    tooLittleButton.isPressed = false
                    tooLittleButton.text = "zu wenig"
                    otherButton.isPressed = true
                    otherButton.text = "✓ anderes"
                    brokenButton.isPressed = false
                    brokenButton.text = "defekt"
                }
            }
        }
    }

    private fun showNoteInput(itemId: String, resultStateKind: StateKind) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(parentActivity)
        builder.setTitle("Bitte das Problem genauer beschreiben")

        val input = EditText(parentActivity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton(
            "OK"
        ) { _, _ ->
            val note = input.text.toString()
            itemClickCallback?.invoke(itemId, false, resultStateKind, null, note)
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
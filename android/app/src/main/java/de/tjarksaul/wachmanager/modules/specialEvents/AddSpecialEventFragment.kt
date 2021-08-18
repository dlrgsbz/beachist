package de.tjarksaul.wachmanager.modules.specialEvents

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.api.HTTPRepo
import de.tjarksaul.wachmanager.api.RequestCallback
import de.tjarksaul.wachmanager.dtos.IdResponse
import de.tjarksaul.wachmanager.dtos.NetworkState
import de.tjarksaul.wachmanager.dtos.SpecialEventKind
import de.tjarksaul.wachmanager.modules.base.BaseFragment


class AddSpecialEventFragment(val viewModel: SpecialEventsViewModel) : BaseFragment(),
    View.OnClickListener, TextWatcher {
    private lateinit var titleText: EditText
    private lateinit var noteText: EditText
    private lateinit var notifierText: EditText
    private lateinit var radioGroup: RadioGroup

    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var nameInputLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_add_special_event, container, false)

        titleText = root.findViewById(R.id.editTextTextSpecialTitle)
        titleText.addTextChangedListener(this)
        noteText = root.findViewById(R.id.editTextSpecialDetails)
        notifierText = root.findViewById(R.id.editTextSpecialNotifier)
        notifierText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val notifier = notifierText.text.toString().trim()

                if (notifier == "" || notifier.length < 2) {
                    nameInputLayout.isErrorEnabled = true
                    nameInputLayout.error = "Bitte mindestens 2 Zeichen eingeben"
                } else {
                    nameInputLayout.isHintEnabled = true
                    nameInputLayout.isErrorEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        radioGroup = root.findViewById(R.id.specialRadioGroup)

        titleInputLayout = root.findViewById(R.id.textInputLayoutSpecialTitle)

        titleInputLayout.isHintEnabled = true
        titleInputLayout.hint = "Kurze Beschreibung"

        nameInputLayout = root.findViewById(R.id.textInputLayoutSpecialNotifier)
        nameInputLayout.isHintEnabled = true
        nameInputLayout.hint = "Meldende*r"

        root.findViewById<FloatingActionButton>(R.id.save_special_event_fab)
            .setOnClickListener(this)

        return root
    }

    override fun onClick(v: View?) {
        var error = false
        val title = titleText.text.toString().trim()

        if (title == "" || title.length < 8) {
            error = true
        }

        val notifier = notifierText.text.toString().trim()

        if (notifier == "" || notifier.length < 2) {
            error = true
        }

        val selectedButtonId = radioGroup.checkedRadioButtonId
        val lastButton = radioGroup.getChildAt(radioGroup.childCount - 1) as RadioButton
        if (selectedButtonId == -1) {
            lastButton.error = "Bitte Typ der Meldung auswählen"
            error = true
        } else {
            lastButton.error = null
        }

        // todo: body validation?

        if (error) {
            Snackbar.make(radioGroup, "Bitte alle Felder ausfüllen", 5000).show()
            return
        }

        val kind =
            if (selectedButtonId == R.id.button_damage) SpecialEventKind.damage else SpecialEventKind.event


        val note = noteText.text.toString().trim()
        val id = viewModel.addEntry(title, note, notifier, kind)

        val stationId = getStoredStationId()
        if (stationId === null) {
            return
        }

        HTTPRepo().createSpecialEvent(
            stationId,
            title,
            note,
            notifier,
            kind,
            callback = object : RequestCallback<IdResponse> {
                override fun onFailure() {
                    viewModel.updateEntryFromNetwork(id, id, NetworkState.failed)
                }

                override fun onResponse(response: IdResponse) {
                    viewModel.updateEntryFromNetwork(id, response.id, NetworkState.successful)
                }
            })

        val imm: InputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(titleText.rootView.windowToken, 0);
        requireActivity().supportFragmentManager.popBackStack(
            "ShowAddSpecialEventsView",
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    override fun afterTextChanged(s: Editable?) {
        val title = titleText.text.toString().trim()

        if (title == "" || title.length < 8) {
            titleInputLayout.isErrorEnabled = true
            titleInputLayout.error = "Bitte mindestens 8 Zeichen eingeben"
        } else {
            titleInputLayout.isHintEnabled = true
            titleInputLayout.isErrorEnabled = false
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}
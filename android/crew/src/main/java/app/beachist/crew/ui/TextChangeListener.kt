package app.beachist.crew.ui

import android.text.Editable
import android.text.TextWatcher

abstract class TextChangeListener : TextWatcher {
    override fun beforeTextChanged(
        s: CharSequence,
        start: Int,
        count: Int,
        after: Int
    ) {
    }

    override fun onTextChanged(
        s: CharSequence,
        start: Int,
        before: Int,
        count: Int
    ) {
    }

    override fun afterTextChanged(s: Editable) {
        this.onTextChanged(s)
    }

    abstract fun onTextChanged(s: Editable?)

}
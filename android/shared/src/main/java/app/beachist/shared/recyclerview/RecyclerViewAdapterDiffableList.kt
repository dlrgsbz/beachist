package app.beachist.shared.recyclerview

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

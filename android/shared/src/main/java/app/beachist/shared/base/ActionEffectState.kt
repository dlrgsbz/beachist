package app.beachist.shared.base

import kotlin.random.Random

//The below are marker interfaces to easily identify the correct types.

/**
 * The actions that may be done to the viewmodel.
 */
abstract class ViewModelAction {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }
}

/**
 * The States the Viewmodel handles.
 */
interface ViewModelState

/**
 * The One-Time effects that may occur in the UI.
 * (Example: show a toast)
 */
abstract class ViewModelEffect {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }
}

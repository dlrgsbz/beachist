package app.beachist.shared.base

//The below are marker interfaces to easily identify the correct types.

/**
 * The actions that may be done to the viewmodel.
 */
interface ViewModelAction

/**
 * The States the Viewmodel handles.
 */
interface ViewModelState

/**
 * The One-Time effects that may occur in the UI.
 * (Example: show a toast)
 */
interface ViewModelEffect

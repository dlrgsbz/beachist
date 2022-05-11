import { runInAction } from 'mobx'

export type AsyncStateStatus = 'idle' | 'pending' | 'success' | 'error'

export interface AsyncState<T> {
  /**
   * Status
   * - pending: actions is in-flight
   * - idle: refers to the initial state
   */
  status: AsyncStateStatus
  error: Error | null
  data: T
}

interface ErrorState<TValue> {
  readonly _type: 'error'
  readonly error: TValue
}
interface SuccessState<TValue> {
  readonly _type: 'success'
  readonly data: TValue
}
export type Result<TLeftValue, TRightValue> = ErrorState<TLeftValue> | SuccessState<TRightValue>

export const error = <TValue>(value: TValue): ErrorState<TValue> => ({ _type: 'error', error: value })
export const success = <TValue>(value: TValue): SuccessState<TValue> => ({ _type: 'success', data: value })
export const isSuccessful = <L, R>(value: Result<L, R>): value is SuccessState<R> => value._type === 'success'
export const isError = <L, R>(value: Result<L, R>): value is ErrorState<L> => value._type === 'error'

/**
 * Initializes the Async state with the given value
 */
export const createAsyncState = <T>(defaultValue: T): AsyncState<T> => ({
  status: 'idle',
  error: null,
  data: defaultValue,
})

/**
 * Runs a Mobx action using Async state
 *
 * @param asyncState current state
 * @param fn async function to be executed
 */
export const runWithAsyncState = async <T, E = Error>(
  asyncState: AsyncState<T>,
  fn: () => Promise<T> | T,
): Promise<Result<E, T>> => {
  asyncState.status = 'pending'

  try {
    const result = await fn()
    runInAction(() => {
      asyncState.data = result
      asyncState.error = null
      asyncState.status = 'success'
    })
    return success(result)
  } catch (e) {
    runInAction(() => {
      asyncState.error = e as Error
      asyncState.status = 'error'
    })
    return error(e as E)
  }
}

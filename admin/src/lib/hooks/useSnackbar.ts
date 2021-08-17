import { OptionsObject, useSnackbar as useNotiStackbar } from 'notistack'
import { useMemo } from 'react'

export const useSnackbar = () => {
  const { enqueueSnackbar } = useNotiStackbar()

  return useMemo(
    () => ({
      successSnackbar: (msg: string, opts?: OptionsObject) => enqueueSnackbar(msg, { variant: 'success', ...opts }),
      errorSnackbar: (msg: string, opts?: OptionsObject) => enqueueSnackbar(msg, { variant: 'error', ...opts }),
      warningSnackbar: (msg: string, opts?: OptionsObject) => enqueueSnackbar(msg, { variant: 'warning', ...opts }),
    }),
    [enqueueSnackbar],
  )
}

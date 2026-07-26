import { useId } from 'react'

export const useGeneratedId = (prefix: string) => {
  const id = useId()
  return prefix + id
}

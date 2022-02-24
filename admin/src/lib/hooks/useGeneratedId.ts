import { useRef } from 'react'

export const useGeneratedId = (prefix: string) => {
    const { current } = useRef(prefix + (Math.random().toString(36) + '00000000000000000').slice(2, 7))
    return current
}

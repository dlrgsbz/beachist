import { useEffect } from 'react'

const version = (() => {
  return import.meta.env.VITE_APP_VERSION ?? 'development'
})()

const buildDate = (() => {
  return import.meta.env.VITE_BUILD_DATE ?? '' + new Date()
})()

export const useInfoMessage = () => {
  useEffect(() => {
    console.log(
      `%c Beachist %c Version ${version} ${buildDate}`,
      'background: #222; color: #bada55; padding: 4px 8px; border-radius: 4px; font-weight: bold;',
      'color: inherit;',
    )
  }, [])
}

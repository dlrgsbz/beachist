import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import commonDE from 'locales/de/common.json'

const resources = {
  de: {
    translation: {
      ...commonDE,
    },
  },
}

export const languages = Object.keys(resources)

// todo: handle error
// eslint-disable-next-line @typescript-eslint/no-floating-promises
i18n.use(initReactI18next).init({
  resources,
  fallbackLng: 'de',
  debug: process.env.NODE_ENV === 'development',
  lng: 'de',

  interpolation: {
    escapeValue: false, // not needed for react as it escapes by default
  },
})

export default i18n

const LOCAL_STORAGE_KEY_TOKEN = '__beachist__token'

export function getToken(): string | null {
  return window.localStorage.getItem(LOCAL_STORAGE_KEY_TOKEN);
}

export function removeTokens() {
  window.localStorage.removeItem(LOCAL_STORAGE_KEY_TOKEN);
}

export function setToken(token: string) {
  window.localStorage.setItem(LOCAL_STORAGE_KEY_TOKEN, token);
}

import { TextDecoder, TextEncoder } from 'util'

// jsdom does not provide TextEncoder/TextDecoder, which react-router expects.
const g = globalThis as unknown as {
  TextEncoder?: typeof TextEncoder
  TextDecoder?: typeof TextDecoder
  IS_REACT_ACT_ENVIRONMENT?: boolean
}

if (typeof g.TextEncoder === 'undefined') {
  g.TextEncoder = TextEncoder
}

if (typeof g.TextDecoder === 'undefined') {
  g.TextDecoder = TextDecoder
}

// Tell React this is an act()-aware testing environment.
g.IS_REACT_ACT_ENVIRONMENT = true

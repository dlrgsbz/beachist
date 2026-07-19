// Polyfills required for dependencies (e.g. react-router v7) that expect
// TextEncoder/TextDecoder, which are not provided by the jsdom environment
// bundled with react-scripts' Jest setup.
import { TextDecoder, TextEncoder } from 'util'

if (typeof global.TextEncoder === 'undefined') {
  // @ts-expect-error - assigning Node's implementation onto the global scope
  global.TextEncoder = TextEncoder
}

if (typeof global.TextDecoder === 'undefined') {
  // @ts-expect-error - assigning Node's implementation onto the global scope
  global.TextDecoder = TextDecoder
}

// Tell React this is an act()-aware testing environment.
// @ts-expect-error - React reads this flag off the global scope
global.IS_REACT_ACT_ENVIRONMENT = true

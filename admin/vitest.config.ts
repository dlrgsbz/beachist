import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import svgr from 'vite-plugin-svgr'
import tsconfigPaths from 'vite-tsconfig-paths'

export default defineConfig({
  // Cast required because Vitest bundles a different Vite version than the
  // project's Vite 8, so the plugin `Plugin` types are structurally distinct.
  plugins: [tsconfigPaths(), svgr(), react()] as never,
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/setupTests.ts'],
  },
})

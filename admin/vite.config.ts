import { reactRouter } from '@react-router/dev/vite'
import svgr from 'vite-plugin-svgr'
import { defineConfig } from 'vite'

const proxyTarget = process.env.API_PROXY_TARGET || 'http://localhost:8001'

export default defineConfig({
  plugins: [svgr(), reactRouter()],
  resolve: {
    tsconfigPaths: true,
  },
  server: {
    host: true,
    allowedHosts: true,
    proxy: {
      '/api': { target: proxyTarget, changeOrigin: true },
      '/auth': { target: proxyTarget, changeOrigin: true },
    },
  },
})

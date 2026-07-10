// dev-only proxy for running the admin app in Docker.
// Routes API calls to the backend container on the shared docker network.

module.exports = function (app) {
  const target = process.env.API_PROXY_TARGET || 'http://beachist-api:8000'
  if (!target) {
    return;
  }

  const { createProxyMiddleware } = require('http-proxy-middleware')

  app.use('/api', createProxyMiddleware({ target, changeOrigin: true }))
  app.use('/auth', createProxyMiddleware({ target, changeOrigin: true }))
}

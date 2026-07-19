import { type RouteConfig, index, layout, route } from '@react-router/dev/routes'

export default [
  index('routes/home.tsx'),
  route('login', 'routes/urlLogin.tsx'),
  layout('layouts/protected.tsx', [
    route('wachfuehrer', 'routes/wachfuehrer.tsx', { id: 'wachfuehrer' }),
    route('wachfuehrer/:date', 'routes/wachfuehrer.tsx', { id: 'wachfuehrer-date' }),
    route('wachfuehrer/:date/:tab', 'routes/wachfuehrer.tsx', { id: 'wachfuehrer-date-tab' }),
    route('admin/station', 'routes/adminStation.tsx'),
    route('admin/items/*', 'routes/adminItems.tsx'),
  ]),
  route('*', 'routes/catchAll.tsx'),
] satisfies RouteConfig

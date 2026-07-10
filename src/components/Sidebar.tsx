import { Link, useLocation } from 'react-router-dom'

export function Sidebar() {
  const location = useLocation()

  const navItems = [
    { name: 'Home', path: '/', icon: 'home' },
    { name: 'Search', path: '/search', icon: 'search' },
    { name: 'Radio', path: '/radio', icon: 'radio' },
    { name: 'Playlist', path: '/library', icon: 'library_music' },
    { name: 'Premium', path: '/premium', icon: 'workspace_premium' },
  ]

  return (
    <aside className="h-screen w-64 fixed left-0 top-0 flex flex-col bg-surface-container-lowest border-r border-outline-variant z-40">
      <div className="flex flex-col h-full py-base">
        {/* Brand Logo Area */}
        <div className="px-6 py-8">
          <Link to="/" className="block w-10 h-10 overflow-hidden rounded-lg">
            <img src="https://lh3.googleusercontent.com/aida-public/AB6AXuCRLubS6wmOz-SvzQjVcy8BLu4328t6BsxBCEnfacK37lvF5pgYwTowU1G0tH63lj-9I3VzpYkf88SR0FPb6mTgGijaK_b5sFALTtFhozKc_Ft0ZoP55G_Qb6Wo1uvfaY1dPwNbKIixAJe8CG0lMQwvNfynpjNp9u_M0Tq0PJQ2YbpbWshBm_dXZ4yKupMIJo5TQzSA4m2PPr7twCDS7EZfkzKNIiQilBsa-mR8-vbweAi4Vp8_2gXjDuA-244GX-OWH_I" alt="Logo" className="w-full h-full object-contain" />
          </Link>
        </div>
        {/* Main Navigation */}
        <nav className="flex-1 px-4 space-y-2">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex items-center gap-4 px-4 py-3 rounded-lg font-medium transition-colors group ${
                  isActive
                    ? 'text-primary font-bold border-r-4 border-primary bg-surface-container-high/50'
                    : 'text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface'
                }`}
              >
                <span
                  className="material-symbols-outlined"
                  style={isActive ? { fontVariationSettings: '"FILL" 1' } : {}}
                >
                  {item.icon}
                </span>
                <span className="font-title-md">{item.name}</span>
              </Link>
            )
          })}
        </nav>
        {/* Bottom Side Nav Links */}
        <div className="px-4 pb-8 space-y-2 border-t border-outline-variant pt-4">
        </div>
      </div>
    </aside>
  )
}

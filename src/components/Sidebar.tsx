import { Link, useLocation } from 'react-router-dom'

export function Sidebar() {
  const location = useLocation()

  const navItems = [
    { name: 'Home', path: '/', icon: 'home' },
    { name: 'Search', path: '/search', icon: 'search' },
    { name: 'Radio', path: '/radio', icon: 'radio' },
  ]

  return (
    <aside className="h-screen w-60 fixed left-0 top-0 flex flex-col bg-[#0a0a0a] border-r border-[#1a1a1a] z-40">
      <div className="flex flex-col h-full pt-12">
        {/* Brand Logo Area */}
        <div className="px-6 py-6 mb-4 flex items-center gap-3">
          <span className="text-2xl">🍉</span>
          <span className="text-xl font-bold text-[#ff3b3b] tracking-wide">Watermelon</span>
        </div>
        
        {/* Main Navigation */}
        <nav className="flex-1 space-y-1 pr-4">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex items-center gap-4 px-6 py-3 rounded-r-xl font-medium transition-colors group ${
                  isActive
                    ? 'text-[#ff3b3b] border-l-4 border-[#ff3b3b] bg-[#1a1a1a]'
                    : 'text-gray-400 hover:text-white border-l-4 border-transparent hover:bg-[#111]'
                }`}
              >
                <span
                  className="material-symbols-outlined text-[22px]"
                  style={isActive ? { fontVariationSettings: '"FILL" 1' } : {}}
                >
                  {item.icon}
                </span>
                <span className="font-semibold text-sm">{item.name}</span>
              </Link>
            )
          })}
        </nav>
      </div>
    </aside>
  )
}

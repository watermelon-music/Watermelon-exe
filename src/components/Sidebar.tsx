import { NavLink } from 'react-router-dom'
import { Home, Search, Library, Trophy, Crown, User } from 'lucide-react'
import { useAuthStore } from '../lib/store'
import { getInitials } from '../lib/utils'

const navItems = [
  { to: '/', icon: Home, label: 'Home' },
  { to: '/search', icon: Search, label: 'Search' },
  { to: '/library', icon: Library, label: 'Library' },
  { to: '/leaderboard', icon: Trophy, label: 'Leaderboard' },
  { to: '/premium', icon: Crown, label: 'Premium' },
]

export function Sidebar() {
  const { user, profile } = useAuthStore()
  const isPremium = profile?.plan !== 'FREE'

  return (
    <aside className="sidebar">
      <nav className="sidebar-nav">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) => `sidebar-item${isActive ? ' active' : ''}`}
          >
            <Icon size={18} />
            <span>{label}</span>
            {label === 'Premium' && isPremium && (
              <span className="premium-badge">PRO</span>
            )}
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-bottom">
        <NavLink
          to="/profile"
          className={({ isActive }) => `sidebar-user${isActive ? ' active' : ''}`}
        >
          <div className="user-avatar">
            {profile?.avatar_url
              ? <img src={profile.avatar_url} alt="avatar" />
              : <span>{getInitials(profile?.display_name ?? null, user?.email ?? 'W')}</span>
            }
          </div>
          <div className="user-info">
            <span className="user-name">
              {profile?.display_name || user?.email?.split('@')[0] || 'User'}
            </span>
            <span className="user-plan">{profile?.plan || 'FREE'}</span>
          </div>
        </NavLink>
      </div>
    </aside>
  )
}

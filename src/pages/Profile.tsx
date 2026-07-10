import { useAuthStore } from '../lib/store'
import { supabase } from '../lib/supabase'
import { getInitials, formatHours } from '../lib/utils'

export function ProfilePage() {
  const { user, profile } = useAuthStore()

  const handleLogout = async () => {
    await supabase.auth.signOut()
  }

  if (!user) return null

  return (
    <div className="main-content">
      <div className="page-header">
        <h1 className="page-title">Profile</h1>
        <p className="page-sub">Your account and stats</p>
      </div>

      <div className="profile-hero">
        <div className="profile-avatar">
          {profile?.avatar_url
            ? <img src={profile.avatar_url} alt="avatar" />
            : <span>{getInitials(profile?.display_name ?? null, user.email ?? 'W')}</span>
          }
        </div>
        <div>
          <div className="profile-name">{profile?.display_name || user.email?.split('@')[0]}</div>
          <div className="profile-email">{user.email}</div>
          <div className="profile-plan">{profile?.plan || 'FREE'}</div>
          <button className="logout-btn" onClick={handleLogout}>Sign Out</button>
        </div>
      </div>

      <div className="profile-stats">
        <div className="profile-stat">
          <div className="profile-stat-val">{formatHours(profile?.hours_listened || 0)}</div>
          <div className="profile-stat-label">Hours Listened</div>
        </div>
        <div className="profile-stat">
          <div className="profile-stat-val">{profile?.songs_played || 0}</div>
          <div className="profile-stat-label">Songs Played</div>
        </div>
        <div className="profile-stat">
          <div className="profile-stat-val">{profile?.xp_level || 1}</div>
          <div className="profile-stat-label">XP Level</div>
        </div>
      </div>
    </div>
  )
}

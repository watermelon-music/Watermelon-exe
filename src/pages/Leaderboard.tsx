import { useState, useEffect } from 'react'
import { api, type LeaderboardUser } from '../lib/api'
import { getInitials, getRankEmoji, formatHours } from '../lib/utils'

export function Leaderboard() {
  const [users, setUsers] = useState<LeaderboardUser[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.leaderboard(50).then(data => {
      setUsers(data || [])
      setLoading(false)
    }).catch(() => setLoading(false))
  }, [])

  const medals = ['🥇', '🥈', '🥉']

  return (
    <div className="main-content">
      <div className="page-header">
        <h1 className="page-title">🏆 Leaderboard</h1>
        <p className="page-sub">Top listeners ranked by hours played</p>
      </div>

      {loading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : users.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">🏆</div>
          <div className="empty-text">No data yet</div>
        </div>
      ) : (
        <div className="leaderboard-list">
          {users.map((user, i) => (
            <div key={i} className="lb-row">
              <div className="lb-rank">
                {i < 3 ? medals[i] : `#${i + 1}`}
              </div>
              <div className="lb-avatar">
                {user.avatar_url
                  ? <img src={user.avatar_url} alt="avatar" />
                  : <span>{getInitials(user.display_name, user.email)}</span>
                }
              </div>
              <div>
                <div className="lb-name">
                  {getRankEmoji(user.rank_tier)} {user.display_name || user.email?.split('@')[0]}
                </div>
                <div className="lb-sub">
                  {user.songs_played || 0} songs · Lvl {user.xp_level || 1}
                </div>
              </div>
              <div className="lb-stat">
                <div className="lb-stat-val">{formatHours(user.hours_listened || 0)}</div>
                <div className="lb-stat-label">played</div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

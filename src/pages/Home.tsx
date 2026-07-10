import { useState, useEffect } from 'react'
import { api, type Song } from '../lib/api'
import { SongCard, SongRow } from '../components/SongRow'
import { formatNumber } from '../lib/utils'

export function Home() {
  const [recommendations, setRecommendations] = useState<Song[]>([])
  const [stats, setStats] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      api.search('latest hits').catch(() => []),
      api.stats().catch(() => null),
    ]).then(([recs, s]) => {
      setRecommendations(recs)
      setStats(s)
      setLoading(false)
    })
  }, [])

  const trending = recommendations.slice(0, 6)
  const recent = recommendations.slice(6, 16)

  return (
    <div className="main-content">
      <div className="page-header">
        <h1 className="page-title">Good vibes 🎵</h1>
        <p className="page-sub">Stream millions of songs, free</p>
      </div>

      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-val">{formatNumber(stats.totalUsers)}</div>
            <div className="stat-label">Total Users</div>
          </div>
          <div className="stat-card">
            <div className="stat-val">{formatNumber(stats.totalStreams)}</div>
            <div className="stat-label">Total Streams</div>
          </div>
          <div className="stat-card">
            <div className="stat-val">{stats.githubStars}</div>
            <div className="stat-label">GitHub Stars</div>
          </div>
          <div className="stat-card">
            <div className="stat-val">{stats.apkDownloads}</div>
            <div className="stat-label">APK Downloads</div>
          </div>
          <div className="stat-card">
            <div className="stat-val">{stats.latestReleaseTag}</div>
            <div className="stat-label">Latest Version</div>
          </div>
        </div>
      )}

      {loading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : (
        <>
          {trending.length > 0 && (
            <div className="section">
              <div className="section-title">🔥 Trending Now</div>
              <div className="songs-grid">
                {trending.map(song => (
                  <SongCard key={song.id} song={song} queue={trending} />
                ))}
              </div>
            </div>
          )}

          {recent.length > 0 && (
            <div className="section">
              <div className="section-title">🎵 Recommended For You</div>
              {recent.map(song => (
                <SongRow key={song.id} song={song} queue={recent} />
              ))}
            </div>
          )}

          {recommendations.length === 0 && (
            <div className="empty">
              <div className="empty-icon">🎶</div>
              <div className="empty-text">Search for songs to start listening!</div>
            </div>
          )}
        </>
      )}
    </div>
  )
}

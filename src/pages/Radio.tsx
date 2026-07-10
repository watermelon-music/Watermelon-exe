import { useState } from 'react'
import { api, Song } from '../lib/api'
import { usePlayerStore } from '../lib/store'
import { SongRow } from '../components/SongRow'

const PREDEFINED_STATIONS = [
  { id: 'global-hits', name: 'Global Top Hits', desc: 'The biggest songs right now', seed: { title: 'Global Top Hits 2024', artist: 'Various Artists' } },
  { id: 'us-pop', name: 'US Pop Radio', desc: 'Top 40 hits from the USA', seed: { title: 'US Pop Hits', artist: 'Various Artists', genre: 'Pop' } },
  { id: 'bollywood', name: 'Bollywood Mix', desc: 'Latest Indian hits', seed: { title: 'Latest Bollywood Songs', artist: 'Various', language: 'Hindi' } },
  { id: 'kpop', name: 'K-Pop Radio', desc: 'Trending Korean pop', seed: { title: 'Trending K-Pop', artist: 'Various', language: 'Korean' } },
  { id: 'lofi', name: 'Lo-Fi Chill', desc: 'Relaxing beats to study to', seed: { title: 'Lofi Hip Hop Beats', artist: 'Chillhop', genre: 'Lofi' } },
  { id: 'latino', name: 'Latino Hits', desc: 'Reggaeton & Latin Pop', seed: { title: 'Latin Pop Hits', artist: 'Various', language: 'Spanish' } },
]

export function Radio() {
  const currentSong = usePlayerStore((s) => s.currentSong)
  const playSong = usePlayerStore((s) => s.playSong)
  
  const [loadingStation, setLoadingStation] = useState<string | null>(null)
  const [recommendations, setRecommendations] = useState<Song[]>([])
  const [error, setError] = useState('')

  const handleStartRadio = async (seedInfo: { title: string, artist: string, language?: string, genre?: string }, stationId: string) => {
    setLoadingStation(stationId)
    setError('')
    try {
      // Create a search query that will yield a good mix of songs
      let query = `${seedInfo.title} ${seedInfo.artist}`
      if (stationId !== 'smart') {
         query = `${seedInfo.title} mix playlist songs`
      } else {
         query = `${seedInfo.title} ${seedInfo.artist} similar songs radio mix`
      }

      const recs = await api.search(query)
      
      if (!recs || !Array.isArray(recs) || recs.length === 0) {
        setError('No recommendations found for this station.')
      } else {
        setRecommendations(recs)
        playSong(recs[0], recs)
      }
    } catch (err) {
      console.error(err)
      setError('Failed to tune into station. Ensure AI backend is connected.')
    } finally {
      setLoadingStation(null)
    }
  }

  return (
    <div className="main-content">
      <div className="page-header">
        <h1 className="page-title">Radio Stations</h1>
        <p className="page-sub">Endless music channels and AI-powered smart radio</p>
      </div>

      <div className="section">
        <h2 className="section-title">Global Channels</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 16 }}>
          {PREDEFINED_STATIONS.map((station) => {
            const isLoading = loadingStation === station.id
            return (
              <div
                key={station.id}
                onClick={() => !loadingStation && handleStartRadio(station.seed, station.id)}
                style={{
                  background: 'var(--surface)',
                  border: '1px solid var(--border)',
                  borderRadius: 'var(--radius)',
                  padding: 20,
                  cursor: loadingStation ? 'not-allowed' : 'pointer',
                  opacity: loadingStation && !isLoading ? 0.5 : 1,
                  transition: 'all 0.2s'
                }}
                onMouseEnter={(e) => { if (!loadingStation) e.currentTarget.style.borderColor = 'var(--border-hover)' }}
                onMouseLeave={(e) => { e.currentTarget.style.borderColor = 'var(--border)' }}
              >
                <div style={{ fontSize: 32, marginBottom: 12 }}>
                  {isLoading ? '⏳' : '📻'}
                </div>
                <h3 style={{ fontSize: 16, fontWeight: 700, marginBottom: 4 }}>{station.name}</h3>
                <p style={{ fontSize: 13, color: 'var(--text2)' }}>{station.desc}</p>
              </div>
            )
          })}
        </div>
      </div>

      <div className="section" style={{ background: 'var(--surface)', padding: 24, borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
        <h2 className="section-title">Smart Radio (Based on current song)</h2>
        {currentSong ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: 16, background: 'var(--surface2)', padding: 16, borderRadius: 'var(--radius-sm)', marginBottom: 20 }}>
            <img src={currentSong.thumbnail} alt={currentSong.title} style={{ width: 64, height: 64, borderRadius: 6, objectFit: 'cover' }} />
            <div>
              <div style={{ fontSize: 16, fontWeight: 600 }}>{currentSong.title}</div>
              <div style={{ color: 'var(--text2)', fontSize: 14 }}>{currentSong.artist}</div>
            </div>
          </div>
        ) : (
          <div style={{ color: 'var(--text3)', fontStyle: 'italic', marginBottom: 20 }}>No song currently playing.</div>
        )}

        <button
          className="login-btn"
          onClick={() => currentSong && handleStartRadio({ title: currentSong.title, artist: currentSong.artist }, 'smart')}
          disabled={loadingStation !== null || !currentSong}
          style={{ width: 'auto', padding: '12px 24px' }}
        >
          {loadingStation === 'smart' ? 'Tuning Station...' : 'Start Smart Radio'}
        </button>
      </div>

      {error && (
        <div style={{ color: 'var(--red)', marginBottom: 24, padding: 16, background: 'var(--red-dim)', borderRadius: 'var(--radius-sm)', border: '1px solid rgba(244,63,94,0.3)' }}>
          {error}
        </div>
      )}

      {recommendations.length > 0 && (
        <div className="section">
          <h2 className="section-title">Up Next on Radio</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
            {recommendations.map((song) => (
              <SongRow key={song.id} song={song} queue={recommendations} />
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

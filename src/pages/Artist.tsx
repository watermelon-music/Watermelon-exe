import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api, Song } from '../lib/api'
import { usePlayerStore } from '../lib/store'
import { SongRow } from '../components/SongRow'

export function Artist() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [artist, setArtist] = useState<any>(null)
  const [songs, setSongs] = useState<Song[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    Promise.all([
      api.getArtist(id),
      api.getArtistSongs(id)
    ])
      .then(([aData, sData]) => {
        setArtist(aData)
        const mappedSongs = sData.map((s: any) => ({
          ...s,
          thumbnail: s.coverUrl || s.thumbnail
        }))
        setSongs(mappedSongs)
      })
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <div className="main-content loading-center">
        <div className="spinner" />
      </div>
    )
  }

  if (!artist) {
    return (
      <div className="main-content empty">
        <button className="logout-btn" onClick={() => navigate(-1)}>Go Back</button>
        <div className="empty-text">Artist not found</div>
      </div>
    )
  }

  return (
    <div className="main-content">
      <div style={{ marginBottom: 20 }}>
        <button className="logout-btn" onClick={() => navigate(-1)} style={{ padding: '6px 12px', fontSize: 12, marginTop: 0 }}>
          ← Back
        </button>
      </div>

      <div className="page-header" style={{ display: 'flex', gap: 24, alignItems: 'center', marginBottom: 40 }}>
        {artist.imageUrl ? (
          <img 
            src={artist.imageUrl} 
            alt={artist.name} 
            style={{ width: 140, height: 140, borderRadius: '50%', objectFit: 'cover', border: '4px solid var(--surface)' }}
          />
        ) : (
          <div style={{ width: 140, height: 140, borderRadius: '50%', background: 'var(--surface3)' }} />
        )}
        <div>
          {artist.verified && (
            <span className="profile-plan" style={{ marginBottom: 8 }}>Verified Artist</span>
          )}
          <h1 className="page-title" style={{ fontSize: 48, marginBottom: 8 }}>{artist.name}</h1>
          <p className="page-sub" style={{ fontSize: 16 }}>
            {(artist.subscriberCount || artist.followersCount || 0).toLocaleString()} Followers • {artist.songCount || songs.length} Songs
          </p>
        </div>
      </div>

      <div className="section">
        <h2 className="section-title">Top Songs</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          {songs.map((song) => (
            <SongRow key={song.id} song={song} queue={songs} />
          ))}
        </div>
      </div>
      
      {artist.bio && (
        <div className="section" style={{ marginTop: 40 }}>
          <h2 className="section-title">About</h2>
          <p style={{ color: 'var(--text2)', lineHeight: 1.6, whiteSpace: 'pre-line', maxWidth: 800, fontSize: 14 }}>
            {artist.bio}
          </p>
        </div>
      )}
    </div>
  )
}

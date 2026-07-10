import { useState, useEffect } from 'react'
import { supabase } from '../lib/supabase'
import { useAuthStore } from '../lib/store'
import { SongRow } from '../components/SongRow'
import type { Song } from '../lib/api'

type Playlist = {
  id: string
  name: string
  description: string | null
  created_at: string
}

export function Library() {
  const { user } = useAuthStore()
  const [playlists, setPlaylists] = useState<Playlist[]>([])
  const [liked, setLiked] = useState<Song[]>([])
  const [selected, setSelected] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [view, setView] = useState<'playlists' | 'liked'>('liked')

  useEffect(() => {
    if (!user) return
    Promise.all([
      supabase.from('playlists').select('*').eq('user_id', user.id).order('created_at', { ascending: false }),
      supabase.from('favorites').select('*').eq('user_id', user.id).order('created_at', { ascending: false }).limit(50),
    ]).then(([pl, fav]) => {
      setPlaylists(pl.data || [])
      // favorites store song data inline
      const songs = (fav.data || []).map((f: any) => ({
        id: f.song_id || f.id,
        title: f.title || f.song_title || 'Unknown',
        artist: f.artist || f.song_artist || 'Unknown',
        thumbnail: f.thumbnail || f.artwork_url || '',
        duration: f.duration || 0,
      }))
      setLiked(songs)
      setLoading(false)
    })
  }, [user])

  if (loading) return (
    <div className="main-content">
      <div className="loading-center"><div className="spinner" /></div>
    </div>
  )

  return (
    <div className="main-content">
      <div className="page-header">
        <h1 className="page-title">Library</h1>
        <p className="page-sub">Your playlists and liked songs</p>
      </div>

      <div className="login-tabs" style={{ maxWidth: 300, marginBottom: 24 }}>
        <button
          className={`login-tab${view === 'liked' ? ' active' : ''}`}
          onClick={() => setView('liked')}
        >
          ❤️ Liked Songs ({liked.length})
        </button>
        <button
          className={`login-tab${view === 'playlists' ? ' active' : ''}`}
          onClick={() => setView('playlists')}
        >
          📋 Playlists ({playlists.length})
        </button>
      </div>

      {view === 'liked' && (
        liked.length === 0
          ? <div className="empty"><div className="empty-icon">❤️</div><div className="empty-text">No liked songs yet</div></div>
          : <>{liked.map(song => <SongRow key={song.id} song={song} queue={liked} />)}</>
      )}

      {view === 'playlists' && (
        playlists.length === 0
          ? <div className="empty"><div className="empty-icon">📋</div><div className="empty-text">No playlists yet</div></div>
          : (
            <div className="songs-grid">
              {playlists.map(pl => (
                <div key={pl.id} className="song-card" style={{ cursor: 'pointer' }}>
                  <div className="song-card-thumb" style={{
                    background: 'linear-gradient(135deg, var(--green-dim), var(--red-dim))',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 32
                  }}>📋</div>
                  <div className="song-card-title">{pl.name}</div>
                  <div className="song-card-artist">{pl.created_at?.slice(0, 10)}</div>
                </div>
              ))}
            </div>
          )
      )}
    </div>
  )
}

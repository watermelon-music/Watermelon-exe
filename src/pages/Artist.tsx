import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api, Song } from '../lib/api'
import { usePlayerStore } from '../lib/store'
import { ArrowLeft, Play, Users } from 'lucide-react'

export function Artist() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [artist, setArtist] = useState<any>(null)
  const [songs, setSongs] = useState<Song[]>([])
  const [loading, setLoading] = useState(true)
  const playSong = usePlayerStore((s) => s.playSong)
  const currentSong = usePlayerStore((s) => s.currentSong)
  const isPlaying = usePlayerStore((s) => s.isPlaying)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    Promise.all([
      api.getArtist(id),
      api.getArtistSongs(id)
    ])
      .then(([aData, sData]) => {
        setArtist(aData)
        // Map the backend song format to the expected Song type if needed
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
      <div className="flex-1 p-8 flex justify-center items-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500" />
      </div>
    )
  }

  if (!artist) {
    return (
      <div className="flex-1 p-8 text-white">
        <button onClick={() => navigate(-1)} className="mb-4 hover:text-red-400">
          <ArrowLeft className="h-6 w-6" />
        </button>
        <h2 className="text-2xl font-bold">Artist not found</h2>
      </div>
    )
  }

  const handlePlaySong = (song: Song, index: number) => {
    playSong(song, songs)
  }

  return (
    <div className="flex-1 overflow-y-auto pb-24 text-white">
      <div className="relative h-64 md:h-80 w-full">
        {artist.bannerUrl || artist.imageUrl ? (
          <img 
            src={artist.bannerUrl || artist.imageUrl} 
            alt={artist.name} 
            className="absolute inset-0 w-full h-full object-cover"
          />
        ) : (
          <div className="absolute inset-0 w-full h-full bg-neutral-800" />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-[#080808] via-[#080808]/60 to-transparent" />
        
        <div className="absolute top-6 left-6">
          <button onClick={() => navigate(-1)} className="p-2 bg-black/40 hover:bg-black/60 rounded-full backdrop-blur-sm transition-colors">
            <ArrowLeft className="h-6 w-6" />
          </button>
        </div>

        <div className="absolute bottom-6 left-6 flex items-end gap-6">
          {artist.imageUrl && (
            <img 
              src={artist.imageUrl} 
              alt={artist.name} 
              className="w-32 h-32 md:w-40 md:h-40 rounded-full border-4 border-[#080808] shadow-2xl object-cover"
            />
          )}
          <div>
            <div className="flex items-center gap-2 mb-2">
              {artist.verified && (
                <span className="px-2 py-0.5 text-xs font-medium bg-blue-500/20 text-blue-400 rounded-full">
                  Verified Artist
                </span>
              )}
            </div>
            <h1 className="text-4xl md:text-6xl font-black mb-4">{artist.name}</h1>
            <div className="flex items-center gap-4 text-neutral-300 text-sm font-medium">
              <span className="flex items-center gap-1">
                <Users className="h-4 w-4" />
                {(artist.subscriberCount || artist.followersCount || 0).toLocaleString()} Followers
              </span>
              <span>•</span>
              <span>{artist.songCount || songs.length} Songs</span>
            </div>
          </div>
        </div>
      </div>

      <div className="p-6">
        <h2 className="text-2xl font-bold mb-4">Top Songs</h2>
        <div className="space-y-2">
          {songs.map((song, i) => {
            const isActive = currentSong?.id === song.id
            return (
              <div
                key={song.id}
                onClick={() => handlePlaySong(song, i)}
                className={`flex items-center gap-4 p-3 rounded-xl hover:bg-white/5 transition-colors cursor-pointer group ${
                  isActive ? 'bg-white/10' : ''
                }`}
              >
                <div className="w-8 text-center text-neutral-500 text-sm font-medium group-hover:hidden">
                  {i + 1}
                </div>
                <div className="w-8 text-center hidden group-hover:block">
                  <Play className="h-4 w-4 mx-auto text-white" />
                </div>
                <img
                  src={song.thumbnail}
                  alt={song.title}
                  className="w-12 h-12 rounded object-cover"
                />
                <div className="flex-1 min-w-0">
                  <div className={`font-medium truncate ${isActive ? 'text-red-400' : 'text-white'}`}>
                    {song.title}
                  </div>
                  <div className="text-sm text-neutral-400 truncate">
                    {song.artist}
                  </div>
                </div>
              </div>
            )
          })}
        </div>
        
        {artist.bio && (
          <div className="mt-12 max-w-3xl">
            <h2 className="text-2xl font-bold mb-4">About</h2>
            <p className="text-neutral-400 leading-relaxed whitespace-pre-line">
              {artist.bio}
            </p>
          </div>
        )}
      </div>
    </div>
  )
}

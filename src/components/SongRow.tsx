import type { Song } from '../lib/api'
import { usePlayerStore } from '../lib/store'
import { playerEngine } from '../lib/player'
import { formatDuration } from '../lib/utils'

interface SongRowProps {
  song: Song
  queue?: Song[]
  index?: number
}

export function SongRow({ song, queue }: SongRowProps) {
  const { currentSong, isPlaying, playSong } = usePlayerStore()
  const isCurrentSong = currentSong?.id === song.id

  const handlePlay = () => {
    if (isCurrentSong) {
      if (isPlaying) playerEngine.pause()
      else playerEngine.play()
      usePlayerStore.getState().pauseResume()
    } else {
      playSong(song, queue)
      playerEngine.load(song.id)
      playerEngine.play()
    }
  }

  return (
    <div className={`song-row${isCurrentSong ? ' playing' : ''}`} onClick={handlePlay}>
      <img
        className="song-thumb"
        src={song.thumbnail || ''}
        alt={song.title}
        onError={(e) => { (e.target as HTMLImageElement).style.visibility = 'hidden' }}
      />
      <div className="song-info">
        <div className={`song-title${isCurrentSong ? ' playing' : ''}`}>{song.title}</div>
        <div className="song-artist">{song.artist}</div>
      </div>
      <div className="song-duration">{formatDuration(song.duration)}</div>
    </div>
  )
}

export function SongCard({ song, queue }: SongRowProps) {
  const { currentSong, playSong } = usePlayerStore()
  const isCurrentSong = currentSong?.id === song.id

  const handlePlay = () => {
    playSong(song, queue)
    playerEngine.load(song.id)
    playerEngine.play()
  }

  return (
    <div className={`song-card${isCurrentSong ? ' playing' : ''}`} onClick={handlePlay}>
      <img
        className="song-card-thumb"
        src={song.thumbnail || ''}
        alt={song.title}
        onError={(e) => { (e.target as HTMLImageElement).style.visibility = 'hidden' }}
      />
      <div className="song-card-title">{song.title}</div>
      <div className="song-card-artist">{song.artist}</div>
    </div>
  )
}

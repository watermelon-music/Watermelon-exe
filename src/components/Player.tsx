import { useRef } from 'react'
import { Play, Pause, SkipBack, SkipForward, Shuffle, Repeat, Repeat1, Volume2, VolumeX, Music } from 'lucide-react'
import { usePlayerStore } from '../lib/store'
import { playerEngine } from '../lib/player'
import { formatSeconds } from '../lib/utils'

export function Player() {
  const {
    currentSong, isPlaying, isBuffering,
    volume, isMuted, position, duration,
    isShuffled, repeatMode,
    pauseResume, next, prev,
    setVolume, toggleMute, setPosition,
    toggleShuffle, toggleRepeat,
  } = usePlayerStore()

  const handlePlayPause = () => {
    if (isPlaying) playerEngine.pause()
    else playerEngine.play()
    pauseResume()
  }

  const handleSeek = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!duration) return
    const rect = e.currentTarget.getBoundingClientRect()
    const pct = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
    const newPos = pct * duration
    playerEngine.seek(newPos)
    setPosition(newPos)
  }

  const handleVolume = (e: React.MouseEvent<HTMLDivElement>) => {
    const rect = e.currentTarget.getBoundingClientRect()
    const pct = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
    playerEngine.setVolume(pct)
    setVolume(pct)
  }

  const handleNext = () => { next(); setTimeout(() => { const s = usePlayerStore.getState().currentSong; if (s) { playerEngine.load(s.id); playerEngine.play() } }, 50) }
  const handlePrev = () => { prev(); setTimeout(() => { const s = usePlayerStore.getState().currentSong; if (s) { playerEngine.load(s.id); playerEngine.play() } }, 50) }

  const progressPct = duration > 0 ? (position / duration) * 100 : 0
  const volumePct = isMuted ? 0 : volume * 100

  const RepeatIcon = repeatMode === 'one' ? Repeat1 : Repeat

  if (!currentSong) {
    return (
      <div className="player">
        <div className="player-empty" style={{ gridColumn: '1 / -1' }}>
          <Music size={16} style={{ marginRight: 8, color: 'var(--text3)' }} />
          Play a song to start listening
        </div>
      </div>
    )
  }

  return (
    <div className="player">
      {/* Track info */}
      <div className="player-track">
        <img
          className="player-thumb"
          src={currentSong.thumbnail || ''}
          alt={currentSong.title}
          onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
        />
        <div className="player-info">
          <div className="player-title">{currentSong.title}</div>
          <div className="player-artist">{currentSong.artist}</div>
        </div>
      </div>

      {/* Controls + progress */}
      <div className="player-center">
        <div className="player-controls">
          <button className={`ctrl-btn${isShuffled ? ' active' : ''}`} onClick={toggleShuffle} title="Shuffle">
            <Shuffle size={15} />
          </button>
          <button className="ctrl-btn" onClick={handlePrev} title="Previous">
            <SkipBack size={17} />
          </button>
          <button className="play-btn" onClick={handlePlayPause} disabled={isBuffering}>
            {isBuffering
              ? <span className="spinner" style={{ width: 16, height: 16, borderWidth: 2 }} />
              : isPlaying ? <Pause size={18} /> : <Play size={18} style={{ marginLeft: 2 }} />
            }
          </button>
          <button className="ctrl-btn" onClick={handleNext} title="Next">
            <SkipForward size={17} />
          </button>
          <button className={`ctrl-btn${repeatMode !== 'off' ? ' active' : ''}`} onClick={toggleRepeat} title="Repeat">
            <RepeatIcon size={15} />
          </button>
        </div>

        <div className="progress-bar-wrap">
          <span className="time-label">{formatSeconds(position)}</span>
          <div className="progress-bar" onClick={handleSeek}>
            <div className="progress-fill" style={{ width: `${progressPct}%` }} />
          </div>
          <span className="time-label" style={{ textAlign: 'right' }}>{formatSeconds(duration)}</span>
        </div>
      </div>

      {/* Volume */}
      <div className="player-right">
        <div className="volume-row">
          <button className="ctrl-btn" onClick={() => { toggleMute(); playerEngine.setMuted(!isMuted) }}>
            {isMuted || volume === 0 ? <VolumeX size={16} /> : <Volume2 size={16} />}
          </button>
          <div className="volume-bar" onClick={handleVolume}>
            <div className="volume-fill" style={{ width: `${volumePct}%` }} />
          </div>
        </div>
      </div>
    </div>
  )
}

import { useRef } from 'react'
import { usePlayerStore } from '../lib/store'
import { playerEngine } from '../lib/player'

function formatTime(seconds: number) {
  if (isNaN(seconds) || seconds < 0) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

export function Player() {
  const { 
    currentSong, isPlaying, volume, isMuted, setVolume, toggleMute, 
    pauseResume, next, prev, toggleShuffle, toggleRepeat, 
    isShuffled, repeatMode, position, duration 
  } = usePlayerStore()

  const progressRef = useRef<HTMLDivElement>(null)

  const handleSeek = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!progressRef.current || !currentSong) return
    const rect = progressRef.current.getBoundingClientRect()
    const pos = (e.clientX - rect.left) / rect.width
    playerEngine.seek(pos * duration)
  }

  if (!currentSong) return null

  return (
    <footer className="fixed bottom-0 left-0 w-full z-50 h-24 bg-surface-container-low/95 backdrop-blur-lg border-t border-outline-variant shadow-2xl flex justify-between items-center px-container-padding">
      {/* Current Song Info */}
      <div className="flex items-center gap-4 w-1/4">
        <div className="w-14 h-14 rounded-lg overflow-hidden shadow-md border border-outline-variant flex-shrink-0">
          {currentSong.thumbnail ? (
            <img className="w-full h-full object-cover" src={currentSong.thumbnail} alt="thumbnail" />
          ) : (
            <div className="w-full h-full bg-surface-container flex items-center justify-center">
              <span className="material-symbols-outlined text-on-surface-variant">music_note</span>
            </div>
          )}
        </div>
        <div className="flex flex-col min-w-0">
          <span className="font-bold text-on-surface truncate">{currentSong.title}</span>
          <span className="text-xs text-on-surface-variant truncate">{currentSong.artist}</span>
        </div>
        <button className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-colors ml-2">favorite</button>
      </div>

      {/* Playback Controls */}
      <div className="flex flex-col items-center gap-2 w-2/4">
        <div className="flex items-center gap-8">
          <button 
            className={`material-symbols-outlined transition-colors ${isShuffled ? 'text-primary' : 'text-on-surface-variant hover:text-primary'}`}
            onClick={toggleShuffle}
          >
            shuffle
          </button>
          <button 
            className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-colors"
            onClick={prev}
          >
            skip_previous
          </button>
          <button 
            className="material-symbols-outlined text-primary scale-[1.75] active:scale-95 transition-transform" 
            style={{ fontVariationSettings: '"FILL" 1' }}
            onClick={pauseResume}
          >
            {isPlaying ? 'pause_circle' : 'play_circle'}
          </button>
          <button 
            className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-colors"
            onClick={next}
          >
            skip_next
          </button>
          <button 
            className={`material-symbols-outlined transition-colors ${repeatMode !== 'off' ? 'text-primary' : 'text-on-surface-variant hover:text-primary'}`}
            onClick={toggleRepeat}
          >
            {repeatMode === 'one' ? 'repeat_one' : 'repeat'}
          </button>
        </div>
        
        <div className="flex items-center gap-3 w-full max-w-lg">
          <span className="text-[10px] text-on-surface-variant font-medium w-8 text-right">{formatTime(position)}</span>
          <div 
            className="flex-1 h-1 bg-surface-container-highest rounded-full relative overflow-hidden group cursor-pointer"
            onClick={handleSeek}
            ref={progressRef}
          >
            <div className="absolute h-full bg-primary" style={{ width: `${(position / duration) * 100 || 0}%` }}></div>
            <div className="absolute h-full bg-white opacity-0 group-hover:opacity-20 transition-opacity" style={{ width: `${(position / duration) * 100 || 0}%` }}></div>
          </div>
          <span className="text-[10px] text-on-surface-variant font-medium w-8">{formatTime(duration)}</span>
        </div>
      </div>

      {/* Extra Controls */}
      <div className="flex items-center justify-end gap-6 w-1/4">
        <button className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-colors">mic_external_on</button>
        <button className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-colors">queue_music</button>
        <div className="flex items-center gap-2 w-32 group">
          <button className="material-symbols-outlined text-on-surface-variant group-hover:text-primary transition-colors" onClick={toggleMute}>
            {isMuted || volume === 0 ? 'volume_off' : volume < 0.5 ? 'volume_down' : 'volume_up'}
          </button>
          <input 
            type="range" 
            min="0" 
            max="1" 
            step="0.01" 
            value={isMuted ? 0 : volume}
            onChange={(e) => setVolume(parseFloat(e.target.value))}
            className="flex-1 h-1 bg-surface-container-highest rounded-full appearance-none cursor-pointer accent-primary" 
          />
        </div>
      </div>
    </footer>
  )
}

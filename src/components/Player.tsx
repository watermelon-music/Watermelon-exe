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
    <footer className="fixed bottom-0 left-0 right-0 h-24 bg-[#121212] border-t border-[#1a1a1a] z-50 px-6 flex items-center justify-between">
      {/* Current Song Info */}
      <div className="flex items-center gap-4 w-1/4">
        <div className="w-14 h-14 rounded-md overflow-hidden bg-[#1a1a1a] flex-shrink-0">
          {currentSong.thumbnail ? <img src={currentSong.thumbnail} alt="cover" className="w-full h-full object-cover" /> : null}
        </div>
        <div className="min-w-0 max-w-[200px]">
          <h4 className="font-bold text-white text-sm truncate hover:underline cursor-pointer">{currentSong.title}</h4>
          <p className="text-xs text-gray-400 truncate hover:underline cursor-pointer">{currentSong.artist}</p>
        </div>
        <button className="material-symbols-outlined text-gray-400 hover:text-[#ff3b3b] ml-4 transition-colors">favorite</button>
      </div>

      {/* Playback Controls */}
      <div className="flex flex-col items-center max-w-[600px] w-full px-4">
        <div className="flex items-center gap-6 mb-2">
          <button 
            className={`material-symbols-outlined transition-colors text-lg ${isShuffled ? 'text-[#ff3b3b]' : 'text-gray-400 hover:text-white'}`}
            onClick={toggleShuffle}
          >
            shuffle
          </button>
          <button className="material-symbols-outlined text-gray-400 hover:text-white transition-colors text-2xl" onClick={prev}>skip_previous</button>
          <button 
            className="w-10 h-10 flex items-center justify-center rounded-full bg-[#ff3b3b] text-white hover:scale-105 active:scale-95 transition-transform"
            onClick={pauseResume}
          >
            <span className="material-symbols-outlined text-[26px]" style={{ fontVariationSettings: '"FILL" 1' }}>{isPlaying ? 'pause' : 'play_arrow'}</span>
          </button>
          <button className="material-symbols-outlined text-gray-400 hover:text-white transition-colors text-2xl" onClick={next}>skip_next</button>
          <button 
            className={`material-symbols-outlined transition-colors text-lg ${repeatMode !== 'off' ? 'text-[#ff3b3b]' : 'text-gray-400 hover:text-white'}`}
            onClick={toggleRepeat}
          >
            {repeatMode === 'one' ? 'repeat_one' : 'repeat'}
          </button>
        </div>
        <div className="w-full flex items-center gap-3">
          <span className="text-[11px] text-gray-400 font-medium min-w-[32px] text-right">{formatTime(position)}</span>
          <div 
            className="h-1 flex-1 bg-[#2a2a2a] rounded-full overflow-hidden cursor-pointer group relative"
            onClick={handleSeek}
            ref={progressRef}
          >
            <div className="h-full bg-white rounded-full group-hover:bg-[#ff3b3b]" style={{ width: `${(position / duration) * 100 || 0}%` }}></div>
          </div>
          <span className="text-[11px] text-gray-400 font-medium min-w-[32px] text-left">{formatTime(duration)}</span>
        </div>
      </div>

      {/* Extra Controls */}
      <div className="flex items-center justify-end gap-4 min-w-[200px] text-gray-400">
        <button className="material-symbols-outlined hover:text-white transition-colors text-lg" onClick={toggleMute}>
          {isMuted || volume === 0 ? 'volume_off' : volume < 0.5 ? 'volume_down' : 'volume_up'}
        </button>
        <div className="w-24 h-1 bg-[#2a2a2a] rounded-full overflow-hidden cursor-pointer group relative">
          <input 
            type="range" 
            min="0" 
            max="1" 
            step="0.01" 
            value={isMuted ? 0 : volume}
            onChange={(e) => setVolume(parseFloat(e.target.value))}
            className="absolute inset-0 opacity-0 cursor-pointer"
          />
          <div className="h-full bg-white rounded-full group-hover:bg-[#ff3b3b]" style={{ width: `${isMuted ? 0 : volume * 100}%` }}></div>
        </div>
        <button className="material-symbols-outlined hover:text-white transition-colors ml-2 text-lg">queue_music</button>
      </div>
    </footer>
  )
}

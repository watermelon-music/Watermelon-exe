import { useState, useEffect } from 'react'
import { api, Song } from '../lib/api'
import { usePlayerStore } from '../lib/store'
import { Play, Loader2, Radio as RadioIcon, Sparkles } from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'

export function Radio() {
  const currentSong = usePlayerStore((s) => s.currentSong)
  const isPlaying = usePlayerStore((s) => s.isPlaying)
  const playSong = usePlayerStore((s) => s.playSong)
  
  const [loading, setLoading] = useState(false)
  const [recommendations, setRecommendations] = useState<Song[]>([])
  const [error, setError] = useState('')

  const handleStartRadio = async () => {
    if (!currentSong) {
      setError('Play a song first to start radio!')
      return
    }
    
    setLoading(true)
    setError('')
    try {
      const recs = await api.recommendations({
        title: currentSong.title,
        artist: currentSong.artist
      })
      
      if (!recs || recs.length === 0) {
        setError('No recommendations found for this track.')
      } else {
        setRecommendations(recs)
        // Auto-play the first recommendation and queue the rest
        playSong(recs[0], recs)
      }
    } catch (err) {
      console.error(err)
      setError('Failed to generate radio station. Ensure AI backend is connected.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex-1 p-8 text-white overflow-y-auto pb-24 relative">
      {/* Dynamic Background */}
      <div className="absolute inset-0 bg-gradient-to-br from-red-900/10 via-[#080808] to-orange-900/10 -z-10" />

      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex items-center gap-4 mb-8"
      >
        <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-red-500 to-orange-500 flex items-center justify-center shadow-lg shadow-red-500/20 relative overflow-hidden">
          <div className="absolute inset-0 bg-white/20 blur-xl rounded-full" />
          <RadioIcon className="w-8 h-8 text-white relative z-10" />
        </div>
        <div>
          <h1 className="text-4xl font-black tracking-tight">Smart Radio</h1>
          <p className="text-neutral-400 mt-1">Endless music based on what you're listening to</p>
        </div>
      </motion.div>

      <motion.div 
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.1 }}
        className="bg-white/5 backdrop-blur-xl border border-white/10 rounded-3xl p-8 mb-8 shadow-2xl relative overflow-hidden"
      >
        {/* Decorative blobs */}
        <div className="absolute top-0 right-0 w-64 h-64 bg-red-500/10 rounded-full blur-3xl -z-10 translate-x-1/2 -translate-y-1/2" />
        
        <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
          <Sparkles className="w-5 h-5 text-red-400" />
          Current Seed
        </h2>
        {currentSong ? (
          <div className="flex items-center gap-4 bg-black/40 p-4 rounded-xl">
            <img src={currentSong.thumbnail} alt={currentSong.title} className="w-16 h-16 rounded-md object-cover" />
            <div>
              <div className="font-semibold text-lg">{currentSong.title}</div>
              <div className="text-neutral-400">{currentSong.artist}</div>
            </div>
          </div>
        ) : (
          <div className="text-neutral-400 italic">No song currently playing.</div>
        )}

        <motion.button
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          onClick={handleStartRadio}
          disabled={loading || !currentSong}
          className="mt-6 flex items-center gap-3 bg-gradient-to-r from-red-500 to-orange-500 hover:from-red-400 hover:to-orange-400 disabled:opacity-50 disabled:grayscale text-white px-8 py-4 rounded-full font-bold transition-all shadow-lg shadow-red-500/25"
        >
          {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : <Play className="w-5 h-5 fill-current" />}
          {loading ? 'Tuning Station & Generating Playlist...' : 'Start Radio Station'}
        </motion.button>

        <AnimatePresence>
          {error && (
            <motion.div 
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              className="mt-4 text-red-400 font-medium"
            >
              {error}
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>

      <AnimatePresence>
        {recommendations.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="mb-8"
          >
            <h2 className="text-2xl font-bold mb-4 px-2">Up Next</h2>
            <div className="space-y-2">
              {recommendations.map((song, i) => (
                <motion.div 
                  key={song.id + i} 
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: i * 0.05 }}
                  className="flex items-center gap-4 p-3 rounded-2xl hover:bg-white/10 transition-colors group cursor-pointer"
                  onClick={() => playSong(song, recommendations)}
                >
                  <div className="w-8 text-center text-neutral-500 font-medium group-hover:text-white transition-colors">
                    {i + 1}
                  </div>
                  <img src={song.thumbnail} alt={song.title} className="w-12 h-12 rounded-xl object-cover shadow-md" />
                  <div>
                    <div className="font-medium text-white group-hover:text-red-400 transition-colors">{song.title}</div>
                    <div className="text-sm text-neutral-400">{song.artist}</div>
                  </div>
                </motion.div>
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}

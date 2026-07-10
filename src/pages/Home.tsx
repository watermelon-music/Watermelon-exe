import { useEffect, useState } from 'react'
import { api, Song } from '../lib/api'
import { usePlayerStore } from '../lib/store'
import { CachedImage } from '../components/CachedImage'

const CATEGORIES = [
  { id: 'trending', title: '🔥 Trending', query: 'trending viral hits' },
  { id: 'hollywood', title: '🎬 Hollywood', query: 'hollywood soundtrack hits' },
  { id: 'bollywood', title: '🎥 Bollywood', query: 'bollywood hits' },
  { id: 'pop', title: '🎤 Pop', query: 'pop hits 2024' },
  { id: 'rock', title: '🎸 Rock', query: 'rock classics' },
  { id: 'jazz', title: '🎷 Jazz & Blues', query: 'best of jazz and blues' },
  { id: 'classical', title: '🎼 Classical', query: 'classical masterpieces' },
  { id: 'hiphop', title: '🎧 Hip-Hop', query: 'hip hop hits' },
  { id: 'electronic', title: '⚡ Electronic', query: 'electronic dance music' }
]

export function Home() {
  const [continueListening, setContinueListening] = useState<Song[]>([])
  const [categoryData, setCategoryData] = useState<Record<string, Song[]>>({})
  
  const playSong = usePlayerStore((s) => s.playSong)

  useEffect(() => {
    // Fetch Continue Listening (Recent style)
    api.search('popular hits 2024').then((res) => setContinueListening(res ? res.slice(0, 8) : [])).catch(console.error)
    
    // Fetch all categories
    CATEGORIES.forEach(cat => {
      api.search(cat.query).then(res => {
        if (res && res.length > 0) {
          setCategoryData(prev => ({ ...prev, [cat.id]: res.slice(0, 8) }))
        }
      }).catch(console.error)
    })
  }, [])

  // Large Card format (Continue Listening & Categories)
  const renderLargeRow = (title: string, songs: Song[]) => {
    if (!songs || songs.length === 0) return null;
    return (
      <section className="mb-12" key={title}>
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-white">{title}</h2>
          <div className="flex gap-2">
            <button className="w-8 h-8 rounded-full bg-[#1a1a1a] flex items-center justify-center hover:bg-[#2a2a2a] transition-colors group">
              <span className="material-symbols-outlined text-gray-400 group-hover:text-white text-sm">chevron_left</span>
            </button>
            <button className="w-8 h-8 rounded-full bg-[#1a1a1a] flex items-center justify-center hover:bg-[#2a2a2a] transition-colors group">
              <span className="material-symbols-outlined text-gray-400 group-hover:text-white text-sm">chevron_right</span>
            </button>
          </div>
        </div>
        <div className="flex gap-4 overflow-x-auto pb-4 hide-scrollbar snap-x snap-mandatory">
          {songs.map((song) => (
            <div 
              key={song.id} 
              className="group cursor-pointer min-w-[240px] max-w-[240px] snap-start bg-[#121212] rounded-xl p-4 hover:bg-[#1a1a1a] transition-colors relative" 
              onClick={() => playSong(song, songs)}
            >
              <div className="relative aspect-square rounded-lg overflow-hidden mb-4 shadow-lg bg-[#1a1a1a]">
                {song.thumbnail ? (
                  <CachedImage className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" src={song.thumbnail} alt={song.title} />
                ) : (
                  <div className="w-full h-full flex items-center justify-center bg-[#1a1a1a]">
                    <span className="material-symbols-outlined text-4xl text-gray-600">music_note</span>
                  </div>
                )}
                {/* Floating Play Button */}
                <div className="absolute bottom-2 right-2 translate-y-2 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 transition-all duration-300">
                  <button className="w-10 h-10 bg-[#ff3b3b] rounded-full flex items-center justify-center text-white shadow-lg hover:scale-105 hover:brightness-110 active:scale-95 transition-transform">
                    <span className="material-symbols-outlined text-xl" style={{ fontVariationSettings: '"FILL" 1' }}>play_arrow</span>
                  </button>
                </div>
              </div>
              <h5 className="font-bold text-white text-sm truncate mb-1">{song.title}</h5>
              <p className="text-xs text-gray-400 truncate">{song.artist}</p>
            </div>
          ))}
        </div>
      </section>
    )
  }

  return (
    <main className="px-8 pb-10 max-w-[1600px] mx-auto bg-[#0a0a0a] min-h-screen">
      <h1 className="text-3xl font-bold text-white mb-8">Good Evening</h1>
      
      {renderLargeRow('Continue Listening', continueListening)}
      
      {CATEGORIES.map(cat => renderLargeRow(cat.title, categoryData[cat.id] || []))}
      
    </main>
  )
}

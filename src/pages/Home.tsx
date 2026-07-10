import { useEffect, useState } from 'react'
import { api, AppStats, Song, LeaderboardUser } from '../lib/api'
import { usePlayerStore } from '../lib/store'
import { Link } from 'react-router-dom'

export function Home() {
  const [stats, setStats] = useState<AppStats | null>(null)
  const [trending, setTrending] = useState<Song[]>([])
  const [leaderboard, setLeaderboard] = useState<LeaderboardUser[]>([])
  const playSong = usePlayerStore((s) => s.playSong)

  useEffect(() => {
    api.stats().then(setStats).catch(console.error)
    api.search('trending pop music 2024').then((res) => {
      if (res && res.length > 0) setTrending(res.slice(0, 5))
    }).catch(console.error)
    api.leaderboard(3).then(setLeaderboard).catch(console.error)
  }, [])

  return (
    <main className="px-container-padding">
      {/* Hero Section / Bento Grid Start */}
      <section className="grid grid-cols-12 gap-6 mb-section-margin">
        <div className="col-span-8 relative h-[380px] rounded-3xl overflow-hidden group shadow-2xl">
          <div className="absolute inset-0 z-0">
            <img className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" alt="Concert" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBfMxKVqTYxI-Q2GhAsr4NHjEc8e0GpFTtJcc7PiLZSzbSouKcWezxNjzSsNrSMIRdKvdo1v3860SOZkNdH66hMVn8nLLo-sRUy1O4p9JHXPKQEnmDFEjbIyzSJ-RK4w7n2daLNXkNAgRpphR5FoRTkLiqfw2ywUO5YnFfs3IrTZsM48Z-D9z18xB_Bj3irWFJ9pV53IiN0s60DCxwGKXZcv8L8FisNXCLW7rqL85EkseboyWt0VznRlA" />
            <div className="absolute inset-0 bg-gradient-to-t from-black via-black/40 to-transparent"></div>
          </div>
          <div className="absolute bottom-0 left-0 p-10 z-10">
            <span className="inline-block px-3 py-1 rounded-full bg-primary text-on-primary text-xs font-bold uppercase tracking-widest mb-4">New Release</span>
            <h3 className="font-display-lg text-white mb-2 text-5xl">Electronic Pulse</h3>
            <p className="text-on-surface-variant max-w-md mb-6 font-medium">Experience the rhythm of the future with our curated high-fidelity synthwave collection.</p>
            <div className="flex gap-4">
              <button 
                className="bg-primary-container text-white px-8 py-3 rounded-full font-bold flex items-center gap-2 active:scale-95 transition-all hover:brightness-110"
                onClick={() => { if(trending[0]) playSong(trending[0], trending) }}
              >
                <span className="material-symbols-outlined" style={{ fontVariationSettings: '"FILL" 1' }}>play_arrow</span>
                Play Now
              </button>
              <button className="bg-surface-container-high/40 backdrop-blur-md text-white px-8 py-3 rounded-full font-bold border border-primary/30 hover:bg-primary/20 transition-all">
                Save Playlist
              </button>
            </div>
          </div>
        </div>
        <div className="col-span-4 grid grid-rows-2 gap-6">
          <div className="bg-surface-container-low border border-outline-variant rounded-3xl p-6 flex flex-col justify-between group hover:border-primary/40 transition-colors">
            <div>
              <h4 className="text-primary font-bold uppercase tracking-widest text-xs mb-2">Daily Mix</h4>
              <p className="text-xl font-bold text-white">Your Personal Top 50</p>
            </div>
            <div className="flex -space-x-4">
              <div className="w-12 h-12 rounded-full border-2 border-surface-container-low bg-surface-container overflow-hidden">
                <img className="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDoG4ucElFU1zVsZ5weFtXV1pgSrwS0GcXQ97fDn8vZnLrRtWm0JWJZAh9UDJvXxHydibtFzJO6yUp-6D5BpjKGPF3eOMIS-i0aiIyZ9lcp1DjMXTynC2ruR-D4Kq0_w8jCRS3bdgPEt9gaZdLLzm_3u1_wljC0_8vrB2X5d04CiMsKR06yWV5dymgQ1AXJmEOjIkQY0ZtiW0BEujGSutHiGKNjruYlzIIDaYoTvtqxlYpMpEhhEk6SMA" />
              </div>
              <div className="w-12 h-12 rounded-full border-2 border-surface-container-low bg-surface-container overflow-hidden">
                <img className="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuALwtCW9eP8H9zqCR0EcVArp2XK0hYhC9algTNJC4safbAQyZBUPKp3Qmf-Y0VLLz_9Jmh33cqrVbcEX3gOFhN5b2cRLwB_ReS4748TtMf7K7G5eo7OQApsINCnIwuTNuH5ewkGB1IE_jXeoWhpuHH_6CvbWt7jxHc_BQyIgn0lZ67wuXZYfDZru413F82SXG96sH5ueRatzKVM1dCH1G6rhbHbo70cMqBDVKDtzDI-wfw24Ia2v7KAiw" />
              </div>
              <div className="w-12 h-12 rounded-full border-2 border-surface-container-low bg-surface-container overflow-hidden">
                <img className="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCIv-I8Ado6bEMaIZ__OIputT99QFSA6K1CJtdSWPqXlDuCN9WKP7pnz7DYXjPElktnNFnmNhCSBsa6jQpHZVKlEAFo7C3DerwJ34fT3oh_CCKEXwryCqk6JeYdCGexncJlpaNF9aKrkSW3rmqkNF_znxFdTth5vffNrc6Z0zoZV7BwvQ6MFsuXqwVvVomkLWpYPzidf3R7TeYlI4y01j9DJIqeZI5bwuA14MtH8-8QWNPa3Vbv7KkmOw" />
              </div>
              <div className="w-12 h-12 rounded-full border-2 border-surface-container-low bg-surface-container flex items-center justify-center text-xs font-bold text-on-surface-variant">
                +42
              </div>
            </div>
          </div>
          <div className="relative rounded-3xl overflow-hidden group shadow-lg cursor-pointer">
            <img className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAJCxp0ywd7jv1tPRsU9Lwpm_lb3XoNlIDL3yZRkcJB2R0SCj1BfDphHHNngfR-Q_cFmyqV1T0q6cdAXOxKhuBpyGoiOMkNExu9dDIkQeG5pH6nl1yIo9WLoBi3v94cpMjbZe1gqhPakYcLUJdHNWgdAGuJF0_A5rYKLkV0KXjQ65pprCpe_EgwsxOH6UKv1VpfdX5RBOCoKpKkASA3hmyH9xX9lQNteFgArZqbich4vtQ6n62mlLJhuQ" />
            <div className="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
              <span className="material-symbols-outlined text-white text-6xl">play_circle</span>
            </div>
            <div className="absolute bottom-4 left-4 bg-black/60 backdrop-blur-md px-3 py-1 rounded-lg">
              <p className="text-sm font-bold text-white">Classic Revival</p>
            </div>
          </div>
        </div>
      </section>

      {/* Recommended Grid*/}
      <section className="mb-section-margin">
        <div className="flex justify-between items-end mb-6">
          <div>
            <h3 className="font-headline-lg text-on-surface">Recommended for You</h3>
            <p className="text-on-surface-variant">Based on your recent listening history</p>
          </div>
          <Link className="text-primary font-bold flex items-center gap-2 hover:gap-3 transition-all" to="/search">
            View All <span className="material-symbols-outlined">arrow_forward</span>
          </Link>
        </div>
        <div className="grid grid-cols-5 gap-6">
          {trending.map((song) => (
            <div key={song.id} className="group cursor-pointer" onClick={() => playSong(song, trending)}>
              <div className="relative aspect-square rounded-2xl overflow-hidden mb-4 shadow-lg bg-surface-container-high">
                {song.thumbnail ? (
                  <img className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" src={song.thumbnail} alt={song.title} />
                ) : (
                  <div className="w-full h-full flex items-center justify-center bg-surface-container">
                    <span className="material-symbols-outlined text-4xl text-on-surface-variant">music_note</span>
                  </div>
                )}
                <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-all">
                  <div className="w-12 h-12 bg-primary rounded-full flex items-center justify-center text-black transform translate-y-4 group-hover:translate-y-0 transition-all duration-300">
                    <span className="material-symbols-outlined" style={{ fontVariationSettings: '"FILL" 1' }}>play_arrow</span>
                  </div>
                </div>
              </div>
              <h5 className="font-title-md text-on-surface truncate">{song.title}</h5>
              <p className="text-sm text-on-surface-variant truncate">{song.artist}</p>
            </div>
          ))}
          {trending.length === 0 && Array.from({length: 5}).map((_, i) => (
            <div key={i} className="animate-pulse">
              <div className="aspect-square bg-surface-container-high rounded-2xl mb-4"></div>
              <div className="h-4 bg-surface-container-high rounded w-3/4 mb-2"></div>
              <div className="h-3 bg-surface-container-high rounded w-1/2"></div>
            </div>
          ))}
        </div>
      </section>

      {/* Leaderboard / Trending Tracks */}
      <section className="grid grid-cols-12 gap-8 mb-section-margin">
        <div className="col-span-4">
          <h3 className="font-headline-lg text-on-surface mb-6">Top Listeners</h3>
          <div className="space-y-4">
            {leaderboard.map((user, i) => (
              <div key={user.email} className="flex items-center gap-4 p-3 rounded-xl hover:bg-surface-container-high transition-colors group cursor-pointer">
                <span className="text-primary font-bold text-lg w-6">0{i+1}</span>
                <div className="w-12 h-12 rounded-lg overflow-hidden flex-shrink-0 bg-surface-container flex items-center justify-center">
                  {user.avatar_url ? (
                    <img className="w-full h-full object-cover" src={user.avatar_url} alt="avatar" />
                  ) : (
                    <span className="material-symbols-outlined text-on-surface-variant">person</span>
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-bold text-on-surface truncate">{user.display_name || user.email.split('@')[0]}</p>
                  <p className="text-xs text-on-surface-variant">Lvl {user.xp_level} • {Math.floor(user.hours_listened)} hrs</p>
                </div>
                <span className="material-symbols-outlined text-on-surface-variant opacity-0 group-hover:opacity-100 transition-opacity">emoji_events</span>
              </div>
            ))}
          </div>
        </div>
        <div className="col-span-8">
          <div className="relative h-full rounded-3xl overflow-hidden bg-surface-container-low p-8 border border-outline-variant group hover:border-primary/20 transition-colors">
            <div className="flex justify-between items-start mb-8">
              <div>
                <h3 className="font-headline-lg text-white">Live Concert Series</h3>
                <p className="text-on-surface-variant">Join 24k others listening live right now</p>
              </div>
              <span className="px-4 py-1 rounded-full bg-red-600 text-white text-xs font-bold animate-pulse uppercase tracking-widest">Live</span>
            </div>
            <div className="flex items-center gap-8">
              <div className="relative w-40 h-40 rounded-2xl overflow-hidden shadow-2xl">
                <img className="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBjKJZgnoX--zKEuo6S596H_8PdVi2DgtIfd74Z8MEfqHaedi258lVsWzurwQVxSHsGX_8BeU9B6N5bKtOBdzmUE9gVl4Ko6dBt4m_WvJcCdXOIe96NS7mnCmj9QHfP7VCgcsOm24znugYnp22yHQ5E_wHiPnBv7OZae6PNQX2D3hTPsp_zfgbQhB92YUle8ZAmm-zQqKy9xMw7ysj6uAyVnVZGzknnl3mDFOc5zj-6sKPF-4YEdcxZBg" />
                <div className="absolute inset-0 bg-primary/10 flex items-center justify-center">
                  <span className="material-symbols-outlined text-6xl text-white">graphic_eq</span>
                </div>
              </div>
              <div className="flex-1 z-10">
                <p className="text-primary font-bold text-sm uppercase tracking-widest mb-1">Now Streaming</p>
                <h4 className="text-3xl font-display-lg text-white mb-4">Midnight Residency Tour</h4>
                <div className="flex items-center gap-4 text-on-surface-variant mb-6">
                  <span className="flex items-center gap-1"><span className="material-symbols-outlined text-sm">schedule</span> 1h 45m left</span>
                  <span className="flex items-center gap-1"><span className="material-symbols-outlined text-sm">group</span> 2.4k watching</span>
                </div>
                <button className="bg-white text-black px-8 py-3 rounded-full font-bold flex items-center gap-2 hover:bg-primary hover:text-black transition-colors">
                  Join Room
                </button>
              </div>
            </div>
            {/* Interactive Visualizer */}
            <div className="absolute bottom-0 left-0 right-0 h-12 flex items-end justify-center gap-1 px-8 overflow-hidden pointer-events-none">
              <div className="w-2 bg-primary/20 rounded-t h-[40%] animate-[bounce_1.2s_infinite]"></div>
              <div className="w-2 bg-primary/40 rounded-t h-[70%] animate-[bounce_1.5s_infinite]"></div>
              <div className="w-2 bg-primary/60 rounded-t h-[50%] animate-[bounce_1s_infinite]"></div>
              <div className="w-2 bg-primary/80 rounded-t h-[90%] animate-[bounce_1.3s_infinite]"></div>
              <div className="w-2 bg-primary/60 rounded-t h-[60%] animate-[bounce_1.1s_infinite]"></div>
              <div className="w-2 bg-primary/40 rounded-t h-[30%] animate-[bounce_1.4s_infinite]"></div>
              <div className="w-2 bg-primary/20 rounded-t h-[80%] animate-[bounce_1.6s_infinite]"></div>
            </div>
          </div>
        </div>
      </section>
    </main>
  )
}

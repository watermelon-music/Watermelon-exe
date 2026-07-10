import { useEffect } from 'react'
import { HashRouter, Routes, Route, Navigate } from 'react-router-dom'
import { supabase } from './lib/supabase'
import { useAuthStore, usePlayerStore } from './lib/store'
import { playerEngine } from './lib/player'

import { Titlebar } from './components/Titlebar'
import { Sidebar } from './components/Sidebar'
import { Player } from './components/Player'

import { Login } from './pages/Login'
import { Home } from './pages/Home'
import { Search } from './pages/Search'
import { Library } from './pages/Library'
import { Leaderboard } from './pages/Leaderboard'
import { Premium } from './pages/Premium'
import { ProfilePage } from './pages/Profile'

export default function App() {
  const { user, isLoading, setUser, setSession, setProfile, setLoading } = useAuthStore()
  const { currentSong, isPlaying, volume, isMuted } = usePlayerStore()

  // Sync auth state with Supabase
  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(session)
      setUser(session?.user ?? null)
      if (session?.user) fetchProfile(session.user.id)
      else setLoading(false)
    })

    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setSession(session)
      setUser(session?.user ?? null)
      if (session?.user) fetchProfile(session.user.id)
      else { setProfile(null); setLoading(false) }
    })

    return () => subscription.unsubscribe()
  }, [])

  const fetchProfile = async (userId: string) => {
    const { data } = await supabase
      .from('profiles')
      .select('*')
      .eq('id', userId)
      .single()
    setProfile(data as any)
    setLoading(false)
  }

  // Sync player engine with store
  useEffect(() => {
    if (!currentSong) return
    playerEngine.load(currentSong.id)
    if (isPlaying) playerEngine.play()
    else playerEngine.pause()
  }, [currentSong?.id])

  useEffect(() => {
    if (isPlaying) playerEngine.play()
    else playerEngine.pause()
  }, [isPlaying])

  useEffect(() => {
    playerEngine.setVolume(volume)
    playerEngine.setMuted(isMuted)
  }, [volume, isMuted])

  if (isLoading) {
    return (
      <div style={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#080808' }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: 48, marginBottom: 16 }}>🍉</div>
          <div className="spinner" style={{ margin: '0 auto' }} />
        </div>
      </div>
    )
  }

  if (!user) {
    return <Login />
  }

  return (
    <HashRouter>
      <div className="app-shell">
        <Titlebar />
        <Sidebar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/search" element={<Search />} />
          <Route path="/library" element={<Library />} />
          <Route path="/leaderboard" element={<Leaderboard />} />
          <Route path="/premium" element={<Premium />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
        <Player />
      </div>
    </HashRouter>
  )
}

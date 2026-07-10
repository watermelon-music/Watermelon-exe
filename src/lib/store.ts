import { create } from 'zustand'
import type { Song } from './api'
import type { Profile } from './supabase'
import type { User, Session } from '@supabase/supabase-js'

interface PlayerState {
  currentSong: Song | null
  queue: Song[]
  queueIndex: number
  isPlaying: boolean
  volume: number
  isMuted: boolean
  position: number
  duration: number
  isBuffering: boolean
  isShuffled: boolean
  repeatMode: 'off' | 'one' | 'all'
  playSong: (song: Song, queue?: Song[]) => void
  pauseResume: () => void
  next: () => void
  prev: () => void
  setVolume: (v: number) => void
  toggleMute: () => void
  setPosition: (p: number) => void
  setDuration: (d: number) => void
  setIsPlaying: (p: boolean) => void
  setIsBuffering: (b: boolean) => void
  toggleShuffle: () => void
  toggleRepeat: () => void
  addToQueue: (song: Song) => void
}

interface AuthState {
  user: User | null
  session: Session | null
  profile: Profile | null
  isLoading: boolean
  setUser: (user: User | null) => void
  setSession: (session: Session | null) => void
  setProfile: (profile: Profile | null) => void
  setLoading: (l: boolean) => void
}

export const usePlayerStore = create<PlayerState>((set, get) => ({
  currentSong: null,
  queue: [],
  queueIndex: -1,
  isPlaying: false,
  volume: 0.8,
  isMuted: false,
  position: 0,
  duration: 0,
  isBuffering: false,
  isShuffled: false,
  repeatMode: 'off',
  playSong: (song, queue) => {
    const q = queue || [song]
    const idx = q.findIndex(s => s.id === song.id)
    set({ currentSong: song, queue: q, queueIndex: idx < 0 ? 0 : idx, isPlaying: true, position: 0 })
  },
  pauseResume: () => set(s => ({ isPlaying: !s.isPlaying })),
  next: () => {
    const { queue, queueIndex, repeatMode, isShuffled } = get()
    if (!queue.length) return
    let nextIdx: number
    if (repeatMode === 'one') nextIdx = queueIndex
    else if (isShuffled) nextIdx = Math.floor(Math.random() * queue.length)
    else {
      nextIdx = queueIndex + 1
      if (nextIdx >= queue.length) nextIdx = repeatMode === 'all' ? 0 : queueIndex
    }
    set({ currentSong: queue[nextIdx], queueIndex: nextIdx, isPlaying: true, position: 0 })
  },
  prev: () => {
    const { queue, queueIndex, position } = get()
    if (position > 3) { set({ position: 0 }); return }
    const prevIdx = Math.max(0, queueIndex - 1)
    set({ currentSong: queue[prevIdx], queueIndex: prevIdx, isPlaying: true, position: 0 })
  },
  setVolume: (v) => set({ volume: v, isMuted: v === 0 }),
  toggleMute: () => set(s => ({ isMuted: !s.isMuted })),
  setPosition: (p) => set({ position: p }),
  setDuration: (d) => set({ duration: d }),
  setIsPlaying: (p) => set({ isPlaying: p }),
  setIsBuffering: (b) => set({ isBuffering: b }),
  toggleShuffle: () => set(s => ({ isShuffled: !s.isShuffled })),
  toggleRepeat: () => set(s => ({
    repeatMode: s.repeatMode === 'off' ? 'all' : s.repeatMode === 'all' ? 'one' : 'off'
  })),
  addToQueue: (song) => set(s => ({ queue: [...s.queue, song] })),
}))

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  session: null,
  profile: null,
  isLoading: true,
  setUser: (user) => set({ user }),
  setSession: (session) => set({ session }),
  setProfile: (profile) => set({ profile }),
  setLoading: (isLoading) => set({ isLoading }),
}))

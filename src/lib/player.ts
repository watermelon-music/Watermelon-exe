import { usePlayerStore } from './store'
import { api } from './api'

class AudioPlayerEngine {
  private audio: HTMLAudioElement
  private currentId: string | null = null

  constructor() {
    this.audio = new Audio()
    this.audio.preload = 'auto'
    this.setupListeners()
  }

  private setupListeners() {
    this.audio.addEventListener('timeupdate', () => {
      usePlayerStore.setState({ position: this.audio.currentTime })
    })
    this.audio.addEventListener('durationchange', () => {
      usePlayerStore.setState({ duration: this.audio.duration || 0 })
    })
    this.audio.addEventListener('playing', () => {
      usePlayerStore.setState({ isPlaying: true, isBuffering: false })
    })
    this.audio.addEventListener('pause', () => {
      usePlayerStore.setState({ isPlaying: false })
    })
    this.audio.addEventListener('waiting', () => {
      usePlayerStore.setState({ isBuffering: true })
    })
    this.audio.addEventListener('canplay', () => {
      usePlayerStore.setState({ isBuffering: false })
    })
    this.audio.addEventListener('ended', () => {
      usePlayerStore.getState().next()
    })
    this.audio.addEventListener('error', () => {
      usePlayerStore.setState({ isPlaying: false, isBuffering: false })
    })
  }

  async load(songId: string) {
    if (this.currentId === songId) return
    this.currentId = songId
    
    // Fallback/clear src while loading
    this.audio.src = ''
    usePlayerStore.setState({ isBuffering: true })
    
    try {
      const url = await window.electron.ytdl.getStreamUrl(songId)
      if (url && this.currentId === songId) {
        this.audio.src = url
        this.audio.load()
        if (usePlayerStore.getState().isPlaying) {
          this.audio.play().catch(() => {})
        }
      } else if (!url) {
        throw new Error('Failed to get stream url')
      }
    } catch (err) {
      console.error('Playback error:', err)
      usePlayerStore.setState({ isPlaying: false, isBuffering: false })
    }
  }

  play() { this.audio.play().catch(() => {}) }
  pause() { this.audio.pause() }
  seek(seconds: number) { this.audio.currentTime = seconds }
  setVolume(v: number) { this.audio.volume = Math.max(0, Math.min(1, v)) }
  setMuted(muted: boolean) { this.audio.muted = muted }
  reset() { this.currentId = null; this.audio.src = ''; this.audio.load() }
}

export const playerEngine = new AudioPlayerEngine()

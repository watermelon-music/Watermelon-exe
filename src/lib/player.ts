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

  load(songId: string) {
    if (this.currentId === songId) return
    this.currentId = songId
    this.audio.src = api.streamUrl(songId)
    this.audio.load()
  }

  play() { this.audio.play().catch(() => {}) }
  pause() { this.audio.pause() }
  seek(seconds: number) { this.audio.currentTime = seconds }
  setVolume(v: number) { this.audio.volume = Math.max(0, Math.min(1, v)) }
  setMuted(muted: boolean) { this.audio.muted = muted }
  reset() { this.currentId = null; this.audio.src = ''; this.audio.load() }
}

export const playerEngine = new AudioPlayerEngine()

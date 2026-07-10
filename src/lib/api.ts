const BASE = import.meta.env.VITE_API_BASE || 'https://watermelon-api-oxx2.onrender.com'

export type Song = {
  id: string
  title: string
  artist: string
  thumbnail: string
  duration: number
  album?: string
}

export type LeaderboardUser = {
  display_name: string | null
  email: string
  hours_listened: number
  songs_played: number
  rank_tier: string | null
  xp_level: number
  avatar_url: string | null
}

export type AppStats = {
  totalUsers: number
  paidUsers: number
  freeUsers: number
  totalPlaylists: number
  totalStreams: number
  totalPlaytime: number
  githubStars: number
  apkDownloads: number
  latestReleaseTag: string
}

async function get<T>(path: string, params?: Record<string, string>): Promise<T> {
  const url = new URL(`${BASE}${path}`)
  if (params) Object.entries(params).forEach(([k, v]) => url.searchParams.set(k, v))
  const res = await fetch(url.toString())
  if (!res.ok) throw new Error(`API error ${res.status}: ${path}`)
  return res.json()
}

export const api = {
  search: (q: string) => get<Song[]>('/search', { q }),
  stats: () => get<AppStats>('/stats'),
  leaderboard: (limit = 50) => get<LeaderboardUser[]>('/api/leaderboard', { limit: String(limit) }),
  recommendations: async (data: { title: string, artist: string, language?: string, genre?: string }) => {
    const res = await fetch(`${BASE}/api/recommendations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return res.json()
  },
  releases: () => get<any[]>('/releases'),
  
  getArtist: (id: string) => get<any>(`/api/artists/${id}`),
  getArtistSongs: (id: string, limit = 50) => get<Song[]>(`/api/artists/${id}/songs`, { limit: String(limit) }),

  createOrder: async (data: {
    userId: string
    email: string
    phone: string
    name: string
    amount: number
    plan: string
  }) => {
    const res = await fetch(`${BASE}/payments/create-order`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return res.json()
  },

  verifyPayment: async (orderId: string, userId: string, plan: string) => {
    const res = await fetch(`${BASE}/payments/verify-payment`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderId, userId, plan }),
    })
    return res.json()
  },
}

import { createClient } from '@supabase/supabase-js'

const SUPABASE_URL = import.meta.env.VITE_SUPABASE_URL
const SUPABASE_ANON_KEY = import.meta.env.VITE_SUPABASE_ANON_KEY

export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
    storageKey: 'watermelon-auth',
  },
})

export type Profile = {
  id: string
  email: string
  display_name: string | null
  username: string | null
  avatar_url: string | null
  plan: 'FREE' | 'PRO' | 'PREMIUM'
  hours_listened: number
  songs_played: number
  rank_tier: string | null
  xp_level: number
  created_at: string
}

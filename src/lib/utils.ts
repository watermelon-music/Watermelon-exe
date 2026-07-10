export function formatDuration(ms: number): string {
  if (!ms || ms <= 0) return '0:00'
  const totalSec = Math.floor(ms / 1000)
  const min = Math.floor(totalSec / 60)
  const sec = totalSec % 60
  return `${min}:${sec.toString().padStart(2, '0')}`
}

export function formatSeconds(s: number): string {
  if (!s || s <= 0) return '0:00'
  const min = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${min}:${sec.toString().padStart(2, '0')}`
}

export function formatHours(hours: number): string {
  if (hours < 1) return `${Math.round(hours * 60)}m`
  return `${hours.toFixed(1)}h`
}

export function getInitials(name: string | null, email: string): string {
  const n = name || email
  return n.slice(0, 2).toUpperCase()
}

export function getRankEmoji(tier: string | null): string {
  switch (tier?.toUpperCase()) {
    case 'DIAMOND': return '💎'
    case 'PLATINUM': return '🏆'
    case 'GOLD': return '🥇'
    case 'SILVER': return '🥈'
    case 'BRONZE': return '🥉'
    default: return '🎵'
  }
}

export function formatNumber(n: number): string {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`
  if (n >= 1_000) return `${(n / 1_000).toFixed(1)}K`
  return String(n)
}

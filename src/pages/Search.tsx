import { useState, useCallback } from 'react'
import { Search as SearchIcon } from 'lucide-react'
import { api, type Song } from '../lib/api'
import { SongRow } from '../components/SongRow'

export function Search() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<Song[]>([])
  const [loading, setLoading] = useState(false)
  const [searched, setSearched] = useState(false)

  const handleSearch = useCallback(async (q: string) => {
    if (!q.trim()) return
    setLoading(true)
    setSearched(true)
    try {
      const data = await api.search(q.trim())
      setResults(data || [])
    } catch {
      setResults([])
    } finally {
      setLoading(false)
    }
  }, [])

  const handleKey = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch(query)
  }

  return (
    <div className="main-content">
      <div className="page-header">
        <h1 className="page-title">Search</h1>
        <p className="page-sub">Find any song, artist, or album</p>
      </div>

      <div className="search-input-wrap">
        <SearchIcon size={16} className="search-icon-pos" />
        <input
          className="search-input"
          type="text"
          placeholder="Search for songs, artists…"
          value={query}
          onChange={e => setQuery(e.target.value)}
          onKeyDown={handleKey}
          autoFocus
        />
      </div>

      {loading && <div className="loading-center"><div className="spinner" /></div>}

      {!loading && searched && results.length === 0 && (
        <div className="empty">
          <div className="empty-icon">🔍</div>
          <div className="empty-text">No results for "{query}"</div>
        </div>
      )}

      {!loading && results.length > 0 && (
        <div className="section">
          <div className="section-title">{results.length} results for "{query}"</div>
          {results.map(song => (
            <SongRow key={song.id} song={song} queue={results} />
          ))}
        </div>
      )}

      {!searched && (
        <div className="empty">
          <div className="empty-icon">🎵</div>
          <div className="empty-text">Start typing to search millions of songs</div>
        </div>
      )}
    </div>
  )
}

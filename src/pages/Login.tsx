import { useState } from 'react'
import { supabase } from '../lib/supabase'

export function Login() {
  const [tab, setTab] = useState<'login' | 'signup'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      if (tab === 'login') {
        const { error } = await supabase.auth.signInWithPassword({ email, password })
        if (error) throw error
      } else {
        const { error } = await supabase.auth.signUp({
          email, password,
          options: { data: { display_name: name } }
        })
        if (error) throw error
      }
    } catch (err: any) {
      setError(err.message || 'An error occurred')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">
          <span className="login-logo-icon">🍉</span>
          <div className="login-logo-name">Watermelon</div>
          <div className="login-logo-sub">Open Source Music Streaming</div>
        </div>

        <div className="login-tabs">
          <button
            className={`login-tab${tab === 'login' ? ' active' : ''}`}
            onClick={() => setTab('login')}
          >
            Sign In
          </button>
          <button
            className={`login-tab${tab === 'signup' ? ' active' : ''}`}
            onClick={() => setTab('signup')}
          >
            Sign Up
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {tab === 'signup' && (
            <div className="login-field">
              <label className="login-label">Display Name</label>
              <input
                className="login-input"
                type="text"
                placeholder="Your name"
                value={name}
                onChange={e => setName(e.target.value)}
                required
              />
            </div>
          )}
          <div className="login-field">
            <label className="login-label">Email</label>
            <input
              className="login-input"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="login-field">
            <label className="login-label">Password</label>
            <input
              className="login-input"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              minLength={6}
            />
          </div>
          {error && <div className="login-error">{error}</div>}
          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? 'Please wait…' : tab === 'login' ? 'Sign In' : 'Create Account'}
          </button>
        </form>
      </div>
    </div>
  )
}

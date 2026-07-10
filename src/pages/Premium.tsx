import { useState } from 'react'
import { useAuthStore } from '../lib/store'
import { api } from '../lib/api'

const PLANS = [
  {
    id: 'monthly',
    name: 'Monthly',
    price: '₹99',
    period: '/month',
    amount: 9900,
    features: ['Ad-free listening', 'HD audio quality', 'Offline mode', 'Priority support'],
  },
  {
    id: 'yearly',
    name: 'Yearly',
    price: '₹799',
    period: '/year',
    amount: 79900,
    features: ['Everything in Monthly', 'Save 33%', 'Exclusive badge', 'Early access to features'],
  },
]

export function Premium() {
  const { user, profile } = useAuthStore()
  const [selectedPlan, setSelectedPlan] = useState<string>('monthly')
  const [phone, setPhone] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const isPremium = profile?.plan !== 'FREE'

  const handleSubscribe = async () => {
    if (!user || !phone.trim()) return
    setLoading(true)
    setError('')
    try {
      const plan = PLANS.find(p => p.id === selectedPlan)!
      const { paymentLink, orderId } = await api.createOrder({
        userId: user.id,
        email: user.email || '',
        phone: phone.trim(),
        name: profile?.display_name || user.email?.split('@')[0] || 'User',
        amount: plan.amount,
        plan: selectedPlan,
      })
      if (paymentLink) {
        // Open in external browser
        window.open(paymentLink, '_blank')
      } else {
        setError('Failed to create payment order. Please try again.')
      }
    } catch (err: any) {
      setError(err.message || 'Payment failed')
    } finally {
      setLoading(false)
    }
  }

  if (isPremium) {
    return (
      <div className="main-content">
        <div className="premium-page">
          <div className="premium-hero">
            <div className="premium-icon">💎</div>
            <div className="premium-title">You're Premium!</div>
            <div className="premium-sub">
              Enjoy unlimited, ad-free music streaming with {profile?.plan} plan.
              <br />Thank you for supporting Watermelon! 🍉
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="main-content">
      <div className="premium-page">
        <div className="premium-hero">
          <div className="premium-icon">🍉</div>
          <div className="premium-title">Go Premium</div>
          <div className="premium-sub">Unlimited music, zero ads. Support open source.</div>
        </div>

        <div className="premium-plans">
          {PLANS.map(plan => (
            <div
              key={plan.id}
              className={`plan-card${selectedPlan === plan.id ? ' selected' : ''}`}
              onClick={() => setSelectedPlan(plan.id)}
            >
              <div className="plan-name">{plan.name}</div>
              <div className="plan-price">{plan.price}</div>
              <div className="plan-period">{plan.period}</div>
              <div className="plan-features">
                {plan.features.map((f, i) => (
                  <div key={i} className="plan-feature">{f}</div>
                ))}
              </div>
            </div>
          ))}
        </div>

        <div className="phone-field">
          <label className="login-label">Phone Number (for payment)</label>
          <input
            className="phone-input"
            type="tel"
            placeholder="+91 9876543210"
            value={phone}
            onChange={e => setPhone(e.target.value)}
          />
        </div>

        {error && <div className="login-error" style={{ marginBottom: 12 }}>{error}</div>}

        <button
          className="subscribe-btn"
          onClick={handleSubscribe}
          disabled={loading || !phone.trim()}
        >
          {loading ? 'Creating order…' : `Subscribe · ${PLANS.find(p => p.id === selectedPlan)?.price}`}
        </button>

        <div style={{ marginTop: 12, fontSize: 12, color: 'var(--text3)', textAlign: 'center' }}>
          Powered by Cashfree · Secure payment
        </div>
      </div>
    </div>
  )
}

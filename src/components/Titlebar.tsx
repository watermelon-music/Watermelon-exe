import { useState, useEffect } from 'react'
import { Minus, Square, X, Maximize2 } from 'lucide-react'

export function Titlebar() {
  const [isMax, setIsMax] = useState(false)

  const handleMaximize = async () => {
    await window.electron?.window.maximize()
    const maximized = await window.electron?.window.isMaximized()
    setIsMax(!!maximized)
  }

  return (
    <div className="titlebar">
      <div className="titlebar-logo">
        <span className="titlebar-icon">🍉</span>
        <span className="titlebar-name">Watermelon</span>
      </div>
      <div className="titlebar-drag" />
      <div className="titlebar-controls">
        <button className="titlebar-btn" onClick={() => window.electron?.window.minimize()}>
          <Minus size={12} />
        </button>
        <button className="titlebar-btn" onClick={handleMaximize}>
          {isMax ? <Square size={11} /> : <Maximize2 size={11} />}
        </button>
        <button className="titlebar-btn close" onClick={() => window.electron?.window.close()}>
          <X size={12} />
        </button>
      </div>
    </div>
  )
}

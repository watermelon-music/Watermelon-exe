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
    <div className="titlebar fixed top-0 left-0 w-full z-[100] bg-surface-container-lowest/50 backdrop-blur-sm pointer-events-none">
      <div className="titlebar-logo pointer-events-auto">
        <span className="titlebar-icon">🍉</span>
        <span className="titlebar-name">Watermelon</span>
      </div>
      <div className="titlebar-drag" />
      <div className="titlebar-controls pointer-events-auto">
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

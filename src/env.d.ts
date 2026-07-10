/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_SUPABASE_URL: string
  readonly VITE_SUPABASE_ANON_KEY: string
  readonly VITE_API_BASE: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare global {
  interface Window {
    electron: {
      window: {
        minimize: () => Promise<void>
        maximize: () => Promise<void>
        close: () => Promise<void>
        isMaximized: () => Promise<boolean>
      }
      updater: {
        onUpdateAvailable: (cb: () => void) => void
        onUpdateDownloaded: (cb: () => void) => void
        install: () => Promise<void>
      }
      platform: string
    }
  }
}

export {}

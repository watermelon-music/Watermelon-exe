import { contextBridge, ipcRenderer } from 'electron'

contextBridge.exposeInMainWorld('electron', {
  window: {
    minimize: () => ipcRenderer.invoke('window:minimize'),
    maximize: () => ipcRenderer.invoke('window:maximize'),
    close: () => ipcRenderer.invoke('window:close'),
    isMaximized: () => ipcRenderer.invoke('window:is-maximized'),
  },
  updater: {
    onUpdateAvailable: (cb: () => void) => ipcRenderer.on('update:available', cb),
    onUpdateDownloaded: (cb: () => void) => ipcRenderer.on('update:downloaded', cb),
    install: () => ipcRenderer.invoke('update:install'),
  },
  ytdl: {
    getStreamUrl: (id: string) => ipcRenderer.invoke('ytdl:get-stream-url', id),
  },
  platform: process.platform,
})

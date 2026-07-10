# Watermelon for Windows 🍉

A beautiful Windows desktop music streaming app built with **Electron + React + TypeScript**.

Connects to the same Supabase backend and Watermelon API as the Android app.

## Features

- 🔐 Login / Signup with Supabase Auth
- 🏠 Home with trending & recommendations
- 🔍 Music search (powered by watermelon-api)
- 🎵 Full music player (play, pause, seek, queue, volume)
- 📋 Library — playlists & liked songs
- 🏆 Leaderboard — real-time top listeners
- 💎 Premium upgrade via Cashfree
- 👤 User profile & stats
- 🔄 Auto-updater

## Development

```bash
# Install dependencies
npm install

# Run in development mode
npm run electron:dev

# Build Windows installer
npm run build
```

## Environment Variables

Copy `.env.example` to `.env` and fill in your credentials.

## Tech Stack

- **Electron** — desktop shell
- **React + TypeScript** — UI
- **Vite** — bundler
- **Zustand** — state management
- **Supabase** — auth & database
- **Watermelon API** — music streaming

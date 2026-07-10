import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

export function Header() {
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState('')

  const handleSearch = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`)
    }
  }

  return (
    <header className="flex justify-end items-center ml-60 px-8 h-20 pt-6 fixed top-0 right-0 left-0 z-30 bg-[#0a0a0a]/95 backdrop-blur-md">
      <div className="flex items-center gap-6 w-full justify-end max-w-2xl">
        <div className="relative group flex-1 max-w-md hidden md:block">
          <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-gray-500 text-[20px]">search</span>
          <input 
            type="text" 
            placeholder="Search songs, artists..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={handleSearch}
            className="bg-[#1a1a1a] border border-transparent rounded-full pl-12 pr-4 py-2.5 w-full text-sm text-white focus:ring-1 focus:ring-gray-600 focus:border-gray-600 transition-all placeholder:text-gray-500 outline-none" 
          />
        </div>
        <Link to="/profile" className="w-10 h-10 rounded-full flex items-center justify-center text-gray-400 hover:text-white hover:bg-[#1a1a1a] transition-colors cursor-pointer">
          <span className="material-symbols-outlined text-[22px]">settings</span>
        </Link>
      </div>
    </header>
  )
}

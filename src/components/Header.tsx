import { Link } from 'react-router-dom'

export function Header() {
  return (
    <header className="flex justify-between items-center ml-64 px-container-padding h-24 fixed top-0 right-0 left-0 z-30 bg-surface-container-lowest/95 backdrop-blur-md border-b border-outline-variant">
      <div className="flex items-center gap-8">
        <div className="flex items-center h-12 w-12 overflow-hidden rounded-lg shadow-sm border border-outline-variant bg-surface-container-low p-2">
          <img src="https://lh3.googleusercontent.com/aida-public/AB6AXuDGRNHu-S18Jt0YN2NrvWMms59TfNMUkbIQ-Hoeils77QGfN040uNDl9JR8wTtnuCp8bdZ2qWa3laM7M5uUGEXNiQCXd9pQ7EsMCXcIao9kOUINtcTst4iHCGDBgQnZh4LEjffR44NsnFYPX346gFuojqJxIF4crmEqfQ0h2eevEwDmM3i3tCe_ZqgFqRthxExcu2rJ6s1tSf4pghG5uPhsdRtXOZVGKxFuUmurU_Yri8Me4Wr6M5m_xXz2SDsLxLjZ2PM" alt="Logo" className="w-full h-full object-contain" />
        </div>
        {/* User Status Metrics (Pro UI) */}
        <div className="flex items-center gap-6 bg-surface-container-low px-6 py-3 rounded-full border border-outline-variant">
          <div className="flex flex-col">
            <span className="text-[10px] uppercase tracking-tighter text-on-surface-variant font-bold">Rank</span>
            <span className="text-title-md font-bold text-secondary">#12</span>
          </div>
          <div className="h-8 w-px bg-outline-variant"></div>
          <div className="flex flex-col">
            <span className="text-[10px] uppercase tracking-tighter text-on-surface-variant font-bold">Level</span>
            <span className="text-title-md font-bold text-primary">45</span>
          </div>
          <div className="w-24 h-2 bg-surface-container-highest rounded-full overflow-hidden">
            <div className="h-full bg-primary" style={{ width: '65%' }}></div>
          </div>
        </div>
      </div>
      <div className="flex items-center gap-6">
        <div className="relative group hidden md:block">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant">search</span>
          <input type="text" placeholder="Search artists, songs..." className="bg-surface-container border-none rounded-full pl-10 pr-4 py-2 w-64 text-body-md focus:ring-2 focus:ring-primary transition-all placeholder:text-on-surface-variant/50" />
        </div>
        <div className="flex items-center gap-4">
          <button className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-colors">notifications</button>
          {/* Profile Avatar Area */}
          <div className="flex items-center gap-3 pl-4 border-l border-outline-variant group cursor-pointer">
            <div className="text-right">
              <p className="text-sm font-bold text-on-surface">Alex Rivera</p>
              <p className="text-[10px] text-primary uppercase font-bold tracking-widest">Pro Member</p>
            </div>
            <div className="w-12 h-12 rounded-full border-2 border-primary overflow-hidden transition-transform group-hover:scale-105">
              <img className="w-full h-full object-cover" alt="Profile" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAT99exZ0cwZWBaU8BYloK-jC1jjuAmSGMII9m3Yj9TjGJYEhKB9pIAYnFt91yrKGm_hivYPYctD_NLAvB6tJH8mdx-jPU-zKlZQRCR4rtJKrIGbGtPS_kliMbK2ue4tR8Kl9u-SDYaTMG57GMr7zm_OJfy7RvDgaRZ35rM3vbONQeyP_EAGF5ZXpCbf96V8XgXQ57WweqxP9AE2ZaEIuN9hYtZmxopf7Bwf5X18qIxNIaK8HzUh4ZFeA" />
            </div>
          </div>
        </div>
      </div>
    </header>
  )
}

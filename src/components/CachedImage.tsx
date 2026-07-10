import { useState, useEffect } from 'react'

const CACHE_NAME = 'watermelon-images-v1'

// Helper to check if a date string is from "today before 5:30 AM" or "yesterday" relative to the 5:30 AM refresh rule.
function requiresRefresh(cachedAtISO: string): boolean {
  if (!cachedAtISO) return true
  
  const cachedDate = new Date(cachedAtISO)
  const now = new Date()
  
  // Create today's 5:30 AM threshold
  const todayThreshold = new Date(now)
  todayThreshold.setHours(5, 30, 0, 0)
  
  // If we are currently past 5:30 AM today, the cache MUST be from AFTER 5:30 AM today to be valid.
  if (now >= todayThreshold) {
    return cachedDate < todayThreshold
  } else {
    // If we are currently before 5:30 AM, the cache must be from AFTER 5:30 AM yesterday.
    const yesterdayThreshold = new Date(todayThreshold)
    yesterdayThreshold.setDate(yesterdayThreshold.getDate() - 1)
    return cachedDate < yesterdayThreshold
  }
}

export function useCachedImage(url: string | undefined) {
  const [src, setSrc] = useState<string | undefined>(undefined)

  useEffect(() => {
    if (!url) {
      setSrc(undefined)
      return
    }

    let isMounted = true

    async function loadImage() {
      try {
        const cache = await caches.open(CACHE_NAME)
        const response = await cache.match(url)
        
        if (response) {
          const cachedAt = response.headers.get('x-cached-at')
          if (cachedAt && !requiresRefresh(cachedAt)) {
            const blob = await response.blob()
            if (isMounted) setSrc(URL.createObjectURL(blob))
            return
          }
        }
        
        // Fetch new image
        const fetchRes = await fetch(url)
        if (!fetchRes.ok) throw new Error('Network error')
        
        const blob = await fetchRes.blob()
        
        // Save to cache with timestamp
        const headers = new Headers(fetchRes.headers)
        headers.set('x-cached-at', new Date().toISOString())
        
        const responseToCache = new Response(blob, {
          status: fetchRes.status,
          statusText: fetchRes.statusText,
          headers
        })
        
        await cache.put(url, responseToCache)
        
        if (isMounted) setSrc(URL.createObjectURL(blob))
        
      } catch (err) {
        console.error('Failed to load/cache image:', url, err)
        // Fallback to original URL on error
        if (isMounted) setSrc(url)
      }
    }

    loadImage()
    
    return () => { isMounted = false }
  }, [url])

  return src
}

interface CachedImageProps extends React.ImgHTMLAttributes<HTMLImageElement> {
  src: string
}

export function CachedImage({ src, alt, ...props }: CachedImageProps) {
  const cachedSrc = useCachedImage(src)
  
  if (!cachedSrc) {
    // Return a skeleton loading state if no src yet
    return <div className={`animate-pulse bg-surface-container ${props.className || ''}`} />
  }
  
  return <img src={cachedSrc} alt={alt} {...props} />
}

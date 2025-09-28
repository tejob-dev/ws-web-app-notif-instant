/** @type {import('next').NextConfig} */
const nextConfig = {
  // Configuration pour Docker
  output: 'standalone',
  
  // Configuration pour les variables d'environnement
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://69.197.142.189:5022',//'http://localhost:5022',
    NEXT_PUBLIC_WS_URL: process.env.NEXT_PUBLIC_WS_URL || 'ws://69.197.142.189:5022',//'ws://localhost:5022',
  },
  
  // Configuration pour les images
  images: {
    unoptimized: true
  }
}

module.exports = nextConfig

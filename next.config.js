/** @type {import('next').NextConfig} */
const nextConfig = {
  // Configuration pour Docker
  output: 'standalone',
  
  // Configuration pour les variables d'environnement
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3001',
    NEXT_PUBLIC_WS_URL: process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:3001',
  },
  
  // Configuration pour les images
  images: {
    unoptimized: true
  }
}

module.exports = nextConfig

'use client'

import { useState, useEffect } from 'react'
import { io, Socket } from 'socket.io-client'
import axios from 'axios'

interface Message {
  id: string
  content: string
  timestamp: Date
  type: 'info' | 'success' | 'warning' | 'error'
}

export default function Home() {
  const [socket, setSocket] = useState<Socket | null>(null)
  const [messages, setMessages] = useState<Message[]>([])
  const [newMessage, setNewMessage] = useState('')
  const [isConnected, setIsConnected] = useState(false)
  useEffect(() => {

    // Connexion WebSocket
    const newSocket = io('http://localhost:3001')
    setSocket(newSocket)

    newSocket.on('connect', () => {
      setIsConnected(true)
      console.log('Connecté au serveur WebSocket')
    })

    newSocket.on('disconnect', () => {
      setIsConnected(false)
      console.log('Déconnecté du serveur WebSocket')
    })

    newSocket.on('message', (data: Message) => {
      setMessages(prev => [...prev, data])
      console.log('Nouveau message reçu:', data)
    })

    return () => {
      newSocket.close()
    }
  }, [])

  const sendMessage = async () => {
    if (!newMessage.trim()) return

    try {
      await axios.post('http://localhost:3001/api/send-message', {
        content: newMessage,
        type: 'info'
      })
      setNewMessage('')
    } catch (error) {
      console.error('Erreur lors de l\'envoi du message:', error)
    }
  }


  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <h1 className="text-4xl font-bold text-center text-gray-800 mb-8">
            Notifications Instantanées
          </h1>
          
          {/* Status de connexion */}
          <div className="bg-white rounded-lg shadow-md p-6 mb-6">
            <div className="flex items-center justify-center">
              <div className="flex items-center space-x-4">
                <div className={`w-3 h-3 rounded-full ${isConnected ? 'bg-green-500' : 'bg-red-500'}`}></div>
                <span className="text-sm font-medium">
                  {isConnected ? 'Connecté au système WebSocket' : 'Déconnecté'}
                </span>
              </div>
            </div>
          </div>

          {/* Formulaire d'envoi */}
          <div className="bg-white rounded-lg shadow-md p-6 mb-6">
            <h2 className="text-xl font-semibold mb-4">Envoyer un message</h2>
            <div className="flex space-x-4">
              <input
                type="text"
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                placeholder="Tapez votre message..."
                className="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
              />
              <button
                onClick={sendMessage}
                disabled={!isConnected || !newMessage.trim()}
                className="px-6 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
              >
                Envoyer
              </button>
            </div>
          </div>

          {/* Messages */}
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-semibold mb-4">Messages reçus</h2>
            <div className="space-y-3 max-h-96 overflow-y-auto">
              {messages.length === 0 ? (
                <p className="text-gray-500 text-center py-8">
                  Aucun message reçu pour le moment
                </p>
              ) : (
                messages.map((message) => (
                  <div
                    key={message.id}
                    className={`p-4 rounded-lg border-l-4 ${
                      message.type === 'error'
                        ? 'bg-red-50 border-red-500'
                        : message.type === 'warning'
                        ? 'bg-yellow-50 border-yellow-500'
                        : message.type === 'success'
                        ? 'bg-green-50 border-green-500'
                        : 'bg-blue-50 border-blue-500'
                    }`}
                  >
                    <p className="text-gray-800">{message.content}</p>
                    <p className="text-xs text-gray-500 mt-1">
                      {new Date(message.timestamp).toLocaleString('fr-FR')}
                    </p>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

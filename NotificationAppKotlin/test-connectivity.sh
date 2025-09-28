#!/bin/bash

# Test simple de connectivité WebSocket
echo "🔍 Test de connectivité WebSocket vers 192.168.1.71:3002"

# Test avec netcat si disponible
if command -v nc >/dev/null 2>&1; then
    echo "📡 Test avec netcat..."
    timeout 5 nc -z 192.168.1.71 3002
    if [ $? -eq 0 ]; then
        echo "✅ Port 3002 accessible"
    else
        echo "❌ Port 3002 non accessible"
    fi
else
    echo "⚠️ netcat non disponible"
fi

# Test avec telnet si disponible
if command -v telnet >/dev/null 2>&1; then
    echo "📡 Test avec telnet..."
    timeout 5 telnet 192.168.1.71 3002 2>/dev/null | head -1
else
    echo "⚠️ telnet non disponible"
fi

echo ""
echo "💡 Si les tests échouent, vérifiez que:"
echo "   1. Le serveur backend fonctionne"
echo "   2. Le serveur WebSocket natif est démarré sur le port 3002"
echo "   3. Le firewall n'bloque pas le port 3002"
echo "   4. L'appareil Android est sur le même réseau"

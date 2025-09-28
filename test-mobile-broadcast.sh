#!/bin/bash

echo "🧪 Test de broadcast vers l'app mobile"
echo "======================================"

# Vérifier que le serveur fonctionne
echo "📡 Vérification du serveur Socket.IO..."
HEALTH_RESPONSE=$(curl -s http://localhost:3001/api/health)
echo "Réponse serveur: $HEALTH_RESPONSE"

if [[ $HEALTH_RESPONSE == *"OK"* ]]; then
    echo "✅ Serveur Socket.IO opérationnel"
else
    echo "❌ Serveur Socket.IO non accessible"
    exit 1
fi

echo ""
echo "📊 Statistiques actuelles:"
STATS_RESPONSE=$(curl -s http://localhost:3001/api/stats)
echo "Statistiques: $STATS_RESPONSE"

echo ""
echo "📱 Envoi de test de message broadcast..."
MESSAGE_RESPONSE=$(curl -s -X POST http://localhost:3001/api/send-message \
    -H "Content-Type: application/json" \
    -d '{"content":"🧪 Test broadcast mobile - '$(date +%H:%M:%S)'","type":"info"}')

echo "Réponse envoi: $MESSAGE_RESPONSE"

if [[ $MESSAGE_RESPONSE == *"success"* ]]; then
    echo "✅ Message envoyé avec succès"
    echo ""
    echo "📱 Vérifiez votre app mobile pour voir si le message apparaît"
    echo "🔍 Consultez les logs Android avec: adb logcat | grep SocketIOService"
else
    echo "❌ Erreur lors de l'envoi du message"
fi

echo ""
echo "🔄 Test de plusieurs messages..."
for i in {1..3}; do
    echo "Envoi message $i..."
    curl -s -X POST http://localhost:3001/api/send-message \
        -H "Content-Type: application/json" \
        -d "{\"content\":\"Message test $i - $(date +%H:%M:%S)\",\"type\":\"info\"}" > /dev/null
    sleep 1
done

echo "✅ Tests terminés"
echo "📱 Vérifiez votre app mobile pour confirmer la réception des messages"

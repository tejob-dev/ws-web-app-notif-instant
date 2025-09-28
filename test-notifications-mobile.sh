#!/bin/bash

echo "🔔 Test des notifications mobiles"
echo "================================="

# Vérifier que le serveur fonctionne
echo "📡 Vérification du serveur..."
HEALTH_RESPONSE=$(curl -s http://localhost:3001/api/health)
echo "Réponse serveur: $HEALTH_RESPONSE"

if [[ $HEALTH_RESPONSE == *"OK"* ]]; then
    echo "✅ Serveur opérationnel"
else
    echo "❌ Serveur non accessible"
    exit 1
fi

echo ""
echo "📱 Envoi de messages de test pour les notifications..."

# Test 1: Message simple
echo "📨 Test 1: Message simple"
curl -s -X POST http://localhost:3001/api/send-message \
    -H "Content-Type: application/json" \
    -d '{"content":"🔔 Notification test simple","type":"info"}' > /dev/null
echo "✅ Message simple envoyé"
sleep 2

# Test 2: Message d'urgence
echo "📨 Test 2: Message d'urgence"
curl -s -X POST http://localhost:3001/api/send-message \
    -H "Content-Type: application/json" \
    -d '{"content":"🚨 URGENCE - Test notification importante","type":"warning"}' > /dev/null
echo "✅ Message d'urgence envoyé"
sleep 2

# Test 3: Message de succès
echo "📨 Test 3: Message de succès"
curl -s -X POST http://localhost:3001/api/send-message \
    -H "Content-Type: application/json" \
    -d '{"content":"✅ Succès - Notification de confirmation","type":"success"}' > /dev/null
echo "✅ Message de succès envoyé"
sleep 2

# Test 4: Message avec emoji
echo "📨 Test 4: Message avec emoji"
curl -s -X POST http://localhost:3001/api/send-message \
    -H "Content-Type: application/json" \
    -d '{"content":"🎉 Félicitations! Notification avec emoji","type":"info"}' > /dev/null
echo "✅ Message avec emoji envoyé"

echo ""
echo "📊 Statistiques finales:"
STATS_RESPONSE=$(curl -s http://localhost:3001/api/stats)
echo "Statistiques: $STATS_RESPONSE"

echo ""
echo "🔍 Instructions de vérification:"
echo "1. Vérifiez votre app mobile - vous devriez voir 4 notifications"
echo "2. Consultez les logs Android: adb logcat | grep -E '(SocketIOService|NotificationService)'"
echo "3. Vérifiez que les notifications apparaissent dans la barre de notification"
echo "4. Testez le clic sur les notifications pour ouvrir l'app"

echo ""
echo "✅ Tests de notifications terminés"

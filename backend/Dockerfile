# Dockerfile pour le backend
FROM node:18-alpine

# Créer le répertoire de travail
WORKDIR /app

# Copier les fichiers package.json et package-lock.json
COPY package*.json ./

# Installer les dépendances
RUN npm ci --only=production

# Copier le code source
COPY . .

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nodejs -u 1001

# Créer les répertoires nécessaires avec les bonnes permissions
RUN mkdir -p /app/data && \
    touch /app/data/notifications.db && \
    chmod 755 /app/data && \
    chmod 664 /app/data/notifications.db && \
    chown -R nodejs:nodejs /app

# Exposer le port 3001
EXPOSE 3001

# Changer vers l'utilisateur non-root
USER nodejs

# Commande pour démarrer l'application
CMD ["npm", "start"]
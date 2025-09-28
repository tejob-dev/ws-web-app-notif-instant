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

# Créer le répertoire pour la base de données avec les bonnes permissions
RUN mkdir -p /app/data && chown -R nodejs:nodejs /app

# Copier le script d'initialisation
COPY init-db.sh /app/init-db.sh
RUN chmod +x /app/init-db.sh

# Exposer le port 3001
EXPOSE 3001

# Changer la propriété des fichiers
RUN chown -R nodejs:nodejs /app
USER nodejs

# Script de démarrage avec initialisation de la base de données
CMD ["sh", "-c", "./init-db.sh && npm start"]

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

# Créer le répertoire pour la base de données
RUN mkdir -p /app/data

# Exposer le port 3001
EXPOSE 3001

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nodejs -u 1001

# Changer la propriété des fichiers
RUN chown -R nodejs:nodejs /app
USER nodejs

# Commande pour démarrer l'application
CMD ["npm", "start"]

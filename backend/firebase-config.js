// Configuration Firebase pour le serveur
// Remplacez ces valeurs par votre configuration Firebase

const firebaseConfig = {
  // Option 1: Utiliser un fichier de service account
  serviceAccount: {
    type: "service_account",
    project_id: "votre-project-id",
    private_key_id: "votre-private-key-id",
    private_key: "-----BEGIN PRIVATE KEY-----\nVOTRE-CLE-PRIVEE\n-----END PRIVATE KEY-----\n",
    client_email: "firebase-adminsdk-xxxxx@votre-project-id.iam.gserviceaccount.com",
    client_id: "votre-client-id",
    auth_uri: "https://accounts.google.com/o/oauth2/auth",
    token_uri: "https://oauth2.googleapis.com/token",
    auth_provider_x509_cert_url: "https://www.googleapis.com/oauth2/v1/certs",
    client_x509_cert_url: "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-xxxxx%40votre-project-id.iam.gserviceaccount.com"
  },

  // Option 2: Utiliser les variables d'environnement
  // Définissez ces variables dans votre fichier .env
  envConfig: {
    projectId: process.env.FIREBASE_PROJECT_ID,
    privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'),
    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
  }
};

module.exports = firebaseConfig;

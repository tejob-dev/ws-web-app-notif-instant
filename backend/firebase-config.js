// Configuration Firebase pour le serveur
// Remplacez ces valeurs par votre configuration Firebase

const firebaseConfig = {
  // Option 1: Utiliser un fichier de service account
  serviceAccount: {
    type: "service_account",
    project_id: "ws-notif-app",
    private_key_id: "996c16e15a38f00c7e86f791a8d8925fd318ce9f",
    private_key: "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCqfljhAboIXLpe\niW7z9zf7zZbGYdjd1/GxhySfMAqqmXhK2N2lliqg14trBBu0qAlr8FpfiwEGfv3t\nD76Tbq1xaO9oew4dzcqFApvMnv4/tur35qM8yHvY//ZALY3b0N9//O8CS2wpLwMn\nfe1K0GQ0/qvjbk9GQGFhnzO3aJz9mYw2Rr98wsawyzXgWD68npMxWI7LVnxKg4Ze\n9DChQb6LmhCaPK/wonlgv6t3YsmZL6eRLKHYUxj1smOHCFsbC7LilJQFkfVswhEz\nspXEqo0MU0Di5xrYISkRfUBvw+l+wjhAdQjIJ5MR4cT4HYbc85C+4VrGzf6heNv7\nb+b2NjunAgMBAAECggEAMdaEOaXtESO2EQuApQ1HzrIe2Hfi0pqc2sXEBUqgW5VI\nLiztwck/RcwsQ4isZqdWNbjJWjOChbkaljErhuJsDgJcNYD/M+QjxrToAS0a6loY\nnoXLgumiFmFgRQtr8+0/YCT6YwPUJsfI1sL/WgbHvBwT+AQXZVRNgyFsFzmb5p+A\neK46ep+xqUXT/XZ1OVCvGXAJzoKdoXR9qwKgwkhg7XppZ5muPjZD/qsEVC2aHlis\nI8O1dX8q2CExMJnzOna9JDfS2JJ1RxOxksu3awc7Z5XmHHtXWBk33mhSEqnnadq4\nz8WGDOnKz9ckXX5k8bSutqBIiTRO588jaZ/6MPI5YQKBgQDrEf1cwjX1nnQK2UKS\nEch5/+7tamWriugLLP1UtYuigeXW3HqgG9kD/J8c0kQTt/QH/rBuBiKekuTRYWrG\nvEqDx1U48MkA61k59yuadfn6nF4B1WNY4E0b+Ud5Xc4VVEiGgHo6V/X4vu2hoW0c\nNvHQ1DImdtEuEWbb8E33qPQ5YQKBgQC5rHIFKCnp+U/ZLsDPkU7F+U58rve/t2vC\nuFua6wV2Xc4+pTBvVl3zf3DZKtfzK2SngrgNSwPf8mFd+xszLReAFhVLj8KCbrqD\nTCUHVmDKJlZwVjkRaVGW55R0QPOFy7C/UDigKBpa43X4J5FqXB1rLaqBT0nADSPR\nEAnIpNbqBwKBgQDfBahdsxzCtdze1hXkw95ycTFcKs/fGuDgiYSoM74RJjDL0068\nTO9uauUC0TMpiiOK2kbFfCioTjGtvUEy4D3KHPpsFXZ+2stCxZCm8TuEW4qNLskc\n72H5Il5/dVIqxlYw1gAhDocdwvdtm9wLIs32FPynpZ78mCIAlVNDnxZmwQKBgCZn\n2R3f9OuEulN+LDvDHrwsmWOI/Y4Wbp/Uan5c5sBZHvQuTxp4ju5zCfChwBF4hFVx\n4Ig/YAnOpP0/l5y6UwYXi0gZ8G3yYXoPIgmmgFbQH0kVTLChTFMei8KQPM8MFv87\npLrcELeEHT67UITjkOI+i+o7cFHdZe80lhF+p0W5AoGBAL2cPeKDHeEYaC+JnmwB\nU8OaJnvmrKoZN/F1FLj2/+sOgZggJfRmFYIzCQxvZX/DRB9Ilx78sYCzVDXAKC9l\nlv8CyM41Wy+MkKQCyoGo4OFwSIdZHaRrSbBtp/1/ebwENHkz31sjuvU/MSNNw1NZ\nZjeRYgdr1++98UvTWe7YaDHb\n-----END PRIVATE KEY-----\n",
    client_email: "firebase-adminsdk-fbsvc@ws-notif-app.iam.gserviceaccount.com",
    client_id: "116953750506722988222",
    auth_uri: "https://accounts.google.com/o/oauth2/auth",
    token_uri: "https://oauth2.googleapis.com/token",
    auth_provider_x509_cert_url: "https://www.googleapis.com/oauth2/v1/certs",
    client_x509_cert_url: "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40ws-notif-app.iam.gserviceaccount.com",
    universe_domain: "googleapis.com"
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

// Konfigurasi publik Firebase Web App & App Check untuk GitHub Pages.
// PENTING: JANGAN PERNAH menuliskan GEMINI_API_KEY rahasia di file ini.
// Akses Gemini di browser diamankan melalui Firebase AI Logic + App Check (reCAPTCHA Enterprise)
// ATAU melalui URL backend proxy aman Anda (Cloud Run / Firebase Functions / Cloudflare Worker).

export const PAI_WEB_CONFIG = {
  // Nama model Gemini pada Firebase AI Logic
  geminiModel: "gemini-3.8-flash",

  // Konfigurasi Web App dari Firebase Console -> Project Settings -> Your apps (Web)
  firebase: {
    apiKey: "AIzaSyB8L6sLPBYSIHmrlEbrtBIh-gerMrhIiGk",
    authDomain: "gen-lang-client-0358370863.firebaseapp.com",
    projectId: "gen-lang-client-0358370863",
    storageBucket: "gen-lang-client-0358370863.firebasestorage.app",
    messagingSenderId: "896427690268",
    appId: "1:896427690268:web:a338406531dcdd4f8092d9"
  },

  // Site Key reCAPTCHA Enterprise (Web) untuk domain aluqmanhakim222-ctrl.github.io
  recaptchaEnterpriseSiteKey: "6LchSzQuAAAAADZ3woo_iZUcRjZNzVuuLk19XxRD",

  // Opsional: URL endpoint POST JSON { action, prompt, schemaMode }.
  // Jika kosong, SDK akan memakai Firebase AI Logic.
  aiBackendEndpoint: ""
};

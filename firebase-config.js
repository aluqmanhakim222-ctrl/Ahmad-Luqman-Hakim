// Konfigurasi publik Firebase Web App & App Check untuk GitHub Pages.
// PENTING: JANGAN PERNAH menuliskan GEMINI_API_KEY rahasia di file ini.
// Akses Gemini di browser diamankan melalui Firebase AI Logic + App Check (reCAPTCHA Enterprise)
// ATAU melalui URL backend proxy aman Anda (Cloud Run / Firebase Functions / Cloudflare Worker).

export const PAI_WEB_CONFIG = {
  // Nama model Gemini pada Firebase AI Logic
  geminiModel: "gemini-3.8-flash",

  // Konfigurasi Web App dari Firebase Console -> Project Settings -> Your apps (Web)
  // Proyek Firebase yang terhubung pada aplikasi Android: "gen-lang-client-0358370863"
  firebase: {
    apiKey: "", // Isi dengan Web API Key publik dari Firebase Console (bukan Gemini API Key rahasia)
    authDomain: "gen-lang-client-0358370863.firebaseapp.com",
    projectId: "gen-lang-client-0358370863",
    storageBucket: "gen-lang-client-0358370863.firebasestorage.app",
    messagingSenderId: "896427690268",
    appId: "" // Isi dengan Web App ID (format: 1:896427690268:web:...) dari Firebase Console
  },

  // Site Key reCAPTCHA Enterprise (Web) untuk domain aluqmanhakim222-ctrl.github.io
  // Buat di Google Cloud Console -> Security -> reCAPTCHA Enterprise -> Create Key (Website)
  recaptchaEnterpriseSiteKey: "6LchSzQuAAAAADZ3woo_iZUcRjZNzVuuLk19XxRD",

  // Opsional: jika menggunakan backend proxy aman (Cloud Function / Cloud Run / Worker),
  // isi URL endpoint POST JSON { action, prompt, schemaMode }. Jika kosong, SDK akan memakai Firebase AI Logic.
  aiBackendEndpoint: ""
};

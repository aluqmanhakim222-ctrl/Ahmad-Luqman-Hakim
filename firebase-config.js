// Konfigurasi publik Firebase Web App & App Check untuk GitHub Pages.
// PENTING: JANGAN PERNAH menuliskan GEMINI_API_KEY rahasia di file ini.
// Akses Gemini di browser diamankan melalui Firebase AI Logic + App Check (reCAPTCHA Enterprise)
// ATAU melalui URL backend proxy aman Anda (Cloud Run / Firebase Functions / Cloudflare Worker).

export const PAI_WEB_CONFIG = {
  geminiModel: "gemini-3.5-flash-lite",
  firebase: {
    apiKey: "AIzaSyB8L6slPBYSIHmrlEbrtBIh-qerMrhIiGk",
    authDomain: "gen-lang-client-0358370863.firebaseapp.com",
    projectId: "gen-lang-client-0358370863",
    storageBucket: "gen-lang-client-0358370863.firebasestorage.app",
    messagingSenderId: "896427690268",
    appId: "1:896427690268:web:a338406531dcdd4f8092d9"
  },
  recaptchaEnterpriseSiteKey: "6LchSzQuAAAAADZ3woo_iZUcRjZNzVuuLk19XxRD",
  aiBackendEndpoint: ""
};

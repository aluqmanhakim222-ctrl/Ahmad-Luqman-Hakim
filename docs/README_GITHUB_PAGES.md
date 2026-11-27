# Panduan Publikasi GitHub Pages & Konfigurasi AI Tutor PAI (Versi Web)

Folder `docs/` berisi versi Web statis yang kompatibel penuh dengan **GitHub Pages** untuk halaman:
`https://aluqmanhakim222-ctrl.github.io/Ahmad-Luqman-Hakim/`

Seluruh desain ruang kelas 3D A-Frame, kamera first-person, tombol D-Pad/WASD, pemilihan 5 objek 3D, tombol `Reset Posisi Awal`, dan `Enter VR` dipertahankan, serta ditambahkan panel **AI Tutor PAI**.

## Daftar File Web (`docs/`)
- `docs/index.html` — Halaman utama Ruang Kelas Virtual 3D A-Frame + UI Drawer AI Tutor PAI.
- `docs/ai-tutor-web.js` — Logika AI Tutor PAI (Generate Studi Kasus, Analisis Strategi, Umpan Balik, Generate Kuis 5 Soal, dan Tanya AI Tutor) menggunakan Firebase AI Logic Web SDK + App Check atau Backend Proxy.
- `docs/ai-tutor-web.css` — Gaya responsif panel AI Tutor PAI & kartu objek 3D untuk desktop dan smartphone.
- `docs/firebase-config.js` — Konfigurasi publik Web App Firebase & App Check (tanpa menyimpan API key Gemini rahasia).

## Keamanan API Key
- **Jangan pernah menuliskan `GEMINI_API_KEY` di HTML, JavaScript, atau repositori GitHub publik.**
- Pada versi Web, pemanggilan Gemini AI diamankan melalui salah satu dari dua mekanisme berikut:
  1. **Firebase AI Logic Web SDK + Firebase App Check (reCAPTCHA Enterprise)** yang membatasi akses hanya dari domain `aluqmanhakim222-ctrl.github.io`.
  2. **Backend Proxy Aman** (Cloud Run / Firebase Cloud Functions / Cloudflare Worker) yang menyimpan `GEMINI_API_KEY` di sisi server.

## Langkah Konfigurasi Firebase AI Logic untuk Web
1. Buka **Firebase Console** -> pilih proyek `gen-lang-client-0358370863`.
2. Klik **Project Settings** -> **Your apps** -> **Add app** -> pilih ikon **Web (`</>`)**.
3. Daftarkan aplikasi web (mis. `PAI Immersive Web`), lalu salin nilai **`apiKey`** (Web API Key publik) dan **`appId`** (`1:896427690268:web:...`).
4. Buka **Google Cloud Console** -> **Security** -> **reCAPTCHA Enterprise** -> **Create Key**:
   - Platform type: **Website**
   - Domains: tambahkan `aluqmanhakim222-ctrl.github.io` (dan `localhost` jika ingin menguji di komputer lokal).
   - Salin **Site Key**.
5. Buka kembali **Firebase Console** -> **App Check** -> pilih aplikasi Web yang baru dibuat -> daftar/tautkan **reCAPTCHA Enterprise** menggunakan key tersebut.
6. Pastikan di **Firebase Console** -> **AI Logic**, layanan **Gemini Developer API** sudah diaktifkan dan proyek terhubung ke billing yang berlaku bila diperlukan kuota produksi.
7. Masukkan `apiKey`, `appId`, dan `recaptchaEnterpriseSiteKey` ke file `docs/firebase-config.js` (atau uji sementara lewat menu **Pengaturan Koneksi Firebase AI Logic / Backend** di dalam panel AI Tutor PAI di browser).

## Langkah Publikasi ke GitHub Pages
1. Pastikan seluruh file di folder `docs/` sudah di-commit dan di-push ke repositori `Ahmad-Luqman-Hakim`.
2. Di GitHub, buka **Settings** -> **Pages**.
3. Pada **Build and deployment** -> **Source**, pilih **Deploy from a branch**.
4. Pilih branch utama (misalnya `main`) dan folder **`/docs`**, lalu klik **Save**.
   - *(Catatan: Jika halaman GitHub Pages Anda saat ini memakai root `/` dan bukan `/docs`, salin isi folder `docs/` ke root repositori agar tidak menimpa file yang tidak diinginkan, atau pindahkan source GitHub Pages ke `/docs`).*
5. Tunggu proses *Actions / Pages build* selesai, lalu buka `https://aluqmanhakim222-ctrl.github.io/Ahmad-Luqman-Hakim/` dan lakukan pengujian langsung di browser.

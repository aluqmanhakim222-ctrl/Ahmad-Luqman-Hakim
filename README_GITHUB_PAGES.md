# PAI Immersive — GitHub Pages

Aplikasi web PAI Immersive dipublikasikan sebagai situs statis melalui GitHub Pages.

**URL:** https://aluqmanhakim222-ctrl.github.io/Ahmad-Luqman-Hakim/

## Sumber publikasi saat ini

Pengaturan GitHub Pages pada repositori ini menggunakan **Deploy from a branch → main → /(root)**. Karena itu, file yang dibaca situs harus berada di root repositori:

- `index.html` — ruang kelas virtual 3D A-Frame, navigasi first-person, objek interaktif, panel AI Tutor.
- `ai-tutor-web.js` — alur AI Tutor: studi kasus, analisis strategi, umpan balik, kuis lima soal, dan tanya jawab.
- `ai-tutor-web.css` — gaya panel dan tata letak responsif.
- `firebase-config.js` — konfigurasi publik Firebase Web App dan App Check; tidak boleh menyimpan Gemini secret API key.

Folder `docs/` adalah salinan sumber dokumentasi/arsip dan **bukan** sumber yang sedang dipublikasikan, kecuali pengaturan Pages sengaja diubah ke `/docs`.

## Status fitur

Ruang kelas 3D dan antarmuka AI Tutor dapat dimuat dari halaman statis. Pemanggilan Gemini belum aktif sampai konfigurasi Web App dan App Check diselesaikan atau URL backend proxy tersedia. Tombol AI tidak menghasilkan jawaban tiruan saat konfigurasi belum tersedia.

## Aktivasi Gemini dengan Firebase AI Logic

1. Buka Firebase Console untuk proyek `gen-lang-client-0358370863`.
2. Pada **Project settings → Your apps**, daftarkan aplikasi Web dan salin Firebase Web config, khususnya `apiKey` dan `appId`.
3. Buat kunci **reCAPTCHA Enterprise untuk Website** dengan domain `aluqmanhakim222-ctrl.github.io`.
4. Hubungkan key tersebut ke Firebase App Check untuk aplikasi Web.
5. Aktifkan Firebase AI Logic / Gemini Developer API pada proyek.
6. Masukkan Web API Key, Web App ID, dan reCAPTCHA Enterprise Site Key ke panel **Pengaturan Koneksi Firebase AI Logic / Backend** di situs (pengaturan tersimpan di browser tersebut), atau isi nilai publik yang sesuai ke `firebase-config.js` sebelum commit.
7. Uji satu per satu tombol Generate Studi Kasus, Analisis Strategi, Umpan Balik, Generate Kuis, dan Tanya AI Tutor.

**Keamanan:** jangan memasukkan `GEMINI_API_KEY` rahasia ke HTML, JavaScript, atau repositori publik. Untuk skenario backend, simpan secret hanya di environment server dan batasi endpoint dengan autentikasi, validasi input, rate limiting, serta CORS yang sesuai.

## Pemeriksaan setelah perubahan

Setiap commit ke branch `main` memicu build/deployment GitHub Pages. Periksa tab **Actions** sampai workflow Pages selesai, lalu buka URL situs dengan hard refresh. Pastikan kontrol ruang 3D, panel AI Tutor, dan tampilan layar kecil berfungsi. Status AI harus tetap menunjukkan konfigurasi belum lengkap sampai pengaturan Firebase/Backend benar-benar berhasil diuji.

## Mode pengembangan alternatif

Repositori ini juga berisi proyek Android di folder `app/`. Publikasi GitHub Pages hanya melayani file web statis di root dan tidak membangun aplikasi Android.

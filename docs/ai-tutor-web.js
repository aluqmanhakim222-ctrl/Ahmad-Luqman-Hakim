import { PAI_WEB_CONFIG } from "./firebase-config.js";
import { initializeApp, getApps } from "https://www.gstatic.com/firebasejs/13.4.0/firebase-app.js";
import { initializeAppCheck, ReCaptchaEnterpriseProvider } from "https://www.gstatic.com/firebasejs/13.4.0/firebase-app-check.js";
import { getAI, getGenerativeModel, GoogleAIBackend, Schema } from "https://www.gstatic.com/firebasejs/13.4.0/firebase-ai.js";

const STORAGE_KEY = "pai_web_runtime_config_v1";

const OBJECT_CATALOG = {
  papan_tulis: {
    title: "Papan Tulis Interaktif",
    category: "Perencanaan & Strategi PAI",
    desc: "Memetakan kompetensi dasar PAI, gaya belajar siswa (Visual, Auditori, Kinestetik), serta langkah PBL, PjBL, CTL, Cooperative Learning, dan Demonstrasi.",
    suggestedTopic: "Analisis Perencanaan Strategi Pembelajaran PAI"
  },
  mushaf: {
    title: "Mushaf Al-Qur'an & Rehal",
    category: "Sumber Primer Al-Qur'an & Adab",
    desc: "Landasan nilai wahyu dalam PAI (tabayyun, ta'awun, musyawarah, hikmah) serta praktik pembelajaran berbasis tilawah, tadabbur, dan demonstrasi adab.",
    suggestedTopic: "Pembelajaran Al-Qur'an Hadis & Adab Berbasis Demonstrasi dan CTL"
  },
  kitab_pesantren: {
    title: "Kitab & Tradisi Pesantren",
    category: "Kearifan Lokal Nusantara",
    desc: "Mengintegrasikan tradisi sorogan, bandongan, bahtsul masail, keteladanan kiai, dan musyawarah santri ke dalam strategi PAI modern.",
    suggestedTopic: "Integrasi Tradisi Pesantren dalam Strategi PBL dan Cooperative Learning"
  },
  panel_batik: {
    title: "Panel Batik & Kearifan Lokal",
    category: "Budaya Lokal & Gotong Royong",
    desc: "Mengaitkan filosofi motif batik Nusantara, nilai gotong royong, serta kehidupan masyarakat sekitar sebagai media pembelajaran PAI kontekstual.",
    suggestedTopic: "Pembelajaran PAI Kontekstual Berbasis Batik, Gotong Royong, dan Budaya Lokal"
  },
  meja_siswa: {
    title: "Klaster Meja & 4 Siswa Beragam",
    category: "Diferensiasi & Karakteristik Siswa",
    desc: "Simulasi kelas heterogen dengan siswa Visual, Auditori, Kinestetik, dan kemampuan akademik beragam untuk merancang pembelajaran PAI inklusif.",
    suggestedTopic: "Diferensiasi Strategi Pembelajaran PAI pada Kelas Heterogen"
  }
};

const SYSTEM_ACADEMIC_INSTRUCTION = `Anda adalah Dosen & Tutor AI ahli mata kuliah Strategi Pembelajaran Pendidikan Agama Islam (PAI) di perguruan tinggi Indonesia.
Gunakan bahasa Indonesia akademis yang jelas, sistematis, santun, dan aplikatif untuk mahasiswa calon guru PAI.
ATURAN WAJIB INTEGRITAS DALIL:
- Jangan pernah mengarang ayat Al-Qur'an, nomor ayat, teks Arab, hadis, atau referensi kitab palsu.
- Jika merujuk ajaran Islam secara tematik, sebutkan prinsip nilainya secara akurat (misalnya: tabayyun, ta'awun, musyawarah, amanah, hikmah, akhlakul karimah) tanpa mengklaim kutipan harfiah yang dikarang.
- Fokus pada analisis pedagogis: karakteristik peserta didik, kondisi kelas, materi PAI, integrasi kearifan lokal Indonesia (tradisi pesantren, gotong royong, batik dan budaya lokal, kehidupan masyarakat sekitar), serta strategi pembelajaran (Problem-Based Learning / PBL, Project-Based Learning / PjBL, Cooperative Learning, Contextual Teaching and Learning / CTL, dan Demonstrasi).`;

const QUIZ_SCHEMA = Schema.object({
  properties: {
    questions: Schema.array({
      description: "Daftar tepat 5 soal pilihan ganda baru tentang Strategi Pembelajaran PAI",
      items: Schema.object({
        properties: {
          topic: Schema.string({ description: "Subtopik spesifik Strategi Pembelajaran PAI" }),
          question: Schema.string({ description: "Pertanyaan studi kasus tingkat mahasiswa calon guru PAI" }),
          options: Schema.array({
            description: "Tepat 4 opsi jawaban pilihan ganda",
            items: Schema.string()
          }),
          correctIndex: Schema.integer({ description: "Indeks jawaban benar dari 0 sampai 3" }),
          explanation: Schema.string({ description: "Pembahasan pedagogis lengkap untuk kunci jawaban" })
        }
      })
    })
  }
});

const state = {
  selectedObjectId: "",
  selectedLocalWisdoms: new Set(["Tradisi Pesantren", "Gotong Royong"]),
  selectedStrategy: "Problem-Based Learning (PBL)",
  caseGenerationIndex: 0,
  quizGenerationIndex: 0,
  recentCaseSnippets: [],
  recentQuizQuestions: [],
  quizItems: [],
  quizAnswers: {},
  showQuizAnswerKey: false,
  isBusy: false,
  firebaseInstance: null,
  aiClient: null,
  appCheckInitialized: false,
  lastEngineSignature: ""
};

function loadMergedConfig() {
  let saved = {};
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (raw) saved = JSON.parse(raw);
  } catch (_) {
    saved = {};
  }
  const baseFb = PAI_WEB_CONFIG.firebase || {};
  const savedFb = saved.firebase || {};
  return {
    geminiModel: saved.geminiModel || PAI_WEB_CONFIG.geminiModel || "gemini-3.8-flash",
    firebase: {
      apiKey: (savedFb.apiKey ?? baseFb.apiKey ?? "").trim(),
      authDomain: (savedFb.authDomain ?? baseFb.authDomain ?? "").trim(),
      projectId: (savedFb.projectId ?? baseFb.projectId ?? "").trim(),
      storageBucket: (savedFb.storageBucket ?? baseFb.storageBucket ?? "").trim(),
      messagingSenderId: (savedFb.messagingSenderId ?? baseFb.messagingSenderId ?? "").trim(),
      appId: (savedFb.appId ?? baseFb.appId ?? "").trim()
    },
    recaptchaEnterpriseSiteKey: (saved.recaptchaEnterpriseSiteKey ?? PAI_WEB_CONFIG.recaptchaEnterpriseSiteKey ?? "").trim(),
    aiBackendEndpoint: (saved.aiBackendEndpoint ?? PAI_WEB_CONFIG.aiBackendEndpoint ?? "").trim()
  };
}

function getConfigurationMode(cfg) {
  if (cfg.aiBackendEndpoint) {
    return { ready: true, mode: "backend" };
  }
  const fb = cfg.firebase;
  const hasFirebaseWeb = Boolean(fb.apiKey && fb.projectId && fb.appId);
  const hasAppCheck = Boolean(cfg.recaptchaEnterpriseSiteKey);
  if (hasFirebaseWeb && hasAppCheck) {
    return { ready: true, mode: "firebase-ai-logic" };
  }
  return {
    ready: false,
    mode: "unconfigured",
    hasFirebaseWeb,
    hasAppCheck
  };
}

function el(id) {
  return document.getElementById(id);
}

function setBanner(kind, message) {
  const banner = el("ai-status-banner");
  if (!banner) return;
  banner.className = "status-banner" + (kind ? " " + kind : "");
  banner.textContent = message;
}

function updateConfigStatusBanner() {
  const cfg = loadMergedConfig();
  const status = getConfigurationMode(cfg);
  if (status.mode === "backend") {
    setBanner(
      "ready",
      `Mode Backend Proxy aktif (${cfg.aiBackendEndpoint}). Klik tombol fitur AI Tutor PAI di bawah untuk menguji koneksi Gemini secara langsung.`
    );
  } else if (status.mode === "firebase-ai-logic") {
    setBanner(
      "ready",
      `Firebase AI Logic + App Check siap diinisialisasi untuk proyek ${cfg.firebase.projectId} (model ${cfg.geminiModel}). Klik tombol fitur AI untuk menguji panggilan langsung ke Gemini.`
    );
  } else {
    const missing = [];
    if (!cfg.firebase.apiKey) missing.push("Web API Key");
    if (!cfg.firebase.appId) missing.push("Web App ID");
    if (!cfg.recaptchaEnterpriseSiteKey) missing.push("reCAPTCHA Enterprise Site Key");
    setBanner(
      "",
      `Menunggu konfigurasi Web Firebase AI Logic / Backend Proxy (belum terisi: ${missing.join(", ")}). Aplikasi tidak memakai jawaban simulasi—isi 'Pengaturan Koneksi' di bawah atau lengkapi firebase-config.js agar Gemini aktif di browser.`
    );
  }
}

function ensureAiClient() {
  const cfg = loadMergedConfig();
  const status = getConfigurationMode(cfg);
  if (!status.ready) {
    throw new Error(
      "Konfigurasi Firebase Web App + App Check (reCAPTCHA Enterprise) atau URL Backend Proxy belum diisi. Silakan lengkapi bagian 'Pengaturan Koneksi Firebase AI / Backend' atau file firebase-config.js."
    );
  }
  if (status.mode === "backend") {
    return { mode: "backend", cfg };
  }

  const signature = `${cfg.firebase.projectId}|${cfg.firebase.appId}|${cfg.firebase.apiKey}|${cfg.recaptchaEnterpriseSiteKey}`;
  if (!state.aiClient || state.lastEngineSignature !== signature) {
    const appName = "pai-immersive-web-tutor";
    const existing = getApps().find((a) => a.name === appName);
    const app = existing || initializeApp(cfg.firebase, appName);
    if (!state.appCheckInitialized) {
      initializeAppCheck(app, {
        provider: new ReCaptchaEnterpriseProvider(cfg.recaptchaEnterpriseSiteKey),
        isTokenAutoRefreshEnabled: true
      });
      state.appCheckInitialized = true;
    }
    state.firebaseInstance = app;
    state.aiClient = getAI(app, { backend: new GoogleAIBackend() });
    state.lastEngineSignature = signature;
  }

  return { mode: "firebase-ai-logic", cfg, ai: state.aiClient };
}

function getContextInputs() {
  const objInfo = OBJECT_CATALOG[state.selectedObjectId];
  const objectContext = objInfo
    ? `${objInfo.title} (${objInfo.category}) — ${objInfo.desc}`
    : "Eksplorasi umum ruang kelas virtual 3D PAI";
  const learnerProfile = (el("tutor-learner")?.value || "").trim() || "Siswa beragam: Visual, Auditori, dan Kinestetik";
  const classCondition = (el("tutor-class-cond")?.value || "").trim() || "Kelas MTs/MA heterogen, 32 siswa, perangkat proyektor & ruang diskusi kelompok";
  const paiMaterial = (el("tutor-topic")?.value || "").trim() || "Akhlak Mulia, Muamalah, dan Kepedulian Sosial dalam PAI";
  const strategy = state.selectedStrategy;
  const rationale = (el("tutor-rationale")?.value || "").trim();
  const question = (el("tutor-question")?.value || "").trim();
  const localWisdoms = state.selectedLocalWisdoms.size
    ? Array.from(state.selectedLocalWisdoms).join(", ")
    : "Tradisi Pesantren, Gotong Royong";

  return {
    objectContext,
    learnerProfile,
    classCondition,
    paiMaterial,
    strategy,
    rationale,
    question,
    localWisdoms
  };
}

function setBusy(busy, badgeText = "") {
  state.isBusy = busy;
  const actionIds = [
    "btn-ai-case",
    "btn-ai-analyze",
    "btn-ai-feedback",
    "btn-ai-quiz",
    "btn-ai-ask"
  ];
  for (const id of actionIds) {
    const button = el(id);
    if (button) button.disabled = busy;
  }
  const badge = el("ai-output-badge");
  if (badge && badgeText) badge.textContent = badgeText;
}

function showTextOutput(title, badge, text) {
  const card = el("ai-output-card");
  const titleEl = el("ai-output-title");
  const badgeEl = el("ai-output-badge");
  const bodyEl = el("ai-output-text");
  const quizWrap = el("ai-quiz-container");
  if (quizWrap) quizWrap.style.display = "none";
  if (card) card.style.display = "block";
  if (titleEl) titleEl.textContent = title;
  if (badgeEl) badgeEl.textContent = badge;
  if (bodyEl) {
    bodyEl.style.display = "block";
    bodyEl.textContent = text;
  }
}

async function runStreamingPrompt({ title, prompt, temperature = 0.75, topP = 0.95 }) {
  setBusy(true, "Memanggil Gemini...");
  showTextOutput(title, "Memproses...", "Menghubungkan ke Gemini AI secara aman...");

  try {
    const engine = ensureAiClient();
    const bodyEl = el("ai-output-text");
    const badgeEl = el("ai-output-badge");

    if (engine.mode === "backend") {
      const resp = await fetch(engine.cfg.aiBackendEndpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          action: "text",
          model: engine.cfg.geminiModel,
          systemInstruction: SYSTEM_ACADEMIC_INSTRUCTION,
          prompt,
          generationConfig: { temperature, topP }
        })
      });
      if (!resp.ok) {
        throw new Error(`Server backend mengembalikan HTTP ${resp.status}`);
      }
      const data = await resp.json();
      const text = (data.text || data.output || "").trim();
      if (!text) throw new Error("Respons teks dari backend kosong.");
      if (bodyEl) bodyEl.textContent = text;
      if (badgeEl) badgeEl.textContent = "Gemini AI (Backend)";
      setBanner("ready", "Respons Gemini AI berhasil diterima melalui backend proxy.");
      return text;
    }

    const model = getGenerativeModel(engine.ai, {
      model: engine.cfg.geminiModel,
      systemInstruction: SYSTEM_ACADEMIC_INSTRUCTION,
      generationConfig: { temperature, topP }
    });

    const result = await model.generateContentStream(prompt);
    let accumulated = "";
    for await (const chunk of result.stream) {
      const chunkText = chunk.text();
      if (chunkText) {
        accumulated += chunkText;
        if (bodyEl) bodyEl.textContent = accumulated;
        if (badgeEl) badgeEl.textContent = "Gemini AI (Streaming)";
      }
    }
    if (!accumulated.trim()) {
      throw new Error("Gemini AI mengembalikan respons kosong.");
    }
    if (badgeEl) badgeEl.textContent = "Gemini AI Aktif";
    setBanner("ready", "Koneksi Firebase AI Logic + App Check berhasil dan respons Gemini diterima.");
    return accumulated;
  } catch (err) {
    const msg = formatGeminiError(err);
    setBanner("error", msg);
    showTextOutput(title, "Gagal", msg);
    return "";
  } finally {
    setBusy(false);
  }
}

function formatGeminiError(err) {
  const raw = (err && err.message) ? String(err.message) : String(err || "Terjadi kesalahan.");
  if (raw.toLowerCase().includes("appcheck") || raw.toLowerCase().includes("recaptcha")) {
    return `Verifikasi App Check / reCAPTCHA gagal: ${raw}. Pastikan domain GitHub Pages (aluqmanhakim222-ctrl.github.io) terdaftar pada reCAPTCHA Enterprise & Firebase App Check.`;
  }
  if (raw.toLowerCase().includes("api key") || raw.toLowerCase().includes("permission") || raw.includes("403")) {
    return `Akses Firebase AI Logic ditolak: ${raw}. Periksa Web API Key, App ID, dan status aktivasi Gemini Developer API / Firebase AI Logic di Firebase Console.`;
  }
  return raw;
}

async function handleGenerateCaseStudy() {
  if (state.isBusy) return;
  const ctx = getContextInputs();
  state.caseGenerationIndex += 1;
  const nonce = `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 6)}`;
  const avoidText = state.recentCaseSnippets.length
    ? state.recentCaseSnippets.map((item, idx) => `${idx + 1}. ${item}`).join("\n")
    : "Belum ada studi kasus sebelumnya.";

  const prompt = `Buatlah SATU STUDI KASUS BARU pembelajaran PAI yang unik (variasi ke-${state.caseGenerationIndex}, kode ${nonce}).
Parameter pembelajaran:
- Objek Ruang Kelas VR aktif: ${ctx.objectContext}
- Karakteristik peserta didik: ${ctx.learnerProfile}
- Kondisi kelas: ${ctx.classCondition}
- Materi PAI: ${ctx.paiMaterial}
- Kearifan lokal yang wajib diintegrasikan: ${ctx.localWisdoms} (angkat konteks nyata tradisi pesantren, gotong royong, batik/budaya lokal, atau kehidupan masyarakat sekitar sesuai pilihan).

Ketentuan wajib:
1. Skenario HARUS berbeda sudut pandang, latar daerah madrasah/sekolah, dan problematika kelas dibanding kasus sebelumnya:
${avoidText}
2. Sertakan struktur rapi:
   A. Judul Kasus & Latar Konteks Madrasah/Sekolah
   B. Profil Kelas, Kondisi Belajar & Tantangan Pembelajaran PAI
   C. Titik Masuk Integrasi Kearifan Lokal
   D. Pertanyaan Pemantik Diskusi Mahasiswa Calon Guru PAI (3 butir)
   E. Saran Strategi Awal yang Layak Diuji`;

  const output = await runStreamingPrompt({
    title: `Studi Kasus PAI #${state.caseGenerationIndex}`,
    prompt,
    temperature: 0.95,
    topP: 0.95
  });

  if (output) {
    const snippet = output.replace(/\s+/g, " ").slice(0, 140);
    state.recentCaseSnippets = [...state.recentCaseSnippets.slice(-3), snippet];
  }
}

async function handleAnalyzeStrategy() {
  if (state.isBusy) return;
  const ctx = getContextInputs();
  const prompt = `Lakukan ANALISIS STRATEGI PEMBELAJARAN PAI secara mendalam dan objektif untuk skenario kelas berikut:
- Objek VR fokus: ${ctx.objectContext}
- Karakteristik peserta didik: ${ctx.learnerProfile}
- Kondisi kelas: ${ctx.classCondition}
- Materi PAI: ${ctx.paiMaterial}
- Kearifan lokal: ${ctx.localWisdoms}
- Strategi kandidat yang dipilih mahasiswa: ${ctx.strategy}
- Alasan mahasiswa (jika ada): ${ctx.rationale || "(belum dituliskan)"}

Tugas Anda:
1. Bandingkan secara ringkas kecocokan 5 strategi berikut untuk kasus di atas:
   - Problem-Based Learning (PBL)
   - Project-Based Learning (PjBL)
   - Cooperative Learning
   - Contextual Teaching and Learning (CTL)
   - Demonstrasi
2. Tentukan strategi PALING TEPAT beserta alasan pedagogis yang kuat (keterkaitan dengan gaya belajar siswa, kondisi kelas, tujuan materi PAI, dan kearifan lokal).
3. Berikan rancangan sintaks pembelajaran PAI 3 langkah (Pendahuluan, Inti bermuatan kearifan lokal, Penutup/Asesmen).`;

  await runStreamingPrompt({
    title: "Analisis Strategi Pembelajaran PAI",
    prompt,
    temperature: 0.7
  });
}

async function handleFeedback() {
  if (state.isBusy) return;
  const ctx = getContextInputs();
  const prompt = `Berikan UMPAN BALIK PEDAGOGIS terhadap keputusan strategi mahasiswa calon guru PAI berikut:
- Strategi yang dipilih mahasiswa: ${ctx.strategy}
- Alasan / rancangan singkat mahasiswa: ${ctx.rationale || "Mahasiswa memilih strategi tersebut untuk menyesuaikan karakteristik siswa dan kearifan lokal tanpa uraian panjang."}
- Konteks kelas: ${ctx.learnerProfile} | ${ctx.classCondition}
- Materi PAI: ${ctx.paiMaterial}
- Kearifan lokal: ${ctx.localWisdoms}

Berikan evaluasi terstruktur:
1. Penilaian Kesesuaian Strategi (Skor Kecocokan 1-100 & penjelasan objektif)
2. Kekuatan Pilihan Mahasiswa
3. Celah atau Risiko di Kelas Nyata & Saran Perbaikan
4. Contoh Modifikasi Aktivitas agar inklusif bagi siswa Visual, Auditori, dan Kinestetik.`;

  await runStreamingPrompt({
    title: "Umpan Balik Evaluasi Strategi",
    prompt,
    temperature: 0.72
  });
}

async function handleAskTutor() {
  if (state.isBusy) return;
  const ctx = getContextInputs();
  const question = ctx.question || `Bagaimana cara mengintegrasikan ${ctx.localWisdoms} dengan strategi ${ctx.strategy} pada materi ${ctx.paiMaterial}?`;
  const prompt = `Pertanyaan mahasiswa tentang Strategi Pembelajaran PAI:
"${question}"

Konteks aktif mahasiswa di ruang kelas virtual 3D:
- Objek VR terpilih: ${ctx.objectContext}
- Karakteristik siswa: ${ctx.learnerProfile}
- Kondisi kelas: ${ctx.classCondition}
- Materi PAI: ${ctx.paiMaterial}
- Kearifan lokal: ${ctx.localWisdoms}
- Strategi yang sedang dikaji: ${ctx.strategy}

Jawab pertanyaan secara kontekstual, jelas, terstruktur dengan langkah praktis yang mudah diterapkan calon guru PAI.`;

  await runStreamingPrompt({
    title: "Jawaban AI Tutor PAI",
    prompt,
    temperature: 0.7
  });
}

async function handleGenerateQuiz() {
  if (state.isBusy) return;
  const ctx = getContextInputs();
  state.quizGenerationIndex += 1;

  setBusy(true, "Menyusun 5 Soal...");
  const card = el("ai-output-card");
  const titleEl = el("ai-output-title");
  const bodyEl = el("ai-output-text");
  const quizWrap = el("ai-quiz-container");
  if (card) card.style.display = "block";
  if (titleEl) titleEl.textContent = `Kuis Evaluasi PAI (Sesi #${state.quizGenerationIndex})`;
  if (quizWrap) quizWrap.style.display = "none";
  if (bodyEl) {
    bodyEl.style.display = "block";
    bodyEl.textContent = "Menghasilkan 5 soal baru berbasis Strategi Pembelajaran PAI melalui Gemini AI...";
  }

  const avoidText = state.recentQuizQuestions.length
    ? state.recentQuizQuestions.slice(-10).map((q, idx) => `${idx + 1}. ${q}`).join("\n")
    : "Belum ada soal sebelumnya.";

  const prompt = `Buatkan TEPAT 5 soal kuis pilihan ganda BARU tentang Strategi Pembelajaran PAI untuk mahasiswa calon guru PAI (Sesi #${state.quizGenerationIndex}).
Konteks aktif:
- Materi fokus: ${ctx.paiMaterial}
- Karakteristik kelas: ${ctx.learnerProfile} (${ctx.classCondition})
- Kearifan lokal: ${ctx.localWisdoms}
- Strategi terkait: PBL, PjBL, Cooperative Learning, CTL, Demonstrasi.

Ketentuan wajib:
1. Array "questions" harus berisi tepat 5 soal yang saling berbeda.
2. Setiap soal memiliki tepat 4 opsi jawaban yang masuk akal, 1 correctIndex (0..3), dan pembahasan pedagogis yang jelas.
3. Hindari pengulangan dari soal berikut:
${avoidText}
4. Jangan mengarang ayat Al-Qur'an atau hadis palsu.`;

  try {
    const engine = ensureAiClient();
    let parsed;

    if (engine.mode === "backend") {
      const resp = await fetch(engine.cfg.aiBackendEndpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          action: "quiz",
          model: engine.cfg.geminiModel,
          systemInstruction: SYSTEM_ACADEMIC_INSTRUCTION,
          prompt,
          generationConfig: {
            temperature: 0.85,
            responseMimeType: "application/json"
          }
        })
      });
      if (!resp.ok) {
        throw new Error(`Server backend mengembalikan HTTP ${resp.status}`);
      }
      const data = await resp.json();
      parsed = typeof data.json === "object" ? data.json : JSON.parse(data.text || "{}");
    } else {
      const quizModel = getGenerativeModel(engine.ai, {
        model: engine.cfg.geminiModel,
        systemInstruction: SYSTEM_ACADEMIC_INSTRUCTION,
        generationConfig: {
          temperature: 0.85,
          responseMimeType: "application/json",
          responseSchema: QUIZ_SCHEMA
        }
      });
      const result = await quizModel.generateContent(prompt);
      const rawText = result.response.text();
      if (!rawText) throw new Error("Respons JSON kuis dari Gemini AI kosong.");
      parsed = JSON.parse(rawText);
    }

    const questions = Array.isArray(parsed?.questions) ? parsed.questions.slice(0, 5) : [];
    if (questions.length < 5) {
      throw new Error(`Gemini hanya mengembalikan ${questions.length} soal (dibutuhkan 5 soal). Silakan coba ulang.`);
    }

    state.quizItems = questions.map((item, idx) => ({
      topic: String(item.topic || ctx.paiMaterial),
      question: String(item.question || `Soal ${idx + 1}`),
      options: Array.isArray(item.options) ? item.options.slice(0, 4).map(String) : [],
      correctIndex: Math.min(3, Math.max(0, Number(item.correctIndex || 0))),
      explanation: String(item.explanation || "Pembahasan tidak tersedia.")
    }));

    for (const q of state.quizItems) {
      if (q.options.length !== 4) {
        throw new Error("Format opsi jawaban tidak lengkap (harus 4 pilihan per soal).");
      }
    }

    state.quizAnswers = {};
    state.showQuizAnswerKey = false;
    state.recentQuizQuestions = [
      ...state.recentQuizQuestions.slice(-10),
      ...state.quizItems.map((q) => q.question)
    ];

    if (bodyEl) bodyEl.style.display = "none";
    const badgeEl = el("ai-output-badge");
    if (badgeEl) badgeEl.textContent = "5 Soal Gemini AI";
    setBanner("ready", "5 soal kuis baru berhasil dibuat langsung oleh Gemini AI.");
    renderQuiz();
  } catch (err) {
    const msg = formatGeminiError(err);
    setBanner("error", msg);
    showTextOutput("Kuis Evaluasi PAI", "Gagal", msg);
  } finally {
    setBusy(false);
  }
}

function renderQuiz() {
  const container = el("ai-quiz-container");
  if (!container) return;
  container.replaceChildren();
  container.style.display = "block";

  const total = state.quizItems.length;
  let answeredCount = 0;
  let correctCount = 0;
  state.quizItems.forEach((item, idx) => {
    const chosen = state.quizAnswers[idx];
    if (typeof chosen === "number") {
      answeredCount += 1;
      if (chosen === item.correctIndex) correctCount += 1;
    }
  });

  const summary = document.createElement("div");
  summary.className = "status-banner ready";
  summary.textContent = `Terjawab: ${answeredCount}/${total} soal • Skor Sementara: ${correctCount}/${total}`;
  container.appendChild(summary);

  const toggleBtn = document.createElement("button");
  toggleBtn.type = "button";
  toggleBtn.className = "outline-btn full-width";
  toggleBtn.style.marginTop = "8px";
  toggleBtn.textContent = state.showQuizAnswerKey
    ? "Sembunyikan Kunci Jawaban & Pembahasan"
    : "Lihat Kunci Jawaban & Pembahasan";
  toggleBtn.addEventListener("click", () => {
    state.showQuizAnswerKey = !state.showQuizAnswerKey;
    renderQuiz();
  });
  container.appendChild(toggleBtn);

  state.quizItems.forEach((item, qIdx) => {
    const box = document.createElement("div");
    box.className = "quiz-question-box";

    const topicEl = document.createElement("div");
    topicEl.className = "obj-card-cat";
    topicEl.textContent = `${qIdx + 1}. ${item.topic}`;
    box.appendChild(topicEl);

    const qText = document.createElement("div");
    qText.style.fontSize = "12.5px";
    qText.style.fontWeight = "700";
    qText.style.marginTop = "6px";
    qText.textContent = item.question;
    box.appendChild(qText);

    const chosen = state.quizAnswers[qIdx];
    const hasAnswered = typeof chosen === "number";

    item.options.forEach((optText, oIdx) => {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "quiz-opt-btn";
      const letter = String.fromCharCode(65 + oIdx);
      btn.textContent = `${letter}. ${optText}`;

      if (hasAnswered || state.showQuizAnswerKey) {
        if (oIdx === item.correctIndex) {
          btn.classList.add("correct");
        } else if (hasAnswered && oIdx === chosen && chosen !== item.correctIndex) {
          btn.classList.add("wrong");
        }
      }

      btn.addEventListener("click", () => {
        state.quizAnswers[qIdx] = oIdx;
        renderQuiz();
      });
      box.appendChild(btn);
    });

    if (hasAnswered || state.showQuizAnswerKey) {
      const explain = document.createElement("div");
      explain.className = "quiz-explain";
      const keyLetter = String.fromCharCode(65 + item.correctIndex);
      explain.textContent = `Kunci Jawaban: ${keyLetter}. Pembahasan: ${item.explanation}`;
      box.appendChild(explain);
    }

    container.appendChild(box);
  });
}

function updateSelectedObjectUI(objId) {
  state.selectedObjectId = objId || "";
  const info = OBJECT_CATALOG[state.selectedObjectId];

  const card = el("web-object-card");
  const titleEl = el("web-obj-title");
  const catEl = el("web-obj-cat");
  const descEl = el("web-obj-desc");
  const contextLabel = el("tutor-context-obj");
  const topicInput = el("tutor-topic");

  if (info) {
    if (titleEl) titleEl.textContent = info.title;
    if (catEl) catEl.textContent = info.category;
    if (descEl) descEl.textContent = info.desc;
    if (card) card.classList.add("visible");
    if (contextLabel) contextLabel.textContent = `Objek VR Terpilih: ${info.title}`;
    if (topicInput && !topicInput.dataset.userEdited) {
      topicInput.value = info.suggestedTopic;
    }
  } else {
    if (card) card.classList.remove("visible");
    if (contextLabel) contextLabel.textContent = "Objek VR Terpilih: Eksplorasi Umum Ruang Kelas";
  }
}

function openDrawer(objId) {
  if (objId) updateSelectedObjectUI(objId);
  el("ai-tutor-backdrop")?.classList.add("open");
  el("ai-tutor-drawer")?.classList.add("open");
}

function closeDrawer() {
  el("ai-tutor-backdrop")?.classList.remove("open");
  el("ai-tutor-drawer")?.classList.remove("open");
}

function bindChips() {
  document.querySelectorAll("[data-wisdom]").forEach((btn) => {
    const value = btn.getAttribute("data-wisdom");
    if (state.selectedLocalWisdoms.has(value)) btn.classList.add("active");
    btn.addEventListener("click", () => {
      if (state.selectedLocalWisdoms.has(value)) {
        if (state.selectedLocalWisdoms.size > 1) {
          state.selectedLocalWisdoms.delete(value);
          btn.classList.remove("active");
        }
      } else {
        state.selectedLocalWisdoms.add(value);
        btn.classList.add("active");
      }
    });
  });

  document.querySelectorAll("[data-strategy]").forEach((btn) => {
    const value = btn.getAttribute("data-strategy");
    if (value === state.selectedStrategy) btn.classList.add("active");
    btn.addEventListener("click", () => {
      state.selectedStrategy = value;
      document.querySelectorAll("[data-strategy]").forEach((other) => {
        other.classList.toggle("active", other === btn);
      });
    });
  });
}

function bindRuntimeConfigForm() {
  const cfg = loadMergedConfig();
  if (el("cfg-api-key")) el("cfg-api-key").value = cfg.firebase.apiKey;
  if (el("cfg-app-id")) el("cfg-app-id").value = cfg.firebase.appId;
  if (el("cfg-project-id")) el("cfg-project-id").value = cfg.firebase.projectId;
  if (el("cfg-recaptcha-key")) el("cfg-recaptcha-key").value = cfg.recaptchaEnterpriseSiteKey;
  if (el("cfg-backend-url")) el("cfg-backend-url").value = cfg.aiBackendEndpoint;

  el("btn-save-config")?.addEventListener("click", () => {
    const payload = {
      firebase: {
        apiKey: (el("cfg-api-key")?.value || "").trim(),
        appId: (el("cfg-app-id")?.value || "").trim(),
        projectId: (el("cfg-project-id")?.value || "").trim() || PAI_WEB_CONFIG.firebase.projectId
      },
      recaptchaEnterpriseSiteKey: (el("cfg-recaptcha-key")?.value || "").trim(),
      aiBackendEndpoint: (el("cfg-backend-url")?.value || "").trim()
    };
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
    state.aiClient = null;
    state.lastEngineSignature = "";
    updateConfigStatusBanner();
  });

  el("btn-clear-config")?.addEventListener("click", () => {
    window.localStorage.removeItem(STORAGE_KEY);
    state.aiClient = null;
    state.lastEngineSignature = "";
    const resetCfg = loadMergedConfig();
    if (el("cfg-api-key")) el("cfg-api-key").value = resetCfg.firebase.apiKey;
    if (el("cfg-app-id")) el("cfg-app-id").value = resetCfg.firebase.appId;
    if (el("cfg-project-id")) el("cfg-project-id").value = resetCfg.firebase.projectId;
    if (el("cfg-recaptcha-key")) el("cfg-recaptcha-key").value = resetCfg.recaptchaEnterpriseSiteKey;
    if (el("cfg-backend-url")) el("cfg-backend-url").value = resetCfg.aiBackendEndpoint;
    updateConfigStatusBanner();
  });
}

function initWebAiTutor() {
  bindChips();
  bindRuntimeConfigForm();
  updateConfigStatusBanner();

  const topicInput = el("tutor-topic");
  if (topicInput) {
    topicInput.addEventListener("input", () => {
      topicInput.dataset.userEdited = "true";
    });
  }

  el("web-obj-close")?.addEventListener("click", () => {
    el("web-object-card")?.classList.remove("visible");
  });
  el("web-obj-open-tutor")?.addEventListener("click", () => {
    openDrawer(state.selectedObjectId);
  });

  el("ai-tutor-close")?.addEventListener("click", closeDrawer);
  el("ai-tutor-backdrop")?.addEventListener("click", closeDrawer);

  el("btn-ai-case")?.addEventListener("click", handleGenerateCaseStudy);
  el("btn-ai-analyze")?.addEventListener("click", handleAnalyzeStrategy);
  el("btn-ai-feedback")?.addEventListener("click", handleFeedback);
  el("btn-ai-quiz")?.addEventListener("click", handleGenerateQuiz);
  el("btn-ai-ask")?.addEventListener("click", handleAskTutor);

  window.paiWebTutor = {
    open: openDrawer,
    close: closeDrawer,
    onObjectSelectedInScene: updateSelectedObjectUI
  };
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initWebAiTutor);
} else {
  initWebAiTutor();
}

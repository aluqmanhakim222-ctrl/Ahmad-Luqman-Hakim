package com.example.data

import com.example.model.ClassroomObjectItem
import com.example.model.LocalWisdomScenario
import com.example.model.PedagogicalOption
import com.example.model.QuizQuestionItem
import com.example.model.StrategyCaseScenario
import com.example.model.StrategyOption
import com.example.model.StudentProfileScenario

object LearningContentCatalog {

    val cpmkObjectives: List<Pair<String, String>> = listOf(
        "CPMK 1 — Pemetaan Karakteristik Peserta Didik" to
            "Mahasiswa mampu menganalisis ragam gaya belajar (visual, auditori, kinestetik) serta keragaman kesiapan akademik siswa dalam pembelajaran PAI.",
        "CPMK 2 — Kontekstualisasi Kearifan Lokal Nusantara" to
            "Mahasiswa mampu mengintegrasikan nilai-nilai tradisi pesantren, budaya gotong royong, filosofi batik, dan adat lokal ke dalam desain materi PAI.",
        "CPMK 3 — Pemilihan Strategi Pembelajaran PAI Tepat" to
            "Mahasiswa mampu mengevaluasi dan menentukan strategi aktif (PBL, PjBL, Cooperative Learning, CTL, Demonstrasi, Diskusi Interaktif) sesuai karakteristik materi dan masalah kelas.",
        "CPMK 4 — Desain Inovatif Berbantuan VR & Generative AI" to
            "Mahasiswa mampu memanfaatkan laboratorium virtual 3D dan asisten Generative AI secara etis dan akademis untuk merancang skenario, asesmen, dan rancangan kegiatan PAI."
    )

    val classroomObjects: List<ClassroomObjectItem> = listOf(
        ClassroomObjectItem(
            id = "papan_tulis",
            name = "Papan Tulis Interaktif",
            category = "Media Visual & Kognitif",
            description = "Papan tulis digital madrasah yang menampilkan peta konsep tujuan pembelajaran, langkah strategi, dan rangkuman diskusi kelas.",
            pedagogicalValue = "Membantu siswa bertipe visual dan kemampuan akademik beragam memahami alur berpikir konseptual secara terstruktur.",
            suggestedStrategy = "Problem Based Learning & Peta Konsep"
        ),
        ClassroomObjectItem(
            id = "mushaf",
            name = "Mushaf Al-Qur'an & Rehal",
            category = "Sumber Primer PAI",
            description = "Mushaf Al-Qur'an yang diletakkan di atas rehal kayu berukir di meja utama sebagai rujukan autentik nilai wahyu dan adab belajar.",
            pedagogicalValue = "Menegaskan orientasi nilai spiritual, ketelitian bacaan (tahsin/tartil), dan penghormatan terhadap sumber primer ajaran Islam.",
            suggestedStrategy = "Demonstrasi & Talaqqi / Musyafahah"
        ),
        ClassroomObjectItem(
            id = "kitab_pesantren",
            name = "Kitab & Tradisi Pesantren",
            category = "Kearifan Pendidikan Islam",
            description = "Koleksi kitab kuning dan catatan bandongan/sorogan yang merepresentasikan sanad keilmuan khas pesantren Nusantara.",
            pedagogicalValue = "Memperkuat tradisi literasi klasik, ketawadhuan ilmiah, dan dialog antara teks turats dengan tantangan kontemporer.",
            suggestedStrategy = "Diskusi Interaktif & Bahtsul Masail"
        ),
        ClassroomObjectItem(
            id = "panel_batik",
            name = "Panel Batik & Kearifan Lokal",
            category = "Konteks Budaya Lokal",
            description = "Panel edukatif bermotif batik Kawung dan Parang yang dipadukan dengan nilai akhlak mulia, keselarasan sosial, dan gotong royong.",
            pedagogicalValue = "Menghadirkan jembatan kultural agar nilai akidah dan akhlak mudah dipahami melalui simbol budaya sehari-hari siswa.",
            suggestedStrategy = "Contextual Teaching and Learning (CTL)"
        ),
        ClassroomObjectItem(
            id = "meja_siswa",
            name = "Klaster Meja & Kursi Siswa",
            category = "Lingkungan Kolaboratif",
            description = "Penataan meja siswa berkelompok yang memfasilitasi kerja tim, tanya jawab aktif, dan interaksi antar siswa berkemampuan beragam.",
            pedagogicalValue = "Mendukung interaksi sosial konstruktif, tutor sebaya, serta keterlibatan siswa kinestetik dan auditori dalam proyek bersama.",
            suggestedStrategy = "Cooperative Learning & Project Based Learning"
        )
    )

    val studentScenarios: List<StudentProfileScenario> = listOf(
        StudentProfileScenario(
            id = "visual_student",
            studentName = "Nadia (Gaya Belajar Visual)",
            learningStyle = "Visual",
            academicProfile = "Cepat memahami materi jika melihat bagan, skema alur, ilustrasi ruang 3D, dan warna pembeda.",
            classroomBehavior = "Sering mencoret-coret peta pikiran saat mendengar ceramah panjang fikih muamalah dan tampak kehilangan fokus jika tidak ada media visual.",
            options = listOf(
                PedagogicalOption(
                    id = "opt_v1",
                    title = "Gunakan infografis alur akad, papan tulis interaktif, dan kartu konsep visual",
                    rationale = "Tepat! Siswa visual membutuhkan representasi spasial dan grafis terstruktur agar hubungan antar konsep fikih terlihat jelas."
                ),
                PedagogicalOption(
                    id = "opt_v2",
                    title = "Perbanyak ceramah lisan satu arah selama 60 menit tanpa media papan tulis",
                    rationale = "Kurang tepat. Ceramah murni tanpa dukungan visual membuat siswa bertipe visual cepat jenuh dan sulit mengorganisasi informasi."
                ),
                PedagogicalOption(
                    id = "opt_v3",
                    title = "Minta siswa hanya menghafal definisi teks buku tanpa bagan ringkasan",
                    rationale = "Kurang tepat. Hafalan verbal monoton mengabaikan kekuatan pemrosesan visual dan menurunkan pemahaman bermakna."
                )
            ),
            bestOptionId = "opt_v1"
        ),
        StudentProfileScenario(
            id = "auditory_student",
            studentName = "Fajar (Gaya Belajar Auditori)",
            learningStyle = "Auditori",
            academicProfile = "Menyerap konsep paling baik melalui dialog lisan, lantunan bacaan, diskusi kelompok, dan penjelasan verbal yang ritmis.",
            classroomBehavior = "Aktif bertanya saat sesi tanya jawab, suka membacakan teks secara pelan, namun kesulitan jika diminta membaca senyap terlalu lama.",
            options = listOf(
                PedagogicalOption(
                    id = "opt_a1",
                    title = "Berikan tugas membaca teks panjang dalam diam secara individu penuh waktu",
                    rationale = "Kurang tepat. Siswa auditori membutuhkan stimulasi pendengaran dan kesempatan mengartikulasikan gagasan secara lisan."
                ),
                PedagogicalOption(
                    id = "opt_a2",
                    title = "Terapkan diskusi terstruktur, talaqqi bacaan, dan presentasi argumen lisan",
                    rationale = "Tepat! Pendekatan dialogis, mendengarkan pelafalan autentik, dan diskusi terarah mengoptimalkan kekuatan auditori siswa."
                ),
                PedagogicalOption(
                    id = "opt_a3",
                    title = "Ganti seluruh sesi interaksi lisan dengan lembar kerja tertulis saja",
                    rationale = "Kurang tepat. Menghilangkan ruang dialog menutup saluran belajar utama bagi peserta didik auditori."
                )
            ),
            bestOptionId = "opt_a2"
        ),
        StudentProfileScenario(
            id = "kinesthetic_student",
            studentName = "Rafi (Gaya Belajar Kinestetik)",
            learningStyle = "Kinestetik",
            academicProfile = "Belajar efektif lewat praktik langsung, simulasi gerakan, eksplorasi objek, dan aktivitas fisik bermakna.",
            classroomBehavior = "Gelisah apabila duduk pasif terus-menerus, tetapi sangat antusias saat praktik tata cara ibadah atau simulasi peran.",
            options = listOf(
                PedagogicalOption(
                    id = "opt_k1",
                    title = "Instruksikan duduk diam di bangku dan menyalin catatan papan tulis sepanjang jam pelajaran",
                    rationale = "Kurang tepat. Siswa kinestetik memerlukan keterlibatan gerak dan praktik nyata untuk menginternalisasi konsep."
                ),
                PedagogicalOption(
                    id = "opt_k2",
                    title = "Libatkan dalam demonstrasi praktik ibadah, role-play akhlak, dan proyek lapangan",
                    rationale = "Tepat! Simulasi gerakan nyata dan proyek berbasis pengalaman mengonversi energi gerak menjadi penguasaan keterampilan PAI."
                ),
                PedagogicalOption(
                    id = "opt_k3",
                    title = "Berikan tes pilihan ganda berturut-turut tanpa aktivitas praktik",
                    rationale = "Kurang tepat. Asesmen kertas semata tidak memfasilitasi belajar berbasis pengalaman fisik bagi siswa kinestetik."
                )
            ),
            bestOptionId = "opt_k2"
        ),
        StudentProfileScenario(
            id = "diverse_academic",
            studentName = "Kelompok Kelas Kemampuan Akademik Beragam",
            learningStyle = "Kemampuan Akademik Beragam",
            academicProfile = "Dalam satu kelas terdapat siswa berkemampuan cepat (pengayaan), menengah, dan siswa yang masih membutuhkan bimbingan dasar membaca Al-Qur'an & konsep.",
            classroomBehavior = "Siswa cepat selesai merasa bosan menunggu, sedangkan siswa yang tertinggal merasa cemas dan enggan bertanya.",
            options = listOf(
                PedagogicalOption(
                    id = "opt_d1",
                    title = "Terapkan pembelajaran berdiferensiasi dengan tutor sebaya heterogen & tugas berjenjang",
                    rationale = "Tepat! Pengelompokan heterogen (Cooperative Learning) dan penugasan berjenjang memberi pendampingan bagi yang tertinggal sekaligus tantangan bermakna bagi siswa cepat."
                ),
                PedagogicalOption(
                    id = "opt_d2",
                    title = "Samakan kecepatan dan tingkat kesulitan tugas untuk semua siswa tanpa bimbingan tambahan",
                    rationale = "Kurang tepat. Pendekatan seragam memperlebar kesenjangan belajar dan membuat siswa lambat tertinggal semakin jauh."
                ),
                PedagogicalOption(
                    id = "opt_d3",
                    title = "Fokus hanya kepada siswa berprestasi tinggi agar target kurikulum cepat selesai",
                    rationale = "Kurang tepat. Mengabaikan peserta didik yang butuh bimbingan bertentangan dengan prinsip keadilan pedagogis PAI."
                )
            ),
            bestOptionId = "opt_d1"
        )
    )

    val localWisdomScenarios: List<LocalWisdomScenario> = listOf(
        LocalWisdomScenario(
            id = "pesantren_tradition",
            themeTitle = "Tradisi Pesantren",
            culturalContext = "Budaya musyawarah keilmuan (bahtsul masail), keteladanan kiai/ustadz, adab terhadap kitab, dan pembiasaan ibadah berjamaah.",
            paiTopic = "Etika Menuntut Ilmu & Adab Bermedia Digital",
            options = listOf(
                PedagogicalOption(
                    id = "lw_p1",
                    title = "Adaptasi metode sorogan & bahtsul masail mini untuk membedah etika verifikasi informasi (tabayyun) di media sosial",
                    rationale = "Sangat tepat! Nilai ketelitian sanad dan musyawarah pesantren relevan langsung dengan literasi digital Islami."
                ),
                PedagogicalOption(
                    id = "lw_p2",
                    title = "Menghilangkan seluruh konteks pesantren karena dianggap tidak relevan dengan teknologi modern",
                    rationale = "Kurang tepat. Tradisi intelektual pesantren justru kaya nilai etika ilmiah yang sangat dibutuhkan di era digital."
                )
            ),
            bestOptionId = "lw_p1"
        ),
        LocalWisdomScenario(
            id = "gotong_royong",
            themeTitle = "Budaya Gotong Royong",
            culturalContext = "Kebiasaan masyarakat Indonesia saling membantu dalam kerja bakti, musyawarah warga, dan solidaritas sosial antar tetangga.",
            paiTopic = "Ta'awun (Tolong-Menolong) & Kepedulian Sosial dalam Fikih Zakat/Infak",
            options = listOf(
                PedagogicalOption(
                    id = "lw_g1",
                    title = "Merancang proyek sosial kelas berbasis gotong royong untuk memetakan kebutuhan warga sekitar dan menyalurkan aksi kepedulian",
                    rationale = "Sangat tepat! Menghubungkan konsep ta'awun dengan aksi gotong royong nyata membentuk pengalaman belajar kontekstual dan berkarakter."
                ),
                PedagogicalOption(
                    id = "lw_g2",
                    title = "Meminta siswa mengerjakan esai individu di rumah tanpa keterlibatan komunitas sama sekali",
                    rationale = "Kurang tepat. Nilai ta'awun dan gotong royong lebih kuat dihayati melalui kolaborasi sosial nyata."
                )
            ),
            bestOptionId = "lw_g1"
        ),
        LocalWisdomScenario(
            id = "batik_context",
            themeTitle = "Batik sebagai Konteks Pembelajaran",
            culturalContext = "Motif batik Nusantara (seperti Kawung yang melambangkan kesucian hati dan keseimbangan, serta Parang tentang keteguhan berjuang).",
            paiTopic = "Akhlak Mulia, Kesabaran & Keindahan Seni dalam Islam",
            options = listOf(
                PedagogicalOption(
                    id = "lw_b1",
                    title = "Menjadikan filosofi motif batik sebagai media refleksi akhlak mulia, ketekunan, dan apresiasi seni Islami Nusantara",
                    rationale = "Sangat tepat! Kontekstualisasi filosofi batik memperkaya pembelajaran akhlak sekaligus menumbuhkan kecintaan pada budaya bangsa."
                ),
                PedagogicalOption(
                    id = "lw_b2",
                    title = "Melarang pembahasan motif budaya daerah di kelas PAI karena dianggap tidak berhubungan dengan akhlak",
                    rationale = "Kurang tepat. Islam Nusantara menghargai kearifan budaya yang selaras dengan tauhid dan nilai akhlak mulia."
                )
            ),
            bestOptionId = "lw_b1"
        ),
        LocalWisdomScenario(
            id = "local_tradition",
            themeTitle = "Tradisi Masyarakat Lokal",
            culturalContext = "Tradisi kenduri/syukuran, saling mengunjungi saat hari raya, serta unggah-ungguh atau tata krama bertutur kepada orang tua dan guru.",
            paiTopic = "Birrul Walidain, Silaturahmi & Moderasi Beragama",
            options = listOf(
                PedagogicalOption(
                    id = "lw_l1",
                    title = "Mengkaji tradisi silaturahmi dan tata krama lokal secara kritis-apresiatif untuk menanamkan moderasi beragama dan hormat kepada sesama",
                    rationale = "Sangat tepat! Pendekatan apresiatif dan kontekstual menumbuhkan sikap moderat, inklusif, dan beradab."
                ),
                PedagogicalOption(
                    id = "lw_l2",
                    title = "Menghakimi seluruh tradisi masyarakat lokal secara sepihak tanpa dialog akademik",
                    rationale = "Kurang tepat. Pendekatan dogmatis yang menolak dialog bertentangan dengan prinsip dakwah hikmah dan moderasi beragama."
                )
            ),
            bestOptionId = "lw_l1"
        )
    )

    val strategyCases: List<StrategyCaseScenario> = listOf(
        StrategyCaseScenario(
            id = "case_pbl",
            caseTitle = "Kasus 1: Fenomena Perundungan Digital & Hoaks di Kalangan Remaja",
            classroomProblem = "Siswa mengetahui larangan ghibah dan fitnah secara hafalan, tetapi belum mampu menganalisis dampak etis penyebaran berita bohong dan komentar negatif di media sosial.",
            options = listOf(
                StrategyOption(
                    id = "pbl",
                    strategyName = "Problem Based Learning (PBL)",
                    summary = "Pembelajaran diawali masalah otentik agar siswa meneliti akar masalah dan merumuskan solusi berbasis nilai PAI.",
                    feedbackIfChosen = "Pilihan paling tepat! PBL mendorong mahasiswa/siswa membedah masalah nyata perundungan digital, mencari landasan nilai tabayyun, dan menyusun solusi etis."
                ),
                StrategyOption(
                    id = "demonstrasi",
                    strategyName = "Demonstrasi",
                    summary = "Guru memeragakan langkah keterampilan prosedural secara langsung di depan kelas.",
                    feedbackIfChosen = "Kurang optimal untuk kasus analisis dilema moral digital; demonstrasi lebih cocok untuk keterampilan prosedural seperti tata cara wudhu atau shalat."
                ),
                StrategyOption(
                    id = "pjbl",
                    strategyName = "Project Based Learning (PjBL)",
                    summary = "Siswa merancang dan menghasilkan produk karya nyata dalam durasi tertentu.",
                    feedbackIfChosen = "Cukup relevan jika fokusnya membuat kampanye karya, namun untuk menganalisis akar masalah etika terlebih dahulu, Problem Based Learning lebih presisi."
                ),
                StrategyOption(
                    id = "cooperative",
                    strategyName = "Cooperative Learning",
                    summary = "Belajar dalam kelompok kecil heterogen dengan tanggung jawab bersama.",
                    feedbackIfChosen = "Mendukung kerja tim, namun tanpa struktur pemecahan masalah otentik seperti PBL, analisis kasus hoaks bisa tetap dangkal."
                ),
                StrategyOption(
                    id = "ctl",
                    strategyName = "Contextual Teaching and Learning (CTL)",
                    summary = "Mengaitkan materi ajar dengan situasi kehidupan nyata siswa.",
                    feedbackIfChosen = "Baik dalam membangun koneksi realitas, tetapi PBL lebih fokus pada investigasi solusi masalah kompleks secara terstruktur."
                ),
                StrategyOption(
                    id = "diskusi",
                    strategyName = "Diskusi Interaktif",
                    summary = "Tukar pendapat terbimbing antar siswa dan guru.",
                    feedbackIfChosen = "Berguna untuk memicu pendapat awal, namun PBL memberi kerangka investigasi masalah hingga solusi tertulis yang lebih mendalam."
                )
            ),
            bestStrategyId = "pbl"
        ),
        StrategyCaseScenario(
            id = "case_demo",
            caseTitle = "Kasus 2: Kesalahan Urutan & Gerakan Praktik Tayamum serta Pengurusan Jenazah",
            classroomProblem = "Banyak siswa keliru mempraktikkan urutan niat, usapan, dan posisi berdiri saat praktik ibadah karena selama ini hanya membaca teks prosedur di buku.",
            options = listOf(
                StrategyOption(
                    id = "demonstrasi",
                    strategyName = "Demonstrasi",
                    summary = "Peragaan langkah demi langkah oleh guru/model diikuti praktik terpandu siswa.",
                    feedbackIfChosen = "Pilihan paling tepat! Materi fikih ibadah yang bersifat psikomotorik dan prosedural sangat efektif diajarkan dengan Demonstrasi diikuti latihan terbimbing."
                ),
                StrategyOption(
                    id = "diskusi",
                    strategyName = "Diskusi Interaktif",
                    summary = "Forum tanya jawab dan tukar gagasan kelas.",
                    feedbackIfChosen = "Kurang tepat sebagai strategi utama karena keterampilan gerakan ibadah tidak cukup dikuasai hanya lewat diskusi verbal."
                ),
                StrategyOption(
                    id = "pbl",
                    strategyName = "Problem Based Learning (PBL)",
                    summary = "Investigasi masalah konseptual terbuka.",
                    feedbackIfChosen = "Kurang presisi. Siswa membutuhkan model peragaan konkret terlebih dahulu sebelum menganalisis variasi kasus fikih."
                ),
                StrategyOption(
                    id = "pjbl",
                    strategyName = "Project Based Learning (PjBL)",
                    summary = "Pembuatan produk proyek jangka menengah.",
                    feedbackIfChosen = "Bisa menjadi tindak lanjut pembuatan video tutorial, tetapi langkah awal penguasaan gerakan tetap memerlukan Demonstrasi."
                ),
                StrategyOption(
                    id = "cooperative",
                    strategyName = "Cooperative Learning",
                    summary = "Kerja kelompok heterogen terstruktur.",
                    feedbackIfChosen = "Dapat melengkapi latihan berpasangan, namun peragaan model oleh pendidik (Demonstrasi) tetap kunci utama."
                ),
                StrategyOption(
                    id = "ctl",
                    strategyName = "Contextual Teaching and Learning (CTL)",
                    summary = "Penghubungan materi dengan konteks keseharian.",
                    feedbackIfChosen = "Membantu pemahaman kondisi darurat tayamum, namun ketepatan gerakan tetap membutuhkan Demonstrasi langsung."
                )
            ),
            bestStrategyId = "demonstrasi"
        ),
        StrategyCaseScenario(
            id = "case_pjbl_coop",
            caseTitle = "Kasus 3: Kampanye Moderasi Beragama & Produk Kreatif Kearifan Lokal",
            classroomProblem = "Dosen ingin mahasiswa/siswa dengan kemampuan beragam berkolaborasi menghasilkan media kampanye toleransi berbasis budaya gotong royong dan batik lokal.",
            options = listOf(
                StrategyOption(
                    id = "pjbl",
                    strategyName = "Project Based Learning (PjBL)",
                    summary = "Pembelajaran berbasis proyek nyata yang menghasilkan produk kolaboratif bermakna.",
                    feedbackIfChosen = "Pilihan paling tepat! PjBL memberi ruang kolaborasi lintas gaya belajar untuk merancang, menguji, dan mempresentasikan produk kampanye nyata."
                ),
                StrategyOption(
                    id = "cooperative",
                    strategyName = "Cooperative Learning",
                    summary = "Kelompok kecil heterogen saling membantu menuntaskan target belajar bersama.",
                    feedbackIfChosen = "Sangat mendekati dan saling melengkapi dengan PjBL! Fokus utamanya pada interaksi tim, sementara PjBL menekankan lahirnya artefak proyek nyata."
                ),
                StrategyOption(
                    id = "ctl",
                    strategyName = "Contextual Teaching and Learning (CTL)",
                    summary = "Menghubungkan nilai materi dengan konteks budaya setempat.",
                    feedbackIfChosen = "Konteks budayanya kuat, namun untuk menghasilkan produk kampanye terukur, PjBL memberikan tahapan produksi proyek yang lebih jelas."
                ),
                StrategyOption(
                    id = "demonstrasi",
                    strategyName = "Demonstrasi",
                    summary = "Peragaan prosedur teknis di depan kelas.",
                    feedbackIfChosen = "Kurang tepat karena tujuan utamanya kreativitas produk dan kolaborasi, bukan meniru gerakan baku."
                ),
                StrategyOption(
                    id = "pbl",
                    strategyName = "Problem Based Learning (PBL)",
                    summary = "Pemecahan masalah konseptual.",
                    feedbackIfChosen = "Baik untuk analisis masalah awal, tetapi target menghasilkan media kampanye kreatif lebih tepat dijawab dengan PjBL."
                ),
                StrategyOption(
                    id = "diskusi",
                    strategyName = "Diskusi Interaktif",
                    summary = "Berdiskusi secara lisan di kelas.",
                    feedbackIfChosen = "Hanya mencakup tahap ideasi, belum menuntun siswa memproduksi karya kampanye hingga tahap evaluasi."
                )
            ),
            bestStrategyId = "pjbl"
        )
    )

    val evaluationQuestions: List<QuizQuestionItem> = listOf(
        QuizQuestionItem(
            id = "q1",
            topicTag = "Analisis Karakteristik Peserta Didik",
            question = "Seorang siswa mudah memahami rukun dan syarat sah jual beli ketika guru menyajikan bagan alur berwarna, namun tampak bingung saat hanya mendengarkan ceramah panjang. Penyesuaian strategi awal yang paling tepat adalah...",
            options = listOf(
                "Menyajikan peta konsep visual, infografis alur akad, dan penanda warna pada papan tulis interaktif",
                "Menambah durasi ceramah verbal tanpa catatan agar siswa terbiasa mendengar",
                "Memberikan tugas merangkum seluruh bab secara individu tanpa media visual",
                "Menghilangkan sesi diskusi agar kelas berjalan lebih tenang"
            ),
            correctIndex = 0,
            explanation = "Siswa tersebut menunjukkan kecenderungan gaya belajar visual. Peta konsep, infografis, dan papan tulis interaktif memfasilitasi pemrosesan spasial dan visual secara optimal."
        ),
        QuizQuestionItem(
            id = "q2",
            topicTag = "Pemilihan Strategi Pembelajaran",
            question = "Dalam pembelajaran materi praktik tata cara shalat jenazah yang menuntut ketepatan urutan bacaan dan gerakan psikomotorik, strategi pembelajaran paling tepat sebagai inti kegiatan adalah...",
            options = listOf(
                "Debat kompetitif antar kelompok",
                "Demonstrasi terpandu oleh pendidik diikuti praktik langsung siswa",
                "Penulisan makalah ilmiah sepanjang semester",
                "Membaca buku teks dalam hati secara individu"
            ),
            correctIndex = 1,
            explanation = "Strategi Demonstrasi sangat efektif untuk materi prosedural dan keterampilan ibadah karena memberikan model konkret sebelum siswa mempraktikkannya."
        ),
        QuizQuestionItem(
            id = "q3",
            topicTag = "Problem Based Learning (PBL)",
            question = "Manakah karakteristik utama yang membedakan Problem Based Learning (PBL) dengan ceramah konvensional dalam mata kuliah Strategi Pembelajaran PAI?",
            options = listOf(
                "PBL selalu berakhir tanpa kesimpulan materi dari pendidik",
                "PBL dimulai dari penyajian masalah otentik di dunia nyata sebagai pemicu penyelidikan dan refleksi nilai PAI",
                "PBL hanya dapat digunakan pada siswa berkemampuan akademik tinggi",
                "PBL melarang siswa bekerja dalam kelompok kecil"
            ),
            correctIndex = 1,
            explanation = "Ciri khas PBL adalah menjadikan masalah nyata yang bermakna sebagai titik berangkat proses berpikir kritis, investigasi sumber, dan perumusan solusi."
        ),
        QuizQuestionItem(
            id = "q4",
            topicTag = "Integrasi Kearifan Lokal",
            question = "Bagaimana cara paling bermakna mengintegrasikan motif batik Nusantara dan budaya gotong royong ke dalam pembelajaran Akhlak dan Muamalah PAI?",
            options = listOf(
                "Menjadikan motif batik sebagai hiasan dinding kelas tanpa dibahas dalam materi",
                "Mengaitkan filosofi motif batik dan praktik gotong royong sebagai konteks nyata nilai kesabaran, keseimbangan, dan ta'awun (CTL)",
                "Mengganti seluruh kajian akhlak dengan sejarah perdagangan kain",
                "Memisahkan sama sekali budaya lokal dari nilai agama agar tidak terjadi dialog"
            ),
            correctIndex = 1,
            explanation = "Pendekatan Contextual Teaching and Learning (CTL) mengaitkan materi PAI dengan budaya dan kehidupan nyata masyarakat agar siswa memahami nilai agama secara membumi."
        ),
        QuizQuestionItem(
            id = "q5",
            topicTag = "Cooperative Learning & Diferensiasi",
            question = "Saat menghadapi kelas PAI dengan tingkat kemampuan baca Al-Qur'an dan pemahaman konsep yang sangat beragam, langkah pedagogis terbaik dalam Cooperative Learning adalah...",
            options = listOf(
                "Mengelompokkan siswa secara homogen agar siswa lambat tidak mengganggu siswa cepat",
                "Membentuk kelompok heterogen dengan peran jelas dan pendampingan tutor sebaya yang saling menguatkan",
                "Memberikan nilai akhir yang sama tanpa asesmen proses",
                "Meminta siswa yang belum lancar hanya diam menyimak"
            ),
            correctIndex = 1,
            explanation = "Cooperative Learning menekankan pengelompokan heterogen, ketergantungan positif, dan akuntabilitas individu sehingga siswa berkemampuan beragam saling mendukung."
        ),
        QuizQuestionItem(
            id = "q6",
            topicTag = "Etika Pemanfaatan Generative AI dalam PAI",
            question = "Dalam memanfaatkan Generative AI pada perancangan pembelajaran PAI, prinsip etis dan validitas akademik terpenting yang wajib dipegang mahasiswa adalah...",
            options = listOf(
                "Langsung menyalin seluruh teks AI termasuk kutipan ayat tanpa pengecekan ulang",
                "Memverifikasi kesahihan dalil Al-Qur'an/hadis dari sumber primer otoritatif serta menyesuaikan rancangan dengan konteks peserta didik",
                "Menjadikan AI sebagai pengganti mutlak peran guru dan interaksi kemanusiaan di kelas",
                "Meminta AI mengarang referensi kitab fiktif agar rancangan tampak lengkap"
            ),
            correctIndex = 1,
            explanation = "Generative AI adalah alat bantu pedagogis; mahasiswa dan pendidik wajib memverifikasi keakuratan dalil pada mushaf dan kitab otoritatif serta menyesuaikannya dengan kebutuhan siswa."
        )
    )
}

# Audit UI: Perbandingan Desain Figma vs Implementasi Aplikasi GeCe Screen Printing

Dokumen ini memuat audit komparatif mendalam antara 24 desain visual Figma (`docs/figma/`) dengan kode implementasi Jetpack Compose yang ada di `ui/` (`app/src/main/java/com/gecesablon/app/ui/`).

### Kategori Penandaan Bagian Kurang / Belum Ada di Aplikasi:
- **(A)**: Dapat dibuat langsung dengan model dan data yang sudah ada di codebase.
- **(B)**: Membutuhkan penambahan model data, entitas DB, endpoint backend API, atau logika fitur baru (dijelaskan spesifikasinya).
- **(C)**: Merupakan ornamen mockup visual statis, dummy UI bawaan tool desain, atau tidak sesuai spesifikasi proyek (`docs/spesifikasi.md` & `DESIGN.md`).

---

## Ringkasan Pemetaan Layar (24 Desain Figma)

| No | File Figma | Nama Layar di Figma | File Implementasi di Kode | Catatan Status |
|---|---|---|---|---|
| 1 | `docs/figma/umum/00-splash-screen.png` | Splash Screen | `ui/splash/SplashScreen.kt` | Padanan Penuh |
| 2 | `docs/figma/umum/01-onboarding1-screen.png` | Onboarding 1 (Layanan Sablon) | `ui/onboarding/OnboardingScreen.kt` (Slide 1) | Padanan Penuh |
| 3 | `docs/figma/umum/02-onboarding2-screen.png` | Onboarding 2 (Pilihan Bahan) | `ui/onboarding/OnboardingScreen.kt` (Slide 2) | Padanan Penuh |
| 4 | `docs/figma/umum/03-onboarding3-screen.png` | Onboarding 3 (Status Pesanan) | `ui/onboarding/OnboardingScreen.kt` (Slide 3) | Padanan Penuh |
| 5 | `docs/figma/umum/04-welcome-screen.png` | Welcome Screen | `ui/welcome/WelcomeScreen.kt` | Padanan Penuh |
| 6 | `docs/figma/umum/05-login-screen.png` | Login Screen | `ui/auth/LoginScreen.kt` | Padanan Penuh |
| 7 | `docs/figma/umum/06-register-screen.png` | Register Screen | `ui/auth/RegisterScreen.kt` | Padanan Penuh |
| 8 | `docs/figma/pelanggan/10-beranda-tamu.png` | Beranda Tamu / Pelanggan | `ui/customer/HomeScreen.kt` | Padanan Penuh |
| 9 | `docs/figma/pelanggan/11-katalog-produk.png` | Katalog Produk | `ui/customer/CatalogScreen.kt` | Padanan Penuh |
| 10 | `docs/figma/pelanggan/12-detail-produk.png` | Detail Produk | `ui/customer/ProductDetailScreen.kt` | Padanan Penuh |
| 11 | `docs/figma/pelanggan/13-formulir-pesanan.png` | Formulir Pemesanan | `ui/customer/order/OrderFormScreen.kt` | Padanan Penuh |
| 12 | `docs/figma/pelanggan/14-konfirmasi-pesanan.png` | Review & Konfirmasi Pesanan | `ui/customer/order/OrderConfirmScreen.kt` | Padanan Penuh |
| 13 | `docs/figma/pelanggan/15-detail-pesanan.png` | Detail Pesanan Pelanggan | `ui/customer/order/OrderDetailScreen.kt` | Padanan Penuh |
| 14 | `docs/figma/pelanggan/16-pesanan-berhasil.png` | Pesanan Berhasil Terkirim | `ui/customer/order/OrderSuccessScreen.kt` | Padanan Penuh |
| 15 | `docs/figma/admin/20-dashboard-admin.png` | Dashboard Admin Workshop | `ui/admin/AdminDashboardScreen.kt` | Padanan Penuh |
| 16 | `docs/figma/admin/21-detail-pesanan-admin.png` | Detail Pesanan Admin | `ui/staff/StaffOrderDetailScreen.kt` | Padanan Penuh |
| 17 | `docs/figma/admin/22-manajemen-pesanan.png` | Manajemen Pesanan | `ui/staff/StaffOrdersScreen.kt` | Padanan Penuh |
| 18 | `docs/figma/admin/23-tambah-pesanan.png` | Tambah Pesanan Manual | `ui/admin/order/AdminManualOrderScreen.kt` | Padanan Penuh |
| 19 | `docs/figma/admin/24-proses-pesanan.png` | Proses Pesanan Workshop | `ui/staff/StaffProcessScreen.kt` | Padanan Penuh |
| 20 | `docs/figma/admin/25-akun-profil.png` | Akun Profil Admin | `ui/admin/AdminAccountScreen.kt` | Padanan Penuh |
| 21 | `docs/figma/owner/30-dashboard.png` | Dashboard Owner | `ui/owner/OwnerDashboardScreen.kt` | Padanan Penuh |
| 22 | `docs/figma/owner/31-laporan-keuangan.png` | Laporan Keuangan Owner | `ui/owner/finance/OwnerReportScreen.kt` | Padanan Penuh |
| 23 | `docs/figma/owner/32-detail-keuangan.png` | Detail Keuangan Owner | `ui/owner/OwnerFinanceScreen.kt` & `ui/owner/finance/OwnerFinanceDetailScreen.kt` | Padanan Penuh |
| 24 | `docs/figma/owner/33-akun-profil.png` | Akun Profil Owner | `ui/owner/OwnerAccountScreen.kt` | Padanan Penuh |

---

## 1. Layar Umum & Autentikasi

### 1.1. Splash Screen
- **Figma**: `docs/figma/umum/00-splash-screen.png`
- **Kode**: [SplashScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/splash/SplashScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| *(Tidak ada)* | Teks footer versi dinamis: `BuildConfig.VERSION_NAME` diambil langsung dari konfigurasi Gradle build. | - Di Figma latar belakang tampak bergradasi halus (soft radial glow), sedangkan di aplikasi menggunakan `Surface(color = GeceColors.Background)` solid (#FAFAF7).<br>- Ukuran kontainer logo: di Figma 108dp dengan radius halus 20dp, di aplikasi sudah presisi 108dp.<br>- Garis aksen loading kuning di aplikasi menggunakan lebar tetap 190dp x 3dp, sangat presisi mengikuti Figma. |

---

### 1.2. Onboarding 1, 2, 3
- **Figma**: 
  - `docs/figma/umum/01-onboarding1-screen.png` (Slide 1: Layanan Sablon)
  - `docs/figma/umum/02-onboarding2-screen.png` (Slide 2: Pilihan Bahan)
  - `docs/figma/umum/03-onboarding3-screen.png` (Slide 3: Status Pesanan)
- **Kode**: [OnboardingScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/onboarding/OnboardingScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Strip ornamen palet warna "Standar Sablon GeCe" dengan 3 bulatan warna (Kuning, Hitam, Abu-abu) di atas tombol aksi utama: di aplikasi digantikan oleh info kartu fitur yang lebih informatif. | - Tautan sekunder teks bergaris bawah `Sudah punya akun? Masuk` di bawah tombol utama Lanjut/Mulai. | - Indikator halaman: di Figma menggunakan bar bulat panjang aktif dan dot abu-abu disertai teks angka numerik `01 / 03`. Di aplikasi menggunakan HorizontalPager dots yang responsif.<br>- Ikon kartu: Figma memakai simbol geometris lingkaran kuning beraksen emas (`AllInclusive`, `Palette`, `Notifications`), aplikasi memakai `ImageVector` Material 3 yang setara. |

---

### 1.3. Welcome Screen
- **Figma**: `docs/figma/umum/04-welcome-screen.png`
- **Kode**: [WelcomeScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/welcome/WelcomeScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Gambar mockup produk kaos/polo realistis di dalam hero card besar (di Figma terdapat kontainer besar kosong untuk ilustrasi apparel). | - Tombol/tautan dialog "Tentang Aplikasi" di bagian bawah untuk melihat identitas build dan developer (Minggu 12). | - Hero Card: Figma menampilkan card rounded 20dp dengan 2 floating pill badge: `Studio Spec` (hitam) di kanan atas dan `HIGH PRECISION • SABLON DTF` di kiri bawah. Kode Compose mengimplementasikan kedua pill ini secara presisi di dalam `Box`.<br>- Chips fitur: 3 chip horizontal `Kaos & Polo Premium`, `DTF Detail & Tajam`, `Tanpa Minimum Order` disusun rapi menggunakan `FlowRow`. |

---

### 1.4. Login Screen
- **Figma**: `docs/figma/umum/05-login-screen.png`
- **Kode**: [LoginScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/auth/LoginScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(B)** Alur Lupa Password aktif: Di Figma terdapat teks "Lupa Password?". Di aplikasi masih berupa tautan dengan pesan "Segera hadir" karena pemulihan kata sandi belum memiliki API email/SMS OTP backend. | - Dukungan redirect otomatis (`redirect=order` atau `redirect=product`) beserta `productId` agar tamu yang login langsung diarahkan kembali ke layar sebelumnya.<br>- Indikator loading (`CircularProgressIndicator`) di dalam tombol Masuk saat request login berlangsung. | - Tombol "Jelajahi Katalog sebagai Tamu": Di Figma diletakkan di atas form field; di aplikasi disembunyikan otomatis jika login dipicu oleh alur pengalihan (*redirect*), dan hanya tampil pada login normal.<br>- Tautan "Lupa Password?": Di Figma berupa teks polos berwarna oranye/emas dengan dot oranye, di aplikasi berupa pill aksen kuning `#F5E85B` dengan warna teks `#171717`. |

---

### 1.5. Register Screen
- **Figma**: `docs/figma/umum/06-register-screen.png`
- **Kode**: [RegisterScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/auth/RegisterScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Ikon profil lingkaran hitam di pojok kanan atas AppTopBar pada Figma: di aplikasi sengaja ditiadakan karena pengguna belum memiliki sesi/login. | - Dialog Syarat & Ketentuan serta Kebijakan Privasi yang interaktif saat teks tautan diklik.<br>- Validasi komprehensif per field (nama minimal 3 huruf, no HP 10-15 digit, format email regex, password minimal 8 karakter, konfirmasi password cocok). | - Tombol checkbox persetujuan: di Figma berbentuk kotak tipis tanpa border tebal; di aplikasi menggunakan `Checkbox` Material 3 dengan warna aktif `#111111` sesuai `DESIGN.md`.<br>- Tombol Daftar: Tombol primary hitam 52dp dengan icon panah `ArrowForward` di sisi kanan. |

---

## 2. Layar Pelanggan & Pemesanan

### 2.1. Beranda Tamu & Pelanggan
- **Figma**: `docs/figma/pelanggan/10-beranda-tamu.png`
- **Kode**: [HomeScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/customer/HomeScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Gambar mockup produk statis dengan badge tag melayang seperti `Cetak DTF Presisi`, `DTF High Density`, `DTF Full Color`, `DTF Spot Color` di portofolio Figma (di aplikasi portofolio menampilkan data produk riil dari API/Repository). | - Integrasi langsung intent WhatsApp resmi ke CS Workshop dengan template pesan konsultasi otomatis.<br>- Status dinamis TopAppBar (ikon WhatsApp dan ikon Avatar Profil yang membuka login jika tamu, atau profil jika pelanggan). | - Banner Utama: Figma menggunakan background hitam dengan thumbnail pakaian di sisi kanan; aplikasi menggunakan card hitam `#111111` dengan badge kuning `#F5E85B` dan tombol aksi cepat "Lihat Katalog".<br>- Grid Portofolio: Di Figma berupa 4 kartu 2x2 statis; di aplikasi menggunakan chunk 2-kolom dinamis `ProductCard` dengan harga terformat Rupiah otomatis. |

---

### 2.2. Katalog Produk
- **Figma**: `docs/figma/pelanggan/11-katalog-produk.png`
- **Kode**: [CatalogScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/customer/CatalogScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Tombol Filter Ikon (Sliders/Filter icon) di samping kolom input pencarian: di aplikasi filter sudah diakomodasi langsung oleh chip kategori horizontal. | - Fitur hapus pencarian instan (ikon `Close` di dalam kolom search).<br>- State handling visual lengkap: `LoadingView`, `EmptyView` (saat produk dicari tidak ditemukan), dan `ErrorView` dengan tombol coba lagi. | - Chip Kategori: Di Figma ada 3 chip (`Semua`, `Kaos`, `Baju Polo`), di aplikasi ada 4 chip terstruktur sesuai `ProductCategory` enum (`Semua`, `Kaos Custom`, `Kaos Oversize`, `Baju Polo`).<br>- Banner Bawah "Punya Spek Custom Sendiri?": Di aplikasi diimplementasikan lengkap dengan tombol aksi WhatsApp CS dan rincian jaminan kualitas. |

---

### 2.3. Detail Produk
- **Figma**: `docs/figma/pelanggan/12-detail-produk.png`
- **Kode**: [ProductDetailScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/customer/ProductDetailScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(B)** Stepper Jumlah Pesanan (+ / - 24 pcs) di layar detail produk: di Figma terdapat kontrol jumlah langsung di layar ini, sedangkan di aplikasi pemilihan jumlah ditempatkan di formulir pemesanan (`OrderFormScreen`) agar tidak redundan.<br>- **(C)** Pilihan bahan bertingkat dengan radio button (`Cotton Combed 24s`, `30s`, `Heavyweight 20s +Rp5.000`): di aplikasi bahan melekat pada masing-masing entitas produk katalog.<br>- **(C)** Indikator titik carousel foto (dot 1/3) dan tombol "Mockup": saat ini aplikasi memuat gambar utama produk via Coil. | - Bottom sticky bar: Menampilkan teks "Mulai Dari Rp...", harga dasar dinamis, dan tombol CTA "Pesan Sekarang".<br>- Proteksi peran tamu: jika tamu mengklik "Pesan Sekarang", diarahkan ke Login dengan membawa redirect dan productId. | - Swatch Warna: Figma menampilkan bulatan warna dengan counter "+24 Opsi Warna"; aplikasi menampilkan deretan horizontal swatch warna bergaris tepi (border) dengan indikator centang pada warna aktif.<br>- Ukuran: Pill ukuran di aplikasi menampilkan tambahan harga (+Rp5.000) secara otomatis jika ukuran memiliki surcharge. |

---

### 2.4. Formulir Pemesanan
- **Figma**: `docs/figma/pelanggan/13-formulir-pesanan.png`
- **Kode**: [OrderFormScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/customer/order/OrderFormScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Pilihan Titik & Ukuran Sablon Manual: Di Figma ada pilihan `Posisi 1: Dada Kiri (A6)`, `Posisi 2: Punggung (A3)`. Berdasarkan `docs/spesifikasi.md` poin 93, posisi dan ukuran sablon ditulis pelanggan di kolom catatan tanpa pilihan area cetak khusus.<br>- **(C)** Badge "Pesan Tanpa Login" & Form input Nama Lengkap + No HP di formulir: Sesuai spesifikasi proyek, pelanggan wajib login untuk memesan (mode tamu diarahkan login terlebih dahulu).<br>- **(B)** Upload berkas format CDR/PDF: Di aplikasi dibatasi berkas gambar (JPG, PNG, WEBP) maksimal 10MB menggunakan Android PhotoPicker bawaan OS. | - Validasi ketat sisi klien: Pilihan warna wajib, ukuran wajib, jumlah (1-999), dan minimal salah satu dari gambar desain atau catatan terisi.<br>- Pratinjau gambar desain langsung di dalam formulir dengan tombol hapus/ganti file.<br>- Perhitungan otomatis harga satuan dan subtotal secara murni melalui `PriceCalculator.kt`. | - Desain kartu upload: Di aplikasi menggunakan `Card` berbingkai outline halus 1dp dengan ikon `CloudUpload` dan tombol pilih gambar yang intuitif.<br>- Rincian Estimasi Biaya: Menampilkan subtotal, badge gratis biaya afdruk/mockup untuk order di atas 24 pcs, dan total estimasi transparan. |

---

### 2.5. Review & Konfirmasi Pesanan
- **Figma**: `docs/figma/pelanggan/14-konfirmasi-pesanan.png`
- **Kode**: [OrderConfirmScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/customer/order/OrderConfirmScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Rincian Nama Lengkap & Nomor WhatsApp pemesan statis tamu: Sesuai arsitektur, data pengguna diambil dari sesi login akun yang aktif.<br>- **(C)** Teks "Metode Cetak Presisi DTF (Otomatis)" dalam kartu terpisah: Di aplikasi dirangkum dalam ringkasan produk. | - Tombol sekunder "Ubah" di bottom bar yang mengembalikan pengguna ke form pemesanan dengan state yang tetap terjaga.<br>- Indikator loading spinner pada tombol konfirmasi saat mutasi data pengiriman order berlangsung ke backend. | - Banner validasi akhir produksi berwarna aksen kuning `#F5E85B` dengan ikon check circle.<br>- Thumbnail desain yang diunggah ditampilkan langsung dalam ukuran thumbnail 54dp dengan teks status kesiapan berkas. |

---

### 2.6. Detail Pesanan Pelanggan
- **Figma**: `docs/figma/pelanggan/15-detail-pesanan.png`
- **Kode**: [OrderDetailScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/customer/order/OrderDetailScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Rincian "Area Cetak Sablon (Dada Kiri A6, Punggung Belakang A3)": Di aplikasi instruksi cetak berada di bagian catatan pelanggan sesuai spesifikasi proyek.<br>- **(B)** Kotak "Pembaruan dari Admin" (catatan broadcast khusus dari workshop): Saat ini aplikasi menampilkan linimasa status riil dan histori aktivitas tahapan produksi. | - Tombol "Batalkan Pesanan" dengan dialog konfirmasi: Hanya aktif dan muncul jika status pesanan masih `DITERIMA` (sesuai `docs/spesifikasi.md` poin 99).<br>- Tombol cepat "Hubungi Workshop via WhatsApp" dengan auto-fill template kode pesanan yang bersangkutan. | - Linimasa Status: Di Figma hanya berupa kotak teks; di aplikasi dibuatkan komponen `OrderTimelineView` 6 tahapan (`Diterima` -> `Sedang Diproses` -> `Cetak DTF` -> `Pressing` -> `Siap Diambil` -> `Selesai`) dengan lingkaran kuning/hitam sesuai status terkini. |

---

### 2.7. Pesanan Berhasil
- **Figma**: `docs/figma/pelanggan/16-pesanan-berhasil.png`
- **Kode**: [OrderSuccessScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/customer/order/OrderSuccessScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Teks ornamen dummy "Tim GeCe akan segera menghubungi Anda via WhatsApp untuk konfirmasi mock-up & approval afdruk (Pesan Tanpa Login)". | - Tombol "Salin" kode pesanan dengan feedback `Toast` sistem Android.<br>- Tombol "Kembali ke Beranda" yang mengosongkan tumpukan navigasi formulir pemesanan. | - Ikon Centang Sukses: Di aplikasi menggunakan lingkaran berlapis aksen kuning `#F5E85B` 80dp dengan icon check hitam tebal.<br>- Penataan tombol: Tombol utama "Lihat Detail Pesanan" (kuning `#F5E85B`) dan tombol sekunder "Kembali ke Beranda" (outline card) di bagian sticky bottom bar. |

---

## 3. Layar Admin Workshop

### 3.1. Dashboard Admin Workshop
- **Figma**: `docs/figma/admin/20-dashboard-admin.png`
- **Kode**: [AdminDashboardScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/admin/AdminDashboardScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Label shift kerja "WORKSHOP AKTIF • SHIFT PAGI" dan tanggal statis kalender di header selamat datang.<br>- **(C)** Kartu info instruksi mesin heat press "Pemeriksaan suhu heat press wajib dijaga 160°C - 165°C". | - Kartu horizontal daftar stok workshop menipis (`LowStockCard`) yang dapat diklik langsung untuk menuju manajemen stok.<br>- Status chip dinamis pada kartu pesanan terbaru. | - Kartu Ringkasan Status: Di Figma berupa 4 kotak (Pesanan Masuk, Menunggu Cek, Sedang Diproses, Siap Diambil). Di aplikasi menggunakan 4 kartu statistik responsif (`Baru`, `Diproses`, `Siap Diambil`, `Stok Menipis`).<br>- Aksi Cepat: 3 tombol akses operasional (`Buat Order`, `Kelola Pesanan`, `Kelola Stok`). |

---

### 3.2. Manajemen Pesanan (Admin / Staf)
- **Figma**: `docs/figma/admin/22-manajemen-pesanan.png`
- **Kode**: [StaffOrdersScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/staff/StaffOrdersScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Tombol floating "+ Pesanan Baru" hitam di sebelah kolom pencarian: Di aplikasi fungsi ini diakses dari Dashboard atau Bottom Navigation. | - Dropdown Filter Periode Waktu (`StaffDateFilterRow`) dengan opsi Semua Tanggal, Hari Ini, Minggu Ini, Bulan Ini.<br>- Teks counter dinamis jumlah antrean aktif yang terfilter. | - Kartu antrean: Figma menggunakan card putih dengan tag status kuning di pojok kanan atas; di aplikasi menggunakan `OrderCard` standar dengan `StatusChip` resmi, rincian produk, dan total biaya. |

---

### 3.3. Tambah Pesanan Manual (Admin Workshop)
- **Figma**: `docs/figma/admin/23-tambah-pesanan.png`
- **Kode**: [AdminManualOrderScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/admin/order/AdminManualOrderScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Pemilihan metode penyerahan radio button ("Ambil di Tempat" vs "Ekspedisi/Kirim").<br>- **(C)** Matriks multi-ukuran per batch (S: 4, M: 8, L: 8, XL: 4, XXL: 0): Berdasarkan `docs/spesifikasi.md` poin 91, satu pesanan manual di UI mencakup 1 ukuran per pesanan.<br>- **(C)** Pemilihan multi-posisi penempatan film DTF (`Posisi 1 Dada Kiri`, `Posisi 2 Punggung`). | - Dropdown pemilihan produk katalog dinamis dengan harga terintegrasi.<br>- Stepper counter kuantitas jumlah pcs (1 - 999) dengan proteksi batas minimum/maksimum.<br>- Perhitungan otomatis harga satuan dan total harga menggunakan fungsi murni `PriceCalculator.kt`. | - Tampilan formulir dibagi ke dalam 3 kartu modular bersih (`Data Pelanggan`, `Spesifikasi Produk & Sablon`, `Instruksi Cetak & Desain`).<br>- Tombol Simpan Pesanan: Menggunakan tombol kuning aksen penuh dengan loading progress saat request submit. |

---

### 3.4. Proses Pesanan Workshop
- **Figma**: `docs/figma/admin/24-proses-pesanan.png`
- **Kode**: [StaffProcessScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/staff/StaffProcessScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Form input nama vendor ekspedisi & nomor resi pengadaan kain (contoh: JNE-882910 Distributor Bandung): Pada aplikasi cukup menggunakan switch aksi `Baju Tiba`.<br>- **(C)** Penugasan nama operator meja sablon (contoh: Meja Press 02 Reza).<br>- **(C)** Kotak log audit teks internal admin/owner di bagian bawah layar proses. | - Filter chip horizontal untuk menyaring antrean berdasarkan 5 tahapan produksi spesifik.<br>- Bilah kemajuan linear (`LinearProgressIndicator`) per kartu pesanan yang menghitung fraksi 0/5 hingga 5/5 otomatis. | - Di Figma layar proses menampilkan 1 pesanan dengan seluruh checklist terbuka panjang; di aplikasi layar ini berfungsi sebagai daftar antrean proses seluruh pesanan aktif, sedangkan checklist interaktif dan detail material berada di layar `StaffOrderDetailScreen.kt`. |

---

### 3.5. Detail Pesanan Admin & Proses Produksi
- **Figma**: `docs/figma/admin/21-detail-pesanan-admin.png`
- **Kode**: [StaffOrderDetailScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/staff/StaffOrderDetailScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Pratinjau RIP software skematik tata letak film sablon transparan.<br>- **(C)** Keputusan review desain radio button (Menunggu, Perlu Revisi, Disetujui).<br>- **(C)** Input teks "Catatan Teknis Artwork (Untuk Pelanggan)". | - Validasi aturan produksi ketat (`ProductionRules.kt`): tahapan harus dicentang berurutan, tidak boleh lompat, dan tidak boleh dibatalkan jika tahap berikutnya sudah selesai.<br>- Penguncian ketergantungan material: tahapan 4 (Penyablonan) terkunci sampai Baju Tiba, DTF Tiba, dan Siap Disablon aktif.<br>- Dialog peringatan perubahan belum disimpan (`hasUnsavedChanges`) saat menekan tombol kembali. | - Tombol aksi bawah: Floating bar yang menampilkan "Simpan Perubahan Tahapan" dan tombol "Tandai Pesanan Selesai" jika status sudah siap diambil.<br>- Panel status material menggunakan switch toggle modern yang responsif. |

---

### 3.6. Akun Profil Admin
- **Figma**: `docs/figma/admin/25-akun-profil.png`
- **Kode**: [AdminAccountScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/admin/AdminAccountScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(B)** Menu Ubah Kata Sandi, Keamanan Akun & 2FA WhatsApp OTP.<br>- **(C)** Switch pengaturan preferensi visual (Mode Tampilan Terang/Gelap/Sistem, Bahasa Aplikasi).<br>- **(C)** Baris bantuan Pusat Bantuan & Panduan SOP Sablon. | - Tautan menu "Tentang Aplikasi" yang memuat informasi versi aplikasi dan pengembang.<br>- Dialog konfirmasi logout native untuk mencegah ketidaksengajaan keluar akun. | - Di Figma terdapat tab switcher di atas "Pratinjau Role (Owner / Admin Workshop)" yang merupakan kontrol preview desain Figma; di aplikasi tidak diperlukan karena role diambil dari sesi token login backend pengguna. |

---

## 4. Layar Owner

### 4.1. Dashboard Owner
- **Figma**: `docs/figma/owner/30-dashboard.png`
- **Kode**: [OwnerDashboardScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/owner/OwnerDashboardScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(B)** Grafik batang tren pendapatan vs pengeluaran 6 bulan terakhir: Saat ini ringkasan keuangan di dashboard menyajikan agregasi bulan berjalan.<br>- **(B)** Kartu pengawasan piutang pelanggan (belum lunas): Di aplikasi status pesanan berbasis lunas saat dibuat/selesai (belum ada modul piutang berjangka). | - Kartu "Perlu Perhatian" yang memfilter pesanan yang membutuhkan approval atau pengawasan khusus.<br>- Dialog feedback "Segera hadir" untuk menu aksi cepat yang dialokasikan ke tahap lanjutan (Katalog Produk & Tim Sablon). | - Kartu Ringkasan Keuangan: Menggunakan kartu kontras hitam pekat `#111111` dengan teks pemasukan hijau, pengeluaran merah, dan laba bersih aksen kuning/hijau sesuai `DESIGN.md`.<br>- Aksi Cepat: 4 kartu pintasan (`Buat Pesanan`, `Catat Pengeluaran`, `Katalog Produk`, `Tim Sablon`). |

---

### 4.2. Laporan Keuangan Owner
- **Figma**: `docs/figma/owner/31-laporan-keuangan.png`
- **Kode**: [OwnerReportScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/owner/finance/OwnerReportScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(B)** Fitur ekspor berkas dokumen fisik (PDF dan Excel .xlsx) dengan opsi watermark rahasia: Sesuai roadmap, ekspor file fisik dijadwalkan pada Tahap 6C.<br>- **(C)** Grafik visual kinerja finansial 6 bulan.<br>- **(C)** Kartu simulasi Unit Economics per order (studi kasus margin order tertentu). | - Rincian distribusi pengeluaran per kategori (`TransactionCategory`) yang dihitung dinamis dengan persentase proporsi terhadap total pengeluaran.<br>- Kartu metrik total pesanan selesai pada periode yang dipilih. | - Filter periode laporan: Menggunakan chip filter yang terintegrasi langsung dengan `PeriodRules.kt` (`Bulan Ini`, `Bulan Lalu`, `7 Hari Terakhir`, `30 Hari Terakhir`).<br>- Penataan tabel laporan tersusun hierarkis dan mudah dibaca pada layar perangkat mobile. |

---

### 4.3. Detail Keuangan & Transaksi Owner
- **Figma**: `docs/figma/owner/32-detail-keuangan.png`
- **Kode**: 
  - [OwnerFinanceScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/owner/OwnerFinanceScreen.kt)
  - [OwnerFinanceDetailScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/owner/finance/OwnerFinanceDetailScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(C)** Indikator teks "Sinkronisasi Realtime Terhubung: Database Produksi & Kasir Cabang" dan "Studi Kasus Order Margin 40%". | - DateRangePicker interaktif berbasis `ModalBottomSheet` Material 3 untuk memilih rentang tanggal kustom.<br>- Form pencarian transaksi berdasarkan teks keterangan atau penerima/pembayar.<br>- Tombol cepat aksi pencatatan: "Catat Pemasukan" (hijau) dan "Catat Pengeluaran" (merah). | - Daftar transaksi: Di aplikasi menggunakan `TransactionListItem` dengan ikon panah masuk/keluar, label kategori, tanggal terformat, dan nilai nominal berwarna hijau (pemasukan) atau merah (pengeluaran). |

---

### 4.4. Akun Profil Owner
- **Figma**: `docs/figma/owner/33-akun-profil.png`
- **Kode**: [OwnerAccountScreen.kt](file:///D:/KULIAH/PROJECTS/GeCeSablon/app/src/main/java/com/gecesablon/app/ui/owner/OwnerAccountScreen.kt)

| Bagian di Figma tapi Belum Ada di Aplikasi | Bagian di Aplikasi tapi Tidak Ada di Figma | Perbedaan Gaya (Warna, Urutan, Ukuran, Ikon) |
|---|---|---|
| - **(B)** Modul Kelola Akun Admin (pembuatan dan manajemen staf admin oleh Owner): Rutenya sudah tersedia di `OwnerAdminAccountsScreen.kt`, namun belum dihubungkan di menu profil utama.<br>- **(B)** Ubah kata sandi dan pengaturan keamanan 2FA.<br>- **(C)** Switch opsi tampilan dan bahasa. | - Tautan informasi "Tentang Aplikasi" yang menampilkan informasi build.<br>- Dialog konfirmasi logout aman. | - Avatar Owner: Di aplikasi menggunakan warna aksen kuning GeCe `#F5E85B` dengan ikon profil gelap `#111111` yang membedakannya secara visual dari avatar Admin. |

---

## 5. Ringkasan Temuan & Rekomendasi Tindak Lanjut

1. **Bagian Berkategori (A) - Dapat Dibuat dengan Data/Model yang Ada**:
   - Menghubungkan menu "Kelola Akun Admin" (`OwnerAdminAccountsScreen.kt`) dan "Kelola Produk" (`OwnerProductsScreen.kt`) ke dalam daftar menu di `OwnerAccountScreen.kt`.
   - Menambahkan ornamen visual label/badge kecil (seperti `Studio Spec` dan `Bespoke Studio`) pada beberapa komponen card jika diperlukan untuk menyamakan estetika 100% dengan Figma.

2. **Bagian Berkategori (B) - Membutuhkan Penambahan Data / Fitur Baru**:
   - **Fitur Ekspor PDF & XLSX**: Memerlukan library pembuatan dokumen (misal Apache POI atau iText/PdfDocument) untuk merealisasikan tombol ekspor di `OwnerReportScreen.kt`.
   - **Visualisasi Chart / Grafik 6 Bulan**: Membutuhkan agregasi database transaksi bulanan dan library charting (atau kustom canvas Compose).
   - **Modul Ubah Kata Sandi**: Memerlukan endpoint backend autentikasi untuk verifikasi password lama dan update hash password baru.

3. **Bagian Berkategori (C) - Ornamen Mockup Visual / Di Luar Spesifikasi**:
   - Pilihan area cetak kaku (A3, A4, A6) di formulir pemesanan tidak perlu dibuat karena `docs/spesifikasi.md` menetapkan posisi cetak cukup ditulis melalui catatan pelanggan (agar fleksibel dan tidak membatasi ukuran cetak DTF meteran).
   - Fitur "Pesan Tanpa Login" diabaikan karena sesuai alur resmi sistem, pelanggan wajib memiliki akun terdaftar untuk melacak status pesanan dan riwayat.
   - Ornamen dummy preview perangkat (seperti tab bar role switcher Figma dan baris SOP workshop) adalah elemen presentasi desain.

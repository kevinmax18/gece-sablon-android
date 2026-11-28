# Spesifikasi Aplikasi GeCe Screen Printing

## Ringkasan
Aplikasi mobile untuk UMKM GeCe Screen Printing (sablon DTF). Satu aplikasi, tiga peran:
pelanggan, admin workshop, owner. Peran ditentukan oleh server saat login, lalu aplikasi
menampilkan navigasi sesuai peran. Acuan: docs/flowchart/ dan docs/figma/.
Flowchart hanya menggambarkan alur UI/UX, belum mencakup backend dan database.
Backend: Laravel + MySQL (repo gece-sablon-api). Aplikasi terhubung lewat REST API (Retrofit), diuji dengan Postman.

## Peran dan navigasi
| Peran | Bottom navbar | Fokus |
|-------|---------------|-------|
| Pelanggan (dan tamu) | Beranda, Katalog, Pesan, Kontak | Melihat produk, membuat pesanan, melacak status, riwayat, menghubungi workshop |
| Admin | Beranda, Pesanan, Proses, Akun | Operasional pesanan, produksi, QC, stok |
| Owner | Beranda, Pesanan, Proses, Keuangan, Akun | Pengawasan operasional, laporan, transaksi, pengaturan |

Owner memiliki semua akses admin, ditambah keuangan, laporan, pengelolaan produk/harga/variasi, dan pengelolaan akun admin.

## Alur umum
Splash -> Onboarding -> Welcome (Masuk | Daftar | Jelajahi sebagai Tamu).
Satu layar Login untuk semua peran. Peran diambil dari respons server, lalu aplikasi membuka
navigasi dan dashboard sesuai peran. Register hanya untuk pelanggan (peran selalu "customer").
Akun admin dibuat oleh Owner lewat menu Kelola Akun Admin. Akun owner dibuat langsung di database.

## Fitur Pelanggan
- Mode tamu: boleh melihat Beranda, Katalog, dan Detail Produk. Saat menekan Pesan atau Kirim,
  tamu diarahkan ke Login/Register, lalu kembali ke layar sebelumnya.
- Register (nama, no HP/WhatsApp, email, password) dan Login.
- Pembuatan pesanan: Pilih Produk -> Pilih Bahan -> Pilih Warna -> Pilih Ukuran -> Atur Jumlah (harga otomatis)
- Status & Detail Pesanan, Riwayat Pesanan.
- Hubungi workshop lewat WhatsApp.
- Profil dan Logout.

## Fitur Admin
- Dashboard: tindak lanjut dan pesanan terbaru.
- Manajemen Pesanan: daftar dengan filter status, Detail Pesanan.
- Proses Workshop: update tahapan produksi per pesanan, Simpan Tahapan, status pesanan ikut diperbarui.
- Aksi produksi: Konfirmasi baju tiba, Update DTF tiba, Tandai siap disablon, Simpan perubahan tahapan.
- Buat Pesanan Manual (untuk pelanggan yang datang langsung).
- Kelola Stok.
- Kelola info kontak workshop dan slide gambar halaman Welcome (Tahap 7).
- Akun: profil, pengaturan, logout.

## Fitur Owner
- Dashboard Owner.
- Manajemen Pesanan dan Proses Workshop (pengawasan, sama seperti admin, tetapi dengan header milik owner).
- Keuangan: Catat Pemasukan/Pengeluaran, Ringkasan Laba/Rugi, Detail Transaksi, Filter Periode.
- Laporan/Ekspor: unduh PDF dan CSV (dibuka di Excel). XLSX asli ditunda sampai library disetujui
- Kelola Produk, Harga & Variasi (warna, ukuran), termasuk gambar produk.
- Kelola Akun Admin (termasuk menghapus akun admin).
- Kelola info kontak workshop dan slide gambar halaman Welcome (sama seperti admin).
- Akun: profil, pengaturan, logout.
- Kelola Kategori dan pilihan bahan produk

## Status dan tahapan pesanan
Status yang dilihat pelanggan: Diterima -> Sedang Diproses -> Cetak DTF -> Pressing -> Siap Diambil -> Selesai.
Tahapan produksi internal (checklist admin): Desain & Mockup -> Pengadaan Baju Polos -> Cetak DTF & Material -> Penyablonan Workshop -> Pemeriksaan QC -> Siap Diambil / Selesai.
Pemetaan tahapan internal ke status pelanggan:
- Pesanan masuk                         -> Diterima
- Desain & Mockup, Pengadaan Baju Polos -> Sedang Diproses
- Cetak DTF & Material                  -> Cetak DTF
- Penyablonan Workshop, Pemeriksaan QC  -> Pressing
- Siap Diambil                          -> Siap Diambil
- Diambil / selesai                     -> Selesai
  Pengelompokan di dashboard Figma (Pesanan Masuk, Menunggu Pemeriksaan, Sedang Diproses, Siap Diambil)
  hanya cara menampilkan daftar, bukan status baru.
  Bilah status di DESIGN.md (Pemisahan Warna, Afdruk Film, dst.) adalah teks bawaan Stitch untuk sablon manual.
  Abaikan; pakai daftar status di atas.

## Data utama (usulan, koordinasikan dengan backend)
Tabel yang sudah ada di database: users, products, product_colors, product_sizes, product_materials, orders,
order_items, production_stages, activity_logs, stock_items, transactions, personal_access_tokens.
- users: id, nama, email, password, no_hp, role (owner/admin/customer), aktif
  Tambahan Tahap 7: avatar_path, two_factor_enabled, two_factor_secret (terenkripsi), two_factor_recovery_codes
  (di-hash), email_verified_at (hanya jika email asli dipakai).
- categories: id, nama (BARU; products.category yang masih enum diganti category_id, data lama dimigrasi:
  kaos_custom -> Kaos Custom, kaos_oversize -> Kaos Oversize, baju_polo -> Baju Polo)
- products: id, nama, deskripsi, harga_dasar, gambar (image_path), category_id, bahan (teks ringkasan, tidak memengaruhi harga), aktif
- product_variants: warna, ukuran, tambahan harga
- product_materials: product_id, nama, tambahan_harga, is_default
- orders: id, pelanggan, status, total_harga, catatan, file_desain, dibuat_oleh (pelanggan atau admin untuk pesanan manual), selesai_pada
- order_items: pesanan, produk, bahan (salinan nama), warna, ukuran, jumlah, harga
- production_stages: pesanan, tahapan, selesai, waktu, diperbarui_oleh
- stocks: item (baju polos, material DTF), jumlah
- transactions: tipe (pemasukan/pengeluaran), nominal, keterangan, tanggal, kategori. sumber, order_id, dibuat_oleh
- workshop_settings (BARU): key, value (info kontak workshop; satu baris per kunci)
- welcome_slides (BARU): id, image_path, judul (opsional), urutan, aktif
- notifications (BARU): id, user_id, tipe, judul, isi, order_id (opsional), dibaca_pada, dibuat_pada
- device_tokens (hanya bila push dikerjakan): id, user_id, token_fcm, platform, diperbarui_pada
- password_reset_tokens (BARU, hanya jika Lupa Password dikerjakan)
- personal_access_tokens: dipakai juga untuk daftar sesi/perangkat (nama perangkat, terakhir dipakai)


## Urutan pengerjaan (sesuai target mingguan)
- Minggu 11: setup project, tema dari Figma, komponen UI dasar.
- Minggu 12: Splash, Onboarding, Login, Register, mode tamu, navigasi per peran, dashboard kosong per peran, tentang aplikasi, logout.
- Minggu 13-14: API dan CRUD: produk, pesanan (pelanggan), manajemen pesanan dan proses (admin), stok.
- Minggu 15: keuangan dan laporan (owner), pengujian, error handling, perbaikan bug, serta Tahap 7 (di bawah).
- Minggu 16: demo prototype dan presentasi.
  Tahap 7 (dikerjakan satu sesi per bagian):
- 7A Pembersihan: perbaiki teks rusak (encoding), hapus "Ubah Jumlah" di Manajemen Stok, header Pesanan/Proses owner.
- 7B Pengaturan: pilihan bahasa dan tema (tanpa backend).
- 7C Data baru dan gambar: kategori, gambar produk, info kontak workshop, slide welcome (butuh endpoint).
- 7D Akun: edit profil, keamanan akun (ubah sandi, sesi perangkat, 2FA, hapus akun), notifikasi dalam aplikasi.
- 7E Lanjutan (opsional, perlu persetujuan terpisah): push notifikasi (FCM); lupa password dan email asli.

## Di luar cakupan (jangan dibuat)
- Pembayaran online / payment gateway.
- Integrasi IoT dan AI (opsional dari kampus, tidak diambil).
- Fitur chat dalam aplikasi (cukup tombol WhatsApp).
  (Notifikasi tidak lagi di luar cakupan; lihat Tahap 7.)

## Aturan pemesanan (Tahap 4)
- Satu pesanan = satu produk. Model data mendukung banyak item, tetapi UI hanya membuat satu item.
- Harga satuan = harga dasar produk + tambahan harga bahan + tambahan harga ukuran. Total = harga satuan x jumlah.
  Jumlah 1-999, tanpa minimum order.
- Wajib diisi: bahan, warna, ukuran, jumlah, dan minimal salah satu dari file desain atau catatan.
  Desain: gambar jpg/png/webp, maksimal 10 MB. Catatan maksimal 500 karakter.
- Kode pesanan format GEC-YYYYMMDD-NNN. Status awal DITERIMA.
- Status: DITERIMA, SEDANG_DIPROSES, CETAK_DTF, PRESSING, SIAP_DIAMBIL, SELESAI, DIBATALKAN.
  Pelanggan hanya boleh membatalkan saat DITERIMA.
- Tab Pesan menampilkan daftar pesanan milik pelanggan (filter Semua/Aktif/Selesai) dan tombol Buat Pesanan Baru.
- Tamu boleh melihat Beranda, Katalog, Detail Produk, Kontak. Tombol Pesan Sekarang untuk tamu membuka Login,
  lalu kembali ke Detail Produk yang sama.
- Semua data produk, harga, warna, dan pesanan yang dipakai di aplikasi berasal dari server; data seed hanyalah data contoh.
- Tidak boleh ada teks atau UI yang menyatakan pelanggan bisa memesan tanpa login.

## Aturan produksi (Tahap 5)
Tahapan produksi (urut, selesai berurutan): Desain & Mockup, Pengadaan Baju Polos, Cetak DTF & Material,
Penyablonan Workshop, Pemeriksaan QC.
Aksi material: Baju tiba, DTF tiba, Siap disablon.
- Pesanan berstatus DITERIMA sampai staff menekan "Terima & Mulai Proses" (jadi SEDANG_DIPROSES).
- Tahapan hanya boleh dicentang berurutan. Yang boleh dibatalkan centangnya hanya tahapan terakhir yang selesai.
- Prasyarat: Pengadaan Baju butuh Baju tiba. Cetak DTF & Material butuh DTF tiba. "Siap disablon" butuh Baju tiba
  dan DTF tiba, dan menjadi prasyarat Penyablonan. Baju tiba/DTF tiba/Siap disablon tidak bisa dibatalkan bila
  tahapan yang bergantung padanya sudah selesai.
- Status pelanggan diturunkan dari tahapan aktif (tahapan pertama yang belum selesai):
  Desain atau Pengadaan -> SEDANG_DIPROSES; Cetak DTF & Material -> CETAK_DTF; Penyablonan atau QC -> PRESSING;
  kelima tahapan selesai -> SIAP_DIAMBIL. "Tandai Selesai (sudah diambil)" -> SELESAI.
- Perubahan tahapan disimpan lewat tombol "Simpan Perubahan" (bukan per centang). Server memvalidasi ulang
  semua aturan, tidak memercayai UI.
- Setiap perubahan dicatat di riwayat aktivitas (waktu, nama staff, aksi).
- Hanya ADMIN dan OWNER yang boleh memakai fungsi staff. Pelanggan hanya melihat statusnya.
- Pesanan DIBATALKAN tidak bisa diproses. Admin tidak membatalkan pesanan di tahap ini.
- Pesanan menyimpan salinan nama dan no HP pelanggan, serta asalnya (pelanggan atau staff/manual).

## Aturan owner (Tahap 6)
Keuangan (hanya OWNER; server menolak role lain):
- Transaksi: id, tipe (PEMASUKAN/PENGELUARAN), kategori, nominal (bulat, > 0, maks 1.000.000.000), tanggal,
  keterangan (maks 200), sumber (MANUAL atau PESANAN), orderId (hanya untuk sumber PESANAN), dibuat oleh.
- Pengeluaran: Bahan Baku (baju), Material DTF, Operasional, Gaji, Lainnya.
  Pemasukan manual: Penjualan Lain, Lainnya (di luar pesanan aplikasi).
- Pesanan berstatus SELESAI dihitung sebagai pemasukan otomatis (kategori "Pesanan") sebesar totalnya, tanggal
  = saat ditandai selesai. Tidak disimpan sebagai transaksi, hanya dihitung. Pesanan DIBATALKAN tidak dihitung.
- Laba/rugi = total pemasukan (manual + pesanan) - total pengeluaran. Nilai negatif ditampilkan sebagai rugi.
- Periode: Bulan Ini, Bulan Lalu, 7 Hari Terakhir, 30 Hari Terakhir, Pilih Tanggal (rentang). Batas periode
  memakai awal hari (00:00) sampai akhir hari (23:59:59) zona waktu perangkat.
- Transaksi manual boleh diubah dan dihapus (dengan konfirmasi). Transaksi sumber PESANAN hanya dilihat.
  Produk (OWNER): nama (3-60), kategori, bahan, deskripsi (maks 500), harga dasar (1.000 - 10.000.000),
  minimal 1 warna (nama + kode #RRGGBB), 1 ukuran (S-XXXL) dengan tambahan harga >= 0, dan 1 bahan (lihat Aturan lanjutan, Tahap 6D). Status aktif/nonaktif;
  produk nonaktif tidak tampil di katalog pelanggan. Pesanan lama tidak berubah karena menyimpan salinan.
  Akun admin (OWNER): nama, email unik, password awal (minimal 8, ada huruf dan angka). Role selalu ADMIN. Akun
  nonaktif tidak bisa login. Akun owner tidak bisa dinonaktifkan atau dihapus lewat aplikasi. Password tidak
  pernah ditampilkan ulang atau dicatat di log.
  Pesanan manual dibuat dari Dashboard Owner memakai layar yang sama dengan Admin (rute staff bersama).
  Tanggal: gunakan java.util.Calendar/SimpleDateFormat (Locale sesuai bahasa terpilih); jangan java.time.
- Tanggal transaksi manual tidak boleh melewati hari ini.

## Aturan lanjutan owner (Tahap 6B-6C)
Produk dan pesanan:
- Produk nonaktif atau dihapus tidak bisa dipesan: server menolak (pelanggan dan pesanan manual staff),
  bukan hanya disembunyikan di UI. Pesanan lama tidak berubah (menyimpan salinan).
- ID produk, warna, dan ukuran tetap saat diubah. Produk boleh dihapus walau punya pesanan (dengan konfirmasi).
  Akun admin:
- Email harus unik terhadap SEMUA akun (admin, owner, pelanggan), tanpa membedakan huruf besar-kecil.
- Pesan tolak login akun nonaktif: "Akun dinonaktifkan. Hubungi Owner."
- Atur ulang password oleh Owner = Owner mengisi password sementara baru (aturan password sama); tidak pernah
  ditampilkan ulang.
- Login dan kelola akun memakai satu sumber akun yang sama di server.
  Ekspor laporan (OWNER saja):
- PDF (PdfDocument bawaan Android) dan CSV berlabel "Unduh Excel (CSV)": pemisah titik koma, penanda UTF-8,
  nominal angka tanpa pemisah ribuan. Isi sama dengan layar Laporan untuk periode terpilih.
- Disimpan lewat CreateDocument (pengguna memilih lokasi). Nama file memuat periode.
- Input warna produk: kode boleh diketik dengan atau tanpa "#" dan dinormalisasi ke #RRGGBB huruf besar sebelum
  divalidasi. Tersedia pilihan warna umum yang mengisi nama dan kode sekaligus.

## Aturan lanjutan (Tahap 6D: no HP admin, bahan, kategori)
No HP:
- No HP wajib untuk akun admin (form tambah admin) dan tetap wajib untuk register pelanggan. Aturan sama untuk
  keduanya, hanya di core/util/PhoneRules.kt (fungsi murni): buang spasi, tanda hubung, dan kurung; awalan +62 atau
  62 diganti 0; hasil hanya angka, diawali 0, panjang 10-14 digit.
- No HP unik terhadap SEMUA akun (admin, owner, pelanggan) setelah dinormalisasi. Tampil di daftar dan detail akun
  admin untuk Owner. Mengubah no HP sendiri tersedia lewat Edit Profil (Tahap 7).
  Pilihan bahan (per produk, dikelola OWNER di form produk):
- ProductMaterial: id, nama (2-80, unik per produk tanpa membedakan huruf besar-kecil), tambahanHarga (0 -
  10.000.000), isDefault. Setiap produk wajib punya minimal 1 bahan dan tepat 1 bahan bawaan. ID bahan stabil saat
  diubah.
- Harga satuan = harga dasar + tambahan harga bahan + tambahan harga ukuran; total = harga satuan x jumlah (1-999).
  Pelanggan wajib memilih bahan (bahan bawaan terpilih lebih dulu). Item pesanan menyimpan SALINAN nama bahan; harga
  satuan sudah termasuk bahan. Pesanan lama tidak berubah bila bahan diubah atau dihapus.
- Server (pesanan pelanggan dan pesanan manual staff) menolak bahan yang tidak ada pada produk aktif tersebut.
- Kolom ringkasan bahan produk (products.material) tetap sebagai teks informasi dan TIDAK memengaruhi harga.
  Kategori produk (dikelola OWNER):
- Category: id, nama (2-40, unik tanpa membedakan huruf besar-kecil). Kategori adalah data, bukan enum. ID stabil
  saat nama diubah. Seed: Kaos Custom, Kaos Oversize, Baju Polo.
- Setiap produk wajib punya satu kategori. Menghapus kategori yang masih dipakai produk DITOLAK dengan pesan yang
  menyebut jumlah produk yang memakainya.
- Filter kategori di Katalog pelanggan membaca daftar dari server dan hanya menampilkan kategori yang punya
  minimal 1 produk aktif.
- Layar Kelola Kategori dibuka dari Akun Owner (bagian Pengelolaan Workshop).

## Ubah kata sandi (semua peran yang login)
- Wajib: kata sandi lama (harus benar), kata sandi baru, konfirmasi (harus sama).
- Kata sandi baru minimal 8 karakter, mengandung huruf dan angka, tidak boleh sama dengan yang lama.
- Kata sandi tidak pernah ditampilkan ulang, dicatat di log, atau disimpan di sesi.
- Email dan no HP tidak disimpan di sesi; tampil di profil dari endpoint profil.

## Aturan Tahap 7A: pembersihan
- Teks rusak (contoh "&#8226;") adalah bug encoding. Perbaiki di sumbernya (file UTF-8 tanpa BOM; periksa string dari
  server) di SEMUA layar, bukan hanya satu layar.
- Hapus fitur "Ubah Jumlah" di Manajemen Stok; perubahan jumlah memakai fitur edit.
- Layar Pesanan dan Proses milik Owner memakai header sendiri yang sama gayanya dengan halaman owner lainnya,
  tidak lagi memakai header admin.

## Aturan Tahap 7B: bahasa dan tema (umum, semua peran dan tamu)
- Bahasa: Indonesia (bawaan) dan Inggris. Pilihan: Ikuti Sistem, Indonesia, English. Disimpan di DataStore dan
  diterapkan lewat pengaturan bahasa per aplikasi. Semua teks UI memakai strings.xml dan values-en/strings.xml.
  Data dari server (nama produk, deskripsi, kategori) tidak diterjemahkan. Pesan error server dipetakan dari kode ke
  string. Format tanggal dan angka mengikuti bahasa terpilih; mata uang tetap Rupiah.
- Tema: Ikuti Sistem (bawaan), Terang, Gelap. Disimpan di DataStore dan diterapkan langsung tanpa memulai ulang
  aplikasi. Skema warna gelap diturunkan dari tema terang di ui/theme (tidak ada desain Figma). Gambar produk dan
  data warna produk tidak berubah mengikuti tema.
- Keduanya dibuka dari layar Pengaturan (menu Akun semua peran).

## Aturan Tahap 7C: gambar produk, kontak workshop, slide welcome
Gambar produk (OWNER):
- Satu gambar utama per produk, jpg/png/webp, maksimal 10 MB. Diunggah dari form produk, boleh diganti atau dihapus.
  Server memeriksa tipe dari isi file, membuat nama file sendiri, dan menghapus file lama saat diganti.
- Tampil di Beranda, Katalog, dan Detail Produk. Produk tanpa gambar menampilkan placeholder.
  Info kontak workshop (ADMIN dan OWNER mengubah; semua orang termasuk tamu membaca):
- Isian: nama workshop, no WhatsApp (divalidasi PhoneRules; tombol WhatsApp memakai format internasional),
  alamat, jam operasional, email (opsional), tautan peta (opsional), Instagram (opsional).
- Tab Kontak pelanggan dan tombol WhatsApp membaca data ini dari server. Bila server tidak terjangkau,
  tampilkan state error dengan tombol coba lagi.
  Slide gambar halaman Welcome (ADMIN dan OWNER mengelola; dibaca publik):
- 1 sampai 6 slide aktif; gambar jpg/png/webp maksimal 10 MB; judul opsional (maks 60); urutan dapat diubah;
  slide dapat dinonaktifkan atau dihapus (dengan konfirmasi).
- Bila tidak ada slide aktif atau gagal dimuat, Welcome memakai gambar bawaan aplikasi.

## Aturan Tahap 7D: profil, keamanan akun, notifikasi
Edit profil (semua peran yang login):
- Yang dapat diubah: nama (2-100), no HP (PhoneRules, unik terhadap semua akun), foto profil (opsional, jpg/png/webp,
  maksimal 5 MB, boleh dihapus). Email hanya ditampilkan (perubahan email menunggu fitur email asli).
- Data profil dibaca dari endpoint profil, tidak disimpan di sesi.
  Keamanan akun (semua peran yang login; layar Keamanan Akun dari menu Akun):
- Ubah kata sandi: aturan di bagian "Ubah kata sandi".
- Sesi dan perangkat: daftar perangkat yang sedang masuk (nama perangkat, terakhir dipakai, penanda perangkat ini),
  "Keluar dari perangkat ini", dan "Keluar dari semua perangkat" (token lain dicabut di server).
- Verifikasi dua langkah (2FA), opsional, berbasis kode 6 digit dari aplikasi authenticator (TOTP):
  - Aktifkan: server membuat kunci rahasia; aplikasi menampilkan kunci itu (dan tombol membuka tautan otpauth://
    bila ada aplikasi authenticator); pengguna memasukkan kode 6 digit untuk konfirmasi; lalu server memberi 8 kode
    cadangan yang ditampilkan sekali saja.
  - Login: setelah email dan password benar dan 2FA aktif, server meminta kode 6 digit (atau satu kode cadangan,
    sekali pakai) sebelum memberi token sesi. Kode salah berulang dibatasi (rate limit di server).
  - Nonaktifkan: wajib kata sandi dan kode 6 digit. Bila perangkat authenticator hilang, OWNER dapat mematikan 2FA
    akun admin; akun owner dan pelanggan memakai kode cadangan.
  - Kunci rahasia dan kode tidak pernah dicatat di log atau ditampilkan ulang setelah pengaturan.
- Hapus akun:
  - Pelanggan boleh menghapus akunnya sendiri (wajib kata sandi dan konfirmasi). Pesanan lama tetap ada karena
    menyimpan salinan nama dan no HP.
  - Admin tidak boleh menghapus akunnya sendiri. Hanya OWNER yang dapat menghapus akun admin (dari Kelola Akun
    Admin, dengan konfirmasi). Akun owner tidak bisa dihapus lewat aplikasi.
  Notifikasi:
- Layar Notifikasi (ikon di header Beranda) berisi daftar dari server, terbaru di atas, dengan penanda belum dibaca,
  "Tandai semua dibaca", dan ketuk untuk membuka pesanan terkait. Badge jumlah belum dibaca di ikon.
- Peristiwa: pelanggan menerima notifikasi saat status pesanannya berubah atau pesanan dibatalkan; admin dan owner
  menerima notifikasi saat ada pesanan baru dan saat stok menipis.
- Tahap 7D hanya notifikasi dalam aplikasi: daftar dibaca dari server saat aplikasi dibuka dan saat layar Notifikasi
  dibuka (tarik untuk menyegarkan). Tanpa push, tanpa Firebase.
- Push saat aplikasi tertutup (FCM) ditunda ke Tahap 7E dan butuh persetujuan terpisah, karena memerlukan project
  Firebase, file google-services.json, dan akun layanan di server.

## Aturan Tahap 7E: lanjutan (dapat ditunda)
- Push notifikasi (FCM): hanya setelah disetujui. Aplikasi mendaftarkan token perangkat setelah login dan menghapusnya
  saat logout; izin notifikasi diminta di Android 13 ke atas; isi push sama dengan notifikasi dalam aplikasi dan tidak
  memuat data sensitif; pengguna dapat mematikannya dari Pengaturan.
- Lupa kata sandi: tautan atur ulang dikirim ke email terdaftar, berlaku terbatas, sekali pakai. Butuh email asli
  (SMTP) di server.
- Email asli: verifikasi email saat register. Sampai tahap ini selesai, email diperlakukan sebagai ID masuk biasa.
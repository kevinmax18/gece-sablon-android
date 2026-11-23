# Spesifikasi Aplikasi GeCe Screen Printing

## Ringkasan
Aplikasi mobile untuk UMKM GeCe Screen Printing (sablon DTF). Satu aplikasi, tiga peran:
pelanggan, admin workshop, owner. Peran ditentukan oleh server saat login, lalu aplikasi
menampilkan navigasi sesuai peran. Acuan: docs/flowchart/ dan docs/figma/.
Flowchart hanya menggambarkan alur UI/UX, belum mencakup backend dan database.

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
- Pembuatan pesanan: Pilih Produk -> Pilih Warna -> Pilih Ukuran -> Atur Jumlah (harga otomatis)
  -> Upload Desain / Catatan -> Ringkasan Pesanan -> Kirim/Konfirmasi -> Pesanan Berhasil.
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
- Akun: profil, pengaturan, logout.

## Fitur Owner
- Dashboard Owner.
- Manajemen Pesanan dan Proses Workshop (pengawasan, sama seperti admin).
- Keuangan: Catat Pemasukan/Pengeluaran, Ringkasan Laba/Rugi, Detail Transaksi, Filter Periode.
- Laporan/Ekspor: unduh PDF atau XLSX.
- Kelola Produk, Harga & Variasi (warna, ukuran).
- Kelola Akun Admin.
- Akun: profil, pengaturan, logout.

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

## Data utama (usulan, belum final)
- users: id, nama, email, password, no_hp, role (owner/admin/customer)
- products: id, nama, deskripsi, harga_dasar, gambar
- product_variants: warna, ukuran, tambahan harga
- orders: id, pelanggan, status, total_harga, catatan, file_desain, dibuat_oleh (pelanggan atau admin untuk pesanan manual)
- order_items: pesanan, produk, warna, ukuran, jumlah, harga
- production_stages: pesanan, tahapan, selesai, waktu, diperbarui_oleh
- stocks: item (baju polos, material DTF), jumlah
- transactions: tipe (pemasukan/pengeluaran), nominal, keterangan, tanggal

## Urutan pengerjaan (sesuai target mingguan)
- Minggu 11: setup project, tema dari Figma, komponen UI dasar.
- Minggu 12: Splash, Onboarding, Login, Register, mode tamu, navigasi per peran, dashboard kosong per peran, tentang aplikasi, logout.
- Minggu 13-14: API dan CRUD: produk, pesanan (pelanggan), manajemen pesanan dan proses (admin), stok.
- Minggu 15: keuangan dan laporan (owner), pengujian, error handling, perbaikan bug.
- Minggu 16: demo prototype dan presentasi.

## Di luar cakupan (jangan dibuat)
- Pembayaran online / payment gateway.
- Notifikasi push.
- Integrasi IoT dan AI (opsional dari kampus, tidak diambil).
- Fitur chat dalam aplikasi (cukup tombol WhatsApp).

## Aturan pemesanan (Tahap 4)
- Satu pesanan = satu produk. Model data mendukung banyak item, tetapi UI hanya membuat satu item.
- Harga satuan = harga dasar produk + tambahan harga ukuran. Total = harga satuan x jumlah.
  Jumlah 1-999, tanpa minimum order. Posisi dan ukuran sablon ditulis pelanggan di catatan (tidak ada pilihan area cetak).
  Rumus hanya ada di core/util/PriceCalculator.kt (fungsi murni), tidak boleh di UI.
- Wajib diisi: warna, ukuran, jumlah, dan minimal salah satu dari file desain atau catatan.
  Desain: gambar jpg/png/webp, maksimal 10 MB. Catatan maksimal 500 karakter.
- Kode pesanan format GEC-YYYYMMDD-NNN. Status awal DITERIMA.
- Status: DITERIMA, SEDANG_DIPROSES, CETAK_DTF, PRESSING, SIAP_DIAMBIL, SELESAI, DIBATALKAN.
  Pelanggan hanya boleh membatalkan saat DITERIMA.
- Tab Pesan menampilkan daftar pesanan milik pelanggan (filter Semua/Aktif/Selesai) dan tombol Buat Pesanan Baru.
- Tamu boleh melihat Beranda, Katalog, Detail Produk, Kontak. Tombol Pesan Sekarang untuk tamu membuka Login,
  lalu kembali ke Detail Produk yang sama.
- Semua data produk, harga, warna, dan pesanan di fake repository hanyalah data contoh.
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
- Perubahan tahapan disimpan lewat tombol "Simpan Perubahan" (bukan per centang). Repository memvalidasi ulang
  semua aturan, tidak memercayai UI.
- Setiap perubahan dicatat di riwayat aktivitas (waktu, nama staff, aksi).
- Hanya ADMIN dan OWNER yang boleh memakai fungsi staff. Pelanggan hanya melihat statusnya.
- Pesanan DIBATALKAN tidak bisa diproses. Admin tidak membatalkan pesanan di tahap ini.
- Pesanan menyimpan salinan nama dan no HP pelanggan, serta asalnya (pelanggan atau staff/manual).

## Aturan owner (Tahap 6)
Keuangan (hanya OWNER; repository menolak role lain):
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
  minimal 1 warna (nama + kode #RRGGBB) dan 1 ukuran (S-XXXL) dengan tambahan harga >= 0. Status aktif/nonaktif;
  produk nonaktif tidak tampil di katalog pelanggan. Pesanan lama tidak berubah karena menyimpan salinan.
  Akun admin (OWNER): nama, email unik, password awal (minimal 8, ada huruf dan angka). Role selalu ADMIN. Akun
  nonaktif tidak bisa login. Akun owner tidak bisa dinonaktifkan atau dihapus lewat aplikasi. Password tidak
  pernah ditampilkan ulang atau dicatat di log.
  Pesanan manual dibuat dari Dashboard Owner memakai layar yang sama dengan Admin (rute staff bersama).
  Tanggal: gunakan java.util.Calendar/SimpleDateFormat (Locale id-ID); jangan java.time.
- Tanggal transaksi manual tidak boleh melewati hari ini.

## Ubah kata sandi (semua peran yang login)
- Wajib: kata sandi lama (harus benar), kata sandi baru, konfirmasi (harus sama).
- Kata sandi baru minimal 8 karakter, mengandung huruf dan angka, tidak boleh sama dengan yang lama.
- Kata sandi tidak pernah ditampilkan ulang, dicatat di log, atau disimpan di sesi.
- Email dan no HP tidak disimpan di sesi; tampil di profil hanya setelah ada endpoint profil (integrasi API).
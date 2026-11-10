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

## Pertanyaan terbuka (konfirmasi sebelum dikerjakan)
- Apakah pelanggan bisa membatalkan pesanan? Sampai tahap apa?
- Format ekspor PDF/XLSX: kolom apa saja yang dimuat?
<p align="center">
  <img src="docs/logo_gece.png" alt="Logo GeCe Sablon" width="220">
</p>

# GeCe Sablon

Aplikasi mobile Android untuk UMKM **GeCe Screen Printing** (sablon DTF). Pelanggan dapat melihat katalog,
memesan, dan memantau status pesanan; admin workshop mengelola pesanan dan produksi; owner memantau keuangan dan pengaturan.

Proyek ini dikerjakan sebagai tugas Project-Based Learning (PjBL), Teknik Komputer dan Jaringan,
Politeknik Negeri Ujung Pandang.

## Teknologi

| Bagian | Teknologi |
|---|---|
| Bahasa dan alat | Kotlin, Android Studio |
| Tampilan | Jetpack Compose + Material 3 |
| Arsitektur | MVVM + Repository Pattern |
| Jaringan | Retrofit + OkHttp + Kotlinx Serialization |
| Penyimpanan lokal | DataStore |
| Backend | Laravel + MySQL |

## Fitur per peran

- **Pelanggan:** katalog dan detail produk, pemesanan (warna, ukuran, jumlah, unggah desain), pelacakan status, riwayat, kontak WhatsApp.
- **Admin workshop:** manajemen pesanan, checklist produksi, kelola stok, pesanan manual.
- **Owner:** dashboard, keuangan dan laporan, kelola produk dan harga, kelola akun admin.

## Perkembangan mingguan

### Minggu 11: Desain UI/UX dan arsitektur
Minggu 11 (4 November 2026). Perancangan UI/UX di Figma dan flowchart alur pengguna.

| Flowchart | Desain Figma |
|---|---|
| <img src="docs/screenshots/minggu-11/01-flowchart.png" width="360">  | <img src="docs/screenshots/minggu-11/02-figma.png" width="360"> |

### Minggu 12: Layar awal, autentikasi, dan navigasi
Minggu 12 (11 November 2026). Splash, onboarding, welcome, login, register, navigasi per peran, dan logout.

| Splash | Onboarding | Welcome | Login | Register |
|---|---|---|---|---|
| <img src="docs/screenshots/minggu-12/01-splash.png" width="150"> | <img src="docs/screenshots/minggu-12/02-onboarding.png" width="150"> | <img src="docs/screenshots/minggu-12/03-welcome.png" width="150"> | <img src="docs/screenshots/minggu-12/04-login.png" width="150"> | <img src="docs/screenshots/minggu-12/05-register.png" width="150"> |

### Minggu 13: Fitur utama
Minggu 13 (18 November 2026). Katalog dan pemesanan pelanggan, pesanan dan proses produksi staff, dashboard admin, dashboard owner, ringkasan keuangan owner, laporan keuangan owner.

| Beranda | Katalog | Detail Produk | Form Pesanan | Detail Pesanan |
|---|---|---|---|---|
| <img src="docs/screenshots/minggu-13/01-beranda.png" width="150"> | <img src="docs/screenshots/minggu-13/02-katalog.png" width="150"> | <img src="docs/screenshots/minggu-13/03-detail-produk.png" width="150"> | <img src="docs/screenshots/minggu-13/04-form-pesanan.png" width="150"> | <img src="docs/screenshots/minggu-13/05-detail-pesanan.png" width="150"> |

| Pesanan (Admin) | Proses Workshop | Detail Pesanan (Admin) | Dashboard Admin | Kelola Stok |
|---|---|---|---|---|
| <img src="docs/screenshots/minggu-13/06-admin-pesanan.png" width="150"> | <img src="docs/screenshots/minggu-13/07-admin-proses.png" width="150"> | <img src="docs/screenshots/minggu-13/08-admin-detail.png" width="150"> | <img src="docs/screenshots/minggu-13/09-admin-dashboard.png" width="150"> | <img src="docs/screenshots/minggu-13/10-admin-stok.png" width="150"> |

| Dashboard Owner | Ringkasan Keuangan | Laporan Keuangan |
|---|---|---|
| <img src="docs/screenshots/minggu-13/11-owner-dashboard.png" width="150"> | <img src="docs/screenshots/minggu-13/12-ringkasan-keuangan.png" width="150"> | <img src="docs/screenshots/minggu-13/13-laporan-keuangan.png" width="150"> |

### ERD Database
<img src="docs/screenshots/minggu-13/ERD-database.png">

## Fitur yang sudah selesai
Berikut fitur-fitur yang telah diimplementasikan dan belum diimplementasikan ke aplikasi:

**Umum**
- [x] Splash, onboarding (hanya sekali), dan layar welcome
- [x] Login, register (khusus pelanggan), sesi tersimpan, dan logout
- [x] Navigasi dengan bottom bar berbeda untuk tiap peran
- [x] Mode tamu: melihat beranda, katalog, dan detail produk tanpa login

**Pelanggan**
- [x] Katalog produk dengan pencarian dan filter kategori, serta detail produk
- [x] Pemesanan: pilih warna, ukuran, jumlah, unggah desain, dan catatan, dengan harga dihitung otomatis
- [x] Konfirmasi pesanan dan layar pesanan berhasil
- [x] Riwayat pesanan dengan filter, detail pesanan, dan linimasa status
- [x] Pembatalan pesanan (hanya saat status Diterima)
- [x] Hubungi workshop lewat WhatsApp

**Admin workshop**
- [x] Dashboard ringkasan pesanan dan stok
- [x] Manajemen pesanan: pencarian, filter, dan detail
- [x] Checklist produksi lima tahap, aksi material (baju tiba, DTF tiba, siap disablon), dan riwayat aktivitas
- [x] Status pesanan pelanggan diturunkan otomatis dari tahapan produksi
- [x] Kelola stok dengan penanda stok menipis
- [x] Pesanan manual untuk pelanggan yang datang langsung

**Owner**
- [x] Dashboard ringkasan keuangan bulan ini dan pesanan
- [x] Keuangan: catat pemasukan dan pengeluaran, ubah dan hapus, filter periode, dan detail transaksi
- [x] Pemasukan otomatis dari pesanan yang selesai
- [x] Laporan keuangan: laba/rugi dan ringkasan per kategori
- [x] Pengawasan pesanan dan proses produksi (layar yang sama dengan admin)

**Pengujian otomatis (unit test)**
- [x] Perhitungan harga, aturan produksi, aturan stok, perhitungan keuangan, dan aturan periode

**Belum selesai**
- [ ] Kelola produk, harga, dan variasi (owner)
- [ ] Kelola akun admin (owner)
- [ ] Ekspor laporan ke PDF dan Excel

## Struktur proyek

```
app/src/main/java/com/gecesablon/app/
├── core/      (network, datastore, di, util)
├── data/      (model, remote, repository)
└── ui/        (theme, components, navigation, customer, admin, owner, staff, auth, ...)
docs/          (spesifikasi, desain, flowchart, screenshots)
```
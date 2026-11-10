# AGENTS.md - GeCe Screen Printing (Android)

## Konteks
Aplikasi mobile UMKM sablon DTF untuk tugas PjBL, tiga peran: pelanggan (dan tamu), admin workshop, owner.
Sebelum mengerjakan fitur, baca docs/spesifikasi.md, lalu lihat docs/flowchart/ dan docs/figma/.
Jika spesifikasi bertentangan dengan gambar Figma, tampilan mengikuti Figma dan alur mengikuti spesifikasi.
Kerjakan hanya fitur yang diminta. Fitur di bagian "Di luar cakupan" pada spesifikasi tidak boleh dibuat.

## Stack wajib (jangan ganti, jangan tambah library tanpa izin)
- Kotlin, build Gradle Kotlin DSL, min SDK 24
- UI: Jetpack Compose + Material 3
- Arsitektur: MVVM + Repository Pattern
- Networking: Retrofit + OkHttp
- Asynchronous: Kotlin Coroutines + Flow
- Penyimpanan lokal: DataStore (token, role), Room (cache bila diperlukan)
- Backend: REST API Laravel di repo terpisah, autentikasi berbasis token
- Fitur yang butuh library tambahan (mis. ekspor PDF/XLSX, memuat gambar): tanyakan dulu, usulkan opsi, tunggu persetujuan.

## Struktur paket
core/ (network, database, datastore, util)
data/ (model, remote, repository)
ui/
theme/, navigation/, components/
splash/, onboarding/, auth/ (login, register), about/
customer/ (beranda, katalog, detail produk, pemesanan, status, riwayat, kontak, profil)
admin/ (dashboard, pesanan, proses, stok, pesanan manual, akun)
owner/ (dashboard, keuangan, laporan, kelola produk, kelola akun admin, akun)
Setiap fitur: Screen (Compose) + ViewModel (StateFlow) + Repository.
Komponen yang dipakai dua peran atau lebih (daftar pesanan, detail pesanan, tahapan produksi) ditaruh di ui/components/ agar tidak digandakan.

## Peran dan navigasi
- Role berasal dari respons login, disimpan di DataStore, dan menentukan NavGraph yang dipakai.
- Pelanggan/tamu: Beranda, Katalog, Pesan, Kontak. Tamu hanya boleh melihat Beranda, Katalog, Detail Produk.
- Admin: Beranda, Pesanan, Proses, Akun.
- Owner: Beranda, Pesanan, Proses, Keuangan, Akun.
- Halaman milik satu peran tidak boleh bisa dibuka oleh peran lain (cek role di navigasi dan di API).

## Aturan kode
- Tampilan mengikuti Figma. Warna, tipografi, dan bentuk disimpan di ui/theme/, jangan ditulis langsung di tiap layar.
- Semua teks yang tampil ke pengguna ada di strings.xml (bahasa Indonesia).
- Tidak ada logika bisnis atau panggilan API di dalam composable. Lewat ViewModel dan Repository.
- Setiap layar yang memanggil API menangani state loading, sukses, kosong, dan error.
- Harga pesanan dihitung otomatis dari produk, variasi, dan jumlah. Rumus harga ditaruh di satu tempat (Repository atau use case), bukan di UI.
- Tombol "Hubungi workshop" membuka WhatsApp lewat Intent. Nomor workshop dibaca dari konfigurasi, bukan ditulis di banyak tempat.
- Nama class PascalCase, fungsi dan variabel camelCase. Beri komentar singkat untuk logika yang tidak jelas.

## Keamanan
- Jangan menulis password, token, API key, atau alamat server langsung di kode.
- Alamat server dan nilai rahasia dibaca dari local.properties lewat BuildConfig.
- Jangan membuat, mengubah, atau mem-commit local.properties, .env, atau keystore.
- Alamat server lokal untuk emulator: http://10.0.2.2:8000/
- Data keuangan dan akun admin hanya boleh diakses owner. Jangan mengandalkan penyembunyian menu saja.

## Cara kerja
- Kerjakan satu fitur atau satu tahap per tugas. Jelaskan rencana singkat sebelum mengubah banyak file.
- Jangan menghapus atau menulis ulang file yang tidak berkaitan dengan tugas.
- Jangan menjalankan git commit, git push, atau perintah yang menghapus file. Saya yang melakukannya.
- Jika spesifikasi kurang jelas, tanyakan, jangan menebak. Lihat bagian "Pertanyaan terbuka" di spesifikasi.
- Setelah selesai, sebutkan file apa saja yang diubah dan apa yang perlu saya tes di emulator.
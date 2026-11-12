# Auto Restart (Zebra / Android)

Aplikasi sederhana: pilih jam + hari, HP restart otomatis.
Butuh status **Device Owner** (tanpa root).

## A. Build APK

### Cara 1: Android Studio
1. Install Android Studio, lalu File > Open > pilih folder ini.
2. Tunggu Gradle sync selesai.
3. Build > Build APK(s). Hasil: app/build/outputs/apk/debug/app-debug.apk

### Cara 2: GitHub (tanpa install apa pun)
1. Buat akun GitHub, buat repository baru, upload seluruh isi folder ini
   (termasuk folder .github).
2. Buka tab Actions > workflow "Build APK" > tunggu hijau.
3. Buka run tersebut, download artifact "AutoReboot-apk" (isinya app-debug.apk).

## B. Pasang & jadikan Device Owner

PENTING: cara ADB hanya berhasil jika HP belum punya akun (Google dll).
Paling aman: factory reset dulu, lewati login Google saat setup awal.

1. Di HP: Pengaturan > Tentang ponsel > ketuk "Nomor build" 7x
   > Opsi pengembang > aktifkan USB debugging.
2. Sambungkan HP ke komputer lewat USB (install ADB platform-tools di komputer).
3. Pasang APK:
       adb install app-debug.apk
4. Jadikan Device Owner:
       adb shell dpm set-device-owner com.example.autoreboot/.AdminReceiver
   Jika muncul "Success", berhasil.
5. Buka aplikasi "Auto Restart", atur jam + hari, aktifkan, lalu Simpan.
6. Coba tombol "Tes: restart sekarang" untuk memastikan.

## Catatan
- Setelah restart, jadwal otomatis dipasang ulang.
- Mode hemat baterai/pembatasan latar belakang dari pabrikan bisa menunda alarm.
- Untuk melepas Device Owner, factory reset HP.

# Deploy LEKSIN Backend ke Render

Repository menyediakan `Dockerfile` multi-stage berbasis Java 21 dan `render.yaml` untuk membuat Web Service Docker serta PostgreSQL. Blueprint adalah konfigurasi awal; periksa plan, region, dan biaya yang tersedia di akun Render sebelum deploy.

## 1. Persiapan

Pastikan repository GitHub memuat:

- `Dockerfile`
- `render.yaml`
- `build.gradle.kts` dan Gradle Wrapper
- `src/`

Jalankan pemeriksaan lokal sebelum push:

```bash
./gradlew clean test bootJar
```

Jangan commit file `.env`, kredensial database, SMTP password, atau JWT secret.

## 2. Deploy melalui Blueprint

1. Buka Render Dashboard, lalu pilih **New + → Blueprint**.
2. Hubungkan repository `projek-mobdev-be`.
3. Render membaca `render.yaml` dan menyiapkan Web Service `leksin-backend` serta database `leksin-db`.
4. Tinjau resource dan plan yang akan dibuat.
5. Deploy Blueprint dan pantau Build Logs serta Runtime Logs.

Dockerfile membangun aplikasi dengan JDK 21, lalu menjalankan `app.jar` menggunakan JRE 21. Service mendengarkan port dari environment variable `PORT` (default 8080).

## 3. Environment variables

Blueprint menghubungkan variabel database dari resource PostgreSQL:

| Variable | Sumber / nilai |
|---|---|
| `DB_HOST` | Host internal database Render |
| `DB_PORT` | Port database Render |
| `DB_NAME` | Nama database |
| `DB_USERNAME` | Username database |
| `DB_PASSWORD` | Password database |
| `DB_POOL_SIZE` | Opsional, default 5 |
| `JWT_SECRET` | Kunci Base64 untuk signing JWT |
| `JWT_EXPIRATION_MS` | Opsional, default 86400000 (24 jam) |
| `JPA_DDL_AUTO` | Blueprint menetapkan `update` |

Aplikasi menyusun JDBC URL dari lima variabel database tersebut. Jangan menyalin PostgreSQL connection URI mentah ke salah satu variabel di atas.

### Validasi JWT secret

Implementasi JWT mendekode `JWT_SECRET` sebagai Base64 dan membutuhkan key minimal 32 byte. Blueprint mengisi nilai awal melalui `generateValue`; pastikan nilainya benar-benar Base64 dan dapat didekode menjadi setidaknya 32 byte sebelum mengandalkan deployment. Jika perlu, buat nilai yang valid secara lokal dengan:

```bash
openssl rand -base64 32
```

Masukkan hasilnya sebagai secret `JWT_SECRET` pada Environment Web Service. Jangan mengganti secret setelah pengguna mulai login, karena token yang sudah diterbitkan akan tidak valid.

### Gmail SMTP untuk OTP

Isi variabel berikut melalui Environment Web Service:

| Variable | Nilai |
|---|---|
| `MAIL_HOST` | `smtp.gmail.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | Alamat Gmail lengkap |
| `MAIL_PASSWORD` | Google App Password |
| `MAIL_SMTP_AUTH` | `true` |
| `MAIL_SMTP_STARTTLS` | `true` |

Aktifkan 2-Step Verification pada akun Google dan buat App Password khusus. Gunakan App Password 16 karakter, bukan password login Google. Simpan nilainya hanya di Render, jangan di repository. Login child melalui OTP tidak akan berfungsi jika SMTP belum dikonfigurasi.

## 4. Health check

Render Blueprint menggunakan path:

```text
GET /actuator/health
```

Setelah deploy, periksa:

```text
https://<service-name>.onrender.com/actuator/health
```

Endpoint ini diizinkan tanpa JWT. Respons sehat seharusnya HTTP 200 dengan status `UP`.

## 5. Verifikasi deployment

Lakukan pemeriksaan berurutan:

1. Panggil endpoint health.
2. Register parent melalui `POST /api/parent/register`.
3. Login melalui `POST /api/parent/login`.
4. Gunakan Bearer token untuk membuat dan membaca child.
5. Uji request OTP dan pastikan email diterima.
6. Verifikasi OTP untuk memperoleh token child.
7. Catat sesi permainan, lalu periksa history dan progress.
8. Uji `POST /api/auth/logout`; token yang dicabut harus ditolak pada request terproteksi berikutnya.

Gunakan URL HTTPS Render sebagai base URL pada Android release build.

## 6. Database dan operasi

Konfigurasi MVP menggunakan `JPA_DDL_AUTO=update`. Ini membantu pembuatan tabel awal, tetapi tidak menggantikan database migration. Sebelum perubahan skema production yang berisiko, siapkan backup dan migrasi Flyway/Liquibase. Jangan menghapus database production untuk mengatasi masalah skema.

## 7. Troubleshooting

| Gejala | Pemeriksaan |
|---|---|
| Service gagal start | Build log, Java 21, Docker build, environment variables |
| Database connection gagal | DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, status dan region database |
| JWT error saat startup | JWT_SECRET valid Base64 dan hasil decode minimal 32 byte |
| OTP tidak masuk | SMTP host/port/auth/STARTTLS, App Password, spam folder |
| Health check gagal | Runtime log dan akses `/actuator/health` |
| Android mendapat 401 | Bearer token, expiry, token revoked, dan format header |
| Android mendapat 403 | Role token dan kepemilikan child |
| Build sukses tetapi aplikasi gagal start | Periksa Runtime Logs, terutama konfigurasi JWT dan database |

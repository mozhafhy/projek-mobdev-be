# Deploy LEKSIN Backend ke Render

Repository menyediakan Dockerfile dan `render.yaml`. File blueprint merupakan konfigurasi awal; cek ketersediaan plan dan harga di dashboard Render sebelum membuat resource.

## 1. Persiapan repository

Push backend ke GitHub. Pastikan repository memuat:

- `Dockerfile`
- `render.yaml`
- `build.gradle.kts` dan Gradle Wrapper
- `src/`

Jangan commit `.env`, password database, SMTP password, atau JWT secret.

## 2. Buat resource melalui Blueprint

1. Buka Render Dashboard.
2. Pilih **New + → Blueprint**.
3. Hubungkan repository backend.
4. Render membaca `render.yaml` dan menampilkan service `leksin-backend` serta database `leksin-db`.
5. Tinjau plan dan biaya, lalu deploy.

Jika plan database pada blueprint tidak tersedia untuk akun/region, pilih plan PostgreSQL yang tersedia di dashboard dan sesuaikan `render.yaml`.

## 3. Environment variables

Blueprint mengisi host, port, nama database, username, dan password secara terpisah serta membuat `JWT_SECRET` secara otomatis. Backend membangun JDBC URL dari komponen tersebut; jangan memasukkan Render `connectionString` mentah ke `spring.datasource.url` karena format URI PostgreSQL Render bukan JDBC URL Spring. Isi variabel SMTP berikut melalui dashboard:

| Variable | Nilai |
|---|---|
| `MAIL_HOST` | Host SMTP provider |
| `MAIL_PORT` | Umumnya `587` untuk STARTTLS |
| `MAIL_USERNAME` | Username SMTP |
| `MAIL_PASSWORD` | Password atau app password SMTP |
| `MAIL_SMTP_AUTH` | `true` |
| `MAIL_SMTP_STARTTLS` | `true` |

Child OTP tidak dapat digunakan bila SMTP belum dikonfigurasi dengan benar. Uji pengiriman email setelah deploy.

`JWT_SECRET` harus Base64 dan minimal 32 byte sebelum encoding. Jangan menggantinya setelah pengguna mulai login, karena semua token yang sudah diterbitkan akan tidak valid.

## 4. Health check

Render menggunakan:

`GET https://<service>.onrender.com/actuator/health`

Endpoint ini tidak memerlukan JWT. Respons sehat seharusnya HTTP `200` dengan status `UP`.

## 5. Uji endpoint production

1. Panggil `GET /actuator/health`.
2. Register parent melalui `POST /api/parent/register`.
3. Login melalui `POST /api/parent/login`.
4. Gunakan access token untuk membuat child.
5. Uji request OTP dan pastikan email tiba.
6. Verifikasi OTP dan gunakan token child untuk membaca progress.
7. Catat sesi permainan dan periksa history/progress.

Gunakan HTTPS URL Render sebagai base URL Android release.

## 6. Catatan database

Konfigurasi MVP menggunakan `JPA_DDL_AUTO=update`. Ini memudahkan pembuatan tabel awal, tetapi bukan pengganti migration. Sebelum perubahan skema yang berisiko, buat backup dan siapkan Flyway/Liquibase. Jangan menghapus database production untuk mengatasi masalah migrasi.

## 7. Troubleshooting

| Gejala | Periksa |
|---|---|
| Service gagal start | Build log, Java 21, Docker build, environment variables |
| Database connection gagal | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, region dan status database |
| JWT error saat startup | `JWT_SECRET` Base64 valid dan minimal 32 byte |
| OTP tidak masuk | SMTP host/port/auth/STARTTLS, spam folder, provider log |
| Health check 401 | Pastikan `/actuator/health` diizinkan oleh SecurityConfig |
| Android mendapat 401 | Bearer token, expiry, dan token role |
| Android mendapat 403 | Child ID harus dimiliki akun parent/child yang tokennya dipakai |


## Gmail SMTP

Atur environment variables pada Render Web Service:

| Variable | Value |
|---|---|
| `MAIL_HOST` | `smtp.gmail.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | Alamat Gmail lengkap |
| `MAIL_PASSWORD` | Google App Password 16 karakter |
| `MAIL_SMTP_AUTH` | `true` |
| `MAIL_SMTP_STARTTLS` | `true` |

Aktifkan 2-Step Verification pada akun Google dan buat App Password khusus. Gunakan App Password, bukan password akun Google. Simpan nilainya sebagai secret di Render, jangan di repository.

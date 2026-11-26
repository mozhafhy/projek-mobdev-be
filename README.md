# LEKSIN Backend

REST API untuk aplikasi pembelajaran LEKSIN. Backend menggunakan Kotlin, Spring Boot, Spring Security, JWT, Spring Data JPA, dan PostgreSQL.

## Stack

- Java 21
- Kotlin 2.3.21
- Spring Boot 4.1.3
- PostgreSQL
- JWT (JJWT 0.12.6)
- Gradle Wrapper 9.9.2
- Docker untuk deployment

## Menjalankan secara lokal

1. Siapkan Java 21 dan PostgreSQL.
2. Buat database `leksin`.
3. Atur environment variables berikut:

| Variable | Contoh lokal | Keterangan |
|---|---|---|
| `DB_HOST` | `localhost` | Host PostgreSQL |
| `DB_PORT` | `5432` | Port PostgreSQL |
| `DB_NAME` | `leksin` | Nama database |
| `DB_USERNAME` | `postgres` | User database |
| `DB_PASSWORD` | `your-local-password` | Password database |
| `DB_POOL_SIZE` | `5` | Opsional: maksimum koneksi pool |
| `JWT_SECRET` | Base64 key minimal 32 byte | Kunci signing JWT |
| `JWT_EXPIRATION_MS` | `86400000` | Opsional: masa berlaku JWT dalam milidetik |
| `MAIL_HOST` | `smtp-relay.brevo.com` | Brevo SMTP host |
| `MAIL_PORT` | `2525` | Port SMTP dengan STARTTLS |
| `MAIL_USERNAME` | SMTP login Brevo | Login SMTP, bukan alamat pengirim |
| `MAIL_PASSWORD` | SMTP key Brevo | SMTP key, bukan API key |
| `MAIL_FROM` | Alamat pengirim terverifikasi | Sender yang sudah diverifikasi di Brevo |
| `MAIL_SMTP_AUTH` | `true` | Aktifkan SMTP AUTH |
| `MAIL_SMTP_STARTTLS` | `true` | Aktifkan STARTTLS |

Gunakan SMTP login dan SMTP key dari halaman SMTP Brevo. Pastikan alamat `MAIL_FROM` sudah diverifikasi di Brevo. Jangan gunakan API key sebagai password SMTP.

Buat JWT secret lokal dengan:

```bash
openssl rand -base64 32
```

4. Jalankan test dan build:

```bash
./gradlew clean test bootJar
```

5. Jalankan aplikasi:

```bash
./gradlew bootRun
```

API berjalan pada `http://localhost:8080`. Health check: `GET /actuator/health`.

## Pengujian

Test memakai konfigurasi profile `test` dengan H2. Jalankan `./gradlew test`; PostgreSQL lokal tidak diperlukan untuk test tersebut.

## Dokumentasi integrasi Android

Lihat [Android API Integration](docs/ANDROID_INTEGRATION.md) untuk endpoint, payload, autentikasi, format respons, aturan otorisasi, dan catatan kompatibilitas Retrofit/OkHttp.

## Deployment

Deployment utama menggunakan Ubuntu Server VM dengan Docker Compose dan PostgreSQL. Ikuti [VM Deployment](docs/VM_DEPLOYMENT.md) untuk persiapan server, environment variables, konfigurasi container, reverse proxy, HTTPS, dan verifikasi API.

Konfigurasi deployment Render lama disimpan sebagai arsip di [archive/render](archive/render/). Konfigurasi tersebut hanya untuk referensi dan tidak digunakan pada deployment aktif.

## Catatan MVP

- OCR dan evaluasi tulisan direncanakan berjalan di perangkat Android. Backend menerima metadata hasil permainan, bukan foto tulisan.
- Logout tersedia melalui `POST /api/auth/logout`; backend mencabut JWT yang sedang digunakan.
- Belum tersedia refresh token. Setelah JWT kedaluwarsa, pengguna harus login kembali.
- Endpoint pencatatan permainan saat ini menerima `starsEarned` dan `completed` dari client. Nilai tersebut belum dapat dianggap anti-cheat. Jangan gunakan sebagai dasar reward bernilai uang atau keputusan berisiko sebelum validasi hasil dipindahkan atau diverifikasi di server.
- Skema database saat ini menggunakan Hibernate `ddl-auto=update` untuk mempercepat MVP. Gunakan migration tool seperti Flyway sebelum perubahan skema production yang lebih serius.

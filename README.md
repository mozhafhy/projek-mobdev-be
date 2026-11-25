# LEKSIN Backend

REST API untuk aplikasi pembelajaran LEKSIN. Backend menggunakan Kotlin, Spring Boot, Spring Security, JWT, Spring Data JPA, dan PostgreSQL.

## Stack

- Java 21
- Kotlin 2.3.21
- Spring Boot 4.1.3
- PostgreSQL
- JWT (JJWT 0.12.6)
- Gradle Wrapper 9.9.2

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
| `MAIL_HOST` | `smtp.gmail.com` | Gmail SMTP host |
| `MAIL_PORT` | `587` | Gmail SMTP port dengan STARTTLS |
| `MAIL_USERNAME` | alamat Gmail lengkap | Akun Gmail pengirim |
| `MAIL_PASSWORD` | Google App Password | App Password 16 karakter, bukan password akun |
| `MAIL_SMTP_AUTH` | `true` | Aktifkan SMTP AUTH |
| `MAIL_SMTP_STARTTLS` | `true` | Aktifkan STARTTLS |

Untuk Gmail SMTP:

1. Aktifkan 2-Step Verification pada akun Google.
2. Buat App Password khusus untuk LEKSIN.
3. Isi `MAIL_USERNAME` dengan alamat Gmail lengkap.
4. Isi `MAIL_PASSWORD` dengan App Password 16 karakter. Jangan gunakan password login Google biasa.
5. Jangan commit App Password ke Git. Di Render, isi nilainya melalui Environment.

Buat JWT secret lokal dengan:

```bash
openssl rand -base64 32
```

4. Jalankan:

```bash
./gradlew bootRun
```

API berjalan pada `http://localhost:8080`. Health check: `GET /actuator/health`.

## Dokumentasi integrasi Android

Lihat [Android API Integration](docs/ANDROID_INTEGRATION.md) untuk endpoint, payload, autentikasi, format respons, aturan otorisasi, dan catatan kompatibilitas Retrofit/OkHttp.

## Deployment

Lihat [Render Deployment](docs/RENDER_DEPLOYMENT.md). Repository sudah menyediakan `Dockerfile` dan `render.yaml` sebagai titik awal deployment.

## Catatan MVP

- OCR dan evaluasi tulisan direncanakan berjalan di perangkat Android. Backend menerima metadata hasil permainan, bukan foto tulisan.
- Endpoint pencatatan permainan saat ini menerima `starsEarned` dan `completed` dari client. Nilai tersebut belum dapat dianggap anti-cheat. Jangan gunakan sebagai dasar reward bernilai uang atau keputusan berisiko sebelum validasi hasil dipindahkan atau diverifikasi di server.
- Skema database saat ini menggunakan Hibernate `ddl-auto=update` untuk mempercepat MVP. Gunakan migration tool seperti Flyway sebelum perubahan skema production yang lebih serius.

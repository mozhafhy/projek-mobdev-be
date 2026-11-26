# LEKSIN Backend

REST API untuk aplikasi pembelajaran LEKSIN. Backend menggunakan Kotlin, Spring Boot, Spring Security, JWT, Spring Data JPA, dan PostgreSQL.

## Quick start: local development with Docker

Persyaratan: Git dan Docker Engine/Desktop dengan Docker Compose v2. Java dan Gradle lokal tidak diperlukan; image build menyediakan Java 21 dan menjalankan Gradle Wrapper di dalam container.

Jalankan perintah berikut dari terminal:

```bash
git clone https://github.com/mozhafhy/projek-mobdev-be.git
cd projek-mobdev-be
cp .env.example .env
openssl rand -base64 32
# Edit .env: isi JWT_SECRET dengan hasil perintah di atas.
# Isi kredensial Brevo SMTP dan MAIL_FROM dengan sender yang sudah diverifikasi.
docker compose config
docker compose up -d --build
docker compose ps
curl -fsS http://localhost:8080/actuator/health
docker compose logs -f backend
```

PowerShell menggunakan `Copy-Item .env.example .env` sebagai pengganti perintah `cp`. Jangan commit file `.env`; file tersebut berisi kredensial dan secret.

Setelah container sehat, layanan tersedia pada:

| Layanan | URL |
|---|---|
| Backend API | http://localhost:8080 |
| Health check | http://localhost:8080/actuator/health |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| OpenAPI YAML | http://localhost:8080/v3/api-docs.yaml |

Swagger UI menyediakan dokumentasi endpoint interaktif. Beberapa endpoint memerlukan JWT; gunakan tombol **Authorize** dan masukkan token dengan format `Bearer <accessToken>`.

Untuk menghentikan layanan tanpa menghapus data PostgreSQL:

```bash
docker compose down
```

Untuk melihat log backend:

```bash
docker compose logs -f backend
```

Jangan gunakan `docker compose down -v` kecuali memang ingin menghapus volume database dan seluruh data lokal.

### Android device over USB

Untuk menguji aplikasi Android pada perangkat fisik yang tersambung melalui USB dengan USB debugging aktif:

```bash
adb devices
adb reverse tcp:8080 tcp:8080
```

Gunakan base URL `http://localhost:8080/` pada build debug Android. Perintah reverse perlu dijalankan kembali setelah koneksi ADB terputus atau perangkat disambungkan ulang.

## Project stack

- Java 21
- Kotlin 2.3.21
- Spring Boot 4.1.3
- Springdoc OpenAPI 3.2.0
- PostgreSQL 16
- JWT (JJWT 0.12.6)
- Gradle Wrapper 9.9.2
- Docker Engine dan Docker Compose v2

## Local development details

### Docker architecture

`compose.yaml` menjalankan dua service:

- `backend`: image dibangun dari `Dockerfile`, memakai Java 21 runtime, dan berjalan sebagai user non-root.
- `database`: PostgreSQL 16 dengan named volume `postgres_data`.

Port backend hanya dipetakan ke `127.0.0.1:8080`. PostgreSQL tidak mempublikasikan port ke host atau internet. Backend mengakses database melalui hostname internal Docker `database:5432`.

Dockerfile menjalankan Gradle Wrapper pada build stage berbasis JDK 21, kemudian menyalin executable JAR ke image runtime berbasis JRE 21. Dengan demikian, kolaborator tidak perlu memasang Java atau Gradle pada host.

### Environment variables

Salin `.env.example` menjadi `.env`, lalu sesuaikan nilai berikut.

| Variable | Wajib | Keterangan |
|---|---:|---|
| `DB_HOST` | Tidak | Compose menetapkan hostname internal menjadi `database`. |
| `DB_PORT` | Tidak | Port PostgreSQL internal, default `5432`. |
| `DB_NAME` | Ya | Nama database, default lokal `leksin`. |
| `DB_USERNAME` | Ya | User PostgreSQL. |
| `DB_PASSWORD` | Ya | Password PostgreSQL. |
| `DB_POOL_SIZE` | Tidak | Maksimum koneksi pool, default aplikasi `5`. |
| `JWT_SECRET` | Ya | Secret Base64 minimal 32 byte sebelum encoding. Buat dengan `openssl rand -base64 32`. |
| `JWT_EXPIRATION_MS` | Tidak | Masa berlaku JWT dalam milidetik, default `86400000`. |
| `MAIL_HOST` | Ya untuk email | Host SMTP Brevo, default `smtp-relay.brevo.com`. |
| `MAIL_PORT` | Ya untuk email | Port SMTP Brevo, umumnya `2525`. |
| `MAIL_USERNAME` | Ya untuk email | SMTP login Brevo. |
| `MAIL_PASSWORD` | Ya untuk email | SMTP key Brevo, bukan API key. |
| `MAIL_FROM` | Ya | Alamat pengirim yang sudah diverifikasi di Brevo. |
| `MAIL_SMTP_AUTH` | Tidak | Aktifkan SMTP AUTH, default `true`. |
| `MAIL_SMTP_STARTTLS` | Tidak | Aktifkan STARTTLS, default `true`. |
| `JPA_DDL_AUTO` | Tidak | Strategi Hibernate, default `update` untuk MVP. |
| `JPA_SHOW_SQL` | Tidak | Aktifkan SQL logging, default `false`. |

Jika kredensial SMTP belum tersedia, aplikasi tetap dapat dibangun dan dijalankan, tetapi alur yang mengirim OTP melalui email tidak akan berfungsi dengan benar. Jangan gunakan secret production pada mesin pengembangan bersama.

### Common Docker commands

```bash
docker compose config
docker compose up -d --build
docker compose ps
docker compose logs -f backend
docker compose logs -f database
docker compose restart backend
docker compose down
```

Test otomatis memakai profile `test` dan H2. Jalankan test dalam build container bila diperlukan:

```bash
docker compose run --rm backend sh -c 'true'
```

Untuk menjalankan seluruh test Gradle, gunakan task CI/build yang memakai JDK 21; jangan mengandalkan JDK host yang berbeda versi.

## API documentation

Dokumentasi dibuat oleh `springdoc-openapi`. Spring Boot sendiri tidak menyertakan Swagger UI secara otomatis; dependensi `springdoc-openapi-starter-webmvc-ui` menambahkan generator OpenAPI dan antarmuka Swagger UI.

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- OpenAPI YAML: `/v3/api-docs.yaml`

Dokumentasi dapat diakses tanpa JWT agar kolaborator mudah mengeksplorasi kontrak API. Endpoint bisnis tetap mengikuti autentikasi JWT dan otorisasi role. Untuk production, pertimbangkan membatasi akses dokumentasi melalui konfigurasi keamanan atau reverse proxy jika spesifikasi API tidak boleh tersedia untuk publik.

## Testing

Test memakai konfigurasi profile `test` dengan H2. PostgreSQL lokal tidak diperlukan untuk test tersebut. Jalankan test melalui Gradle Wrapper di lingkungan JDK 21, atau tambahkan test stage pada pipeline Docker/CI sebelum image dirilis.

## Android API integration

Lihat [Android API Integration](docs/ANDROID_INTEGRATION.md) untuk endpoint, payload, autentikasi, format respons, aturan otorisasi, dan catatan kompatibilitas Retrofit/OkHttp.

## Remote deployment

Deployment aktif menggunakan Ubuntu VM, Docker Compose, Nginx, Cloudflare DNS/proxy, dan HTTPS Let's Encrypt. Ikuti [Remote VM Deployment](docs/VM_DEPLOYMENT.md) untuk daftar perintah setup, persiapan domain, konfigurasi Cloudflare, VM, firewall, TLS, pembaruan, dan verifikasi.

Sebelum deployment, jalankan environment checker dari VM:

```bash
chmod +x scripts/check-remote-env.sh
./scripts/check-remote-env.sh api.example.com
```

Ganti domain contoh dengan domain API yang akan digunakan. Konfigurasi deployment Render lama disimpan sebagai arsip di `archive/render/` dan tidak menjadi jalur deployment aktif.

## MVP notes

- OCR dan evaluasi tulisan direncanakan berjalan di perangkat Android. Backend menerima metadata hasil permainan, bukan foto tulisan.
- Logout tersedia melalui `POST /api/auth/logout`; backend mencabut JWT yang sedang digunakan.
- Belum tersedia refresh token. Setelah JWT kedaluwarsa, pengguna harus login kembali.
- Endpoint pencatatan permainan saat ini menerima `starsEarned` dan `completed` dari client. Nilai tersebut belum dapat dianggap anti-cheat. Jangan gunakan sebagai dasar reward bernilai uang atau keputusan berisiko sebelum validasi hasil dipindahkan atau diverifikasi di server.
- Skema database saat ini menggunakan Hibernate `ddl-auto=update` untuk mempercepat MVP. Gunakan migration tool seperti Flyway sebelum perubahan skema production yang lebih serius.

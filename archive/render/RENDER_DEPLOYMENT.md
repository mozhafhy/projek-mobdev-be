# Deploy LEKSIN Backend ke Render

> Arsip historis. Deployment aktif LEKSIN menggunakan Ubuntu VM dengan Docker Compose. Panduan ini dipertahankan untuk referensi dan bukan instruksi deployment utama.

Repository lama menggunakan `Dockerfile` dan Render Blueprint untuk membuat Web Service Docker serta PostgreSQL.

## 1. Persiapan

Pastikan repository memuat `Dockerfile`, `render.yaml`, Gradle Wrapper, dan source code. Jalankan:

```bash
./gradlew clean test bootJar
```

Jangan commit file `.env`, kredensial database, SMTP password, atau JWT secret.

## 2. Deploy melalui Blueprint

1. Buka Render Dashboard, lalu pilih **New + → Blueprint**.
2. Hubungkan repository.
3. Render membaca `render.yaml` dan menyiapkan Web Service serta PostgreSQL.
4. Tinjau resource, plan, region, dan biaya.
5. Deploy Blueprint dan pantau Build Logs serta Runtime Logs.

## 3. Environment variables

Blueprint lama menghubungkan `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, dan `DB_PASSWORD` dari resource PostgreSQL. Variabel lain mencakup `JWT_SECRET`, `JPA_DDL_AUTO`, serta konfigurasi SMTP.

Implementasi JWT membutuhkan `JWT_SECRET` Base64 yang dapat didekode menjadi key minimal 32 byte. Buat dengan:

```bash
openssl rand -base64 32
```

Konfigurasi SMTP lama harus disesuaikan dengan provider email yang digunakan saat itu. Jangan menganggap konfigurasi Gmail lama sebagai konfigurasi aktif LEKSIN.

## 4. Health check

```text
GET /actuator/health
```

Respons sehat seharusnya HTTP 200 dengan status `UP`.

## 5. Verifikasi deployment

1. Panggil endpoint health.
2. Uji register dan login Parent.
3. Uji pembuatan serta pembacaan Child.
4. Uji request dan verifikasi OTP.
5. Uji pencatatan game, history, dan progress.
6. Uji logout dan pastikan token yang dicabut ditolak.

## 6. Catatan database

Konfigurasi lama menggunakan Hibernate `ddl-auto=update`. Ini membantu pembuatan tabel awal, tetapi tidak menggantikan migration tool. Siapkan backup dan migrasi Flyway/Liquibase sebelum perubahan skema production yang berisiko.

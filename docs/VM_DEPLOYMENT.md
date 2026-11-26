# Deployment LEKSIN Backend pada Ubuntu VM

Panduan ini menjadi jalur deployment aktif LEKSIN. Target VM saat ini: Ubuntu 22.04, 2 vCPU, RAM 2 GB, storage 20 GB, dan public IP.

Arsitektur MVP:
- Docker Compose menjalankan Spring Boot dan PostgreSQL.
- PostgreSQL hanya dapat diakses melalui Docker network, bukan port publik.
- Backend dipetakan ke `127.0.0.1:8080` pada VM.
- Nginx pada host menjadi reverse proxy.
- Brevo menangani pengiriman email OTP.

## 1. Persiapan VM

Pasang Docker Engine dan Compose plugin mengikuti dokumentasi resmi Docker untuk Ubuntu. Periksa instalasi:

```bash
docker --version
docker compose version
```

Clone repository ke VM:

```bash
sudo mkdir -p /opt/leksin
sudo chown "$USER":"$USER" /opt/leksin
git clone <REPOSITORY_URL> /opt/leksin
cd /opt/leksin
```

Ganti `<REPOSITORY_URL>` dengan URL repository GitHub.

## 2. Environment variables

Buat file `.env` pada root repository di VM. Jangan commit file ini.

```dotenv
DB_HOST=database
DB_PORT=5432
DB_NAME=leksin
DB_USERNAME=leksin
DB_PASSWORD=CHANGE_TO_A_LONG_RANDOM_PASSWORD
DB_POOL_SIZE=3

JWT_SECRET=REPLACE_WITH_BASE64_SECRET
JWT_EXPIRATION_MS=86400000

MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=2525
MAIL_USERNAME=REPLACE_WITH_BREVO_SMTP_LOGIN
MAIL_PASSWORD=REPLACE_WITH_BREVO_SMTP_KEY
MAIL_FROM=REPLACE_WITH_VERIFIED_SENDER
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
```

Buat JWT secret:

```bash
openssl rand -base64 32
```

Gunakan SMTP login dan SMTP key dari Brevo. `MAIL_FROM` harus merupakan alamat pengirim yang sudah diverifikasi. Jangan gunakan Brevo API key sebagai password SMTP.

Batasi permission:

```bash
chmod 600 .env
```

## 3. Build dan jalankan

File `compose.yaml` menjalankan backend dan PostgreSQL. PostgreSQL tidak mempublikasikan port ke host; backend mengaksesnya melalui hostname `database`.

```bash
docker compose config
docker compose up -d --build
docker compose ps
```

Periksa log dan health check:

```bash
docker compose logs -f backend
curl http://127.0.0.1:8080/actuator/health
```

Untuk menghentikan aplikasi tanpa menghapus data:

```bash
docker compose down
```

Jangan gunakan `docker compose down -v` kecuali memang ingin menghapus volume database.

## 4. Nginx reverse proxy

Pasang Nginx pada host:

```bash
sudo apt update
sudo apt install -y nginx
```

Buat server block yang meneruskan request ke `http://127.0.0.1:8080`. Sertakan header `Host`, `X-Real-IP`, `X-Forwarded-For`, dan `X-Forwarded-Proto`. Jalankan `sudo nginx -t` sebelum reload.

Untuk production, gunakan HTTPS. Let's Encrypt memerlukan domain yang mengarah ke public IP VM. Jika domain belum tersedia, lakukan pengujian melalui SSH tunnel:

```bash
ssh -L 8080:127.0.0.1:8080 <VM_USER>@<VM_PUBLIC_IP>
```

Akses `http://localhost:8080` dari komputer lokal. Jangan mengirim kredensial atau JWT melalui HTTP publik sebagai konfigurasi production.

## 5. Firewall

Buka hanya port yang diperlukan:
- SSH 22, sebaiknya dibatasi ke IP administrator.
- HTTP 80 untuk redirect dan validasi sertifikat.
- HTTPS 443 untuk API production.

Jangan membuka PostgreSQL 5432 ke internet. Port backend 8080 juga tidak perlu dibuka karena Nginx menjadi pintu masuk publik.

## 6. Resource VM

VM memiliki RAM 2 GB. Compose membatasi backend dan PostgreSQL masing-masing 512 MB. Dockerfile memakai Serial GC dan MaxRAMPercentage 65%; pantau penggunaan RAM dan OOM setelah deployment. Sisakan memori untuk Ubuntu, Nginx, Docker daemon, dan proses sistem.

Storage 20 GB terbatas. Pantau image, log Docker, dan volume PostgreSQL. Aktifkan rotasi log yang sudah disetel pada Compose dan simpan backup database di luar VM.

## 7. Pembaruan aplikasi

```bash
git pull
docker compose up -d --build
docker compose ps
```

Periksa log dan health check setelah pembaruan. Backup database sebelum perubahan skema atau deployment berisiko.

## 8. Verifikasi MVP

1. `GET /actuator/health` menghasilkan HTTP 200.
2. Parent dapat register dan login.
3. Parent dapat membuat serta membaca profil Child.
4. Request OTP mengirim email melalui Brevo.
5. OTP valid menghasilkan JWT Child.
6. OTP yang sudah digunakan atau kedaluwarsa ditolak.
7. Game result, history, dan progress dapat dibaca sesuai otorisasi.
8. Logout mencabut token yang digunakan.

## 9. Keamanan dan operasional

- Ganti seluruh placeholder pada `.env`.
- Simpan backup PostgreSQL di luar VM; volume Docker bukan backup.
- Jangan expose PostgreSQL atau detail actuator ke publik.
- Gunakan HTTPS untuk seluruh trafik production.
- `JPA_DDL_AUTO=update` masih digunakan untuk MVP. Rencanakan Flyway sebelum evolusi skema production.
- VM tunggal merupakan single point of failure. Siapkan backup dan prosedur pemulihan.

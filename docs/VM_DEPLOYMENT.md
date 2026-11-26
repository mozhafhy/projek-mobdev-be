# Remote deployment: LEKSIN Backend on Ubuntu VM

Dokumen ini menjadi panduan deployment aktif LEKSIN. Target awal: Ubuntu Server 22.04 atau 24.04 LTS, minimal 2 vCPU, RAM 2 GB, storage 20 GB, dan public IPv4. Sesuaikan resource setelah mengukur beban nyata.

Arsitektur:

- Cloudflare mengelola DNS dan dapat menjadi proxy HTTP(S).
- Nginx pada VM menjadi reverse proxy.
- Let's Encrypt menyediakan sertifikat TLS pada origin.
- Docker Compose menjalankan Spring Boot dan PostgreSQL.
- PostgreSQL hanya dapat diakses melalui Docker network.
- Backend hanya dipetakan ke loopback VM pada `127.0.0.1:8080`.
- Brevo menangani pengiriman email OTP.

## Quick command list

Daftar ini memberi gambaran urutan kerja. Ganti semua placeholder sebelum menjalankan perintah. Perintah dijelaskan dan diberi konteks pada bagian setelah daftar ini.

### A. Dari komputer administrator

```bash
# Daftarkan domain, tambahkan ke Cloudflare, lalu ubah nameserver di registrar.
# Buat DNS A record: api -> public IPv4 VM.
ssh <VM_USER>@<VM_PUBLIC_IP>
```

### B. Di Ubuntu VM

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y ca-certificates curl git openssl nginx certbot
# Pasang Docker Engine dan Docker Compose plugin dari repository resmi Docker.
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
# Keluar dari SSH lalu login kembali agar grup docker diterapkan.
docker --version
docker compose version
sudo mkdir -p /opt/leksin
sudo chown "$USER":"$USER" /opt/leksin
git clone https://github.com/mozhafhy/projek-mobdev-be.git /opt/leksin
cd /opt/leksin
chmod +x scripts/check-remote-env.sh
./scripts/check-remote-env.sh api.example.com
cp .env.example .env
openssl rand -base64 32
# Edit .env: set DB_PASSWORD, JWT_SECRET, dan kredensial Brevo SMTP.
chmod 600 .env
docker compose config
docker compose up -d --build
docker compose ps
curl -fsS http://127.0.0.1:8080/actuator/health
sudo mkdir -p /var/www/html
sudo certbot certonly --webroot -w /var/www/html -d api.example.com
sudo cp deploy/nginx/leksin.conf.example /etc/nginx/sites-available/leksin
# Ganti api.example.com pada file Nginx dengan domain aktual.
sudo ln -s /etc/nginx/sites-available/leksin /etc/nginx/sites-enabled/leksin
sudo nginx -t
sudo systemctl reload nginx
sudo certbot renew --dry-run
curl -fsS https://api.example.com/actuator/health
```

Perintah instalasi Docker harus mengikuti instruksi resmi untuk versi Ubuntu dan arsitektur VM yang digunakan. Jangan memasang Docker dari skrip acak yang tidak diverifikasi.

## 1. Domain dan DNS Cloudflare

### 1.1 Persiapkan domain

1. Daftarkan domain melalui registrar yang dipilih.
2. Pastikan kamu memiliki akses ke panel registrar untuk mengubah nameserver.
3. Tentukan subdomain API, misalnya `api.example.com`.
4. Catat public IPv4 VM. Gunakan IPv4 statis bila penyedia VM menyediakannya.

Domain diperlukan agar API memiliki alamat stabil dan sertifikat TLS dapat diterbitkan. Jangan mengandalkan alamat IP publik sebagai alamat production aplikasi Android.

### 1.2 Tambahkan domain ke Cloudflare

1. Buat akun Cloudflare jika belum tersedia.
2. Pilih **Add a site** dan masukkan domain apex, misalnya `example.com`.
3. Pilih paket yang sesuai. Paket Free cukup untuk DNS proxy dasar.
4. Periksa hasil DNS scan. Pastikan record yang tidak relevan atau record lama tidak mengarahkan trafik ke server yang salah.
5. Cloudflare akan memberikan dua nameserver. Ganti nameserver domain di panel registrar dengan dua nameserver tersebut.
6. Tunggu sampai status domain di Cloudflare menunjukkan **Active**.

Jika domain sebelumnya memakai DNSSEC, periksa DS record di registrar sebelum mengganti nameserver. DS lama yang tidak cocok dapat membuat domain gagal di-resolve. Aktifkan kembali DNSSEC melalui Cloudflare setelah nameserver aktif dan ikuti nilai DS yang diberikan Cloudflare.

### 1.3 Buat DNS record

Pada Cloudflare DNS, tambahkan:

| Type | Name | IPv4 content | Proxy |
|---|---|---|---|
| A | `api` | Public IPv4 VM | Proxied (orange cloud) |

TTL dapat dibiarkan Auto. Pastikan tidak ada record A/AAAA lama yang mengarahkan subdomain API ke server lain. Jika VM tidak memiliki IPv6 yang dikonfigurasi, jangan membuat AAAA record fiktif.

Verifikasi dari terminal administrator:

```bash
dig +short NS example.com
dig +short A api.example.com
```

Ketika proxy Cloudflare aktif, hasil A record biasanya berupa alamat IP Cloudflare, bukan IP origin VM. Itu normal. Pastikan alamat origin pada dashboard Cloudflare benar.

Untuk proses awal penerbitan sertifikat, DNS-only (grey cloud) dapat dipakai sementara jika troubleshooting diperlukan. Setelah origin Nginx dan TLS siap, aktifkan kembali proxy.

### 1.4 Konfigurasi SSL/TLS Cloudflare

Setelah sertifikat origin Let's Encrypt terpasang:

1. Buka **SSL/TLS → Overview**.
2. Pilih mode **Full (strict)**. Jangan gunakan Flexible karena koneksi Cloudflare ke origin harus tetap HTTPS.
3. Aktifkan **Always Use HTTPS** bila ingin Cloudflare mengarahkan request HTTP ke HTTPS. Nginx juga sudah menyediakan redirect HTTP ke HTTPS.
4. Jangan menonaktifkan validasi sertifikat origin.
5. Uji akses melalui domain setelah DNS proxy aktif.

Cloudflare proxy melindungi trafik HTTP(S) yang melewatinya. SSH tetap terhubung langsung ke IP VM dan harus dilindungi firewall.

## 2. Persiapan VM

### 2.1 Spesifikasi dan akses

Target awal:

| Resource | Minimum awal |
|---|---|
| OS | Ubuntu Server 22.04/24.04 LTS 64-bit |
| CPU | 2 vCPU |
| RAM | 2 GB |
| Storage | 20 GB SSD |
| Network | Public IPv4 |
| Access | SSH dengan user non-root dan sudo |

Gunakan SSH key, bukan password login, bila penyedia VM mendukungnya. Batasi akses SSH ke IP administrator bila alamat IP tersebut stabil.

### 2.2 Update sistem dan pasang tool dasar

```bash
sudo apt update
sudo apt upgrade -y
sudo apt install -y ca-certificates curl git openssl nginx certbot
```

Ikuti panduan resmi Docker Engine untuk Ubuntu untuk menambahkan repository Docker dan memasang `docker-ce`, `docker-ce-cli`, `containerd.io`, `docker-buildx-plugin`, serta `docker-compose-plugin`. Setelah instalasi:

```bash
sudo systemctl enable --now docker
docker --version
docker compose version
sudo docker run --rm hello-world
```

Agar perintah `docker compose` pada panduan ini dapat dijalankan tanpa `sudo`, tambahkan user deployment ke grup Docker:

```bash
sudo usermod -aG docker "$USER"
```

Keluar dari SSH lalu login kembali sebelum menjalankan Docker. Keanggotaan grup Docker setara dengan hak root pada host; berikan hanya kepada administrator tepercaya. Jika kebijakan server melarang grup Docker, pertahankan penggunaan `sudo docker` dan `sudo docker compose` pada seluruh perintah berikutnya.

### 2.3 Clone repository dan jalankan checker

```bash
sudo mkdir -p /opt/leksin
sudo chown "$USER":"$USER" /opt/leksin
git clone https://github.com/mozhafhy/projek-mobdev-be.git /opt/leksin
cd /opt/leksin
chmod +x scripts/check-remote-env.sh
./scripts/check-remote-env.sh api.example.com
```

Checker memeriksa OS, arsitektur, CPU, RAM, storage, Docker, Docker Compose, akses Docker daemon, tool dasar, port layanan, firewall UFW, dan resolusi DNS domain. Hasilnya berupa PASS/WARN/FAIL; checker tidak mengubah konfigurasi server. Jangan lanjut sebelum seluruh pemeriksaan wajib berstatus PASS. Peringatan resource atau port perlu ditinjau sesuai kapasitas dan konfigurasi VM.

## 3. Deploy backend dan PostgreSQL

### 3.1 Konfigurasi environment

Buat file environment:

```bash
cp .env.example .env
openssl rand -base64 32
chmod 600 .env
```

Edit `.env` dan isi setidaknya:

```dotenv
PORT=8080
DB_HOST=database
DB_PORT=5432
DB_NAME=leksin
DB_USERNAME=leksin
DB_PASSWORD=GANTI_DENGAN_PASSWORD_RANDOM_PANJANG
DB_POOL_SIZE=3

JPA_DDL_AUTO=update
JPA_SHOW_SQL=false

MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=2525
MAIL_USERNAME=BREVO_SMTP_LOGIN
MAIL_PASSWORD=BREVO_SMTP_KEY
MAIL_FROM=ALAMAT_PENGIRIM_TERVERIFIKASI
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

JWT_SECRET=HASIL_OPENSSL_RAND_BASE64_32
JWT_EXPIRATION_MS=86400000
```

Gunakan SMTP login dan SMTP key dari halaman SMTP Brevo. `MAIL_FROM` harus berupa sender yang sudah diverifikasi. Jangan menggunakan Brevo API key sebagai password SMTP. Gunakan secret yang berbeda dari development dan jangan pernah commit `.env`.

### 3.2 Build dan jalankan container

`compose.yaml` menjalankan backend dan PostgreSQL. Backend mengakses database melalui hostname Docker `database`; port PostgreSQL tidak dipublikasikan. Backend dipetakan ke loopback host agar hanya Nginx pada VM yang menjadi pintu masuk publik.

```bash
docker compose config
docker compose up -d --build
docker compose ps
docker compose logs -f backend
curl -fsS http://127.0.0.1:8080/actuator/health
```

Docker build menjalankan automated tests melalui Gradle Wrapper sebelum membuat executable JAR. Health check harus mengembalikan status UP. Jika gagal, periksa log backend dan database:

```bash
docker compose logs --tail=200 backend
docker compose logs --tail=200 database
```

Jangan expose port 5432 atau 8080 ke internet. Jangan menghapus volume PostgreSQL ketika memperbarui container.

## 4. Nginx reverse proxy dan HTTPS

### 4.1 Konfigurasi Nginx

Pastikan DNS domain sudah mengarah ke VM dan port 80 dapat dijangkau dari internet. Buat webroot untuk validasi Let's Encrypt:

```bash
sudo mkdir -p /var/www/html
sudo cp deploy/nginx/leksin.conf.example /etc/nginx/sites-available/leksin
```

Edit file `/etc/nginx/sites-available/leksin` dan ganti seluruh `api.example.com` dengan domain aktual. Template memiliki dua server block: HTTP untuk redirect dan ACME challenge, serta HTTPS untuk meneruskan request ke `127.0.0.1:8080`.

### 4.2 Terbitkan sertifikat

Jalankan Certbot webroot setelah DNS dan port 80 siap:

```bash
sudo certbot certonly --webroot -w /var/www/html -d api.example.com
```

Jika validasi gagal, periksa resolusi DNS, proxy Cloudflare, firewall provider, UFW, dan apakah Nginx dapat menyajikan path `/.well-known/acme-challenge/`. Cloudflare proxy dapat tetap aktif untuk HTTP-01 selama request challenge diteruskan dengan benar; gunakan DNS-only sementara untuk mengisolasi masalah.

### 4.3 Aktifkan site dan uji TLS

```bash
sudo ln -s /etc/nginx/sites-available/leksin /etc/nginx/sites-enabled/leksin
sudo nginx -t
sudo systemctl reload nginx
sudo certbot renew --dry-run
curl -fsS https://api.example.com/actuator/health
```

Jika symlink sudah ada, jangan membuat symlink duplikat. Pastikan timer Certbot aktif agar sertifikat diperpanjang otomatis.

### 4.4 Cloudflare SSL mode

Setelah origin melayani HTTPS dengan sertifikat valid, set Cloudflare SSL/TLS mode menjadi **Full (strict)**. Kemudian aktifkan proxy DNS record jika sebelumnya menggunakan DNS-only. Uji domain dari jaringan di luar VM untuk memastikan Cloudflare, Nginx, dan backend tersambung.

## 5. Firewall dan akses jaringan

Buka port berikut pada firewall provider dan UFW:

- TCP 22: SSH, batasi ke IP administrator bila memungkinkan.
- TCP 80: HTTP redirect dan Let's Encrypt HTTP-01.
- TCP 443: HTTPS API.

Jangan membuka TCP 5432 (PostgreSQL) atau TCP 8080 (backend) ke internet. Port backend hanya terikat pada `127.0.0.1`.

Contoh UFW jika SSH memakai port 22:

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
sudo ufw status verbose
```

Pastikan aturan firewall provider juga mengizinkan koneksi yang sama. Jangan mengaktifkan UFW sebelum memastikan akses SSH diizinkan, atau kamu berisiko mengunci diri dari VM.

## 6. Resource, storage, dan backup

VM 2 GB RAM adalah konfigurasi awal yang ketat. Compose membatasi backend dan PostgreSQL masing-masing 512 MB. Dockerfile menggunakan Serial GC dan MaxRAMPercentage 65%; pantau penggunaan RAM dan OOM setelah deployment. Sisakan memori untuk Ubuntu, Nginx, Docker daemon, dan proses sistem.

Storage 20 GB juga terbatas. Pantau image, log Docker, dan volume PostgreSQL. Compose membatasi ukuran file log container, tetapi itu tidak menggantikan backup database.

Buat backup PostgreSQL secara berkala dan simpan di luar VM. Contoh dump manual:

```bash
docker compose exec -T database sh -c 'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' > leksin-backup.sql
```

File backup berisi data aplikasi. Batasi permission dan enkripsi sebelum dipindahkan ke storage eksternal. Uji prosedur restore secara berkala.

## 7. Pembaruan aplikasi

Sebelum pembaruan yang berisiko, buat backup database. Kemudian:

```bash
cd /opt/leksin
git pull --ff-only
docker compose config
docker compose up -d --build
docker compose ps
docker compose logs --tail=200 backend
curl -fsS http://127.0.0.1:8080/actuator/health
```

Perintah `docker compose down` menghentikan container tanpa menghapus volume. Hindari `docker compose down -v` pada server yang menyimpan data penting.

## 8. URL layanan

Setelah deployment berhasil:

| Layanan | URL |
|---|---|
| Backend API | `https://api.example.com` |
| Health check | `https://api.example.com/actuator/health` |
| Swagger UI | `https://api.example.com/swagger-ui.html` |
| OpenAPI JSON | `https://api.example.com/v3/api-docs` |
| OpenAPI YAML | `https://api.example.com/v3/api-docs.yaml` |

Ganti domain contoh dengan domain aktual. Swagger UI dan OpenAPI JSON/YAML saat ini dapat diakses tanpa JWT; batasi melalui security policy atau reverse proxy bila dokumentasi tidak boleh tersedia untuk publik.

## 9. Verifikasi MVP

1. `GET /actuator/health` menghasilkan HTTP 200.
2. Parent dapat register dan login.
3. Parent dapat membuat serta membaca profil Child.
4. Request OTP mengirim email melalui Brevo.
5. OTP valid menghasilkan JWT Child.
6. OTP yang sudah digunakan atau kedaluwarsa ditolak.
7. Game result, history, dan progress dapat dibaca sesuai otorisasi.
8. Logout mencabut token yang digunakan.
9. Swagger UI dapat memuat OpenAPI specification.

## 10. Keamanan dan operasional

- Ganti seluruh placeholder pada `.env` dan konfigurasi Nginx.
- Simpan backup PostgreSQL di luar VM; volume Docker bukan backup.
- Jangan expose PostgreSQL atau detail actuator ke publik.
- Gunakan HTTPS untuk seluruh trafik production.
- Simpan SSH key dan secret di tempat aman; jangan commit secret ke Git.
- Pertimbangkan membatasi akses Swagger UI/OpenAPI pada production.
- `JPA_DDL_AUTO=update` masih digunakan untuk MVP. Rencanakan Flyway sebelum evolusi skema production.
- VM tunggal merupakan single point of failure. Siapkan backup dan prosedur pemulihan.

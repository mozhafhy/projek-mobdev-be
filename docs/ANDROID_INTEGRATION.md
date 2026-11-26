# LEKSIN Android API Integration

Dokumen ini menjadi kontrak integrasi sementara antara aplikasi Android Kotlin/Jetpack Compose dan backend Spring Boot.

## 1. Konfigurasi koneksi

| Lingkungan | Base URL |
|---|---|
| Android Emulator ke backend lokal | `http://10.0.2.2:8080/` |
| Perangkat fisik via USB (ADB reverse) | `http://localhost:8080/` |
| Perangkat fisik melalui Wi-Fi/LAN | `http://<LAN-IP-komputer>:8080/` |
| Production (Ubuntu VM + Nginx + Cloudflare) | `https://api.example.com/` |

Gunakan base URL dengan trailing slash untuk Retrofit. Untuk production, ganti `api.example.com` dengan domain API yang benar-benar dikonfigurasi pada Cloudflare dan Nginx mengikuti [panduan remote VM deployment](VM_DEPLOYMENT.md). Repository ini menggunakan target deployment Ubuntu VM; URL Render tidak lagi menjadi alamat production pada panduan ini.

Android native tidak terkena aturan browser CORS. Untuk pengujian HTTP lokal, Android perlu mengizinkan cleartext traffic pada build debug. Jangan aktifkan cleartext secara global pada build release.

### Perangkat fisik melalui USB

Jalankan backend lokal melalui Docker Compose di komputer pengembangan. Aktifkan **Developer options** dan **USB debugging** pada perangkat Android, sambungkan melalui USB, lalu jalankan:

```bash
adb devices
adb reverse tcp:8080 tcp:8080
```

Atur base URL Android menjadi `http://localhost:8080/`. ADB meneruskan koneksi port 8080 pada perangkat ke port 8080 komputer. Ulangi perintah `adb reverse` setelah koneksi ADB terputus atau perangkat tersambung ulang.

Alternatifnya, gunakan alamat IP LAN komputer jika perangkat dan komputer berada pada jaringan yang sama. Pastikan firewall komputer mengizinkan koneksi ke port backend. Production wajib menggunakan HTTPS.

## 2. Format umum

- Request dan response menggunakan JSON (`Content-Type: application/json`).
- ID menggunakan UUID string.
- Timestamp `playedAt` menggunakan ISO-8601 UTC, misalnya `2026-11-25T06:30:00Z`.
- Tanggal `lastActivityDate` menggunakan `YYYY-MM-DD`.
- Field JSON menggunakan `camelCase`.
- Endpoint terproteksi wajib mengirim `Authorization: Bearer <accessToken>`.
- Token berlaku sesuai `expiresIn` dalam detik. Default backend saat ini 86.400 detik (24 jam).

## 3. Autentikasi parent

### Register

`POST /api/parent/register` (public)

Request:

```json
{
  "username": "parent01",
  "password": "password-minimal-8-karakter",
  "email": "parent@example.com"
}
```

Response `201 Created`:

```json
{
  "id": "<parent-uuid>",
  "username": "parent01",
  "email": "parent@example.com"
}
```

Aturan: username 4-50 karakter, password 8-72 karakter, email harus valid.

### Login

`POST /api/parent/login` (public)

Request:

```json
{
  "username": "parent01",
  "password": "password-minimal-8-karakter"
}
```

Response `200 OK`:

```json
{
  "userId": "<parent-uuid>",
  "username": "parent01",
  "role": "PARENT",
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

Simpan token secara aman. Pada Android, gunakan Android Keystore-backed storage atau solusi encrypted preferences. Jangan simpan password plaintext.

### Profil parent

`GET /api/parent/{username}` (authenticated, role `PARENT`)

Response `200 OK` memiliki bentuk yang sama dengan response register. Parent hanya dapat membaca profil miliknya sendiri.

### Perbarui profil parent

`PATCH /api/parent/profile` (role `PARENT`)

Kirim hanya field yang ingin diubah:

```json
{
  "username": "parent02",
  "email": "parent.new@example.com"
}
```

Kedua field bersifat opsional; `username` dibatasi 3-50 karakter dan `email` harus valid. Response `200 OK` menggunakan `ParentResponse`.

## 4. Pengelolaan child

Semua endpoint berikut membutuhkan token parent.

### Buat child

`POST /api/parent/children`

```json
{
  "nickname": "Alya"
}
```

Response `201 Created`:

```json
{
  "id": "<child-uuid>",
  "nickname": "Alya"
}
```

Nickname 2-32 karakter dan harus unik di bawah parent yang sama.

### Daftar child

`GET /api/parent/children`

Response `200 OK`:

```json
[
  {
    "id": "<child-uuid>",
    "nickname": "Alya"
  }
]
```

### Perbarui profil child

`PUT /api/parent/children/{childId}`

```json
{
  "nickname": "Alya Baru"
}
```

Response `200 OK` menggunakan `ChildResponse`. Nickname harus 2-32 karakter dan tetap unik di bawah parent yang sama.

### Hapus child

`DELETE /api/parent/children/{childId}`

Response `204 No Content`. Parent hanya dapat menghapus child miliknya.

## 5. Autentikasi child dengan OTP

### Minta OTP

`POST /api/child/auth/request-otp` (public)

```json
{
  "parentUsername": "parent01",
  "nickname": "Alya"
}
```

Response `200 OK`:

```json
{
  "message": "If the account exists, an OTP has been sent"
}
```

Respons sengaja tidak mengungkap apakah akun ditemukan. OTP dikirim ke email parent, berlaku 1 menit, dan maksimal 3 permintaan dalam 10 menit per child.

### Verifikasi OTP

`POST /api/child/auth/verify-otp` (public)

```json
{
  "parentUsername": "parent01",
  "nickname": "Alya",
  "code": "0123"
}
```

Response `200 OK` memakai struktur `AuthResponse` yang sama seperti login parent, dengan `role: "CHILD"` dan `username` berupa nickname. Kode OTP harus dikirim sebagai string agar nol di depan tidak hilang.

OTP memiliki batas 3 percobaan gagal. Setelah batas tercapai, OTP tersebut tidak dapat digunakan lagi.

## 6. Game, history, dan progress

Semua endpoint membutuhkan JWT milik child terkait atau JWT parent pemilik child.

### Catat sesi permainan

`POST /api/child/{childId}/games/sessions`

```json
{
  "level": 1,
  "durationSeconds": 95,
  "attempts": 2,
  "starsEarned": 2,
  "completed": true
}
```

Response `201 Created`:

```json
{
  "id": "<history-uuid>",
  "level": 1,
  "durationSeconds": 95,
  "attempts": 2,
  "starsEarned": 2,
  "completed": true,
  "playedAt": "2026-11-25T06:30:00Z"
}
```

Validasi: level 1-26, durationSeconds minimal 0, attempts minimal 1, starsEarned 0-3. Sesi yang tidak selesai wajib mengirim `starsEarned: 0`.

Backend menambah `totalStars` hanya sebesar peningkatan bintang terbaik untuk level yang sama. Streak dihitung berdasarkan hari kalender `Asia/Jakarta`, dan hanya sesi yang selesai memperbarui streak.

**Penting:** skor masih berasal dari client. Client yang dimodifikasi dapat mengirim nilai palsu. Integrasi MVP boleh menggunakan kontrak ini, tetapi jangan menganggap skor terverifikasi atau aman untuk reward bernilai nyata.

### Riwayat permainan

`GET /api/child/{childId}/games/history`

Response `200 OK`: array `GameHistoryResponse`, urut dari sesi terbaru ke terlama.

### Progress

`GET /api/child/{childId}/games/progress`

Response `200 OK`:

```json
{
  "childId": "<child-uuid>",
  "highestLevel": 1,
  "totalStars": 2,
  "currentStreak": 1,
  "longestStreak": 1,
  "lastActivityDate": "2026-11-25"
}
```

Jika child belum memiliki progress, backend mengembalikan nilai angka `0` dan `lastActivityDate: null`.

## 7. Logout

`POST /api/auth/logout` (authenticated, role `PARENT` atau `CHILD`)

Kirim header `Authorization: Bearer <accessToken>`. Response `204 No Content` tanpa body. Backend memasukkan ID token ke daftar revoked; token tersebut akan ditolak pada request berikutnya.

Setelah menerima `204`, hapus token dari penyimpanan lokal dan arahkan pengguna ke layar login. Jika request logout gagal karena jaringan, penghapusan token lokal tetap mengakhiri sesi pada perangkat, tetapi token di server belum tentu tercabut dan masih dapat dipakai sampai kedaluwarsa.

## 8. HTTP status dan error

| Status | Makna | Tindakan client |
|---|---|---|
| `200` | Berhasil | Proses response |
| `201` | Resource dibuat | Simpan resource/ID |
| `204` | Berhasil tanpa body | Jangan decode JSON |
| `400` | Validasi request gagal | Tampilkan kesalahan field |
| `401` | Token tidak ada/tidak valid, kredensial atau OTP salah | Login ulang atau minta OTP baru |
| `403` | Role atau kepemilikan resource tidak sesuai | Tampilkan akses ditolak |
| `404` | Parent/child tidak ditemukan | Perbarui data lokal |
| `409` | Username/nickname duplikat | Minta input lain |
| `500` | Kesalahan server | Tampilkan pesan umum dan catat request ID bila kelak tersedia |

Error menggunakan `ProblemDetail` Spring. Bentuk umum:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "One or more fields were invalid",
  "timestamp": "2026-11-25T06:30:00Z",
  "errors": {
    "username": "must not be blank"
  }
}
```

Tidak semua error memiliki properti `errors`. Client sebaiknya membaca `status`, `title`, dan `detail` secara defensif.

## 9. Model Kotlin minimum

```kotlin
data class AuthResponse(
    val userId: String,
    val username: String,
    val role: String,
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
)

data class ChildResponse(
    val id: String,
    val nickname: String,
)

data class GameSessionRequest(
    val level: Int,
    val durationSeconds: Int,
    val attempts: Int,
    val starsEarned: Int,
    val completed: Boolean,
)

data class GameProgressResponse(
    val childId: String,
    val highestLevel: Int,
    val totalStars: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActivityDate: String?,
)
```

Untuk Android, UUID direpresentasikan sebagai `String` agar parsing JSON sederhana. Jika memakai `java.util.UUID`, pastikan serializer mengubahnya menjadi string.

## 10. Retrofit interface reference

```kotlin
interface LeksinApi {
    @POST("api/parent/register")
    suspend fun registerParent(
        @Body request: ParentRegisterRequest,
    ): ParentResponse

    @POST("api/parent/login")
    suspend fun loginParent(
        @Body request: ParentLoginRequest,
    ): AuthResponse

    @GET("api/parent/children")
    suspend fun getChildren(): List<ChildResponse>

    @PATCH("api/parent/profile")
    suspend fun updateParentProfile(
        @Body request: ParentProfileUpdateRequest,
    ): ParentResponse

    @PUT("api/parent/children/{childId}")
    suspend fun updateChildProfile(
        @Path("childId") childId: String,
        @Body request: ChildProfileUpdateRequest,
    ): ChildResponse

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/parent/children")
    suspend fun createChild(
        @Body request: ChildCreateRequest,
    ): ChildResponse

    @POST("api/child/auth/request-otp")
    suspend fun requestChildOtp(
        @Body request: ChildOtpRequest,
    ): MessageResponse

    @POST("api/child/auth/verify-otp")
    suspend fun verifyChildOtp(
        @Body request: ChildOtpVerifyRequest,
    ): AuthResponse

    @POST("api/child/{childId}/games/sessions")
    suspend fun recordGameSession(
        @Path("childId") childId: String,
        @Body request: GameSessionRequest,
    ): GameHistoryResponse

    @GET("api/child/{childId}/games/history")
    suspend fun getGameHistory(
        @Path("childId") childId: String,
    ): List<GameHistoryResponse>

    @GET("api/child/{childId}/games/progress")
    suspend fun getGameProgress(
        @Path("childId") childId: String,
    ): GameProgressResponse
}
```

Tambahkan endpoint lain sesuai kebutuhan aplikasi. Pasang OkHttp interceptor untuk menambahkan Bearer token pada request terproteksi; jangan menambahkan token ke endpoint register, login, request OTP, atau verify OTP sebelum token tersedia.

## 11. Batas kontrak saat ini

- Saat JWT kedaluwarsa, client harus mengarahkan pengguna login kembali.
- Logout server-side tersedia dan mencabut JWT yang sedang dipakai. Tidak ada refresh token.
- Belum ada pagination untuk riwayat permainan.
- Belum ada idempotency key untuk pencatatan sesi. Hindari retry otomatis POST sesi setelah timeout karena dapat membuat duplikasi.
- OTP berlaku 1 menit, maksimal 3 permintaan dalam 10 menit per child, dan maksimal 3 percobaan verifikasi per OTP.
- Belum ada server-side verification atas OCR atau skor game.
- Tidak ada upload foto tulisan. OCR/Tesseract berjalan di perangkat Android sesuai rancangan MVP.

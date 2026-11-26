#!/usr/bin/env bash
set -Eeuo pipefail

DOMAIN="${1:-}"
MIN_VCPU=2
MIN_RAM_GIB=2
MIN_DISK_GIB=20

PASS_COUNT=0
WARN_COUNT=0
FAIL_COUNT=0

print_result() {
  local status="$1"
  local message="$2"

  printf '[%s] %s\n' "$status" "$message"

  case "$status" in
    PASS) PASS_COUNT=$((PASS_COUNT + 1)) ;;
    WARN) WARN_COUNT=$((WARN_COUNT + 1)) ;;
    FAIL) FAIL_COUNT=$((FAIL_COUNT + 1)) ;;
  esac
}

has_command() {
  command -v "$1" >/dev/null 2>&1
}

if [[ "$(uname -s)" != "Linux" ]]; then
  print_result FAIL "Script ini hanya mendukung Linux."
  exit 2
fi

if [[ -r /etc/os-release ]]; then
  . /etc/os-release
  if [[ "${ID:-}" == "ubuntu" ]]; then
    case "${VERSION_ID:-}" in
      22.04|24.04)
        print_result PASS "OS didukung: ${PRETTY_NAME:-Ubuntu}"
        ;;
      *)
        print_result WARN "Ubuntu ${VERSION_ID:-unknown}; target yang diuji adalah 22.04 dan 24.04 LTS."
        ;;
    esac
  else
    print_result WARN "OS terdeteksi: ${PRETTY_NAME:-unknown}; panduan deployment ditulis untuk Ubuntu."
  fi
else
  print_result WARN "Tidak dapat membaca /etc/os-release."
fi

ARCH="$(uname -m)"
case "$ARCH" in
  x86_64|aarch64)
    print_result PASS "Arsitektur didukung Docker image: $ARCH"
    ;;
  *)
    print_result FAIL "Arsitektur belum divalidasi: $ARCH"
    ;;
esac

CPU_COUNT="$(getconf _NPROCESSORS_ONLN 2>/dev/null || printf '0')"
if [[ "$CPU_COUNT" -ge "$MIN_VCPU" ]]; then
  print_result PASS "CPU: $CPU_COUNT vCPU"
else
  print_result WARN "CPU: $CPU_COUNT vCPU; target minimal awal $MIN_VCPU vCPU."
fi

if has_command free; then
  RAM_GIB="$(free -b | awk '/^Mem:/ {printf "%.1f", $2 / 1073741824}')"
  if awk -v actual="$RAM_GIB" -v minimum="$MIN_RAM_GIB" 'BEGIN {exit !(actual >= minimum)}'; then
    print_result PASS "RAM: sekitar $RAM_GIB GiB"
  else
    print_result WARN "RAM: sekitar $RAM_GIB GiB; target awal $MIN_RAM_GIB GiB."
  fi
else
  print_result WARN "Command free tidak tersedia; pemeriksaan RAM dilewati."
fi

if has_command df; then
  DISK_GIB="$(df -BG --output=size / 2>/dev/null | tail -n 1 | tr -dc '0-9')"
  DISK_GIB="${DISK_GIB:-0}"
  if [[ "$DISK_GIB" -ge "$MIN_DISK_GIB" ]]; then
    print_result PASS "Kapasitas root filesystem: $DISK_GIB GiB"
  else
    print_result WARN "Kapasitas root filesystem: $DISK_GIB GiB; target awal $MIN_DISK_GIB GiB."
  fi
fi

for command_name in git curl openssl; do
  if has_command "$command_name"; then
    print_result PASS "Command tersedia: $command_name"
  else
    print_result FAIL "Command tidak ditemukan: $command_name"
  fi
done

if has_command docker; then
  print_result PASS "Docker tersedia: $(docker --version)"
else
  print_result FAIL "Docker Engine belum terpasang."
fi

if docker compose version >/dev/null 2>&1; then
  print_result PASS "Docker Compose plugin tersedia: $(docker compose version)"
else
  print_result FAIL "Docker Compose v2 plugin tidak tersedia."
fi

if docker info >/dev/null 2>&1; then
  print_result PASS "Docker daemon dapat diakses oleh user saat ini."
else
  print_result FAIL "Docker daemon tidak dapat diakses. Periksa service Docker atau permission user."
fi

if has_command systemctl; then
  if systemctl is-active --quiet docker; then
    print_result PASS "Docker service aktif."
  else
    print_result WARN "Docker service tidak aktif."
  fi
fi

if has_command ss; then
  if ss -lnt "( sport = :22 )" | grep -q LISTEN; then
    print_result PASS "SSH listener terdeteksi pada TCP 22."
  else
    print_result WARN "Tidak ditemukan SSH listener pada TCP 22. Pastikan port SSH VM sesuai konfigurasi."
  fi

  for port in 80 443; do
    if ss -lnt "( sport = :$port )" | grep -q LISTEN; then
      print_result WARN "Port TCP $port sudah digunakan; pastikan tidak ada konflik dengan Nginx."
    else
      print_result PASS "Port TCP $port tersedia untuk Nginx."
    fi
  done
else
  print_result WARN "Command ss tidak tersedia; pemeriksaan port dilewati."
fi

if has_command ufw; then
  if [[ "$EUID" -eq 0 ]]; then
    UFW_STATUS="$(ufw status 2>/dev/null | head -n 1 || true)"
  elif sudo -n true >/dev/null 2>&1; then
    UFW_STATUS="$(sudo -n ufw status 2>/dev/null | head -n 1 || true)"
  else
    UFW_STATUS=""
  fi

  if [[ "$UFW_STATUS" == "Status: active" ]]; then
    print_result PASS "UFW aktif."
  elif [[ "$UFW_STATUS" == "Status: inactive" ]]; then
    print_result WARN "UFW tidak aktif. Periksa firewall provider sebelum deployment."
  else
    print_result WARN "Status UFW tidak dapat dibaca tanpa sudo non-interaktif."
  fi
else
  print_result WARN "UFW belum terpasang; firewall provider tetap harus dikonfigurasi."
fi

if [[ -n "$DOMAIN" ]]; then
  if has_command getent && getent ahosts "$DOMAIN" >/dev/null 2>&1; then
    print_result PASS "Domain dapat di-resolve: $DOMAIN"
  else
    print_result FAIL "Domain tidak dapat di-resolve: $DOMAIN"
  fi
else
  print_result WARN "Domain tidak diberikan; jalankan: $0 api.example.com"
fi

printf '\nRingkasan: %d PASS, %d WARN, %d FAIL\n' "$PASS_COUNT" "$WARN_COUNT" "$FAIL_COUNT"

if [[ "$FAIL_COUNT" -gt 0 ]]; then
  exit 1
fi

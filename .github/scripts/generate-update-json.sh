#!/usr/bin/env bash
set -euo pipefail

repo=""
channel=""
tag=""
version_code=""
version_name=""
apk=""
notes=""
output=""

usage() {
  cat <<'EOF'
Usage: generate-update-json.sh \
  --repo <owner/repo> \
  --channel <channel> \
  --tag <tag> \
  --version-code <int> \
  --version-name <name> \
  --apk <apk-path> \
  --notes <notes-path> \
  --output <output-path>
EOF
}

github_release_asset_name() {
  local name="$1"
  name="${name//(/.}"
  name="${name//)/}"
  printf '%s' "${name}"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo)
      repo="$2"
      shift 2
      ;;
    --channel)
      channel="$2"
      shift 2
      ;;
    --tag)
      tag="$2"
      shift 2
      ;;
    --version-code)
      version_code="$2"
      shift 2
      ;;
    --version-name)
      version_name="$2"
      shift 2
      ;;
    --apk)
      apk="$2"
      shift 2
      ;;
    --notes)
      notes="$2"
      shift 2
      ;;
    --output)
      output="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

for required in repo channel tag version_code version_name apk notes output; do
  if [[ -z "${!required}" ]]; then
    echo "Missing required argument: ${required}" >&2
    usage >&2
    exit 1
  fi
done

if [[ ! -f "${apk}" ]]; then
  echo "APK not found: ${apk}" >&2
  exit 1
fi

if [[ ! -f "${notes}" ]]; then
  echo "Release notes not found: ${notes}" >&2
  exit 1
fi

apk_name="$(github_release_asset_name "$(basename "${apk}")")"
apk_url="https://github.com/${repo}/releases/download/${tag}/${apk_name}"
sha256="$(sha256sum "${apk}" | awk '{print $1}')"
published_at="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
changelog="$(cat "${notes}")"

mkdir -p "$(dirname "${output}")"

jq -n \
  --arg repo "${repo}" \
  --arg channel "${channel}" \
  --arg tagName "${tag}" \
  --arg versionName "${version_name}" \
  --arg publishedAt "${published_at}" \
  --arg changelog "${changelog}" \
  --arg apkName "${apk_name}" \
  --arg apkUrl "${apk_url}" \
  --arg sha256 "${sha256}" \
  --argjson versionCode "${version_code}" \
  '{
    repo: $repo,
    channel: $channel,
    versionCode: $versionCode,
    versionName: $versionName,
    tagName: $tagName,
    publishedAt: $publishedAt,
    prerelease: true,
    changelog: $changelog,
    apkName: $apkName,
    apkUrl: $apkUrl,
    sha256: $sha256
  }' > "${output}"

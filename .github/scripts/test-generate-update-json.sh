#!/usr/bin/env bash
set -euo pipefail

tmp_dir="$(mktemp -d)"
apk_path="${tmp_dir}/release-4.0.0-recovery.12(390109).apk"
notes_path="${tmp_dir}/release-notes.md"
output_path="${tmp_dir}/update.json"

cleanup() {
  rm -rf "${tmp_dir}"
}
trap cleanup EXIT

printf 'fake apk' > "${apk_path}"
cat > "${notes_path}" <<'EOF'
## Changes
- smoke
EOF

bash .github/scripts/generate-update-json.sh \
  --repo "xiaoancute/TiebaLite" \
  --channel "recovery" \
  --tag "v4.0.0-recovery.12" \
  --version-code "390109" \
  --version-name "4.0.0-recovery.12" \
  --apk "${apk_path}" \
  --notes "${notes_path}" \
  --output "${output_path}"

jq -e '.repo == "xiaoancute/TiebaLite"' "${output_path}" >/dev/null
jq -e '.channel == "recovery"' "${output_path}" >/dev/null
jq -e '.versionCode == 390109' "${output_path}" >/dev/null
jq -e '.apkName == "release-4.0.0-recovery.12(390109).apk"' "${output_path}" >/dev/null
jq -e '.apkUrl | contains("/releases/download/v4.0.0-recovery.12/")' "${output_path}" >/dev/null
jq -e '.sha256 | length > 0' "${output_path}" >/dev/null

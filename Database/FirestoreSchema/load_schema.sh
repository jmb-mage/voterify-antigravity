#!/usr/bin/env bash
#
# load_schema.sh
# Deploys Voterify Firestore schema rules, composite indexes, and optional seed documents.
#
# Usage:
#   ./load_schema.sh [project-id]
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ID="${1:-voterify-dev}"

echo "==================================================="
echo "Voterify Google Firestore Schema Loader"
echo "Target Project ID: ${PROJECT_ID}"
echo "Schema Directory:  ${SCRIPT_DIR}"
echo "==================================================="

if command -v firebase >/dev/null 2>&1; then
  echo "[1/2] Deploying Firestore security rules & composite indexes..."
  (
    cd "${SCRIPT_DIR}"
    firebase deploy --only firestore:rules,firestore:indexes --project "${PROJECT_ID}"
  )
  echo "[2/2] Schema rules and indexes deployed successfully."
else
  echo "Note: 'firebase' CLI not found in PATH."
  echo "Install Firebase Tools ('npm install -g firebase-tools') and run:"
  echo "  cd \"${SCRIPT_DIR}\" && firebase deploy --only firestore:rules,firestore:indexes --project ${PROJECT_ID}"
fi

#!/usr/bin/env bash
# Cierra las PRs de dependabot que ya están obsoletas: main tiene una versión
# igual o más nueva que el propio destino de la PR (comprobado a mano contra
# gradle/libs.versions.toml en main el 2026-08-12).
#
# Requisitos: gh CLI instalado y autenticado con tu perfil PRIVADO de GitHub.
#
# Uso:
#   chmod +x close_stale_dependabot_prs.sh
#   ./close_stale_dependabot_prs.sh          # dry-run: solo muestra qué haría
#   ./close_stale_dependabot_prs.sh --apply  # cierra de verdad

set -euo pipefail

REPO="gcaguilar/biciradar"
APPLY=false
if [[ "${1:-}" == "--apply" ]]; then
  APPLY=true
fi

# PR -> motivo (versión ya presente en main)
declare -A STALE_PRS=(
  [77]="agp ya está en 9.3.1 en main (la PR apuntaba a 9.2.1)"
  [96]="androidx.lifecycle-runtime-compose ya está en 2.11.0 en main"
  [91]="androidx.core:core-ktx ya está en 1.19.0 en main"
  [87]="androidx-wear-compose ya está en 1.6.2 en main"
  [109]="androidx.wear.tiles:tiles ya está en 1.6.2 en main (la PR apuntaba a 1.6.1)"
  [108]="androidx-wear-protolayout ya está en 1.4.2 en main (la PR apuntaba a 1.4.1)"
  [103]="ktor ya está en 3.5.2 en main (la PR apuntaba a 3.5.1)"
  [107]="metro ya está en 1.4.0 en main (la PR apuntaba a 1.3.0)"
)

echo "== Verificando cuenta de gh CLI activa =="
gh auth status
echo ""
read -rp "¿Es esta tu cuenta PRIVADA de GitHub? Si no, ejecuta 'gh auth switch' y vuelve a correr el script. [y/N] " CONFIRM
if [[ "$CONFIRM" != "y" && "$CONFIRM" != "Y" ]]; then
  echo "Abortado. Cambia de cuenta con 'gh auth switch' e inténtalo de nuevo."
  exit 1
fi

echo ""
echo "Se van a cerrar ${#STALE_PRS[@]} PRs obsoletas en $REPO:"
for PR in "${!STALE_PRS[@]}"; do
  echo "  #$PR — ${STALE_PRS[$PR]}"
done
echo ""

for PR in "${!STALE_PRS[@]}"; do
  COMMENT="Cerrando: ${STALE_PRS[$PR]}. Si dependabot sigue viendo la dependencia desactualizada respecto a esta versión, abrirá una PR nueva desde la base actual."
  if [[ "$APPLY" == true ]]; then
    gh pr close "$PR" --repo "$REPO" --comment "$COMMENT" --delete-branch
    echo "#$PR cerrada."
  else
    echo "(dry-run) gh pr close $PR --repo $REPO --comment \"$COMMENT\" --delete-branch"
  fi
done

echo ""
echo "Listo. Ejecuta con --apply para cerrar de verdad."

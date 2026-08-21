#!/usr/bin/env bash
# Mergea las PRs de dependabot del repo gcaguilar/biciradar que ya se
# revisaron y son seguras (bases al día con main, patch/minor sin cambios
# de API relevantes en el diff).
#
# Excluidas a propósito de este script:
#   - #77, #96, #91, #87, #109, #108, #103, #107 -> obsoletas, usar
#     close_stale_dependabot_prs.sh en su lugar.
#   - #129 (compose beta01->beta03) y #104 (gradle-wrapper, base desactualizada
#     respecto a main) -> requieren revisión manual, no se mergean aquí.
#
# Requisitos: gh CLI instalado y autenticado con tu perfil PRIVADO de GitHub.
#
# Uso:
#   chmod +x merge_dependabot_prs.sh
#   ./merge_dependabot_prs.sh            # dry-run: solo muestra qué haría
#   ./merge_dependabot_prs.sh --apply    # mergea de verdad las que estén OK

set -euo pipefail

REPO="gcaguilar/biciradar"
APPLY=false
if [[ "${1:-}" == "--apply" ]]; then
  APPLY=true
fi

# PRs ya revisadas manualmente y con base al día con main.
SAFE_PRS=(132 128 131 130 127 105 88 86 80 74 73 72 70)

echo "== Verificando cuenta de gh CLI activa =="
gh auth status
echo ""
read -rp "¿Es esta tu cuenta PRIVADA de GitHub? Si no, ejecuta 'gh auth switch' y vuelve a correr el script. [y/N] " CONFIRM
if [[ "$CONFIRM" != "y" && "$CONFIRM" != "Y" ]]; then
  echo "Abortado. Cambia de cuenta con 'gh auth switch' e inténtalo de nuevo."
  exit 1
fi

echo ""
echo "== PRs a procesar en $REPO: ${SAFE_PRS[*]} =="
echo ""

MERGED=()
SKIPPED=()

for PR in "${SAFE_PRS[@]}"; do
  echo "---------------------------------------------"
  echo "PR #$PR"
  TITLE=$(gh pr view "$PR" --repo "$REPO" --json title --jq '.title')
  MERGEABLE=$(gh pr view "$PR" --repo "$REPO" --json mergeable --jq '.mergeable')
  MERGE_STATE=$(gh pr view "$PR" --repo "$REPO" --json mergeStateStatus --jq '.mergeStateStatus')
  echo "  Título: $TITLE"
  echo "  Mergeable: $MERGEABLE / Estado: $MERGE_STATE"

  if gh pr checks "$PR" --repo "$REPO" --watch=false 2>/tmp/checks_err.$$ ; then
    CHECKS_OK=true
  else
    CHECKS_OK=false
  fi
  rm -f /tmp/checks_err.$$

  if [[ "$MERGEABLE" == "MERGEABLE" && "$CHECKS_OK" == true ]]; then
    echo "  -> OK para mergear (checks en verde, sin conflictos)."
    if [[ "$APPLY" == true ]]; then
      # Squash: 1 commit atómico por PR, sin trailers de co-autor añadidos por nosotros.
      gh pr merge "$PR" --repo "$REPO" --squash --delete-branch
      MERGED+=("$PR: $TITLE")
    else
      echo "  (dry-run, no se mergea; ejecuta con --apply para aplicar)"
    fi
  else
    echo "  -> SE OMITE (checks fallando, pendientes o conflicto). Revisar manualmente."
    SKIPPED+=("$PR: $TITLE ($MERGE_STATE)")
  fi
done

echo ""
echo "== Resumen =="
echo "Mergeadas (${#MERGED[@]}):"
printf '  - %s\n' "${MERGED[@]:-}"
echo "Omitidas (${#SKIPPED[@]}):"
printf '  - %s\n' "${SKIPPED[@]:-}"
echo ""
echo "Pendientes de revisión manual (no incluidas en este script):"
echo "  #129 (compose beta01->beta03): correr CI completo antes de mergear."
echo "  #104 (gradle-wrapper): base desactualizada respecto a main (9.5.0);"
echo "        probablemente en conflicto. Cerrar y dejar que dependabot la"
echo "        regenere, o rebasear a mano a 9.6.1."
echo "  #77, #96, #91, #87, #109, #108, #103, #107: obsoletas, usar"
echo "        close_stale_dependabot_prs.sh."

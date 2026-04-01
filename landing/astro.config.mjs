import { defineConfig } from 'astro/config';
import react from '@astrojs/react';

const isProduction = process.env.NODE_ENV === 'production';

/** Host publicado por Coolify (inyectado en el build de Nixpacks/Docker). */
function coolifyHost() {
  const fqdn = process.env.COOLIFY_FQDN?.trim();
  if (fqdn) return fqdn;
  const raw = process.env.COOLIFY_URL?.trim();
  if (!raw) return undefined;
  try {
    return new URL(raw).host;
  } catch {
    return undefined;
  }
}

function resolveSiteAndBase() {
  const coolify = coolifyHost();
  // En Coolify las variables “solo runtime” no llegan a `astro build`; COOLIFY_* sí suele inyectarse en la fase de build.
  if (coolify) {
    return {
      site: `https://${coolify}`,
      base: '/',
    };
  }

  return {
    site: process.env.PUBLIC_SITE_URL ?? 'https://gcaguilar.github.io',
    // GitHub Pages (proyecto): /biciradar. Local: raíz.
    base:
      process.env.PUBLIC_BASE_PATH ??
      (isProduction ? '/biciradar' : '/'),
  };
}

const { site, base } = resolveSiteAndBase();

export default defineConfig({
  integrations: [react()],
  output: 'static',
  site,
  base,
});
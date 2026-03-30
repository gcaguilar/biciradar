import { defineConfig } from 'astro/config';
import react from '@astrojs/react';

const isProduction = process.env.NODE_ENV === 'production';

export default defineConfig({
  integrations: [react()],
  output: 'static',
  site: process.env.PUBLIC_SITE_URL ?? 'https://gcaguilar.github.io',
  // Local dev at root, GitHub Pages production under /biciradar.
  base: process.env.PUBLIC_BASE_PATH ?? (isProduction ? '/biciradar' : '/'),
});
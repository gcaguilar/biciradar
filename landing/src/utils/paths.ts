const ABSOLUTE_URL_PATTERN = /^[a-z][a-z\d+.-]*:/i;

export function normalizeBasePath(basePath = '/') {
  const trimmed = basePath.trim();

  if (!trimmed || trimmed === '/') {
    return '/';
  }

  const withoutEdgeSlashes = trimmed.replace(/^\/+|\/+$/g, '');
  return withoutEdgeSlashes ? `/${withoutEdgeSlashes}` : '/';
}

export function withBase(path: string, basePath: string) {
  if (!path) {
    return normalizeBasePath(basePath);
  }

  if (
    path.startsWith('#') ||
    path.startsWith('//') ||
    ABSOLUTE_URL_PATTERN.test(path)
  ) {
    return path;
  }

  const normalizedBase = normalizeBasePath(basePath);
  const normalizedPath = path.replace(/^\/+/, '');

  if (!normalizedPath) {
    return normalizedBase;
  }

  return normalizedBase === '/'
    ? `/${normalizedPath}`
    : `${normalizedBase}/${normalizedPath}`;
}

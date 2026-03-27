import { useState, useEffect } from 'react';

export default function Navbar() {
  const [isDark, setIsDark] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem('theme');
    if (saved === 'dark' || (!saved && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      setIsDark(true);
      document.documentElement.classList.add('dark');
    }
  }, []);

  const toggleTheme = () => {
    setIsDark(!isDark);
    if (isDark) {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    } else {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    }
  };

  return (
    <header className="fixed top-0 w-full z-50 bg-white/80 dark:bg-[#121212]/80 backdrop-blur-md shadow-sm dark:shadow-none dark:border-b dark:border-white/5">
      <nav className="flex justify-between items-center px-8 py-4 max-w-7xl mx-auto">
        <div className="flex items-center gap-2">
          <span className="text-2xl font-black text-blue-800 dark:text-primary tracking-tighter font-headline">
            BiciRadar
          </span>
        </div>
        <div className="hidden md:flex items-center space-x-8">
          <a className="text-blue-700 dark:text-on-surface border-b-2 border-blue-600 dark:border-primary pb-1 font-headline" href="#">
            Features
          </a>
        </div>
        <div className="flex items-center gap-4">
          <button
            onClick={toggleTheme}
            className="p-2 rounded-lg hover:bg-surface-container transition-colors"
            aria-label="Toggle theme"
          >
            {isDark ? (
              <span className="material-symbols-outlined text-on-surface">light_mode</span>
            ) : (
              <span className="material-symbols-outlined text-on-surface">dark_mode</span>
            )}
          </button>
          <button className="bg-primary text-on-primary px-6 py-2.5 rounded-xl font-bold tracking-tight active:scale-95 duration-200 transition-all hover:shadow-lg hover:shadow-primary/20">
            Download App
          </button>
        </div>
      </nav>
    </header>
  );
}
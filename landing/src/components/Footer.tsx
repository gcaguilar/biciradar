import { useState, useEffect } from 'react';

export default function Footer() {
  const [isDark, setIsDark] = useState(false);

  useEffect(() => {
    setIsDark(document.documentElement.classList.contains('dark'));
  }, []);

  const languages = ['ESPAÑOL', 'ENGLISH', 'CATALÀ', 'EUSKARA', 'GALEGO'];
  const [activeLang, setActiveLang] = useState('ESPAÑOL');

  return (
    <footer className="w-full py-8 bg-slate-50 dark:bg-[#080808] font-body text-sm border-t border-slate-200 dark:border-white/5">
      <div className="px-8 max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-6">
        <div className="flex items-center gap-6">
          <div className="flex flex-col gap-1">
            <span className="text-lg font-bold text-slate-900 dark:text-primary font-headline">BiciRadar</span>
            <span className="text-slate-500 dark:text-on-surface-variant text-xs">© 2024 BiciRadar</span>
          </div>
        </div>
        
        <div className="flex items-center gap-6">
          <div className="flex flex-wrap gap-2">
            {languages.map((lang) => (
              <button
                key={lang}
                onClick={() => setActiveLang(lang)}
                className={`px-2 py-1 text-[10px] font-bold rounded transition-colors ${
                  activeLang === lang
                    ? 'bg-secondary-container dark:bg-secondary-container text-on-secondary-container dark:text-white'
                    : 'bg-transparent text-slate-400 dark:text-on-surface-variant hover:bg-surface-container dark:hover:bg-surface-container'
                }`}
              >
                {lang}
              </button>
            ))}
          </div>
        </div>

        <div className="flex items-center gap-4">
          <a className="text-slate-500 dark:text-on-surface-variant hover:text-primary dark:hover:text-primary transition-colors hover:underline text-xs" href="#">
            Legal
          </a>
          <a className="text-slate-500 dark:text-on-surface-variant hover:text-primary dark:hover:text-primary transition-colors hover:underline text-xs" href="#">
            Contacto
          </a>
        </div>
      </div>
    </footer>
  );
}
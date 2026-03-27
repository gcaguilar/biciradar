import { useState, useEffect } from 'react';

type Language = 'ES' | 'EN' | 'CA' | 'EU' | 'GL';

const translations = {
  ES: {
    velocityEngine: 'VELOCITY NEON ENGINE',
    heroTitle: 'Tu ciudad en tu',
    radar: 'radar',
    heroSubtitle: 'Gestiona tu movilidad urbana con precisión quirúrgica. Accede a datos en tiempo real de todos los sistemas de bicicletas compartidas en una sola interfaz premium.',
    downloadApp: 'Download App',
    downloadOnThe: 'Download on the',
    appStore: 'App Store',
    getTheBeta: 'Get the Beta',
    androidApp: 'Android App',
    bicimadAvailable: 'BICIMAD DISPONIBLE',
    units: 'Unidades',
    experience: 'Experiencia',
    kinetic: 'Kinetic',
    advancedVisualization: 'Visualización Avanzada',
    realTimeMap: 'Mapa en Tiempo Real',
    mapDescription: 'Nuestra tecnología radar escanea cada estación cada 15 segundos para garantizar que nunca llegues a una base vacía.',
    zeroLatency: 'Latencia zero en actualizaciones',
    vectorMaps: 'Mapas vectoriales de alta precisión',
    notifications: 'Notificaciones Inteligentes',
    notificationsDesc: 'Alertas predictivas basadas en tu historial de rutas y disponibilidad futura.',
    favoritesByDistance: 'Favoritos por Distancia',
    favoritesDesc: 'Organizamos tus estaciones preferidas automáticamente según tu ubicación actual.',
    home: 'Casa',
    meters: 'm',
    advancedFilters: 'Filtros Avanzados',
    filtersDesc: '¿Buscas una bici eléctrica con más del 80% de batería? ¿O una estación con anclajes libres para tu llegada? Tú pones las reglas.',
    ebikesOnly: 'E-Bikes Only',
    highBattery: 'High Battery',
    openDataEngine: 'Open Data Engine',
    basedOnGBFS: 'Basado en el estándar mundial GBFS',
    gbfsDescription: 'BiciRadar se integra directamente con las API abiertas de las ciudades, garantizando transparencia y fidelidad absoluta en los datos. No usamos estimaciones; usamos la fuente oficial.',
    uptime: 'Uptime de Datos',
    refreshRate: 'Refresh Rate',
    madrid: 'Madrid',
    barcelona: 'Barcelona',
    zaragoza: 'Zaragoza',
    valencia: 'Valencia',
    bicimad: 'BiciMAD',
    bicing: 'Bicing',
    bizi: 'Bizi',
    valenbisi: 'Valenbisi',
    joinRevolution: 'Únete a la revolución cinética',
    ctaSubtitle: 'La aplicación definitiva para el ciclista urbano moderno. Descarga BiciRadar hoy y toma el control de tu ruta.',
    startFree: 'Empieza Gratis',
    watchDemo: 'Ver Vídeo Demo',
    legal: 'Legal',
    contact: 'Contacto',
    copyright: '© 2024 BiciRadar',
    features: 'Features'
  },
  EN: {
    velocityEngine: 'VELOCITY NEON ENGINE',
    heroTitle: 'Your city on your',
    radar: 'radar',
    heroSubtitle: 'Manage your urban mobility with surgical precision. Access real-time data from all shared bike systems in a single premium interface.',
    downloadApp: 'Download App',
    downloadOnThe: 'Download on the',
    appStore: 'App Store',
    getTheBeta: 'Get the Beta',
    androidApp: 'Android App',
    bicimadAvailable: 'BICIMAD AVAILABLE',
    units: 'Units',
    experience: 'Experience',
    kinetic: 'Kinetic',
    advancedVisualization: 'Advanced Visualization',
    realTimeMap: 'Real-Time Map',
    mapDescription: 'Our radar technology scans each station every 15 seconds to guarantee you never arrive at an empty base.',
    zeroLatency: 'Zero latency updates',
    vectorMaps: 'High-precision vector maps',
    notifications: 'Smart Notifications',
    notificationsDesc: 'Predictive alerts based on your route history and future availability.',
    favoritesByDistance: 'Favorites by Distance',
    favoritesDesc: 'We automatically organize your preferred stations based on your current location.',
    home: 'Home',
    meters: 'm',
    advancedFilters: 'Advanced Filters',
    filtersDesc: 'Looking for an electric bike with 80%+ battery? Or a station with free docks for your arrival? You set the rules.',
    ebikesOnly: 'E-Bikes Only',
    highBattery: 'High Battery',
    openDataEngine: 'Open Data Engine',
    basedOnGBFS: 'Based on the global GBFS standard',
    gbfsDescription: 'BiciRadar integrates directly with city open APIs, guaranteeing absolute transparency and fidelity in the data. We don\'t use estimates; we use the official source.',
    uptime: 'Data Uptime',
    refreshRate: 'Refresh Rate',
    madrid: 'Madrid',
    barcelona: 'Barcelona',
    zaragoza: 'Zaragoza',
    valencia: 'Valencia',
    bicimad: 'BiciMAD',
    bicing: 'Bicing',
    bizi: 'Bizi',
    valenbisi: 'Valenbisi',
    joinRevolution: 'Join the kinetic revolution',
    ctaSubtitle: 'The ultimate app for the modern urban cyclist. Download BiciRadar today and take control of your route.',
    startFree: 'Start Free',
    watchDemo: 'Watch Demo',
    legal: 'Legal',
    contact: 'Contact',
    copyright: '© 2024 BiciRadar',
    features: 'Features'
  },
  CA: {
    velocityEngine: 'VELOCITY NEON ENGINE',
    heroTitle: 'La teva ciutat al teu',
    radar: 'radar',
    heroSubtitle: 'Gestiona la teva mobilitat urbana amb precisió quirúrgica. Accedeix a dades en temps real de tots els sistemes de bicicletes compartides en una sola interfície premium.',
    downloadApp: 'Download App',
    downloadOnThe: 'Download on the',
    appStore: 'App Store',
    getTheBeta: 'Get the Beta',
    androidApp: 'Android App',
    bicimadAvailable: 'BICIMAD DISPONIBLE',
    units: 'Unitats',
    experience: 'Experiència',
    kinetic: 'Kinetic',
    advancedVisualization: 'Visualització Avançada',
    realTimeMap: 'Mapa en Temps Real',
    mapDescription: 'La nostra tecnologia radar escaneja cada estació cada 15 segons per garantir que mai arribis a una base buida.',
    zeroLatency: 'Latència zero en actualitzacions',
    vectorMaps: 'Mapes vectorials d\'alta precisió',
    notifications: 'Notificacions Intel·ligents',
    notificationsDesc: 'Alertes predictives basades en el teu historial de rutes i disponibilitat futura.',
    favoritesByDistance: 'Favorits per Distància',
    favoritesDesc: 'Organitzem automàticament les teves estacions preferides segons la teva ubicació actual.',
    home: 'Casa',
    meters: 'm',
    advancedFilters: 'Filtres Avançats',
    filtersDesc: 'Busques una bici elèctrica amb més del 80% de bateria? O una estació amb ancles lliures per la teva arribada? Tu poses les regles.',
    ebikesOnly: 'E-Bikes Only',
    highBattery: 'High Battery',
    openDataEngine: 'Open Data Engine',
    basedOnGBFS: 'Basat en l\'estàndard mundial GBFS',
    gbfsDescription: 'BiciRadar s\'integra directament amb les API obertes de les ciutats, garantint transparència i fidelitat absolutes en les dades. No fem estimacions; fem servir la font oficial.',
    uptime: 'Uptime de Dades',
    refreshRate: 'Refresh Rate',
    madrid: 'Madrid',
    barcelona: 'Barcelona',
    zaragoza: 'Saragossa',
    valencia: 'València',
    bicimad: 'BiciMAD',
    bicing: 'Bicing',
    bizi: 'Bizi',
    valenbisi: 'Valenbisi',
    joinRevolution: 'Uneix-te a la revolució cinètica',
    ctaSubtitle: 'L\'aplicació definitiva per al ciclista urbà modern. Descarrega BiciRadar avui i pren el control de la teva ruta.',
    startFree: 'Comença Gratis',
    watchDemo: 'Veure Demo',
    legal: 'Legal',
    contact: 'Contacte',
    copyright: '© 2024 BiciRadar',
    features: 'Features'
  },
  EU: {
    velocityEngine: 'VELOCITY NEON ENGINE',
    heroTitle: 'Zure hiria zure',
    radar: 'radar',
    heroSubtitle: 'Kudeatu zure mugikortasun urbanoa zehaztasun kirurgikoarekin. Sar ezazu denbora errealeko datuak partekatutako bizikleta sistemu guztietatik interfaze premium bakarrean.',
    downloadApp: 'Download App',
    downloadOnThe: 'Download on the',
    appStore: 'App Store',
    getTheBeta: 'Get the Beta',
    androidApp: 'Android App',
    bicimadAvailable: 'BICIMAD ESkuragarri',
    units: 'Unitate',
    experience: 'Esperientzia',
    kinetic: 'Kinetic',
    advancedVisualization: 'Ikuskara Aurreratua',
    realTimeMap: 'Mapa Denbora Realean',
    mapDescription: 'Gure radar teknologiak 15 segunroan behin eskaneatzen du estazio bakoitza ziurtatzeko inoiz oinarri hutsera iritsiko zaren.',
    zeroLatency: 'Eguneratzeetan latentzia zero',
    vectorMaps: 'Mapavektore zehatzak',
    notifications: 'Jakinarazpen Adimendun',
    notificationsDesc: 'Zure ibilbide historikoan eta etorkizuneko erabilgarritasunean oinarritutako alertak aurresaleak.',
    favoritesByDistance: 'Gustukoen Distanziaren arabera',
    favoritesDesc: 'Antolatu automatikoki zure estazio gustukoenak zure kokapen aktualaren arabera.',
    home: 'Etxea',
    meters: 'm',
    advancedFilters: 'Iragazki Aurreratuak',
    filtersDesc: 'Bizi elektrikoa bilatzen du 80% baino bateria gehiagorekin? Edo estazio bat anklelibrekin? Zuk jarri arauak.',
    ebikesOnly: 'E-Bikes Only',
    highBattery: 'High Battery',
    openDataEngine: 'Open Data Engine',
    basedOnGBFS: 'GBFS munduko estandarrean oinarritua',
    gbfsDescription: 'BiciRadar hirien API irekiekin zuzenean integratzen da, datuetan gardentasun eta fidelitate absolutua bermatuz. Ez dugu estimazioak erabiltzen; iturri ofiziala erabiltzen dugu.',
    uptime: 'Datuen Uptime',
    refreshRate: 'Refresh Rate',
    madrid: 'Madril',
    barcelona: 'Bartzelona',
    zaragoza: 'Zaragoza',
    valencia: 'Valentzia',
    bicimad: 'BiciMAD',
    bicing: 'Bicing',
    bizi: 'Bizi',
    valenbisi: 'Valenbisi',
    joinRevolution: 'Elkartuiraunaldi zinetikora',
    ctaSubtitle: '.bizikleta urbano modernorako aplikazioa definitiboa. Deskargatu BiciRadar gaur eta hartu zure ibilbidearen kontrola.',
    startFree: 'Hasi Doan',
    watchDemo: 'Ikusi Demo',
    legal: 'Legea',
    contact: 'Kontaktua',
    copyright: '© 2024 BiciRadar',
    features: 'Features'
  },
  GL: {
    velocityEngine: 'VELOCITY NEON ENGINE',
    heroTitle: 'A túa cidade no teu',
    radar: 'radar',
    heroSubtitle: 'Xestiona a túa mobilidade urbana con precisión cirúrxica. Accede a datos en tempo real de todos os sistemas de bicis compartidas nunha única interface premium.',
    downloadApp: 'Download App',
    downloadOnThe: 'Download on the',
    appStore: 'App Store',
    getTheBeta: 'Get the Beta',
    androidApp: 'Android App',
    bicimadAvailable: 'BICIMAD DISPOÑIBLE',
    units: 'Unidades',
    experience: 'Experiencia',
    kinetic: 'Kinetic',
    advancedVisualization: 'Visualización Avanzada',
    realTimeMap: 'Mapa en Tempo Real',
    mapDescription: 'A nosa tecnoloxía radar escanea cada estación cada 15 segundos para garantir que nunca chegues a unha base baleira.',
    zeroLatency: 'Latencia cero nas actualizacións',
    vectorMaps: 'Mapas vectoriais de alta precisión',
    notifications: 'Notificacións Intelixentes',
    notificationsDesc: 'Alertas preditivas baseadas no teu historial de rutas e dispoñibilidade futura.',
    favoritesByDistance: 'Favoritos por Distancia',
    favoritesDesc: 'Organizamos automaticamente as túas estacións preferidas segundo a túa ubicación actual.',
    home: 'Casa',
    meters: 'm',
    advancedFilters: 'Filtros Avanzados',
    filtersDesc: 'Buscas unha bici eléctrica con máis do 80% de batería? Ou unha estación con aniños libres para a túa chegada? Ti poñes as regras.',
    ebikesOnly: 'E-Bikes Only',
    highBattery: 'High Battery',
    openDataEngine: 'Open Data Engine',
    basedOnGBFS: 'Baseado no estándar mundial GBFS',
    gbfsDescription: 'BiciRadar intégrase directamente coas API abertas das cidades, garantindo transparencia e fidelidade absoluta nos datos. Non usamos estimacións; usamos a fonte oficial.',
    uptime: 'Uptime de Datos',
    refreshRate: 'Refresh Rate',
    madrid: 'Madrid',
    barcelona: 'Barcelona',
    zaragoza: 'Zaragoza',
    valencia: 'Valencia',
    bicimad: 'BiciMAD',
    bicing: 'Bicing',
    bizi: 'Bizi',
    valenbisi: 'Valenbisi',
    joinRevolution: 'Únete á revolución cinética',
    ctaSubtitle: 'A aplicación definitiva para o cidadán urbano moderno. Descarga BiciRadar hoxe e toma o control da túa ruta.',
    startFree: 'Empeza Gratis',
    watchDemo: 'Ver Demo',
    legal: 'Legal',
    contact: 'Contacto',
    copyright: '© 2024 BiciRadar',
    features: 'Features'
  }
};

const languageNames: Record<Language, string> = {
  ES: 'ESPAÑOL',
  EN: 'ENGLISH',
  CA: 'CATALÀ',
  EU: 'EUSKARA',
  GL: 'GALEGO'
};

export default function Landing() {
  const [isDark, setIsDark] = useState(false);
  const [lang, setLang] = useState<Language>('ES');

  useEffect(() => {
    const saved = localStorage.getItem('theme');
    if (saved === 'dark' || (!saved && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      setIsDark(true);
      document.documentElement.classList.add('dark');
    }
    
    const savedLang = localStorage.getItem('lang') as Language;
    if (savedLang && languageNames[savedLang]) {
      setLang(savedLang);
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

  const changeLanguage = (newLang: Language) => {
    setLang(newLang);
    localStorage.setItem('lang', newLang);
  };

  const t = translations[lang];

  const cities = [
    { name: t.madrid, service: t.bicimad },
    { name: t.barcelona, service: t.bicing },
    { name: t.zaragoza, service: t.bizi },
    { name: t.valencia, service: t.valenbisi },
  ];

  return (
    <div className="min-h-screen">
      <header className="fixed top-0 w-full z-50 bg-white/80 dark:bg-[#121212]/80 backdrop-blur-md shadow-sm dark:shadow-none dark:border-b dark:border-white/5">
        <nav className="flex justify-between items-center px-8 py-4 max-w-7xl mx-auto">
          <div className="flex items-center gap-2">
            <span className="text-2xl font-black text-blue-800 dark:text-primary tracking-tighter font-headline">
              BiciRadar
            </span>
          </div>
          <div className="hidden md:flex items-center space-x-8">
            <a className="text-blue-700 dark:text-on-surface border-b-2 border-blue-600 dark:border-primary pb-1 font-headline" href="#">
              {t.features}
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
              {t.downloadApp}
            </button>
          </div>
        </nav>
      </header>

      <main className="pt-20">
        <section className="relative min-h-[921px] flex items-center px-8 max-w-7xl mx-auto">
          <div className="grid lg:grid-cols-2 gap-12 items-center w-full">
            <div className="z-10">
              <div className="inline-flex items-center px-3 py-1 rounded-full bg-secondary-container text-on-secondary-container dark:text-white text-xs font-bold tracking-widest uppercase mb-6">
                {t.velocityEngine}
              </div>
              <h1 className="text-6xl lg:text-8xl font-headline font-bold text-on-surface leading-[0.9] tracking-tighter mb-8">
                {t.heroTitle} <span className="text-primary">{t.radar}</span>
              </h1>
              <p className="text-xl text-on-surface-variant max-w-lg mb-10 leading-relaxed">
                {t.heroSubtitle}
              </p>
              <div className="flex flex-wrap gap-4">
                <button className="flex items-center gap-3 bg-on-surface text-surface-container-lowest px-8 py-4 rounded-xl font-bold hover:bg-primary transition-all active:scale-95">
                  <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>ios</span>
                  <div className="text-left leading-none">
                    <p className="text-[10px] uppercase opacity-70">{t.downloadOnThe}</p>
                    <p className="text-lg">{t.appStore}</p>
                  </div>
                </button>
                <button className="flex items-center gap-3 bg-surface-container-high border-2 border-outline-variant/20 text-on-surface px-8 py-4 rounded-xl font-bold hover:bg-secondary-container dark:hover:bg-secondary transition-all active:scale-95">
                  <span className="material-symbols-outlined">android</span>
                  <div className="text-left leading-none">
                    <p className="text-[10px] uppercase opacity-70">{t.getTheBeta}</p>
                    <p className="text-lg">{t.androidApp}</p>
                  </div>
                </button>
              </div>
            </div>
            <div className="relative">
              <div className={`absolute inset-0 ${isDark ? 'bg-primary/20' : 'bg-primary/10'} rounded-full blur-[120px] -z-10`}></div>
              <div className="relative flex justify-center lg:justify-end">
                <img 
                  alt="BiciRadar App" 
                  className={`w-full max-w-[500px] object-contain drop-shadow-2xl ${isDark ? 'brightness-90 grayscale-[0.2]' : ''}`}
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuDZ7AhtxbTmQQlAEICBi1RcN8wjpc9bWynvTTGpTyDPSxjBkF8kBo6JJ0xxcEWyzsAdF7EeO8_OHbFHaQ2Kehq690ONWxhlsW1mDssUlH74cpY8_S4I3pOCnKglYSYF8Cz-8Mr0IaEhFhuTWxLsF1ZYSWgOxBjD9IiKSzx_NnYMnNjpm41RyhsOJbKawwEW4LKNeWevYuIXT9dvI0IayWE3cBY_EbX4-88A5PpU65hNqlG-iRq3eN2edn8A2cxksnxo80wu7yZaC0DV"
                />
                <div className={`absolute top-1/4 -left-10 bg-white/90 dark:bg-surface-container/90 backdrop-blur-xl p-4 rounded-2xl shadow-xl hidden md:block border border-outline-variant/10 dark:border-white/10`}>
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-secondary-container flex items-center justify-center">
                      <span className="material-symbols-outlined text-secondary dark:text-white" style={{ fontVariationSettings: "'FILL' 1" }}>directions_bike</span>
                    </div>
                    <div>
                      <p className="text-xs font-bold text-on-surface-variant">{t.bicimadAvailable}</p>
                      <p className="text-lg font-black font-headline text-on-surface">12 {t.units}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="py-24 px-8 max-w-7xl mx-auto">
          <div className="mb-16">
            <h2 className="text-4xl lg:text-5xl font-headline font-bold tracking-tight mb-4">
              {t.experience} <span className="text-primary italic">{t.kinetic}</span>
            </h2>
            <div className="h-1.5 w-24 bg-secondary-container dark:bg-accent rounded-full"></div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-12 gap-8">
            <div className="md:col-span-8 bg-surface-container dark:bg-surface-container rounded-3xl overflow-hidden p-12 flex flex-col md:flex-row items-center gap-8 group dark:border dark:border-white/5">
              <div className="flex-1">
                <span className="text-primary font-bold tracking-widest text-xs uppercase mb-4 block">{t.advancedVisualization}</span>
                <h3 className="text-3xl font-headline font-bold mb-6">{t.realTimeMap}</h3>
                <p className="text-on-surface-variant mb-8">{t.mapDescription}</p>
                <ul className="space-y-3 mb-8">
                  <li className="flex items-center gap-2 font-semibold">
                    <span className="material-symbols-outlined text-secondary dark:text-accent">check_circle</span> 
                    {t.zeroLatency}
                  </li>
                  <li className="flex items-center gap-2 font-semibold">
                    <span className="material-symbols-outlined text-secondary dark:text-accent">check_circle</span> 
                    {t.vectorMaps}
                  </li>
                </ul>
              </div>
              <div className="flex-1 transform group-hover:scale-105 transition-transform duration-500">
                <img 
                  alt="Real-time map" 
                  className={`rounded-2xl shadow-2xl ${isDark ? 'brightness-75' : ''}`}
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuBegatPdPmVWr6VsTNfs6258zy4n09XiXdO8BSPMFIltGqS-eSU-NmLy60H4ZBwjCrlxbnQ30iBCtybC2CMjaLC82EB9a9w5QDNQLADbCtbzgIKB5VRF6jKkDl2tCRF8OzI_HfgVVVzbb3qJakVtWWGEJJSLkuNgG4e_REN4auWh5qA08jeT11Zu_whp45h9ronRAdDlEUe6zDLyrcYqQ3Tjc4_iSeJARogiTrTxqQvweTp_l-zoQXd11Wvlq7BgfOuOHCnx98CqE8P"
                />
              </div>
            </div>

            <div className="md:col-span-4 bg-surface-container-high dark:bg-surface-container-high rounded-3xl p-8 flex flex-col justify-between group dark:border dark:border-white/5">
              <div className="mb-8">
                <div className="w-14 h-14 bg-white dark:bg-background rounded-2xl flex items-center justify-center mb-6 shadow-sm dark:border dark:border-white/10">
                  <span className="material-symbols-outlined text-primary text-3xl">notifications_active</span>
                </div>
                <h3 className="text-2xl font-headline font-bold mb-4">{t.notifications}</h3>
                <p className="text-on-surface-variant">{t.notificationsDesc}</p>
              </div>
              <img 
                alt="Notifications" 
                className={`rounded-xl w-full object-cover h-40 group-hover:opacity-90 transition-opacity ${isDark ? 'brightness-75' : ''}`}
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuCuz0qfVRqzYOOEpa2RGA9TcI5JWaGomrnHftQRfT_phAg96z3QhuNxBwWXBFQggv-KmVxKJJ3EZf67sPr26YhocMg_sO4hhH2uQR_AORvnZhcCt-0f5omH4YpHxQTYcnJbVZrlvfArcbz7Kr0yz19a5SUpnpOfQ8L03Z8Gd1N4HimjII07-qYVbNaonBgXe4tj-OuyAN0Q6NYJrbjx2LUEiGHoxk3OnFmHNikcT9QqDYICQpCuT0O-k6-lS7NXwnjTxaakK03R-wnQ"
              />
            </div>

            <div className="md:col-span-4 bg-primary text-on-primary rounded-3xl p-8 flex flex-col justify-between group relative overflow-hidden">
              <div className="absolute top-0 right-0 p-8 opacity-10 dark:opacity-20">
                <span className="material-symbols-outlined text-9xl">favorite</span>
              </div>
              <div className="relative z-10">
                <h3 className="text-2xl font-headline font-bold mb-4">{t.favoritesByDistance}</h3>
                <p className="opacity-80">{t.favoritesDesc}</p>
              </div>
              <div className="mt-12 bg-white/10 p-4 rounded-xl backdrop-blur-md border border-white/20">
                <div className="flex justify-between items-center mb-2">
                  <span className="font-bold">{t.home}</span>
                  <span className="text-secondary-container dark:text-accent font-black">200{t.meters}</span>
                </div>
                <div className="h-1.5 w-full bg-white/20 rounded-full">
                  <div className={`h-full w-4/5 bg-secondary-container dark:bg-accent rounded-full ${isDark ? 'shadow-[0_0_10px_rgba(127,255,0,0.5)]' : ''}`}></div>
                </div>
              </div>
            </div>

            <div className="md:col-span-8 bg-surface-container-low dark:bg-surface-container-low rounded-3xl p-12 flex flex-col md:flex-row gap-12 items-center group dark:border dark:border-white/5">
              <div className="flex-1 order-2 md:order-1">
                <img 
                  alt="Advanced Filters" 
                  className={`rounded-2xl shadow-xl transform group-hover:rotate-2 transition-transform ${isDark ? 'brightness-90 grayscale-[0.2]' : ''}`}
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuCXXyNdx345trBP-lVa1tIOJPv5TVvFUXy_LU_CDodgwfmOjQMdlN0agPli1X9hPbvar4gbCfG2XYJdBuPb_DFX0Sr-qKYVCJnTaSz-6VsZL69XSeOnL842u3dAZktnv-SoN5mZZ02JyiPxjCGYTO9BDxCYzlYBwnRrfdmiRa-CczdTn_T0NHZNgYz_vvS_qppaiCMFR1sHYZwQsNv9UUflF15A-N7RQkDIhWg-eZgteFpf1VnKiSmwc1NMYo9-TT70lSgYSK7sYuGU"
                />
              </div>
              <div className="flex-1 order-1 md:order-2">
                <h3 className="text-3xl font-headline font-bold mb-6">{t.advancedFilters}</h3>
                <p className="text-on-surface-variant mb-6">{t.filtersDesc}</p>
                <div className="flex flex-wrap gap-2">
                  <span className="px-4 py-2 bg-secondary-container text-on-secondary-container dark:bg-secondary dark:text-white rounded-full text-xs font-bold uppercase tracking-widest">{t.ebikesOnly}</span>
                  <span className="px-4 py-2 bg-surface-container-highest text-on-surface dark:bg-surface-container-highest dark:text-on-surface rounded-full text-xs font-bold uppercase tracking-widest">{t.highBattery}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="py-24 bg-surface text-on-surface relative overflow-hidden">
          <div className="max-w-7xl mx-auto px-8 relative z-10">
            <div className="grid lg:grid-cols-2 gap-16 items-center">
              <div>
                <div className="inline-flex items-center gap-2 text-primary font-bold mb-6">
                  <span className="material-symbols-outlined">dataset</span>
                  <span className="uppercase tracking-widest text-sm">{t.openDataEngine}</span>
                </div>
                <h2 className="text-4xl lg:text-6xl font-headline font-bold mb-8 leading-tight">
                  {t.basedOnGBFS}
                </h2>
                <p className="text-lg text-on-surface-variant leading-relaxed mb-10">
                  {t.gbfsDescription}
                </p>
                <div className="grid grid-cols-2 gap-6">
                  <div className="p-6 bg-white dark:bg-surface-container rounded-2xl shadow-sm dark:border dark:border-white/5">
                    <p className="text-3xl font-headline font-black text-primary mb-1">99.9%</p>
                    <p className="text-sm font-bold opacity-60 uppercase tracking-tighter">{t.uptime}</p>
                  </div>
                  <div className="p-6 bg-white dark:bg-surface-container rounded-2xl shadow-sm dark:border dark:border-white/5">
                    <p className="text-3xl font-headline font-black text-secondary dark:text-accent mb-1">15s</p>
                    <p className="text-sm font-bold opacity-60 uppercase tracking-tighter">{t.refreshRate}</p>
                  </div>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                {cities.map((city, i) => (
                  <div 
                    key={city.name}
                    className={`bg-surface-container-lowest dark:bg-surface-container-lowest p-8 rounded-3xl hover:bg-secondary-container dark:hover:bg-primary transition-all duration-300 group border dark:border-white/5 ${i % 2 === 1 ? 'mt-8' : ''} ${isDark ? 'hover:border-primary/50' : ''}`}
                  >
                    <h4 className="text-2xl font-headline font-bold mb-2">{city.name}</h4>
                    <p className="text-on-surface-variant group-hover:text-on-secondary-container dark:group-hover:text-white font-bold mb-4">{city.service}</p>
                    <span className={`material-symbols-outlined text-4xl opacity-20 group-hover:opacity-100 transition-opacity ${isDark ? 'text-accent' : ''}`}>location_on</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
          <div className={`absolute top-0 right-0 -mr-20 w-1/2 h-full opacity-5 dark:opacity-[0.03] pointer-events-none`}>
            <img 
              alt="Map background" 
              className={`w-full h-full object-cover ${isDark ? 'invert' : ''}`}
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuBrZhR050XMfen-kCIHA0mgiuYfUehHIRI0Sb2duYm0BMmFDWsqV3BGznT74qwAKhFpSR_SiQW-L4s24EakURHhCNbKp2roG3F92vHfVmrtoYAHfbKJJd5e8OlfVagWJR0p7TVl7uQbkXbNRBWWd7q2QT58T6x8wWb1OVtzqT4AuvEl14bS_5gYQ1J-uPIdV6b8lJW4M8XiZIHQapsRlF3tJE5kdtdkbp5YwyAHJXyvwlwbw0ZMwE0XQK0E_io0TE9MnJhyP1sS9nX4"
            />
          </div>
        </section>

        <section className="py-24 px-8 max-w-7xl mx-auto text-center">
          <div className="bg-inverse-surface text-surface-container-lowest dark:bg-[#0a011a] dark:text-on-surface rounded-[3rem] p-16 relative overflow-hidden dark:border dark:border-white/5">
            <div className="relative z-10">
              <h2 className="text-5xl lg:text-7xl font-headline font-bold mb-8">{t.joinRevolution}</h2>
              <p className="text-xl opacity-70 mb-12 max-w-2xl mx-auto">{t.ctaSubtitle}</p>
              <div className="flex flex-wrap justify-center gap-6">
                <button className="bg-primary text-on-primary px-10 py-5 rounded-2xl font-bold text-lg hover:bg-primary-container dark:hover:brightness-110 transition-all dark:hover:scale-105">{t.startFree}</button>
                <button className="bg-white/10 dark:bg-white/5 backdrop-blur-md border border-white/20 dark:border-white/20 px-10 py-5 rounded-2xl font-bold text-lg hover:bg-white/20 dark:hover:bg-white/10 transition-all">{t.watchDemo}</button>
              </div>
            </div>
            <div className="absolute top-0 left-0 w-64 h-64 bg-primary/20 dark:bg-primary/20 blur-[100px]"></div>
            <div className="absolute bottom-0 right-0 w-96 h-96 bg-secondary/10 dark:bg-secondary/10 blur-[120px]"></div>
          </div>
        </section>
      </main>

      <footer className="w-full py-8 bg-slate-50 dark:bg-[#080808] font-body text-sm border-t border-slate-200 dark:border-white/5">
        <div className="px-8 max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-6">
          <div className="flex items-center gap-6">
            <div className="flex flex-col gap-1">
              <span className="text-lg font-bold text-slate-900 dark:text-primary font-headline">BiciRadar</span>
              <span className="text-slate-500 dark:text-on-surface-variant text-xs">{t.copyright}</span>
            </div>
          </div>
          
          <div className="flex items-center gap-6">
            <div className="flex flex-wrap gap-2">
              {(Object.keys(languageNames) as Language[]).map((l) => (
                <button
                  key={l}
                  onClick={() => changeLanguage(l)}
                  className={`px-2 py-1 text-[10px] font-bold rounded transition-colors ${
                    lang === l
                      ? 'bg-secondary-container dark:bg-secondary-container text-on-secondary-container dark:text-white'
                      : 'bg-transparent text-slate-400 dark:text-on-surface-variant hover:bg-surface-container dark:hover:bg-surface-container'
                  }`}
                >
                  {languageNames[l]}
                </button>
              ))}
            </div>
          </div>

          <div className="flex items-center gap-4">
            <a className="text-slate-500 dark:text-on-surface-variant hover:text-primary dark:hover:text-primary transition-colors hover:underline text-xs" href="#">
              {t.legal}
            </a>
            <a className="text-slate-500 dark:text-on-surface-variant hover:text-primary dark:hover:text-primary transition-colors hover:underline text-xs" href="#">
              {t.contact}
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
}
import { useState, useEffect } from 'react';

type Language = 'ES' | 'EN' | 'CA' | 'EU' | 'GL';

declare global {
  interface Window {
    umami?: {
      track: (eventName: string, eventData?: Record<string, string>) => void;
    };
  }
}

const translations = {
  ES: {
    heroTitle: 'Tu ciudad en tu',
    radar: 'radar',
    heroSubtitle: 'Gestiona tu movilidad urbana con precisión quirúrgica. Accede a datos en tiempo real de todos los sistemas de bicicletas compartidas en una sola interfaz premium.',
    downloadApp: 'Descargar app',
    downloadOnThe: 'Download on the',
    appStore: 'App Store',
    joinTesters: 'Paso 1: Unete a testers',
    downloadAndroidApp: 'Paso 2: Descarga Android',
    bicimadAvailable: 'BICIMAD DISPONIBLE',
    units: 'Unidades',
    experience: 'Funciones principales',
    feature1Title: 'Disponibilidad en tiempo real',
    feature1Desc: 'Consulta cuantas bicis y huecos hay en cada estacion antes de llegar.',
    feature1Highlight1: 'Consulta antes de llegar',
    feature1Highlight2: 'Datos en tiempo real',
    feature2Title: 'Estaciones favoritas',
    feature2Desc: 'Guarda tus estaciones favoritas y consulta su disponibilidad rápidamente.',
    feature3Title: 'Avisos cuando cambie',
    feature3Desc: 'Monitoriza una estación y recibe avisos cuando haya bicis o huecos disponibles.',
    feature3Label: 'Casa',
    feature3Distance: 'm',
    feature4Title: 'Widgets y reloj',
    feature4Desc: 'Consulta estaciones desde widgets o tu reloj sin abrir la app.',
    feature4Tag1: 'Widgets',
    feature4Tag2: 'Wear OS',
    feature4Tag3: 'Apple Watch',
    openDataEngine: 'Open Data Engine',
    basedOnGBFS: 'Basado en el estándar mundial GBFS',
    gbfsDescription: 'BiciRadar se integra directamente con las API abiertas de las ciudades, garantizando transparencia y fidelidad absoluta en los datos. No usamos estimaciones; usamos la fuente oficial.',
    uptime: 'Uptime de Datos',
    refreshRate: 'Refresh Rate',
    madrid: 'Madrid',
    barcelona: 'Barcelona',
    zaragoza: 'Zaragoza',
    valencia: 'Valencia',
    sevilla: 'Sevilla',
    bicimad: 'BiciMAD',
    bicing: 'Bicing',
    bizi: 'Bizi',
    valenbisi: 'Valenbisi',
    manyMoreCities: 'Y muchas más',
    legal: 'Legal',
    contact: 'Contacto',
    cities: 'Ciudades',
    copyright: 'BiciRadar',
    features: 'Funciones',
    faqs: 'FAQs',
    faqTitle: 'Preguntas frecuentes',
    faqSubtitle: 'Respuestas claras para saber cuándo usar BiciRadar como complemento a la app oficial.'
  },
  EN: {
    heroTitle: 'Your city on your',
    radar: 'radar',
    heroSubtitle: 'Manage your urban mobility with surgical precision. Access real-time data from all shared bike systems in a single premium interface.',
    downloadApp: 'Download App',
    downloadOnThe: 'Download on the',
    appStore: 'App Store',
    joinTesters: 'Step 1: Join testers',
    downloadAndroidApp: 'Step 2: Download Android',
    bicimadAvailable: 'BICIMAD AVAILABLE',
    units: 'Units',
    experience: 'Main features',
    feature1Title: 'Real-time availability',
    feature1Desc: 'Check how many bikes and docks are available at each station before you arrive.',
    feature1Highlight1: 'Check before arriving',
    feature1Highlight2: 'Real-time data',
    feature2Title: 'Favorite Stations',
    feature2Desc: 'Save your favorite stations and quickly check their availability.',
    feature3Title: 'Availability alerts',
    feature3Desc: 'Monitor a station and get alerts when bikes or docks become available.',
    feature3Label: 'Home',
    feature3Distance: 'm',
    feature4Title: 'Widgets & Watch',
    feature4Desc: 'Check stations from widgets or your watch without opening the app.',
    feature4Tag1: 'Widgets',
    feature4Tag2: 'Wear OS',
    feature4Tag3: 'Apple Watch',
    openDataEngine: 'Open Data Engine',
    basedOnGBFS: 'Based on the global GBFS standard',
    gbfsDescription: 'BiciRadar integrates directly with city open APIs, guaranteeing absolute transparency and fidelity in the data. We don\'t use estimates; we use the official source.',
    uptime: 'Data Uptime',
    refreshRate: 'Refresh Rate',
    madrid: 'Madrid',
    barcelona: 'Barcelona',
    zaragoza: 'Zaragoza',
    valencia: 'Valencia',
    sevilla: 'Seville',
    bicimad: 'BiciMAD',
    bicing: 'Bicing',
    bizi: 'Bizi',
    valenbisi: 'Valenbisi',
    manyMoreCities: 'And many more',
    legal: 'Legal',
    contact: 'Contact',
    cities: 'Cities',
    copyright: 'BiciRadar',
    features: 'Features',
    faqs: 'FAQs',
    faqTitle: 'Frequently asked questions',
    faqSubtitle: 'Clear answers about when BiciRadar works best as a complement to the official app.'
  },
  CA: {
    heroTitle: 'La teva ciutat al teu',
    radar: 'radar',
    heroSubtitle: 'Gestiona la teva mobilitat urbana amb precisió quirúrgica. Accedeix a dades en temps real de tots els sistemes de bicicletes compartides en una sola interfície premium.',
    downloadApp: 'Download App',
    downloadOnThe: 'Download on the',
    appStore: 'App Store',
    joinTesters: 'Pas 1: Uneix-te a testers',
    downloadAndroidApp: 'Pas 2: Descarrega Android',
    bicimadAvailable: 'BICIMAD DISPONIBLE',
    units: 'Unitats',
    experience: 'Funcions principals',
    feature1Title: 'Disponibilitat en temps real',
    feature1Desc: 'Consulta quantes bicis i ancoratges hi ha a cada estacio abans d\'arribar.',
    feature1Highlight1: 'Consulta abans d arribar',
    feature1Highlight2: 'Dades en temps real',
    feature2Title: 'Estacions Preferides',
    feature2Desc: 'Guarda les teves estacions preferides i consulta la seva disponibilitat ràpidament.',
    feature3Title: 'Avisos quan canvii',
    feature3Desc: 'Monitoritza una estació i rep avisos quan hi hagi bicis o buits disponibles.',
    feature3Label: 'Casa',
    feature3Distance: 'm',
    feature4Title: 'Widgets i Rellotge',
    feature4Desc: 'Consulta estacions des de widgets o el teu rellotge sense obrir l\'app.',
    feature4Tag1: 'Widgets',
    feature4Tag2: 'Wear OS',
    feature4Tag3: 'Apple Watch',
    openDataEngine: 'Open Data Engine',
    basedOnGBFS: 'Basat en l\'estàndard mundial GBFS',
    gbfsDescription: 'BiciRadar s\'integra directament amb les API obertes de les ciutats, garantint transparència i fidelitat absolutes en les dades. No fem estimacions; fem servir la font oficial.',
    uptime: 'Uptime de Dades',
    refreshRate: 'Refresh Rate',
    madrid: 'Madrid',
    barcelona: 'Barcelona',
    zaragoza: 'Saragossa',
    valencia: 'València',
    sevilla: 'Sevilla',
    bicimad: 'BiciMAD',
    bicing: 'Bicing',
    bizi: 'Bizi',
    valenbisi: 'Valenbisi',
    manyMoreCities: 'I moltes mes',
    legal: 'Legal',
    contact: 'Contacte',
    cities: 'Ciutats',
    copyright: 'BiciRadar',
    features: 'Funcions',
    faqs: 'FAQs',
    faqTitle: 'Preguntes frequents',
    faqSubtitle: "Respostes clares sobre quan BiciRadar funciona millor com a complement de l'app oficial."
  },
  EU: {
    heroTitle: 'Zure hiria zure',
    radar: 'radar',
    heroSubtitle: 'Kudeatu zure mugikortasun urbanoa zehaztasun kirurgikoarekin. Sar ezazu denbora errealeko datuak partekatutako bizikleta sistemu guztietatik interfaze premium bakarrean.',
    downloadApp: 'Download App',
    downloadOnThe: 'Download on the',
    appStore: 'App Store',
    joinTesters: '1. urratsa: Testerren taldean sartu',
    downloadAndroidApp: '2. urratsa: Deskargatu Android',
    bicimadAvailable: 'BICIMAD ESkuragarri',
    units: 'Unitate',
    experience: 'Ezaugarri nagusiak',
    feature1Title: 'Denbora errealeko erabilgarritasuna',
    feature1Desc: 'Kontsultatu estaziora iritsi aurretik bizikleta edo leku libreak dauden.',
    feature1Highlight1: 'Iritsi aurretik kontsultatu',
    feature1Highlight2: 'Denbora errealeko datuak',
    feature2Title: 'Gogoko Estazioak',
    feature2Desc: 'Gorde zure gogoko estazioak eta kontsultatu bere eskuragarritasuna azkar.',
    feature3Title: 'Aldaketaren abisuak',
    feature3Desc: 'Monitorizatu estazio bat eta jaso abisuak bizikleta edo leku libreak daudenean.',
    feature3Label: 'Etxea',
    feature3Distance: 'm',
    feature4Title: 'Widget-ak eta Erlojua',
    feature4Desc: 'Kontsultatu estazioak widget-etatik edo zure erlojutik app-a ireki gabe.',
    feature4Tag1: 'Widget-ak',
    feature4Tag2: 'Wear OS',
    feature4Tag3: 'Apple Watch',
    openDataEngine: 'Open Data Engine',
    basedOnGBFS: 'GBFS munduko estandarrean oinarritua',
    gbfsDescription: 'BiciRadar hirien API irekiekin zuzenean integratzen da, datuetan gardentasun eta fidelitate absolutua bermatuz. Ez dugu estimazioak erabiltzen; iturri ofiziala erabiltzen dugu.',
    uptime: 'Datuen Uptime',
    refreshRate: 'Refresh Rate',
    madrid: 'Madril',
    barcelona: 'Bartzelona',
    zaragoza: 'Zaragoza',
    valencia: 'Valentzia',
    sevilla: 'Sevilla',
    bicimad: 'BiciMAD',
    bicing: 'Bicing',
    bizi: 'Bizi',
    valenbisi: 'Valenbisi',
    manyMoreCities: 'Eta askoz gehiago',
    legal: 'Legea',
    contact: 'Kontaktua',
    cities: 'Hiriak',
    copyright: 'BiciRadar',
    features: 'Ezaugarriak',
    faqs: 'FAQs',
    faqTitle: 'Ohiko galderak',
    faqSubtitle: 'Erantzun argiak BiciRadar noiz den app ofizialaren osagarri hoberena ulertzeko.'
  },
  GL: {
    heroTitle: 'A túa cidade no teu',
    radar: 'radar',
    heroSubtitle: 'Xestiona a túa mobilidade urbana con precisión cirúrxica. Accede a datos en tempo real de todos os sistemas de bicis compartidas nunha única interface premium.',
    downloadApp: 'Download App',
    downloadOnThe: 'Download on the',
    appStore: 'App Store',
    joinTesters: 'Paso 1: Unete a testers',
    downloadAndroidApp: 'Paso 2: Descarga Android',
    bicimadAvailable: 'BICIMAD DISPOÑIBLE',
    units: 'Unidades',
    experience: 'Funcións principais',
    feature1Title: 'Dispoñibilidade en tempo real',
    feature1Desc: 'Consulta se hai bicis ou ocos dispoñibles antes de chegar á estación.',
    feature1Highlight1: 'Consulta antes de chegar',
    feature1Highlight2: 'Datos en tempo real',
    feature2Title: 'Estacións Favoritas',
    feature2Desc: 'Garda as túas estacións favoritas e consulta a súa dispoñibilidade rapidamente.',
    feature3Title: 'Avisos cando cambie',
    feature3Desc: 'Monitoriza unha estación e recibe avisos cando haxa bicis ou ocos dispoñibles.',
    feature3Label: 'Casa',
    feature3Distance: 'm',
    feature4Title: 'Widgets e Reloxo',
    feature4Desc: 'Consulta estacións desde widgets ou o teu reloxo sen abrir a app.',
    feature4Tag1: 'Widgets',
    feature4Tag2: 'Wear OS',
    feature4Tag3: 'Apple Watch',
    openDataEngine: 'Open Data Engine',
    basedOnGBFS: 'Baseado no estándar mundial GBFS',
    gbfsDescription: 'BiciRadar intégrase directamente coas API abertas das cidades, garantindo transparencia e fidelidade absoluta nos datos. Non usamos estimacións; usamos a fonte oficial.',
    uptime: 'Uptime de Datos',
    refreshRate: 'Refresh Rate',
    madrid: 'Madrid',
    barcelona: 'Barcelona',
    zaragoza: 'Zaragoza',
    valencia: 'Valencia',
    sevilla: 'Sevilla',
    bicimad: 'BiciMAD',
    bicing: 'Bicing',
    bizi: 'Bizi',
    valenbisi: 'Valenbisi',
    manyMoreCities: 'E moitas mais',
    legal: 'Legal',
    contact: 'Contacto',
    cities: 'Cidades',
    copyright: 'BiciRadar',
    features: 'Funcions',
    faqs: 'FAQs',
    faqTitle: 'Preguntas frecuentes',
    faqSubtitle: 'Respostas claras para entender cando BiciRadar funciona mellor como complemento da app oficial.'
  }
};

const languageNames: Record<Language, string> = {
  ES: 'ESPAÑOL',
  EN: 'ENGLISH',
  CA: 'CATALÀ',
  EU: 'EUSKARA',
  GL: 'GALEGO'
};

const faqByLanguage: Record<Language, { question: string; answer: string }[]> = {
  ES: [
    {
      question: '¿Si ya tengo la app oficial, qué me aporta BiciRadar?',
      answer: 'Rapidez para lo que más miras al salir: estación más cercana, bicis disponibles, huecos libres, favoritos, estado y ruta.'
    },
    {
      question: '¿Me ahorra tiempo de verdad?',
      answer: 'Sí. Incluye voz y atajos en iPhone, Apple Watch y Android para resolver consultas en menos pasos, a veces sin abrir la app.'
    },
    {
      question: '¿Solo sirve en el móvil?',
      answer: 'No. Está pensado para móvil y reloj: Android, iOS, Wear OS y Apple Watch.'
    },
    {
      question: '¿Los datos son fiables?',
      answer: 'Usa fuentes oficiales de open data de las ciudades. Puede haber pequeños desfases normales en sistemas en tiempo real.'
    },
    {
      question: '¿Por qué usar BiciRadar como complemento?',
      answer: 'La app oficial es el canal institucional; BiciRadar está optimizado para consultar más rápido y con menos fricción.'
    }
  ],
  EN: [
    {
      question: 'If I already use the official app, what does BiciRadar add?',
      answer: 'Speed for what matters most: nearest station, available bikes, free docks, favorites, station status, and route.'
    },
    {
      question: 'Does it really save time?',
      answer: 'Yes. It includes voice and shortcuts on iPhone, Apple Watch, and Android to solve queries in fewer steps, sometimes without opening the app.'
    },
    {
      question: 'Is it only for mobile?',
      answer: 'No. It is designed for phone and watch: Android, iOS, Wear OS, and Apple Watch.'
    },
    {
      question: 'Is the data reliable?',
      answer: 'It uses official city open-data sources. Small delays can happen as in any real-time system.'
    },
    {
      question: 'Why use BiciRadar as a complement?',
      answer: 'The official app is the institutional channel; BiciRadar is optimized for faster, lower-friction daily checks.'
    }
  ],
  CA: [
    {
      question: 'Si ja tinc l’app oficial, què m’aporta BiciRadar?',
      answer: 'Rapidesa per al que més consultes: estació més propera, bicis disponibles, ancoratges lliures, preferides, estat i ruta.'
    },
    {
      question: 'Realment estalvia temps?',
      answer: 'Sí. Inclou veu i dreceres a iPhone, Apple Watch i Android per resoldre consultes amb menys passos.'
    },
    {
      question: 'Només serveix al mòbil?',
      answer: 'No. Està pensada per a mòbil i rellotge: Android, iOS, Wear OS i Apple Watch.'
    },
    {
      question: 'Les dades són fiables?',
      answer: 'Fa servir fonts oficials d’open data de les ciutats. Pot haver-hi petits desfasaments típics del temps real.'
    },
    {
      question: 'Per què fer servir BiciRadar com a complement?',
      answer: 'L’app oficial és el canal institucional; BiciRadar està optimitzada per consultar més ràpid i amb menys fricció.'
    }
  ],
  EU: [
    {
      question: 'App ofiziala badut, zer ematen dit BiciRadar-ek?',
      answer: 'Abiadura: gertuko estazioa, bizikletak, ainguraleku libreak, gogokoak, egoera eta ibilbidea azkar ikusteko.'
    },
    {
      question: 'Benetan denbora aurrezten du?',
      answer: 'Bai. Ahotsa eta lasterbideak ditu iPhone, Apple Watch eta Android-en, kontsultak urrats gutxiagotan egiteko.'
    },
    {
      question: 'Mugikorrerako bakarrik da?',
      answer: 'Ez. Mugikor eta erlojurako dago: Android, iOS, Wear OS eta Apple Watch.'
    },
    {
      question: 'Datuak fidagarriak dira?',
      answer: 'Hirietako open data iturri ofizialak erabiltzen ditu. Denbora errealeko sistemetan ohiko atzerapen txikiak egon daitezke.'
    },
    {
      question: 'Zergatik erabili osagarri gisa?',
      answer: 'App ofiziala kanal instituzionala da; BiciRadar eguneroko kontsulta azkar eta erosoetarako optimizatuta dago.'
    }
  ],
  GL: [
    {
      question: 'Se xa teño a app oficial, que me aporta BiciRadar?',
      answer: 'Rapidez para o importante: estación máis próxima, bicis dispoñibles, ocos libres, favoritas, estado e ruta.'
    },
    {
      question: 'Aforra tempo de verdade?',
      answer: 'Si. Inclúe voz e atallos en iPhone, Apple Watch e Android para resolver consultas con menos pasos.'
    },
    {
      question: 'Só serve no móbil?',
      answer: 'Non. Está pensada para móbil e reloxo: Android, iOS, Wear OS e Apple Watch.'
    },
    {
      question: 'Os datos son fiables?',
      answer: 'Usa fontes oficiais de open data das cidades. Pode haber pequenos desfases normais en sistemas en tempo real.'
    },
    {
      question: 'Por que usar BiciRadar como complemento?',
      answer: 'A app oficial é a canle institucional; BiciRadar está optimizada para consultar máis rápido e con menos fricción.'
    }
  ]
};

const seoPageCtaByLanguage: Record<Language, string> = {
  ES: 'Leer explicación completa',
  EN: 'Read full explanation',
  CA: 'Llegir explicació completa',
  EU: 'Irakurri azalpen osoa',
  GL: 'Ler explicación completa'
};

export default function Landing() {
  const baseUrl = import.meta.env.BASE_URL;
  const [isDark, setIsDark] = useState(false);
  const [lang, setLang] = useState<Language>('ES');
  const currentYear = new Date().getFullYear();

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
  const trackDownloadClick = (store: 'app_store' | 'play_store' | 'android_testers') => {
    window.umami?.track('download_click', { store, lang });
  };

  const cities = [
    { name: t.madrid, service: t.bicimad },
    { name: t.barcelona, service: t.bicing },
    { name: t.zaragoza, service: t.bizi },
    { name: t.valencia, service: t.valenbisi },
    { name: t.sevilla, service: 'Sevici' },
  ];
  const faqItems = faqByLanguage[lang];

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
            <a className="text-blue-700 dark:text-on-surface hover:text-primary pb-1 font-headline" href="#features">
              {t.features}
            </a>
            <a className="text-blue-700 dark:text-on-surface hover:text-primary pb-1 font-headline" href="#cities">
              {t.cities}
            </a>
            <a className="text-blue-700 dark:text-on-surface hover:text-primary pb-1 font-headline" href="#faqs">
              {t.faqs}
            </a>
            <a className="text-blue-700 dark:text-on-surface hover:text-primary pb-1 font-headline" href="#">
              {t.contact}
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
            <a href="#download" className="bg-primary text-on-primary px-6 py-2.5 rounded-xl font-bold tracking-tight active:scale-95 duration-200 transition-all hover:shadow-lg hover:shadow-primary/20">
              {t.downloadApp}
            </a>
          </div>
        </nav>
      </header>

      <main className="pt-20">
        <section className="relative min-h-[921px] flex items-center px-8 max-w-7xl mx-auto">
          <div className="grid lg:grid-cols-2 gap-12 items-center w-full">
            <div className="z-10">
              <h1 className="text-6xl lg:text-8xl font-headline font-bold text-on-surface leading-[0.9] tracking-tighter mb-8">
                {t.heroTitle} <span className="text-primary">{t.radar}</span>
              </h1>
              <p className="text-xl text-on-surface-variant max-w-lg mb-10 leading-relaxed">
                {t.heroSubtitle}
              </p>
              <div id="download" className="flex flex-wrap gap-4">
                <a
                  href="https://apps.apple.com/es/app/biciradar/id6760931316"
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={() => trackDownloadClick('app_store')}
                  className="flex items-center gap-3 bg-on-surface text-surface-container-lowest px-8 py-4 rounded-xl font-bold hover:bg-primary transition-all active:scale-95"
                >
                  <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>ios</span>
                  <div className="text-left leading-none">
                    <p className="text-[10px] uppercase opacity-70">{t.downloadOnThe}</p>
                    <p className="text-lg">{t.appStore}</p>
                  </div>
                </a>
                <a
                  href="https://groups.google.com/g/testers-biciradar"
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={() => trackDownloadClick('android_testers')}
                  className="flex items-center gap-3 bg-surface-container-high border-2 border-outline-variant/20 text-on-surface px-8 py-4 rounded-xl font-bold hover:bg-secondary-container dark:hover:bg-secondary transition-all active:scale-95"
                >
                  <span className="material-symbols-outlined">android</span>
                  <div className="text-left leading-none">
                    <p className="text-[10px] uppercase opacity-70">Android</p>
                    <p className="text-lg">{t.joinTesters}</p>
                  </div>
                </a>
                <a
                  href="https://play.google.com/store/apps/details?id=com.gcaguilar.biciradar"
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={() => trackDownloadClick('play_store')}
                  className="flex items-center gap-3 bg-surface-container-high border-2 border-outline-variant/20 text-on-surface px-8 py-4 rounded-xl font-bold hover:bg-secondary-container dark:hover:bg-secondary transition-all active:scale-95"
                >
                  <span className="material-symbols-outlined">download</span>
                  <div className="text-left leading-none">
                    <p className="text-[10px] uppercase opacity-70">Android</p>
                    <p className="text-lg">{t.downloadAndroidApp}</p>
                  </div>
                </a>
              </div>
            </div>
            <div className="relative">
              <div className={`absolute inset-0 ${isDark ? 'bg-primary/20' : 'bg-primary/10'} rounded-full blur-[120px] -z-10`}></div>
              <div className="relative flex justify-center lg:justify-end">
                <img 
                  alt="BiciRadar App" 
                  className={`w-full max-w-[500px] object-contain drop-shadow-2xl ${isDark ? 'brightness-90 grayscale-[0.2]' : ''}`}
                  src={`${baseUrl}images/hero.jpg`}
                  width={500}
                  height={1000}
                  loading="eager"
                  decoding="async"
                  fetchPriority="high"
                />
                <div className={`absolute top-1/4 -left-10 bg-white/90 dark:bg-surface-container/90 backdrop-blur-xl p-4 rounded-2xl shadow-xl hidden md:block border border-outline-variant/10 dark:border-white/10`}>
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-secondary-container flex items-center justify-center">
                      <span className="material-symbols-outlined text-secondary dark:text-white" style={{ fontVariationSettings: "'FILL' 1" }}>directions_bike</span>
                    </div>
                    <div>
                      <p className="text-xs font-bold text-slate-700 dark:text-slate-300">{t.bicimadAvailable}</p>
                      <p className="text-lg font-black font-headline text-slate-900 dark:text-white">12 {t.units}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section id="features" className="py-24 px-8 max-w-7xl mx-auto">
          <div className="mb-16">
            <h2 className="text-4xl lg:text-5xl font-headline font-bold tracking-tight mb-4">{t.experience}</h2>
            <div className="h-1.5 w-24 bg-secondary-container dark:bg-accent rounded-full"></div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-12 gap-8 md:items-start">
            <div className="md:col-span-8 bg-surface-container dark:bg-surface-container rounded-3xl overflow-hidden p-12 flex flex-col md:flex-row items-center gap-8 group dark:border dark:border-white/5">
              <div className="flex-1 min-w-0">
                <span className="text-primary font-bold tracking-widest text-xs uppercase mb-4 block">{t.feature1Title}</span>
                <h3 className="text-3xl font-headline font-bold mb-6">{t.feature1Title}</h3>
                <p className="text-on-surface-variant mb-8">{t.feature1Desc}</p>
                <ul className="space-y-3 mb-8">
                  <li className="flex items-center gap-2 font-semibold">
                    <span className="material-symbols-outlined text-secondary dark:text-accent">check_circle</span> 
                    {t.feature1Highlight1}
                  </li>
                  <li className="flex items-center gap-2 font-semibold">
                    <span className="material-symbols-outlined text-secondary dark:text-accent">check_circle</span> 
                    {t.feature1Highlight2}
                  </li>
                </ul>
              </div>
              <div className="flex-1 min-w-0 w-full transform group-hover:scale-105 transition-transform duration-500">
                <img 
                  alt="Consulta disponibilidad" 
                  className={`w-full max-h-[min(36rem,70vh)] object-contain rounded-2xl shadow-2xl ${isDark ? 'brightness-75' : ''}`}
                  src={`${baseUrl}images/feature-availability.jpg`}
                  width={1080}
                  height={2340}
                  loading="lazy"
                  decoding="async"
                />
              </div>
            </div>

            <div className="md:col-span-4 bg-surface-container-high dark:bg-surface-container-high rounded-3xl p-8 flex flex-col justify-between group self-stretch dark:border dark:border-white/5">
              <div className="mb-8">
                <div className="w-14 h-14 bg-white dark:bg-background rounded-2xl flex items-center justify-center mb-6 shadow-sm dark:border dark:border-white/10">
                  <span className="material-symbols-outlined text-primary text-3xl">star</span>
                </div>
                <h3 className="text-2xl font-headline font-bold mb-4">{t.feature2Title}</h3>
                <p className="text-on-surface-variant">{t.feature2Desc}</p>
              </div>
              <img 
                alt="Estaciones favoritas" 
                className={`rounded-xl w-full object-cover h-40 group-hover:opacity-90 transition-opacity ${isDark ? 'brightness-75' : ''}`}
                src={`${baseUrl}images/feature-favorites.jpg`}
                width={1080}
                height={720}
                loading="lazy"
                decoding="async"
              />
            </div>

            <div className="md:col-span-4 bg-primary text-on-primary rounded-3xl p-8 flex flex-col justify-between group relative overflow-hidden self-start">
              <div className="absolute top-0 right-0 p-8 opacity-10 dark:opacity-20">
                <span className="material-symbols-outlined text-9xl">notifications_active</span>
              </div>
              <div className="relative z-10">
                <h3 className="text-2xl font-headline font-bold mb-4">{t.feature3Title}</h3>
                <p className="opacity-80">{t.feature3Desc}</p>
              </div>
              <div className="mt-12 bg-black/50 p-4 rounded-xl backdrop-blur-md border border-white/20">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-white font-bold">{t.feature3Label}</span>
                  <span className="text-white font-black">200{t.feature3Distance}</span>
                </div>
                <div className="h-1.5 w-full bg-white/20 rounded-full">
                  <div className={`h-full w-4/5 bg-secondary-container dark:bg-accent rounded-full ${isDark ? 'shadow-[0_0_10px_rgba(127,255,0,0.5)]' : ''}`}></div>
                </div>
              </div>
            </div>

            <div className="md:col-span-8 bg-surface-container-low dark:bg-surface-container-low rounded-3xl p-12 flex flex-col md:flex-row gap-12 items-center group dark:border dark:border-white/5">
              <div className="flex-1 min-w-0 w-full order-2 md:order-1">
                <img 
                  alt="Widgets y reloj" 
                  className={`w-full max-h-[min(36rem,70vh)] object-contain rounded-2xl shadow-xl transform group-hover:rotate-2 transition-transform ${isDark ? 'brightness-90 grayscale-[0.2]' : ''}`}
                  src={`${baseUrl}images/feature-widgets.jpg`}
                  width={1080}
                  height={2340}
                  loading="lazy"
                  decoding="async"
                />
              </div>
              <div className="flex-1 min-w-0 order-1 md:order-2">
                <h3 className="text-3xl font-headline font-bold mb-6">{t.feature4Title}</h3>
                <p className="text-on-surface-variant mb-6">{t.feature4Desc}</p>
                <div className="flex flex-wrap gap-2">
                  <span className="px-4 py-2 bg-secondary-container text-on-secondary-container dark:bg-secondary dark:text-white rounded-full text-xs font-bold uppercase tracking-widest">{t.feature4Tag1}</span>
                  <span className="px-4 py-2 bg-surface-container-highest text-on-surface dark:bg-surface-container-highest dark:text-on-surface rounded-full text-xs font-bold uppercase tracking-widest">{t.feature4Tag2}</span>
                  <span className="px-4 py-2 bg-surface-container-highest text-on-surface dark:bg-surface-container-highest dark:text-on-surface rounded-full text-xs font-bold uppercase tracking-widest">{t.feature4Tag3}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section id="cities" className="py-24 bg-surface text-on-surface relative overflow-hidden">
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
                    <p className="text-3xl font-headline font-black text-primary mb-1">15s</p>
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
                    <h3 className="text-2xl font-headline font-bold mb-2">{city.name}</h3>
                    <p className="text-on-surface-variant group-hover:text-on-secondary-container dark:group-hover:text-white font-bold mb-4">{city.service}</p>
                    <span className={`material-symbols-outlined text-4xl opacity-20 group-hover:opacity-100 transition-opacity ${isDark ? 'text-accent' : ''}`}>location_on</span>
                  </div>
                ))}
                <div className="bg-surface-container-lowest dark:bg-surface-container-lowest p-8 rounded-3xl border dark:border-white/5">
                    <h3 className="text-2xl font-headline font-bold mb-2">{t.manyMoreCities}</h3>
                  <p className="text-on-surface-variant font-bold mb-4">GBFS</p>
                  <span className="material-symbols-outlined text-4xl opacity-30">public</span>
                </div>
              </div>
            </div>
          </div>
          <div className={`absolute top-0 right-0 -mr-20 w-1/2 h-full opacity-5 dark:opacity-[0.03] pointer-events-none`}>
            <img 
              alt="Map background" 
              className={`w-full h-full object-cover ${isDark ? 'invert' : ''}`}
              src={`${baseUrl}images/map-bg.jpg`}
              width={1920}
              height={1080}
              loading="lazy"
              decoding="async"
            />
          </div>
        </section>

        <section id="faqs" className="py-24 px-8 max-w-7xl mx-auto">
          <div className="mb-12">
            <h2 className="text-4xl lg:text-5xl font-headline font-bold tracking-tight mb-4">{t.faqTitle}</h2>
            <p className="text-on-surface-variant max-w-3xl">{t.faqSubtitle}</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {faqItems.map((item) => (
              <article key={item.question} className="bg-surface-container-low dark:bg-surface-container rounded-3xl p-8 border border-outline-variant/20 dark:border-white/10">
                <h3 className="text-xl font-headline font-bold mb-3">{item.question}</h3>
                <p className="text-on-surface-variant leading-relaxed">{item.answer}</p>
              </article>
            ))}
          </div>
          <div className="mt-10">
            <a
              href={`${baseUrl}biciradar-complemento-app-oficial`}
              className="inline-flex items-center gap-2 bg-primary text-on-primary px-6 py-3 rounded-xl font-bold hover:shadow-lg hover:shadow-primary/20 transition-all"
            >
              {seoPageCtaByLanguage[lang]}
              <span className="material-symbols-outlined">arrow_forward</span>
            </a>
          </div>
        </section>

      </main>

      <footer id="contact" className="w-full py-8 bg-slate-50 dark:bg-[#080808] font-body text-sm border-t border-slate-200 dark:border-white/5">
        <div className="px-8 max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-6">
          <div className="flex items-center gap-6">
            <div className="flex flex-col gap-1">
              <span className="text-lg font-bold text-slate-900 dark:text-primary font-headline">BiciRadar</span>
              <span className="text-slate-500 dark:text-on-surface-variant text-xs">© {currentYear} {t.copyright}</span>
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
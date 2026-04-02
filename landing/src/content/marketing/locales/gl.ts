import type { LocaleContent } from '../types';

const operatingSystems = [
  { value: 'ios', label: 'iPhone / iOS' },
  { value: 'android', label: 'Android' },
  { value: 'both', label: 'Uso ambos' },
];

const frequencies = [
  { value: 'daily', label: 'Todos os días' },
  { value: 'weekly', label: 'Varias veces por semana' },
  { value: 'occasional', label: 'De vez en cando' },
];

const interests = [
  { value: 'bikes', label: 'Atopar bicis' },
  { value: 'docks', label: 'Atopar ocos' },
  { value: 'alerts', label: 'Recibir avisos' },
  { value: 'widgets', label: 'Usar widgets e accesos rápidos' },
];

const yesNo = [
  { value: 'yes', label: 'Si' },
  { value: 'no', label: 'Non' },
];

export const gl = {
  locale: 'gl',
  languageName: 'Galego',
  localeLabel: 'GL',
  metadata: {
    siteName: 'BiciRadar',
    siteTagline: 'Consulta bicis e ocos antes de chegar',
    defaultOgTitle: 'BiciRadar | Consulta bicis e ocos en tempo real',
    defaultOgDescription:
      'Mira dispoñibilidade en sistemas de bici compartida, garda favoritas e solicita acceso á beta de BiciRadar.',
  },
  common: {
    skipToContent: 'Saltar ao contido',
    appStoreLabel: 'Descargar na App Store',
    androidBetaLabel: 'Solicitar acceso beta',
    openMenu: 'Abrir navegación',
    closeMenu: 'Pechar navegación',
    backToHome: 'Volver ao inicio',
    viewCities: 'Ver cidades',
    cityPageCta: 'Ver páxina local',
    heroCta: 'Entrar na beta',
    heroSecondaryCta: 'Ver como funciona',
    finalCtaTitle: 'Consulta antes de pedalear',
    finalCtaDescription:
      'Solicita acceso á beta e axúdanos a priorizar novas cidades con demanda real.',
    cityRevisitLabel: 'Volver ver cidades',
    shareDescription: 'Comparte a beta con outra persoa da túa cidade.',
    betaInviteLabel: 'Acceso por invitación pública',
    faqLabel: 'Preguntas frecuentes',
    thankYouShareFallback: 'Ligazón copiada. Xa a podes compartir.',
    githubAriaLabel: 'Código fonte en GitHub',
  },
  home: {
    seo: {
      title: 'BiciRadar beta | Consulta bicis e ocos en tempo real antes de chegar',
      description:
        'BiciRadar axúdache a ver dispoñibilidade en BiciMAD, Bicing, Sevici, Valenbisi, Bizi e máis. Garda favoritas, recibe avisos e entra na beta.',
    },
    header: {
      sections: [
        { id: 'problema', label: 'Problema' },
        { id: 'solucion', label: 'Solución' },
        { id: 'como-funciona', label: 'Como funciona' },
        { id: 'cidades', label: 'Cidades' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Entrar na beta',
      languageLabel: 'Idioma',
    },
    hero: {
      title: 'Consulta bicis e ocos en tempo real antes de chegar',
      description:
        'BiciRadar axúdache a ver dispoñibilidade en sistemas de bici compartida como BiciMAD, Bicing, Sevici e máis. Garda favoritas, recibe avisos e accede rápido desde widgets.',
      primaryCta: 'Descargar na App Store',
      secondaryCta: 'Solicitar acceso beta',
      microcopy: 'Solicita acceso e usaremos o teu email para xestionar a túa entrada na beta de Android.',
      primaryBadge: 'iPhone · Dispoñible agora',
      secondaryBadge: 'Android · Beta pechada',
      mockup: {
        stationLabel: 'Estación Sol',
        availability: '12 bicis · 5 ocos',
        bikesLabel: 'Bicis listas',
        docksLabel: 'Ocos libres',
        chips: ['Favoritas', 'Avisos', 'Widgets'],
        shortcutLabel: 'Abrir traxecto da mañá',
      },
    },
    problem: {
      title: 'O problema non é coller a bici. É chegar e que non haxa.',
      intro:
        'Ir ata unha estación sen saber se haberá bicis ou ocos engade fricción xusto cando tes máis présa.',
      cards: [
        { title: 'Chegas e non quedan bicis', description: 'Perdes tempo cambiando de estación cando xa estás en marcha.' },
        { title: 'Chegas e non hai ocos libres', description: 'A devolución tamén falla cando non podes anticipar dispoñibilidade.' },
        { title: 'Demasiados pasos para unha consulta simple', description: 'Abrir a app, buscar, refrescar e comparar fai lenta unha decisión que debería ser inmediata.' },
      ],
    },
    solution: {
      title: 'Solución: información útil antes de moverte',
      cards: [
        { title: 'Tempo real', description: 'Consulta bicis e ocos antes de saír.' },
        { title: 'Favoritas', description: 'Garda as túas estacións clave e míraas primeiro.' },
        { title: 'Avisos', description: 'Recibe notificacións cando haxa bicis ou ocos dispoñibles.' },
        { title: 'Widgets', description: 'Abre o que precisas desde a pantalla principal ou o reloxo.' },
      ],
    },
    howItWorks: {
      title: 'Como funciona',
      steps: [
        { number: '1', title: 'Escolle cidade e sistema', description: 'Comezas coa rede de bici pública que xa usas.' },
        { number: '2', title: 'Garda favoritas', description: 'Fixas as estacións habituais para consultar estado en segundos.' },
        { number: '3', title: 'Consulta ou recibe avisos', description: 'Decides antes de chegar se che convén ir, esperar ou cambiar de estación.' },
      ],
    },
    cities: {
      title: 'Cidades e sistemas',
      description: 'A arquitectura queda preparada para escalar por cidade sen duplicar toda a landing.',
      items: [
        { key: 'madrid', name: 'Madrid', system: 'BiciMAD', description: 'Consulta BiciMAD antes de saír.' },
        { key: 'barcelona', name: 'Barcelona', system: 'Bicing', description: 'Atopa unha estación útil de Bicing cando vas con présa.' },
        { key: 'sevilla', name: 'Sevilla', system: 'Sevici', description: 'Anticipa dispoñibilidade de Sevici nos teus traxectos diarios.' },
        { key: 'valencia', name: 'Valencia', system: 'Valenbisi', description: 'Evita voltas innecesarias buscando ocos libres.' },
        { key: 'zaragoza', name: 'Zaragoza', system: 'Bizi', description: 'Revisa Bizi en tempo real e abre favoritas rápido.' },
      ],
      moreLabel: 'Máis cidades compatibles proximamente',
    },
    midCta: {
      title: 'Dous accesos rápidos segundo a plataforma',
      description: 'A beta está pensada para captar demanda agora e escalar invitacións por plataforma e cidade.',
      primaryCta: { label: 'Descargar na App Store', note: 'Dispoñible agora en iPhone' },
      secondaryCta: { label: 'Solicitar acceso beta', note: 'Android en acceso controlado' },
    },
    betaForm: {
      title: 'Acceso Android beta',
      description: 'Usaremos o teu email para xestionar o acceso á beta e priorizar cidades con máis demanda.',
      helper: 'Tamén nos axuda a medir interese por sistema, plataforma, widgets e frecuencia de uso.',
      honeypotLabel: 'Empresa',
      consentLabel: 'Acepto que BiciRadar use estes datos para xestionar o meu acceso á beta e contactarme sobre esta solicitude.',
      consentHint: 'Só usamos a información para o proceso beta.',
      submitLabel: 'Solicitar acceso beta',
      loadingLabel: 'Enviando solicitude...',
      successTitle: 'Solicitude enviada',
      successMessage: 'Gardamos a túa solicitude. Avisarémoste cando abramos o teu acceso.',
      errorMessage: 'Non puidemos enviar a túa solicitude. Téntao de novo nuns segundos.',
      options: {
        operatingSystems,
        cities: [
          { value: 'madrid', label: 'Madrid' },
          { value: 'barcelona', label: 'Barcelona' },
          { value: 'sevilla', label: 'Sevilla' },
          { value: 'valencia', label: 'Valencia' },
          { value: 'zaragoza', label: 'Zaragoza' },
          { value: 'other', label: 'Outra cidade' },
        ],
        systems: [
          { value: 'bicimad', label: 'BiciMAD' },
          { value: 'bicing', label: 'Bicing' },
          { value: 'sevici', label: 'Sevici' },
          { value: 'valenbisi', label: 'Valenbisi' },
          { value: 'bizi', label: 'Bizi' },
          { value: 'other', label: 'Outro sistema' },
        ],
        frequencies,
        interests,
        yesNo,
      },
      fields: {
        email: { label: 'Email', placeholder: 'ti@email.com' },
        operatingSystem: { label: 'Sistema operativo', placeholder: 'Selecciona o teu sistema operativo' },
        city: { label: 'Cidade', placeholder: 'Selecciona a túa cidade principal' },
        bikeSystem: { label: 'Sistema de bici', placeholder: 'Selecciona o sistema que usas máis' },
        frequency: { label: 'Frecuencia de uso', placeholder: 'Con que frecuencia usas bici compartida?' },
        interest: { label: 'Interese principal', placeholder: 'Que che interesa máis de BiciRadar?' },
        widgets: { label: 'Interésanche os widgets?', placeholder: 'Selecciona unha opción' },
        smartwatch: { label: 'Usas smartwatch?', placeholder: 'Selecciona unha opción' },
      },
      validation: {
        required: 'Completa este campo.',
        email: 'Introduce un email válido.',
        consent: 'Necesitamos o teu consentimento para xestionar a beta.',
        turnstile: 'Completa a verificación de seguridade e téntao de novo.',
        server: 'Houbo un problema ao gardar a túa solicitude.',
      },
    },
    faq: {
      title: 'FAQ',
      items: [
        { question: 'Se xa teño a app oficial, que me aporta BiciRadar?', answer: 'Rapidez para o importante: estación máis próxima, bicis dispoñibles, ocos libres, favoritas, estado e ruta.' },
        { question: 'Aforra tempo de verdade?', answer: 'Si. Inclúe voz e atallos en iPhone, Apple Watch e Android para resolver consultas con menos pasos.' },
        { question: 'Só serve no móbil?', answer: 'Non. Está pensada para móbil e reloxo: Android, iOS, Wear OS e Apple Watch.' },
        { question: 'Os datos son fiables?', answer: 'Usa fontes oficiais de open data das cidades. Pode haber pequenos desfases normais en sistemas en tempo real.' },
        { question: 'Por que usar BiciRadar como complemento?', answer: 'A app oficial é a canle institucional; BiciRadar está optimizada para consultar máis rápido e con menos fricción.' },
        { question: 'Que ides facer cos datos do formulario?', answer: 'Usaremos o teu email para enviarche os pasos e ligazóns da beta (grupo de testers en Google e descarga en Play ou App Store). Non o usaremos para outros fins fóra deste proceso.' },
      ],
    },
    footer: {
      tagline: 'Beta de bici compartida orientada a conversión, SEO local e escalado por cidade.',
      links: [
        { label: 'Privacidade', href: 'mailto:hola@biciradar.es?subject=Privacidade%20beta' },
        { label: 'Contacto', href: 'mailto:hola@biciradar.es?subject=BiciRadar%20beta' },
        { label: 'Soporte', href: 'mailto:soporte@biciradar.es?subject=Soporte%20BiciRadar' },
      ],
      primaryCta: 'Entrar na beta',
      note: 'BiciRadar · iPhone e Android · Datos baseados en fontes oficiais',
      githubLine: 'Código aberto en GitHub',
    },
  },
  thankYou: {
    seo: {
      title: 'Grazas por apuntarte á beta de BiciRadar',
      description: 'A túa solicitude para a beta enviouse correctamente. Revisa próximos pasos, comparte a beta e visita páxinas locais.',
    },
    badge: 'Solicitude enviada',
    ios: {
      title: 'Grazas. Xa podes descargar BiciRadar no iPhone.',
      description:
        'Enviámosche un email coa ligazón á App Store. Mentres tanto podes explorar cidades dispoñibles ou compartir a beta.',
      steps: [
        'Abre o email de confirmación na bandexa de entrada.',
        'Descarga BiciRadar desde a App Store no teu iPhone.',
        'Activa notificacións e garda as estacións favoritas.',
      ],
    },
    androidOrBoth: {
      title: 'Grazas. Seguinte paso: únete ao grupo de testers en Google.',
      description:
        'Para a beta en Android debes entrar no grupo testers-biciradar coa túa conta de Google e despois instalar desde Google Play. Enviámosche un email coas mesmas ligazóns.',
      stepsAndroid: [
        'Únete ao grupo testers-biciradar en Google Groups coa mesma conta de Google que usarás no móbil.',
        'Cando estés dentro do grupo, descarga BiciRadar desde Google Play.',
      ],
      stepsBoth: [
        'Únete ao grupo testers-biciradar en Google Groups coa mesma conta de Google que usarás en Android.',
        'Descarga BiciRadar desde Google Play para a beta en Android.',
        'No iPhone, instala tamén desde a App Store co botón de abaixo.',
      ],
    },
    cityLinksTitle: 'Explora cidades dispoñibles mentres tanto',
    cityCardCtaPrefix: 'Ver',
    shareLabel: 'Compartir a beta',
    primaryCta: 'Explora cidades dispoñibles mentres tanto',
    appStoreCta: 'Descargar na App Store',
    playStoreCta: 'Descargar en Google Play',
    googleGroupCta: 'Unirse ao grupo testers-biciradar',
    footnote: 'Non recibes o email? Revisa spam ou escríbenos a soporte.',
  },
  cityPages: {
    madrid: {
      seo: { title: 'BiciMAD en tempo real con BiciRadar | Beta Madrid', description: 'Consulta BiciMAD antes de saír, evita estacións baleiras e solicita acceso á beta de BiciRadar para Madrid.' },
      badge: 'Madrid · BiciMAD',
      title: 'Consulta BiciMAD en tempo real antes de saír',
      description: 'Comproba bicis e ocos en estacións clave de Madrid e evita desprazamentos innecesarios a bases baleiras ou cheas.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Evita estacións baleiras', description: 'Decide antes de ir a unha base de BiciMAD.' },
        { title: 'Planifica traxectos urbanos', description: 'Combina favoritas e avisos nos teus percorridos diarios.' },
        { title: 'Recibe avisos útiles', description: 'Vixía zonas clave cando precisas devolver ou coller bici rápido.' },
      ],
      faqTitle: 'FAQ Madrid',
      faq: [
        { question: 'Funciona con BiciMAD?', answer: 'Si. A páxina queda preparada para contido específico de BiciMAD e demanda local.' },
        { question: 'Madrid tamén entra na beta de Android?', answer: 'Si. Podes solicitar acceso e convidaremos usuarios por fases.' },
      ],
      mockup: { stationLabel: 'Estación Sol', availability: '12 bicis · 5 ocos' },
    },
    barcelona: {
      seo: { title: 'Bicing en tempo real con BiciRadar | Beta Barcelona', description: 'Consulta Bicing antes de moverte, garda favoritas e solicita acceso á beta de BiciRadar para Barcelona.' },
      badge: 'Barcelona · Bicing',
      title: 'Consulta Bicing antes de chegar á estación',
      description: 'Ve bicis e ancoraxes libres en Barcelona para decidir se che convén manter ruta, cambiar de base ou esperar.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Menos voltas en hora punta', description: 'Consulta Bicing antes de saír do metro ou da oficina.' },
        { title: 'Favoritas para a túa rutina', description: 'Ten a man estacións preto da casa, traballo ou universidade.' },
        { title: 'Widgets para consultas rápidas', description: 'Abre o estado de Bicing sen entrar na app completa.' },
      ],
      faqTitle: 'FAQ Barcelona',
      faq: [
        { question: 'Podo usalo con Bicing se xa teño a app oficial?', answer: 'Si. BiciRadar complementa o uso diario con consultas e accesos máis rápidos.' },
        { question: 'Barcelona terá invitacións beta?', answer: 'Si. Estamos recollendo demanda para priorizar acceso por cidade.' },
      ],
      mockup: { stationLabel: 'Pg. de Gràcia', availability: '8 bicis · 4 ocos' },
    },
    sevilla: {
      seo: { title: 'Sevici en tempo real con BiciRadar | Beta Sevilla', description: 'Anticipa dispoñibilidade de Sevici, garda estacións favoritas e solicita acceso á beta de BiciRadar en Sevilla.' },
      badge: 'Sevilla · Sevici',
      title: 'Consulta Sevici en tempo real antes de moverte',
      description: 'Comproba dispoñibilidade en Sevilla e evita chegar sen bicis ou sen ocos cando precisas decidir rápido.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Menos incerteza ao saír', description: 'Consulta Sevici xusto antes de comezar o traxecto.' },
        { title: 'Acceso rápido a favoritas', description: 'Ten a man estacións clave para casa, traballo ou estudo.' },
        { title: 'Avisos para devolver mellor', description: 'Recibe sinais útiles cando volvan quedar ocos libres.' },
      ],
      faqTitle: 'FAQ Sevilla',
      faq: [
        { question: 'Sevici está previsto na beta?', answer: 'Si. A landing xa queda preparada para captar demanda local de Sevilla con SEO propio.' },
        { question: 'Podo deixar os meus datos aínda que non haxa invitación inmediata?', answer: 'Si. Gardamos o teu interese para priorizar aperturas futuras por cidade.' },
      ],
      mockup: { stationLabel: 'Puerta Jerez', availability: '6 bicis · 7 ocos' },
    },
    valencia: {
      seo: { title: 'Valenbisi en tempo real con BiciRadar | Beta Valencia', description: 'Consulta Valenbisi, evita estacións completas e solicita acceso á beta de BiciRadar para Valencia.' },
      badge: 'Valencia · Valenbisi',
      title: 'Consulta Valenbisi antes de achegarte á estación',
      description: 'Ten visibilidade de bicis e ocos en Valencia para non improvisar a devolución ou a recollida sobre a marcha.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Evita estacións completas', description: 'Comproba ocos libres antes de chegar ao destino.' },
        { title: 'Mellora a primeira decisión', description: 'Ve cal é a mellor base próxima con menos fricción.' },
        { title: 'Garda traxectos repetidos', description: 'Repite consultas clave con favoritas, avisos e widgets.' },
      ],
      faqTitle: 'FAQ Valencia',
      faq: [
        { question: 'BiciRadar serve para Valenbisi?', answer: 'Si. Está preparada para páxinas e campañas específicas de Valencia.' },
        { question: 'Tamén haberá acceso beta para Android?', answer: 'Si. Podes apuntarte desde esta páxina e contactarémoste por email.' },
      ],
      mockup: { stationLabel: 'Colón', availability: '10 bicis · 2 ocos' },
    },
    zaragoza: {
      seo: { title: 'Bizi en tempo real con BiciRadar | Beta Zaragoza', description: 'Consulta Bizi en Zaragoza, abre favoritas e solicita acceso á beta de BiciRadar con contido local.' },
      badge: 'Zaragoza · Bizi',
      title: 'Consulta Bizi en tempo real antes de pedalear',
      description: 'Accede á dispoñibilidade de Bizi en Zaragoza, garda estacións habituais e reduce o tempo de consulta diaria.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Consulta rápida de Bizi', description: 'Abre o estado de estacións frecuentes en segundos.' },
        { title: 'Contido local preparado para SEO', description: 'Cada cidade ten copy, FAQ e metadatos propios.' },
        { title: 'Beta escalable por cidade', description: 'Recollémola demanda de Zaragoza sen duplicar a arquitectura.' },
      ],
      faqTitle: 'FAQ Zaragoza',
      faq: [
        { question: 'Zaragoza está incluída na primeira fase?', answer: 'Si. Xa queda preparada como unha das páxinas locais iniciais con CTA á beta.' },
        { question: 'Podo compartir esta páxina con outra xente de Zaragoza?', answer: 'Si. Animámoste a compartila para medir mellor a demanda local.' },
      ],
      mockup: { stationLabel: 'Praza España', availability: '7 bicis · 6 ocos' },
    },
  },
} satisfies LocaleContent;

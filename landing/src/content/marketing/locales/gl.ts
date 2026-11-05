import type { LocaleContent } from '../types';

const operatingSystems = [
  { value: 'ios', label: 'iPhone / iOS' },
  { value: 'android', label: 'Android' },
  { value: 'both', label: 'Uso ambos' },
];

export const gl = {
  locale: 'gl',
  languageName: 'Galego',
  localeLabel: 'GL',
  metadata: {
    siteName: 'BiciRadar',
    siteTagline: 'A túa estación de bici, dunha ollada',
    defaultOgTitle: 'BiciRadar | Consulta bicis e ocos en tempo real',
    defaultOgDescription:
      'Mira dispoñibilidade en sistemas de bici compartida, garda favoritas e descarga BiciRadar.',
  },
  common: {
    skipToContent: 'Saltar ao contido',
    appStoreLabel: 'Descargar na App Store',
    androidBetaLabel: 'Descargar en Google Play',
    openMenu: 'Abrir navegación',
    closeMenu: 'Pechar navegación',
    backToHome: 'Volver ao inicio',
    viewCities: 'Ver cidades',
    cityPageCta: 'Ver páxina local',
    heroCta: 'Descargar agora',
    heroSecondaryCta: 'Ver como funciona',
    cityHeroMessage:
      'Abres a app, miras a túa estación e nun momento xa sabes se che compensa saír ou cambiar de plan.',
    citySeoTitleSuffix: 'BiciRadar',
    citySeoDescriptionSuffix:
      'Consulta a túa estación antes de saír desde o móbil ou o reloxo.',
    finalCtaTitle: 'Descárgaa gratis e próbaa',
    finalCtaDescription:
      'Garda as túas estacións de sempre e desde o primeiro día decides antes de saír. Sen conta para comezar e con datos abertos de cada rede.',
    cityRevisitLabel: 'Volver ver cidades',
    shareDescription: 'Comparte BiciRadar con outra persoa da túa cidade.',
    betaInviteLabel: 'Descarga directa',
    faqLabel: 'Preguntas frecuentes',
    thankYouShareFallback: 'Ligazón copiada. Xa a podes compartir.',
    githubAriaLabel: 'Código fonte en GitHub',
  },
  home: {
    seo: {
      title: 'BiciRadar | Bicis e ocos da túa estación antes de saír',
      description:
        'Mira se hai bicis ou ocos libres na túa estación de bici pública antes de saír. Favoritas, widgets, reloxo e avisos. Gratis en iPhone e Android.',
    },
    header: {
      sections: [
        { id: 'vantaxes', label: 'Vantaxes' },
        { id: 'app', label: 'A app' },
        { id: 'como-funciona', label: 'Como funciona' },
        { id: 'cidades', label: 'Cidades' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Descargar',
      languageLabel: 'Idioma',
    },
    hero: {
      title: 'Hai bici na túa estación? Mírao antes de saír.',
      description:
        'BiciRadar móstrache as bicis e os ocos libres da túa bici pública. As túas estacións de sempre, a un toque desde o móbil, un widget ou o reloxo.',
      primaryCta: 'Descargar na App Store',
      secondaryCta: 'Descargar en Google Play',
      microcopy: 'Gratis · Sen conta para comezar · iPhone e Android',
      trust: ['Máis de 40 cidades', 'Apple Watch e Wear OS', 'Widgets e voz'],
      screenshotAlt: 'Mapa de BiciRadar coa estación máis próxima, as súas bicis e ocos libres',
      mockup: {
        stationLabel: 'Estación Sol',
        availability: '12 bicis · 5 ocos',
        bikesLabel: 'Bicis listas',
        docksLabel: 'Ocos libres',
        chips: ['Favoritas', 'Avisos', 'Widgets'],
        shortcutLabel: 'Abrir traxecto da mañá',
      },
    },
    solution: {
      title: 'Feita para saber que hai antes de baixar á rúa',
      intro:
        'Chegar a unha estación valdeira, ou sen un oco libre, é unha perda de tempo evitable. Isto é o que BiciRadar che pon doado.',
      cards: [
        {
          icon: 'pin',
          title: 'A estación útil, nada máis abrir a app',
          description:
            'Ves a máis próxima con bicis e a máis próxima con ocos, con distancia e ruta a un toque. Se vas a pé, móstrache as que teñen bicis; se vas en bici, as que teñen ocos.',
        },
        {
          icon: 'sparkles',
          title: 'Filtra e ordena como che veña ben',
          description:
            'Por distancia, por bicis, por ocos ou pola dispoñibilidade que adoita haber a esa hora. Onde a rede o publica, tamén eléctricas ou mecánicas.',
        },
        {
          icon: 'star',
          title: 'Casa, Traballo e favoritas',
          description:
            'Fixa as túas estacións de sempre e ábreas cun toque, sen buscalas cada vez.',
        },
        {
          icon: 'bell',
          title: 'Avisos das túas estacións',
          description:
            'Alertas para Casa, Traballo e favoritas. Cunha conta (opcional), as rutinas vixían a túa estación antes de saír e propóñente unha alternativa próxima.',
        },
        {
          icon: 'phone',
          title: 'En widgets e no reloxo',
          description:
            'Widgets no móbil e app para Apple Watch e Wear OS, para mirar a túa estación sen sacar o teléfono.',
        },
        {
          icon: 'globe',
          title: 'Pregúntalle ao teu asistente',
          description:
            'Con Siri, Atallos ou Google Assistant podes preguntar se hai bicis ou ocos na túa estación.',
        },
      ],
      note: 'BiciRadar mostra a dispoñibilidade para axudarche a decidir; non xestiona alugueiros nin pagamentos.',
    },
    showcase: {
      title: 'Así se ve por dentro',
      intro: 'Capturas reais da app, sen adornos.',
      items: [
        {
          key: 'map',
          title: 'Mapa en directo',
          description: 'Bicis e ocos dunha ollada, coa hora da última actualización. Filtra por só bicis ou só ocos.',
          alt: 'Mapa con estacións e filtros Bicis + Ocos, Só bicis e Só ocos',
        },
        {
          key: 'nearby',
          title: 'Preto de ti',
          description: 'A estación máis próxima con bicis e a máis próxima con ocos, e ruta cun toque.',
          alt: 'Lapela Preto coa estación máis próxima con bicis e con ocos',
        },
        {
          key: 'favorites',
          title: 'Favoritas, Casa e Traballo',
          description: 'As túas estacións gardadas e a xestión de alertas, nun único sitio.',
          alt: 'Lapela Favoritas con alertas de estacións e os accesos Casa e Traballo',
        },
        {
          key: 'detail',
          title: 'Detalle de estación',
          description: 'Distancia, mapa e gardar como favorita, Casa ou Traballo cun toque.',
          alt: 'Detalle dunha estación con distancia, mapa e botóns para gardala',
        },
      ],
    },
    howItWorks: {
      title: 'Comezar leva un minuto',
      steps: [
        {
          number: '1',
          title: 'Descarga e escolle a túa cidade',
          description: 'Selecciona a túa rede de bici pública. Non fai falta crear unha conta.',
        },
        {
          number: '2',
          title: 'Mira o que tes preto',
          description: 'Coa túa localización, a app móstrache que estación che convén agora mesmo.',
        },
        {
          number: '3',
          title: 'Garda as túas estacións',
          description: 'Fixa Casa, Traballo e favoritas e decide en segundos, desde o móbil ou o reloxo.',
        },
      ],
    },
    cities: {
      title: 'Cidades e sistemas',
      description: 'Empezamos por varias cidades nas que a bici pública forma parte do día a día e iremos engadindo máis.',
      items: [
        { key: 'madrid', name: 'Madrid', system: 'BiciMAD', description: 'Consulta BiciMAD antes de saír.' },
        { key: 'barcelona', name: 'Barcelona', system: 'Bicing', description: 'Atopa unha estación útil de Bicing cando vas con présa.' },
        { key: 'sevilla', name: 'Sevilla', system: 'Sevici', description: 'Anticipa dispoñibilidade de Sevici nos teus traxectos diarios.' },
        { key: 'valencia', name: 'Valencia', system: 'Valenbisi', description: 'Evita voltas innecesarias buscando ocos libres.' },
        { key: 'zaragoza', name: 'Zaragoza', system: 'Bizi', description: 'Revisa Bizi en tempo real e abre favoritas rápido.' },
      ],
      moreLabel: 'E máis cidades dentro da app: escolle a túa ao abrila.',
      otherCitiesLabel: 'Páxinas doutras cidades',
    },
    midCta: {
      title: 'Antes de saír, mira a túa estación.',
      description:
        'Descarga BiciRadar gratis, escolle a túa cidade e ten as túas estacións a un toque.',
      reassurance: 'Gratis · Sen conta para comezar',
      primaryCta: {
        label: 'Descargar na App Store',
        note: 'Para iPhone',
      },
      secondaryCta: {
        label: 'Descargar en Google Play',
        note: 'Para Android',
      },
    },
    betaForm: {
      title: 'Escolle a túa plataforma e descarga',
      description: 'Instala BiciRadar directamente desde a App Store ou Google Play.',
      honeypotLabel: 'Empresa',
      consentLabel: 'Acepto que BiciRadar use estes datos para enviarme a ligazón de descarga e contactar comigo sobre esta solicitude.',
      consentHint: 'Só usamos a información para enviarche a ligazón e axudarche con esta solicitude.',
      submitLabel: 'Enviar ligazón',
      loadingLabel: 'Enviando ligazón...',
      successTitle: 'Ligazón enviada',
      successMessage: 'Podes descargar BiciRadar agora mesmo desde a túa tenda.',
      errorMessage: 'Non puidemos enviar a ligazón. Téntao de novo nuns segundos.',
      options: {
        operatingSystems,
      },
      fields: {
        email: { label: 'Email', placeholder: 'ti@email.com' },
        operatingSystem: { label: 'Sistema operativo', placeholder: 'Selecciona o teu sistema operativo' },
      },
      validation: {
        required: 'Completa este campo.',
        email: 'Introduce un email válido.',
        consent: 'Necesitamos o teu consentimento para enviarche a ligazón.',
        turnstile: 'Completa a verificación de seguridade e téntao de novo.',
        server: 'Houbo un problema ao enviar a ligazón.',
      },
    },
    faq: {
      title: 'Preguntas frecuentes',
      items: [
        {
          question: 'É gratis? Necesito crear unha conta?',
          answer:
            'Descárgase e úsase gratis. Podes consultar estacións, gardar favoritas e usar widgets sen conta. A conta é opcional e só fai falta para as rutinas e o seguimento de traxectos, que se vixían desde o noso servidor.',
        },
        {
          question: 'Substitúe a app oficial da miña cidade?',
          answer:
            'Non. BiciRadar serve para consultar a dispoñibilidade e decidir antes de saír; non xestiona alugueiros nin pagamentos. Podes usala xunto coa app do teu sistema.',
        },
        {
          question: 'Os datos son en tempo real?',
          answer:
            'Usamos datos abertos que publica cada sistema e actualízanse con frecuencia, pero o ritmo depende de cada provedor. En cada estación ves hai canto se actualizou.',
        },
        {
          question: 'Necesita a miña localización?',
          answer:
            'Úsaa para dicirche cal é a túa estación máis próxima. Se non das permiso, podes seguir usando a app, pero algúns resultados serán menos precisos.',
        },
        {
          question: 'Funciona no reloxo e coa voz?',
          answer:
            'Si. Hai app para Apple Watch e Wear OS, widgets en iPhone e Android, e podes preguntar con Siri, Atallos ou Google Assistant.',
        },
        {
          question: 'Que cidades cobre?',
          answer:
            'Madrid, Barcelona, Sevilla, Valencia, Zaragoza, Bilbao e moitas máis. Ao abrir a app escolles a túa cidade; se a túa ten páxina propia, atoparala na sección de cidades.',
        },
        {
          question: 'Que pasa coas miñas favoritas e cos meus datos?',
          answer:
            'As túas favoritas e lugares gardados quedan no teu dispositivo. Tes máis detalle na política de privacidade, ligada ao final da páxina.',
        },
      ],
    },
    footer: {
      tagline: '',
      links: [
        { label: 'Privacidade', href: 'https://gcaguilar.github.io/biciradar-privacy-policy/' },
        { label: 'Contacto', href: 'mailto:hola@biciradar.es?subject=BiciRadar%20app' },
        { label: 'Soporte', href: 'mailto:soporte@biciradar.es?subject=Soporte%20BiciRadar' },
      ],
      primaryCta: 'Descargar agora',
      note: 'BiciRadar · iPhone e Android · Datos abertos de cada sistema',
      githubLine: 'Código aberto en GitHub',
    },
  },
  thankYou: {
    seo: {
      title: 'Grazas por solicitar a ligazón de BiciRadar',
      description:
        'A túa solicitude enviouse correctamente. Explora a app, as cidades dispoñibles ou compártea.',
    },
    badge: 'Solicitude enviada',
    title: 'Grazas, enviarémosche a ligazón',
    description:
      'Podes instalar BiciRadar agora mesmo ou explorar as páxinas locais de cada cidade.',
    installStepByOs: {
      ios: 'No iPhone: tamén podes descargar BiciRadar xa na App Store.',
      android: 'En Android: revisa o email que che enviaremos coa ligazón a Google Play.',
      both: 'Segundo o teu móbil: App Store no iPhone ou Google Play en Android. Tamén che enviaremos as ligazóns por email.',
    },
    steps: [
      'Explora as páxinas locais para ver dispoñibilidade en tempo real.',
      'Para dúbidas, escribe a hola@biciradar.es.',
    ],
    cityLinksTitle: 'Explora cidades dispoñibles mentres tanto',
    cityCardCtaPrefix: 'Ver',
    shareLabel: 'Compartir BiciRadar',
    primaryCta: 'Explora cidades dispoñibles mentres tanto',
    appStoreCta: 'Descargar na App Store',
    playStoreCta: 'Descargar en Google Play',
    footnote: 'Dúbidas? hola@biciradar.es',
  },
  cityPages: {
    madrid: {
      seo: { title: 'BiciMAD en tempo real con BiciRadar | Madrid', description: 'Consulta BiciMAD antes de saír, evita estacións baleiras e descarga BiciRadar para Madrid.' },
      badge: 'Madrid · BiciMAD',
      title: 'Consulta BiciMAD en tempo real antes de saír',
      description: 'Comproba bicis e ocos en estacións clave de Madrid e evita desprazamentos innecesarios a bases baleiras ou cheas.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de baixar á rúa', description: 'Abre BiciMAD e confirma se a túa base habitual che compensa antes de saír.' },
        { title: 'Evita cambios de estación a última hora', description: 'Se esa base non encaixa, escolles outra antes de perder tempo xa en marcha.' },
        { title: 'Resolve a dúbida dun golpe de vista', description: 'Favoritas, avisos e accesos rápidos dánche a resposta sen fricción.' },
      ],
      faqTitle: 'FAQ Madrid',
      faq: [
        { question: 'Que me resolve BiciRadar con BiciMAD antes de saír?', answer: 'Axúdache a saber se a túa base habitual ten bicis ou ocos e se che convén baixar xa, esperar ou cambiar de estación.' },
        { question: 'Podo ver BiciMAD rápido desde o móbil ou o reloxo?', answer: 'Si. Favoritas, widgets e accesos rápidos déixanche resolver a dispoñibilidade en segundos, sen abrir toda a app.' },
      ],
      mockup: { stationLabel: 'Estación Sol', availability: '12 bicis · 5 ocos' },
    },
    barcelona: {
      seo: { title: 'Bicing en tempo real con BiciRadar | Barcelona', description: 'Consulta Bicing antes de moverte, garda favoritas e descarga BiciRadar para Barcelona.' },
      badge: 'Barcelona · Bicing',
      title: 'Consulta Bicing antes de chegar á estación',
      description: 'Ve bicis e ancoraxes libres en Barcelona para decidir se che convén manter ruta, cambiar de base ou esperar.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide ao saír do metro', description: 'Consulta Bicing antes de subir á rúa ou saír da oficina.' },
        { title: 'Evita voltas en hora punta', description: 'Se esa base non che convén, cambias de estación antes de facer o desvío.' },
        { title: 'Mantén a túa rutina a un toque', description: 'As túas estacións clave quedan listas para resolver a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Barcelona',
      faq: [
        { question: 'Que me resolve BiciRadar con Bicing antes de chegar?', answer: 'Axúdache a saber se esa estación che compensa ou se é mellor cambiar de base antes de facer o desvío.' },
        { question: 'E se xa miro Bicing na app oficial?', answer: 'BiciRadar reduce pasos xusto cando vas con présa: abres favoritas, ves dispoñibilidade e tomas a decisión antes.' },
      ],
      mockup: { stationLabel: 'Pg. de Gràcia', availability: '8 bicis · 4 ocos' },
    },
    sevilla: {
      seo: { title: 'Sevici en tempo real con BiciRadar | Sevilla', description: 'Anticipa dispoñibilidade de Sevici, garda estacións favoritas e descarga BiciRadar en Sevilla.' },
      badge: 'Sevilla · Sevici',
      title: 'Consulta Sevici en tempo real antes de moverte',
      description: 'Comproba dispoñibilidade en Sevilla e evita chegar sen bicis ou sen ocos cando precisas decidir rápido.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Saes con menos incerteza', description: 'Consulta Sevici xusto antes de arrancar e decide con contexto.' },
        { title: 'Ten a base clave á man', description: 'As túas estacións habituais quedan listas para resolver a dúbida en segundos.' },
        { title: 'Devolve mellor sen improvisar', description: 'Recibe sinais útiles cando volven aparecer ocos onde os precisas.' },
      ],
      faqTitle: 'FAQ Sevilla',
      faq: [
        { question: 'Que decisión me axuda a tomar con Sevici antes de moverme?', answer: 'Se che convén ir á túa estación habitual, esperar uns minutos ou cambiar de base antes de perder tempo en marcha.' },
        { question: 'Podo ter Sevici á man sen entrar en toda a app?', answer: 'Si. Favoritas, widgets e accesos rápidos déixanche ver a dispoñibilidade e actuar en segundos.' },
      ],
      mockup: { stationLabel: 'Puerta Jerez', availability: '6 bicis · 7 ocos' },
    },
    valencia: {
      seo: { title: 'Valenbisi en tempo real con BiciRadar | Valencia', description: 'Consulta Valenbisi, evita estacións completas e descarga BiciRadar para Valencia.' },
      badge: 'Valencia · Valenbisi',
      title: 'Consulta Valenbisi antes de achegarte á estación',
      description: 'Ten visibilidade de bicis e ocos en Valencia para non improvisar a devolución ou a recollida sobre a marcha.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Evita chegar a unha base chea', description: 'Comproba ocos libres antes de achegarte ao destino e decide mellor.' },
        { title: 'Escolle mellor a primeira estación', description: 'Ves que base che convén para coller ou devolver bici sen improvisar.' },
        { title: 'Resolve a rutina sen empezar de cero', description: 'Favoritas, avisos e accesos rápidos afórranche pasos en cada traxecto.' },
      ],
      faqTitle: 'FAQ Valencia',
      faq: [
        { question: 'Que me resolve BiciRadar con Valenbisi antes de achegarme á estación?', answer: 'Axúdache a decidir se esa base che convén para coller ou devolver bici antes de improvisar sobre a marcha.' },
        { question: 'Podo velo rápido en iPhone ou Android?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Colón', availability: '10 bicis · 2 ocos' },
    },
    zaragoza: {
      seo: { title: 'Bizi en tempo real con BiciRadar | Zaragoza', description: 'Consulta Bizi en Zaragoza, abre favoritas e descarga BiciRadar con contido local.' },
      badge: 'Zaragoza · Bizi',
      title: 'Consulta Bizi en tempo real antes de pedalear',
      description: 'Accede á dispoñibilidade de Bizi en Zaragoza, garda estacións habituais e reduce o tempo de consulta diaria.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de pedalear', description: 'Comproba se a túa estación habitual ten dispoñibilidade antes de saír.' },
        { title: 'Evita rodeos entre bases', description: 'Se esa estación non che compensa, cambias de plan antes de perder tempo.' },
        { title: 'Consulta Bizi en segundos', description: 'Favoritas e accesos rápidos déixanche resolver a dúbida case ao instante.' },
      ],
faqTitle: 'FAQ Zaragoza',
      faq: [
        { question: 'Que me resolve BiciRadar con Bizi antes de pedalear?', answer: 'Axúdache a saber se a túa estación habitual ten bicis ou ocos e se che compensa saír xa ou cambiar de base.' },
        { question: 'Podo compartilo con alguén de Zaragoza?', answer: 'Si. Se outra persoa ten a mesma dúbida diaria, esta páxina e a app axúdana a decidir antes de moverse.' },
      ],
      mockup: { stationLabel: 'Praza España', availability: '7 bicis · 6 prazas' },
    },
    bilbao: {
      seo: { title: 'Bilbao en tempo real con BiciRadar | Bilbao', description: 'Consulta bicis e prazas en Bilbao antes de saír e descarga BiciRadar.' },
      badge: 'Bilbao · Bilbo',
      title: 'Consulta Bilbao en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Bilbao antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Bilbao',
      faq: [
        { question: 'Que me resolve BiciRadar en Bilbao antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Bilbao', availability: '5 bicis · 3 prazas' },
    },
    murcia: {
      seo: { title: 'Murcia en tempo real con BiciRadar | Murcia', description: 'Consulta bicis e prazas en Murcia antes de saír e descarga BiciRadar.' },
      badge: 'Murcia · Murcia',
      title: 'Consulta Murcia en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Murcia antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Murcia',
      faq: [
        { question: 'Que me resolve BiciRadar en Murcia antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Murcia', availability: '4 bicis · 2 prazas' },
    },
    leon: {
      seo: { title: 'León en tempo real con BiciRadar | León', description: 'Consulta bicis e prazas en León antes de saír e descarga BiciRadar.' },
      badge: 'León · León',
      title: 'Consulta León en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en León antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ León',
      faq: [
        { question: 'Que me resolve BiciRadar en León antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'León', availability: '3 bicis · 2 prazas' },
    },
    valladolid: {
      seo: { title: 'Valladolid en tempo real con BiciRadar | Valladolid', description: 'Consulta bicis e prazas en Valladolid antes de saír e descarga BiciRadar.' },
      badge: 'Valladolid · Valladolid',
      title: 'Consulta Valladolid en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Valladolid antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Valladolid',
      faq: [
        { question: 'Que me resolve BiciRadar en Valladolid antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Valladolid', availability: '4 bicis · 3 prazas' },
    },
    palma: {
      seo: { title: 'Palma en tempo real con BiciRadar | Palma', description: 'Consulta bicis e prazas en Palma antes de saír e descarga BiciRadar.' },
      badge: 'Palma · Palma',
      title: 'Consulta Palma en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Palma antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Palma',
      faq: [
        { question: 'Que me resolve BiciRadar en Palma antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Palma', availability: '5 bicis · 4 prazas' },
    },
    las_palmas: {
      seo: { title: 'Las Palmas en tempo real con BiciRadar | Las Palmas', description: 'Consulta bicis e prazas en Las Palmas antes de saír e descarga BiciRadar.' },
      badge: 'Las Palmas · Las Palmas',
      title: 'Consulta Las Palmas en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Las Palmas antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Las Palmas',
      faq: [
        { question: 'Que me resolve BiciRadar en Las Palmas antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Las Palmas', availability: '4 bicis · 3 prazas' },
    },
    a_coruna: {
      seo: { title: 'A Coruña en tempo real con BiciRadar | A Coruña', description: 'Consulta bicis e prazas en A Coruña antes de saír e descarga BiciRadar.' },
      badge: 'A Coruña · A Coruña',
      title: 'Consulta A Coruña en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en A Coruña antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ A Coruña',
      faq: [
        { question: 'Que me resolve BiciRadar en A Coruña antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'A Coruña', availability: '3 bicis · 2 prazas' },
    },
    gijon: {
      seo: { title: 'Gijón en tempo real con BiciRadar | Gijón', description: 'Consulta bicis e prazas en Gijón antes de saír e descarga BiciRadar.' },
      badge: 'Gijón · Gijón',
      title: 'Consulta Gijón en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Gijón antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Gijón',
      faq: [
        { question: 'Que me resolve BiciRadar en Gijón antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Gijón', availability: '4 bicis · 3 prazas' },
    },
    vitoria_gasteiz: {
      seo: { title: 'Vitoria-Gasteiz en tempo real con BiciRadar | Vitoria', description: 'Consulta bicis e prazas en Vitoria antes de saír e descarga BiciRadar.' },
      badge: 'Vitoria-Gasteiz · Vitoria',
      title: 'Consulta Vitoria-Gasteiz en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Vitoria antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Vitoria',
      faq: [
        { question: 'Que me resolve BiciRadar en Vitoria-Gasteiz antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Vitoria', availability: '3 bicis · 2 prazas' },
    },
    pamplona: {
      seo: { title: 'Pamplona en tempo real con BiciRadar | Pamplona', description: 'Consulta bicis e prazas en Pamplona antes de saír e descarga BiciRadar.' },
      badge: 'Pamplona · Pamplona',
      title: 'Consulta Pamplona en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Pamplona antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Pamplona',
      faq: [
        { question: 'Que me resolve BiciRadar en Pamplona antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Pamplona', availability: '5 bicis · 4 prazas' },
    },
    castellon: {
      seo: { title: 'Castellón en tempo real con BiciRadar | Castellón', description: 'Consulta bicis e prazas en Castellón antes de saír e descarga BiciRadar.' },
      badge: 'Castellón · Castellón',
      title: 'Consulta Castellón en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Castellón antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Castellón',
      faq: [
        { question: 'Que me resolve BiciRadar en Castellón antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Castellón', availability: '3 bicis · 2 prazas' },
    },
    santander: {
      seo: { title: 'Santander en tempo real con BiciRadar | Santander', description: 'Consulta bicis e prazas en Santander antes de saír e descarga BiciRadar.' },
      badge: 'Santander · Santander',
      title: 'Consulta Santander en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Santander antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Santander',
      faq: [
        { question: 'Que me resolve BiciRadar en Santander antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Santander', availability: '4 bicis · 3 prazas' },
    },
    girona: {
      seo: { title: 'Girona en tempo real con BiciRadar | Girona', description: 'Consulta bicis e prazas en Girona antes de saír e descarga BiciRadar.' },
      badge: 'Girona · Girona',
      title: 'Consulta Girona en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Girona antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Girona',
      faq: [
        { question: 'Que me resolve BiciRadar en Girona antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Girona', availability: '3 bicis · 2 prazas' },
    },
    gran_canaria: {
      seo: { title: 'Gran Canaria en tempo real con BiciRadar | Gran Canaria', description: 'Consulta bicis e prazas en Gran Canaria antes de saír e descarga BiciRadar.' },
      badge: 'Gran Canaria · Gran Canaria',
      title: 'Consulta Gran Canaria en tempo real antes de saír',
      description: 'Consulta bicis e prazas dispoñibles en Gran Canaria antes de saír.',
      benefitsTitle: 'Beneficios locais',
      benefits: [
        { title: 'Decide antes de saír', description: 'Comproba se a túa estación habitual ten bicis ou prazas antes de poñerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Se esa base non che compensa, cambias de plan antes de perder tempo na rúa.' },
        { title: 'Consúltao dun golpe de vista', description: 'Abre favoritas, widget ou móbil e resolve a dúbida en segundos.' },
      ],
      faqTitle: 'FAQ Gran Canaria',
      faq: [
        { question: 'Que me resolve BiciRadar en Gran Canaria antes de moverme?', answer: 'Axúdache a saber se a túa estación habitual ten dispoñibilidade e se che convén saír xa, esperar ou cambiar de base.' },
        { question: 'Podo velo rápido desde o móbil ou o reloxo?', answer: 'Si. A dispoñibilidade queda a man en favoritas, widgets e accesos rápidos para decidir sen abrir todo o fluxo.' },
      ],
      mockup: { stationLabel: 'Gran Canaria', availability: '4 bicis · 3 prazas' },
    },
  },
} satisfies LocaleContent;

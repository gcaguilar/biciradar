import type { LocaleContent } from '../types';

const operatingSystems = [
  { value: 'ios', label: 'iPhone / iOS' },
  { value: 'android', label: 'Android' },
  { value: 'both', label: 'Faig servir tots dos' },
];

export const ca = {
  locale: 'ca',
  languageName: 'Català',
  localeLabel: 'CA',
  metadata: {
    siteName: 'BiciRadar',
    siteTagline: 'Consulta bicis i ancoratges abans d’arribar',
    defaultOgTitle: 'BiciRadar | Consulta bicis i ancoratges en temps real',
    defaultOgDescription:
      'Mira disponibilitat en sistemes de bici compartida, desa preferides i descarrega BiciRadar.',
  },
  common: {
    skipToContent: 'Saltar al contingut',
    appStoreLabel: 'Descarrega a l’App Store',
    androidBetaLabel: 'Descarrega a Google Play',
    openMenu: 'Obrir navegació',
    closeMenu: 'Tancar navegació',
    backToHome: 'Tornar a la portada',
    viewCities: 'Veure ciutats',
    cityPageCta: 'Veure pàgina local',
    heroCta: 'T’ho enviem per correu',
    heroSecondaryCta: 'Veure com funciona',
    finalCtaTitle: 'Descarrega-la ara o rep l’enllaç per correu',
    finalCtaDescription:
      'BiciRadar ja és a l’App Store i a Google Play. Si vols, deixa’ns el teu correu i t’enviarem l’enllaç.',
    cityRevisitLabel: 'Tornar a veure ciutats',
    shareDescription: 'Comparteix BiciRadar amb una altra persona de la teva ciutat.',
    betaInviteLabel: 'També per correu',
    faqLabel: 'Preguntes freqüents',
    thankYouShareFallback: 'Enllaç copiat. Ja el pots compartir.',
    githubAriaLabel: 'Codi font a GitHub',
  },
  home: {
    seo: {
      title: 'BiciRadar | Consulta bicis i ancoratges en temps real abans d’arribar',
      description:
        'BiciRadar t’ajuda a veure disponibilitat a BiciMAD, Bicing, Sevici, Valenbisi, Bizi i més. Desa preferides, rep avisos i hi accedeixes ràpid des de widgets.',
    },
    header: {
      sections: [
        { id: 'problema', label: 'Problema' },
        { id: 'solucio', label: 'Solució' },
        { id: 'com-funciona', label: 'Com funciona' },
        { id: 'ciutats', label: 'Ciutats' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: "Descarrega l'app",
      languageLabel: 'Idioma',
    },
    hero: {
      title: 'Consulta bicis i ancoratges en temps real abans d’arribar',
      description:
        'BiciRadar t’ajuda a veure disponibilitat en sistemes de bici compartida com BiciMAD, Bicing, Sevici i més. Desa preferides, rep avisos i accedeix ràpid des de widgets.',
      primaryCta: 'Descarrega a l’App Store',
      secondaryCta: 'Descarrega a Google Play',
      microcopy: '',
      primaryBadge: 'iPhone · Disponible ara',
      secondaryBadge: 'Android · Disponible ara',
      mockup: {
        stationLabel: 'Estació Sol',
        availability: '12 bicis · 5 ancoratges',
        bikesLabel: 'Bicis a punt',
        docksLabel: 'Ancoratges lliures',
        chips: ['Preferides', 'Avisos', 'Widgets'],
        shortcutLabel: 'Obrir trajecte del matí',
      },
    },
    problem: {
      title: 'El problema no és agafar la bici. És arribar i que no n’hi hagi.',
      intro:
        'Anar fins a una estació sense saber si hi haurà bicis o ancoratges lliures afegeix fricció just quan tens pressa.',
      cards: [
        {
          title: 'Arribes i no queden bicis',
          description: 'Perds temps canviant d’estació quan ja estàs en moviment.',
        },
        {
          title: 'Arribes i no queden ancoratges',
          description: 'La devolució també falla quan no pots anticipar disponibilitat.',
        },
        {
          title: 'Massa passos per a una consulta simple',
          description: 'Obrir l’app, buscar, refrescar i comparar fa lenta una decisió que hauria de ser immediata.',
        },
      ],
    },
    solution: {
      title: 'Solució: informació útil abans de moure’t',
      cards: [
        { title: 'Temps real', description: 'Consulta bicis i ancoratges abans de sortir.' },
        { title: 'Preferides', description: 'Desa les teves estacions clau i veu-les primer.' },
        { title: 'Avisos', description: 'Rep notificacions quan hi hagi bicis o llocs lliures.' },
        { title: 'Widgets', description: 'Obre el que necessites des de la pantalla principal o el rellotge.' },
      ],
    },
    howItWorks: {
      title: 'Com funciona',
      steps: [
        {
          number: '1',
          title: 'Tria ciutat i sistema',
          description: 'Comences amb la xarxa de bici pública que ja fas servir.',
        },
        {
          number: '2',
          title: 'Desa preferides',
          description: 'Fixes les estacions habituals per consultar estat en segons.',
        },
        {
          number: '3',
          title: 'Consulta o rep avisos',
          description: 'Decideixes abans d’arribar si et convé anar-hi, esperar o canviar d’estació.',
        },
      ],
    },
    cities: {
      title: 'Ciutats i sistemes',
      description:
        'L’arquitectura queda preparada per escalar per ciutat sense duplicar tota la landing.',
      items: [
        { key: 'madrid', name: 'Madrid', system: 'BiciMAD', description: 'Consulta BiciMAD abans de sortir.' },
        { key: 'barcelona', name: 'Barcelona', system: 'Bicing', description: 'Troba una estació útil de Bicing quan tens pressa.' },
        { key: 'sevilla', name: 'Sevilla', system: 'Sevici', description: 'Anticipa disponibilitat de Sevici en trajectes diaris.' },
        { key: 'valencia', name: 'València', system: 'Valenbisi', description: 'Evita voltes innecessàries buscant ancoratges lliures.' },
        { key: 'zaragoza', name: 'Saragossa', system: 'Bizi', description: 'Revisa Bizi en temps real i obre preferides ràpid.' },
      ],
      moreLabel: 'Més ciutats compatibles properament',
    },
    midCta: {
      title: 'Dos accessos ràpids segons la teva plataforma',
      description:
        'Descarrega BiciRadar ara a iPhone o Android. Si ho prefereixes, més avall t’enviarem l’enllaç per correu.',
      primaryCta: { label: 'Descarrega a l’App Store', note: 'Disponible ara a iPhone' },
      secondaryCta: { label: 'Descarrega a Google Play', note: 'Disponible ara a Android' },
    },
    betaForm: {
      title: 'T’ho enviem per correu',
      description: 'Deixa’ns el teu correu i t’enviarem l’enllaç de descàrrega per a la teva plataforma.',
      honeypotLabel: 'Empresa',
      consentLabel:
        'Accepto que BiciRadar faci servir aquestes dades per enviar-me l’enllaç de descàrrega i contactar-me sobre aquesta sol·licitud.',
      consentHint: 'Només fem servir la informació per enviar-te l’enllaç i ajudar-te amb aquesta sol·licitud.',
      submitLabel: 'Enviar enllaç',
      loadingLabel: 'Enviant enllaç...',
      successTitle: 'Enllaç enviat',
      successMessage: 'T’enviarem l’enllaç per correu d’aquí a uns minuts.',
      errorMessage: 'No hem pogut enviar l’enllaç. Torna-ho a provar en uns segons.',
      options: {
        operatingSystems,
      },
      fields: {
        email: { label: 'Email', placeholder: 'tu@email.com' },
        operatingSystem: { label: 'Sistema operatiu', placeholder: 'Selecciona el teu sistema operatiu' },
      },
      validation: {
        required: 'Completa aquest camp.',
        email: 'Introdueix un email vàlid.',
        consent: 'Necessitem el teu consentiment per enviar-te l’enllaç.',
        turnstile: 'Completa la verificació de seguretat i torna-ho a provar.',
        server: 'Hi ha hagut un problema en enviar l’enllaç.',
      },
    },
    faq: {
      title: 'FAQ',
      items: [
        { question: 'Si ja tinc l’app oficial, què m’aporta BiciRadar?', answer: 'Rapidesa per al que més consultes: estació més propera, bicis disponibles, ancoratges lliures, preferides, estat i ruta.' },
        { question: 'Realment estalvia temps?', answer: 'Sí. Inclou veu i dreceres a iPhone, Apple Watch i Android per resoldre consultes amb menys passos.' },
        { question: 'Només serveix al mòbil?', answer: 'No. Està pensada per a mòbil i rellotge: Android, iOS, Wear OS i Apple Watch.' },
        { question: 'Les dades són fiables?', answer: 'Fa servir fonts oficials d’open data de les ciutats. Pot haver-hi petits desfasaments típics del temps real.' },
        { question: 'Per què fer servir BiciRadar com a complement?', answer: 'L’app oficial és el canal institucional; BiciRadar està optimitzada per consultar més ràpid i amb menys fricció.' },
        { question: 'Què fareu amb les dades del formulari?', answer: 'Farem servir el teu correu per enviar-te els enllaços de descàrrega d’App Store o Google Play i, si cal, escriure’t sobre aquesta sol·licitud. No el farem servir per a cap altre ús.' },
      ],
    },
    footer: {
      tagline: '',
      links: [
        { label: 'Privacitat', href: 'https://gcaguilar.github.io/biciradar-privacy-policy/' },
        { label: 'Contacte', href: 'mailto:hola@biciradar.es?subject=BiciRadar%20app' },
        { label: 'Suport', href: 'mailto:soporte@biciradar.es?subject=Suport%20BiciRadar' },
      ],
      primaryCta: 'T’ho enviem per correu',
      note: 'BiciRadar · iPhone i Android · Dades basades en fonts oficials',
      githubLine: 'Codi obert a GitHub',
    },
  },
  thankYou: {
    seo: {
      title: 'Gràcies per demanar l’enllaç de BiciRadar',
      description:
        'La teva sol·licitud s’ha enviat correctament. Explora l’app, les ciutats disponibles o comparteix-la.',
    },
    badge: 'Sol·licitud enviada',
    title: 'Gràcies, t’enviarem l’enllaç',
    description:
      'T’enviarem per correu l’enllaç de descàrrega. Mentrestant, pots instal·lar BiciRadar ara o explorar les pàgines locals de cada ciutat.',
    installStepByOs: {
      ios: 'A l’iPhone: també pots descarregar BiciRadar ara mateix des de l’App Store.',
      android: 'A Android: revisa el correu que t’enviarem amb l’enllaç a Google Play.',
      both: 'Segons el mòbil: App Store a l’iPhone o Google Play a Android. També t’enviarem els enllaços per email.',
    },
    steps: [
      'Explora les pàgines locals per veure disponibilitat en temps real.',
      'Per dubtes, escriu a hola@biciradar.es.',
    ],
    cityLinksTitle: 'Explora ciutats disponibles mentrestant',
    cityCardCtaPrefix: 'Veure',
    shareLabel: 'Compartir BiciRadar',
    primaryCta: 'Explora ciutats disponibles mentrestant',
    appStoreCta: 'Descarregar a l’App Store',
    playStoreCta: 'Descarregar a Google Play',
    footnote: 'Preguntes? hola@biciradar.es',
  },
  cityPages: {
    madrid: {
      seo: { title: 'BiciMAD en temps real amb BiciRadar | Madrid', description: 'Consulta BiciMAD abans de sortir, evita estacions buides i descarrega BiciRadar per a Madrid.' },
      badge: 'Madrid · BiciMAD',
      title: 'Consulta BiciMAD en temps real abans de sortir',
      description: 'Comprova bicis i ancoratges en estacions clau de Madrid i evita trajectes innecessaris cap a bases buides o plenes.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Evita estacions buides', description: 'Decideix abans d’anar a una base de BiciMAD.' },
        { title: 'Planifica trajectes urbans', description: 'Combina preferides i avisos en els teus recorreguts diaris.' },
        { title: 'Rep avisos útils', description: 'Vigila zones clau quan necessites tornar o agafar bici ràpid.' },
      ],
      faqTitle: 'FAQ Madrid',
      faq: [
        { question: 'Funciona amb BiciMAD?', answer: 'Sí. La pàgina queda preparada per a contingut específic de BiciMAD i demanda local.' },
        { question: 'També puc fer servir BiciRadar a Android a Madrid?', answer: 'Sí. Ja és a Google Play i, si vols, també t’enviarem l’enllaç per correu.' },
      ],
      mockup: { stationLabel: 'Estació Sol', availability: '12 bicis · 5 ancoratges' },
    },
    barcelona: {
      seo: { title: 'Bicing en temps real amb BiciRadar | Barcelona', description: 'Consulta Bicing abans de moure’t, desa preferides i descarrega BiciRadar per a Barcelona.' },
      badge: 'Barcelona · Bicing',
      title: 'Consulta Bicing abans d’arribar a l’estació',
      description: 'Veu bicis i ancoratges lliures a Barcelona per decidir si et convé mantenir ruta, canviar de base o esperar.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Menys voltes en hora punta', description: 'Consulta Bicing abans de sortir del metro o de l’oficina.' },
        { title: 'Preferides per a la teva rutina', description: 'Tingues a mà estacions prop de casa, feina o universitat.' },
        { title: 'Widgets per a consultes ràpides', description: 'Obre l’estat de Bicing sense entrar a tota l’app.' },
      ],
      faqTitle: 'FAQ Barcelona',
      faq: [
        { question: 'Puc fer-lo servir amb Bicing si ja tinc l’app oficial?', answer: 'Sí. BiciRadar complementa l’ús diari amb consultes i accessos més ràpids.' },
        { question: 'Barcelona també està disponible a l’app?', answer: 'Sí. Barcelona ja té contingut local i continuarem prioritzant millores segons la demanda.' },
      ],
      mockup: { stationLabel: 'Pg. de Gràcia', availability: '8 bicis · 4 ancoratges' },
    },
    sevilla: {
      seo: { title: 'Sevici en temps real amb BiciRadar | Sevilla', description: 'Anticipa disponibilitat de Sevici, desa estacions preferides i descarrega BiciRadar a Sevilla.' },
      badge: 'Sevilla · Sevici',
      title: 'Consulta Sevici en temps real abans de moure’t',
      description: 'Comprova disponibilitat a Sevilla i evita arribar sense bicis o sense ancoratges quan necessites decidir ràpid.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Menys incertesa en sortir', description: 'Consulta Sevici just abans de començar el trajecte.' },
        { title: 'Accés ràpid a preferides', description: 'Tingues a mà les estacions clau per a casa, feina o estudi.' },
        { title: 'Avisos per tornar millor', description: 'Rep senyals útils quan tornin a quedar ancoratges lliures.' },
      ],
      faqTitle: 'FAQ Sevilla',
      faq: [
        { question: 'Sevici ja està contemplat a BiciRadar?', answer: 'Sí. Aquesta landing ja està preparada per captar interès local de Sevilla amb SEO propi.' },
        { question: 'Puc deixar el meu correu per rebre l’enllaç?', answer: 'Sí. T’enviarem l’enllaç de descàrrega i, a més, ens ajudarà a saber des de quina ciutat ens visites.' },
      ],
      mockup: { stationLabel: 'Puerta Jerez', availability: '6 bicis · 7 ancoratges' },
    },
    valencia: {
      seo: { title: 'Valenbisi en temps real amb BiciRadar | València', description: 'Consulta Valenbisi, evita estacions completes i descarrega BiciRadar per a València.' },
      badge: 'València · Valenbisi',
      title: 'Consulta Valenbisi abans d’acostar-te a l’estació',
      description: 'Tingues visibilitat de bicis i ancoratges a València per no improvisar la devolució o la recollida sobre la marxa.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Evita estacions completes', description: 'Comprova ancoratges lliures abans d’arribar al destí.' },
        { title: 'Millora la primera decisió', description: 'Veu quina és la millor base propera amb menys fricció.' },
        { title: 'Desa trajectes repetits', description: 'Repeteix consultes clau amb preferides, avisos i widgets.' },
      ],
      faqTitle: 'FAQ València',
      faq: [
        { question: 'BiciRadar serveix per a Valenbisi?', answer: 'Sí. Està preparada per a pàgines i campanyes específiques de València.' },
        { question: 'També està disponible a Android?', answer: 'Sí. Ja la pots descarregar des de Google Play o demanar-nos que t’enviem l’enllaç per correu.' },
      ],
      mockup: { stationLabel: 'Colón', availability: '10 bicis · 2 ancoratges' },
    },
    zaragoza: {
      seo: { title: 'Bizi en temps real amb BiciRadar | Saragossa', description: 'Consulta Bizi a Saragossa, obre preferides i descarrega BiciRadar amb contingut local.' },
      badge: 'Saragossa · Bizi',
      title: 'Consulta Bizi en temps real abans de pedalar',
      description: 'Accedeix a la disponibilitat de Bizi a Saragossa, desa estacions habituals i redueix el temps de consulta diària.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Consulta ràpida de Bizi', description: 'Obre l’estat d’estacions freqüents en segons.' },
        { title: 'Contingut local preparat per a SEO', description: 'Cada ciutat té copy, FAQ i metadades pròpies.' },
        { title: 'Escalable per ciutat', description: 'Recollim demanda de Saragossa sense duplicar l’arquitectura.' },
      ],
      faqTitle: 'FAQ Saragossa',
      faq: [
        { question: 'Saragossa ja està inclosa?', answer: 'Sí. Ja és una de les pàgines locals inicials amb accés directe a l’app.' },
        { question: 'Puc compartir aquesta pàgina amb altra gent de Saragossa?', answer: 'Sí. T’animem a compartir-la per mesurar millor la demanda local.' },
      ],
      mockup: { stationLabel: 'Plaça Espanya', availability: '7 bicis · 6 ancoratges' },
    },
  },
} satisfies LocaleContent;

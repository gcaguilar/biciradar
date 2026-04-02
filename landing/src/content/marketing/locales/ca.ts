import type { LocaleContent } from '../types';

const operatingSystems = [
  { value: 'ios', label: 'iPhone / iOS' },
  { value: 'android', label: 'Android' },
  { value: 'both', label: 'Faig servir tots dos' },
];

const frequencies = [
  { value: 'daily', label: 'Cada dia' },
  { value: 'weekly', label: 'Diverses vegades per setmana' },
  { value: 'occasional', label: 'De tant en tant' },
];

const interests = [
  { value: 'bikes', label: 'Trobar bicis' },
  { value: 'docks', label: 'Trobar ancoratges lliures' },
  { value: 'alerts', label: 'Rebre avisos' },
  { value: 'widgets', label: 'Fer servir widgets i accessos ràpids' },
];

const yesNo = [
  { value: 'yes', label: 'Sí' },
  { value: 'no', label: 'No' },
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
      'Mira disponibilitat en sistemes de bici compartida, desa preferides i demana accés a la beta de BiciRadar.',
  },
  common: {
    skipToContent: 'Saltar al contingut',
    appStoreLabel: 'Descarrega a l’App Store',
    androidBetaLabel: 'Sol·licita accés beta',
    openMenu: 'Obrir navegació',
    closeMenu: 'Tancar navegació',
    backToHome: 'Tornar a la portada',
    viewCities: 'Veure ciutats',
    cityPageCta: 'Veure pàgina local',
    heroCta: 'Entrar a la beta',
    heroSecondaryCta: 'Veure com funciona',
    finalCtaTitle: 'Consulta abans de pedalar',
    finalCtaDescription:
      'Demana accés a la beta i ajuda’ns a prioritzar noves ciutats amb demanda real.',
    cityRevisitLabel: 'Tornar a veure ciutats',
    shareDescription: 'Comparteix la beta amb una altra persona de la teva ciutat.',
    betaInviteLabel: 'Accés per invitació pública',
    faqLabel: 'Preguntes freqüents',
    thankYouShareFallback: 'Enllaç copiat. Ja el pots compartir.',
  },
  home: {
    seo: {
      title: 'BiciRadar beta | Consulta bicis i ancoratges en temps real abans d’arribar',
      description:
        'BiciRadar t’ajuda a veure disponibilitat a BiciMAD, Bicing, Sevici, Valenbisi, Bizi i més. Desa preferides, rep avisos i entra a la beta.',
    },
    header: {
      sections: [
        { id: 'problema', label: 'Problema' },
        { id: 'solucio', label: 'Solució' },
        { id: 'com-funciona', label: 'Com funciona' },
        { id: 'ciutats', label: 'Ciutats' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Entrar a la beta',
      languageLabel: 'Idioma',
    },
    hero: {
      title: 'Consulta bicis i ancoratges en temps real abans d’arribar',
      description:
        'BiciRadar t’ajuda a veure disponibilitat en sistemes de bici compartida com BiciMAD, Bicing, Sevici i més. Desa preferides, rep avisos i accedeix ràpid des de widgets.',
      primaryCta: 'Descarrega a l’App Store',
      secondaryCta: 'Sol·licita accés beta',
      microcopy: 'Sol·licita accés i farem servir el teu correu per gestionar la teva entrada a la beta d’Android.',
      primaryBadge: 'iPhone · Disponible ara',
      secondaryBadge: 'Android · Beta tancada',
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
        'La beta està pensada per captar demanda ara i escalar invitacions per plataforma i ciutat.',
      primaryCta: { label: 'Descarrega a l’App Store', note: 'Disponible ara a iPhone' },
      secondaryCta: { label: 'Sol·licita accés beta', note: 'Android en accés controlat' },
    },
    betaForm: {
      title: 'Accés Android beta',
      description:
        'Farem servir el teu email per gestionar l’accés a la beta i prioritzar ciutats amb més demanda.',
      helper:
        'També ens ajuda a mesurar interès per sistema, plataforma, widgets i freqüència d’ús.',
      honeypotLabel: 'Empresa',
      consentLabel:
        'Accepto que BiciRadar faci servir aquestes dades per gestionar el meu accés a la beta i contactar-me sobre aquesta sol·licitud.',
      consentHint: 'Només fem servir la informació per al procés beta.',
      submitLabel: 'Sol·licitar accés beta',
      loadingLabel: 'Enviant sol·licitud...',
      successTitle: 'Sol·licitud enviada',
      successMessage: 'Hem desat la teva sol·licitud. T’avisarem quan obrim el teu accés.',
      errorMessage: 'No hem pogut enviar la teva sol·licitud. Torna-ho a provar en uns segons.',
      options: {
        operatingSystems,
        cities: [
          { value: 'madrid', label: 'Madrid' },
          { value: 'barcelona', label: 'Barcelona' },
          { value: 'sevilla', label: 'Sevilla' },
          { value: 'valencia', label: 'València' },
          { value: 'zaragoza', label: 'Saragossa' },
          { value: 'other', label: 'Una altra ciutat' },
        ],
        systems: [
          { value: 'bicimad', label: 'BiciMAD' },
          { value: 'bicing', label: 'Bicing' },
          { value: 'sevici', label: 'Sevici' },
          { value: 'valenbisi', label: 'Valenbisi' },
          { value: 'bizi', label: 'Bizi' },
          { value: 'other', label: 'Un altre sistema' },
        ],
        frequencies,
        interests,
        yesNo,
      },
      fields: {
        email: { label: 'Email', placeholder: 'tu@email.com' },
        operatingSystem: { label: 'Sistema operatiu', placeholder: 'Selecciona el teu sistema operatiu' },
        city: { label: 'Ciutat', placeholder: 'Selecciona la teva ciutat principal' },
        bikeSystem: { label: 'Sistema de bici', placeholder: 'Selecciona el sistema que fas servir més' },
        frequency: { label: 'Freqüència d’ús', placeholder: 'Amb quina freqüència fas servir bici compartida?' },
        interest: { label: 'Interès principal', placeholder: 'Què t’interessa més de BiciRadar?' },
        widgets: { label: 'T’interessen els widgets?', placeholder: 'Selecciona una opció' },
        smartwatch: { label: 'Fas servir smartwatch?', placeholder: 'Selecciona una opció' },
      },
      validation: {
        required: 'Completa aquest camp.',
        email: 'Introdueix un email vàlid.',
        consent: 'Necessitem el teu consentiment per gestionar la beta.',
        server: 'Hi ha hagut un problema en desar la teva sol·licitud.',
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
      ],
    },
    footer: {
      tagline: 'Beta de bici compartida orientada a conversió, SEO local i escalat per ciutat.',
      links: [
        { label: 'Privacitat', href: 'mailto:hola@biciradar.es?subject=Privacitat%20beta' },
        { label: 'Contacte', href: 'mailto:hola@biciradar.es?subject=BiciRadar%20beta' },
        { label: 'Suport', href: 'mailto:soporte@biciradar.es?subject=Suport%20BiciRadar' },
      ],
      primaryCta: 'Entrar a la beta',
      note: 'BiciRadar · iPhone i Android · Dades basades en fonts oficials',
    },
  },
  thankYou: {
    seo: {
      title: 'Gràcies per apuntar-te a la beta de BiciRadar',
      description:
        'La teva sol·licitud per a la beta s’ha enviat correctament. Revisa els pròxims passos, comparteix la beta i visita pàgines locals.',
    },
    badge: 'Sol·licitud enviada',
    title: 'Gràcies. La teva sol·licitud per a Android beta s’ha enviat correctament.',
    description:
      'Revisarem la teva sol·licitud i farem servir el teu email per gestionar l’accés a la beta i prioritzar ciutats amb més demanda.',
    steps: [
      'Confirmem la teva sol·licitud.',
      'Prioritzem invitacions segons ciutat, sistema i plataforma.',
      'Reps instruccions per email quan hi hagi accés disponible.',
    ],
    cityLinksTitle: 'Explora ciutats disponibles mentrestant',
    shareLabel: 'Compartir la beta',
    primaryCta: 'Veure ciutats disponibles',
    secondaryCta: 'Descarregar a l’App Store',
    footnote: 'No reps cap email? Escriu-nos a suport.',
  },
  cityPages: {
    madrid: {
      seo: { title: 'BiciMAD en temps real amb BiciRadar | Beta Madrid', description: 'Consulta BiciMAD abans de sortir, evita estacions buides i demana accés a la beta de BiciRadar per a Madrid.' },
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
        { question: 'Madrid també entra a la beta d’Android?', answer: 'Sí. Pots demanar accés i convidarem usuaris per fases.' },
      ],
      mockup: { stationLabel: 'Estació Sol', availability: '12 bicis · 5 ancoratges' },
    },
    barcelona: {
      seo: { title: 'Bicing en temps real amb BiciRadar | Beta Barcelona', description: 'Consulta Bicing abans de moure’t, desa preferides i demana accés a la beta de BiciRadar per a Barcelona.' },
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
        { question: 'Barcelona tindrà invitacions beta?', answer: 'Sí. Estem recollint demanda per prioritzar accés per ciutat.' },
      ],
      mockup: { stationLabel: 'Pg. de Gràcia', availability: '8 bicis · 4 ancoratges' },
    },
    sevilla: {
      seo: { title: 'Sevici en temps real amb BiciRadar | Beta Sevilla', description: 'Anticipa disponibilitat de Sevici, desa estacions preferides i demana accés a la beta de BiciRadar a Sevilla.' },
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
        { question: 'Sevici està previst a la beta?', answer: 'Sí. La landing ja queda preparada per captar demanda local de Sevilla amb SEO propi.' },
        { question: 'Puc deixar les meves dades encara que no hi hagi invitació immediata?', answer: 'Sí. Guardem el teu interès per prioritzar obertures futures per ciutat.' },
      ],
      mockup: { stationLabel: 'Puerta Jerez', availability: '6 bicis · 7 ancoratges' },
    },
    valencia: {
      seo: { title: 'Valenbisi en temps real amb BiciRadar | Beta València', description: 'Consulta Valenbisi, evita estacions completes i demana accés a la beta de BiciRadar per a València.' },
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
        { question: 'També hi haurà accés beta per a Android?', answer: 'Sí. Pots apuntar-t’hi des d’aquesta pàgina i et contactarem per email.' },
      ],
      mockup: { stationLabel: 'Colón', availability: '10 bicis · 2 ancoratges' },
    },
    zaragoza: {
      seo: { title: 'Bizi en temps real amb BiciRadar | Beta Saragossa', description: 'Consulta Bizi a Saragossa, obre preferides i demana accés a la beta de BiciRadar amb contingut local.' },
      badge: 'Saragossa · Bizi',
      title: 'Consulta Bizi en temps real abans de pedalar',
      description: 'Accedeix a la disponibilitat de Bizi a Saragossa, desa estacions habituals i redueix el temps de consulta diària.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Consulta ràpida de Bizi', description: 'Obre l’estat d’estacions freqüents en segons.' },
        { title: 'Contingut local preparat per a SEO', description: 'Cada ciutat té copy, FAQ i metadades pròpies.' },
        { title: 'Beta escalable per ciutat', description: 'Recollim demanda de Saragossa sense duplicar l’arquitectura.' },
      ],
      faqTitle: 'FAQ Saragossa',
      faq: [
        { question: 'Saragossa està inclosa en la primera fase?', answer: 'Sí. Ja queda preparada com una de les pàgines locals inicials amb CTA a beta.' },
        { question: 'Puc compartir aquesta pàgina amb altra gent de Saragossa?', answer: 'Sí. T’animem a compartir-la per mesurar millor la demanda local.' },
      ],
      mockup: { stationLabel: 'Plaça Espanya', availability: '7 bicis · 6 ancoratges' },
    },
  },
} satisfies LocaleContent;

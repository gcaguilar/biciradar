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
    siteTagline: 'La teva estació de bici, en un cop d’ull',
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
    heroCta: 'Descarrega ara',
    heroSecondaryCta: 'Veure com funciona',
    cityHeroMessage:
      'Obres l’app, mires la teva estació i en un moment saps si et convé sortir o canviar de pla.',
    citySeoTitleSuffix: 'BiciRadar',
    citySeoDescriptionSuffix:
      'Consulta la teva estació abans de sortir des del mòbil o el rellotge.',
    finalCtaTitle: 'Descarrega-la gratis i prova-la',
    finalCtaDescription:
      'Guarda les teves estacions de sempre i des del primer dia decideixes abans de sortir. Sense compte per començar i amb dades obertes de cada xarxa.',
    cityRevisitLabel: 'Tornar a veure ciutats',
    shareDescription: 'Comparteix BiciRadar amb una altra persona de la teva ciutat.',
    betaInviteLabel: 'Descàrrega directa',
    faqLabel: 'Preguntes freqüents',
    thankYouShareFallback: 'Enllaç copiat. Ja el pots compartir.',
    githubAriaLabel: 'Codi font a GitHub',
  },
  home: {
    seo: {
      title: 'BiciRadar | Bicis i ancoratges de la teva estació abans de sortir',
      description:
        'Mira si hi ha bicis o ancoratges lliures a la teva estació de bici pública abans de sortir. Preferides, widgets, rellotge i avisos. Gratis a iPhone i Android.',
    },
    header: {
      sections: [
        { id: 'avantatges', label: 'Avantatges' },
        { id: 'app', label: 'L’app' },
        { id: 'com-funciona', label: 'Com funciona' },
        { id: 'ciutats', label: 'Ciutats' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Descarrega',
      languageLabel: 'Idioma',
    },
    hero: {
      title: 'Hi ha bici a la teva estació? Mira-ho abans de sortir.',
      description:
        'BiciRadar t’ensenya les bicis i els ancoratges lliures de la teva bici pública. Les teves estacions de sempre, a un toc des del mòbil, un widget o el rellotge.',
      primaryCta: 'Descarrega a l’App Store',
      secondaryCta: 'Descarrega a Google Play',
      microcopy: 'Gratis · Sense compte per començar · iPhone i Android',
      trust: ['Més de 40 ciutats', 'Apple Watch i Wear OS', 'Widgets i veu'],
      screenshotAlt: 'Mapa de BiciRadar amb l’estació més propera, les seves bicis i ancoratges lliures',
      mockup: {
        stationLabel: 'Estació Sol',
        availability: '12 bicis · 5 ancoratges',
        bikesLabel: 'Bicis a punt',
        docksLabel: 'Ancoratges lliures',
        chips: ['Preferides', 'Avisos', 'Widgets'],
        shortcutLabel: 'Obrir trajecte del matí',
      },
    },
    solution: {
      title: 'Feta per saber què hi ha abans de baixar al carrer',
      intro:
        'Arribar a una estació buida, o sense cap ancoratge lliure, és una pèrdua de temps evitable. Això és el que BiciRadar et posa fàcil.',
      cards: [
        {
          icon: 'pin',
          title: 'L’estació útil, tot just obrir l’app',
          description:
            'Veus la més propera amb bicis i la més propera amb ancoratges, amb distància i ruta a un toc. Si vas a peu, t’ensenya les que tenen bicis; si vas en bici, les que tenen ancoratges.',
        },
        {
          icon: 'sparkles',
          title: 'Filtra i ordena com et vagi millor',
          description:
            'Per distància, per bicis, per ancoratges o per la disponibilitat que sol haver-hi a aquella hora. On la xarxa ho publica, també elèctriques o mecàniques.',
        },
        {
          icon: 'star',
          title: 'Casa, Feina i preferides',
          description:
            'Fixa les teves estacions de sempre i obre-les amb un toc, sense buscar-les cada cop.',
        },
        {
          icon: 'bell',
          title: 'Avisos de les teves estacions',
          description:
            'Alertes per a Casa, Feina i preferides. Amb un compte (opcional), les rutines vigilen la teva estació abans de sortir i et proposen una alternativa propera.',
        },
        {
          icon: 'phone',
          title: 'En widgets i al rellotge',
          description:
            'Widgets al mòbil i app per a Apple Watch i Wear OS, per mirar la teva estació sense treure el telèfon.',
        },
        {
          icon: 'globe',
          title: 'Pregunta-li al teu assistent',
          description:
            'Amb Siri, Dreceres o Google Assistant pots preguntar si hi ha bicis o ancoratges a la teva estació.',
        },
      ],
      note: 'BiciRadar mostra la disponibilitat per ajudar-te a decidir; no gestiona lloguers ni pagaments.',
    },
    showcase: {
      title: 'Així es veu per dins',
      intro: 'Captures reals de l’app, sense adorns.',
      items: [
        {
          key: 'map',
          title: 'Mapa en directe',
          description: 'Bicis i ancoratges d’un cop d’ull, amb l’hora de l’última actualització. Filtra per només bicis o només ancoratges.',
          alt: 'Mapa amb estacions i filtres Bicis + Ancoratges, Només bicis i Només ancoratges',
        },
        {
          key: 'nearby',
          title: 'A prop teu',
          description: 'L’estació més propera amb bicis i la més propera amb ancoratges, i ruta amb un toc.',
          alt: 'Pestanya A prop amb l’estació més propera amb bicis i amb ancoratges',
        },
        {
          key: 'favorites',
          title: 'Preferides, Casa i Feina',
          description: 'Les teves estacions desades i la gestió d’alertes, en un únic lloc.',
          alt: 'Pestanya Preferides amb alertes d’estacions i els accessos Casa i Feina',
        },
        {
          key: 'detail',
          title: 'Detall d’estació',
          description: 'Distància, mapa i desar com a preferida, Casa o Feina amb un toc.',
          alt: 'Detall d’una estació amb distància, mapa i botons per desar-la',
        },
      ],
    },
    howItWorks: {
      title: 'Començar porta un minut',
      steps: [
        {
          number: '1',
          title: 'Descarrega i tria la teva ciutat',
          description: 'Selecciona la teva xarxa de bici pública. No cal crear un compte.',
        },
        {
          number: '2',
          title: 'Mira què tens a prop',
          description: 'Amb la teva ubicació, l’app t’ensenya quina estació et convé ara mateix.',
        },
        {
          number: '3',
          title: 'Desa les teves estacions',
          description: 'Fixa Casa, Feina i preferides i decideix en segons, des del mòbil o el rellotge.',
        },
      ],
    },
    cities: {
      title: 'Ciutats i sistemes',
      description:
        'Comença per una d’aquestes ciutats; dins de l’app trobaràs més xarxes de bici pública.',
      items: [
        { key: 'madrid', name: 'Madrid', system: 'BiciMAD', description: 'Consulta BiciMAD abans de sortir.' },
        { key: 'barcelona', name: 'Barcelona', system: 'Bicing', description: 'Troba una estació útil de Bicing quan tens pressa.' },
        { key: 'sevilla', name: 'Sevilla', system: 'Sevici', description: 'Anticipa disponibilitat de Sevici en trajectes diaris.' },
        { key: 'valencia', name: 'València', system: 'Valenbisi', description: 'Evita voltes innecessàries buscant ancoratges lliures.' },
        { key: 'zaragoza', name: 'Saragossa', system: 'Bizi', description: 'Revisa Bizi en temps real i obre preferides ràpid.' },
      ],
      moreLabel: 'I més ciutats dins de l’app: tria la teva en obrir-la.',
      otherCitiesLabel: 'Pàgines d’altres ciutats',
    },
    midCta: {
      title: 'Abans de sortir, mira la teva estació.',
      description:
        'Descarrega BiciRadar gratis, tria la teva ciutat i tingues les teves estacions a un toc.',
      reassurance: 'Gratis · Sense compte per començar',
      primaryCta: {
        label: 'Descarrega a l’App Store',
        note: 'Per a iPhone',
      },
      secondaryCta: {
        label: 'Descarrega a Google Play',
        note: 'Per a Android',
      },
    },
    betaForm: {
      title: 'Tria la teva plataforma i descarrega',
      description: 'Instal·la BiciRadar directament des de l’App Store o Google Play.',
      honeypotLabel: 'Empresa',
      consentLabel:
        'Accepto que BiciRadar faci servir aquestes dades per enviar-me l’enllaç de descàrrega i contactar-me sobre aquesta sol·licitud.',
      consentHint: 'Només fem servir la informació per enviar-te l’enllaç i ajudar-te amb aquesta sol·licitud.',
      submitLabel: 'Enviar enllaç',
      loadingLabel: 'Enviant enllaç...',
      successTitle: 'Enllaç enviat',
      successMessage: 'Pots descarregar BiciRadar ara mateix des de la teva botiga.',
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
      title: 'Preguntes freqüents',
      items: [
        {
          question: 'És gratis? Necessito crear un compte?',
          answer:
            'Es descarrega i s’usa gratis. Pots consultar estacions, desar preferides i usar widgets sense compte. El compte és opcional i només cal per a les rutines i el seguiment de trajectes, que es vigilen des del nostre servidor.',
        },
        {
          question: 'Substitueix l’app oficial de la meva ciutat?',
          answer:
            'No. BiciRadar serveix per consultar disponibilitat i decidir abans de sortir; no gestiona lloguers ni pagaments. Pots fer-la servir juntament amb l’app del teu sistema.',
        },
        {
          question: 'Les dades són en temps real?',
          answer:
            'Fem servir dades obertes que publica cada sistema i s’actualitzen amb freqüència, però el ritme depèn de cada proveïdor. A cada estació veus fa quant es va actualitzar.',
        },
        {
          question: 'Necessita la meva ubicació?',
          answer:
            'La fa servir per dir-te quina és la teva estació més propera. Si no dónes permís, pots seguir usant l’app, però alguns resultats seran menys precisos.',
        },
        {
          question: 'Funciona al rellotge i amb la veu?',
          answer:
            'Sí. Hi ha app per a Apple Watch i Wear OS, widgets a iPhone i Android, i pots preguntar amb Siri, Dreceres o Google Assistant.',
        },
        {
          question: 'Quines ciutats cobreix?',
          answer:
            'Madrid, Barcelona, Sevilla, València, Saragossa, Bilbao i moltes més. En obrir l’app tries la teva ciutat; si la teva té pàgina pròpia, la trobaràs a la secció de ciutats.',
        },
        {
          question: 'Què passa amb les meves preferides i les meves dades?',
          answer:
            'Les teves preferides i els llocs desats es queden al teu dispositiu. Tens més detall a la política de privacitat, enllaçada al final de la pàgina.',
        },
      ],
    },
    footer: {
      tagline: '',
      links: [
        { label: 'Privacitat', href: 'https://gcaguilar.github.io/biciradar-privacy-policy/' },
        { label: 'Contacte', href: 'mailto:hola@biciradar.es?subject=BiciRadar%20app' },
        { label: 'Suport', href: 'mailto:soporte@biciradar.es?subject=Suport%20BiciRadar' },
      ],
      primaryCta: 'Descarrega ara',
      note: 'BiciRadar · iPhone i Android · Dades obertes de cada sistema',
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
      'Pots instal·lar BiciRadar ara mateix o explorar les pàgines locals de cada ciutat.',
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
        { title: 'Decideix abans de baixar al carrer', description: 'Obre BiciMAD i confirma si la teva base habitual et compensa abans de sortir.' },
        { title: 'Evita canvis d’estació a última hora', description: 'Si aquella base no encaixa, en tries una altra abans de perdre temps ja en marxa.' },
        { title: 'Resol el dubte d’un cop d’ull', description: 'Preferides, avisos i accessos ràpids et donen la resposta sense fricció.' },
      ],
      faqTitle: 'FAQ Madrid',
      faq: [
        { question: 'Què em resol BiciRadar amb BiciMAD abans de sortir?', answer: 'T’ajuda a saber si la teva base habitual té bicis o ancoratges i si et convé baixar ara, esperar o canviar d’estació.' },
        { question: 'Puc veure BiciMAD ràpid des del mòbil o el rellotge?', answer: 'Sí. Preferides, widgets i accessos ràpids et deixen resoldre la disponibilitat en segons, sense obrir tota l’app.' },
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
        { title: 'Decideix en sortir del metro', description: 'Consulta Bicing abans de pujar al carrer o sortir de l’oficina.' },
        { title: 'Evita voltes en hora punta', description: 'Si aquella base no et convé, canvies d’estació abans de fer el desviament.' },
        { title: 'Mantén la rutina a un toc', description: 'Les teves estacions clau queden a punt per resoldre el dubte en segons.' },
      ],
      faqTitle: 'FAQ Barcelona',
      faq: [
        { question: 'Què em resol BiciRadar amb Bicing abans d’arribar?', answer: 'T’ajuda a saber si aquella estació et compensa o si és millor canviar de base abans de fer el desviament.' },
        { question: 'I si ja miro Bicing a l’app oficial?', answer: 'BiciRadar redueix passos just quan tens pressa: obres preferides, veus disponibilitat i prens la decisió abans.' },
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
        { title: 'Surt amb menys incertesa', description: 'Consulta Sevici just abans d’arrencar i decideix amb context.' },
        { title: 'Tingues la base clau a mà', description: 'Les teves estacions habituals queden a punt per resoldre el dubte en segons.' },
        { title: 'Torna millor sense improvisar', description: 'Rep senyals útils quan tornen a aparèixer ancoratges on els necessites.' },
      ],
      faqTitle: 'FAQ Sevilla',
      faq: [
        { question: 'Quina decisió m’ajuda a prendre amb Sevici abans de moure’m?', answer: 'Si et convé anar a la teva estació habitual, esperar uns minuts o canviar de base abans de perdre temps en marxa.' },
        { question: 'Puc tenir Sevici a mà sense entrar a tota l’app?', answer: 'Sí. Preferides, widgets i accessos ràpids et deixen veure la disponibilitat i actuar en segons.' },
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
        { title: 'Evita arribar a una base plena', description: 'Comprova ancoratges lliures abans d’acostar-te al destí i decideix millor.' },
        { title: 'Tria millor la primera estació', description: 'Veus quina base et convé per agafar o deixar bici sense improvisar.' },
        { title: 'Resol la rutina sense començar de zero', description: 'Preferides, avisos i accessos ràpids t’estalvien passos a cada trajecte.' },
      ],
      faqTitle: 'FAQ València',
      faq: [
        { question: 'Què em resol BiciRadar amb Valenbisi abans d’acostar-me a l’estació?', answer: 'T’ajuda a decidir si aquella base et convé per agafar o tornar bici abans d’improvisar sobre la marxa.' },
        { question: 'Ho puc veure ràpid a iPhone o Android?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
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
        { title: 'Decideix abans de pedalar', description: 'Comprova si la teva estació habitual té disponibilitat abans de sortir.' },
        { title: 'Evita rodejos entre bases', description: 'Si aquella estació no et compensa, canvies de pla abans de perdre temps.' },
        { title: 'Consulta Bizi en segons', description: 'Preferides i accessos ràpids et deixen resoldre el dubte gairebé a l’instant.' },
      ],
      faqTitle: 'FAQ Saragossa',
      faq: [
        { question: 'Què em resol BiciRadar amb Bizi abans de pedalar?', answer: 'T’ajuda a saber si la teva estació habitual té bicis o ancoratges i si et compensa sortir ara o canviar de base.' },
        { question: 'Ho puc compartir amb algú de Saragossa?', answer: 'Sí. Si una altra persona té el mateix dubte diari, aquesta pàgina i l’app l’ajuden a decidir abans de moure’s.' },
      ],
      mockup: { stationLabel: 'Plaça Espanya', availability: '7 bicis · 6 ancoratges' },
    },
    bilbao: {
      seo: { title: 'Bilbao en temps real amb BiciRadar | Bilbao', description: 'Consulta bicis i ancoratges a Bilbao abans de sortir i descarrega BiciRadar.' },
      badge: 'Bilbao · Bilbao',
      title: 'Consulta Bilbao en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Bilbao abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Bilbao',
      faq: [
        { question: 'Què em resol BiciRadar a Bilbao abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Bilbao', availability: '5 bicis · 3 ancoratges' },
    },
    murcia: {
      seo: { title: 'Múrcia en temps real amb BiciRadar | Múrcia', description: 'Consulta bicis i ancoratges a Múrcia abans de sortir i descarrega BiciRadar.' },
      badge: 'Múrcia · Murcia',
      title: 'Consulta Múrcia en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Múrcia abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Múrcia',
      faq: [
        { question: 'Què em resol BiciRadar a Múrcia abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Múrcia', availability: '4 bicis · 2 ancoratges' },
    },
    leon: {
      seo: { title: 'Lleó en temps real amb BiciRadar | Lleó', description: 'Consulta bicis i ancoratges a Lleó abans de sortir i descarrega BiciRadar.' },
      badge: 'Lleó · Lleó',
      title: 'Consulta Lleó en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Lleó abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Lleó',
      faq: [
        { question: 'Què em resol BiciRadar a Lleó abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Lleó', availability: '3 bicis · 2 ancoratges' },
    },
    valladolid: {
      seo: { title: 'Valladolid en temps real amb BiciRadar | Valladolid', description: 'Consulta bicis i ancoratges a Valladolid abans de sortir i descarrega BiciRadar.' },
      badge: 'Valladolid · Valladolid',
      title: 'Consulta Valladolid en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Valladolid abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Valladolid',
      faq: [
        { question: 'Què em resol BiciRadar a Valladolid abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Valladolid', availability: '4 bicis · 3 ancoratges' },
    },
    palma: {
      seo: { title: 'Palma en temps real amb BiciRadar | Palma', description: 'Consulta bicis i ancoratges a Palma abans de sortir i descarrega BiciRadar.' },
      badge: 'Palma · Palma',
      title: 'Consulta Palma en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Palma abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Palma',
      faq: [
        { question: 'Què em resol BiciRadar a Palma abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Palma', availability: '5 bicis · 4 ancoratges' },
    },
    las_palmas: {
      seo: { title: 'Las Palmas en temps real amb BiciRadar | Las Palmas', description: 'Consulta bicis i ancoratges a Las Palmas abans de sortir i descarrega BiciRadar.' },
      badge: 'Las Palmas · Las Palmas',
      title: 'Consulta Las Palmas en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Las Palmas abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Las Palmas',
      faq: [
        { question: 'Què em resol BiciRadar a Las Palmas abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Las Palmas', availability: '4 bicis · 3 ancoratges' },
    },
    a_coruna: {
      seo: { title: 'La Coruña en temps real amb BiciRadar | La Coruña', description: 'Consulta bicis i ancoratges a La Coruña abans de sortir i descarrega BiciRadar.' },
      badge: 'La Coruña · La Coruña',
      title: 'Consulta La Coruña en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a La Coruña abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ La Coruña',
      faq: [
        { question: 'Què em resol BiciRadar a La Coruña abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'La Coruña', availability: '3 bicis · 2 ancoratges' },
    },
    gijon: {
      seo: { title: 'Gijón en temps real amb BiciRadar | Gijón', description: 'Consulta bicis i ancoratges a Gijón abans de sortir i descarrega BiciRadar.' },
      badge: 'Gijón · Gijón',
      title: 'Consulta Gijón en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Gijón abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Gijón',
      faq: [
        { question: 'Què em resol BiciRadar a Gijón abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Gijón', availability: '4 bicis · 3 ancoratges' },
    },
    vitoria_gasteiz: {
      seo: { title: 'Vitòria-Gasteiz en temps real amb BiciRadar | Vitòria', description: 'Consulta bicis i ancoratges a Vitòria abans de sortir i descarrega BiciRadar.' },
      badge: 'Vitòria-Gasteiz · Vitòria',
      title: 'Consulta Vitòria-Gasteiz en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Vitòria abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Vitòria',
      faq: [
        { question: 'Què em resol BiciRadar a Vitòria abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Vitòria', availability: '3 bicis · 2 ancoratges' },
    },
    pamplona: {
      seo: { title: 'Pamplona en temps real amb BiciRadar | Pamplona', description: 'Consulta bicis i ancoratges a Pamplona abans de sortir i descarrega BiciRadar.' },
      badge: 'Pamplona · Pamplona',
      title: 'Consulta Pamplona en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Pamplona abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Pamplona',
      faq: [
        { question: 'Què em resol BiciRadar a Pamplona abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Pamplona', availability: '5 bicis · 4 ancoratges' },
    },
    castellon: {
      seo: { title: 'Castelló en temps real amb BiciRadar | Castelló', description: 'Consulta bicis i ancoratges a Castelló abans de sortir i descarrega BiciRadar.' },
      badge: 'Castelló · Castelló',
      title: 'Consulta Castelló en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Castelló abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Castelló',
      faq: [
        { question: 'Què em resol BiciRadar a Castelló abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Castelló', availability: '3 bicis · 2 ancoratges' },
    },
    santander: {
      seo: { title: 'Santander en temps real amb BiciRadar | Santander', description: 'Consulta bicis i ancoratges a Santander abans de sortir i descarrega BiciRadar.' },
      badge: 'Santander · Santander',
      title: 'Consulta Santander en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Santander abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Santander',
      faq: [
        { question: 'Què em resol BiciRadar a Santander abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Santander', availability: '4 bicis · 3 ancoratges' },
    },
    girona: {
      seo: { title: 'Girona en temps real amb BiciRadar | Girona', description: 'Consulta bicis i ancoratges a Girona abans de sortir i descarrega BiciRadar.' },
      badge: 'Girona · Girona',
      title: 'Consulta Girona en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Girona abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Girona',
      faq: [
        { question: 'Què em resol BiciRadar a Girona abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Girona', availability: '3 bicis · 2 ancoratges' },
    },
    gran_canaria: {
      seo: { title: 'Gran Canària en temps real amb BiciRadar | Gran Canària', description: 'Consulta bicis i ancoratges a Gran Canària abans de sortir i descarrega BiciRadar.' },
      badge: 'Gran Canària · Gran Canària',
      title: 'Consulta Gran Canària en temps real abans de sortir',
      description: 'Consulta bicis i ancoratges disponibles a Gran Canària abans de sortir.',
      benefitsTitle: 'Beneficis locals',
      benefits: [
        { title: 'Decideix abans de sortir', description: 'Comprova si la teva estació habitual té bicis o ancoratges abans de posar-te en marxa.' },
        { title: 'Evita desviaments innecessaris', description: 'Si aquella base no et compensa, canvies de pla abans de perdre temps al carrer.' },
        { title: 'Consulta-ho d’un cop d’ull', description: 'Obre preferides, widget o mòbil i resol el dubte en segons.' },
      ],
      faqTitle: 'FAQ Gran Canària',
      faq: [
        { question: 'Què em resol BiciRadar a Gran Canària abans de moure’m?', answer: 'T’ajuda a saber si la teva estació habitual té disponibilitat i si et convé sortir ara, esperar o canviar de base.' },
        { question: 'Ho puc veure ràpid des del mòbil o el rellotge?', answer: 'Sí. La disponibilitat queda a mà en preferides, widgets i accessos ràpids per decidir sense obrir tot el flux.' },
      ],
      mockup: { stationLabel: 'Gran Canària', availability: '4 bicis · 3 ancoratges' },
    },
  },
} satisfies LocaleContent;

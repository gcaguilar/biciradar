import type { LocaleContent } from '../types';

const operatingSystems = [
  { value: 'ios', label: 'iPhone / iOS' },
  { value: 'android', label: 'Android' },
  { value: 'both', label: 'Biak erabiltzen ditut' },
];

export const eu = {
  locale: 'eu',
  languageName: 'Euskara',
  localeLabel: 'EU',
  metadata: {
    siteName: 'BiciRadar',
    siteTagline: 'Kontsultatu bizikletak eta lekuak iritsi aurretik',
    defaultOgTitle: 'BiciRadar | Kontsultatu bizikletak eta leku libreak denbora errealean',
    defaultOgDescription:
      'Ikusi bizikleta partekatuaren sistemetako erabilgarritasuna, gorde gustukoak eta eskatu BiciRadar betarako sarbidea.',
  },
  common: {
    skipToContent: 'Joan edukira',
    appStoreLabel: 'Deskargatu App Storen',
    androidBetaLabel: 'Eskatu beta sarbidea',
    openMenu: 'Ireki nabigazioa',
    closeMenu: 'Itxi nabigazioa',
    backToHome: 'Itzuli hasierara',
    viewCities: 'Ikusi hiriak',
    cityPageCta: 'Ikusi tokiko orria',
    heroCta: 'Sartu betan',
    heroSecondaryCta: 'Ikusi nola dabilen',
    finalCtaTitle: 'Kontsultatu pedalei ekin aurretik',
    finalCtaDescription:
      'Eskatu betarako sarbidea eta lagundu hurrengo hiriak benetako eskariaren arabera lehenesten.',
    cityRevisitLabel: 'Berriz ikusi hiriak',
    shareDescription: 'Partekatu beta zure hiriko beste pertsona batekin.',
    betaInviteLabel: 'Gonbidapen publiko bidezko sarbidea',
    faqLabel: 'Ohiko galderak',
    thankYouShareFallback: 'Esteka kopiatu da. Orain parteka dezakezu.',
    githubAriaLabel: 'Iturburu kodea GitHub-en',
  },
  home: {
    seo: {
      title: 'BiciRadar beta | Kontsultatu bizikletak eta leku libreak denbora errealean iritsi aurretik',
      description:
        'BiciRadarrek BiciMAD, Bicing, Sevici, Valenbisi, Bizi eta gehiagoko erabilgarritasuna ikusten laguntzen dizu. Gorde gustukoak, jaso abisuak eta sartu betan.',
    },
    header: {
      sections: [
        { id: 'arazoa', label: 'Arazoa' },
        { id: 'irtenbidea', label: 'Irtenbidea' },
        { id: 'nola-dabil', label: 'Nola dabil' },
        { id: 'hiriak', label: 'Hiriak' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Sartu betan',
      languageLabel: 'Hizkuntza',
    },
    hero: {
      title: 'Kontsultatu bizikletak eta leku libreak denbora errealean iritsi aurretik',
      description:
        'BiciRadarrek BiciMAD, Bicing, Sevici eta beste bizikleta partekatu sistemetako erabilgarritasuna ikusten laguntzen dizu. Gorde gustukoak, jaso abisuak eta ireki azkar widgetetatik.',
      primaryCta: 'Deskargatu App Store-n',
      secondaryCta: 'Eskatu beta sarbidea',
      microcopy: 'Eskatu sarbidea plataforma eta hiriarekin; sistema bakoitzeko eskariaren arabera lehenesten dugu beta.',
      primaryBadge: 'iPhone · Eskuragarri orain',
      secondaryBadge: 'Android · Beta itxia',
      mockup: {
        stationLabel: 'Sol geltokia',
        availability: '12 bizikleta · 5 leku',
        bikesLabel: 'Prest dauden bizikletak',
        docksLabel: 'Leku libre',
        chips: ['Gustukoak', 'Abisuak', 'Widgetak'],
        shortcutLabel: 'Ireki goizeko joan-etorria',
      },
    },
    problem: {
      title: 'Arazoa ez da bizikleta hartzea. Iritsi eta ezer ez egotea baizik.',
      intro:
        'Estazio batera joatea bizikletarik edo leku librerik egongo den jakin gabe, presarik handiena duzunean sortzen da marruskadura.',
      cards: [
        { title: 'Iritsi eta ez dago bizikletarik', description: 'Denbora galtzen duzu beste estazio batera aldatzen jada bidean zaudenean.' },
        { title: 'Iritsi eta ez dago leku librerik', description: 'Itzulketa ere zaildu egiten da erabilgarritasuna aurreikusi ezin duzunean.' },
        { title: 'Pauso gehiegi kontsulta sinple baterako', description: 'Aplikazioa ireki, bilatu, freskatu eta alderatzeak moteldu egiten du berehalakoa izan behar duen erabakia.' },
      ],
    },
    solution: {
      title: 'Irtenbidea: mugitu aurretik erabilgarria den informazioa',
      cards: [
        { title: 'Denbora erreala', description: 'Kontsultatu bizikletak eta lekuak etxetik edo lanetik atera aurretik.' },
        { title: 'Gustukoak', description: 'Gorde zure estazio garrantzitsuenak eta ikusi lehenengo beti.' },
        { title: 'Abisuak', description: 'Jaso jakinarazpenak estazio batek bizikletak edo lekuak dituenean.' },
        { title: 'Widgetak', description: 'Ireki behar duzuna hasierako pantailatik edo erlojutik.' },
      ],
    },
    howItWorks: {
      title: 'Nola dabil',
      steps: [
        { number: '1', title: 'Aukeratu hiria eta sistema', description: 'Egunero erabiltzen duzun bizikleta sare publikoarekin hasten zara.' },
        { number: '2', title: 'Gorde gustukoak', description: 'Ohiko estazioak finkatzen dituzu egoera segundo gutxitan ikusteko.' },
        { number: '3', title: 'Kontsultatu edo jaso abisuak', description: 'Iritsi aurretik erabakitzen duzu hara joan, itxaron edo estazioa aldatu.' },
      ],
    },
    cities: {
      title: 'Hiriak eta sistemak',
      description: 'Arkitektura hiriz hiri eskalatzeko prestatuta dago landing osoa bikoiztu gabe.',
      items: [
        { key: 'madrid', name: 'Madril', system: 'BiciMAD', description: 'Kontsultatu BiciMAD atera aurretik.' },
        { key: 'barcelona', name: 'Bartzelona', system: 'Bicing', description: 'Aurkitu Bicingeko estazio erabilgarri bat presaka zoazenean.' },
        { key: 'sevilla', name: 'Sevilla', system: 'Sevici', description: 'Aurreikusi Seviciren erabilgarritasuna eguneroko joan-etorrietan.' },
        { key: 'valencia', name: 'Valentzia', system: 'Valenbisi', description: 'Saihestu itzulinguruak leku libreak bilatzean.' },
        { key: 'zaragoza', name: 'Zaragoza', system: 'Bizi', description: 'Ikusi Bizi denbora errealean eta ireki gustukoak azkar.' },
      ],
      moreLabel: 'Hiri bateragarri gehiago laster',
    },
    midCta: {
      title: 'Bi sarbide azkar zure plataformaren arabera',
      description: 'Beta orain eskaria jasotzeko eta gonbidapenak plataforma eta hiriaren arabera eskalatzeko diseinatuta dago.',
      primaryCta: { label: 'Deskargatu App Storen', note: 'Eskuragarri orain iPhonen' },
      secondaryCta: { label: 'Eskatu beta sarbidea', note: 'Android sarbide kontrolatuan' },
    },
    betaForm: {
      title: 'Beta sarbide eskaera',
      description:
        'Adierazi sistema eragilea eta bizikleta publikoa erabiltzen duzun hiria. Zure baimenarekin kudeatzen dugu beta interesa.',
      helper:
        'Hiri bakoitza bere sistemari lotuta dago: Madril · BiciMAD, Bartzelona · Bicing, Sevilla · Sevici, Valentzia · Valenbisi, Zaragoza · Bizi.',
      honeypotLabel: 'Enpresa',
      consentLabel: 'Onartzen dut BiciRadarrek datu hauek erabiltzea beta sarbidea kudeatzeko eta eskaera honi buruz nirekin harremanetan jartzeko.',
      consentHint: 'Informazioa beta prozesurako bakarrik erabiltzen dugu.',
      submitLabel: 'Eskatu beta sarbidea',
      loadingLabel: 'Eskaera bidaltzen...',
      successTitle: 'Eskaera bidalia',
      successMessage: 'Eskaera bidalia. Eskerrik asko zure interesagatik.',
      errorMessage: 'Ezin izan dugu zure eskaera bidali. Saiatu berriro segundo batzuk barru.',
      options: {
        operatingSystems,
        cities: [
          { value: 'madrid', label: 'Madril · BiciMAD' },
          { value: 'barcelona', label: 'Bartzelona · Bicing' },
          { value: 'sevilla', label: 'Sevilla · Sevici' },
          { value: 'valencia', label: 'Valentzia · Valenbisi' },
          { value: 'zaragoza', label: 'Zaragoza · Bizi' },
        ],
      },
      fields: {
        operatingSystem: { label: 'Sistema eragilea', placeholder: 'Aukeratu zure sistema eragilea' },
        city: { label: 'Hiria eta sistema', placeholder: 'Aukeratu hiria eta sistema' },
      },
      validation: {
        required: 'Bete eremu hau.',
        consent: 'Zure baimena behar dugu beta kudeatzeko.',
        turnstile: 'Osatu segurtasun egiaztapena eta saiatu berriro.',
        server: 'Arazo bat egon da zure eskaera bidaltzean.',
      },
    },
    faq: {
      title: 'FAQ',
      items: [
        { question: 'App ofiziala badut, zer ematen dit BiciRadar-ek?', answer: 'Abiadura: gertuko estazioa, bizikletak, ainguraleku libreak, gogokoak, egoera eta ibilbidea azkar ikusteko.' },
        { question: 'Benetan denbora aurrezten du?', answer: 'Bai. Ahotsa eta lasterbideak ditu iPhone, Apple Watch eta Android-en, kontsultak urrats gutxiagotan egiteko.' },
        { question: 'Mugikorrerako bakarrik da?', answer: 'Ez. Mugikor eta erlojurako dago: Android, iOS, Wear OS eta Apple Watch.' },
        { question: 'Datuak fidagarriak dira?', answer: 'Hirietako open data iturri ofizialak erabiltzen ditu. Denbora errealeko sistemetan ohiko atzerapen txikiak egon daitezke.' },
        { question: 'Zergatik erabili osagarri gisa?', answer: 'App ofiziala kanal instituzionala da; BiciRadar eguneroko kontsulta azkar eta erosoetarako optimizatuta dago.' },
        { question: 'Zer egingo duzue formularioaren datuekin?', answer: 'Sistema eragilea eta hiria erabiltzen ditugu beta interesa kudeatzeko eta eskaria lehenesteko. Ez dugu eskaera fitxategi lokal batean gordetzen; taldeak jakinarazpen barnekoa jasotzen du jarraipena egiteko.' },
      ],
    },
    footer: {
      tagline: 'Bizikleta partekatuko beta, bihurketara, SEO lokalera eta hiriz hiriko eskalara bideratua.',
      links: [
        { label: 'Pribatutasuna', href: 'mailto:hola@biciradar.es?subject=Beta%20pribatutasuna' },
        { label: 'Kontaktua', href: 'mailto:hola@biciradar.es?subject=BiciRadar%20beta' },
        { label: 'Laguntza', href: 'mailto:soporte@biciradar.es?subject=BiciRadar%20laguntza' },
      ],
      primaryCta: 'Sartu betan',
      note: 'BiciRadar · iPhone eta Android · Iturri ofizialetan oinarritutako datuak',
      githubLine: 'Iturburu irekia GitHub-en',
    },
  },
  thankYou: {
    seo: {
      title: 'Eskerrik asko BiciRadar betan apuntatzeagatik',
      description:
        'Zure beta eskaera ondo bidali da. Arakatu aplikazioa, eskuragarri dauden hiriak edo partekatu beta.',
    },
    badge: 'Eskaera bidalia',
    title: 'Eskerrik asko, jaso dugu zure eskaera',
    description:
      'Beta interesa erregistratuta dago. BiciRadar instalatu dezakezu orain edo hiri bakoitzeko tokiko orriak arakatu.',
    steps: [
      'Deskargatu BiciRadar App Store-tik edo Google Play-tik orain probatu nahi baduzu.',
      'Arakatu tokiko orriak erabilgarritasuna denbora errealean ikusteko.',
      'Zalantzarik baduzu, idatzi hola@biciradar.es helbidera.',
    ],
    cityLinksTitle: 'Bitartean, arakatu eskuragarri dauden hiriak',
    cityCardCtaPrefix: 'Ikusi',
    shareLabel: 'Partekatu beta',
    primaryCta: 'Bitartean, arakatu eskuragarri dauden hiriak',
    appStoreCta: 'Deskargatu App Storen',
    playStoreCta: 'Deskargatu Google Play-n',
    footnote: 'Galderak? hola@biciradar.es',
  },
  cityPages: {
    madrid: {
      seo: { title: 'BiciMAD denbora errealean BiciRadarrekin | Madril beta', description: 'Kontsultatu BiciMAD atera aurretik, saihestu estazio hutsak eta eskatu BiciRadar betarako sarbidea Madrilentzat.' },
      badge: 'Madril · BiciMAD',
      title: 'Kontsultatu BiciMAD denbora errealean atera aurretik',
      description: 'Ikusi bizikletak eta lekuak Madrilgo estazio garrantzitsuetan eta saihestu alferrikako joan-etorriak oinarri huts edo beteetara.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Saihestu estazio hutsak', description: 'Erabaki BiciMAD oinarri batera joan aurretik.' },
        { title: 'Planifikatu hiri barruko joan-etorriak', description: 'Konbinatu gustukoak eta abisuak zure eguneroko ibilbideetan.' },
        { title: 'Jaso abisu erabilgarriak', description: 'Begiratu gune giltzarriak bizikleta hartu edo itzuli behar duzunean.' },
      ],
      faqTitle: 'Madril FAQ',
      faq: [
        { question: 'BiciMADekin dabil?', answer: 'Bai. Orria BiciMADerako eduki espezifikorako eta tokiko eskariarako prestatuta dago.' },
        { question: 'Madril Android betan ere sartzen da?', answer: 'Bai. Sarbidea eskatu dezakezu eta erabiltzaileak faseka gonbidatuko ditugu.' },
      ],
      mockup: { stationLabel: 'Sol geltokia', availability: '12 bizikleta · 5 leku' },
    },
    barcelona: {
      seo: { title: 'Bicing denbora errealean BiciRadarrekin | Bartzelona beta', description: 'Kontsultatu Bicing mugitu aurretik, gorde gustukoak eta eskatu BiciRadar betarako sarbidea Bartzelonarentzat.' },
      badge: 'Bartzelona · Bicing',
      title: 'Kontsultatu Bicing estaziora iritsi aurretik',
      description: 'Ikusi bizikletak eta leku libre dauden Bartzelonan eta erabaki ibilbide bera mantendu, oinarria aldatu edo itxaron.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Buelta gutxiago puntako orduetan', description: 'Kontsultatu Bicing metro edo bulegotik atera aurretik.' },
        { title: 'Gustukoak zure errutinarako', description: 'Etxe, lan edo unibertsitatetik gertu dauden estazioak gertu eduki.' },
        { title: 'Kontsulta azkarretarako widgetak', description: 'Ireki Bicingen egoera aplikazio osoa ireki gabe.' },
      ],
      faqTitle: 'Bartzelona FAQ',
      faq: [
        { question: 'Bicingekin erabili al dezaket app ofiziala badut?', answer: 'Bai. BiciRadarrek eguneroko erabilera osatzen du kontsulta eta lasterbide azkarragoekin.' },
        { question: 'Bartzelonak beta gonbidapenak izango ditu?', answer: 'Bai. Eskaria jasotzen ari gara sarbidea hiriaren arabera lehenesteko.' },
      ],
      mockup: { stationLabel: 'Pg. de Gràcia', availability: '8 bizikleta · 4 leku' },
    },
    sevilla: {
      seo: { title: 'Sevici denbora errealean BiciRadarrekin | Sevilla beta', description: 'Aurreikusi Seviciren erabilgarritasuna, gorde gustuko estazioak eta eskatu BiciRadar betarako sarbidea Sevillan.' },
      badge: 'Sevilla · Sevici',
      title: 'Kontsultatu Sevici denbora errealean mugitu aurretik',
      description: 'Ikusi erabilgarritasuna Sevillan eta saihestu bizikletarik edo lekurik gabe iristea erabaki azkarra behar duzunean.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Ziurgabetasun gutxiago abiatu aurretik', description: 'Kontsultatu Sevici ibilbidea hasi aurretik.' },
        { title: 'Gustukoetarako sarbide azkarra', description: 'Etxe, lan edo ikasketarako estazio garrantzitsuak eskura izan.' },
        { title: 'Itzulketa hobeetarako abisuak', description: 'Jaso seinale erabilgarriak leku libreak berriro daudenean.' },
      ],
      faqTitle: 'Sevilla FAQ',
      faq: [
        { question: 'Sevici betaren barruan dago?', answer: 'Bai. Landing-a Sevillako eskari lokala SEO propioarekin harrapatzeko prestatuta dago.' },
        { question: 'Nire datuak utzi al ditzaket gonbidapenik berehala ez badago ere?', answer: 'Bai. Zure interesa gordetzen dugu etorkizuneko irekierak lehenesteko.' },
      ],
      mockup: { stationLabel: 'Puerta Jerez', availability: '6 bizikleta · 7 leku' },
    },
    valencia: {
      seo: { title: 'Valenbisi denbora errealean BiciRadarrekin | Valentzia beta', description: 'Kontsultatu Valenbisi, saihestu estazio beteak eta eskatu BiciRadar betarako sarbidea Valentziarentzat.' },
      badge: 'Valentzia · Valenbisi',
      title: 'Kontsultatu Valenbisi estaziora hurbildu aurretik',
      description: 'Ikusi bizikletak eta lekuak Valentziako sarean, itzulera edo jasotzea martxan inprobisatu behar ez izateko.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Saihestu estazio beteak', description: 'Begiratu leku libreak helmugara iritsi aurretik.' },
        { title: 'Lehen erabakia hobetu', description: 'Ikusi zein den inguruko oinarririk onena marruskadura gutxiagorekin.' },
        { title: 'Errepikatutako joan-etorriak gorde', description: 'Errepikatu kontsulta garrantzitsuak gustuko, abisu eta widgetekin.' },
      ],
      faqTitle: 'Valentzia FAQ',
      faq: [
        { question: 'BiciRadarrek Valenbisirekin balio du?', answer: 'Bai. Valentziarako orri eta kanpaina espezifikoetarako prestatuta dago.' },
        { question: 'Android betarako sarbidea ere egongo da?', answer: 'Bai. Orrialde honetatik eman dezakezu izena eta email bidez jarriko gara harremanetan.' },
      ],
      mockup: { stationLabel: 'Colón', availability: '10 bizikleta · 2 leku' },
    },
    zaragoza: {
      seo: { title: 'Bizi denbora errealean BiciRadarrekin | Zaragoza beta', description: 'Kontsultatu Bizi Zaragozan, ireki gustukoak eta eskatu BiciRadar betarako sarbidea tokiko edukiarekin.' },
      badge: 'Zaragoza · Bizi',
      title: 'Kontsultatu Bizi denbora errealean pedalei ekin aurretik',
      description: 'Ireki Biziren erabilgarritasuna Zaragozan, gorde ohiko estazioak eta murriztu eguneroko kontsulten denbora.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Biziren kontsulta azkarra', description: 'Ireki ohiko estazioen egoera segundo gutxitan.' },
        { title: 'SEO-rako prestatutako tokiko edukia', description: 'Hiri bakoitzak bere copy-a, FAQ-a eta metadata du.' },
        { title: 'Hiriz hiriko beta eskalagarria', description: 'Zaragozako eskaria jasotzen dugu arkitektura bikoiztu gabe.' },
      ],
      faqTitle: 'Zaragoza FAQ',
      faq: [
        { question: 'Zaragoza lehen fasean sartuta dago?', answer: 'Bai. Hasierako tokiko orrietako bat bezala prestatuta dago beta CTArekin.' },
        { question: 'Orri hau Zaragozako beste pertsona batzuekin parteka dezaket?', answer: 'Bai. Partekatzea gomendatzen dizugu tokiko eskaria hobeto neurtzeko.' },
      ],
      mockup: { stationLabel: 'Espainia plaza', availability: '7 bizikleta · 6 leku' },
    },
  },
} satisfies LocaleContent;

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
    siteTagline: 'Zure bizikleta-geltokia, begiratu batean',
    defaultOgTitle: 'BiciRadar | Kontsultatu bizikletak eta leku libreak denbora errealean',
    defaultOgDescription:
      'Ikusi bizikleta partekatuaren sistemetako erabilgarritasuna, gorde gustukoak eta deskargatu BiciRadar.',
  },
  common: {
    skipToContent: 'Joan edukira',
    appStoreLabel: 'Deskargatu App Storen',
    androidBetaLabel: 'Deskargatu Google Play-n',
    openMenu: 'Ireki nabigazioa',
    closeMenu: 'Itxi nabigazioa',
    backToHome: 'Itzuli hasierara',
    viewCities: 'Ikusi hiriak',
    cityPageCta: 'Ikusi tokiko orria',
    heroCta: 'Deskargatu orain',
    heroSecondaryCta: 'Ikusi nola dabilen',
    cityHeroMessage:
      'App-a ireki, zure geltokia begiratu eta berehala ikusiko duzu irtetea komeni zaizun ala plana aldatu behar duzun.',
    citySeoTitleSuffix: 'BiciRadar',
    citySeoDescriptionSuffix:
      'Begiratu zure geltokia irten aurretik mugikorretik edo erlojutik.',
    finalCtaTitle: 'Deskargatu doan eta probatu',
    finalCtaDescription:
      'Gorde zure ohiko geltokiak eta lehen egunetik irten aurretik erabakiko duzu. Konturik gabe hasteko eta sare bakoitzeko datu irekiekin.',
    cityRevisitLabel: 'Berriz ikusi hiriak',
    shareDescription: 'Partekatu BiciRadar zure hiriko beste norbaitekin.',
    betaInviteLabel: 'Deskarga zuzena',
    faqLabel: 'Ohiko galderak',
    thankYouShareFallback: 'Esteka kopiatu da. Orain parteka dezakezu.',
    githubAriaLabel: 'Iturburu kodea GitHub-en',
  },
  home: {
    seo: {
      title: 'BiciRadar | Zure geltokiko bizikletak eta leku libreak irten aurretik',
      description:
        'Begiratu zure bizikleta publikoaren geltokian bizikletarik edo leku librerik dagoen irten aurretik. Gogokoak, widgetak, erlojua eta abisuak. Doan iPhone eta Android-erako.',
    },
    header: {
      sections: [
        { id: 'abantailak', label: 'Abantailak' },
        { id: 'app', label: 'Aplikazioa' },
        { id: 'nola-dabil', label: 'Nola dabil' },
        { id: 'hiriak', label: 'Hiriak' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Deskargatu',
      languageLabel: 'Hizkuntza',
    },
    hero: {
      title: 'Bizikletarik badago zure geltokian? Begiratu irten aurretik.',
      description:
        'BiciRadarrek zure bizikleta publikoko bizikletak eta leku libreak erakusten dizkizu. Zure ohiko geltokiak, ukitu batera mugikorretik, widget batetik edo erlojutik.',
      primaryCta: 'Deskargatu App Store-n',
      secondaryCta: 'Deskargatu Google Play-n',
      microcopy: 'Doan · Konturik gabe hasteko · iPhone eta Android',
      trust: ['40 hiri baino gehiago', 'Apple Watch eta Wear OS', 'Widgetak eta ahotsa'],
      screenshotAlt: 'BiciRadar-en mapa, geltokirik hurbilena eta bere bizikletak eta leku libreak erakusten',
      mockup: {
        stationLabel: 'Sol geltokia',
        availability: '12 bizikleta · 5 leku',
        bikesLabel: 'Prest dauden bizikletak',
        docksLabel: 'Leku libre',
        chips: ['Gogokoak', 'Abisuak', 'Widgetak'],
        shortcutLabel: 'Ireki goizeko joan-etorria',
      },
    },
    solution: {
      title: 'Kalera jaitsi aurretik zer dagoen jakiteko egina',
      intro:
        'Geltoki huts batera iristea, edo leku libre barik dagoen batera, saihets daitekeen denbora galera da. BiciRadarrek hori errazten dizu.',
      cards: [
        {
          icon: 'pin',
          title: 'Geltoki erabilgarria, irekitzen duzun orduko',
          description:
            'Bizikletak dituen geltokirik hurbilena eta leku libreak dituena ikusten dituzu, distantziarekin eta bidea ukitu batera. Oinez bazoaz, bizikletak dituztenak erakusten dizkizu; bizikletan bazoaz, leku libreak dituztenak.',
        },
        {
          icon: 'sparkles',
          title: 'Iragazi eta ordenatu nahi duzun moduan',
          description:
            'Distantziaren, bizikleten, leku libreen edo ordu horretan ohiko erabilgarritasunaren arabera. Sareak argitaratzen duen tokian, bizikleta elektrikoak edo mekanikoak ere bai.',
        },
        {
          icon: 'star',
          title: 'Etxea, Lana eta gogokoak',
          description:
            'Finkatu zure ohiko geltokiak eta ireki itzazu ukitu batean, aldiro bilatu gabe.',
        },
        {
          icon: 'bell',
          title: 'Zure geltokien abisuak',
          description:
            'Etxea, Lana eta gogokoetarako alertak. Kontu batekin (aukerakoa), errutinek zure geltokia zaintzen dute irten aurretik eta ondoko alternatiba bat proposatzen dizute.',
        },
        {
          icon: 'phone',
          title: 'Widgetetan eta erlojuan',
          description:
            'Widgetak mugikorrean eta aplikazioa Apple Watch eta Wear OS-erako, telefonoa atera gabe zure geltokia begiratzeko.',
        },
        {
          icon: 'globe',
          title: 'Galdetu zure laguntzaileari',
          description:
            'Siri, Lasterbideak edo Google Assistant-ekin galdetu dezakezu zure geltokian bizikletarik edo leku librerik dagoen.',
        },
      ],
      note: 'BiciRadarrek erabilgarritasuna erakusten du erabakitzen laguntzeko; ez ditu alokairuak edo ordainketak kudeatzen.',
    },
    showcase: {
      title: 'Horrelakoa da barrutik',
      intro: 'Aplikazioaren pantaila-argazki errealak, apaindurarik gabe.',
      items: [
        {
          key: 'map',
          title: 'Mapa zuzenean',
          description: 'Bizikletak eta lekuak begiratu batean, azken eguneratzearen orduarekin. Iragazi bizikletak bakarrik edo lekuak bakarrik.',
          alt: 'Geltokiak eta Bizikletak + Lekuak, Bizikletak bakarrik eta Lekuak bakarrik iragazkiak dituen mapa',
        },
        {
          key: 'nearby',
          title: 'Zure ondoan',
          description: 'Bizikletak dituen geltokirik hurbilena eta leku libreak dituena, eta bidea ukitu batera.',
          alt: 'Ondoan fitxa, bizikletak eta leku libreak dituen geltokirik hurbilenarekin',
        },
        {
          key: 'favorites',
          title: 'Gogokoak, Etxea eta Lana',
          description: 'Gordetako geltokiak eta alerten kudeaketa, leku bakarrean.',
          alt: 'Gogokoak fitxa, geltoki-alertekin eta Etxea eta Lana sarbideekin',
        },
        {
          key: 'detail',
          title: 'Geltokiaren xehetasuna',
          description: 'Distantzia, mapa eta gogoko, Etxea edo Lana gisa gordetzea ukitu batean.',
          alt: 'Geltoki baten xehetasuna, distantziarekin, mapa eta gordetzeko botoiekin',
        },
      ],
    },
    howItWorks: {
      title: 'Hasteak minutu bat hartzen du',
      steps: [
        {
          number: '1',
          title: 'Deskargatu eta aukeratu zure hiria',
          description: 'Hautatu zure bizikleta publikoko sarea. Ez da konturik sortu beharrik.',
        },
        {
          number: '2',
          title: 'Begiratu zer duzun ondoan',
          description: 'Zure kokapenarekin, aplikazioak une honetan zein geltoki komeni zaizun erakusten dizu.',
        },
        {
          number: '3',
          title: 'Gorde zure geltokiak',
          description: 'Finkatu Etxea, Lana eta gogokoak eta erabaki segundo gutxitan, mugikorretik edo erlojutik.',
        },
      ],
    },
    cities: {
      title: 'Hiriak eta sistemak',
      description: 'Bizikleta publikoa eguneroko parte den hainbat hiritatik hasi gara eta gehiago gehitzen joango gara.',
      items: [
        { key: 'madrid', name: 'Madril', system: 'BiciMAD', description: 'Kontsultatu BiciMAD atera aurretik.' },
        { key: 'barcelona', name: 'Bartzelona', system: 'Bicing', description: 'Aurkitu Bicingeko estazio erabilgarri bat presaka zoazenean.' },
        { key: 'sevilla', name: 'Sevilla', system: 'Sevici', description: 'Aurreikusi Seviciren erabilgarritasuna eguneroko joan-etorrietan.' },
        { key: 'valencia', name: 'Valentzia', system: 'Valenbisi', description: 'Saihestu itzulinguruak leku libreak bilatzean.' },
        { key: 'zaragoza', name: 'Zaragoza', system: 'Bizi', description: 'Ikusi Bizi denbora errealean eta ireki gustukoak azkar.' },
      ],
      moreLabel: 'Eta hiri gehiago aplikazioaren barruan: aukeratu zurea irekitzean.',
      otherCitiesLabel: 'Beste hirien orriak',
    },
    midCta: {
      title: 'Irten aurretik, begiratu zure geltokia.',
      description:
        'Deskargatu BiciRadar doan, aukeratu zure hiria eta izan itzazu zure geltokiak ukitu batera.',
      reassurance: 'Doan · Konturik gabe hasteko',
      primaryCta: {
        label: 'Deskargatu App Store-n',
        note: 'iPhone-rako',
      },
      secondaryCta: {
        label: 'Deskargatu Google Play-n',
        note: 'Android-erako',
      },
    },
    betaForm: {
      title: 'Aukeratu zure plataforma eta deskargatu',
      description: 'Instalatu BiciRadar zuzenean App Storetik edo Google Play-tik.',
      honeypotLabel: 'Enpresa',
      consentLabel: 'Onartzen dut BiciRadarrek datu hauek erabiltzea deskarga-esteka bidaltzeko eta eskaera honi buruz nirekin harremanetan jartzeko.',
      consentHint: 'Informazioa esteka bidaltzeko eta eskaera honekin laguntzeko bakarrik erabiltzen dugu.',
      submitLabel: 'Bidali esteka',
      loadingLabel: 'Esteka bidaltzen...',
      successTitle: 'Esteka bidalia',
      successMessage: 'BiciRadar oraintxe deskarga dezakezu zure dendatik.',
      errorMessage: 'Ezin izan dugu esteka bidali. Saiatu berriro segundo batzuk barru.',
      options: {
        operatingSystems,
      },
      fields: {
        email: { label: 'Emaila', placeholder: 'zure@email.com' },
        operatingSystem: { label: 'Sistema eragilea', placeholder: 'Aukeratu zure sistema eragilea' },
      },
      validation: {
        required: 'Bete eremu hau.',
        email: 'Sartu baliozko email bat.',
        consent: 'Zure baimena behar dugu esteka bidaltzeko.',
        turnstile: 'Osatu segurtasun egiaztapena eta saiatu berriro.',
        server: 'Arazo bat egon da esteka bidaltzean.',
      },
    },
    faq: {
      title: 'Ohiko galderak',
      items: [
        {
          question: 'Doakoa da? Kontua sortu behar dut?',
          answer:
            'Doan deskargatzen eta erabiltzen da. Geltokiak kontsultatu, gogokoak gorde eta widgetak erabil ditzakezu konturik gabe. Kontua aukerakoa da eta errutinetarako eta bidaien jarraipenerako bakarrik behar da, gure zerbitzaritik zaintzen direlako.',
        },
        {
          question: 'Nire hiriko aplikazio ofiziala ordezkatzen du?',
          answer:
            'Ez. BiciRadar erabilgarritasuna kontsultatzeko eta irten aurretik erabakitzeko da; ez ditu alokairuak edo ordainketak kudeatzen. Zure sistemaren aplikazioarekin batera erabil dezakezu.',
        },
        {
          question: 'Datuak denbora errealekoak dira?',
          answer:
            'Sistema bakoitzak argitaratzen dituen datu irekiak erabiltzen ditugu eta maiz eguneratzen dira, baina erritmoa hornitzaile bakoitzaren araberakoa da. Geltoki bakoitzean ikusten duzu noiz eguneratu zen.',
        },
        {
          question: 'Nire kokapena behar du?',
          answer:
            'Zure geltokirik hurbilena zein den esateko erabiltzen du. Baimenik ematen ez baduzu, aplikazioa erabiltzen jarraitu dezakezu, baina emaitza batzuk zehaztasun gutxiagokoak izango dira.',
        },
        {
          question: 'Erlojuan eta ahotsarekin funtzionatzen du?',
          answer:
            'Bai. Apple Watch eta Wear OS-erako aplikazioa dago, widgetak iPhone eta Android-en, eta Siri, Lasterbideak edo Google Assistant-ekin galdetu dezakezu.',
        },
        {
          question: 'Zein hiri hartzen ditu?',
          answer:
            'Madril, Bartzelona, Sevilla, Valentzia, Zaragoza, Bilbo eta beste asko. Aplikazioa irekitzean zure hiria aukeratzen duzu; zurea orri propioa badu, hirien atalean aurkituko duzu.',
        },
        {
          question: 'Zer gertatzen da nire gogokoekin eta datuekin?',
          answer:
            'Zure gogokoak eta gordetako lekuak zure gailuan geratzen dira. Xehetasun gehiago pribatutasun-politikan dituzu, orriaren amaieran estekatuta.',
        },
      ],
    },
    footer: {
      tagline: '',
      links: [
        { label: 'Pribatutasuna', href: 'https://gcaguilar.github.io/biciradar-privacy-policy/' },
        { label: 'Kontaktua', href: 'mailto:hola@biciradar.es?subject=BiciRadar%20app' },
        { label: 'Laguntza', href: 'mailto:soporte@biciradar.es?subject=BiciRadar%20laguntza' },
      ],
      primaryCta: 'Deskargatu orain',
      note: 'BiciRadar · iPhone eta Android · Sistema bakoitzeko datu irekiak',
      githubLine: 'Iturburu irekia GitHub-en',
    },
  },
  thankYou: {
    seo: {
      title: 'Eskerrik asko BiciRadar esteka eskatzeagatik',
      description:
        'Zure eskaera ondo bidali da. Arakatu aplikazioa, eskuragarri dauden hiriak edo partekatu.',
    },
    badge: 'Eskaera bidalia',
    title: 'Eskerrik asko, esteka bidaliko dizugu',
    description:
      'BiciRadar oraintxe instala dezakezu edo hiri bakoitzeko tokiko orriak arakatu.',
    installStepByOs: {
      ios: 'iPhone-an: BiciRadar App Store-tik deskarga dezakezu oraintxe bertan.',
      android: 'Android-en: begiratu bidaliko dizugun emaila Google Play estekarekin.',
      both: 'Zure mugikorraren arabera: erabili App Store iPhone-n edo Google Play Android-en.',
    },
    steps: [
      'Arakatu tokiko orriak erabilgarritasuna denbora errealean ikusteko.',
      'Zalantzarik baduzu, idatzi hola@biciradar.es helbidera.',
    ],
    cityLinksTitle: 'Bitartean, arakatu eskuragarri dauden hiriak',
    cityCardCtaPrefix: 'Ikusi',
    shareLabel: 'Partekatu BiciRadar',
    primaryCta: 'Bitartean, arakatu eskuragarri dauden hiriak',
    appStoreCta: 'Deskargatu App Storen',
    playStoreCta: 'Deskargatu Google Play-n',
    footnote: 'Galderak? hola@biciradar.es',
  },
  cityPages: {
    madrid: {
      seo: { title: 'BiciMAD denbora errealean BiciRadarrekin | Madril', description: 'Kontsultatu BiciMAD atera aurretik, saihestu estazio hutsak eta deskargatu BiciRadar Madrilentzat.' },
      badge: 'Madril · BiciMAD',
      title: 'Kontsultatu BiciMAD denbora errealean atera aurretik',
      description: 'Ikusi bizikletak eta lekuak Madrilgo estazio garrantzitsuetan eta saihestu alferrikako joan-etorriak oinarri huts edo beteetara.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Kalera atera aurretik erabaki', description: 'Ireki BiciMAD eta egiaztatu zure ohiko oinarriak merezi duen atera aurretik.' },
        { title: 'Saihestu azken uneko geltoki-aldaketak', description: 'Oinarri horrek ez badu balio, beste bat aukeratzen duzu bidean denbora galdu aurretik.' },
        { title: 'Begirada batean ebatzi zalantza', description: 'Gustukoek, abisuek eta sarbide azkarrek erantzuna frikziorik gabe ematen dizute.' },
      ],
      faqTitle: 'Madril FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek BiciMADekin atera aurretik?', answer: 'Zure ohiko oinarriak bizikletak edo lekuak dituen eta orain jaitsi, itxaron edo geltokia aldatu behar duzun esaten dizu.' },
        { question: 'Azkar ikus al dezaket BiciMAD mugikorretik edo erlojutik?', answer: 'Bai. Gustukoek, widgetek eta sarbide azkarrek erabilgarritasuna segundo gutxitan ebazten uzten dizute, app osoa ireki gabe.' },
      ],
      mockup: { stationLabel: 'Sol geltokia', availability: '12 bizikleta · 5 leku' },
    },
    barcelona: {
      seo: { title: 'Bicing denbora errealean BiciRadarrekin | Bartzelona', description: 'Kontsultatu Bicing mugitu aurretik, gorde gustukoak eta deskargatu BiciRadar Bartzelonarentzat.' },
      badge: 'Bartzelona · Bicing',
      title: 'Kontsultatu Bicing estaziora iritsi aurretik',
      description: 'Ikusi bizikletak eta leku libre dauden Bartzelonan eta erabaki ibilbide bera mantendu, oinarria aldatu edo itxaron.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Metroa utzi aurretik erabaki', description: 'Kontsultatu Bicing kalera atera edo bulegotik irten aurretik.' },
        { title: 'Saihestu itzulinguruak puntako orduan', description: 'Geltoki horrek ez badu merezi, beste oinarri batera aldatzen zara desbideratu aurretik.' },
        { title: 'Mantendu zure errutina ukitu bakarrean', description: 'Zure geltoki garrantzitsuak prest geratzen dira zalantza segundo gutxitan ebazteko.' },
      ],
      faqTitle: 'Bartzelona FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Bicingekin iritsi aurretik?', answer: 'Geltoki horrek merezi duen edo desbideratu aurretik beste oinarri batera aldatzea hobe den jakiten laguntzen dizu.' },
        { question: 'Eta jada app ofizialean begiratzen badut?', answer: 'BiciRadarrek urratsak kentzen ditu presaka zaudenean: gustukoak ireki, erabilgarritasuna ikusi eta erabakia lehenago hartu.' },
      ],
      mockup: { stationLabel: 'Pg. de Gràcia', availability: '8 bizikleta · 4 leku' },
    },
    sevilla: {
      seo: { title: 'Sevici denbora errealean BiciRadarrekin | Sevilla', description: 'Aurreikusi Seviciren erabilgarritasuna, gorde gustuko estazioak eta deskargatu BiciRadar Sevillan.' },
      badge: 'Sevilla · Sevici',
      title: 'Kontsultatu Sevici denbora errealean mugitu aurretik',
      description: 'Ikusi erabilgarritasuna Sevillan eta saihestu bizikletarik edo lekurik gabe iristea erabaki azkarra behar duzunean.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Abiatu ziurgabetasun gutxiagorekin', description: 'Kontsultatu Sevici abiatu aurretik eta erabaki testuinguruarekin.' },
        { title: 'Eduki oinarri giltzarria eskura', description: 'Zure ohiko geltokiak prest geratzen dira zalantza segundo gutxitan ebazteko.' },
        { title: 'Itzuli hobeto inprobisatu gabe', description: 'Jaso seinale erabilgarriak behar dituzun leku libreak berriro agertzen direnean.' },
      ],
      faqTitle: 'Sevilla FAQ',
      faq: [
        { question: 'Zer erabaki hartzen laguntzen dit Sevicirekin mugitu aurretik?', answer: 'Zure ohiko geltokira joatea merezi duen, minutu batzuk itxaron behar duzun edo denbora galdu aurretik oinarria aldatzea hobe den.' },
        { question: 'Sevici eskura izan al dezaket app osoa ireki gabe?', answer: 'Bai. Gustukoek, widgetek eta sarbide azkarrek erabilgarritasuna ikusi eta segundo gutxitan jarduten uzten dizute.' },
      ],
      mockup: { stationLabel: 'Puerta Jerez', availability: '6 bizikleta · 7 leku' },
    },
    valencia: {
      seo: { title: 'Valenbisi denbora errealean BiciRadarrekin | Valentzia', description: 'Kontsultatu Valenbisi, saihestu estazio beteak eta deskargatu BiciRadar Valentziarentzat.' },
      badge: 'Valentzia · Valenbisi',
      title: 'Kontsultatu Valenbisi estaziora hurbildu aurretik',
      description: 'Ikusi bizikletak eta lekuak Valentziako sarean, itzulera edo jasotzea martxan inprobisatu behar ez izateko.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Saihestu oinarri bete batera iristea', description: 'Begiratu leku libreak helmugara hurbildu aurretik eta hobeto erabaki.' },
        { title: 'Lehen geltokia hobeto aukeratu', description: 'Ikusten duzu zein oinarri den egokiena bizikleta hartzeko edo uzteko, inprobisatu gabe.' },
        { title: 'Errutina zerotik hasi gabe ebatzi', description: 'Gustukoek, abisuek eta sarbide azkarrek urratsak aurrezten dizkizute joan-etorri bakoitzean.' },
      ],
      faqTitle: 'Valentzia FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Valenbisirekin geltokira hurbildu aurretik?', answer: 'Oinarri hori bizikleta hartzeko edo uzteko egokia den erabakitzen laguntzen dizu, bidean inprobisatu aurretik.' },
        { question: 'Azkar ikus al dezaket iPhonean edo Androiden?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, fluxu osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Colón', availability: '10 bizikleta · 2 leku' },
    },
    zaragoza: {
      seo: { title: 'Bizi denbora errealean BiciRadarrekin | Zaragoza', description: 'Kontsultatu Bizi Zaragozan, ireki gustukoak eta deskargatu BiciRadar tokiko edukiarekin.' },
      badge: 'Zaragoza · Bizi',
      title: 'Kontsultatu Bizi denbora errealean pedalei ekin aurretik',
      description: 'Ireki Biziren erabilgarritasuna Zaragozan, gorde ohiko estazioak eta murriztu eguneroko kontsulten denbora.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Pedalei ekin aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak erabilgarritasuna duen abiatu aurretik.' },
        { title: 'Saihestu oinarrien arteko itzulinguruak', description: 'Geltoki horrek ez badu merezi, plana aldatzen duzu denbora galdu aurretik.' },
        { title: 'Kontsultatu Bizi segundo gutxitan', description: 'Gustukoek eta sarbide azkarrek zalantza ia berehala ebazten uzten dizute.' },
      ],
faqTitle: 'Zaragoza FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Bizirekin pedalei ekin aurretik?', answer: 'Zure ohiko geltokiak bizikletak edo lekuak dituen eta orain abiatu edo oinarria aldatu behar duzun jakiten laguntzen dizu.' },
        { question: 'Zaragozako beste norbaitekin parteka al dezaket?', answer: 'Bai. Eguneroko zalantza bera duen beste pertsona bati orri honek eta app-ak mugitu aurretik erabakitzen laguntzen diote.' },
      ],
      mockup: { stationLabel: 'Espainia plaza', availability: '7 bizikleta · 6 plug' },
    },
    bilbao: {
      seo: { title: 'Bilbao denbora errealean BiciRadarrekin | Bilbo', description: 'Kontsultatu bizikleta eta plug Bilbo irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Bilbo · Bilbo',
      title: 'Kontsultatu Bilbo denbora errealean irten aurretik',
      description: 'Kontsultatu Bilboko bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Bilbo FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Bilbon mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Bilbo', availability: '5 bizikleta · 3 plug' },
    },
    murcia: {
      seo: { title: 'Murcia denbora errealean BiciRadarrekin | Murtzia', description: 'Kontsultatu bizikleta eta plug Murcia irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Murtzia · Murcia',
      title: 'Kontsultatu Murcia denbora errealean irten aurretik',
      description: 'Kontsultatu Murciako bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Murtzia FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Murtzian mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Murtzia', availability: '4 bizikleta · 2 plug' },
    },
    leon: {
      seo: { title: 'León denbora errealean BiciRadarrekin | León', description: 'Kontsultatu bizikleta eta plug León irten aurretik eta deskargatu BiciRadar.' },
      badge: 'León · León',
      title: 'Kontsultatu León denbora errealean irten aurretik',
      description: 'Kontsultatu Leóneko bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'León FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Leónen mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'León', availability: '3 bizikleta · 2 plug' },
    },
    valladolid: {
      seo: { title: 'Valladolid denbora errealean BiciRadarrekin | Valladolid', description: 'Kontsultatu bizikleta eta plug Valladolid irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Valladolid · Valladolid',
      title: 'Kontsultatu Valladolid denbora errealean irten aurretik',
      description: 'Kontsultatu Valladolideko bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Valladolid FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Valladoliden mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Valladolid', availability: '4 bizikleta · 3 plug' },
    },
    palma: {
      seo: { title: 'Palma denbora errealean BiciRadarrekin | Palma', description: 'Kontsultatu bizikleta eta plug Palma irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Palma · Palma',
      title: 'Kontsultatu Palma denbora errealean irten aurretik',
      description: 'Kontsultatu Palmako bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Palma FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Palman mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Palma', availability: '5 bizikleta · 4 plug' },
    },
    las_palmas: {
      seo: { title: 'Las Palmas denbora errealean BiciRadarrekin | Las Palmas', description: 'Kontsultatu bizikleta eta plug Las Palmas irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Las Palmas · Las Palmas',
      title: 'Kontsultatu Las Palmas denbora errealean irten aurretik',
      description: 'Kontsultatu Las Palmasko bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Las Palmas FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Las Palmasen mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Las Palmas', availability: '4 bizikleta · 3 plug' },
    },
    a_coruna: {
      seo: { title: 'A Coruña denbora errealean BiciRadarrekin | Coruña', description: 'Kontsultatu bizikleta eta plug A Coruña irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Coruña · Coruña',
      title: 'Kontsultatu A Coruña denbora errealean irten aurretik',
      description: 'Kontsultatu A Coruñako bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'A Coruña FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Coruñan mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Coruña', availability: '3 bizikleta · 2 plug' },
    },
    gijon: {
      seo: { title: 'Gijón denbora errealean BiciRadarrekin | Gijón', description: 'Kontsultatu bizikleta eta plug Gijón irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Gijón · Gijón',
      title: 'Kontsultatu Gijón denbora errealean irten aurretik',
      description: 'Kontsultatu Gijóneko bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Gijón FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Gijónen mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Gijón', availability: '4 bizikleta · 3 plug' },
    },
    vitoria_gasteiz: {
      seo: { title: 'Vitoria-Gasteiz denbora errealean BiciRadarrekin | Gasteiz', description: 'Kontsultatu bizikleta eta plug Vitoria irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Gasteiz · Gasteiz',
      title: 'Kontsultatu Vitoria-Gasteiz denbora errealean irten aurretik',
      description: 'Kontsultatu Gasteizko bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Gasteiz FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Gasteizen mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Gasteiz', availability: '3 bizikleta · 2 plug' },
    },
    pamplona: {
      seo: { title: 'Pamplona denbora errealean BiciRadarrekin | Iruña', description: 'Kontsultatu bizikleta eta plug Pamplona irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Iruña · Iruña',
      title: 'Kontsultatu Pamplona denbora errealean irten aurretik',
      description: 'Kontsultatu Iruñako bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Iruña FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Iruñean mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Iruña', availability: '5 bizikleta · 4 plug' },
    },
    castellon: {
      seo: { title: 'Castellón denbora errealean BiciRadarrekin | Castelló', description: 'Kontsultatu bizikleta eta plug Castellón irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Castelló · Castelló',
      title: 'Kontsultatu Castellón denbora errealean irten aurretik',
      description: 'Kontsultatu Castellóko bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Castelló FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Castellón mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Castelló', availability: '3 bizikleta · 2 plug' },
    },
    santander: {
      seo: { title: 'Santander denbora errealean BiciRadarrekin | Santander', description: 'Kontsultatu bizikleta eta plug Santander irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Santander · Santander',
      title: 'Kontsultatu Santander denbora errealean irten aurretik',
      description: 'Kontsultatu Santanderko bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Santander FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Santanderren mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Santander', availability: '4 bizikleta · 3 plug' },
    },
    girona: {
      seo: { title: 'Girona denbora errealean BiciRadarrekin | Girona', description: 'Kontsultatu bizikleta eta plug Girona irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Girona · Girona',
      title: 'Kontsultatu Girona denbora errealean irten aurretik',
      description: 'Kontsultatu Gironako bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Girona FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Gironan mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Girona', availability: '3 bizikleta · 2 plug' },
    },
    gran_canaria: {
      seo: { title: 'Gran Canaria denbora errealean BiciRadarrekin | Gran Canaria', description: 'Kontsultatu bizikleta eta plug Gran Canaria irten aurretik eta deskargatu BiciRadar.' },
      badge: 'Gran Canaria · Gran Canaria',
      title: 'Kontsultatu Gran Canaria denbora errealean irten aurretik',
      description: 'Kontsultatu Gran Canariako bizikleta eta plug erabilgarriak irten aurretik.',
      benefitsTitle: 'Tokiko onurak',
      benefits: [
        { title: 'Irten aurretik erabaki', description: 'Egiaztatu zure ohiko geltokiak bizikletak edo lekuak dituen martxan jarri aurretik.' },
        { title: 'Saihestu alferrikako desbideratzeak', description: 'Oinarri horrek ez badizu balio, plana aldatzen duzu kalean denbora galdu aurretik.' },
        { title: 'Begirada batean kontsultatu', description: 'Ireki gustukoak, widgeta edo mugikorra eta ebatzi zalantza segundo gutxitan.' },
      ],
      faqTitle: 'Gran Canaria FAQ',
      faq: [
        { question: 'Zer konpontzen dit BiciRadarrek Gran Canarian mugitu aurretik?', answer: 'Zure ohiko geltokiak erabilgarritasuna duen eta orain abiatu, itxaron edo beste oinarri batera joan behar duzun jakiten laguntzen dizu.' },
        { question: 'Azkar ikus al dezaket mugikorretik edo erlojutik?', answer: 'Bai. Erabilgarritasuna gustukoetan, widgetetan eta sarbide azkarretan geratzen da, app osoa ireki gabe erabakitzeko.' },
      ],
      mockup: { stationLabel: 'Gran Canaria', availability: '4 bizikleta · 3 plug' },
    },
  },
} satisfies LocaleContent;

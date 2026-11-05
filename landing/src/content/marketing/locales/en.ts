import type { LocaleContent } from '../types';

const operatingSystems = [
  { value: 'ios', label: 'iPhone / iOS' },
  { value: 'android', label: 'Android' },
  { value: 'both', label: 'I use both' },
];

export const en = {
  locale: 'en',
  languageName: 'English',
  localeLabel: 'EN',
  metadata: {
    siteName: 'BiciRadar',
    siteTagline: 'Your bike station, at a glance',
    defaultOgTitle: 'BiciRadar | Check bike and dock availability in real time',
    defaultOgDescription:
      'See live availability across shared-bike systems, save favorites, and download BiciRadar.',
  },
  common: {
    skipToContent: 'Skip to content',
    appStoreLabel: 'Download on the App Store',
    androidBetaLabel: 'Get it on Google Play',
    openMenu: 'Open navigation',
    closeMenu: 'Close navigation',
    backToHome: 'Back to home',
    viewCities: 'View cities',
    cityPageCta: 'View local page',
    heroCta: 'Download now',
    heroSecondaryCta: 'See how it works',
    cityHeroMessage:
      'Open the app, check your station, and you will know in a moment whether it is worth leaving or changing plans.',
    citySeoTitleSuffix: 'BiciRadar',
    citySeoDescriptionSuffix:
      'Check your station before you leave from your phone or your watch.',
    finalCtaTitle: 'Download it free and try it',
    finalCtaDescription:
      'Save your usual stations and from day one you decide before you leave. No account needed to start, and open data from each network.',
    cityRevisitLabel: 'Browse city pages again',
    shareDescription: 'Share BiciRadar with someone else in your city.',
    betaInviteLabel: 'Direct download',
    faqLabel: 'Frequently asked questions',
    thankYouShareFallback: 'Link copied. You can share it now.',
    githubAriaLabel: 'Source code on GitHub',
  },
  home: {
    seo: {
      title: 'BiciRadar | Bikes and free docks at your station, before you go',
      description:
        'Check whether your public bike station has bikes or free docks before you leave. Favorites, widgets, watch and alerts. Free on iPhone and Android.',
    },
    header: {
      sections: [
        { id: 'benefits', label: 'Benefits' },
        { id: 'app', label: 'The app' },
        { id: 'how-it-works', label: 'How it works' },
        { id: 'cities', label: 'Cities' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Download',
      languageLabel: 'Language',
    },
    hero: {
      title: 'Is there a bike at your station? Check before you go.',
      description:
        'BiciRadar shows the bikes and free docks of your public bike system. Your usual stations, one tap away from your phone, a widget or your watch.',
      primaryCta: 'Download on the App Store',
      secondaryCta: 'Get it on Google Play',
      microcopy: 'Free · No account needed to start · iPhone and Android',
      trust: ['40+ cities', 'Apple Watch and Wear OS', 'Widgets and voice'],
      screenshotAlt: 'BiciRadar map showing the nearest station with its bikes and free docks',
      mockup: {
        stationLabel: 'Sol Station',
        availability: '12 bikes · 5 docks',
        bikesLabel: 'Ready bikes',
        docksLabel: 'Open docks',
        chips: ['Favorites', 'Alerts', 'Widgets'],
        shortcutLabel: 'Open morning commute',
      },
    },
    solution: {
      title: 'Built to tell you what is there before you head out',
      intro:
        'Getting to an empty station, or one with no free dock, is wasted time you can avoid. This is what BiciRadar makes easy.',
      cards: [
        {
          icon: 'pin',
          title: 'The useful station, as soon as you open it',
          description:
            'See the nearest station with bikes and the nearest with free docks, with distance and directions one tap away. On foot it lists stations with bikes; on a bike, stations with free docks.',
        },
        {
          icon: 'sparkles',
          title: 'Filter and sort your way',
          description:
            'By distance, bikes, free docks, or the availability usually seen at that hour. Where the network publishes it, also e-bikes or mechanical bikes.',
        },
        {
          icon: 'star',
          title: 'Home, Work and favorites',
          description:
            'Pin your usual stations and open them in one tap, without searching every time.',
        },
        {
          icon: 'bell',
          title: 'Alerts for your stations',
          description:
            'Alerts for Home, Work and favorites. With an optional account, routines watch your station before you leave and suggest a nearby alternative.',
        },
        {
          icon: 'phone',
          title: 'On widgets and on your wrist',
          description:
            'Phone widgets plus apps for Apple Watch and Wear OS, so you can check your station without taking out your phone.',
        },
        {
          icon: 'globe',
          title: 'Just ask your assistant',
          description:
            'With Siri, Shortcuts or Google Assistant you can ask whether your station has bikes or free docks.',
        },
      ],
      note: 'BiciRadar shows availability to help you decide; it does not handle rentals or payments.',
    },
    showcase: {
      title: 'This is what it looks like',
      intro: 'Real screenshots of the app, nothing staged.',
      items: [
        {
          key: 'map',
          title: 'Live map',
          description: 'Bikes and docks at a glance, with the time of the last update. Filter by bikes only or docks only.',
          alt: 'Map with stations and the filters Bikes + Docks, Bikes only and Docks only',
        },
        {
          key: 'nearby',
          title: 'Near you',
          description: 'The nearest station with bikes and the nearest with free docks, plus directions in one tap.',
          alt: 'Nearby tab showing the nearest station with bikes and with free docks',
        },
        {
          key: 'favorites',
          title: 'Favorites, Home and Work',
          description: 'Your saved stations and alert management, all in one place.',
          alt: 'Favorites tab with station alerts and Home and Work shortcuts',
        },
        {
          key: 'detail',
          title: 'Station details',
          description: 'Distance, map, and saving as a favorite, Home or Work in one tap.',
          alt: 'Station detail with distance, map and buttons to save it',
        },
      ],
    },
    howItWorks: {
      title: 'Getting started takes a minute',
      steps: [
        {
          number: '1',
          title: 'Download and pick your city',
          description: 'Choose your public bike network. No account required.',
        },
        {
          number: '2',
          title: 'See what is near you',
          description: 'Using your location, the app shows which station suits you right now.',
        },
        {
          number: '3',
          title: 'Save your stations',
          description: 'Pin Home, Work and favorites and decide in seconds, from your phone or watch.',
        },
      ],
    },
    cities: {
      title: 'Cities and systems',
      description:
        'Start with one of these cities; inside the app you will find more public bike networks.',
      items: [
        {
          key: 'madrid',
          name: 'Madrid',
          system: 'BiciMAD',
          description: 'Check BiciMAD bikes and docks before heading out.',
        },
        {
          key: 'barcelona',
          name: 'Barcelona',
          system: 'Bicing',
          description: 'Find the best Bicing station when time matters.',
        },
        {
          key: 'sevilla',
          name: 'Seville',
          system: 'Sevici',
          description: 'Anticipate Sevici availability on daily commutes.',
        },
        {
          key: 'valencia',
          name: 'Valencia',
          system: 'Valenbisi',
          description: 'Avoid unnecessary detours while looking for open docks.',
        },
        {
          key: 'zaragoza',
          name: 'Zaragoza',
          system: 'Bizi',
          description: 'See Bizi availability fast and keep favorite stations close.',
        },
      ],
      moreLabel: 'And more cities inside the app: pick yours when you open it.',
      otherCitiesLabel: 'Pages for other cities',
    },
    midCta: {
      title: 'Check your station before you go.',
      description:
        'Download BiciRadar for free, pick your city and keep your stations one tap away.',
      reassurance: 'Free · No account needed to start',
      primaryCta: {
        label: 'Download on the App Store',
        note: 'For iPhone',
      },
      secondaryCta: {
        label: 'Get it on Google Play',
        note: 'For Android',
      },
    },
    betaForm: {
      title: 'Choose your platform and download',
      description: 'Install BiciRadar directly from the App Store or Google Play.',
      honeypotLabel: 'Company',
      consentLabel:
        'I agree that BiciRadar can use these details to send me the download link and contact me about this request.',
      consentHint: 'We only use this information to send the link and help with this request.',
      submitLabel: 'Email me the link',
      loadingLabel: 'Sending link...',
      successTitle: 'Link sent',
      successMessage: 'You can download BiciRadar right now from your store.',
      errorMessage: 'We could not send the link. Please try again in a few seconds.',
      options: {
        operatingSystems,
      },
      fields: {
        email: { label: 'Email', placeholder: 'you@email.com' },
        operatingSystem: {
          label: 'Operating system',
          placeholder: 'Choose your operating system',
        },
      },
      validation: {
        required: 'Please complete this field.',
        email: 'Enter a valid email address.',
        consent: 'We need your consent to send the link.',
        turnstile: 'Complete the security verification and try again.',
        server: 'There was a problem sending the link.',
      },
    },
    faq: {
      title: 'Frequently asked questions',
      items: [
        {
          question: 'Is it free? Do I need an account?',
          answer:
            'It is free to download and use. You can check stations, save favorites and use widgets without an account. The account is optional and only needed for routines and trip monitoring, which are watched from our server.',
        },
        {
          question: 'Does it replace my city’s official app?',
          answer:
            'No. BiciRadar is for checking availability and deciding before you leave; it does not handle rentals or payments. You can use it alongside your system’s own app.',
        },
        {
          question: 'Is the data real-time?',
          answer:
            'We use open data published by each system, which is updated frequently, but the pace depends on each provider. Each station shows how long ago it was updated.',
        },
        {
          question: 'Does it need my location?',
          answer:
            'It uses it to tell you which station is nearest. If you do not grant permission you can still use the app, but some results will be less precise.',
        },
        {
          question: 'Does it work on a watch and with voice?',
          answer:
            'Yes. There are apps for Apple Watch and Wear OS, widgets on iPhone and Android, and you can ask with Siri, Shortcuts or Google Assistant.',
        },
        {
          question: 'Which cities does it cover?',
          answer:
            'Madrid, Barcelona, Seville, Valencia, Zaragoza, Bilbao and many more. You pick your city when you open the app; if yours has its own page, you will find it in the cities section.',
        },
        {
          question: 'What happens to my favorites and my data?',
          answer:
            'Your favorites and saved places stay on your device. You will find more detail in the privacy policy, linked at the bottom of the page.',
        },
      ],
    },
    footer: {
      tagline: '',
      links: [
        { label: 'Privacy', href: 'https://gcaguilar.github.io/biciradar-privacy-policy/' },
        { label: 'Contact', href: 'mailto:hola@biciradar.es?subject=BiciRadar%20app' },
        { label: 'Support', href: 'mailto:soporte@biciradar.es?subject=BiciRadar%20support' },
      ],
      primaryCta: 'Download now',
      note: 'BiciRadar · iPhone and Android · Open data from each system',
      githubLine: 'Open source on GitHub',
    },
  },
  thankYou: {
    seo: {
      title: 'Thanks for requesting the BiciRadar link',
      description:
        'Your request was sent successfully. Explore the app, supported cities, or share it.',
    },
    badge: 'Request sent',
    title: 'Thanks for visiting BiciRadar',
    description:
      'We will email you the download link shortly. In the meantime, you can install BiciRadar now or browse local city pages.',
    installStepByOs: {
      ios: 'On iPhone: you can also download BiciRadar right now from the App Store.',
      android: 'On Android: check the email we will send with the Google Play link.',
      both: 'Pick your platform: App Store on iPhone or Google Play on Android. We will also email you both links.',
    },
    steps: [
      'Browse local pages to see live availability.',
      'Questions? Email hola@biciradar.es.',
    ],
    cityLinksTitle: 'Explore supported cities in the meantime',
    cityCardCtaPrefix: 'View',
    shareLabel: 'Share BiciRadar',
    primaryCta: 'Explore supported cities in the meantime',
    appStoreCta: 'Download on the App Store',
    playStoreCta: 'Get it on Google Play',
    footnote: 'Questions? hola@biciradar.es',
  },
  cityPages: {
    madrid: {
      seo: {
        title: 'BiciMAD live availability with BiciRadar | Madrid',
        description:
          'Check BiciMAD before you leave, avoid empty stations, and download BiciRadar for Madrid.',
      },
      badge: 'Madrid · BiciMAD',
      title: 'Check BiciMAD in real time before you head out',
      description:
        'See bikes and docks at key Madrid stations and avoid unnecessary trips to empty or full bases.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you step outside', description: 'Open BiciMAD and confirm whether your usual base is worth it before you leave.' },
        { title: 'Avoid last-minute station changes', description: 'If that base does not fit, you choose another one before losing time on the move.' },
        { title: 'Resolve the doubt at a glance', description: 'Favorites, alerts, and quick access give you the answer without friction.' },
      ],
      faqTitle: 'Madrid FAQ',
      faq: [
        {
          question: 'What does BiciRadar solve for BiciMAD before I leave?',
          answer: 'It tells you whether your usual base has bikes or docks and whether you should head out now, wait, or switch stations.',
        },
        {
          question: 'Can I check BiciMAD quickly from my phone or watch?',
          answer: 'Yes. Favorites, widgets, and quick access let you resolve availability in seconds without opening the full app flow.',
        },
      ],
      mockup: { stationLabel: 'Sol Station', availability: '12 bikes · 5 docks' },
    },
    barcelona: {
      seo: {
        title: 'Bicing live availability with BiciRadar | Barcelona',
        description:
          'Check Bicing before you move, save favorites, and download BiciRadar for Barcelona.',
      },
      badge: 'Barcelona · Bicing',
      title: 'Check Bicing before you arrive at the station',
      description:
        'See bikes and open docks across Barcelona to decide whether to keep your route, switch bases, or wait.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide as you leave the metro', description: 'Check Bicing before stepping onto the street or leaving the office.' },
        { title: 'Avoid rush-hour detours', description: 'If that base is not worth it, switch stations before taking the extra loop.' },
        { title: 'Keep your routine one tap away', description: 'Your key stations stay ready so you can resolve the doubt in seconds.' },
      ],
      faqTitle: 'Barcelona FAQ',
      faq: [
        {
          question: 'What does BiciRadar solve for Bicing before I arrive?',
          answer: 'It helps you know whether that station is worth it or whether it is better to switch bases before taking the detour.',
        },
        {
          question: 'What if I already check Bicing in the official app?',
          answer: 'BiciRadar removes steps when you are in a hurry: open favorites, see availability, and make the decision earlier.',
        },
      ],
      mockup: { stationLabel: 'Pg. de Gràcia', availability: '8 bikes · 4 docks' },
    },
    sevilla: {
      seo: {
        title: 'Sevici live availability with BiciRadar | Seville',
        description:
          'Anticipate Sevici availability, save favorite stations, and download BiciRadar in Seville.',
      },
      badge: 'Seville · Sevici',
      title: 'Check Sevici in real time before you move',
      description:
        'See availability in Seville and avoid arriving without bikes or docks when you need a fast decision.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Leave with less uncertainty', description: 'Check Sevici right before you start and decide with context.' },
        { title: 'Keep your key base close', description: 'Your usual stations stay ready so you can resolve the doubt in seconds.' },
        { title: 'Return better without improvising', description: 'Get useful signals when docks show up again where you need them.' },
      ],
      faqTitle: 'Seville FAQ',
      faq: [
        {
          question: 'What decision does it help me make with Sevici before I move?',
          answer: 'Whether your usual station is worth heading to, whether you should wait a few minutes, or whether it is better to switch bases before losing time.',
        },
        {
          question: 'Can I keep Sevici close without opening the whole app?',
          answer: 'Yes. Favorites, widgets, and quick access let you see availability and act in seconds.',
        },
      ],
      mockup: { stationLabel: 'Puerta Jerez', availability: '6 bikes · 7 docks' },
    },
    valencia: {
      seo: {
        title: 'Valenbisi live availability with BiciRadar | Valencia',
        description:
          'Check Valenbisi, avoid full stations, and download BiciRadar for Valencia.',
      },
      badge: 'Valencia · Valenbisi',
      title: 'Check Valenbisi before you head to the station',
      description:
        'Get visibility into bikes and docks in Valencia so you do not improvise pickups or returns on the move.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Avoid arriving at a full base', description: 'Check open docks before you get close to your destination and decide better.' },
        { title: 'Choose the first station better', description: 'See which base works best for pickup or return without improvising.' },
        { title: 'Resolve your routine without starting from zero', description: 'Favorites, alerts, and quick access remove steps on every commute.' },
      ],
      faqTitle: 'Valencia FAQ',
      faq: [
        {
          question: 'What does BiciRadar solve for Valenbisi before I get close to the station?',
          answer: 'It helps you decide whether that base works for pickup or return before you start improvising on the move.',
        },
        {
          question: 'Can I check it quickly on iPhone or Android?',
          answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full flow.',
        },
      ],
      mockup: { stationLabel: 'Colón', availability: '10 bikes · 2 docks' },
    },
    zaragoza: {
      seo: {
        title: 'Bizi live availability with BiciRadar | Zaragoza',
        description:
          'Check Bizi in Zaragoza, open favorites fast, and download BiciRadar with local content.',
      },
      badge: 'Zaragoza · Bizi',
      title: 'Check Bizi in real time before you ride',
      description:
        'Open Bizi availability in Zaragoza, save regular stations, and reduce the time spent on daily checks.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you ride', description: 'Check whether your usual station has availability before you head out.' },
        { title: 'Avoid loops between bases', description: 'If that station is not worth it, change plans before losing time.' },
        { title: 'Check Bizi in seconds', description: 'Favorites and quick access let you resolve the doubt almost instantly.' },
      ],
      faqTitle: 'Zaragoza FAQ',
      faq: [
        {
          question: 'What does BiciRadar solve for Bizi before I ride?',
          answer: 'It helps you know whether your usual station has bikes or docks and whether it is worth heading out now or switching bases.',
        },
        {
          question: 'Can I share it with someone else in Zaragoza?',
          answer: 'Yes. If someone else has the same daily doubt, this page and the app help them decide before they move.',
        },
      ],
      mockup: { stationLabel: 'Plaza España', availability: '7 bikes · 6 docks' },
    },
    bilbao: {
      seo: {
        title: 'Bilbao live with BiciRadar | Bilbao',
        description: 'Check bikes and docks in Bilbao before leaving and download BiciRadar.',
      },
      badge: 'Bilbao · Bilbao',
      title: 'Check Bilbao live before leaving',
      description: 'See available bikes and docks in Bilbao before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Bilbao FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Bilbao before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Bilbao', availability: '5 bikes · 3 docks' },
    },
    murcia: {
      seo: {
        title: 'Murcia live with BiciRadar | Murcia',
        description: 'Check bikes and docks in Murcia before leaving and download BiciRadar.',
      },
      badge: 'Murcia · Murcia',
      title: 'Check Murcia live before leaving',
      description: 'See available bikes and docks in Murcia before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Murcia FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Murcia before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Murcia', availability: '4 bikes · 2 docks' },
    },
    leon: {
      seo: {
        title: 'León live with BiciRadar | León',
        description: 'Check bikes and docks in León before leaving and download BiciRadar.',
      },
      badge: 'León · León',
      title: 'Check León live before leaving',
      description: 'See available bikes and docks in León before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'León FAQ',
      faq: [
        { question: 'What does BiciRadar solve in León before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'León', availability: '3 bikes · 2 docks' },
    },
    valladolid: {
      seo: {
        title: 'Valladolid live with BiciRadar | Valladolid',
        description: 'Check bikes and docks in Valladolid before leaving and download BiciRadar.',
      },
      badge: 'Valladolid · Valladolid',
      title: 'Check Valladolid live before leaving',
      description: 'See available bikes and docks in Valladolid before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Valladolid FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Valladolid before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Valladolid', availability: '4 bikes · 3 docks' },
    },
    palma: {
      seo: {
        title: 'Palma live with BiciRadar | Palma',
        description: 'Check bikes and docks in Palma before leaving and download BiciRadar.',
      },
      badge: 'Palma · Palma',
      title: 'Check Palma live before leaving',
      description: 'See available bikes and docks in Palma before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Palma FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Palma before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Palma', availability: '5 bikes · 4 docks' },
    },
    las_palmas: {
      seo: {
        title: 'Las Palmas live with BiciRadar | Las Palmas',
        description: 'Check bikes and docks in Las Palmas before leaving and download BiciRadar.',
      },
      badge: 'Las Palmas · Las Palmas',
      title: 'Check Las Palmas live before leaving',
      description: 'See available bikes and docks in Las Palmas before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Las Palmas FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Las Palmas before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Las Palmas', availability: '4 bikes · 3 docks' },
    },
    a_coruna: {
      seo: {
        title: 'A Coruña live with BiciRadar | A Coruña',
        description: 'Check bikes and docks in A Coruña before leaving and download BiciRadar.',
      },
      badge: 'A Coruña · A Coruña',
      title: 'Check A Coruña live before leaving',
      description: 'See available bikes and docks in A Coruña before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'A Coruña FAQ',
      faq: [
        { question: 'What does BiciRadar solve in A Coruña before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'A Coruña', availability: '3 bikes · 2 docks' },
    },
    gijon: {
      seo: {
        title: 'Gijón live with BiciRadar | Gijón',
        description: 'Check bikes and docks in Gijón before leaving and download BiciRadar.',
      },
      badge: 'Gijón · Gijón',
      title: 'Check Gijón live before leaving',
      description: 'See available bikes and docks in Gijón before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Gijón FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Gijón before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Gijón', availability: '4 bikes · 3 docks' },
    },
    vitoria_gasteiz: {
      seo: {
        title: 'Vitoria-Gasteiz live with BiciRadar | Vitoria',
        description: 'Check bikes and docks in Vitoria before leaving and download BiciRadar.',
      },
      badge: 'Vitoria-Gasteiz · Vitoria',
      title: 'Check Vitoria-Gasteiz live before leaving',
      description: 'See available bikes and docks in Vitoria before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Vitoria FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Vitoria-Gasteiz before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Vitoria', availability: '3 bikes · 2 docks' },
    },
    pamplona: {
      seo: {
        title: 'Pamplona live with BiciRadar | Pamplona',
        description: 'Check bikes and docks in Pamplona before leaving and download BiciRadar.',
      },
      badge: 'Pamplona · Pamplona',
      title: 'Check Pamplona live before leaving',
      description: 'See available bikes and docks in Pamplona before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Pamplona FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Pamplona before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Pamplona', availability: '5 bikes · 4 docks' },
    },
    castellon: {
      seo: {
        title: 'Castellón live with BiciRadar | Castellón',
        description: 'Check bikes and docks in Castellón before leaving and download BiciRadar.',
      },
      badge: 'Castellón · Castellón',
      title: 'Check Castellón live before leaving',
      description: 'See available bikes and docks in Castellón before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Castellón FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Castellón before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Castellón', availability: '3 bikes · 2 docks' },
    },
    santander: {
      seo: {
        title: 'Santander live with BiciRadar | Santander',
        description: 'Check bikes and docks in Santander before leaving and download BiciRadar.',
      },
      badge: 'Santander · Santander',
      title: 'Check Santander live before leaving',
      description: 'See available bikes and docks in Santander before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Santander FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Santander before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Santander', availability: '4 bikes · 3 docks' },
    },
    girona: {
      seo: {
        title: 'Girona live with BiciRadar | Girona',
        description: 'Check bikes and docks in Girona before leaving and download BiciRadar.',
      },
      badge: 'Girona · Girona',
      title: 'Check Girona live before leaving',
      description: 'See available bikes and docks in Girona before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Girona FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Girona before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Girona', availability: '3 bikes · 2 docks' },
    },
    gran_canaria: {
      seo: {
        title: 'Gran Canaria live with BiciRadar | Gran Canaria',
        description: 'Check bikes and docks in Gran Canaria before leaving and download BiciRadar.',
      },
      badge: 'Gran Canaria · Gran Canaria',
      title: 'Check Gran Canaria live before leaving',
      description: 'See available bikes and docks in Gran Canaria before you head out.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Decide before you leave', description: 'Check whether your usual station has bikes or docks before you start moving.' },
        { title: 'Avoid unnecessary detours', description: 'If that base is not worth it, change plans before you lose time on the street.' },
        { title: 'Check it at a glance', description: 'Open favorites, widget, or phone and resolve the doubt in seconds.' },
      ],
      faqTitle: 'Gran Canaria FAQ',
      faq: [
        { question: 'What does BiciRadar solve in Gran Canaria before I head out?', answer: 'It helps you know whether your usual station has availability and whether you should leave now, wait, or switch bases.' },
        { question: 'Can I check it quickly from my phone or watch?', answer: 'Yes. Availability stays close through favorites, widgets, and quick access so you can decide without opening the full app flow.' },
      ],
      mockup: { stationLabel: 'Gran Canaria', availability: '4 bikes · 3 docks' },
    },
  },
} satisfies LocaleContent;

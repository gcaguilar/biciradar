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
    siteTagline: 'Check bikes and docks before you arrive',
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
    heroCta: "We'll email it to you",
    heroSecondaryCta: 'See how it works',
    finalCtaTitle: 'Download it now or get the link by email',
    finalCtaDescription:
      'BiciRadar is now available on the App Store and Google Play. If you prefer, leave your email and we will send the link.',
    cityRevisitLabel: 'Browse city pages again',
    shareDescription: 'Share BiciRadar with someone else in your city.',
    betaInviteLabel: 'Also by email',
    faqLabel: 'Frequently asked questions',
    thankYouShareFallback: 'Link copied. You can share it now.',
    githubAriaLabel: 'Source code on GitHub',
  },
  home: {
    seo: {
      title: 'BiciRadar | Check bikes and docks in real time before you arrive',
      description:
        'BiciRadar helps you see availability across BiciMAD, Bicing, Sevici, Valenbisi, Bizi, and more. Save favorites, get alerts, and open them fast from widgets.',
    },
    header: {
      sections: [
        { id: 'problem', label: 'Problem' },
        { id: 'solution', label: 'Solution' },
        { id: 'how-it-works', label: 'How it works' },
        { id: 'cities', label: 'Cities' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Download the app',
      languageLabel: 'Language',
    },
    hero: {
      title: 'Check bikes and docks in real time before you arrive',
      description:
        'BiciRadar helps you see availability across bike-share systems like BiciMAD, Bicing, Sevici, and more. Save favorites, get alerts, and open them fast from widgets.',
      primaryCta: 'Download on the App Store',
      secondaryCta: 'Get it on Google Play',
      microcopy: '',
      primaryBadge: 'iPhone · Available now',
      secondaryBadge: 'Android · Available now',
      mockup: {
        stationLabel: 'Sol Station',
        availability: '12 bikes · 5 docks',
        bikesLabel: 'Ready bikes',
        docksLabel: 'Open docks',
        chips: ['Favorites', 'Alerts', 'Widgets'],
        shortcutLabel: 'Open morning commute',
      },
    },
    problem: {
      title: 'The problem is not taking the bike. It is arriving and finding nothing.',
      intro:
        'Walking to a station without knowing whether there will be bikes or docks adds friction exactly when you are in a hurry.',
      cards: [
        {
          title: 'You arrive and there are no bikes left',
          description: 'You lose time switching stations once you are already moving.',
        },
        {
          title: 'You arrive and there are no docks left',
          description: 'Returns also fail when you cannot anticipate availability.',
        },
        {
          title: 'Too many steps for a simple check',
          description: 'Opening the app, searching, refreshing, and comparing slows down an instant decision.',
        },
      ],
    },
    solution: {
      title: 'Solution: useful information before you move',
      cards: [
        {
          title: 'Real time',
          description: 'Check bikes and docks before you leave home or the office.',
        },
        {
          title: 'Favorites',
          description: 'Save your key stations and keep them first every time.',
        },
        {
          title: 'Alerts',
          description: 'Get notified when a station has bikes or docks available.',
        },
        {
          title: 'Widgets',
          description: 'Open the info you need from your home screen or watch.',
        },
      ],
    },
    howItWorks: {
      title: 'How it works',
      steps: [
        {
          number: '1',
          title: 'Choose city and system',
          description: 'Start with the public bike network you already use.',
        },
        {
          number: '2',
          title: 'Save favorites',
          description: 'Pin daily stations so you can check status in seconds.',
        },
        {
          number: '3',
          title: 'Check or get alerts',
          description: 'Decide before arriving whether to go, wait, or switch stations.',
        },
      ],
    },
    cities: {
      title: 'Cities and systems',
      description:
        'The structure is ready to scale city by city without cloning the whole landing page.',
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
      moreLabel: 'More compatible cities coming soon',
    },
    midCta: {
      title: 'Two quick entry points by platform',
      description:
        'Download BiciRadar now on iPhone or Android. If you prefer, we can email you the link below.',
      primaryCta: {
        label: 'Download on the App Store',
        note: 'Available now on iPhone',
      },
      secondaryCta: {
        label: 'Get it on Google Play',
        note: 'Available now on Android',
      },
    },
    betaForm: {
      title: "We'll email you the link",
      description: 'Leave your email and we will send the download link for your platform.',
      honeypotLabel: 'Company',
      consentLabel:
        'I agree that BiciRadar can use these details to send me the download link and contact me about this request.',
      consentHint: 'We only use this information to send the link and help with this request.',
      submitLabel: 'Email me the link',
      loadingLabel: 'Sending link...',
      successTitle: 'Link sent',
      successMessage: 'We will email you the link in a few minutes.',
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
      title: 'FAQ',
      items: [
        {
          question: 'If I already use the official app, why download BiciRadar?',
          answer:
            'Because it is built for fast everyday checks: you see bikes and free docks before you leave, save favorites, get alerts, and open what you need from widgets or your watch in fewer steps.',
        },
        {
          question: 'Does it really save time day to day?',
          answer:
            'Yes. It cuts friction when you are in a hurry: you can open your usual stations right away and often check availability without having to move through the full official app flow.',
        },
        {
          question: 'Do I need to create an account to download it?',
          answer:
            'No. You can go straight to the App Store or Google Play to download it. If you prefer to continue later, you can also leave your email and we will send you the link.',
        },
        {
          question: 'Which cities can I check right now?',
          answer:
            'Right now we highlight Madrid, Barcelona, Seville, Valencia, and Zaragoza, and the landing already shows more supported cities so you can quickly check whether yours is included.',
        },
        {
          question: 'Is the data reliable?',
          answer:
            'It uses official city open-data sources. As with any real-time system, small delays can happen, but the data reference is the official one.',
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
      primaryCta: 'Email me the link',
      note: 'BiciRadar · iPhone and Android · Data based on official sources',
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
    title: "Thanks, we'll email you the link",
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
        { title: 'Avoid empty stations', description: 'Decide before going to a BiciMAD base.' },
        { title: 'Plan urban trips', description: 'Combine favorites and alerts on daily routes.' },
        { title: 'Get useful alerts', description: 'Watch key zones when you need to return or pick up fast.' },
      ],
      faqTitle: 'Madrid FAQ',
      faq: [
        {
          question: 'Does it work with BiciMAD?',
          answer: 'Yes. The page is ready for BiciMAD-specific content and local demand.',
        },
        {
          question: 'Can I also use BiciRadar on Android in Madrid?',
          answer: 'Yes. It is already available on Google Play and, if you want, we can also email you the link.',
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
        { title: 'Fewer detours at rush hour', description: 'Check Bicing before leaving the metro or office.' },
        { title: 'Favorites for your routine', description: 'Keep home, work, and campus stations ready.' },
        { title: 'Widgets for fast checks', description: 'Open Bicing status without launching the full app.' },
      ],
      faqTitle: 'Barcelona FAQ',
      faq: [
        {
          question: 'Can I use it with Bicing if I already have the official app?',
          answer: 'Yes. BiciRadar complements daily use with faster checks and shortcuts.',
        },
        {
          question: 'Is Barcelona also available in the app?',
          answer: 'Yes. Barcelona already has dedicated local content and we will keep prioritizing improvements based on demand.',
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
        { title: 'Less uncertainty when leaving', description: 'Check Sevici right before you start your trip.' },
        { title: 'Fast access to favorites', description: 'Keep key stations close for home, work, or study.' },
        { title: 'Alerts for smarter returns', description: 'Get notified when docks become available again.' },
      ],
      faqTitle: 'Seville FAQ',
      faq: [
        {
          question: 'Is Sevici already included in BiciRadar?',
          answer: 'Yes. This landing is already set up to capture Seville interest with dedicated SEO.',
        },
        {
          question: 'Can I leave my email to receive the link?',
          answer: 'Yes. We will send you the download link and it will also help us understand which city you are visiting from.',
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
        { title: 'Avoid full stations', description: 'Check open docks before reaching your destination.' },
        { title: 'Improve the first decision', description: 'See the best nearby base with less friction.' },
        { title: 'Save repeated commutes', description: 'Reuse key checks with favorites, alerts, and widgets.' },
      ],
      faqTitle: 'Valencia FAQ',
      faq: [
        {
          question: 'Does BiciRadar work for Valenbisi?',
          answer: 'Yes. It is ready for Valencia-specific pages and campaigns.',
        },
        {
          question: 'Is it also available on Android?',
          answer: 'Yes. You can already download it from Google Play or ask us to email you the link.',
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
        { title: 'Fast Bizi checks', description: 'Open frequent station status in seconds.' },
        { title: 'Local SEO-ready content', description: 'Each city gets dedicated copy, FAQ, and metadata.' },
        { title: 'City-scalable structure', description: 'We capture Zaragoza demand without duplicating the architecture.' },
      ],
      faqTitle: 'Zaragoza FAQ',
      faq: [
        {
          question: 'Is Zaragoza already included?',
          answer: 'Yes. It is already one of the initial local pages with direct access to the app.',
        },
        {
          question: 'Can I share this page with other people in Zaragoza?',
          answer: 'Yes. Please share it so we can measure local demand more accurately.',
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Bilbao FAQ',
      faq: [
        { question: 'Does it work with Bilbao?', answer: 'Yes. You can check bikes and docks in Bilbao.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Murcia FAQ',
      faq: [
        { question: 'Does it work with Murcia?', answer: 'Yes. You can check bikes and docks in Murcia.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'León FAQ',
      faq: [
        { question: 'Does it work with León?', answer: 'Yes. You can check bikes and docks in León.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Valladolid FAQ',
      faq: [
        { question: 'Does it work with Valladolid?', answer: 'Yes. You can check bikes and docks in Valladolid.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Palma FAQ',
      faq: [
        { question: 'Does it work with Palma?', answer: 'Yes. You can check bikes and docks in Palma.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Las Palmas FAQ',
      faq: [
        { question: 'Does it work with Las Palmas?', answer: 'Yes. You can check bikes and docks in Las Palmas.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'A Coruña FAQ',
      faq: [
        { question: 'Does it work with A Coruña?', answer: 'Yes. You can check bikes and docks in A Coruña.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Gijón FAQ',
      faq: [
        { question: 'Does it work with Gijón?', answer: 'Yes. You can check bikes and docks in Gijón.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Vitoria FAQ',
      faq: [
        { question: 'Does it work with Vitoria?', answer: 'Yes. You can check bikes and docks in Vitoria.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Pamplona FAQ',
      faq: [
        { question: 'Does it work with Pamplona?', answer: 'Yes. You can check bikes and docks in Pamplona.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Castellón FAQ',
      faq: [
        { question: 'Does it work with Castellón?', answer: 'Yes. You can check bikes and docks in Castellón.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Santander FAQ',
      faq: [
        { question: 'Does it work with Santander?', answer: 'Yes. You can check bikes and docks in Santander.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Girona FAQ',
      faq: [
        { question: 'Does it work with Girona?', answer: 'Yes. You can check bikes and docks in Girona.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
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
        { title: 'Real time check', description: 'Check bikes and docks before leaving.' },
        { title: 'Favorites', description: 'Save your key stations for quick access.' },
        { title: 'Alerts', description: 'Get notified when bikes or docks are available.' },
      ],
      faqTitle: 'Gran Canaria FAQ',
      faq: [
        { question: 'Does it work with Gran Canaria?', answer: 'Yes. You can check bikes and docks in Gran Canaria.' },
        { question: 'Is there an Android app?', answer: 'Yes. Available on Google Play.' },
      ],
      mockup: { stationLabel: 'Gran Canaria', availability: '4 bikes · 3 docks' },
    },
  },
} satisfies LocaleContent;

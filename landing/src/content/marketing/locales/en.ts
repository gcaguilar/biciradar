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
      'See live availability across shared-bike systems, save favorites, and request beta access to BiciRadar.',
  },
  common: {
    skipToContent: 'Skip to content',
    appStoreLabel: 'Download on the App Store',
    androidBetaLabel: 'Request beta access',
    openMenu: 'Open navigation',
    closeMenu: 'Close navigation',
    backToHome: 'Back to home',
    viewCities: 'View cities',
    cityPageCta: 'View local page',
    heroCta: 'Join the beta',
    heroSecondaryCta: 'See how it works',
    finalCtaTitle: 'Check before you ride',
    finalCtaDescription:
      'Request beta access and help us prioritize the next city rollouts with real demand.',
    cityRevisitLabel: 'Browse city pages again',
    shareDescription: 'Share the beta with someone else in your city.',
    betaInviteLabel: 'Open invite access',
    faqLabel: 'Frequently asked questions',
    thankYouShareFallback: 'Link copied. You can share it now.',
    githubAriaLabel: 'Source code on GitHub',
  },
  home: {
    seo: {
      title: 'BiciRadar beta | Check bikes and docks in real time before you arrive',
      description:
        'BiciRadar helps you see availability across BiciMAD, Bicing, Sevici, Valenbisi, Bizi, and more. Save favorites, get alerts, and join the beta.',
    },
    header: {
      sections: [
        { id: 'problem', label: 'Problem' },
        { id: 'solution', label: 'Solution' },
        { id: 'how-it-works', label: 'How it works' },
        { id: 'cities', label: 'Cities' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Join the beta',
      languageLabel: 'Language',
    },
    hero: {
      title: 'Check bikes and docks in real time before you arrive',
      description:
        'BiciRadar helps you see availability across bike-share systems like BiciMAD, Bicing, Sevici, and more. Save favorites, get alerts, and open them fast from widgets.',
      primaryCta: 'Download on the App Store',
      secondaryCta: 'Request beta access',
      microcopy: 'Request access with your platform and city; we prioritize the beta by demand in each system.',
      primaryBadge: 'iPhone · Available now',
      secondaryBadge: 'Android · Closed beta',
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
        'The beta flow is built to capture demand now and scale invitations by platform and city.',
      primaryCta: {
        label: 'Download on the App Store',
        note: 'Available now on iPhone',
      },
      secondaryCta: {
        label: 'Request beta access',
        note: 'Android in controlled access',
      },
    },
    betaForm: {
      title: 'Beta access request',
      description:
        'Tell us your operating system and the city where you ride. With your consent we can manage your interest in the beta.',
      helper:
        'Each city maps to its system: Madrid · BiciMAD, Barcelona · Bicing, Seville · Sevici, Valencia · Valenbisi, Zaragoza · Bizi.',
      honeypotLabel: 'Company',
      consentLabel:
        'I agree that BiciRadar can use these details to manage beta access and contact me about this request.',
      consentHint: 'We only use this information for the beta process.',
      submitLabel: 'Request beta access',
      loadingLabel: 'Sending request...',
      successTitle: 'Request sent',
      successMessage: 'Request sent. Thank you for your interest.',
      errorMessage: 'We could not send your request. Please try again in a few seconds.',
      options: {
        operatingSystems,
        cities: [
          { value: 'madrid', label: 'Madrid · BiciMAD' },
          { value: 'barcelona', label: 'Barcelona · Bicing' },
          { value: 'sevilla', label: 'Seville · Sevici' },
          { value: 'valencia', label: 'Valencia · Valenbisi' },
          { value: 'zaragoza', label: 'Zaragoza · Bizi' },
        ],
      },
      fields: {
        operatingSystem: {
          label: 'Operating system',
          placeholder: 'Choose your operating system',
        },
        city: { label: 'City and system', placeholder: 'Choose city and system' },
      },
      validation: {
        required: 'Please complete this field.',
        consent: 'We need your consent to manage the beta.',
        turnstile: 'Complete the security verification and try again.',
        server: 'There was a problem sending your request.',
      },
    },
    faq: {
      title: 'FAQ',
      items: [
        {
          question: 'If I already use the official app, what does BiciRadar add?',
          answer:
            'Speed for what matters most: nearest station, available bikes, free docks, favorites, station status, and route.',
        },
        {
          question: 'Does it really save time?',
          answer:
            'Yes. It includes voice and shortcuts on iPhone, Apple Watch, and Android to solve queries in fewer steps, sometimes without opening the app.',
        },
        {
          question: 'Is it only for mobile?',
          answer:
            'No. It is designed for phone and watch: Android, iOS, Wear OS, and Apple Watch.',
        },
        {
          question: 'Is the data reliable?',
          answer:
            'It uses official city open-data sources. Small delays can happen as in any real-time system.',
        },
        {
          question: 'Why use BiciRadar as a complement?',
          answer:
            'The official app is the institutional channel; BiciRadar is optimized for faster, lower-friction daily checks.',
        },
        {
          question: 'What will you do with the data from the form?',
          answer:
            'We use operating system and city to manage beta interest and prioritize demand. We do not store submissions in a local file; the team gets an internal notification to follow up.',
        },
      ],
    },
    footer: {
      tagline: 'Shared-bike beta built for conversion, local SEO, and city-by-city scale.',
      links: [
        { label: 'Privacy', href: 'mailto:hola@biciradar.es?subject=Beta%20privacy' },
        { label: 'Contact', href: 'mailto:hola@biciradar.es?subject=BiciRadar%20beta' },
        { label: 'Support', href: 'mailto:soporte@biciradar.es?subject=BiciRadar%20support' },
      ],
      primaryCta: 'Join the beta',
      note: 'BiciRadar · iPhone and Android · Data based on official sources',
      githubLine: 'Open source on GitHub',
    },
  },
  thankYou: {
    seo: {
      title: 'Thanks for joining the BiciRadar beta',
      description:
        'Your beta request was sent successfully. Explore the app, supported cities, or share the beta.',
    },
    badge: 'Request sent',
    title: 'Thanks, we received your request',
    description:
      'Your beta interest is recorded. You can install BiciRadar now or browse local city pages.',
    steps: [
      'Download BiciRadar from the App Store or Google Play if you want to try it now.',
      'Browse local pages to see live availability.',
      'Questions? Email hola@biciradar.es.',
    ],
    cityLinksTitle: 'Explore supported cities in the meantime',
    cityCardCtaPrefix: 'View',
    shareLabel: 'Share the beta',
    primaryCta: 'Explore supported cities in the meantime',
    appStoreCta: 'Download on the App Store',
    playStoreCta: 'Get it on Google Play',
    footnote: 'Questions? hola@biciradar.es',
  },
  cityPages: {
    madrid: {
      seo: {
        title: 'BiciMAD live availability with BiciRadar | Madrid beta',
        description:
          'Check BiciMAD before you leave, avoid empty stations, and request BiciRadar beta access for Madrid.',
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
          question: 'Is Madrid also included in Android beta access?',
          answer: 'Yes. You can request access and we will invite users in waves.',
        },
      ],
      mockup: { stationLabel: 'Sol Station', availability: '12 bikes · 5 docks' },
    },
    barcelona: {
      seo: {
        title: 'Bicing live availability with BiciRadar | Barcelona beta',
        description:
          'Check Bicing before you move, save favorites, and request BiciRadar beta access for Barcelona.',
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
          question: 'Will Barcelona receive beta invites?',
          answer: 'Yes. We are collecting demand to prioritize access by city.',
        },
      ],
      mockup: { stationLabel: 'Pg. de Gràcia', availability: '8 bikes · 4 docks' },
    },
    sevilla: {
      seo: {
        title: 'Sevici live availability with BiciRadar | Seville beta',
        description:
          'Anticipate Sevici availability, save favorite stations, and request BiciRadar beta access in Seville.',
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
          question: 'Is Sevici part of the beta scope?',
          answer: 'Yes. The landing is already ready to capture local Seville demand with dedicated SEO.',
        },
        {
          question: 'Can I leave my details even if invitations are not immediate yet?',
          answer: 'Yes. We keep your interest to prioritize future city openings.',
        },
      ],
      mockup: { stationLabel: 'Puerta Jerez', availability: '6 bikes · 7 docks' },
    },
    valencia: {
      seo: {
        title: 'Valenbisi live availability with BiciRadar | Valencia beta',
        description:
          'Check Valenbisi, avoid full stations, and request BiciRadar beta access for Valencia.',
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
          question: 'Will Android beta access also be available?',
          answer: 'Yes. You can sign up from this page and we will contact you by email.',
        },
      ],
      mockup: { stationLabel: 'Colón', availability: '10 bikes · 2 docks' },
    },
    zaragoza: {
      seo: {
        title: 'Bizi live availability with BiciRadar | Zaragoza beta',
        description:
          'Check Bizi in Zaragoza, open favorites fast, and request BiciRadar beta access with local content.',
      },
      badge: 'Zaragoza · Bizi',
      title: 'Check Bizi in real time before you ride',
      description:
        'Open Bizi availability in Zaragoza, save regular stations, and reduce the time spent on daily checks.',
      benefitsTitle: 'Local benefits',
      benefits: [
        { title: 'Fast Bizi checks', description: 'Open frequent station status in seconds.' },
        { title: 'Local SEO-ready content', description: 'Each city gets dedicated copy, FAQ, and metadata.' },
        { title: 'City-scalable beta', description: 'We capture Zaragoza demand without duplicating the architecture.' },
      ],
      faqTitle: 'Zaragoza FAQ',
      faq: [
        {
          question: 'Is Zaragoza included in the first phase?',
          answer: 'Yes. It is already set up as one of the initial city pages with beta CTA.',
        },
        {
          question: 'Can I share this page with other people in Zaragoza?',
          answer: 'Yes. Please share it so we can measure local demand more accurately.',
        },
      ],
      mockup: { stationLabel: 'Plaza España', availability: '7 bikes · 6 docks' },
    },
  },
} satisfies LocaleContent;

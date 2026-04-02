export const locales = ['es', 'en', 'ca', 'gl', 'eu'] as const;
export type Locale = (typeof locales)[number];

export const defaultLocale: Locale = 'es';

export const cityKeys = [
  'madrid',
  'barcelona',
  'sevilla',
  'valencia',
  'zaragoza',
] as const;
export type CityKey = (typeof cityKeys)[number];

export interface SeoContent {
  title: string;
  description: string;
}

export interface CallToActionContent {
  label: string;
  href?: string;
  note?: string;
  event?: string;
}

export interface SectionLinkContent {
  id: string;
  label: string;
}

export interface ProblemCardContent {
  title: string;
  description: string;
}

export interface SolutionCardContent {
  title: string;
  description: string;
}

export interface StepContent {
  number: string;
  title: string;
  description: string;
}

export interface CityCardContent {
  key: CityKey;
  name: string;
  system: string;
  description: string;
}

export interface FaqItemContent {
  question: string;
  answer: string;
}

export interface HeroMockupContent {
  stationLabel: string;
  availability: string;
  bikesLabel: string;
  docksLabel: string;
  chips: string[];
  shortcutLabel: string;
}

export interface HomeContent {
  seo: SeoContent;
  header: {
    sections: SectionLinkContent[];
    primaryCta: string;
    languageLabel: string;
  };
  hero: {
    title: string;
    description: string;
    primaryCta: string;
    secondaryCta: string;
    microcopy: string;
    primaryBadge: string;
    secondaryBadge: string;
    mockup: HeroMockupContent;
  };
  problem: {
    title: string;
    intro: string;
    cards: ProblemCardContent[];
  };
  solution: {
    title: string;
    cards: SolutionCardContent[];
  };
  howItWorks: {
    title: string;
    steps: StepContent[];
  };
  cities: {
    title: string;
    description: string;
    items: CityCardContent[];
    moreLabel: string;
  };
  midCta: {
    title: string;
    description: string;
    primaryCta: CallToActionContent;
    secondaryCta: CallToActionContent;
  };
  betaForm: {
    title: string;
    description: string;
    helper: string;
    honeypotLabel: string;
    consentLabel: string;
    consentHint: string;
    submitLabel: string;
    loadingLabel: string;
    successTitle: string;
    successMessage: string;
    errorMessage: string;
    options: {
      operatingSystems: Array<{ value: string; label: string }>;
      cities: Array<{ value: string; label: string }>;
      systems: Array<{ value: string; label: string }>;
      frequencies: Array<{ value: string; label: string }>;
      interests: Array<{ value: string; label: string }>;
      yesNo: Array<{ value: string; label: string }>;
    };
    fields: {
      email: { label: string; placeholder: string };
      operatingSystem: { label: string; placeholder: string };
      city: { label: string; placeholder: string };
      bikeSystem: { label: string; placeholder: string };
      frequency: { label: string; placeholder: string };
      interest: { label: string; placeholder: string };
      widgets: { label: string; placeholder: string };
      smartwatch: { label: string; placeholder: string };
    };
    validation: {
      required: string;
      email: string;
      consent: string;
      turnstile: string;
      server: string;
    };
  };
  faq: {
    title: string;
    items: FaqItemContent[];
  };
  footer: {
    tagline: string;
    links: Array<{ label: string; href: string }>;
    primaryCta: string;
    note: string;
    githubLine: string;
  };
}

export interface CityPageContent {
  seo: SeoContent;
  badge: string;
  title: string;
  description: string;
  benefitsTitle: string;
  benefits: ProblemCardContent[];
  faqTitle: string;
  faq: FaqItemContent[];
  mockup: {
    stationLabel: string;
    availability: string;
  };
}

export interface ThankYouVariantContent {
  title: string;
  description: string;
  steps: string[];
}

export interface ThankYouAndroidOrBothContent {
  title: string;
  description: string;
  stepsAndroid: string[];
  stepsBoth: string[];
}

export interface ThankYouContent {
  seo: SeoContent;
  badge: string;
  ios: ThankYouVariantContent;
  androidOrBoth: ThankYouAndroidOrBothContent;
  cityLinksTitle: string;
  cityCardCtaPrefix: string;
  shareLabel: string;
  primaryCta: string;
  appStoreCta: string;
  playStoreCta: string;
  googleGroupCta: string;
  footnote: string;
}

export interface LocaleContent {
  locale: Locale;
  languageName: string;
  localeLabel: string;
  metadata: {
    siteName: string;
    siteTagline: string;
    defaultOgTitle: string;
    defaultOgDescription: string;
  };
  common: {
    skipToContent: string;
    appStoreLabel: string;
    androidBetaLabel: string;
    openMenu: string;
    closeMenu: string;
    backToHome: string;
    viewCities: string;
    cityPageCta: string;
    heroCta: string;
    heroSecondaryCta: string;
    finalCtaTitle: string;
    finalCtaDescription: string;
    cityRevisitLabel: string;
    shareDescription: string;
    betaInviteLabel: string;
    faqLabel: string;
    thankYouShareFallback: string;
    githubAriaLabel: string;
  };
  home: HomeContent;
  thankYou: ThankYouContent;
  cityPages: Record<CityKey, CityPageContent>;
}

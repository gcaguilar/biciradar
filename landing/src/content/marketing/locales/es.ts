import type { LocaleContent } from '../types';

const operatingSystems = [
  { value: 'ios', label: 'iPhone / iOS' },
  { value: 'android', label: 'Android' },
  { value: 'both', label: 'Uso ambos' },
];

const frequencies = [
  { value: 'daily', label: 'Cada día' },
  { value: 'weekly', label: 'Varias veces por semana' },
  { value: 'occasional', label: 'De vez en cuando' },
];

const interests = [
  { value: 'bikes', label: 'Encontrar bicis' },
  { value: 'docks', label: 'Encontrar huecos' },
  { value: 'alerts', label: 'Recibir alertas' },
  { value: 'widgets', label: 'Usar widgets y accesos rápidos' },
];

const yesNo = [
  { value: 'yes', label: 'Sí' },
  { value: 'no', label: 'No' },
];

export const es = {
  locale: 'es',
  languageName: 'Castellano',
  localeLabel: 'ES',
  metadata: {
    siteName: 'BiciRadar',
    siteTagline: 'Consulta bicis y huecos antes de llegar',
    defaultOgTitle: 'BiciRadar | Consulta bicis y huecos en tiempo real',
    defaultOgDescription:
      'Comprueba disponibilidad en sistemas de bici compartida, guarda favoritas y solicita acceso a la beta de BiciRadar.',
  },
  common: {
    skipToContent: 'Saltar al contenido',
    appStoreLabel: 'Descargar en App Store',
    androidBetaLabel: 'Solicitar acceso beta',
    openMenu: 'Abrir navegación',
    closeMenu: 'Cerrar navegación',
    backToHome: 'Volver a la home',
    viewCities: 'Ver ciudades',
    cityPageCta: 'Ver página local',
    heroCta: 'Entrar en la beta',
    heroSecondaryCta: 'Ver cómo funciona',
    finalCtaTitle: 'Consulta antes de pedalear',
    finalCtaDescription:
      'Solicita acceso a la beta y deja preparada tu ciudad para cuando abramos nuevas invitaciones.',
    cityRevisitLabel: 'Volver a ver ciudades',
    shareDescription: 'Comparte la beta con otra persona de tu ciudad.',
    betaInviteLabel: 'Acceso por invitación pública',
    faqLabel: 'Preguntas frecuentes',
    thankYouShareFallback: 'Enlace copiado. Ya puedes compartirlo.',
  },
  home: {
    seo: {
      title: 'BiciRadar beta | Consulta bicis y huecos en tiempo real antes de llegar',
      description:
        'BiciRadar te ayuda a ver disponibilidad en sistemas como BiciMAD, Bicing, Sevici, Valenbisi y Bizi. Guarda favoritos, recibe alertas y entra en la beta.',
    },
    header: {
      sections: [
        { id: 'problema', label: 'Problema' },
        { id: 'solucion', label: 'Solución' },
        { id: 'como-funciona', label: 'Cómo funciona' },
        { id: 'ciudades', label: 'Ciudades' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Entrar en la beta',
      languageLabel: 'Idioma',
    },
    hero: {
      title: 'Consulta bicis y huecos en tiempo real antes de llegar',
      description:
        'BiciRadar te ayuda a ver disponibilidad en sistemas de bici compartida como BiciMAD, Bicing, Sevici y más. Guarda favoritos, recibe alertas y accede rápido desde widgets.',
      primaryCta: 'Descargar en App Store',
      secondaryCta: 'Solicitar acceso beta',
      microcopy: 'Solicita acceso y usaremos tu email para gestionar tu entrada en la beta de Android.',
      primaryBadge: 'iPhone · Disponible ahora',
      secondaryBadge: 'Android · Beta cerrada',
      mockup: {
        stationLabel: 'Estación Sol',
        availability: '12 bicis · 5 huecos',
        bikesLabel: 'Bicis listas',
        docksLabel: 'Huecos libres',
        chips: ['Favoritas', 'Alertas', 'Widgets'],
        shortcutLabel: 'Abrir trayecto de mañana',
      },
    },
    problem: {
      title: 'El problema no es coger la bici. Es llegar y que no haya.',
      intro:
        'Ir hasta una estación sin saber si habrá bicis o huecos añade fricción justo cuando más prisa tienes.',
      cards: [
        {
          title: 'Llegas y no quedan bicis',
          description: 'Pierdes tiempo cambiando de estación cuando ya estás en marcha.',
        },
        {
          title: 'Llegas y no hay huecos libres',
          description: 'La devolución también falla cuando no puedes anticipar disponibilidad.',
        },
        {
          title: 'Demasiados pasos para una consulta simple',
          description: 'Abrir la app, buscar, refrescar y comparar ralentiza una decisión que debería ser inmediata.',
        },
      ],
    },
    solution: {
      title: 'Solución: información útil antes de moverte',
      cards: [
        {
          title: 'Tiempo real',
          description: 'Consulta bicis y huecos antes de salir de casa o de la oficina.',
        },
        {
          title: 'Favoritas',
          description: 'Guarda tus estaciones clave para verlas primero cada vez.',
        },
        {
          title: 'Alertas',
          description: 'Recibe avisos cuando una estación tenga bicis o huecos disponibles.',
        },
        {
          title: 'Widgets',
          description: 'Abre la información que necesitas desde la pantalla principal o el reloj.',
        },
      ],
    },
    howItWorks: {
      title: 'Cómo funciona',
      steps: [
        {
          number: '1',
          title: 'Elige ciudad y sistema',
          description: 'Empiezas con la red de bici pública que usas a diario.',
        },
        {
          number: '2',
          title: 'Guarda favoritas',
          description: 'Fijas estaciones habituales para consultar estado en segundos.',
        },
        {
          number: '3',
          title: 'Consulta o recibe alertas',
          description: 'Decides antes de llegar si te conviene ir, esperar o cambiar de estación.',
        },
      ],
    },
    cities: {
      title: 'Ciudades y sistemas',
      description:
        'La arquitectura queda preparada para escalar por ciudad sin duplicar la landing completa.',
      items: [
        {
          key: 'madrid',
          name: 'Madrid',
          system: 'BiciMAD',
          description: 'Consulta bicis y huecos de BiciMAD antes de salir.',
        },
        {
          key: 'barcelona',
          name: 'Barcelona',
          system: 'Bicing',
          description: 'Encuentra una estación útil de Bicing cuando vas con prisa.',
        },
        {
          key: 'sevilla',
          name: 'Sevilla',
          system: 'Sevici',
          description: 'Anticipa disponibilidad de Sevici en tus trayectos diarios.',
        },
        {
          key: 'valencia',
          name: 'Valencia',
          system: 'Valenbisi',
          description: 'Evita vueltas innecesarias buscando huecos con Valenbisi.',
        },
        {
          key: 'zaragoza',
          name: 'Zaragoza',
          system: 'Bizi',
          description: 'Revisa Bizi en tiempo real y abre tus estaciones favoritas rápido.',
        },
      ],
      moreLabel: 'Más ciudades compatibles próximamente',
    },
    midCta: {
      title: 'Dos accesos rápidos según tu plataforma',
      description:
        'La beta está pensada para captar interés ahora y escalar la invitación por plataforma y ciudad.',
      primaryCta: {
        label: 'Descargar en App Store',
        note: 'Disponible ahora en iPhone',
      },
      secondaryCta: {
        label: 'Solicitar acceso beta',
        note: 'Android en acceso controlado',
      },
    },
    betaForm: {
      title: 'Acceso Android beta',
      description:
        'Usaremos tu email para gestionar tu acceso a la beta y priorizar ciudades con más demanda.',
      helper:
        'También nos sirve para medir el interés por sistema, plataforma, widgets y frecuencia de uso.',
      honeypotLabel: 'Empresa',
      consentLabel:
        'Acepto que BiciRadar use estos datos para gestionar mi acceso a la beta y contactar conmigo sobre esta solicitud.',
      consentHint: 'Solo usamos la información para el proceso beta.',
      submitLabel: 'Solicitar acceso beta',
      loadingLabel: 'Enviando solicitud...',
      successTitle: 'Solicitud enviada',
      successMessage: 'Hemos guardado tu solicitud. Te avisaremos cuando abramos tu acceso.',
      errorMessage: 'No hemos podido enviar tu solicitud. Inténtalo de nuevo en unos segundos.',
      options: {
        operatingSystems,
        cities: [
          { value: 'madrid', label: 'Madrid' },
          { value: 'barcelona', label: 'Barcelona' },
          { value: 'sevilla', label: 'Sevilla' },
          { value: 'valencia', label: 'Valencia' },
          { value: 'zaragoza', label: 'Zaragoza' },
          { value: 'other', label: 'Otra ciudad' },
        ],
        systems: [
          { value: 'bicimad', label: 'BiciMAD' },
          { value: 'bicing', label: 'Bicing' },
          { value: 'sevici', label: 'Sevici' },
          { value: 'valenbisi', label: 'Valenbisi' },
          { value: 'bizi', label: 'Bizi' },
          { value: 'other', label: 'Otro sistema' },
        ],
        frequencies,
        interests,
        yesNo,
      },
      fields: {
        email: { label: 'Email', placeholder: 'tu@email.com' },
        operatingSystem: {
          label: 'Sistema operativo',
          placeholder: 'Selecciona tu sistema operativo',
        },
        city: { label: 'Ciudad', placeholder: 'Selecciona tu ciudad principal' },
        bikeSystem: {
          label: 'Sistema de bici',
          placeholder: 'Selecciona el sistema que usas más',
        },
        frequency: {
          label: 'Frecuencia de uso',
          placeholder: '¿Con qué frecuencia usas bici compartida?',
        },
        interest: {
          label: 'Interés principal',
          placeholder: '¿Qué te interesa más de BiciRadar?',
        },
        widgets: {
          label: '¿Te interesan widgets?',
          placeholder: 'Selecciona una opción',
        },
        smartwatch: {
          label: '¿Usas smartwatch?',
          placeholder: 'Selecciona una opción',
        },
      },
      validation: {
        required: 'Completa este campo.',
        email: 'Introduce un email válido.',
        consent: 'Necesitamos tu consentimiento para gestionar la beta.',
        server: 'Ha habido un problema al guardar tu solicitud.',
      },
    },
    faq: {
      title: 'FAQ',
      items: [
        {
          question: '¿Si ya tengo la app oficial, qué me aporta BiciRadar?',
          answer:
            'Rapidez para lo que más miras al salir: estación más cercana, bicis disponibles, huecos libres, favoritos, estado y ruta.',
        },
        {
          question: '¿Me ahorra tiempo de verdad?',
          answer:
            'Sí. Incluye voz y atajos en iPhone, Apple Watch y Android para resolver consultas en menos pasos, a veces sin abrir la app.',
        },
        {
          question: '¿Solo sirve en el móvil?',
          answer:
            'No. Está pensado para móvil y reloj: Android, iOS, Wear OS y Apple Watch.',
        },
        {
          question: '¿Los datos son fiables?',
          answer:
            'Usa fuentes oficiales de open data de las ciudades. Puede haber pequeños desfases normales en sistemas en tiempo real.',
        },
        {
          question: '¿Por qué usar BiciRadar como complemento?',
          answer:
            'La app oficial es el canal institucional; BiciRadar está optimizado para consultar más rápido y con menos fricción.',
        },
        {
          question: '¿Qué vais a hacer con los datos del formulario?',
          answer:
            'Usaremos tu email únicamente para avisarte cuando puedas descargar la app y gestionar tu acceso a la beta. No lo usaremos para otros fines fuera de este proceso.',
        },
      ],
    },
    footer: {
      tagline: 'Beta de bici compartida orientada a conversión, SEO local y escalado por ciudad.',
      links: [
        { label: 'Privacidad', href: 'mailto:hola@biciradar.es?subject=Privacidad%20beta' },
        { label: 'Contacto', href: 'mailto:hola@biciradar.es?subject=BiciRadar%20beta' },
        { label: 'Soporte', href: 'mailto:soporte@biciradar.es?subject=Soporte%20BiciRadar' },
      ],
      primaryCta: 'Entrar en la beta',
      note: 'BiciRadar · iPhone y Android · Datos basados en fuentes oficiales',
    },
  },
  thankYou: {
    seo: {
      title: 'Gracias por apuntarte a la beta de BiciRadar',
      description:
        'Tu solicitud para la beta se ha enviado correctamente. Revisa próximos pasos, comparte la beta y visita páginas locales.',
    },
    badge: 'Solicitud enviada',
    title: 'Gracias. Tu solicitud para Android beta se ha enviado correctamente.',
    description:
      'Revisaremos tu solicitud y usaremos tu email para gestionar el acceso al canal beta y priorizar ciudades con mayor demanda.',
    steps: [
      'Confirmamos tu solicitud.',
      'Priorizamos invitaciones según ciudad, sistema y plataforma.',
      'Recibes instrucciones por email cuando haya acceso disponible.',
    ],
    cityLinksTitle: 'Explora ciudades disponibles mientras tanto',
    cityCardCtaPrefix: 'Ver',
    shareLabel: 'Compartir la beta',
    primaryCta: 'Explora ciudades disponibles mientras tanto',
    secondaryCta: 'Descargar en App Store',
    footnote: '¿No recibes email? Escríbenos a soporte.',
  },
  cityPages: {
    madrid: {
      seo: {
        title: 'BiciMAD en tiempo real con BiciRadar | Beta Madrid',
        description:
          'Consulta BiciMAD antes de salir, evita estaciones vacías y entra en la beta de BiciRadar para Madrid.',
      },
      badge: 'Madrid · BiciMAD',
      title: 'Consulta BiciMAD en tiempo real antes de salir',
      description:
        'Comprueba bicis y huecos en estaciones clave de Madrid y evita viajes innecesarios hacia bases vacías o llenas.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Evita estaciones vacías', description: 'Decide antes de ir a una base de BiciMAD.' },
        { title: 'Planifica trayectos urbanos', description: 'Combina favoritas y alertas para tus recorridos diarios.' },
        { title: 'Recibe alertas útiles', description: 'Vigila zonas clave cuando necesitas devolver o coger bici rápido.' },
      ],
      faqTitle: 'FAQ Madrid',
      faq: [
        {
          question: '¿Funciona con BiciMAD?',
          answer: 'Sí. La página está preparada para contenido específico de BiciMAD y su demanda local.',
        },
        {
          question: '¿Android en Madrid también entra en beta?',
          answer: 'Sí. Puedes solicitar acceso y te iremos invitando por fases.',
        },
      ],
      mockup: { stationLabel: 'Estación Sol', availability: '12 bicis · 5 huecos' },
    },
    barcelona: {
      seo: {
        title: 'Bicing en tiempo real con BiciRadar | Beta Barcelona',
        description:
          'Consulta Bicing antes de moverte, guarda favoritas y solicita acceso a la beta de BiciRadar para Barcelona.',
      },
      badge: 'Barcelona · Bicing',
      title: 'Consulta Bicing antes de llegar a la estación',
      description:
        'Ve bicis y anclajes libres en Barcelona para decidir si te conviene mantener ruta, cambiar base o esperar.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Menos vueltas en horas punta', description: 'Consulta Bicing antes de salir del metro o la oficina.' },
        { title: 'Favoritas para tu rutina', description: 'Accede primero a estaciones cerca de casa, trabajo o universidad.' },
        { title: 'Widgets para consultas rápidas', description: 'Abre el estado de Bicing sin entrar a toda la app.' },
      ],
      faqTitle: 'FAQ Barcelona',
      faq: [
        {
          question: '¿Puedo usarlo con Bicing aunque ya tenga la app oficial?',
          answer: 'Sí. BiciRadar complementa el uso diario con accesos y consultas más rápidos.',
        },
        {
          question: '¿Barcelona tendrá invitaciones beta?',
          answer: 'Sí. Estamos recogiendo demanda para priorizar accesos por ciudad.',
        },
      ],
      mockup: { stationLabel: 'Pg. de Gràcia', availability: '8 bicis · 4 huecos' },
    },
    sevilla: {
      seo: {
        title: 'Sevici en tiempo real con BiciRadar | Beta Sevilla',
        description:
          'Anticipa disponibilidad de Sevici, guarda estaciones favoritas y solicita acceso a la beta de BiciRadar en Sevilla.',
      },
      badge: 'Sevilla · Sevici',
      title: 'Consulta Sevici en tiempo real antes de moverte',
      description:
        'Comprueba disponibilidad en Sevilla y evita llegar sin bicis o sin huecos cuando necesitas una decisión rápida.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Menos incertidumbre al salir', description: 'Consulta Sevici justo antes de arrancar tu trayecto.' },
        { title: 'Acceso rápido a favoritas', description: 'Ten a mano tus estaciones clave para casa, trabajo o estudio.' },
        { title: 'Alertas para devolver mejor', description: 'Recibe señales útiles cuando vuelva a haber huecos libres.' },
      ],
      faqTitle: 'FAQ Sevilla',
      faq: [
        {
          question: '¿Sevici está contemplado en la beta?',
          answer: 'Sí. La landing ya queda preparada para captar leads de Sevilla con SEO propio.',
        },
        {
          question: '¿Puedo dejar mis datos aunque aún no haya invitación inmediata?',
          answer: 'Sí. Guardamos tu interés para priorizar aperturas por ciudad.',
        },
      ],
      mockup: { stationLabel: 'Puerta Jerez', availability: '6 bicis · 7 huecos' },
    },
    valencia: {
      seo: {
        title: 'Valenbisi en tiempo real con BiciRadar | Beta Valencia',
        description:
          'Consulta Valenbisi, evita estaciones completas y solicita acceso a la beta de BiciRadar para Valencia.',
      },
      badge: 'Valencia · Valenbisi',
      title: 'Consulta Valenbisi antes de acercarte a la estación',
      description:
        'Ten visibilidad de bicis y huecos en Valencia para no improvisar la devolución o la recogida sobre la marcha.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Evita estaciones completas', description: 'Comprueba huecos antes de llegar a tu destino.' },
        { title: 'Mejora la primera decisión', description: 'Ve cuál es la mejor base cercana con menos fricción.' },
        { title: 'Guarda tus trayectos repetidos', description: 'Repite consultas clave con favoritos, alertas y widgets.' },
      ],
      faqTitle: 'FAQ Valencia',
      faq: [
        {
          question: '¿BiciRadar sirve para Valenbisi?',
          answer: 'Sí. Está preparada para páginas y campañas locales específicas de Valencia.',
        },
        {
          question: '¿También habrá acceso beta para Android?',
          answer: 'Sí. Puedes apuntarte desde esta página y te contactaremos por email.',
        },
      ],
      mockup: { stationLabel: 'Colón', availability: '10 bicis · 2 huecos' },
    },
    zaragoza: {
      seo: {
        title: 'Bizi en tiempo real con BiciRadar | Beta Zaragoza',
        description:
          'Consulta Bizi en Zaragoza, abre favoritas y solicita acceso a la beta de BiciRadar con contenido local.',
      },
      badge: 'Zaragoza · Bizi',
      title: 'Consulta Bizi en tiempo real antes de pedalear',
      description:
        'Accede a la disponibilidad de Bizi en Zaragoza, guarda estaciones habituales y reduce el tiempo de consulta diaria.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Consulta rápida de Bizi', description: 'Abre el estado de estaciones frecuentes en segundos.' },
        { title: 'Contenido local preparado para SEO', description: 'Cada ciudad tiene copy, FAQ y metadata propia.' },
        { title: 'Beta escalable por ciudad', description: 'Recogemos demanda de Zaragoza sin duplicar la arquitectura.' },
      ],
      faqTitle: 'FAQ Zaragoza',
      faq: [
        {
          question: '¿Está Zaragoza incluida en la primera fase?',
          answer: 'Sí. Ya queda preparada como página local inicial con CTA a beta.',
        },
        {
          question: '¿Puedo compartir esta página con otras personas de Zaragoza?',
          answer: 'Sí. Te animamos a compartirla para medir mejor la demanda local.',
        },
      ],
      mockup: { stationLabel: 'Plaza España', availability: '7 bicis · 6 huecos' },
    },
  },
} satisfies LocaleContent;

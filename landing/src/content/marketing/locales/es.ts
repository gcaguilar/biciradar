import type { LocaleContent } from '../types';

const operatingSystems = [
  { value: 'ios', label: 'iPhone / iOS' },
  { value: 'android', label: 'Android' },
  { value: 'both', label: 'Uso ambos' },
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
      'Comprueba disponibilidad en sistemas de bici compartida, guarda favoritas y descarga BiciRadar.',
  },
  common: {
    skipToContent: 'Saltar al contenido',
    appStoreLabel: 'Descargar en App Store',
    androidBetaLabel: 'Descargar en Google Play',
    openMenu: 'Abrir navegación',
    closeMenu: 'Cerrar navegación',
    backToHome: 'Volver a la home',
    viewCities: 'Ver ciudades',
    cityPageCta: 'Ver página local',
    heroCta: 'Descargar ahora',
    heroSecondaryCta: 'Ver cómo funciona',
    cityHeroMessage:
      'Abres la app, miras tu estación y en un momento sabes si te compensa salir o cambiar de plan.',
    citySeoTitleSuffix: 'BiciRadar',
    citySeoDescriptionSuffix:
      'Consulta tu estación antes de salir desde el móvil o el reloj.',
    finalCtaTitle: 'Descárgala y pruébala',
    finalCtaDescription:
      'Si usas bici pública a menudo, en cuanto guardes tus estaciones de siempre le vas a sacar partido.',
    cityRevisitLabel: 'Volver a ver ciudades',
    shareDescription: 'Comparte BiciRadar con otra persona de tu ciudad.',
    betaInviteLabel: 'Descarga directa',
    faqLabel: 'Preguntas frecuentes',
    thankYouShareFallback: 'Enlace copiado. Ya puedes compartirlo.',
    githubAriaLabel: 'Código fuente en GitHub',
  },
  home: {
    seo: {
      title: 'BiciRadar | Consulta bicis y huecos en tiempo real antes de llegar',
      description:
        'BiciRadar te ayuda a ver disponibilidad en sistemas como BiciMAD, Bicing, Sevici, Valenbisi y Bizi. Guarda favoritos, recibe alertas y accede rápido desde widgets.',
    },
    header: {
      sections: [
        { id: 'problema', label: 'Problema' },
        { id: 'solucion', label: 'Solución' },
        { id: 'como-funciona', label: 'Cómo funciona' },
        { id: 'ciudades', label: 'Ciudades' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Descarga la app',
      languageLabel: 'Idioma',
    },
    hero: {
      title: 'Mira si hay bicis antes de salir',
      description:
        'Abres tu estación de siempre y ves enseguida si merece la pena bajar, esperar un poco o ir a otra. Así te ahorras vueltas justo cuando vas con prisa.',
      primaryCta: 'Descargar en App Store',
      secondaryCta: 'Descargar en Google Play',
      microcopy: '',
      primaryBadge: 'iPhone · Disponible ahora',
      secondaryBadge: 'Android · Disponible ahora',
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
      title: 'Lo malo no es coger la bici. Es enterarte tarde.',
      intro:
        'Sales con el tiempo justo, llegas a la estación y justo ahí descubres que no hay bicis o no hay hueco. Ese rato perdido es lo que BiciRadar intenta ahorrarte.',
      cards: [
        {
          title: 'Llegas y no hay bici',
          description: 'Te plantas en la estación y toca improvisar cuando ya ibas con la hora pegada.',
        },
        {
          title: 'O no hay dónde dejarla',
          description: 'La vuelta también se complica cuando descubres tarde que no queda ni un hueco libre.',
        },
        {
          title: 'Y para comprobarlo das demasiadas vueltas',
          description: 'Para una consulta tan simple, acabar navegando por varias pantallas da bastante rabia.',
        },
      ],
    },
    solution: {
      title: 'Lo que hace BiciRadar',
      cards: [
        {
          title: 'Miras tu estación en segundos',
          description: 'Abres la app y enseguida ves si te compensa ir o si prefieres otra opción.',
        },
        {
          title: 'Tus favoritas están a mano',
          description: 'Guardas las estaciones que más usas y no tienes que buscarlas cada vez.',
        },
        {
          title: 'Te avisa cuando merece la pena mirar',
          description: 'Las alertas ayudan a no estar refrescando todo el rato para ver si algo ha cambiado.',
        },
        {
          title: 'También lo llevas en el reloj',
          description: 'Si te viene mejor, consultas desde el widget o el reloj y listo.',
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
        'Empezamos por varias ciudades donde la bici pública se usa a diario e iremos ampliando.',
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
      title: 'Si te cuadra, la tienes aquí',
      description:
        'Está en iPhone y Android. Elige tu tienda y pruébala cuando quieras.',
      primaryCta: {
        label: 'Descargar en App Store',
        note: 'Disponible ahora en iPhone',
      },
      secondaryCta: {
        label: 'Descargar en Google Play',
        note: 'Disponible ahora en Android',
      },
    },
    betaForm: {
      title: 'Elige tu plataforma y descarga',
      description: 'Instala BiciRadar directamente desde App Store o Google Play.',
      honeypotLabel: 'Empresa',
      consentLabel:
        'Acepto que BiciRadar use estos datos para enviarme el enlace de descarga y contactar conmigo sobre esta solicitud.',
      consentHint: 'Solo usamos la información para enviarte el enlace y ayudarte con esta solicitud.',
      submitLabel: 'Enviar enlace',
      loadingLabel: 'Enviando enlace...',
      successTitle: 'Enlace enviado',
      successMessage: 'Puedes descargar BiciRadar ahora mismo desde tu tienda.',
      errorMessage: 'No hemos podido enviar el enlace. Inténtalo de nuevo en unos segundos.',
      options: {
        operatingSystems,
      },
      fields: {
        email: { label: 'Email', placeholder: 'tu@email.com' },
        operatingSystem: {
          label: 'Sistema operativo',
          placeholder: 'Selecciona tu sistema operativo',
        },
      },
      validation: {
        required: 'Completa este campo.',
        email: 'Introduce un email válido.',
        consent: 'Necesitamos tu consentimiento para enviarte el enlace.',
        turnstile: 'Completa la verificación de seguridad e inténtalo de nuevo.',
        server: 'Ha habido un problema al enviar el enlace.',
      },
    },
    faq: {
      title: 'FAQ',
      items: [
        {
          question: '¿Cómo puedo saber si habrá bicis en mi estación antes de salir de casa?',
          answer:
            'Abres la estación que usas siempre y ves al momento si hay bicis o huecos. Con eso ya sabes si salir, esperar un poco o ir a otra.',
        },
        {
          question: '¿Qué cambia frente a mirar la app oficial cuando ya voy con prisa?',
          answer:
            'La app oficial también sirve, pero BiciRadar va más al grano: favoritas, alertas, widget y reloj para hacer la misma consulta en menos pasos.',
        },
        {
          question: '¿Puedo ver mi disponibilidad de transporte en el reloj o en un widget?',
          answer:
            'Sí. Puedes llevar tus estaciones a mano en el móvil, en widgets y también en el reloj.',
        },
        {
          question: '¿Necesito crear cuenta para empezar?',
          answer:
            'No. La descargas y puedes empezar a mirar estaciones sin registros raros ni pasos de más.',
        },
        {
          question: '¿Qué ciudades y datos cubre ahora mismo?',
          answer:
            'Ahora mismo destacamos Madrid, Barcelona, Sevilla, Valencia y Zaragoza, además de otras ciudades que ya verás enlazadas en la landing. Usamos datos oficiales y la app los ordena para que la consulta sea rápida.',
        },
      ],
    },
    footer: {
      tagline: '',
      links: [
        { label: 'Privacidad', href: 'https://gcaguilar.github.io/biciradar-privacy-policy/' },
        { label: 'Contacto', href: 'mailto:hola@biciradar.es?subject=BiciRadar%20app' },
        { label: 'Soporte', href: 'mailto:soporte@biciradar.es?subject=Soporte%20BiciRadar' },
      ],
      primaryCta: 'Descargar ahora',
      note: 'BiciRadar · iPhone y Android · Datos basados en fuentes oficiales',
      githubLine: 'Código abierto en GitHub',
    },
  },
  thankYou: {
    seo: {
      title: 'Gracias por solicitar el enlace de BiciRadar',
      description:
        'Tu solicitud se ha enviado correctamente. Explora la app, las ciudades disponibles o compártela.',
    },
    badge: 'Solicitud enviada',
    title: 'Gracias por visitar BiciRadar',
    description:
      'Puedes instalar BiciRadar ahora mismo o explorar las páginas locales de cada ciudad.',
    installStepByOs: {
      ios: 'En iPhone: también puedes descargar BiciRadar ya desde el App Store.',
      android: 'En Android: puedes descargar BiciRadar ahora mismo desde Google Play.',
      both: 'Según tu móvil: usa App Store en iPhone o Google Play en Android.',
    },
    steps: [
      'Explora las páginas locales para ver disponibilidad en tiempo real.',
      'Para dudas, escribe a hola@biciradar.es.',
    ],
    cityLinksTitle: 'Explora ciudades disponibles mientras tanto',
    cityCardCtaPrefix: 'Ver',
    shareLabel: 'Compartir BiciRadar',
    primaryCta: 'Explora ciudades disponibles mientras tanto',
    appStoreCta: 'Descargar en App Store',
    playStoreCta: 'Descargar en Google Play',
    footnote: '¿Preguntas? hola@biciradar.es',
  },
  cityPages: {
    madrid: {
      seo: {
        title: 'BiciMAD en tiempo real con BiciRadar | Madrid',
        description:
          'Consulta BiciMAD antes de salir, evita estaciones vacías y descarga BiciRadar para Madrid.',
      },
      badge: 'Madrid · BiciMAD',
      title: 'Consulta BiciMAD en tiempo real antes de salir',
      description:
        'Comprueba bicis y huecos en estaciones clave de Madrid y evita viajes innecesarios hacia bases vacías o llenas.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de bajar a la calle', description: 'Abre BiciMAD y confirma si tu base habitual te compensa antes de salir.' },
        { title: 'Evita el cambio de estación a última hora', description: 'Si esa base no encaja, eliges otra antes de perder tiempo ya en marcha.' },
        { title: 'Resuelve la duda en un vistazo', description: 'Favoritos, alertas y accesos rápidos te dan la respuesta sin fricción.' },
      ],
      faqTitle: 'FAQ Madrid',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar con BiciMAD antes de salir?',
          answer: 'Te ayuda a saber si tu base habitual tiene bicis o huecos y si te conviene bajar ya, esperar o cambiar de estación.',
        },
        {
          question: '¿Puedo ver BiciMAD rápido desde el móvil o el reloj?',
          answer: 'Sí. Favoritos, widgets y accesos rápidos te dejan resolver la disponibilidad en segundos, sin abrir toda la app.',
        },
      ],
      mockup: { stationLabel: 'Estación Sol', availability: '12 bicis · 5 huecos' },
    },
    barcelona: {
      seo: {
        title: 'Bicing en tiempo real con BiciRadar | Barcelona',
        description:
          'Consulta Bicing antes de moverte, guarda favoritas y descarga BiciRadar para Barcelona.',
      },
      badge: 'Barcelona · Bicing',
      title: 'Consulta Bicing antes de llegar a la estación',
      description:
        'Ve bicis y anclajes libres en Barcelona para decidir si te conviene mantener ruta, cambiar base o esperar.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide al salir del metro', description: 'Comprueba Bicing antes de subir a la calle o salir de la oficina.' },
        { title: 'Evita vueltas en hora punta', description: 'Si esa base no te conviene, cambias de estación antes de hacer el desvío.' },
        { title: 'Mantén tu rutina a un toque', description: 'Tus estaciones clave quedan listas para resolver la duda en segundos.' },
      ],
      faqTitle: 'FAQ Barcelona',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar con Bicing antes de llegar?',
          answer: 'Te ayuda a saber si esa estación te compensa o si es mejor cambiar de base antes de hacer el desvío.',
        },
        {
          question: '¿Qué me aporta si ya miro Bicing en la app oficial?',
          answer: 'BiciRadar reduce pasos justo cuando vas con prisa: abres favoritos, ves disponibilidad y tomas la decisión antes.',
        },
      ],
      mockup: { stationLabel: 'Pg. de Gràcia', availability: '8 bicis · 4 huecos' },
    },
    sevilla: {
      seo: {
        title: 'Sevici en tiempo real con BiciRadar | Sevilla',
        description:
          'Anticipa disponibilidad de Sevici, guarda estaciones favoritas y descarga BiciRadar en Sevilla.',
      },
      badge: 'Sevilla · Sevici',
      title: 'Consulta Sevici en tiempo real antes de moverte',
      description:
        'Comprueba disponibilidad en Sevilla y evita llegar sin bicis o sin huecos cuando necesitas una decisión rápida.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Sal con menos dudas', description: 'Consulta Sevici justo antes de arrancar y sabrás mejor qué hacer.' },
        { title: 'Ten tu base clave a mano', description: 'Tus estaciones habituales quedan listas para resolver la duda en segundos.' },
        { title: 'Devuelve mejor sin improvisar', description: 'Recibe avisos cuando vuelven a aparecer huecos donde los necesitas.' },
      ],
      faqTitle: 'FAQ Sevilla',
      faq: [
        {
          question: '¿Qué decisión me ayuda a tomar con Sevici antes de moverme?',
          answer: 'Si te conviene ir a tu estación habitual, esperar unos minutos o cambiar de base antes de perder tiempo en marcha.',
        },
        {
          question: '¿Puedo tener Sevici a mano sin entrar en toda la app?',
          answer: 'Sí. Favoritos, widgets y accesos rápidos te dejan ver la disponibilidad y actuar en segundos.',
        },
      ],
      mockup: { stationLabel: 'Puerta Jerez', availability: '6 bicis · 7 huecos' },
    },
    valencia: {
      seo: {
        title: 'Valenbisi en tiempo real con BiciRadar | Valencia',
        description:
          'Consulta Valenbisi, evita estaciones completas y descarga BiciRadar para Valencia.',
      },
      badge: 'Valencia · Valenbisi',
      title: 'Consulta Valenbisi antes de acercarte a la estación',
      description:
        'Ten visibilidad de bicis y huecos en Valencia para no improvisar la devolución o la recogida sobre la marcha.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Evita llegar a una base llena', description: 'Comprueba huecos antes de acercarte a tu destino y decide mejor.' },
        { title: 'Elige mejor la primera estación', description: 'Ves qué base te conviene para coger o devolver bici sin improvisar.' },
        { title: 'Resuelve la rutina sin empezar de cero', description: 'Favoritos, alertas y accesos rápidos te ahorran pasos en cada trayecto.' },
      ],
      faqTitle: 'FAQ Valencia',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar con Valenbisi antes de acercarme a la estación?',
          answer: 'Te ayuda a decidir si esa base te conviene para coger o devolver bici antes de improvisar sobre la marcha.',
        },
        {
          question: '¿Puedo verlo rápido en iPhone o Android?',
          answer: 'Sí. La disponibilidad queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Colón', availability: '10 bicis · 2 huecos' },
    },
    zaragoza: {
      seo: {
        title: 'Bizi en tiempo real con BiciRadar | Zaragoza',
        description:
          'Consulta Bizi en Zaragoza, abre favoritas y descarga BiciRadar con contenido local.',
      },
      badge: 'Zaragoza · Bizi',
      title: 'Consulta Bizi en tiempo real antes de pedalear',
      description:
        'Accede a la disponibilidad de Bizi en Zaragoza, guarda estaciones habituales y reduce el tiempo de consulta diaria.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de pedalear', description: 'Comprueba si tu estación habitual tiene disponibilidad antes de salir.' },
        { title: 'Evita rodeos entre bases', description: 'Si esa estación no te compensa, cambias de plan antes de perder tiempo.' },
        { title: 'Consulta Bizi en segundos', description: 'Favoritos y accesos rápidos te dejan resolver la duda casi al instante.' },
      ],
      faqTitle: 'FAQ Zaragoza',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar con Bizi antes de pedalear?',
          answer: 'Te ayuda a saber si tu estación habitual tiene bicis o huecos y si te compensa salir ya o cambiar de base.',
        },
        {
          question: '¿Puedo compartirlo con alguien de Zaragoza?',
          answer: 'Sí. Si otra persona tiene la misma duda diaria, esta página y la app le ayudan a decidir antes de moverse.',
        },
      ],
      mockup: { stationLabel: 'Plaza España', availability: '7 bicis · 6 huecos' },
    },
    bilbao: {
      seo: {
        title: 'Bilbao en tiempo real con BiciRadar | Bilbao',
        description: 'Consulta bicis y huecos de Bilbao antes de salir y descarga BiciRadar.',
      },
      badge: 'Bilbao · Bilbao',
      title: 'Consulta Bilbao en tiempo real antes de salir',
      description: 'Descubre bicis y huecos disponibles en Bilbao para moverte más rápido.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Bilbao',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Bilbao antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Bilbao', availability: '5 bicis · 3 huecos' },
    },
    murcia: {
      seo: {
        title: 'Murcia en tiempo real con BiciRadar | Murcia',
        description: 'Consulta bicis y huecos de Murcia antes de salir y descarga BiciRadar.',
      },
      badge: 'Murcia · Murcia',
      title: 'Consulta Murcia en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en Murcia antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Murcia',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Murcia antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Murcia', availability: '4 bicis · 2 huecos' },
    },
    leon: {
      seo: {
        title: 'León en tiempo real con BiciRadar | León',
        description: 'Consulta bicis y huecos de León antes de salir y descarga BiciRadar.',
      },
      badge: 'León · León',
      title: 'Consulta León en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en León antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ León',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en León antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'León', availability: '3 bicis · 2 huecos' },
    },
    valladolid: {
      seo: {
        title: 'Valladolid en tiempo real con BiciRadar | Valladolid',
        description: 'Consulta bicis y huecos de Valladolid antes de salir y descarga BiciRadar.',
      },
      badge: 'Valladolid · Valladolid',
      title: 'Consulta Valladolid en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en Valladolid antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Valladolid',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Valladolid antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Valladolid', availability: '4 bicis · 3 huecos' },
    },
    palma: {
      seo: {
        title: 'Palma en tiempo real con BiciRadar | Palma',
        description: 'Consulta bicis y huecos de Palma antes de salir y descarga BiciRadar.',
      },
      badge: 'Palma · Palma',
      title: 'Consulta Palma en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en Palma antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Palma',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Palma antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Palma', availability: '5 bicis · 4 huecos' },
    },
    las_palmas: {
      seo: {
        title: 'Las Palmas en tiempo real con BiciRadar | Las Palmas',
        description: 'Consulta bicis y huecos de Las Palmas antes de salir y descarga BiciRadar.',
      },
      badge: 'Las Palmas · Las Palmas',
      title: 'Consulta Las Palmas en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en Las Palmas antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Las Palmas',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Las Palmas antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Las Palmas', availability: '4 bicis · 3 huecos' },
    },
    a_coruna: {
      seo: {
        title: 'A Coruña en tiempo real con BiciRadar | A Coruña',
        description: 'Consulta bicis y huecos de A Coruña antes de salir y descarga BiciRadar.',
      },
      badge: 'A Coruña · A Coruña',
      title: 'Consulta A Coruña en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en A Coruña antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ A Coruña',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en A Coruña antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'A Coruña', availability: '3 bicis · 2 huecos' },
    },
    gijon: {
      seo: {
        title: 'Gijón en tiempo real con BiciRadar | Gijón',
        description: 'Consulta bicis y huecos de Gijón antes de salir y descarga BiciRadar.',
      },
      badge: 'Gijón · Gijón',
      title: 'Consulta Gijón en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en Gijón antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Gijón',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Gijón antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Gijón', availability: '4 bicis · 3 huecos' },
    },
    vitoria_gasteiz: {
      seo: {
        title: 'Vitoria-Gasteiz en tiempo real con BiciRadar | Vitoria',
        description: 'Consulta bicis y huecos de Vitoria antes de salir y descarga BiciRadar.',
      },
      badge: 'Vitoria-Gasteiz · Vitoria',
      title: 'Consulta Vitoria-Gasteiz en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en Vitoria antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Vitoria',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Vitoria-Gasteiz antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Vitoria', availability: '3 bicis · 2 huecos' },
    },
    pamplona: {
      seo: {
        title: 'Pamplona en tiempo real con BiciRadar | Pamplona',
        description: 'Consulta bicis y huecos de Pamplona antes de salir y descarga BiciRadar.',
      },
      badge: 'Pamplona · Pamplona',
      title: 'Consulta Pamplona en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en Pamplona antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Pamplona',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Pamplona antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Pamplona', availability: '5 bicis · 4 huecos' },
    },
    castellon: {
      seo: {
        title: 'Castellón en tiempo real con BiciRadar | Castellón',
        description: 'Consulta bicis y huecos de Castellón antes de salir y descarga BiciRadar.',
      },
      badge: 'Castellón · Castellón',
      title: 'Consulta Castellón en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en Castellón antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Castellón',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Castellón antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Castellón', availability: '3 bicis · 2 huecos' },
    },
    santander: {
      seo: {
        title: 'Santander en tiempo real con BiciRadar | Santander',
        description: 'Consulta bicis y huecos de Santander antes de salir y descarga BiciRadar.',
      },
      badge: 'Santander · Santander',
      title: 'Consulta Santander en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en Santander antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Santander',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Santander antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Santander', availability: '4 bicis · 3 huecos' },
    },
    girona: {
      seo: {
        title: 'Girona en tiempo real con BiciRadar | Girona',
        description: 'Consulta bicis y huecos de Girona antes de salir y descarga BiciRadar.',
      },
      badge: 'Girona · Girona',
      title: 'Consulta Girona en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en Girona antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Girona',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Girona antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Girona', availability: '3 bicis · 2 huecos' },
    },
    gran_canaria: {
      seo: {
        title: 'Gran Canaria en tiempo real con BiciRadar | Gran Canaria',
        description: 'Consulta bicis y huecos de Gran Canaria antes de salir y descarga BiciRadar.',
      },
      badge: 'Gran Canaria · Gran Canaria',
      title: 'Consulta Gran Canaria en tiempo real antes de salir',
      description: 'Mira la disponibilidad de bicis y huecos en Gran Canaria antes de moverte.',
      benefitsTitle: 'Beneficios locales',
      benefits: [
        { title: 'Decide antes de salir', description: 'Comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.' },
        { title: 'Evita desvíos innecesarios', description: 'Si esa base no te compensa, cambias de plan antes de perder tiempo en la calle.' },
        { title: 'Consulta en un vistazo', description: 'Abre favoritos, widget o móvil y resuelve la duda en segundos.' },
      ],
      faqTitle: 'FAQ Gran Canaria',
      faq: [
        {
          question: '¿Qué me resuelve BiciRadar en Gran Canaria antes de moverme?',
          answer: 'Te ayuda a saber si tu estación habitual tiene disponibilidad y si te conviene salir ya, esperar o cambiar de base.',
        },
        {
          question: '¿Puedo verlo rápido desde el móvil o el reloj?',
          answer: 'Sí. La disponibilidad se queda a mano en favoritos, widgets y accesos rápidos para decidir sin abrir todo el flujo.',
        },
      ],
      mockup: { stationLabel: 'Gran Canaria', availability: '4 bicis · 3 huecos' },
    },
  },
} satisfies LocaleContent;

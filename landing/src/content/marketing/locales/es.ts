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
    siteTagline: 'Tu estación de bici, en un vistazo',
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
    finalCtaTitle: 'Descárgala gratis y pruébala',
    finalCtaDescription:
      'Guarda tus estaciones de siempre y desde el primer día decides antes de salir. Sin cuenta para empezar y con datos abiertos de cada red.',
    cityRevisitLabel: 'Volver a ver ciudades',
    shareDescription: 'Comparte BiciRadar con otra persona de tu ciudad.',
    betaInviteLabel: 'Descarga directa',
    faqLabel: 'Preguntas frecuentes',
    thankYouShareFallback: 'Enlace copiado. Ya puedes compartirlo.',
    githubAriaLabel: 'Código fuente en GitHub',
  },
  home: {
    seo: {
      title: 'BiciRadar | Bicis y huecos de tu estación antes de salir',
      description:
        'Mira si hay bicis o huecos libres en tu estación de bici pública antes de salir. Favoritas, widgets, reloj y avisos. Gratis en iPhone y Android.',
    },
    header: {
      sections: [
        { id: 'ventajas', label: 'Ventajas' },
        { id: 'capturas', label: 'La app' },
        { id: 'como-funciona', label: 'Cómo funciona' },
        { id: 'ciudades', label: 'Ciudades' },
        { id: 'faq', label: 'FAQ' },
      ],
      primaryCta: 'Descargar',
      languageLabel: 'Idioma',
    },
    hero: {
      title: '¿Hay bici en tu estación? Míralo antes de salir.',
      description:
        'BiciRadar te enseña las bicis y los huecos libres de tu bici pública. Tus estaciones de siempre, a un toque desde el móvil, un widget o el reloj.',
      primaryCta: 'Descargar en App Store',
      secondaryCta: 'Descargar en Google Play',
      microcopy: 'Gratis · Sin cuenta para empezar · iPhone y Android',
      trust: ['Más de 40 ciudades', 'Apple Watch y Wear OS', 'Widgets y voz'],
      screenshotAlt: 'Mapa de BiciRadar con la estación más cercana, sus bicis y huecos libres',
      mockup: {
        stationLabel: 'Estación Sol',
        availability: '12 bicis · 5 huecos',
        bikesLabel: 'Bicis listas',
        docksLabel: 'Huecos libres',
        chips: ['Favoritas', 'Alertas', 'Widgets'],
        shortcutLabel: 'Abrir trayecto de mañana',
      },
    },
    solution: {
      title: 'Hecha para saber qué hay antes de bajar a la calle',
      intro:
        'Llegar a una estación vacía, o sin un hueco libre, es una pérdida de tiempo evitable. Esto es lo que BiciRadar te pone fácil.',
      cards: [
        {
          icon: 'pin',
          title: 'La estación útil, nada más abrirla',
          description:
            'Ves la más cercana con bicis y la más cercana con huecos, con distancia y ruta a un toque. Si vas a pie, te enseña las que tienen bicis; si vas en bici, las que tienen huecos.',
        },
        {
          icon: 'sparkles',
          title: 'Filtra y ordena como te convenga',
          description:
            'Por distancia, por bicis, por huecos o por la disponibilidad que suele haber a esa hora. Donde la red lo publica, también eléctricas o mecánicas.',
        },
        {
          icon: 'star',
          title: 'Casa, Trabajo y favoritas',
          description:
            'Fija tus estaciones de siempre y ábrelas con un toque, sin buscarlas cada vez.',
        },
        {
          icon: 'bell',
          title: 'Avisos de tus estaciones',
          description:
            'Alertas para Casa, Trabajo y favoritas. Con una cuenta (opcional), las rutinas vigilan tu estación antes de salir y te proponen una alternativa cercana.',
        },
        {
          icon: 'phone',
          title: 'En widgets y en el reloj',
          description:
            'Widgets en el móvil y app para Apple Watch y Wear OS, para mirar tu estación sin sacar el teléfono.',
        },
        {
          icon: 'globe',
          title: 'Pregúntale a tu asistente',
          description:
            'Con Siri, Atajos o Google Assistant puedes preguntar si hay bicis o huecos en tu estación.',
        },
      ],
      note: 'BiciRadar muestra disponibilidad para decidir mejor; no gestiona alquileres ni pagos.',
    },
    showcase: {
      title: 'Así se ve por dentro',
      intro: 'Capturas reales de la app, sin florituras.',
      items: [
        {
          key: 'map',
          title: 'Mapa en vivo',
          description: 'Bicis y huecos de un vistazo, con la hora de la última actualización. Filtra por solo bicis o solo huecos.',
          alt: 'Mapa con estaciones y filtros Bicis + Huecos, Solo bicis y Solo huecos',
        },
        {
          key: 'nearby',
          title: 'Cerca de ti',
          description: 'La estación más cercana con bicis y la más cercana con huecos, y ruta con un toque.',
          alt: 'Pestaña Cerca con la estación más cercana con bicis y con huecos',
        },
        {
          key: 'favorites',
          title: 'Favoritas, Casa y Trabajo',
          description: 'Tus estaciones guardadas y la gestión de alertas, en un único sitio.',
          alt: 'Pestaña Favoritas con alertas de estaciones y los accesos Casa y Trabajo',
        },
        {
          key: 'detail',
          title: 'Detalle de estación',
          description: 'Distancia, mapa y guardado como favorita, Casa o Trabajo con un toque.',
          alt: 'Detalle de una estación con distancia, mapa y botones para guardarla',
        },
      ],
    },
    howItWorks: {
      title: 'Empezar lleva un minuto',
      steps: [
        {
          number: '1',
          title: 'Descarga y elige tu ciudad',
          description: 'Selecciona tu red de bici pública. No hace falta crear una cuenta.',
        },
        {
          number: '2',
          title: 'Mira lo que tienes cerca',
          description: 'Con tu ubicación, la app te enseña qué estación te conviene ahora mismo.',
        },
        {
          number: '3',
          title: 'Guarda tus estaciones',
          description: 'Fija Casa, Trabajo y favoritas y decide en segundos, desde el móvil o el reloj.',
        },
      ],
    },
    cities: {
      title: 'Ciudades y sistemas',
      description:
        'Empieza por una de estas ciudades; dentro de la app encontrarás más redes de bici pública.',
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
      moreLabel: 'Y más ciudades dentro de la app: elige la tuya al abrirla.',
      otherCitiesLabel: 'Páginas de otras ciudades',
    },
    midCta: {
      title: 'Antes de salir, mira tu estación.',
      description:
        'Descarga BiciRadar gratis, elige tu ciudad y ten tus estaciones a un toque.',
      reassurance: 'Gratis · Sin cuenta para empezar',
      primaryCta: {
        label: 'Descargar en App Store',
        note: 'Para iPhone',
      },
      secondaryCta: {
        label: 'Descargar en Google Play',
        note: 'Para Android',
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
      title: 'Preguntas frecuentes',
      items: [
        {
          question: '¿Es gratis? ¿Necesito crear una cuenta?',
          answer:
            'Se descarga y se usa gratis. Puedes consultar estaciones, guardar favoritas y usar widgets sin cuenta. La cuenta es opcional y solo hace falta para las rutinas y el seguimiento de trayectos, que se vigilan desde nuestro servidor.',
        },
        {
          question: '¿Sustituye a la app oficial de mi ciudad?',
          answer:
            'No. BiciRadar sirve para consultar disponibilidad y decidir antes de salir; no gestiona alquileres ni pagos. Puedes usarla junto a la app de tu sistema.',
        },
        {
          question: '¿Los datos son en tiempo real?',
          answer:
            'Usamos datos abiertos que publica cada sistema y se actualizan con frecuencia, pero el ritmo depende de cada proveedor. En cada estación ves hace cuánto se actualizó.',
        },
        {
          question: '¿Necesita mi ubicación?',
          answer:
            'La usa para decirte cuál es tu estación más cercana. Si no das permiso, puedes seguir usando la app, pero algunos resultados serán menos precisos.',
        },
        {
          question: '¿Funciona en el reloj y con la voz?',
          answer:
            'Sí. Hay app para Apple Watch y Wear OS, widgets en iPhone y Android, y puedes preguntar con Siri, Atajos o Google Assistant.',
        },
        {
          question: '¿Qué ciudades cubre?',
          answer:
            'Madrid, Barcelona, Sevilla, Valencia, Zaragoza, Bilbao y muchas más. Al abrir la app eliges tu ciudad; si la tuya tiene página propia, la encontrarás en la sección de ciudades.',
        },
        {
          question: '¿Qué pasa con mis favoritas y mis datos?',
          answer:
            'Tus favoritas y lugares guardados se quedan en tu dispositivo. Tienes más detalle en la política de privacidad, enlazada al final de la página.',
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
      note: 'BiciRadar · iPhone y Android · Datos abiertos de cada sistema',
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

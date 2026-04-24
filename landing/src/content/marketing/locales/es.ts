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
    heroCta: 'Te lo enviamos por correo',
    heroSecondaryCta: 'Ver cómo funciona',
    finalCtaTitle: 'Descárgala ahora o recíbela por correo',
    finalCtaDescription:
      'BiciRadar ya está disponible en App Store y Google Play. Si lo prefieres, déjanos tu email y te enviamos el enlace.',
    cityRevisitLabel: 'Volver a ver ciudades',
    shareDescription: 'Comparte BiciRadar con otra persona de tu ciudad.',
    betaInviteLabel: 'También por correo',
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
      title: 'Consulta bicis y huecos en tiempo real antes de llegar',
      description:
        'BiciRadar te ayuda a ver disponibilidad en sistemas de bici compartida como BiciMAD, Bicing, Sevici y más. Guarda favoritos, recibe alertas y accede rápido desde widgets.',
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
        'Descarga BiciRadar ya en iPhone o Android. Si lo prefieres, más abajo te enviamos el enlace por correo.',
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
      title: 'Te lo enviamos por correo',
      description: 'Déjanos tu email y te mandamos el enlace de descarga para tu plataforma.',
      honeypotLabel: 'Empresa',
      consentLabel:
        'Acepto que BiciRadar use estos datos para enviarme el enlace de descarga y contactar conmigo sobre esta solicitud.',
      consentHint: 'Solo usamos la información para enviarte el enlace y ayudarte con esta solicitud.',
      submitLabel: 'Enviar enlace',
      loadingLabel: 'Enviando enlace...',
      successTitle: 'Enlace enviado',
      successMessage: 'Te enviaremos el enlace por correo en unos minutos.',
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
          question: '¿Si ya tengo la app oficial, por qué descargar BiciRadar?',
          answer:
            'Porque está pensada para la consulta rápida del día a día: ves bicis y huecos antes de salir, guardas favoritas, recibes alertas y entras desde widgets o reloj en menos pasos.',
        },
        {
          question: '¿De verdad me ahorra tiempo en el día a día?',
          answer:
            'Sí. Reduce la fricción justo cuando vas con prisa: abres tus estaciones habituales al momento y, en muchos casos, consultas disponibilidad sin tener que navegar por toda la app oficial.',
        },
        {
          question: '¿Necesito crear cuenta para descargarla?',
          answer:
            'No. Puedes ir directamente a App Store o Google Play para descargarla. Si prefieres seguir luego, también puedes dejarnos tu email y te enviamos el enlace.',
        },
        {
          question: '¿Qué ciudades puedo consultar ahora mismo?',
          answer:
            'Ahora mismo destacamos Madrid, Barcelona, Sevilla, Valencia y Zaragoza, y la landing ya muestra más ciudades compatibles para que compruebes si la tuya está incluida.',
        },
        {
          question: '¿Los datos son fiables?',
          answer:
            'Usa fuentes oficiales de open data de cada ciudad. Como en cualquier sistema en tiempo real puede haber pequeños desfases, pero la referencia de datos es la oficial.',
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
      primaryCta: 'Te lo enviamos por correo',
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
    title: 'Gracias, te enviaremos el enlace',
    description:
      'Te avisaremos por correo con el enlace de descarga. Mientras tanto, puedes instalar BiciRadar ya o explorar las páginas locales de cada ciudad.',
    installStepByOs: {
      ios: 'En iPhone: también puedes descargar BiciRadar ya desde el App Store.',
      android: 'En Android: revisa el email que te enviaremos con el enlace a Google Play.',
      both: 'Según tu móvil: App Store en iPhone o Google Play en Android. También te enviaremos los enlaces por email.',
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
        { title: 'Evita estaciones vacías', description: 'Decide antes de ir a una base de BiciMAD.' },
        { title: 'Planifica trayectos urbanos', description: 'Combina favoritas y alertas para tus recorridos diarios.' },
        { title: 'Recibe alertas útiles', description: 'Vigila zonas clave cuando necesitas devolver o coger bici rápido.' },
      ],
      faqTitle: 'FAQ Madrid',
      faq: [
        {
          question: '¿BiciRadar funciona con BiciMAD en Madrid?',
          answer: 'Sí. Puedes consultar bicis y huecos de BiciMAD en tiempo real y guardar tus estaciones habituales para abrirlas más rápido.',
        },
        {
          question: '¿Merece la pena si ya uso la app oficial de BiciMAD?',
          answer: 'Sí. BiciRadar te ayuda a resolver la consulta diaria con menos fricción: favoritas, alertas y acceso rápido antes de salir o al llegar a una estación.',
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
        { title: 'Menos vueltas en horas punta', description: 'Consulta Bicing antes de salir del metro o la oficina.' },
        { title: 'Favoritas para tu rutina', description: 'Accede primero a estaciones cerca de casa, trabajo o universidad.' },
        { title: 'Widgets para consultas rápidas', description: 'Abre el estado de Bicing sin entrar a toda la app.' },
      ],
      faqTitle: 'FAQ Barcelona',
      faq: [
        {
          question: '¿Funciona con Bicing en Barcelona?',
          answer: 'Sí. Puedes ver bicis y anclajes libres de Bicing en tiempo real y dejar a mano las estaciones que más usas.',
        },
        {
          question: '¿Qué me aporta si ya miro Bicing en la app oficial?',
          answer: 'Menos pasos para la misma decisión importante: saber antes de llegar si te conviene ir a esa base, cambiar de estación o esperar.',
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
        { title: 'Menos incertidumbre al salir', description: 'Consulta Sevici justo antes de arrancar tu trayecto.' },
        { title: 'Acceso rápido a favoritas', description: 'Ten a mano tus estaciones clave para casa, trabajo o estudio.' },
        { title: 'Alertas para devolver mejor', description: 'Recibe señales útiles cuando vuelva a haber huecos libres.' },
      ],
      faqTitle: 'FAQ Sevilla',
      faq: [
        {
          question: '¿Puedo consultar Sevici con BiciRadar en Sevilla?',
          answer: 'Sí. BiciRadar te permite comprobar bicis y huecos de Sevici antes de moverte y guardar estaciones clave para tu rutina.',
        },
        {
          question: '¿Y si prefiero recibir el enlace por correo en vez de descargar ahora?',
          answer: 'También puedes. Dejas tu email, eliges plataforma y te enviamos el enlace de descarga para seguir más tarde sin perder la intención.',
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
        { title: 'Evita estaciones completas', description: 'Comprueba huecos antes de llegar a tu destino.' },
        { title: 'Mejora la primera decisión', description: 'Ve cuál es la mejor base cercana con menos fricción.' },
        { title: 'Guarda tus trayectos repetidos', description: 'Repite consultas clave con favoritos, alertas y widgets.' },
      ],
      faqTitle: 'FAQ Valencia',
      faq: [
        {
          question: '¿BiciRadar sirve para Valenbisi en Valencia?',
          answer: 'Sí. Puedes revisar bicis y huecos de Valenbisi antes de acercarte a la estación y decidir mejor tu ruta.',
        },
        {
          question: '¿También está disponible en Android e iPhone?',
          answer: 'Sí. Puedes descargar BiciRadar desde App Store o Google Play, o pedir que te enviemos el enlace por correo.',
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
        { title: 'Consulta rápida de Bizi', description: 'Abre el estado de estaciones frecuentes en segundos.' },
        { title: 'Favoritas para tu rutina', description: 'Guarda estaciones clave de casa, trabajo o estudio para consultarlas primero.' },
        { title: 'Menos vueltas innecesarias', description: 'Decide antes de llegar si te conviene esa estación o prefieres otra cercana.' },
      ],
      faqTitle: 'FAQ Zaragoza',
      faq: [
        {
          question: '¿Puedo consultar Bizi en Zaragoza con BiciRadar?',
          answer: 'Sí. BiciRadar te ayuda a ver bicis y huecos de Bizi en tiempo real y a entrar más rápido a tus estaciones frecuentes.',
        },
        {
          question: '¿Se puede descargar ya o enviarme el enlace para después?',
          answer: 'Ambas opciones. Puedes descargarla ahora o dejarnos tu email para recibir el enlace y retomarlo cuando te venga mejor.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Bilbao',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Bilbao con BiciRadar?',
          answer: 'Sí. Puedes ver disponibilidad en tiempo real y guardar tus estaciones habituales para resolver la consulta mucho más rápido.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. BiciRadar está disponible en Google Play y App Store, y si quieres también te enviamos el enlace por correo.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Murcia',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Murcia con BiciRadar?',
          answer: 'Sí. BiciRadar te permite comprobar disponibilidad en tiempo real y entrar antes a las estaciones que más usas.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. Puedes descargarla en Google Play o App Store, o dejar tu email para recibir el enlace.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ León',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de León con BiciRadar?',
          answer: 'Sí. Puedes revisar disponibilidad en tiempo real y preparar favoritas para no perder tiempo al salir.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. BiciRadar está en Google Play y App Store, y también puedes pedir que te enviemos el enlace por correo.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Valladolid',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Valladolid con BiciRadar?',
          answer: 'Sí. Puedes revisar disponibilidad en tiempo real y dejar guardadas tus estaciones habituales para decidir más rápido.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. BiciRadar está disponible en Google Play y App Store, y si lo prefieres te enviamos el enlace por correo.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Palma',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Palma con BiciRadar?',
          answer: 'Sí. BiciRadar te muestra la disponibilidad en tiempo real para que no pierdas tiempo yendo a una estación sin opciones.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. Puedes descargar BiciRadar en Google Play y App Store o pedir el enlace por correo para instalarla más tarde.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Las Palmas',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Las Palmas con BiciRadar?',
          answer: 'Sí. Puedes ver disponibilidad en tiempo real y abrir tus estaciones frecuentes sin repetir la búsqueda cada vez.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. BiciRadar está disponible en Google Play y App Store, y también puedes recibir el enlace por correo.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ A Coruña',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de A Coruña con BiciRadar?',
          answer: 'Sí. BiciRadar te da acceso rápido a la disponibilidad en tiempo real para que decidas mejor antes de moverte.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. Puedes instalarla desde Google Play o App Store, o dejar tu email para recibir el enlace.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Gijón',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Gijón con BiciRadar?',
          answer: 'Sí. Puedes comprobar disponibilidad en tiempo real y dejar favoritas preparadas para consultar en segundos.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. BiciRadar se puede descargar en Google Play y App Store, o puedes pedir el enlace por correo.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Vitoria',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Vitoria-Gasteiz con BiciRadar?',
          answer: 'Sí. BiciRadar te permite ver disponibilidad en tiempo real y entrar rápido a tus estaciones más frecuentes.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. Puedes descargarla en Google Play o App Store, o recibir el enlace por correo para instalarla luego.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Pamplona',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Pamplona con BiciRadar?',
          answer: 'Sí. Puedes revisar disponibilidad en tiempo real y guardar las estaciones que más usas para resolver la consulta más rápido.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. BiciRadar está en Google Play y App Store, y si te conviene más también te enviamos el enlace por email.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Castellón',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Castellón con BiciRadar?',
          answer: 'Sí. BiciRadar te ayuda a ver disponibilidad en tiempo real y a reducir vueltas innecesarias al buscar estación.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. Puedes instalarla desde Google Play o App Store, o recibir el enlace por correo para hacerlo después.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Santander',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Santander con BiciRadar?',
          answer: 'Sí. Puedes comprobar disponibilidad en tiempo real y dejar a mano tus estaciones más habituales para decidir antes de llegar.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. BiciRadar está disponible en Google Play y App Store, y también puedes pedir el enlace por email.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Girona',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Girona con BiciRadar?',
          answer: 'Sí. BiciRadar te da visibilidad en tiempo real para que elijas mejor estación antes de moverte.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. Puedes descargarla desde Google Play o App Store, o pedir el enlace por correo y retomarlo más tarde.',
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
        { title: 'Consulta en tiempo real', description: 'Verifica bicis y huecos antes de salir.' },
        { title: 'Favoritas', description: 'Guarda tus estaciones clave para consultarlas rápido.' },
        { title: 'Alertas', description: 'Recibe avisos cuando haya bicis o huecos disponibles.' },
      ],
      faqTitle: 'FAQ Gran Canaria',
      faq: [
        {
          question: '¿Puedo consultar bicis y huecos de Gran Canaria con BiciRadar?',
          answer: 'Sí. Puedes ver disponibilidad en tiempo real y preparar favoritas para resolver la consulta diaria mucho más rápido.',
        },
        {
          question: '¿También está disponible en Android y iPhone?',
          answer: 'Sí. BiciRadar se puede descargar en Google Play y App Store, o puedes recibir el enlace por email.',
        },
      ],
      mockup: { stationLabel: 'Gran Canaria', availability: '4 bicis · 3 huecos' },
    },
  },
} satisfies LocaleContent;

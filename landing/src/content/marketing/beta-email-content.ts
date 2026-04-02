import type { Locale } from './types';

export interface BetaUserEmailContent {
  greeting: string;
  iosSubject: string;
  iosHeadline: string;
  iosBody: string;
  androidSubject: string;
  androidHeadline: string;
  androidBody: string;
  bothSubject: string;
  bothHeadline: string;
  bothBody: string;
  delayedHint: string;
  appStoreLabel: string;
  playStoreLabel: string;
  supportLine: string;
  closing: string;
  signature: string;
}

export const betaUserEmailContent: Record<Locale, BetaUserEmailContent> = {
  es: {
    greeting: 'Hola,',
    iosSubject: 'Ya puedes descargar BiciRadar en App Store',
    iosHeadline: 'Tu acceso en iPhone ya está listo',
    iosBody:
      'Gracias por apuntarte. Ya puedes entrar en BiciRadar desde App Store y empezar a usar la app en iPhone.',
    androidSubject: 'Tu acceso beta de Android ya está en marcha',
    androidHeadline: 'Tu acceso beta de Android está en proceso',
    androidBody:
      'Hemos recibido tu solicitud. En paralelo estamos tramitando tu alta en el grupo de testers para que puedas entrar desde Google Play.',
    bothSubject: 'Tu acceso a BiciRadar ya está en marcha',
    bothHeadline: 'Ya tienes el acceso preparado para BiciRadar',
    bothBody:
      'Hemos recibido tu solicitud para iPhone y Android. Te dejamos ambos enlaces para que puedas entrar desde la plataforma que prefieras.',
    delayedHint:
      'Si Google Play todavía no te deja entrar, espera un poco y vuelve a probar en unos minutos.',
    appStoreLabel: 'Abrir App Store',
    playStoreLabel: 'Abrir Google Play',
    supportLine: 'Si algo no te encaja, responde a este correo y lo revisamos.',
    closing: 'Gracias por apuntarte a la beta.',
    signature: 'Equipo BiciRadar',
  },
  en: {
    greeting: 'Hi,',
    iosSubject: 'BiciRadar is ready for you on the App Store',
    iosHeadline: 'Your iPhone access is ready',
    iosBody:
      'Thanks for signing up. You can now open BiciRadar from the App Store and start using the app on iPhone.',
    androidSubject: 'Your Android beta access is in progress',
    androidHeadline: 'Your Android beta access is being prepared',
    androidBody:
      'We received your request. In parallel, we are processing your access to the tester group so you can join from Google Play.',
    bothSubject: 'Your BiciRadar access is already in motion',
    bothHeadline: 'Your BiciRadar access is ready to go',
    bothBody:
      'We received your request for iPhone and Android. Here are both links so you can open BiciRadar from the platform you prefer.',
    delayedHint:
      'If Google Play still does not let you in, wait a bit and try again in a few minutes.',
    appStoreLabel: 'Open App Store',
    playStoreLabel: 'Open Google Play',
    supportLine: 'If anything looks off, reply to this email and we will check it.',
    closing: 'Thanks for joining the beta.',
    signature: 'The BiciRadar team',
  },
  ca: {
    greeting: 'Hola,',
    iosSubject: 'Ja pots descarregar BiciRadar a l’App Store',
    iosHeadline: 'El teu accés a iPhone ja està llest',
    iosBody:
      'Gràcies per apuntar-t’hi. Ja pots obrir BiciRadar des de l’App Store i començar a fer servir l’app a iPhone.',
    androidSubject: 'El teu accés beta d’Android ja està en marxa',
    androidHeadline: 'El teu accés beta d’Android s’està preparant',
    androidBody:
      'Hem rebut la teva sol·licitud. En paral·lel estem tramitant l’alta al grup de testers perquè puguis entrar des de Google Play.',
    bothSubject: 'El teu accés a BiciRadar ja està en marxa',
    bothHeadline: 'Ja tens l’accés preparat per a BiciRadar',
    bothBody:
      'Hem rebut la teva sol·licitud per a iPhone i Android. Et deixem tots dos enllaços perquè puguis entrar des de la plataforma que prefereixis.',
    delayedHint:
      'Si Google Play encara no et deixa entrar, espera una mica i torna-ho a provar d’aquí uns minuts.',
    appStoreLabel: 'Obrir App Store',
    playStoreLabel: 'Obrir Google Play',
    supportLine: 'Si hi ha res que no et quadri, respon aquest correu i ho revisarem.',
    closing: 'Gràcies per apuntar-te a la beta.',
    signature: 'Equip BiciRadar',
  },
  gl: {
    greeting: 'Ola,',
    iosSubject: 'Xa podes descargar BiciRadar na App Store',
    iosHeadline: 'O teu acceso en iPhone xa está listo',
    iosBody:
      'Grazas por apuntarte. Xa podes abrir BiciRadar desde a App Store e comezar a usar a app en iPhone.',
    androidSubject: 'O teu acceso beta de Android xa está en marcha',
    androidHeadline: 'O teu acceso beta de Android estase preparando',
    androidBody:
      'Recibimos a túa solicitude. En paralelo estamos tramitando a alta no grupo de testers para que poidas entrar desde Google Play.',
    bothSubject: 'O teu acceso a BiciRadar xa está en marcha',
    bothHeadline: 'Xa tes o acceso preparado para BiciRadar',
    bothBody:
      'Recibimos a túa solicitude para iPhone e Android. Deixámosche as dúas ligazóns para que poidas entrar desde a plataforma que prefiras.',
    delayedHint:
      'Se Google Play aínda non che deixa entrar, agarda un pouco e volve tentalo nuns minutos.',
    appStoreLabel: 'Abrir App Store',
    playStoreLabel: 'Abrir Google Play',
    supportLine: 'Se algo non encaixa, responde a este correo e revisámolo.',
    closing: 'Grazas por apuntarte á beta.',
    signature: 'Equipo BiciRadar',
  },
  eu: {
    greeting: 'Kaixo,',
    iosSubject: 'BiciRadar App Store-n deskargatzeko prest duzu',
    iosHeadline: 'Zure iPhone sarbidea prest dago',
    iosBody:
      'Eskerrik asko izena emateagatik. Orain BiciRadar App Store-tik ireki dezakezu eta iPhone-n erabiltzen hasi.',
    androidSubject: 'Zure Android beta sarbidea martxan dago',
    androidHeadline: 'Zure Android beta sarbidea prestatzen ari gara',
    androidBody:
      'Zure eskaera jaso dugu. Aldi berean, tester taldeko alta izapidetzen ari gara, Google Play-tik sar zaitezen.',
    bothSubject: 'Zure BiciRadar sarbidea martxan dago jada',
    bothHeadline: 'BiciRadarerako sarbidea prest duzu',
    bothBody:
      'Zure iPhone eta Android eskaera jaso dugu. Bi estekak uzten dizkizugu nahi duzun plataformatik sar zaitezen.',
    delayedHint:
      'Google Play-k oraindik sartzen uzten ez badizu, itxaron pixka bat eta saiatu berriro minutu batzuen buruan.',
    appStoreLabel: 'Ireki App Store',
    playStoreLabel: 'Ireki Google Play',
    supportLine: 'Zerbait ondo ez badabil, erantzun mezu honi eta begiratuko dugu.',
    closing: 'Eskerrik asko betan izena emateagatik.',
    signature: 'BiciRadar taldea',
  },
};


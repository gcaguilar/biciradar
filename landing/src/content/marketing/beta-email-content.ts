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
  googleGroupLabel: string;
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
    androidSubject: 'Siguiente paso: únete al grupo de testers de BiciRadar',
    androidHeadline: 'Únete al grupo en Google con tu cuenta',
    androidBody:
      'Para la beta en Android, entra en el grupo testers-biciradar en Google Groups con la misma cuenta de Google que usarás en el móvil. Cuando estés dentro, instala BiciRadar desde Google Play.',
    bothSubject: 'Siguiente paso: grupo de testers e instalación de BiciRadar',
    bothHeadline: 'Android: grupo en Google primero; iPhone: App Store',
    bothBody:
      'Para Android, únete al grupo testers-biciradar en Google con tu cuenta de Google y luego instala desde Google Play. Para iPhone, también puedes instalar desde App Store cuando quieras.',
    delayedHint:
      'Si Google Play no muestra la beta al momento, espera unos minutos tras unirte al grupo y vuelve a probar.',
    googleGroupLabel: 'Unirse al grupo testers-biciradar',
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
    androidSubject: 'Next step: join the BiciRadar testers group',
    androidHeadline: 'Join the Google group with your Google account',
    androidBody:
      'For the Android beta, join the testers-biciradar group on Google Groups using the same Google account you use on your phone. Then install BiciRadar from Google Play.',
    bothSubject: 'Next step: testers group and installing BiciRadar',
    bothHeadline: 'Android: Google group first; iPhone: App Store',
    bothBody:
      'For Android, join testers-biciradar on Google with your Google account, then install from Google Play. For iPhone, you can also install from the App Store whenever you like.',
    delayedHint:
      'If Google Play does not show the beta right away, wait a few minutes after joining the group and try again.',
    googleGroupLabel: 'Join testers-biciradar group',
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
    androidSubject: 'Pas següent: uneix-te al grup de testers de BiciRadar',
    androidHeadline: 'Entra al grup a Google amb el teu compte',
    androidBody:
      'Per a la beta d’Android, entra al grup testers-biciradar a Google Groups amb el mateix compte de Google que faràs servir al mòbil. Després instal·la BiciRadar des de Google Play.',
    bothSubject: 'Pas següent: grup de testers i instal·lació de BiciRadar',
    bothHeadline: 'Android: primer el grup a Google; iPhone: App Store',
    bothBody:
      'Per a Android, uneix-te al grup testers-biciradar a Google amb el teu compte i després instal·la des de Google Play. Per a iPhone, també pots instal·lar des de l’App Store quan vulguis.',
    delayedHint:
      'Si Google Play no mostra la beta de seguida, espera uns minuts després d’entrar al grup i torna-ho a provar.',
    googleGroupLabel: 'Unir-se al grup testers-biciradar',
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
    androidSubject: 'Seguinte paso: únete ao grupo de testers de BiciRadar',
    androidHeadline: 'Entra no grupo en Google coa túa conta',
    androidBody:
      'Para a beta en Android, entra no grupo testers-biciradar en Google Groups coa mesma conta de Google que usarás no móbil. Despois instala BiciRadar desde Google Play.',
    bothSubject: 'Seguinte paso: grupo de testers e instalación de BiciRadar',
    bothHeadline: 'Android: primeiro o grupo en Google; iPhone: App Store',
    bothBody:
      'Para Android, únete ao grupo testers-biciradar en Google coa túa conta e despois instala desde Google Play. Para iPhone, tamén podes instalar desde a App Store cando queiras.',
    delayedHint:
      'Se Google Play non amosa a beta enseguida, agarda uns minutos tras entrar no grupo e volve tentalo.',
    googleGroupLabel: 'Unirse ao grupo testers-biciradar',
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
    androidSubject: 'Hurrengo urratsa: batu testers taldera BiciRadarren',
    androidHeadline: 'Sartu Google taldea zure Google kontuarekin',
    androidBody:
      'Android betarako, sartu testers-biciradar Google Groups taldean telefonoan erabiliko duzun Google kontu berarekin. Gero instalatu BiciRadar Google Play-tik.',
    bothSubject: 'Hurrengo urratsa: testers taldea eta BiciRadar instalatzea',
    bothHeadline: 'Android: lehenik Google taldea; iPhone: App Store',
    bothBody:
      'Androiderako, batu testers-biciradar taldera zure Google kontuarekin eta instalatu Google Play-tik. iPhonerako, App Store-tik ere instalatu dezakezu nahi duzunean.',
    delayedHint:
      'Google Play-k beta berehala erakusten ez badizu, itxaron taldean sartu ondoren minutu batzuk eta saiatu berriro.',
    googleGroupLabel: 'Batu testers-biciradar taldera',
    appStoreLabel: 'Ireki App Store',
    playStoreLabel: 'Ireki Google Play',
    supportLine: 'Zerbait ondo ez badabil, erantzun mezu honi eta begiratuko dugu.',
    closing: 'Eskerrik asko betan izena emateagatik.',
    signature: 'BiciRadar taldea',
  },
};


import type { CityKey, CityPageContent, Locale } from '../content/marketing/types';

export interface CityPageMessaging {
  title: string;
  description: string;
  seoTitle: string;
  seoDescription: string;
}

function getCityName(badge: string) {
  const [cityName = badge] = badge.split(' · ');
  return cityName.trim();
}

function esMessaging(cityKey: CityKey, cityName: string): CityPageMessaging {
  switch (cityKey) {
    case 'madrid':
      return {
        title: 'Mira BiciMAD antes de bajar a la calle',
        description:
          'Abre tu base de siempre y comprueba si hoy te compensa ir a por la bici o si es mejor cambiar de estación.',
        seoTitle: 'BiciMAD en Madrid: mira tu estación antes de salir',
        seoDescription:
          'Consulta BiciMAD y comprueba si tu base habitual tiene bicis o huecos antes de bajar a la calle.',
      };
    case 'barcelona':
      return {
        title: 'Comprueba Bicing antes de salir del metro',
        description:
          'Si esa estación no te viene bien, lo sabes antes de subir a la calle y dar la vuelta.',
        seoTitle: 'Bicing en Barcelona: compruébalo antes de llegar',
        seoDescription:
          'Consulta Bicing y decide si seguir ruta, cambiar de base o esperar un poco antes de llegar.',
      };
    case 'sevilla':
      return {
        title: 'Mira Sevici antes de arrancar',
        description:
          'Comprueba si tu estación tiene bicis o huecos y evita plantarte allí para nada.',
        seoTitle: 'Sevici en Sevilla: compruébalo antes de salir',
        seoDescription:
          'Consulta Sevici y mira si tu estación habitual te compensa antes de empezar el trayecto.',
      };
    case 'valencia':
      return {
        title: 'Comprueba Valenbisi antes de acercarte',
        description:
          'Te sirve para ver si esa base te viene bien para coger o dejar la bici sin improvisar sobre la marcha.',
        seoTitle: 'Valenbisi en Valencia: míralo antes de acercarte',
        seoDescription:
          'Consulta Valenbisi y decide mejor dónde coger o dejar la bici antes de llegar a la estación.',
      };
    case 'zaragoza':
      return {
        title: 'Mira Bizi antes de ponerte en marcha',
        description:
          'Abres tu estación habitual y sabes enseguida si te compensa salir ya o mirar otra base.',
        seoTitle: 'Bizi en Zaragoza: míralo antes de salir',
        seoDescription:
          'Consulta Bizi y comprueba si tu estación habitual tiene bicis o huecos antes de ponerte en marcha.',
      };
    default:
      return {
        title: `Mira tu estación en ${cityName} antes de salir`,
        description:
          `Consulta bicis y huecos en ${cityName} y decide si te compensa salir ya o si prefieres ir a otra estación.`,
        seoTitle: `${cityName}: consulta tu estación antes de salir`,
        seoDescription:
          `Consulta bicis y huecos en ${cityName} y comprueba si te compensa salir ya o cambiar de estación.`,
      };
  }
}

function enMessaging(cityKey: CityKey, cityName: string): CityPageMessaging {
  switch (cityKey) {
    case 'madrid':
      return {
        title: 'Check BiciMAD before you head out',
        description:
          'Open the base you usually use and you will quickly see whether it is worth heading there or trying another station.',
        seoTitle: 'BiciMAD in Madrid: check your station before you leave',
        seoDescription:
          'Check BiciMAD and see whether your usual base has bikes or docks before you step outside.',
      };
    case 'barcelona':
      return {
        title: 'Check Bicing before you leave the metro',
        description:
          'If that station is not going to work, it is better to know before you come up to street level and take the longer way round.',
        seoTitle: 'Bicing in Barcelona: check it before you arrive',
        seoDescription:
          'Check Bicing and decide whether to stay on route, switch bases, or wait a bit before you get there.',
      };
    case 'sevilla':
      return {
        title: 'Check Sevici before you get moving',
        description:
          'See whether your station has bikes or docks before you set off and avoid walking there for nothing.',
        seoTitle: 'Sevici in Seville: check it before you leave',
        seoDescription:
          'Check Sevici and see whether your usual station is worth heading to before your trip starts.',
      };
    case 'valencia':
      return {
        title: 'Check Valenbisi before you get close',
        description:
          'It helps you see whether that station makes sense for picking up or returning a bike before you start improvising on the move.',
        seoTitle: 'Valenbisi in Valencia: check before you get close',
        seoDescription:
          'Check Valenbisi and choose a better station before you get there.',
      };
    case 'zaragoza':
      return {
        title: 'Check Bizi before you set off',
        description:
          'Open the station you usually use and you will quickly know whether it is worth heading out now or trying another base.',
        seoTitle: 'Bizi in Zaragoza: check before you leave',
        seoDescription:
          'Check Bizi and see whether your usual station has bikes or docks before you start moving.',
      };
    default:
      return {
        title: `Check your station in ${cityName} before you leave`,
        description:
          `Check bikes and docks in ${cityName} and see whether it is worth heading out now or trying another station.`,
        seoTitle: `${cityName}: check your station before you leave`,
        seoDescription:
          `Check bikes and docks in ${cityName} and see whether it makes sense to head out now or switch stations.`,
      };
  }
}

function caMessaging(cityKey: CityKey, cityName: string): CityPageMessaging {
  switch (cityKey) {
    case 'madrid':
      return {
        title: 'Mira BiciMAD abans de baixar al carrer',
        description:
          'Comprova si la teva base habitual de BiciMAD té bicis o ancoratges abans de sortir i evita baixar sense context.',
        seoTitle: 'BiciMAD a Madrid: decideix abans de sortir',
        seoDescription:
          'Consulta BiciMAD i decideix si la teva base habitual et compensa abans de baixar al carrer.',
      };
    case 'barcelona':
      return {
        title: 'Comprova Bicing abans de sortir del metro',
        description:
          'Mira si aquella estació de Bicing et convé abans de sortir del metro, seguir ruta o canviar de base.',
        seoTitle: 'Bicing a Barcelona: evita el desviament abans d’arribar',
        seoDescription:
          'Consulta Bicing i decideix si mantens ruta, canvies de base o esperes abans de sortir del metro o de l’oficina.',
      };
    case 'sevilla':
      return {
        title: 'Mira Sevici abans d’arrencar',
        description:
          'Comprova si la teva estació habitual de Sevici té bicis o ancoratges abans d’arrencar el trajecte.',
        seoTitle: 'Sevici a Sevilla: surt amb menys incertesa',
        seoDescription:
          'Consulta Sevici i decideix si vas a la teva estació habitual, esperes o canvies de base abans de començar el trajecte.',
      };
    case 'valencia':
      return {
        title: 'Comprova Valenbisi abans d’acostar-t’hi',
        description:
          'Veu si aquella estació et convé per agafar o deixar bici abans d’improvisar sobre la marxa.',
        seoTitle: 'Valenbisi a València: decideix abans d’acostar-t’hi',
        seoDescription:
          'Comprova Valenbisi i decideix on agafar o tornar bici abans d’acostar-te a l’estació.',
      };
    case 'zaragoza':
      return {
        title: 'Mira Bizi abans de posar-te en marxa',
        description:
          'Obre la teva estació habitual de Bizi i decideix si surts ara, esperes o canvies de base abans de moure’t.',
        seoTitle: 'Bizi a Saragossa: comprova abans de pedalar',
        seoDescription:
          'Consulta Bizi i decideix si la teva estació habitual et compensa abans de sortir o canviar de base.',
      };
    default:
      return {
        title: `Mira la teva estació a ${cityName} abans de sortir`,
        description:
          `Consulta bicis i ancoratges a ${cityName} i decideix si et convé sortir ara, esperar o canviar de base abans de moure’t.`,
        seoTitle: `${cityName}: decideix la teva estació abans de sortir`,
        seoDescription:
          `Consulta bicis i ancoratges a ${cityName} i decideix si et convé sortir ara, esperar o canviar de base abans de moure’t.`,
      };
  }
}

function glMessaging(cityKey: CityKey, cityName: string): CityPageMessaging {
  switch (cityKey) {
    case 'madrid':
      return {
        title: 'Mira BiciMAD antes de baixar á rúa',
        description:
          'Comproba se a túa base habitual de BiciMAD ten bicis ou prazas antes de saír e evita facelo sen contexto.',
        seoTitle: 'BiciMAD en Madrid: decide antes de saír',
        seoDescription:
          'Consulta BiciMAD e decide se a túa base habitual che compensa antes de baixar a rúa.',
      };
    case 'barcelona':
      return {
        title: 'Comproba Bicing antes de saír do metro',
        description:
          'Mira se esa estación de Bicing che compensa antes de saír do metro, seguir ruta ou cambiar de base.',
        seoTitle: 'Bicing en Barcelona: evita o desvío antes de chegar',
        seoDescription:
          'Consulta Bicing e decide se mantés ruta, cambias de base ou esperas antes de saír do metro ou da oficina.',
      };
    case 'sevilla':
      return {
        title: 'Mira Sevici antes de arrincar',
        description:
          'Comproba se a túa estación habitual de Sevici ten bicis ou prazas antes de arrincar o traxecto.',
        seoTitle: 'Sevici en Sevilla: sae con menos incerteza',
        seoDescription:
          'Consulta Sevici e decide se vas á túa estación habitual, esperas ou cambias de base antes de comezar o traxecto.',
      };
    case 'valencia':
      return {
        title: 'Comproba Valenbisi antes de achegarte',
        description:
          'Ve se esa estación che convén para coller ou devolver bici antes de improvisar sobre a marcha.',
        seoTitle: 'Valenbisi en Valencia: decide antes de achegarte',
        seoDescription:
          'Comproba Valenbisi e decide onde coller ou devolver bici antes de achegarte á estación.',
      };
    case 'zaragoza':
      return {
        title: 'Mira Bizi antes de poñerte en marcha',
        description:
          'Abre a túa estación habitual de Bizi e decide se saes xa, esperas ou cambias de base antes de poñerte en marcha.',
        seoTitle: 'Bizi en Zaragoza: comproba antes de pedalear',
        seoDescription:
          'Consulta Bizi e decide se a túa estación habitual che compensa antes de saír ou cambiar de base.',
      };
    default:
      return {
        title: `Mira a túa estación en ${cityName} antes de saír`,
        description:
          `Consulta bicis e prazas en ${cityName} e decide se che convén saír xa, esperar ou cambiar de base antes de moverte.`,
        seoTitle: `${cityName}: decide a túa estación antes de saír`,
        seoDescription:
          `Consulta bicis e prazas en ${cityName} e decide se saír xa, esperar ou cambiar de base antes de moverte.`,
      };
  }
}

function euMessaging(cityKey: CityKey, cityName: string): CityPageMessaging {
  switch (cityKey) {
    case 'madrid':
      return {
        title: 'Begiratu BiciMAD kalera atera aurretik',
        description:
          'Egiaztatu zure ohiko BiciMAD oinarriak bizikletak edo lekuak dituen abiatu aurretik eta ez irten testuingururik gabe.',
        seoTitle: 'BiciMAD Madrilen: erabaki atera aurretik',
        seoDescription:
          'Kontsultatu BiciMAD eta erabaki zure ohiko oinarriak merezi duen kalera atera aurretik.',
      };
    case 'barcelona':
      return {
        title: 'Begiratu Bicing metroa utzi aurretik',
        description:
          'Ikusi Bicing geltoki horrek merezi duen metroa utzi, ibilbidea mantendu edo oinarria aldatu aurretik.',
        seoTitle: 'Bicing Bartzelonan: saihestu desbideratzea iritsi aurretik',
        seoDescription:
          'Kontsultatu Bicing eta erabaki ibilbidea mantendu, oinarria aldatu edo itxaron metroa edo bulegoa utzi aurretik.',
      };
    case 'sevilla':
      return {
        title: 'Begiratu Sevici abiatu aurretik',
        description:
          'Egiaztatu zure ohiko Sevici geltokiak bizikletak edo lekuak dituen ibilbidea hasi aurretik.',
        seoTitle: 'Sevici Sevillan: irten ziurgabetasun gutxiagorekin',
        seoDescription:
          'Kontsultatu Sevici eta erabaki zure ohiko geltokira joan, itxaron edo oinarria aldatu ibilbidea hasi aurretik.',
      };
    case 'valencia':
      return {
        title: 'Begiratu Valenbisi hurbildu aurretik',
        description:
          'Ikusi geltoki hori bizikleta hartzeko edo uzteko egokia den, bidean inprobisatu aurretik.',
        seoTitle: 'Valenbisi Valentzian: erabaki hurbildu aurretik',
        seoDescription:
          'Kontsultatu Valenbisi eta erabaki non hartu edo utzi bizikleta geltokira hurbildu aurretik.',
      };
    case 'zaragoza':
      return {
        title: 'Begiratu Bizi abiatu aurretik',
        description:
          'Ireki zure ohiko Bizi geltokia eta erabaki orain abiatu, itxaron edo oinarria aldatu behar duzun.',
        seoTitle: 'Bizi Zaragozan: egiaztatu pedalei ekin aurretik',
        seoDescription:
          'Kontsultatu Bizi eta erabaki zure ohiko geltokiak merezi duen abiatu edo oinarria aldatu aurretik.',
      };
    default:
      return {
        title: `Begiratu zure geltokia ${cityName} hirian irten aurretik`,
        description:
          `${cityName} hiriko bizikletak eta lekuak kontsultatu, eta irten, itxaron edo oinarria aldatu behar duzun erabaki mugitu aurretik.`,
        seoTitle: `${cityName}: erabaki zure geltokia irten aurretik`,
        seoDescription:
          `${cityName} hiriko bizikletak eta lekuak kontsultatu, eta irten, itxaron edo oinarria aldatu behar duzun erabaki mugitu aurretik.`,
      };
  }
}

export function getCityPageMessaging(
  locale: Locale,
  cityKey: CityKey,
  city: CityPageContent,
): CityPageMessaging {
  const cityName = getCityName(city.badge);

  switch (locale) {
    case 'es':
      return esMessaging(cityKey, cityName);
    case 'en':
      return enMessaging(cityKey, cityName);
    case 'ca':
      return caMessaging(cityKey, cityName);
    case 'gl':
      return glMessaging(cityKey, cityName);
    case 'eu':
      return euMessaging(cityKey, cityName);
    default:
      return {
        title: city.title,
        description: city.description,
        seoTitle: city.seo.title,
        seoDescription: city.seo.description,
      };
  }
}

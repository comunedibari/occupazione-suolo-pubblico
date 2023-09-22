// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

import { Environment } from "@models/environment";

export const environment: Environment = {
  production: false,
  occupazioneSuoloPubblico_BE_URL: "",
  useMoks: false,
  tipologiaAmbiente: 'Collaudo',
  groups: {
    idGruppoAdmin: 1,
    idGruppoOperatoreSportello: 2,
    idGruppoDirettoreMunicipio: 3,
    idGruppoIstruttoreMunicipio: 4,
    idGruppoPoliziaLocale: 5,
    idGruppoRipartizioneUrbanistica: 7,
    idGruppoConcessionario: 13,
    idGruppoSettoreUrbanizzazioniPrimarie: 14,
    idGruppoSettoreGiardini: 15,
    idGruppoSettoreInterventiTerritorio: 16,
    idGruppoSettoreInfrastruttureRete: 17,
    idGruppoRipartizionePatrimonio: 18,
    idGruppoEGov: 20,
    idGruppoDestinatariOrdinanze: 21
  },
  roles: {
    idRuoloPersonaFisica: 1,
    idRuoloAltro: 6
  },
  processes: {
    concessioneTemporanea: 1,
    prorogaConcessioneTemporanea: 2,
    rinunciaConcessione: 3,
    revocaConcessione: 5,
    decadenzaConcessione: 6,
    annullamentoConcessione: 7,
    rettificaErrori: 4
  },
  statiPratica: {
    bozza: 0,
    inserita: 1,
    verificaFormale: 2,
    richiestaPareri: 3,
    necessariaIntegrazione: 4,
    praticaDaRigettare: 5,
    approvata: 6,
    preavvisoDiniego: 7,
    attesaPagamento: 8,
    prontaRilascio: 9,
    archiviata: 18,
    rigettata: 20,
    concessioneValida: 10,
    rettificaDate: 27,
    rinunciata: 26,
    revocata: 21,
    decaduta: 22,
    annullata: 23,
    terminata: 25,
    inseritaModificaDate: 28
  },
  template: {
    determinaDiConcessione: 1,
    determinaDiProroga: 2,
    determinaRigetto: 3,
    determinaRettifica: 5,
    determinaDiRevoca: 6,
    determinaDiDecadenza: 7,
    determinaDiAnnullamento: 8,
    ordinanza: 10,
    relazioneDiServizio: 11,
    urbanizzazioniPrimarie: 12,
    settoreGiardini: 13,
    interventiTerritorio: 14,
    infrastruttureRete: 15,
    istruttoriaTecnicaUrbanistica: 16,
    istruttoriaTecnicaPatrimonio: 17
  },
  scadenzario: {
    concessioneScaduta: 1,
    concessioneTemporaneaInScadenza: 2,
    tempiProcedimentaliScaduti: 3,
    tempiProcedimentaliInScadenza: 4,
    scadenzaTempisticheIntegrazione: 5,
    scadenzaTempisticheDiniego: 6,
    scadenzaTempistichePagamento: 7,
  },
  validaPratica: {
    verificaOccupazione: 'VERIFICA_OCCUPAZIONE',
    saltaVerificaOccupazione: 'SALTA_VERIFICA_OCCUPAZIONE',
    occupazioneCorretta: 'OCCUPAZIONE_CORRETTA',
  },
  tipologiaTitoloEdilizio: {
    altro: 4,
  },
  tipologiaAllegati: {
    ordinanza: 15
  },
  defaultTableRows: 5,
  maxRichiesteIntegrazione: 2,
  scadenzarioCheckSeconds: 60, // 1 minuto
  diffMininimaOreOccupazione: 60 //1h

};

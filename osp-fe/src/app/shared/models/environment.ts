export interface Environment {
    production: boolean,
    occupazioneSuoloPubblico_BE_URL: string,
    useMoks: boolean,
    groups: {
        idGruppoAdmin: number,
        idGruppoOperatoreSportello: number,
        idGruppoDirettoreMunicipio: number,
        idGruppoIstruttoreMunicipio: number,
        idGruppoPoliziaLocale: number,
        idGruppoRipartizioneUrbanistica: number,
        idGruppoConcessionario: number,
        idGruppoSettoreUrbanizzazioniPrimarie: number,
        idGruppoSettoreGiardini: number,
        idGruppoSettoreInterventiTerritorio: number,
        idGruppoSettoreInfrastruttureRete: number,
        idGruppoRipartizionePatrimonio: number,
        idGruppoEGov: number,
        idGruppoDestinatariOrdinanze: number
    },
    roles: {
        idRuoloPersonaFisica: number,
        idRuoloAltro: number
    },
    processes: {
        concessioneTemporanea: number,
        prorogaConcessioneTemporanea: number,
        rinunciaConcessione: number,
        revocaConcessione: number,
        decadenzaConcessione: number,
        annullamentoConcessione: number
        rettificaErrori: number,
    },
    statiPratica: {
      bozza: number,
      inserita: number,
      verificaFormale: number,
      richiestaPareri: number,
      necessariaIntegrazione: number,
      praticaDaRigettare: number,
      approvata: number,
      preavvisoDiniego: number,
      attesaPagamento: number,
      prontaRilascio: number,
      archiviata: number,
      rigettata: number,
      concessioneValida: number,
      rettificaDate: number,
      rinunciata: number,
      revocata: number,
      decaduta: number,
      annullata: number,
      terminata: number,
      inseritaModificaDate: number
    };
    template: {
        relazioneDiServizio: number,
        determinaRigetto: number,
        determinaDiConcessione: number,
        determinaRettifica: number,
        determinaDiProroga: number,
        determinaDiRevoca: number,
        determinaDiDecadenza: number,
        determinaDiAnnullamento: number,
        urbanizzazioniPrimarie: number,
        ordinanza: number,
        settoreGiardini: number,
        interventiTerritorio: number,
        infrastruttureRete: number,
        istruttoriaTecnicaUrbanistica: number,
        istruttoriaTecnicaPatrimonio: number
    },
    scadenzario: {
        concessioneScaduta: number,
        concessioneTemporaneaInScadenza: number,
        tempiProcedimentaliScaduti: number,
        tempiProcedimentaliInScadenza: number,
        scadenzaTempisticheIntegrazione: number,
        scadenzaTempisticheDiniego: number,
        scadenzaTempistichePagamento: number,
      },
    validaPratica: {
        verificaOccupazione: string,
        saltaVerificaOccupazione: string,
        occupazioneCorretta: string,
    },
    tipologiaTitoloEdilizio: {
        altro: number;
    }
    tipologiaAllegati: {
        ordinanza: number
    },
    defaultTableRows: number,
    maxRichiesteIntegrazione: number,
    scadenzarioCheckSeconds: number,
    diffMininimaOreOccupazione: number,
    tipologiaAmbiente: string


}

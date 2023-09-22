export class RicercaPraticaRequest {
    idsMunicipi: number[];
    idStatoPratica: number;
    idsStatiPratica: number[];
    nome: string;
    cognome: string;
    denominazioneRagSoc: string;
    codFiscalePIva: string;
    numProtocollo: string;
    numProvvedimento: string;
    indirizzo: string;
    tipologiaProcesso: string;
    richiestaVerificaRipristinoLuoghi?: boolean;
}

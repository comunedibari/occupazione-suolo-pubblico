export class RichiedenteDTO {
    id: number;
    idTipoRuoloRichiedente: number;
    nome: string;
    cognome: string;
    denominazione: string;
    codiceFiscalePartitaIva: string;
    dataDiNascita: string;
    comuneDiNascita: string;
    provinciaDiNascita: string;
    nazionalita: string;
    citta: string;
    indirizzo: string;
    civico: string;
    provincia: string;
    cap: string;
    recapitoTelefonico: string;
    email: string;
    idTipoDocumentoAllegato: number;
    numeroDocumentoAllegato: string;
    amministrazioneDocumentoAllegato: string;
    flagFirmatario: boolean;
    flagDestinatario: boolean;

    dataDiNascitaForm: Date;
}

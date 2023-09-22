import {UtenteDTO} from "@models/utente-dto";
import {environment} from "environments/environment";
import {DatiRichiestaDTO} from "./dati-richiesta-dto";
import {ProtocolloDTO} from "./protocollo-dto";
import {RichiedenteDTO} from "./richiedente-dto";
import {RichiestaIntegrazioneDTO} from "./richiesta-integrazione-dto";
import {RichiestaParereDTO} from "./richiesta-parere-dto";
import {TipologicaDTO} from "./tipologica-dto";

export class PraticaDto {
  id: number;
  idPraticaOriginaria: number;
  idProrogaPrecedente: number;
  nomeCittadinoEgov: string;
  cognomeCittadinoEgov: string;
  cfCittadinoEgov: string;
  motivazioneRichiesta: string;
  infoPassaggioStato: string;
  datiRichiesta: DatiRichiestaDTO;
  firmatario: RichiedenteDTO;
  destinatario: RichiedenteDTO;
  municipio: TipologicaDTO;
  utentePresaInCarico: UtenteDTO;
  utenteCreazione: UtenteDTO;
  utenteModifica: UtenteDTO;
  utenteAssegnatario: UtenteDTO;
  dataCreazione: Date;
  dataInserimento: Date;
  dataModifica: Date;
  tipoProcesso: TipologicaDTO;
  statoPratica: TipologicaDTO;
  flagVerificaFormale: boolean;
  flagProceduraDiniego: boolean;
  protocolli: ProtocolloDTO[];
  codiceDetermina: string;
  dataScadenzaPratica: Date;
  dataScadenzaRigetto: Date;
  dataScadenzaPreavvisoDiniego: Date;
  dataScadenzaPagamento: Date;
  contatoreRichiesteIntegrazioni: number;
  richiestePareri: RichiestaParereDTO[];
  richiesteIntegrazioni: RichiestaIntegrazioneDTO[];
  flagEsenzionePagamentoCUP: boolean;
  motivazioneEsenzionePagamentoCup: string;
  qualitaRuolo: string;
  descrizioneRuolo: string;

  get sortDate(): Date {
    return this.dataModifica ? this.dataModifica : this.dataCreazione;
  }

  set sortDate(date: Date) {
  }

  get getProtocollo(): string {
    let ret = null;
    if (this.protocolli) {
      console.log(this.protocolli);
      let protocollo: ProtocolloDTO = this.protocolli.find((x: ProtocolloDTO) => x.idStatoPratica === environment.statiPratica.inserita);
      console.log(protocollo);
      if (protocollo) {
        ret = protocollo.codiceProtocollo;
      }
    }
    return ret;
  }
}

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { GeoMultiPointDTO } from '@models/dto/geo-multi-point-dto';
import { ParereDTO } from '@models/dto/parere-dto';
import { PraticaDto } from '@models/dto/pratica-dto';
import { LoginInfo } from '@models/login/login-info';
import { Page } from '@models/page';
import { ParereInsertRequest } from '@models/parere-insert-request';
import { PraticaRequest } from '@models/pratica-request';
import { RicercaPraticaRequest } from '@models/ricerca-pratica-request';
import { Observable } from 'rxjs';
import { BaseService } from './base/base-service';
import { OccupazioneSuoloPubblicoApiConfiguration } from './base/occupazione-suolo-pubblico-api-configuration';
import { PageRequest } from './base/page-request';
import { UtilityService } from '@services/utility.service';
import { RichiestaIntegrazioneDTO } from '@models/dto/richiesta-integrazione-dto';
import { RichiestaParereDTO } from '@models/dto/richiesta-parere-dto';
import { EsenzioneCup } from '@models/esenzione-cup';
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class PraticaService extends BaseService {

  constructor(
    protected config: OccupazioneSuoloPubblicoApiConfiguration,
    private utilityService: UtilityService,
    protected http: HttpClient
  ) {
    super(config, http);
  }

  public getPratica(id: number): Observable<PraticaDto> {
    return this.httpGet(`pratica-management/pratica/${id}`);
  }

  public inserisciPratica(data: PraticaRequest): Observable<PraticaDto>
  {
    return this.httpPost<PraticaDto>("pratica-management/pratica", data);
  }

  public modificaPratica(data: PraticaRequest): Observable<PraticaDto>
  {
    return this.httpPut<PraticaDto>("pratica-management/pratica", data);
  }

  public rettificaConcessione(data: PraticaRequest): Observable<PraticaDto>
  {
    return this.httpPut<PraticaDto>("pratica-management/pratica/rettifica", data);
  }

  public verificaOccupazione(idPratica: number, idUtente: number, tipoOperazione: string, coordUbicazioneDefinitiva: GeoMultiPointDTO, skipCheck: boolean): Observable<PraticaDto>
  {
    const params = {
      idPratica: idPratica,
      idUtente: idUtente,
      tipoOperazione: tipoOperazione,
      coordUbicazioneDefinitiva: coordUbicazioneDefinitiva,
      skipCheck: skipCheck
    }

    return this.httpPut<PraticaDto>("pratica-management/pratica/verifica-occupazione", params);
  }

  public checkSovrapposizioneOccupazione(idPratica: number, idUtente: number, tipoOperazione: string, coordUbicazioneDefinitiva: GeoMultiPointDTO): Observable<any> {
    return this.httpPost<any>("pratica-management/pratica/check-sovrapposizione-occupazione", { idPratica: idPratica, idUtente: idUtente, tipoOperazione: tipoOperazione, coordUbicazioneDefinitiva: coordUbicazioneDefinitiva });
  }

  public statoInserita(idPratica: number, idUtente: number): Observable<any> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtente', ''+idUtente);
    return this.httpPut<any>("pratica-management/pratica/stato-inserita", params);
  }

  public statoPresaInCarico(idPratica: number, idUtenteIstruttore : number, idUtenteDirettore?: number): Observable<any> {
    let params = this.defaultParams;

    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtenteIstruttore', ''+idUtenteIstruttore);
    if (idUtenteDirettore) {
      params = params.append('idUtenteDirettore', ''+idUtenteDirettore);
    }

    return this.httpPut<any>("pratica-management/pratica/presa-in-carico", params);
  }

  public approvaPratica(idPratica: number, idUtente: number): Observable<PraticaDto> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtente', ''+idUtente);
    return this.httpPut("pratica-management/pratica/approvazione", params);
  }

  public archiviazionePratica(idPratica: number, idUtente : number): Observable<PraticaDto> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtente', ''+idUtente);
    return this.httpPut<any>("pratica-management/pratica/archiviazione", params);
  }

  public praticaDaRigettare(idPratica: number, idUtenteRichiedente: number, testoNota: string, flagEsenzioneMarcaDaBollo: boolean, idAllegato: number): Observable<any> {
    return this.httpPost<any>("pratica-management/pratica/rigetto-richiesta",
      {
        idPratica: idPratica,
        idUtenteRichiedente: idUtenteRichiedente,
        flagEsenzioneMarcaDaBollo,
        idAllegato,
        motivoRichiesta: testoNota
      });
  }

  public statoRichiestaPareri(idPratica: number, idUtenteRichiedente: number, riabilitaEsenzioneMarcaDaBollo: boolean): Observable<RichiestaParereDTO> {
    return this.httpPost<any>("richiesta-parere-management/richiesta-parere/verifica-formale", {
      idPratica: idPratica,
      idUtenteRichiedente: idUtenteRichiedente,
      riabilitaEsenzioneMarcaDaBollo
    });
  }

  public esprimiPareri(parere: ParereInsertRequest, isRipristinoLuoghi = false): Observable<ParereDTO> {
    return this.httpPost<ParereDTO>("parere-management/parere?isRipristinoLuoghi=" + isRipristinoLuoghi, parere);
  }


  public statoRichiestaIntegrazione(idPratica: number, idUtenteRichiedente: number, testoNota: string, flagEsenzioneMarcaDaBollo?: boolean, idAllegato?: number): Observable<RichiestaIntegrazioneDTO> {
    return this.httpPost<any>("richiesta-integrazione-management/richiesta-integrazione",
      {
        idPratica: idPratica,
        idUtenteRichiedente: idUtenteRichiedente,
        motivoRichiesta: testoNota,
        flagEsenzioneMarcaDaBollo,
        idAllegato
      });
  }

  public statoIntegrazione(idRichiestaIntegrazione: number, idUtenteIntegrazione: number, testoNota?: string): Observable<any> {
    return this.httpPost<any>("integrazione-management/integrazione", { idRichiestaIntegrazione: idRichiestaIntegrazione, idUtenteIntegrazione: idUtenteIntegrazione, motivoIntegrazione: testoNota });
  }

  public statoDeterminaRigetto(idPratica: number, idUtente: number, codiceDeterminaRigetto: string, dataEmissioneDetermina: string): Observable<PraticaDto> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtente', ''+idUtente);
    params = params.append('codiceDeterminaRigetto', codiceDeterminaRigetto);
    params = params.append('dataEmissioneDetermina', dataEmissioneDetermina);

    return this.httpPut("pratica-management/pratica/inserimento-determina/rigetto", params);
  }

  public richiestaUlterioriPareri(idPratica: number, idGruppiDestinatariPareri: number[]): Observable<any> {
    return this.httpPost<any>("richiesta-parere-management/richiesta-parere/ulteriori-pareri", { idPratica: idPratica, idGruppiDestinatariPareri: idGruppiDestinatariPareri });
  }

  public statoProntoRilascio(idPratica: number, idUtente: number): Observable<PraticaDto> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtente', ''+idUtente);
    return this.httpPut<PraticaDto>("pratica-management/pratica/pronto-al-rilascio", params);
  }

  public statoConcessioneValida(idPratica: number, idUtente: number): Observable<PraticaDto> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtente', ''+idUtente);
    return this.httpPut<PraticaDto>("pratica-management/pratica/concessione-valida", params);
  }

  public inserimentoDetermina(idPratica: number, idUtente: number, codiceDetermina: string, dataEmissioneDetermina: string): Observable<PraticaDto> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtente', ''+idUtente);
    params = params.append('codiceDetermina', codiceDetermina);
    params = params.append('dataEmissioneDetermina', dataEmissioneDetermina);
    return this.httpPut<PraticaDto>("pratica-management/pratica/inserimento-determina", params);
  }

  public inserimentoDeterminaRevoca(idPratica: number, idUtente: number, codiceDetermina: string, dataEmissioneDetermina: string, notaAlCittadino: string): Observable<PraticaDto> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtente', ''+idUtente);
    params = params.append('codiceDetermina', codiceDetermina);
    params = params.append('notaAlCittadino', notaAlCittadino);
    params = params.append('dataEmissioneDetermina', dataEmissioneDetermina);
    return this.httpPut<PraticaDto>("pratica-management/pratica/inserimento-determina/revoca", params);
  }

  public inserimentoDeterminaAnnullamento(idPratica: number, idUtente: number, codiceDetermina: string, dataEmissioneDetermina: string, notaAlCittadino: string): Observable<PraticaDto> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtente', ''+idUtente);
    params = params.append('codiceDetermina', codiceDetermina);
    params = params.append('notaAlCittadino', notaAlCittadino);
    params = params.append('dataEmissioneDetermina', dataEmissioneDetermina);
    return this.httpPut<PraticaDto>("pratica-management/pratica/inserimento-determina/annullamento", params);
  }

  public inserimentoDeterminaDecadenza(idPratica: number, idUtente: number, codiceDetermina: string, dataEmissioneDetermina: string, notaAlCittadino: string): Observable<PraticaDto> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtente', ''+idUtente);
    params = params.append('codiceDetermina', codiceDetermina);
    params = params.append('notaAlCittadino', notaAlCittadino);
    params = params.append('dataEmissioneDetermina', dataEmissioneDetermina);
    return this.httpPut<PraticaDto>("pratica-management/pratica/inserimento-determina/decadenza", params);
  }

  public getPratiche(data: RicercaPraticaRequest, user: LoginInfo) : Observable<Page>{
    let params = this.defaultParams;

    if (data.idsMunicipi) {
      params = params.append('idsMunicipi', user.userLogged.municipio_ids.toString());
    }
    if (data.idStatoPratica != null) {
      params = params.append('idsStatiPratica', ''+ data.idStatoPratica);
    }
    if (data.idsStatiPratica != null) {
      params = params.append('idsStatiPratica', data.idsStatiPratica.toString());
    }
    if (data.nome) {
      params = params.append('nome', ''+ data.nome);
    }
    if (data.cognome) {
      params = params.append('cognome', ''+ data.cognome);
    }
    if (data.denominazioneRagSoc) {
      params = params.append('denominazioneRagSoc', ''+ data.denominazioneRagSoc);
    }
    if (data.codFiscalePIva) {
      params = params.append('codFiscalePIva', ''+ data.codFiscalePIva);
    }
    if (data.numProtocollo) {
      params = params.append('numProtocollo', ''+ data.numProtocollo);
    }
    if (data.numProvvedimento) {
      params = params.append('numProvvedimento', ''+ data.numProvvedimento);
    }
    if (data.indirizzo) {
      params = params.append('indirizzo', '' + data.indirizzo);
    }
    if (data.tipologiaProcesso) {
      params = params.append('tipologiaProcesso', '' + data.tipologiaProcesso);
    }
    if (data.richiestaVerificaRipristinoLuoghi) {
      params = params.append('richiestaVerificaRipristinoLuoghi', ''+ data.richiestaVerificaRipristinoLuoghi);
    }

    return this.httpGet<Page>('pratica-management/pratica', params).pipe(
      map(
        el => {
          el.content.map(
            row => {
              if (row.protocolli != null) {
                row.protocollo = UtilityService.getProtocolloInserimento(row.protocolli);
              }
              return row;
            }
          );
          return el;
        }
      )
    );
  }

  public getCountPratiche(idsStatiPratica: number[], idsMunicipi?: number[]) : Observable<number> {
    let params = this.defaultParams;
    params = params.append('idsStatiPratica', '' + idsStatiPratica);
    if(idsMunicipi.length > 0) {
      params = params.append('idsMunicipi', '' + idsMunicipi);
    }
    return this.httpGet<number>('pratica-management/pratica/count', params);
  }

  public eliminaPratica(idPratica: number) {
    return this.httpDelete<any>(`pratica-management/pratica/${idPratica}`);
  }


  public storicoPratica(idPratica: number, page: PageRequest): Observable<Page> {
    let params = this.getDefaultPageParams(page);
    params = params.append('id', ''+idPratica);
    return this.httpGet('pratica-management/pratica/storico', params);
  }

  public prorogaPreCompilata(idPratica: number): Observable<any> {
    return this.httpGet(`pratica-management/pratica/proroga/pratica-precompilata/${idPratica}`);
  }

  public dichiaraEsenzioneCup(esenzione: EsenzioneCup): Observable<any> {
    return this.httpPut(`pratica-management/flag-esenzione-cup`, esenzione);
  }

  public inizioRettificaDate(idPratica: number, idUtente: number, notaAlCittadino?: string): Observable<RichiestaIntegrazioneDTO> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idUtente', ''+idUtente);
    params = params.append('notaAlCittadino', notaAlCittadino);
    return this.httpPut<RichiestaIntegrazioneDTO>("richiesta-integrazione-management/richiesta-rettifica-date", params);
  }

  public concludiRettificaDate(idRichiestaIntegrazione: number, idUtenteIntegrazione: number,
    dataInizioOccupazione?: string, oraInizioOccupazione?: string,
    dataScadenzaOccupazione?: string, oraScadenzaOccupazione?: string): Observable<any> {
    return this.httpPost<any>("integrazione-management/rettifica-date",
    { idRichiestaIntegrazione: idRichiestaIntegrazione, idUtenteIntegrazione: idUtenteIntegrazione,
      dataInizioOccupazione: dataInizioOccupazione, oraInizioOccupazione: oraInizioOccupazione,
      dataScadenzaOccupazione: dataScadenzaOccupazione, oraScadenzaOccupazione: oraScadenzaOccupazione});
  }

  public rinunciaPreCompilata(idPratica: number): Observable<any> {
    return this.httpGet(`pratica-management/pratica/rinuncia/pratica-precompilata/${idPratica}`);

  }
}

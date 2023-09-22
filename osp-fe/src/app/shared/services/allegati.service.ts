import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AllegatoIntegrazioneInsertRequest } from '@models/allegato-integrazione-insert-request';
import { AllegatoParereInsertRequest } from '@models/allegato-parere-insert-request';
import { AllegatoPraticaInsertRequest } from '@models/allegato-pratica-insert-request';
import { AllegatoRichiestaIntegrazioneInsertRequest } from '@models/allegato-richiesta-integrazione-insert-request';
import { AllegatoDTO } from '@models/dto/allegato-dto';
import { Observable } from 'rxjs';
import { BaseService } from './base/base-service';
import { OccupazioneSuoloPubblicoApiConfiguration } from './base/occupazione-suolo-pubblico-api-configuration';

@Injectable({
  providedIn: 'root'
})
export class AllegatiService extends BaseService {

  constructor(
    protected config: OccupazioneSuoloPubblicoApiConfiguration,
    protected http: HttpClient
  ) {
    super(config, http);
  }

  public sendAllegatoPratica(allegato: AllegatoPraticaInsertRequest): Observable<AllegatoDTO> {
    return this.httpPost<AllegatoDTO>('allegato-management/allegato/pratica', allegato);
  }
  public sendAllegatoParere(allegato: AllegatoParereInsertRequest): Observable<AllegatoDTO> {
    return this.httpPost<AllegatoDTO>('allegato-management/allegato/parere', allegato);
  }
  public sendAllegatoIntegrazione(allegato: AllegatoIntegrazioneInsertRequest): Observable<AllegatoDTO> {
    return this.httpPost<AllegatoDTO>('allegato-management/allegato/integrazione', allegato);
  }
  public sendAllegatoRichiestaIntegrazione(allegato: AllegatoRichiestaIntegrazioneInsertRequest): Observable<AllegatoDTO> {
    return this.httpPost<AllegatoDTO>('allegato-management/allegato/richiesta-integrazione', allegato);
  }

  public getAllegatiPraticaPerStato(idPratica: number, idStatoPratica: number): Observable<AllegatoDTO[]> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idStatoPratica', ''+idStatoPratica);
    return this.httpGet<AllegatoDTO[]>('allegato-management/allegato', params);
  }
  public getAllegatiPraticaPerStatoAndTipoProcesso(idPratica: number, idStatoPratica: number, idTipoProcesso: number): Observable<AllegatoDTO[]> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idStatoPratica', '' + idStatoPratica);
    params = params.append('idTipoProcesso', '' + idTipoProcesso);
    return this.httpGet<AllegatoDTO[]>('allegato-management/allegato', params);
  }

  public getAllegatiPratica(idPratica: number, idTipoProcesso: number): Observable<AllegatoDTO[]> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    params = params.append('idTipoProcesso', ''+idTipoProcesso);
    return this.httpGet<AllegatoDTO[]>('allegato-management/allegato', params);
  }

  public getAllegatiRichiestaParere(idRichiestaParere): Observable<AllegatoDTO[]> {
    let params = this.defaultParams;
    params = params.append('idRichiestaParere', idRichiestaParere);
    return this.httpGet<AllegatoDTO[]>('allegato-management/allegato/richiesta-parere', params);
  }

  public getAllegato(idAllegato: number): Observable<AllegatoDTO> {
    return this.httpGet<AllegatoDTO>(`allegato-management/allegato/${idAllegato}`);
  }

  public deleteAllegato(idAllegato: number): Observable<any>{
    return this.httpDelete<any>(`allegato-management/allegato/${idAllegato}`);
  }

  public documentalePratica(idPratica: number): Observable<any> {
    let params = this.defaultParams;
    params = params.append('idPratica', ''+idPratica);
    return this.httpGet<AllegatoDTO[]>('allegato-management/allegato/documentale-pratica', params);
  }
}

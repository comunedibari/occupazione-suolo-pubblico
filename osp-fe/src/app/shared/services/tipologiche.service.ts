import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { GruppoDTO } from '@models/dto/gruppo-dto';
import { PraticaDto } from '@models/dto/pratica-dto';
import { TemplateDTO } from '@models/dto/template-dto';
import { TipoAllegatoDTO } from '@models/dto/tipo-allegato-dto';
import { TipoAllegatoGruppoStatoProcessoDTO } from '@models/dto/tipo-allegato-gruppo-stato-processo-dto';
import { TipoManufatto } from '@models/dto/tipo-manufatto';
import { TipologicaDTO } from '@models/dto/tipologica-dto';
import { TipologicaTestoLibero } from '@models/dto/tipologica-testo-libero-dto';
import { LoginInfo } from '@models/login/login-info';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { CacheableBase } from './base/cacheable-base';
import { OccupazioneSuoloPubblicoApiConfiguration } from './base/occupazione-suolo-pubblico-api-configuration';

@Injectable({
  providedIn: 'root'
})
export class TipologicheService extends CacheableBase {
  private emtpyItem: TipologicaDTO = {id: null, descrizione: '-- Selezionare un elemento --'};

  constructor(
    protected config: OccupazioneSuoloPubblicoApiConfiguration,
    protected http: HttpClient
  ) {
    super(config, http);
  }

  public getGruppi(): Observable<GruppoDTO[]> {
    return this.httpGet<GruppoDTO[]>('tipologiche/gruppi');
  }

  public getMunicipi(): Observable<TipologicaDTO[]> {
    return this.httpGet<TipologicaDTO[]>('tipologiche/municipi');
  }

  public getStatiPratiche(): Observable<TipologicaDTO[]> {
    return this.httpGet<TipologicaDTO[]>('tipologiche/stati-pratiche');
  }

  public getTipiProcesso(): Observable<TipologicaDTO[]> {
    return this.httpGet<TipologicaDTO[]>('tipologiche/tipi-processi');
  }

  //Servizio recupero tipi allegati per stato e processo. 
  public getAllegati(user: LoginInfo, idPratica: number, idTipoProcesso : number): Observable<TipoAllegatoGruppoStatoProcessoDTO[]> {
    let params = this.defaultParams;
    params = params.append('idUtente', '' + user.userLogged.id);
    params = params.append('idStatoPratica', '' + idPratica);
    params = params.append('idTipoProcesso', '' + idTipoProcesso);
    return this.httpGetNoCache<TipoAllegatoGruppoStatoProcessoDTO[]>('tipologiche/tipi-allegati-stato-processo', params);
  }

  public getTipiAllegati(user: LoginInfo, pratica: PraticaDto): Observable<TipoAllegatoGruppoStatoProcessoDTO[]> {
    let params = this.defaultParams;
    params = params.append('idUtente', '' + user.userLogged.id);
    params = params.append('idPratica', '' + pratica.id);
    return this.httpGetNoCache<TipoAllegatoGruppoStatoProcessoDTO[]>('tipologiche/tipi-allegati', params);
  }

  public getAllTipiAllegati(): Observable<TipoAllegatoDTO[]> {
    return this.httpGet('tipologiche/tipi-allegati/all');
  }

  public getTipiRuoliRichiedenti(addEmptySelection?:boolean): Observable<TipologicaDTO[]> {
    return this.httpGet<TipologicaDTO[]>('tipologiche/tipi-ruoli-richiedenti').pipe(
      tap(data=>{
        if (addEmptySelection) {
          data.unshift(this.emtpyItem);
        }
      })
    );
  }

  public getTipoAttivitaDaSvolgere(): Observable<TipologicaTestoLibero[]> {
    return this.httpGet<TipologicaTestoLibero[]>('tipologiche/tipi-attivita-da-svolgere');
  }

  public getTipoDocumento(addEmptySelection?:boolean): Observable<TipologicaDTO[]> {
    return this.httpGet<TipologicaDTO[]>('tipologiche/tipi-documenti-allegati').pipe(
      tap(data=>{
        if (addEmptySelection) {
          data.unshift(this.emtpyItem);
        }
      })
    );
  }

  public getTipoTitoloEdilizio(): Observable<TipologicaTestoLibero[]> {
    return this.httpGet<TipologicaTestoLibero[]>('tipologiche/tipologie-titoli-edilizi');
  }

  public getTipoManufatto(): Observable<TipoManufatto[]> {
    return this.httpGet<TipoManufatto[]>('tipologiche/tipi-manufatti');
  }

  public getTipoTemplate(): Observable<TipologicaDTO[]> {
    return this.httpGet<TipologicaDTO[]>('tipologiche/tipi-template');
  }
}

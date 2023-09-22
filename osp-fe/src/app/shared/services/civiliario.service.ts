import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CiviliarioResponse } from '@models/civiliario-response';
import { DataSingoloMunicipioResponse } from '@models/data-singolo-municipio-response';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BaseService } from './base/base-service';
import { OccupazioneSuoloPubblicoApiConfiguration } from './base/occupazione-suolo-pubblico-api-configuration';

@Injectable({
  providedIn: 'root'
})
export class CiviliarioService extends BaseService {

  constructor(
    protected config: OccupazioneSuoloPubblicoApiConfiguration,
    protected http: HttpClient
  ) { 
    super(config, http);
  }

  public civico(indirizzo: string, municipio_id?: number) {
    let numero: string = indirizzo.replace(/\D/g,'');  
    let splitIndirizzo = indirizzo.split(',');
    indirizzo = splitIndirizzo[0].replace(/[^a-zA-Z' ]/g, '').trim(); 
 
    if(splitIndirizzo.length > 1){
      let splitCivico = splitIndirizzo[1].split('(');
      numero = splitCivico[0].replace(/\D/g,'');
    }
    if (numero != null && numero.trim().length == 0)
    {
      numero = null;
    }

    return this.getDataSingoloMunicipio(indirizzo, numero, municipio_id).pipe(map(data => data.result));
  }

  public getDataSingoloMunicipio(indirizzo: string, numero: string, idMunicipio?: number): Observable<DataSingoloMunicipioResponse> {
    let params = this.defaultParams;
    params = params.append('indirizzo', indirizzo);
    if (numero) {
      params = params.append('numero', numero);
    }
    if (idMunicipio) {
      params = params.append('idMunicipio', ''+idMunicipio);
    }

    return this.httpGet<DataSingoloMunicipioResponse>('civiliario/data-singolo-municipio', params);
  }

  public addressListFormatter(data: CiviliarioResponse) {
    return data ? (data.nome_via || '--') + ', ' + (data.numero || '--') + (data.esponente ? `/${data.esponente}` : '') + ' (' + data.municipio + ')' + (data.localita ? ` - ${data.localita}` : '') : '';
  }
}

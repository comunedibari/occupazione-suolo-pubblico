import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseService } from './base/base-service';
import { OccupazioneSuoloPubblicoApiConfiguration } from './base/occupazione-suolo-pubblico-api-configuration';

@Injectable({
  providedIn: 'root'
})
export class ScadenzarioService extends BaseService {

  constructor(
    protected config: OccupazioneSuoloPubblicoApiConfiguration,
    protected http: HttpClient, 
  ) {
    super(config, http);
  }

  public cercaNotificheScadenziario(idUtente: number): Observable<any>{
    let params = this.defaultParams;
    params = params.append('idUtente', ''+idUtente);
    return this.httpGet('notifica-scadenzario-management/notifica-scadenzario', params);
  }

  public numeroNotifiche(idUtente: number): Observable<any>{
    let params = this.defaultParams;
    params = params.append('idUtente', ''+idUtente);
    return this.httpGet('notifica-scadenzario-management/notifica-scadenzario/count', params);
  }
}

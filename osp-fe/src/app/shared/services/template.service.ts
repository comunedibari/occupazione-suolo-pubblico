import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TemplateDTO } from '@models/dto/template-dto';
import { Observable } from 'rxjs';
import { BaseService } from './base/base-service';
import { OccupazioneSuoloPubblicoApiConfiguration } from './base/occupazione-suolo-pubblico-api-configuration';

@Injectable({
  providedIn: 'root'
})
export class TemplateService extends BaseService {

  constructor(
    protected config: OccupazioneSuoloPubblicoApiConfiguration,
    protected http: HttpClient
  ) {
    super(config, http);
  }

  public insertTemplate(template: TemplateDTO) {
    return this.httpPost<TemplateDTO>('template-management/template', template);
  }

  public updateTemplate(template: TemplateDTO) {
    return this.httpPut<TemplateDTO>('template-management/template', template);
  }

  public getTemplate(idTipoTemplate: number): Observable<TemplateDTO> {
    let params = this.defaultParams;
    params = params.append('idTipoTemplate', ''+idTipoTemplate);
    return this.httpGet<TemplateDTO>('template-management/template/tipo', params);
  }

  public getTemplateElaborato(idPratica: number, idTipoTemplate: number, notaParere?: string): Observable<TemplateDTO> {
    let params = this.defaultParams;

    params = params.append('idPratica', ''+idPratica);
    params = params.append('idTipoTemplate', ''+idTipoTemplate);
    if(notaParere) {
      params = params.append('notaParere', ''+notaParere);
    }
    return this.httpGet<TemplateDTO>('template-management/template/elaborato', params);
  }

  public getAllTemplates(): Observable<TemplateDTO[]> {
    return this.httpGet<TemplateDTO[]>('template-management/template');
  }
}

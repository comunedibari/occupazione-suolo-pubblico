import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { SessionService } from "@services/session.service";
import { Observable, of, throwError } from "rxjs";
import { tap } from "rxjs/operators";
import { BaseService } from "./base-service";
import { ServiceConfiguration } from "./service-configuration";

export class CacheableBase extends BaseService {
    private session: SessionService = new SessionService();

    constructor(
        protected config: ServiceConfiguration,
        protected http: HttpClient,
    ) {
        super(config, http);
    }

    protected httpGetNoCache<T>(uri: string, params?: HttpParams, headers?: HttpHeaders): Observable<T> {
        return super.httpGet(uri, params, headers);
    }

    protected httpGet<T>(uri: string, params?: HttpParams, headers?: HttpHeaders): Observable<T> {
        if (params) {
            throwError("Params are not supported");
        }
        let ret: any = this.session.getCacheValue(uri);
        if (!ret) {
            ret = super.httpGet<T>(uri, params, headers).pipe(
                tap(data=>{
                    this.session.setCacheValue(uri, data); 
                })
            );
        }
        else {
            ret = of(ret);
        }
        return ret;
    }

    protected httpPost<T>(uri: string, payload:any, headers?: HttpHeaders): Observable<T> {
        return throwError("Operation not supported");
    }

}

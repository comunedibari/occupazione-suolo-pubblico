import { ServiceConfiguration } from "./service-configuration";
import { HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import { CustomHttpUrlEncoderCodec } from "./custom-http-url-encoder-codec";
import { Observable } from "rxjs";
import { TokenStorage } from "@services/token.storage.service";
import { PageRequest } from "./page-request";
import { SortDirection, SortRequest } from "./sort-request";

export class BaseService {

    constructor(
        protected config: ServiceConfiguration,
        protected http: HttpClient,
    ) {}

    protected get defaultHeaders(): HttpHeaders {
        let headers = new HttpHeaders();
        let tokenStorage = new TokenStorage();
        headers = headers.set('Accept', 'application/json');
        headers = headers.set('Authorization', `Bearer ${tokenStorage.getToken()}`); 
        return headers;
    }

    protected get defaultParams(): HttpParams {
        let params = new HttpParams({encoder: new CustomHttpUrlEncoderCodec()});
        return params;
    }

    protected getDefaultPageParams(page: PageRequest): HttpParams {
        let params = this.defaultParams;
        if (page) {
            params = params.append('page', ''+page.page);
            params = params.append('size', ''+page.size);
            if (page.sort) {
                page.sort.forEach((elem: SortRequest)=> {
                    params = params.append('sort', `${elem.field},${SortDirection[elem.direction].toLowerCase()}`);
                });
            }
        }
        else {
            params = params.append('size', '1');
            params = params.append('page', '0');
        }

        return params;
    }

    get rootUrl(): string {
        return this.config.rootUrl;
    }

    protected httpGet<T>(uri: string, params?: HttpParams, headers?: HttpHeaders): Observable<T> {
        if (headers === undefined || headers === null) {
            headers = this.defaultHeaders;
        }
        if (params == undefined || params === null) {
            params = this.defaultParams;
        }

        return this.http.get<T> (
            `${this.rootUrl}/${uri}`,
            {headers: headers, params: params}
        );
    }

    protected httpPost<T>(uri: string, payload:any, headers?: HttpHeaders): Observable<T> {
        if (headers === undefined || headers === null) {
            headers = this.defaultHeaders;
        }
        const httpOptions = {headers: headers};
 
        return this.http.post<T> (
            `${this.rootUrl}/${uri}`,
            payload,
            httpOptions
        );
    }

    protected httpPut<T>(uri: string, payload:any, headers?: HttpHeaders): Observable<T> {
        if (headers === undefined || headers === null) {
            headers = this.defaultHeaders;
        }
        const httpOptions = {headers: headers};
 
        return this.http.put<T> (
            `${this.rootUrl}/${uri}`,
            payload,
            httpOptions
        );
    }

    protected httpDelete<T>(uri: string, headers?: HttpHeaders): Observable<T> {
        if (headers === undefined || headers === null) {
            headers = this.defaultHeaders;
        }
        const httpOptions = {headers: headers};
 
        return this.http.delete<T> (
            `${this.rootUrl}/${uri}`,
            httpOptions
        );
    }

}




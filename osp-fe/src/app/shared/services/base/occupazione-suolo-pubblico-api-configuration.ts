import { Injectable } from "@angular/core";
import { environment } from "environments/environment";
import { ServiceConfiguration } from "./service-configuration";

@Injectable({
    providedIn: 'root'
})
export class OccupazioneSuoloPubblicoApiConfiguration implements ServiceConfiguration {
    rootUrl: string = environment.occupazioneSuoloPubblico_BE_URL;
}

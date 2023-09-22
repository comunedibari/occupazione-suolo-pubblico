import { TestBed } from "@angular/core/testing";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { BaseService } from "./base-service";
import { OccupazioneSuoloPubblicoApiConfiguration } from "./occupazione-suolo-pubblico-api-configuration";
import { ServiceConfiguration } from "./service-configuration";
import { HttpClient } from "@angular/common/http";

describe('BaseService', () => {
  const config: ServiceConfiguration = new OccupazioneSuoloPubblicoApiConfiguration();
  let service: BaseService = new BaseService(config, null as unknown as HttpClient);

  it('should create an instance', () => {
    expect(service).toBeTruthy();
  });
});

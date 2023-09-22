import { HttpClient } from '@angular/common/http';
import { CacheableBase } from './cacheable-base';
import { OccupazioneSuoloPubblicoApiConfiguration } from './occupazione-suolo-pubblico-api-configuration';
import { ServiceConfiguration } from './service-configuration';

describe('CacheableBase', () => {
  const config: ServiceConfiguration = new OccupazioneSuoloPubblicoApiConfiguration();
  let service: CacheableBase = new CacheableBase(config, null as unknown as HttpClient);
  it('should create an instance', () => {
    expect(service).toBeTruthy();
  });
});

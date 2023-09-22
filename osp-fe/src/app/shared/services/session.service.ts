import { THIS_EXPR } from "@angular/compiler/src/output/output_ast";
import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private static readonly CACHE_KEY_PREFIX = "cache-";

  constructor() {}

  private buildCacheKey(key: string): string {
    return `${SessionService.CACHE_KEY_PREFIX}${key}`;
  }

  public getCacheValue(key: string): any {
    return this.getSessionValue(this.buildCacheKey(key));
  }

  public setCacheValue(key: string, obj: any): void {
    this.setSessionValue(this.buildCacheKey(key), obj);
  }

  public getSessionValue(key: string): any {
    let ret = JSON.parse(window.sessionStorage.getItem(key));
    return ret;
  }

  public setSessionValue(key: string, obj: any): void {
    sessionStorage.setItem(key, JSON.stringify(obj));
  }

  public clear(): void {
    sessionStorage.clear();
  }

  public clearCache(): void {
    let key: string;
    let blackList: string[] = [];

    for (let i=0; i<sessionStorage.length; i++)
    {
      key = sessionStorage.key(i);
      if (key.startsWith(SessionService.CACHE_KEY_PREFIX)) {
        blackList.push(key);
      }
    }
    blackList.forEach(
      (elem: string) => {
        sessionStorage.removeItem(elem);
      }
    );
  }
}

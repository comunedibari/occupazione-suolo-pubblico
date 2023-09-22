import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '@services/auth/auth.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private router: Router, private auth: AuthService) { }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree 
  {
    let ret: boolean = false;

    if (this.auth.getToken()) {
      if (route.data.requiredGroup === undefined ||
          route.data.requiredGroup === null) {
          ret = true;
      } else if (this.auth.checkGroups(route.data.requiredGroup)) {
          ret = true;
      } else {
          this.router.navigate(['/unauthorized-error']);
      }
    }
    else {
      // not logged in so redirect to login page with the return url
      this.router.navigate(['/login']);
    }
    return ret;
  }
}

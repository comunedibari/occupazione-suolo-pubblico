import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';

@Injectable({
  providedIn: 'root',
})
export class ErrorInterceptor implements HttpInterceptor {

  constructor(private spinnerDialogService: SpinnerDialogService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError(
        (error: HttpErrorResponse, _caught: Observable<HttpEvent<any>>) =>
        {
          console.log("AuthInterceptor. Error: ", error);
          this.spinnerDialogService.showSpinner(false);
          return throwError(error);
        }
      )
    );
  }
}

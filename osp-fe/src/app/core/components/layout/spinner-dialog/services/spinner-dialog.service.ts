import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class SpinnerDialogService {
  private subject = new BehaviorSubject<boolean>(false);
  
  constructor() { }

  isShowingSpinner(): boolean {
    return this.subject.getValue();
  }

  showSpinner(show: boolean) 
  {
    this.subject.next(show);
  }

  onShowSpinner(): Observable<boolean> {
    return this.subject.asObservable();
  }
}

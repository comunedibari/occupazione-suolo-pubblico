import { Injectable } from '@angular/core';
import {MessageService as MSPrimeNG} from 'primeng/api';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  constructor(private messageService: MSPrimeNG) { }

  private getErrorMessage(err: any): string {
    let ret = '';
console.log("err:", err);
    if(err && err.title) {
      if(err.title.includes("Errore in fase di Richiesta Protocollo Entrata: Error while insert in")) {
        ret = "Errore in fase di Richiesta Protocollo Entrata: Errore nell'inserimento in Audit";
      } else {
        ret = err.title;
      }
    } else if (err.error.code && err.error.message) {
      ret = err.error.message;
    }    
    else if (err.code && err.message) {
      ret = err.message;
    } 
    else {
      ret = "Si è verificato un problema. Si prega di riprovare più tardi.";
    }   
    return ret;
  }

  showErrorMessage(summary: string, err: any, life?: number) {

    this.showMessage('error', summary, this.getErrorMessage(err), life);
  }

  showMessage(severity: string, summary: string, detail: string, life?: number){
    // this.close();

    if(!life)
      switch(severity) { 
          case 'success': { 
              life = 3000;
              break; 
          } 
          case 'warn': { 
              life = 5000; 
              break; 
          } 
          case 'error': { 
              life = 7000; 
              break; 
            }
          default: { 
              life = 3000;
              break; 
          } 
      } 

    this.messageService.add({severity: severity, summary: summary, detail: detail, life: life});
  }

  close(){
    this.messageService.clear();
  }
}

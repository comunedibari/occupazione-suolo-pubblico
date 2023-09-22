import { ChangeDetectorRef, Component, DoCheck, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { MediaMatcher } from '@angular/cdk/layout';
import { DialogService } from 'primeng/dynamicdialog';
import { MenuItem } from 'primeng/api';
import { Group } from '@enums/Group.enum';
import { AuthService } from '@services/auth/auth.service';
import { UtilityService } from '@services/utility.service';
import { Municipio } from '@enums/Municipio.enum';
import { MessageService } from '@services/message.service';
import { ScadenzarioService } from '@services/scadenzario.service';
import { timer } from 'rxjs';
import { environment } from 'environments/environment';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { ScadenzarioComponent } from '@features/scadenzario/scadenzario.component';
import { HelpComponent } from '@shared-components/help/help.component';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy  {

  @Output() isLogged: EventEmitter<any> = new EventEmitter<any>();
  @Output() clickMenuButton: EventEmitter<any> = new EventEmitter<any>();
  isAuthenticate: boolean = false;
  routerLinkRielaborazionePareri: string = '/pratiche_da_lavorare/rielaborazione_pareri';
  routerLinkRichiestaPareri: string = '/pratiche_da_lavorare/richiesta_pareri';
  routerLinkPresaInCarico: string = '/pratiche_da_lavorare/presa_in_carico';
  tipologiaAmbiente: string = environment.tipologiaAmbiente;

  menuItems: MenuItem[] = [];
  homeItems: MenuItem = {icon: 'pi pi-home', routerLink: '/home'};

  //scadenziario
  badgeValue = 0;
  dataSource: any[] = [];
  notificheControllate: boolean = false;
  erroriNotifiche: number = 0;

  mobileQuery: MediaQueryList;
  private _mobileQueryListener: () => void;

  constructor(
      public dialogService: DialogService,
      private router: Router,
      private activatedRoute: ActivatedRoute,
      private authService: AuthService,
      private scadenzarioService : ScadenzarioService,
      public utilityService: UtilityService,
      private messageService: MessageService,
      private spinnerService: SpinnerDialogService,
      changeDetectorRef: ChangeDetectorRef,
      media: MediaMatcher
    ) {
      this.mobileQuery = media.matchMedia('(max-width: 650px)');
      this._mobileQueryListener = () => changeDetectorRef.detectChanges();
      this.mobileQuery.addEventListener('change', this._mobileQueryListener);
  }

  get infoUtente(): string {
    let ret: string = this.isAuthenticate ? this.authService.getInfoUtente() : '';
    return ret;
  }

  get tipoUtente(): string{
    return this.isAuthenticate ? Group[this.authService.getGroup()] : '';
  }

  get lastLogin(): string {
    return this.isAuthenticate ? this.authService.getLastLogin() : '';
  }

  get isAdmin(): boolean{
    return this.authService.getGroup() == Group.Admin ? true : false;
  }

  get isInternalUser(): boolean{
    return this.utilityService.isInternalUser();
  }

  get municipiAppartenenza(): string {
    let municipi_id = this.authService.getMunicipiAppartenenza();
    return municipi_id.length > 0 ? `Municipio ${municipi_id}` : '';
  }

  get infoBadge(): string {
    return this.badgeValue ? 'Clicca qui per vedere le notifiche dello scadenzario' : 'Non ci sono notifiche dello scadenzario';
  }

  get abilitaNotifiche(): boolean{
    return this.authService.getGroup() == environment.groups.idGruppoDirettoreMunicipio
            || this.authService.getGroup() == environment.groups.idGruppoIstruttoreMunicipio
            || this.authService.getGroup() == environment.groups.idGruppoPoliziaLocale
            || this.authService.getGroup() == environment.groups.idGruppoRipartizionePatrimonio
            || this.authService.getGroup() == environment.groups.idGruppoRipartizioneUrbanistica
          ? true : false;
  }

  ngOnInit() {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.menuItems = this.createBreadcrumbs(this.activatedRoute.root)
      }
    );
    this.isAuthenticate = this.authService.checkAuth();
    this.isLogged.emit(this.isAuthenticate);
    this.authService.onUserLogged().subscribe(
      (data: boolean) => {
        this.isAuthenticate = data;
        this.isLogged.emit(this.isAuthenticate);
        if (data) {
          this.initScadenziario();
        }
      }
    );
    this.initScadenziario();
    timer(0, environment.scadenzarioCheckSeconds * 1000).subscribe(() => {
      this.checkScadenzario();
    });
  }

  createBreadcrumbs(activatedRoute: ActivatedRoute, url: string = '', breadcrumbs: MenuItem[] = []): MenuItem[] {
    var children: ActivatedRoute[] = activatedRoute.children;

    if (children.length === 0) {
      return breadcrumbs;
    }

    for (var child of children) {
      var routeURL: string = child.snapshot.url.map(segment => segment.path).join('/');
      if (routeURL !== '') {
        url += `/${routeURL}`;
      }

      var label = child.snapshot.data['breadcrumb'];

      if(url == this.routerLinkRielaborazionePareri){
        label = this.authService.getGroup() == Group.IstruttoreMunicipio || this.authService.getGroup() == Group.DirettoreMunicipio ? 'Rielaborazione pareri' : 'Esprimi parere'
      }

      if(url == this.routerLinkRichiestaPareri) {
        label = this.authService.getGroup() == Group.IstruttoreMunicipio || this.authService.getGroup() == Group.DirettoreMunicipio ? 'Rielaborazione pareri': 'Esprimi parere';
      }

      if(url == this.routerLinkPresaInCarico) {
        label = this.authService.getGroup() == Group.IstruttoreMunicipio || this.authService.getGroup() == Group.OperatoreSportello ? 'Presa in carico' : 'Assegnazione pratica';
      }

      if (label) {
        breadcrumbs.push({label, url});
      }

      return this.createBreadcrumbs(child, url, breadcrumbs);
    }
  }

  private checkScadenzario(): void {
    if (this.authService.checkAuth()) {
      if (this.abilitaNotifiche && !this.notificheControllate && !this.spinnerService.isShowingSpinner()) {
        this.initScadenziario();
      }
    }
  }

  ngOnDestroy() {
    this.mobileQuery.removeEventListener('change', this._mobileQueryListener);
  }

  signOut() {
    this.spinnerService.showSpinner(true);
    this.authService.updateLastLogin().subscribe(
      data => {
        this.badgeValue = 0;
        this.authService.signOut();
        this.spinnerService.showSpinner(false);
        //this.router.navigate(['/login']);
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.router.navigate(['/login']);
        console.log("Errore logout : ", err);
      }
    );
  }


  goHome(){
    this.utilityService.goHome();
  }

  openMenu(){
    this.clickMenuButton.emit();
  }

  //scadenziario

  initScadenziario() {
    if (this.authService.getLoggedUser() != null && this.authService.getLoggedUser().userLogged != null) {
      this.scadenzarioService.numeroNotifiche(this.authService.getLoggedUser().userLogged.id).subscribe(
        (data: any) => {
          this.spinnerService.showSpinner(false);
          this.badgeValue = data;
        },
        err => {
          this.spinnerService.showSpinner(false);
          this.messageService.showErrorMessage("Ricerca numero notifiche", err);
        }
      );
    }
  }

  openScadenziarioDialog(){
    let dialogRef = this.dialogService.open(ScadenzarioComponent,  this.utilityService.configDynamicDialogFullScreen(this.dataSource, "Notifiche scadenzario"));
    dialogRef.onClose.subscribe( resp => {
      this.spinnerService.showSpinner(true);
      this.scadenzarioService.numeroNotifiche(this.authService.getLoggedUser().userLogged.id).subscribe(
      (data: any) => {
        this.spinnerService.showSpinner(false);
        this.badgeValue = data;
      },
      err => {
        this.spinnerService.showSpinner(false);
        this.messageService.showErrorMessage("Ricerca numero notifiche", err);
      });
    });
  }

  helpStatiPratica() {
    this.dialogService.open(HelpComponent,  this.utilityService.configDynamicDialogFullScreen(undefined, ""));
  }

  get badgeValueUpdated(): string {
    return this.badgeValue ? ( (this.badgeValue > 10) ? '10+' : '' + this.badgeValue) : '';
  }
/*   resetNotificheScadenziario() {
    this.dataSource = [];
    this.badgeValue = '';
    this.notificheControllate = false;
  } */
}

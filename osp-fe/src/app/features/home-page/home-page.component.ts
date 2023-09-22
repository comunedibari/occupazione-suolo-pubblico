import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { SpinnerDialogService } from '@core-components/layout/spinner-dialog/services/spinner-dialog.service';
import { MenuService } from '@services/menu.service';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css']
})
export class HomePageComponent implements OnInit {

  menuItems: MenuItem[] = [];
  thereAreUtilities: boolean = false;

  constructor(
    private spinnerService: SpinnerDialogService,
    private menuService: MenuService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.spinnerService.showSpinner(true);
    this.menuService.getAppMenu().then(res => {
      this.spinnerService.showSpinner(false);
      this.menuItems = res;
      this.thereAreUtilities = this.menuItems.filter(el => !el.items && el.visible).length > 0;
    });

  }

  navigate(path) {
    this.router.navigate([path]);
  }


}

import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { MenuService } from '@services/menu.service';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent implements OnInit {

  menuItems: MenuItem[] = [];
  @Output() clickItem: EventEmitter<any> = new EventEmitter<any>();

  constructor(
    private menuService: MenuService
  ) {}

  ngOnInit() {
    this.menuService.getAppMenu().then(res => {
      this.menuItems = res;
    });
  }

  clickMenuItem() {
    this.clickItem.emit();
  }

}

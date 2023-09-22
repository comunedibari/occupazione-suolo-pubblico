import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HomePageComponent } from './home-page.component';
import { TooltipModule } from 'primeng/tooltip';
import { BadgeModule } from 'primeng/badge';



@NgModule({
  declarations: [
    HomePageComponent
  ],
  imports: [
    BadgeModule,
    CommonModule,
    TooltipModule
  ],
  exports: [
    HomePageComponent
  ]
})
export class HomePageModule { }

import { Directive, Input, HostListener } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
  selector: '[minMax]'
})
export class MinMaxDirective {

  @Input()
  public min: number;

  @Input()
  public max: number;

  @Input()
  public maxLength: number;

  constructor(private ngControl: NgControl) { }

  ngOnInit() {
    const initialOnChange = (this.ngControl.valueAccessor as any).onChange;

    (this.ngControl.valueAccessor as any).onChange = value =>
      initialOnChange(this.processInput(value));
  }

  processInput(value: any) {
    if(value === null) {
      return null;
    }
    const val = parseInt(value);
    if(this.max !== null && this.max !== undefined  && val >= this.max) {
      return this.max.toString();
    }
    else if (this.min !== null && this.min !== undefined  && val <= this.min) {
      return this.min.toString();
    }
    if(this.maxLength !== null && this.maxLength !== undefined  && value.length > this.maxLength) {
      return value.slice(0, this.maxLength);
    }
    return value;
  }

  @HostListener("ngModelChange", ["$event"])
  ngModelChange(value: any) {
    this.ngControl.valueAccessor.writeValue(this.processInput(value));
  }

}
import { SortRequest } from "./sort-request";

export interface PageRequest {
    page: number;
    size: number;
    sort?: SortRequest[];
}

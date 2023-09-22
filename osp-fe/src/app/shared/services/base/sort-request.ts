export enum SortDirection {
    ASC,
    DESC
}

export interface SortRequest {
    field: string;
    direction: SortDirection;
    
}

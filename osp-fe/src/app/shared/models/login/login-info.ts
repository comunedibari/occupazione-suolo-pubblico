import { Groups } from "./groups";
import { User } from "./user";

export interface LoginInfo {
    idUser: number,
    auth: boolean,
    token: string,
    username: string,
    groups: Groups,
    userLogged: User,
}

package it.fincons.osp.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode { 
	
	E1("ID non presente nel sistema"), 
    E2("Modifica dati non consentita"),
	E3("Username utente non presente nel sistema"),
	E4("Recupero dati non consentito"),
	E5("Password non corrispondente"),
	E6("Utente eliminato"),
	E7("Inserimento dati non consentito"),
	E8("Errore invio email"),
	E9("Descrizione gruppo non presente nel sistema"),
	E10("Pratica in elaborazione con la stessa ubicazione"),
	E11("Gruppo utente non consentito"),
	E12("Errore nell'elaborazione del template"),
	E13("Stato della pratica non coerente"),
	E14("Errore protocollazione"),
	E15("Raggiunto numero massimo proroghe"),
	E16("Raggiunto numero massimo giorni proroga"),
	E17("Orario minimo occupazione"),
	E18("Coordinate poligono occupazione non valide"),
	E19("Sovrapposizione coordinate occupazione"),
	E20("Mime Type Template non valido"),
	E21("Codice protocollo non presente nel sistema"),
	E22("Errore conversione docx in pdf"),
	E23("Password troppo corta"),
	E24("Password troppo lunga"),
	E25("Credenziali non valide"),
	E26("L'utente è disabilitato"),
	E27("L'account è stato eliminato"),
	E28("Accesso negato"),
	E29("Tipo di file non consentito"),
	E30("Informazioni cittadino EGOV obbligatorie"),
	E31("Errore in fase di lettura template"),
	E32("Utente Concessionario già presente a sistema"),
	E33("Non sono presenti referenti del gruppo Polizia Locale per il "),
	E34("La data inizio occupazione proroga non corrisponde alla data scadenza occupazione concessione originaria incrementata di un giorno"),
	E35("L'orario inizio occupazione proroga non corrisponde all'orario scadenza occupazione concessione originaria incrementata di un minuto"),
	E36("Errore: esiste una pratica di proroga avviata"),
	E37("Errore: esiste una pratica di rinuncia avviata"),
	ERRORE_INTERNO("Errore interno");

    private String message;
}

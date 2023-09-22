package it.fincons.osp.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import io.micrometer.core.instrument.util.StringUtils;
import it.fincons.osp.model.Pratica;
import it.fincons.osp.model.RichiestaIntegrazione;

public final class EmailUtils {
	
	private EmailUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String getContentEmailResetPassword(String username, String password) {
		return "<div style='border-style: solid; border-width: 1px;'>"
			    +   "<div style='width: 100%; background-color: #E20A16; height:60px; padding:5px 5px;'>"
			    +     "<img style='float: left;' src='cid:logo_comune_bari' height='50' />"
			    +     "<span style='line-height:50px; color: white; font-weight: 500; font-size: 18px; display: inline-block;'>Gestionale Occupazione Suolo Pubblico - Recupero password</span>"
			    +   "</div> "
			    +   "<div style='padding: 10px'>"
			    +     "<span>Salve,</span>"	
			    +     "<br/><br/><span>la nuova password associata all\'utente <strong>" + username + "</strong> è la seguente:</span>"
			    +     "<br/><br/><span>Password: <strong>" + password + "</strong></span>"
			    +     "<br/><br/><span>Si prega di modificare la password al primo accesso.<span>"
			    +     "<br/><br/><span>Cordiali saluti,</span>"
			    +     "<br/><br/><span>COMUNE DI BARI</span>"
			    +   	"<div style='width: 100%; height:60px;'>"
			    +     		"<img src='cid:logo_footer_comune_bari' height='60' />"
			    +   	"</div>"
			    +   "</div>"
			    + "</div>";
	}
	/*function getContentEmail(messaggio, subject){
		 let content =
		   '<div style="border-style: solid; border-width: 1px;">'
		  +  '<div style="background-color: #E20A16; height:50px; padding:5px 5px;">'
		  // +   '<img style="float: left;" src="https://www.comune.bari.it/image/layout_set_logo?img_id=21701&t=1530879469139.0002" height="50" />'
		  +   '<img style="float: left;" src="cid:logo_comune_bari" height="50" />'
		  +   '<span style="line-height:50px; color: white; font-weight: 500; font-size: 1.2vw;">' + subject + '</span>'
		  +  '</div> '
		  +  '<div style="padding: 10px">'
		  +   '<span>' + messaggio + '</span>'
		  +   '<br/><br/><span>Si prega di non rispondere alla mail, perchè è stata generata automaticamente.<span>'
		  +   '<br/><br/><span>Cordiali saluti,</span>'
		  +   '<br/><span>Comune di Bari</span>'
		  +  '</div>';
		  + '</div>';

		 return content;

	}*/
	public static String getContentEmail(String messaggio, String subject){
		  return  "<div style='border-style: solid; border-width: 1px;'>"
				+    "<div style=\"background-color: #E20A16; height:50px; padding:5px 5px;\">"
				+      "<img style=\"float: left;\" src=\"cid:logo_comune_bari\" height=\"50\" />"
				+      "<span style=\"line-height:50px; color: white; font-weight: 500; font-size: 1.2vw;\">Concessione di Occupazione Suolo Pubblico - " + subject + "</span>"
				+    "</div> "
				+    "<div style='padding: 10px'>"
				+      "<span>" + messaggio + "</span>"
				+      "<br/><br/><span>Si prega di non rispondere a questa mail perché è stata generata automaticamente.<span>"
				+      "<br/><br/><span>Cordiali saluti,</span>"
				+      "<br/><br/><span>COMUNE DI BARI</span>"
				+   	"<div style='width: 100%; height:60px;'>"
				+     		"<img src='cid:logo_footer_comune_bari' height='60' />"
				+   	"</div>"
				+    "</div>"
				+ "</div>";
	}
	
	public static String getMessageCambioDateOccupazioneToAttori(Pratica pratica, String codiceProtocollo) {
		StringBuilder sb = new StringBuilder();
		sb.append("Agli uffici competenti,");
		sb.append("<br/><br/>con la presente email si comunica che sono state cambiate le date di occupazione della pratica di ");
		sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
		sb.append(" richiesta dal cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b> ");
		sb.append(" in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>."); 
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocollo).append("</b>.");
		return sb.toString();
	}
	
	public static String getMessageCambioDateOccupazioneToCittadino(Pratica pratica, String codiceProtocollo) {
		StringBuilder sb = new StringBuilder();
		sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che sono state cambiate le date di occupazione della pratica di ");
		sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
		sb.append(" da lei richiesta in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo da lei fornito: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>."); 
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocollo).append("</b>.");
		return sb.toString();
	}
	
	public static String getMessageInserimentoPraticaUfficiCompetenti(Pratica pratica, String codiceProtocollo) {
		StringBuilder sb = new StringBuilder();
		sb.append("Agli uffici competenti,");
		sb.append("<br/><br/>con la presente email si comunica che è stata avviata la pratica di ");
		sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
		sb.append(" richiesta dal cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b> ");
		sb.append(" in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>."); 
		sb.append("<br/><br/>Numero protocollo associato alla pratica: <b>").append(codiceProtocollo).append("</b>.");
		return sb.toString();
	}
	
	public static String getMessageInserimentoPraticaCittadino(Pratica pratica, String codiceProtocollo) {
		StringBuilder sb = new StringBuilder();
		sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che la pratica di ");
		sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
		sb.append(" di occupazione suolo pubblico da lei richiesta per l\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>");
		sb.append(" in data: <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>");
		sb.append(" è stata protocollata."); 
		sb.append("<br/>Il protocollo associato alla pratica è: <b>").append(codiceProtocollo).append("</b>.");
		sb.append("<br/><br/>Le comunichiamo che riceverà il nominativo dell’istruttore del Municipio incaricato delle verifiche quando la pratica sarà assegnata.");
		return sb.toString();
	}
	
	public static String getMessagePresaInCaricoPraticaToIstruttore(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
		StringBuilder sb = new StringBuilder();
		sb.append("All'Istruttore di Municipio,");
		sb.append("<br/><br/>con la presente email si comunica che è stato incaricato della pratica di ");
		sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
		sb.append(" di occupazione suolo pubblico richiesta dal cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b> ");
		sb.append(" in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>."); 
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");
		return sb.toString();
	}
	
	public static String getMessagePresaInCaricoPraticaToDirettore(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
		StringBuilder sb = new StringBuilder();
		sb.append("Al Direttore Municipio "+pratica.getMunicipio().getId()+", ");
		sb.append("<br/><br/>con la presente email si comunica che è stata assegnata la pratica di ");
		sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
		sb.append(" di occupazione suolo pubblico richiesta dal cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b> ");
		sb.append(" in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>."); 
		sb.append("<br/><br/>L\'istruttore municipio di riferimento per la pratica è <b>").append(pratica.getUtentePresaInCarico().getNome() + " " + pratica.getUtentePresaInCarico().getCognome()).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");
		return sb.toString();
	}
	
	public static String getMessagePresaInCaricoPraticaCittadino(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
		StringBuilder sb = new StringBuilder();
		sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che la pratica di ");
		sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
		sb.append(" di occupazione suolo pubblico da lei richiesta per l\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>");
		sb.append(" in data: <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>");
		sb.append(" con numero protocollo <b>").append(codiceProtocolloPratica).append("</b>");
		sb.append(" è stata assegnata all'istruttore del Municipio <b>").append(pratica.getUtentePresaInCarico().getNome() + " " + pratica.getUtentePresaInCarico().getCognome()).append("</b>.");
		
		return sb.toString();
	}
	
	public static String getMessageRichiestaPareriToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica la necessaria verifica per la richiesta di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" di occupazione suolo pubblico e della documentazione allegata dal cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b>.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageRichiestaVerificaRipristinoLuoghiToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica la necessaria verifica del ripristino dei luoghi per la richiesta di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" di occupazione suolo pubblico del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b>.");
	    /*sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");*/
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageRichiestaIntegrazioneCittadino(Pratica pratica, String codiceProtocolloPratica, RichiestaIntegrazione richiestaIntegrazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che la pratica di ").append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" da lei richiesta in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>");
	    sb.append(" con numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>");
		sb.append(" per l\'indirizzo da lei fornito: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>"); 
	    sb.append(" necessita di una <b>integrazione</b> relativamente alla documentazione allegata.");
		sb.append("<br/><br/>La pratica dovrà essere integrata entro il: <b>").append(richiestaIntegrazione.getDataScadenza().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>.");
	 	if(!pratica.getTipoProcesso().getId().equals(Constants.ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA)){
			sb.append("<br/>Se l'integrazione non avviene entro il quindicesimo giorno antecedente la data di inizio dell'occupazione richiesta, sarà necessario rideterminare le date di occupazione.");
		}
		sb.append("<br/><br/>Di seguito le note dell\'operatore che spiegano cosa integrare:<br/>").append(richiestaIntegrazione.getMotivoRichiesta());
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(richiestaIntegrazione.getCodiceProtocollo()).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");
		return sb.toString();
	}
	
	public static String getMessageRichiestaIntegrazioneToAttori(Pratica pratica, String codiceProtocolloPratica, RichiestaIntegrazione richiestaIntegrazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica l'avvio della richiesta di <b>integrazione</b> della documentazione al cittadino identificato con il codice fiscale <b>");
	    sb.append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b> per la richiesta di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" relativamente all\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>");
	    sb.append("<br/<br/>La pratica dovrà essere integrata entro il: <b>").append(richiestaIntegrazione.getDataScadenza().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>.");
		sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(richiestaIntegrazione.getCodiceProtocollo()).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}

	public static String getMessagePreavvisoDiniegoCittadino(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione, Integer numGiorniScadenzaPreavvisoDiniego, String note) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
	    sb.append("<br/><br/>con la presente notifica le comunichiamo che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" di occupazione suolo pubblico da lei richiesta per l\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>");
	    sb.append(" è in fase di rigetto e questa email rappresenta il preavviso di diniego di tale richiesta.");
	    sb.append("<br/><br/>Di seguito le note dell\'operatore che ne spiegano la causa.");
	    sb.append("<br/><br/>Note: ").append(note);
	    sb.append("<br/><br/>Nel caso voglia contestare tale disposizione, le comunichiamo che ha ").append(numGiorniScadenzaPreavvisoDiniego).append(" giorni a disposizione,");
	    sb.append(" oltre tale termine (<b>").append(pratica.getDataScadenzaPreavvisoDiniego().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>), la pratica sarà rigettata automaticamente e la richiesta sarà negata.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessagePreavvisoDiniegoToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica che è stato inviato il <b>preavviso di diniego</b> alla richiesta di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b>.");
	    sb.append("<br/<br/>Il cittadino dovrà rispondere entro la data: <b>").append(pratica.getDataScadenzaPreavvisoDiniego().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageParereToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione, String descrizioneGruppo, String note) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica che la verifica della richiesta di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b>");
	    sb.append("</b> per l\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b> è stata effettuata.");
	    sb.append("<br/><br/>Di seguito le note relative alla verifica effettuata da <b>").append(descrizioneGruppo).append("</b>:");
	    sb.append("<br/>").append(note).append("<br/>");
		if(codiceProtocolloOperazione!=null&&!"".equals(codiceProtocolloOperazione)){
			sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		}
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageInserimentoDeterminaToCittadino(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che alla pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" da lei richiesta in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo dell'occupazione suolo pubblico da lei fornito: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>"); 
	    sb.append(" è stata associata la determina visionabile sull'Albo Pretorio.");
		if (
			pratica.getDatiRichiesta() != null &&
			pratica.getDataScadenzaPagamento().isBefore(
				pratica.getDatiRichiesta().getDataInizioOccupazione().atStartOfDay()
			)
		) {
	    	sb.append("<br/><br/>Le chiediamo gentilmente di proseguire con il pagamento dei tributi per completare la richiesta. Il pagamento dovrà essere effettuato entro il <b>").append(pratica.getDataScadenzaPagamento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>.");
		} else {
	    	sb.append("<br/><br/>Le chiediamo gentilmente di proseguire con il pagamento dei tributi per completare la richiesta. Il pagamento dovrà essere effettuato entro il <b>").append(pratica.getDataScadenzaPagamento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> salvo eventuali diversi accordi assunti con l'Amministrazione.");
		}
	    sb.append("<br/><br/>Successivamente, potrà recarsi allo sportello del Municipio "+(pratica.getMunicipio()!=null?pratica.getMunicipio().getId():"di appartenenza")+" per il ritiro dell’atto concessorio.");
	    sb.append("<br/><br/>Per conoscere indirizzo e orari di ricevimento del Municipio "+(pratica.getMunicipio()!=null?pratica.getMunicipio().getId():"di appartenenza")+" è possibile consultare il seguente link: <a href=\"https://www.comune.bari.it/web/egov/-/occupazione-temporanea-suolo-pubblico\">https://www.comune.bari.it/web/egov/-/occupazione-temporanea-suolo-pubblico</a> .");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageInserimentoDeterminaToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione, String codiceDetermina, LocalDate dataEmissioneDetermina) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente si comunica che alla pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b> è stata associata la determina.");
	    sb.append("<br/><br/>Il numero della determina è: <b>").append(codiceDetermina).append("</b> rilasciata in data ").append(dataEmissioneDetermina.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>.");
		sb.append("<br/><br/>Il cittadino, quindi, è stato informato della necessità di pagare i tributi e la possibilità di <u>ritirare l’atto concessorio</u> presso lo sportello del Municipio "+(pratica.getMunicipio()!=null?pratica.getMunicipio().getId():"di appartenenza")+".");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageInserimentoDeterminaRinunciaToCittadino(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che alla pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" da lei richiesta in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo dell'occupazione suolo pubblico da lei fornito: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>"); 
	    sb.append(" è stata associata la determina di rinuncia visionabile sull'Albo Pretorio.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		if(codiceProtocolloPratica!=null&&!codiceProtocolloPratica.equals("")){
			sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");
		}
		return sb.toString();
	}
	
	public static String getMessageInserimentoDeterminaRinunciaToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione, String codiceDetermina, LocalDate dataEmissioneDetermina) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente si comunica che alla pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b> è stata associata la determina di rinuncia.");
	    sb.append("<br/><br/>Il numero della determina è: <b>").append(codiceDetermina).append("</b> rilasciata in data ").append(dataEmissioneDetermina.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageRitiroAttoConcessorioToCittadino(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" da lei richiesta in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo dell'occupazione suolo pubblico da lei fornito: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>"); 
	    sb.append(" è <b>conclusa</b>.");

		if(codiceProtocolloOperazione!=null&&!"".equals(codiceProtocolloOperazione)){
			sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		}

		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}

	public static String getMessageRitiroAttoConcessorioToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b>");
	    sb.append(" è <b>conclusa</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageReInvioPraticaToCittadino(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
		 StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" da lei richiesta in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo dell'occupazione suolo pubblico da lei fornito <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>"); 
	    sb.append(" è stata inoltrata nuovamente al Comune dopo l'integrazione della documentazione.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageReInvioPraticaToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica che il cittadino identificato con il codice fiscale <b>");
	    sb.append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b> ha allegato nuovamente i documenti alla pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" per l'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageRigettoToCittadino(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" da lei richiesta in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo dell'occupazione suolo pubblico da lei fornito: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>"); 
	    sb.append(" è stata <u><b>rigettata</b></u>.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageRigettoToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
		StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b>");
	    sb.append(" è stata <b>rigettata</b>.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageOrdinanza(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente notifica si trasmette copia dell'ordinanza emessa per la richiesta di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b>");
	    sb.append("</b> per l\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>.");
		if(codiceProtocolloOperazione!=null&&!"".equals(codiceProtocolloOperazione)){
			sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		}
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageRichiestaRettificaDateCittadino(Pratica pratica, String codiceProtocolloPratica, RichiestaIntegrazione richiestaIntegrazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" da lei richiesta in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo da lei fornito: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>"); 
	    sb.append(" necessita di una <b>rettifica</b> relativamente alle date di occupazione richieste.");
	    if (StringUtils.isNotBlank(richiestaIntegrazione.getMotivoRichiesta())) {
	    	 sb.append("<br/><br/>Nota dell'istruttore del municipio: <b>").append(richiestaIntegrazione.getMotivoRichiesta()).append("</b>.");
	    }
	    sb.append("<br/><br/>La pratica dovrà essere integrata entro il: <b>").append(richiestaIntegrazione.getDataScadenza().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(richiestaIntegrazione.getCodiceProtocollo()).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}

	public static String getMessageRichiestaRettificaDateAttori(Pratica pratica, String codiceProtocolloPratica, RichiestaIntegrazione richiestaIntegrazione) {
	    StringBuilder sb = new StringBuilder();
	     sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica l'avvio della richiesta di una <b>rettifica</b>, relativamente alle date di occupazione, al cittadino identificato con il codice fiscale <b>");
	    sb.append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b> per la richiesta di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" relativamente all\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>");
	    if (StringUtils.isNotBlank(richiestaIntegrazione.getMotivoRichiesta())) {
	    	 sb.append("<br/><br/>Nota dell'istruttore del municipio: <b>").append(richiestaIntegrazione.getMotivoRichiesta()).append("</b>.");
	    }
	    sb.append("<br/><br/>La pratica dovrà essere integrata entro il: <b>").append(richiestaIntegrazione.getDataScadenza().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(richiestaIntegrazione.getCodiceProtocollo()).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}

	public static String getMessageRettificaDateCittadino(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" da lei richiesta in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo da lei fornito: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>"); 
	    sb.append(" è stata <u><b>rettificata</b></u> relativamente alle date di occupazione richieste.");
		sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	public static String getMessageRettificaDateToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>");
	    sb.append(pratica.getFirmatario().getCodiceFiscalePartitaIva());
	    sb.append("</b> per l'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b> è stata <u><b>rettificata</b></u> relativamente alle date di occupazione richieste.");
		sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}

	public static String getMessageRettificaToCittadino(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" da lei richiesta in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo dell'occupazione suolo pubblico da lei fornito: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>"); 
	    sb.append(" ha subito una rettifica.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageRettificaToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione, String codiceDetermina, LocalDate dataEmissioneDetermina) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente si comunica che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b> è stata rettificata.");
	    sb.append("<br/><br/>Il numero della determina di rettifica è: <b>").append(codiceDetermina).append("</b> rilasciata in data ").append(dataEmissioneDetermina.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b>.");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageInserimentoDeterminaRdaToCittadino(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione, String notaAlCittadino, Integer idTipoProcesso) {
		String azione = idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_REVOCA_DELLA_CONCESSIONE) ? "è stata <u><b>revocata</b></u>"
				: idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_DECADENZA_DELLA_CONCESSIONE) ? "è <u><b>decaduta</b></u>"
						: idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_ANNULLAMENTO_DELLA_CONCESSIONE)
								? "è stata <u><b>annullata</b></u>"
								: null;
		
		String descProcesso = idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_REVOCA_DELLA_CONCESSIONE)
				? "La revoca è esaustiva"
				: idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_DECADENZA_DELLA_CONCESSIONE)
						? "La decadenza è esaustiva"
						: idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_ANNULLAMENTO_DELLA_CONCESSIONE)
								? "L'annullamento è esaustivo"
								: null;

	    StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che la concessione per l'indirizzo dell'occupazione suolo pubblico <b>");
	    sb.append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b> "); 
	    sb.append("<b>").append(azione).append("</b>; la determina relativa è visionabile sull'Albo Pretorio. ");
	    sb.append("La informiamo che ha a disposizione 5 giorni per effettuare i lavori a regola d'arte per il ripristino dei luoghi. ");
	    sb.append("<br/><br/>Di seguito le note dell'operatore che spiegano i motivi della revoca della concessione: <br/>");
	    sb.append(notaAlCittadino);
	    sb.append("<br/>").append(descProcesso).append(" dal ").append(pratica.getDataEmissioneDeterminaRda().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append(".");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessageInserimentoDeterminaRdaToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione, String codiceDetermina, Integer idTipoProcesso) {
		
		String azione = idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_REVOCA_DELLA_CONCESSIONE) ? "è stata <u><b>revocata</b></u>."
				: idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_DECADENZA_DELLA_CONCESSIONE) ? "è <u><b>decaduta</b></u>."
						: idTipoProcesso.equals(Constants.ID_TIPO_PROCESSO_ANNULLAMENTO_DELLA_CONCESSIONE)
								? "è stata <u><b>annullata</b></u>."
								: null;

	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente si comunica che la concessione del cittadino identificato con il codice fiscale <b>");
	    sb.append(pratica.getFirmatario().getCodiceFiscalePartitaIva());
	    sb.append("</b> per l'indirizzo dell'occupazione suolo pubblico <b>");
	    sb.append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b> "); 
	    sb.append("<b>").append(azione).append("</b>");
	    sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
	
	public static String getMessagePraticaDaRigettareToAttori(Pratica pratica, String codiceProtocolloPratica) {
		StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica che la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b>");
	    sb.append(" è passata nello stato <b>da rigettare</b>.");
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}

	public static String getNotificaRicevutePagateCittadino(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Gentile Sig./Sig.ra ").append(pratica.getFirmatario().getNome()).append(" ").append(pratica.getFirmatario().getCognome()).append(",");
		sb.append("<br/><br/>con la presente notifica le comunichiamo che le ricevute di pagamento necessarie per la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" da lei richiesta in data <b>").append(pratica.getDataInserimento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</b> per l\'indirizzo dell'occupazione suolo pubblico da lei fornito: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b>"); 
 		sb.append(" sono state associate.");
		if(codiceProtocolloOperazione!=null&&!"".equals(codiceProtocolloOperazione)){
			sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		}
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}

 	public static String getNotificaRicevutePagateToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione) {
		StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica che le ricevute di pagamento necessarie per la pratica di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b>");
		sb.append(" sono state associate.");
		if(codiceProtocolloOperazione!=null&&!"".equals(codiceProtocolloOperazione)){
			sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		}
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}

	public static String getMessageParereRipristinoToAttori(Pratica pratica, String codiceProtocolloPratica, String codiceProtocolloOperazione, String descrizioneGruppo, String note) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Agli uffici competenti,");
	    sb.append("<br/><br/>con la presente email si comunica che la verifica sul ripristino dei luoghi relativa alla richiesta di ");
	    sb.append(pratica.getTipoProcesso().getDescrizione().toLowerCase());
	    sb.append(" del cittadino identificato con il codice fiscale <b>").append(pratica.getFirmatario().getCodiceFiscalePartitaIva()).append("</b>");
	    sb.append("</b> per l\'indirizzo: <b>").append(pratica.getDatiRichiesta().getUbicazioneOccupazione()).append("</b> è stata effettuata.");
	    sb.append("<br/><br/>Di seguito le note relative alla verifica effettuata sul ripristino dei luoghi da <b>").append(descrizioneGruppo).append("</b>:");
	    sb.append("<br/>").append(note).append("<br/>");
		if(codiceProtocolloOperazione!=null&&!"".equals(codiceProtocolloOperazione)){
			sb.append("<br/><br/>Il numero di protocollo associato a questa operazione è: <b>").append(codiceProtocolloOperazione).append("</b>.");
		}
		sb.append("<br/><br/>Numero di protocollo associato alla pratica: <b>").append(codiceProtocolloPratica).append("</b>.");

		return sb.toString();
	}
}

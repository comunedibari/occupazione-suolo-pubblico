package it.fincons.osp.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Constants {

	private Constants() {
		throw new IllegalStateException("Constants class");
	}

	public static final Integer ID_GRUPPO_ADMIN = 1;
	public static final Integer ID_GRUPPO_OPERATORE_SPORTELLO = 2;
	public static final Integer ID_GRUPPO_DIRETTORE_MUNICIPIO = 3;
	public static final Integer ID_GRUPPO_ISTRUTTORE_MUNICIPIO = 4;
	public static final Integer ID_GRUPPO_POLIZIA_LOCALE = 5;
	public static final Integer ID_GRUPPO_RIP_URBANISTICA = 7;
	public static final Integer ID_GRUPPO_RIP_TRIBUTI = 11;
	public static final Integer ID_GRUPPO_RIP_RAGIONERIA = 12;
	public static final Integer ID_GRUPPO_CONCESSIONARIO = 13;
	public static final Integer ID_GRUPPO_IVOOPP_SET_URB_PRIMARIE = 14;
	public static final Integer ID_GRUPPO_IVOOPP_SET_GIARDINI = 15;
	public static final Integer ID_GRUPPO_IVOOPP_SET_INTERVENTI_SUL_TERRITORIO = 16;
	public static final Integer ID_GRUPPO_IVOOPP_SET_INFRASTRUTTURE_A_RETE = 17;
	public static final Integer ID_GRUPPO_RIP_PATRIMONIO = 18;
	public static final Integer ID_GRUPPO_POLIZIA_LOCALE_GENERALE = 19;
	public static final Integer ID_GRUPPO_EGOV = 20;
	public static final Integer ID_GRUPPO_DESTINATARI_ORDINANZE = 21;

	public static final Integer ID_STATO_PRATICA_BOZZA = 0;
	public static final Integer ID_STATO_PRATICA_INSERITA = 1;
	public static final Integer ID_STATO_PRATICA_VERIFICA_FORMALE = 2;
	public static final Integer ID_STATO_PRATICA_RICHIESTA_PARERI = 3;
	public static final Integer ID_STATO_PRATICA_NECESSARIA_INTEGRAZIONE = 4;
	public static final Integer ID_STATO_PRATICA_DA_RIGETTARE = 5;
	public static final Integer ID_STATO_PRATICA_APPROVATA = 6;
	public static final Integer ID_STATO_PRATICA_PREAVVISO_DINIEGO = 7;
	public static final Integer ID_STATO_PRATICA_ATTESA_DI_PAGAMENTO = 8;
	public static final Integer ID_STATO_PRATICA_PRONTO_AL_RILASCIO = 9;
	public static final Integer ID_STATO_PRATICA_CONCESSIONE_VALIDA = 10;
	public static final Integer ID_STATO_PRATICA_ARCHIVIATA = 18;
	public static final Integer ID_STATO_PRATICA_RIGETTATA = 20;
	public static final Integer ID_STATO_PRATICA_REVOCATA = 21;
	public static final Integer ID_STATO_PRATICA_DECADUTA = 22;
	public static final Integer ID_STATO_PRATICA_ANNULLATA = 23;
	public static final Integer ID_STATO_PRATICA_SOSPESA = 24;
	public static final Integer ID_STATO_PRATICA_TERMINATA = 25;
	public static final Integer ID_STATO_PRATICA_RINUNCIATA = 26;
	public static final Integer ID_STATO_PRATICA_RETTIFICA_DATE = 27;
	public static final Integer ID_STATO_PRATICA_INSERITA_MODIFICA_DATE = 28;

	public static final Integer ID_TIP_RUOLO_RICHIEDENTE_DESTINATARIO = 1;
	public static final Integer ID_TIP_RUOLO_RICHIEDENTE_LEGALE_RAPPR = 2;
	public static final Integer ID_TIP_RUOLO_RICHIEDENTE_DELEGATO = 3;

	public static final Integer ID_TIPO_ALLEGATO_ATTESTAZIONE_PAGAMENTO_MARCA_DA_BOLLO = 6;
	public static final Integer ID_TIPO_ALLEGATO_DOCUMENTO_DESTINATARIO = 8;
	public static final Integer ID_TIPO_ALLEGATO_ALTRO_DOCUMENTO = 12;
	public static final Integer ID_TIPO_ALLEGATO_RELAZIONE_SERVIZIO = 13;
	public static final Integer ID_TIPO_ALLEGATO_ISTRUTTORIA_TECNICA = 14;
	public static final Integer ID_TIPO_ALLEGATO_ORDINANZA = 15;
	public static final Integer ID_TIPO_ALLEGATO_BOLLO = 17;
	public static final Integer ID_TIPO_ALLEGATO_DETERMINA_DI_CONCESSIONE = 16;
	public static final Integer ID_TIPO_ALLEGATO_RICEVUTA_DI_PAGAMENTO_CUP = 18;
	public static final Integer ID_TIPO_ALLEGATO_DETERMINA_DI_RIGETTO = 19;
	public static final Integer ID_TIPO_ALLEGATO_ATTESTAZIONE_PAGAMENTO_CUP_CONCESSIONE = 23;
	public static final Integer ID_TIPO_ALLEGATO_DETERMINA_DI_RINUNCIA = 27;
	public static final Integer ID_TIPO_ALLEGATO_DETERMINA_DI_RETTIFICA = 28;
	public static final Integer ID_TIPO_ALLEGATO_DETERMINA_DI_REVOCA = 29;
	public static final Integer ID_TIPO_ALLEGATO_DETERMINA_DI_DECADENZA = 30;
	public static final Integer ID_TIPO_ALLEGATO_DETERMINA_DI_ANNULLAMENTO = 31;
	public static final Integer ID_TIPO_ALLEGATO_DETERMINA_DI_PROROGA = 32;
	public static final Integer ID_TIPO_ALLEGATO_DICHIARAZIONE_ESENZIONE_MARCA_BOLLO = 33;
	public static final Integer ID_TIPO_ALLEGATO_DOCUMENTAZIONE_INTEGRAZIONE_DINIEGO = 34;

	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_CONCESSIONE = 1;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_PROROGA = 2;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_RIGETTO = 3;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_RINUNCIA = 4;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_RETTIFICA = 5;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_REVOCA = 6;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_DECADENZA = 7;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_ANNULLAMENTO = 8;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_SOSPENSIONE = 9;

	public static final Integer ID_TIPO_TEMPLATE_ORDINANZA = 10;
	public static final Integer ID_TIPO_TEMPLATE_RELAZIONE_SERVIZIO = 11;
	public static final Integer ID_TIPO_TTEMPLATE_ISTRUTTORIA_IVOOPP_URBANIZZAZIONI_PRIMARIE = 12;
	public static final Integer ID_TIPO_TTEMPLATE_ISTRUTTORIA_IVOOPP_URBANIZZAZIONI_GIARDINI = 13;
	public static final Integer ID_TIPO_TTEMPLATE_ISTRUTTORIA_IVOOPP_INTERVENTI_TERRITORIO = 14;
	public static final Integer ID_TIPO_TTEMPLATE_ISTRUTTORIA_IVOOPP_INFRASTRUTTURE_RETE = 15;
	public static final Integer TID_TIPO_TEMPLATE_ISTRUTTORIA_URBANISTICA = 16;
	public static final Integer ID_TIPO_TTEMPLATE_ISTRUTTORIA_PATRIMONIO = 17;

	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_PROROGA_ESENTE_BOLLO = 18;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_PROROGA_ESENTE_BOLLO_CUP = 19;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_PROROGA_ESENTE_CUP = 20;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_CONCESSIONE_ESENTE_BOLLO = 21;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_CONCESSIONE_ESENTE_BOLLO_CUP = 22;
	public static final Integer ID_TIPO_TEMPLATE_DETERMINA_CONCESSIOONE_ESENTE_CUP = 23;

	public static final String TEMPLATE_NAME_PROTOCOLLO_PRATICA = "TemplateProtocolloPraticaOSP.docx";
	public static final String TEMPLATE_NAME_INSERIMENTO_PRATICA = "TemplateInserimentoPraticaOSP.docx";
	
	public static final Integer ID_TIPO_NOTIFICA_SCADENZARIO_CONCESSIONE_SCADUTA = 1;
	public static final Integer ID_TIPO_NOTIFICA_SCADENZARIO_CONCESSIONE_TEMPORANEA_IN_SCADENZA = 2;
	public static final Integer ID_TIPO_NOTIFICA_SCADENZARIO_TEMPI_PROCEDIMENTALI_SCADUTI = 3;
	public static final Integer ID_TIPO_NOTIFICA_SCADENZARIO_TEMPI_PROCEDIMENTALI_IN_SCADENZA = 4;
	public static final Integer ID_TIPO_NOTIFICA_SCADENZARIO_SCADENZA_TEMPISTICHE_INTEGRAZIONE = 5;
	public static final Integer ID_TIPO_NOTIFICA_SCADENZARIO_SCADENZA_TEMPISTICHE_DINIEGO = 6;
	public static final Integer ID_TIPO_NOTIFICA_SCADENZARIO_SCADENZA_TEMPISTICHE_PAGAMENTO = 7;
	public static final Integer ID_TIPO_NOTIFICA_SCADENZARIO_SCADENZA_RETTIFICA_DATE = 8;

	public static final String TIPO_RICHIESTA_INTEGRAZIONE_PROCEDURA_DINIEGO = "Procedura di diniego";
	
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_CITTADINO = "Richiesta cittadino";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_PRESA_IN_CARICO = "Presa in carico";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA = "Determina";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_RIGETTO = "Determina rigetto";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_RINUNCIA = "Determina rinuncia";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_RETTIFICA = "Determina rettifica";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_REVOCA = "Determina revoca";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_DECADENZA = "Determina decadenza";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_DETERMINA_ANNULLAMENTO = "Determina annullamento";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_CONCESSIONE_VALIDA = "Concessione valida";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_PARERE = "Richiesta parere";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_PARERE = "Parere";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_INTEGRAZIONE = "Richiesta integrazione";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_INTEGRAZIONE = "Integrazione";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_PREAVVISO_DINIEGO = "Preavviso diniego";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_VERIFICA_RIPRISTINO_LUOGHI = "Richiesta verifica del ripristino dei luoghi";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_RICHIESTA_RETTIFICA_DATE = "Richiesta rettifica date";
	public static final String TIPO_EVENTO_PROTOCOLLAZIONE_RETTIFICA_DATE = "Rettifica date";
	
	public static final String MIME_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	public static final String MIME_TYPE_PDF = "application/pdf";
	public static final String MIME_TYPE_DOC = "application/msword";
	public static final String MIME_TYPE_JPG = "image/jpeg";
	public static final String MIME_TYPE_PNG = "image/png";

	//zip
	public static final String MIME_TYPE_ZIP = "application/zip";
	public static final String MIME_TYPE_GZIP = "application/gzip";
	public static final String MIME_TYPE_X_ZIP_COMPRESSED = "application/x-zip-compressed";
	public static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";
	public static final String MIME_TYPE_VND_RAR = "application/vnd.rar";
	public static final String MIME_TYPE_X_RAR_COMPRESSED = "application/x-rar-compressed";
	//.p7m
	public static final String MIME_TYPE_PKCS7_MIME = "application/pkcs7-mime";
	public static final String MIME_TYPE_X_PKCS7_MIME = "application/x-pkcs7-mime";
	public static final String MIME_TYPE_X_PKCS7_CERTREQRESP = "application/x-pkcs7-certreqresp";
	public static final String MIME_TYPE_X_PKCS7_CERTIFICATES = "application/x-pkcs7-certificates";
	public static final String MIME_TYPE_PKCS7 = "application/pkcs7";

	public static final List<String> MIME_TYPE_LIST = new ArrayList<>();

	static{
		MIME_TYPE_LIST.add(MIME_TYPE_DOCX);
		MIME_TYPE_LIST.add(MIME_TYPE_PDF);
		MIME_TYPE_LIST.add(MIME_TYPE_DOC);
		MIME_TYPE_LIST.add(MIME_TYPE_JPG);
		MIME_TYPE_LIST.add(MIME_TYPE_PNG);
		MIME_TYPE_LIST.add(MIME_TYPE_ZIP);
		MIME_TYPE_LIST.add(MIME_TYPE_GZIP);
		MIME_TYPE_LIST.add(MIME_TYPE_X_ZIP_COMPRESSED);
		MIME_TYPE_LIST.add(MIME_TYPE_OCTET_STREAM);
		MIME_TYPE_LIST.add(MIME_TYPE_VND_RAR);
		MIME_TYPE_LIST.add(MIME_TYPE_X_RAR_COMPRESSED);
		MIME_TYPE_LIST.add(MIME_TYPE_PKCS7_MIME);
		MIME_TYPE_LIST.add(MIME_TYPE_X_PKCS7_MIME);
		MIME_TYPE_LIST.add(MIME_TYPE_X_PKCS7_CERTREQRESP);
		MIME_TYPE_LIST.add(MIME_TYPE_X_PKCS7_CERTIFICATES);
		MIME_TYPE_LIST.add(MIME_TYPE_PKCS7);
	}

	public static final Integer PASSWORD_LENGTH_MIN = 6;
	public static final Integer PASSWORD_LENGTH_MAX = 120;

	public static final Integer ID_TIPO_ATTIVITA_EDILIZIA = 1;
	
	public static final Integer ID_TIPO_PROCESSO_CONCESSIONE_TEMPORANEA = 1;
	public static final Integer ID_TIPO_PROCESSO_PROROGA_CONCESSIONE_TEMPORANEA = 2;
	public static final Integer ID_TIPO_PROCESSO_RINUNCIA_CONCESSIONE_TEMPORANEA = 3;
	public static final Integer ID_TIPO_PROCESSO_RETTIFICA_PER_CORREZIONE_ERRORI_MATERIALI = 4;
	public static final Integer ID_TIPO_PROCESSO_REVOCA_DELLA_CONCESSIONE = 5;
	public static final Integer ID_TIPO_PROCESSO_DECADENZA_DELLA_CONCESSIONE = 6;
	public static final Integer ID_TIPO_PROCESSO_ANNULLAMENTO_DELLA_CONCESSIONE = 7;
	
	public static final String NOTA_RICHIESTA_PARERE_VERIFICA_RIPRISTINO_LUOGHI = "Necessaria verifica del ripristino dei luoghi";
}

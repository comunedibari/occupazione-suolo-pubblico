create sequence hibernate_sequence start 1 increment 1;
create sequence seq_allegato start 1 increment 1;
create sequence seq_com_email start 1 increment 1;
create sequence seq_dati_richiesta start 1 increment 1;
create sequence seq_integrazione start 1 increment 1;
create sequence seq_municipio start 1 increment 1;
create sequence seq_notifica_scadenzario start 1 increment 1;
create sequence seq_parere start 1 increment 1;
create sequence seq_pratica start 1 increment 1;
create sequence seq_protocollo start 1 increment 1;
create sequence seq_rich_integrazione start 1 increment 1;
create sequence seq_rich_parere start 1 increment 1;
create sequence seq_richiedente start 1 increment 1;
create sequence seq_template start 1 increment 1;
create sequence seq_utente start 1 increment 1;

--nuova
create sequence SEQ_MARCA_BOLLO_PRATICA start 1 increment 1;
--nuova
create sequence SEQ_MARCA_BOLLO_DETERMINA start 1 increment 1;
--nuova
create sequence SEQ_AUTOMI_PROTOCOLLO start 1 increment 1;

--nuova
create table t_automi_protocollo (
   id int4 not null,
   uo_id varchar(100) not null,
   id_municipio int4,
    denominazione varchar(100) not null,
    label varchar(100),
    data_inserimento timestamp not null,
    data_modifica timestamp,
    primary key (id)
);
--nuova
create table t_automi_protocollo_aud (
   id int4 not null,
   rev int4 not null,
   revtype int2,
   uo_id varchar(100) not null,
   id_municipio int4,
    denominazione varchar(100) not null,
    label varchar(100),
    data_inserimento timestamp not null,
    data_modifica timestamp,
    primary key (id,rev)
);

    create table a_tipo_allegato_grup_stat_proc (
       id int4 not null,
        flg_obbligatorio boolean default false not null,
        flg_testo_libero boolean default false not null,
        id_gruppo int4 not null,
        id_stato_pratica int4 not null,
        id_tipo_allegato int4 not null,
        id_tipo_processo int4 not null,
        primary key (id)
    );

    create table a_utente_municipio (
       id_utente int8 not null,
        id_municipio int4 not null,
        primary key (id_utente, id_municipio)
    );

    create table revinfo (
       rev int4 not null,
        revtstmp int8,
        primary key (rev)
    );

    create table t_allegato (
       id int8 not null,
        codice_protocollo varchar(100),
        data_inserimento timestamp not null,
        file_allegato bytea,
        id_richiesta_integrazione int8,
        id_richiesta_parere int8,
        mime_type varchar(255),
        nome_file varchar(255),
        nota varchar(255),
        revisione int4 check (revisione>=1),
        id_integrazione int8,
        id_parere int8,
        id_pratica int8,
        id_tipo_allegato int4,
        primary key (id)
    );

    create table t_comunicazione_mail (
       id int8 not null,
        date_inserimento timestamp not null,
        date_invio timestamp,
        destinatari varchar(500) not null,
        destinatari_cc varchar(500),
        file_allegato bytea,
        flg_inviata boolean default false not null,
        flg_pec boolean default false not null,
        mime_type_file_allegato varchar(255),
        nome_file_allegato varchar(255),
        numero_tentativi_invio int4,
        oggetto varchar(998) not null,
        testo varchar(2000),
        id_pratica int8,
        id_rich_integrazione int8,
        id_rich_parere int8,
        primary key (id)
    );

    create table t_dati_richiesta (
       id int8 not null,
        cod_via varchar(20),
        coord_ubicazione_definitiva GEOMETRY,
        coord_ubicazione_temporanea GEOMETRY,
        data_inizio_occupazione date not null,
        data_scadenza_occupazione date not null,
        descrizione_att_da_svolgere varchar(255),
        descrizione_manufatto varchar(255),
        descrizione_titolo_edilizio varchar(100),
        flg_accett_reg_suolo_pubblico boolean default false not null,
        flg_conosc_tassa_occupazione boolean default false not null,
        flg_non_modifiche_rispetto_concessione boolean default false not null,
        flg_num_civico_assente boolean default false not null,
        flg_obbligo_rip_danni boolean default false not null,
        flg_risp_disp_regolamento boolean default false not null,
        flg_risp_interesse_terzi boolean default false not null,
        id_municipio int4,
        larghezza_carr_m float8,
        larghezza_m float8 not null,
        larghezza_marc_m float8,
        localita varchar(50),
        lunghezza_carr_m float8,
        lunghezza_m float8 not null,
        lunghezza_marc_m float8,
        nome_via varchar(255),
        note_ubicazione text,
        numero_via varchar(5),
        ora_inizio_occupazione time,
        ora_scadenza_occupazione time,
        pres_pass_carr_div_abili boolean,
        pres_scivoli_div_abili boolean,
        riferimento_titolo_edilizio varchar(100),
        stallo_di_sosta boolean,
        superficie_area_mq float8 not null,
        superficie_marc_mq float8,
        tp_op_verifica_occupazione varchar(255),
        ubicazione_occupazione varchar(300) not null,
        id_tipo_attivita_da_svolgere int4,
        id_tipo_manufatto int4,
        id_tipologia_titolo_edilizio int4,
        primary key (id)
    );

    create table t_dati_richiesta_aud (
       id int8 not null,
        rev int4 not null,
        revtype int2,
        cod_via varchar(255),
        coord_ubicazione_definitiva GEOMETRY,
        coord_ubicazione_temporanea GEOMETRY,
        data_inizio_occupazione date,
        data_scadenza_occupazione date,
        descrizione_att_da_svolgere varchar(255),
        descrizione_manufatto varchar(255),
        descrizione_titolo_edilizio varchar(255),
        flg_accett_reg_suolo_pubblico boolean default false,
        flg_conosc_tassa_occupazione boolean default false,
        flg_non_modifiche_rispetto_concessione boolean default false,
        flg_num_civico_assente boolean default false,
        flg_obbligo_rip_danni boolean default false,
        flg_risp_disp_regolamento boolean default false,
        flg_risp_interesse_terzi boolean default false,
        id_municipio int4,
        larghezza_carr_m float8,
        larghezza_m float8,
        larghezza_marc_m float8,
        localita varchar(255),
        lunghezza_carr_m float8,
        lunghezza_m float8,
        lunghezza_marc_m float8,
        nome_via varchar(255),
        note_ubicazione text,
        numero_via varchar(255),
        ora_inizio_occupazione time,
        ora_scadenza_occupazione time,
        pres_pass_carr_div_abili boolean,
        pres_scivoli_div_abili boolean,
        riferimento_titolo_edilizio varchar(255),
        stallo_di_sosta boolean,
        superficie_area_mq float8,
        superficie_marc_mq float8,
        tp_op_verifica_occupazione varchar(255),
        ubicazione_occupazione varchar(255),
        id_tipo_attivita_da_svolgere int4,
        id_tipo_manufatto int4,
        id_tipologia_titolo_edilizio int4,
        primary key (id, rev)
    );

    create table t_gruppo (
       id int4 not null,
        descrizione varchar(50) not null,
        flg_attesa_pagamento boolean default false not null,
        flg_concessioni_valide boolean default false not null,
        flg_fascicolo boolean default false not null,
        flg_gest_invio_mail_determina boolean default false not null,
        flg_gest_richiesta_parere boolean default false not null,
        flg_gestione_profilo boolean default false not null,
        flg_gestione_utenti boolean default false not null,
        flg_inserisci_richiesta boolean default false not null,
        flg_necessaria_integrazione boolean default false not null,
        flg_pratica_inserita boolean default false not null,
        flg_pratiche_approvate boolean default false not null,
        flg_pratiche_archiviate boolean default false not null,
        flg_pratiche_da_rigettare boolean default false not null,
        flg_pratiche_preavviso_diniego boolean default false not null,
        flg_pratiche_rigettate boolean default false not null,
        flg_pronte_rilascio boolean default false not null,
        flg_richiesta_pareri boolean default false not null,
        flg_verifica_pratiche boolean default false not null,
        flg_verifica_ripristino_luoghi boolean default false not null,
        primary key (id)
    );

    create table t_integrazione (
       id int8 not null,
        cf_cittadino_egov varchar(16),
        codice_protocollo varchar(255),
        cognome_cittadino_egov varchar(50),
        data_inizio_occupazione date,
        data_inserimento timestamp not null,
        data_protocollo timestamp,
        data_scadenza_occupazione date,
        motivo_integrazione varchar(512),
        nome_cittadino_egov varchar(50),
        ora_inizio_occupazione time,
        ora_scadenza_occupazione time,
        id_richiesta_integrazione int8 not null,
        id_utente_integrazione int8 not null,
        primary key (id)
    );

    create table t_integrazione_aud (
       id int8 not null,
        rev int4 not null,
        revtype int2,
        cf_cittadino_egov varchar(16),
        codice_protocollo varchar(255),
        cognome_cittadino_egov varchar(255),
        data_inizio_occupazione date,
        data_inserimento timestamp,
        data_protocollo timestamp,
        data_scadenza_occupazione date,
        motivo_integrazione varchar(255),
        nome_cittadino_egov varchar(255),
        ora_inizio_occupazione time,
        ora_scadenza_occupazione time,
        id_richiesta_integrazione int8,
        id_utente_integrazione int8,
        primary key (id, rev)
    );

    create table t_municipio (
       id int4 not null,
        descrizione varchar(50) not null,
        primary key (id)
    );

    create table t_notifica_scadenzario (
       id int8 not null,
        data_notifica timestamp,
        id_pratica int8 not null,
        id_tipo_notifica_scadenzario int4 not null,
        id_utente int8 not null,
        primary key (id)
    );

    create table t_parere (
       id int8 not null,
        codice_protocollo varchar(255),
        data_inserimento timestamp not null,
        data_protocollo timestamp,
        flg_esito boolean,
        flg_competenza boolean,
        nota varchar(255),
        id_richiesta_parere int8 not null,
        id_utente_parere int8 not null,
        primary key (id)
    );

    create table t_parere_aud (
       id int8 not null,
        rev int4 not null,
        revtype int2,
        codice_protocollo varchar(255),
        data_inserimento timestamp,
        data_protocollo timestamp,
        flg_esito boolean,
        flg_competenza boolean,
        nota varchar(255),
        id_richiesta_parere int8,
        id_utente_parere int8,
        primary key (id, rev)
    );

    create table t_pratica (
       id int8 not null,
        cf_cittadino_egov varchar(16),
        codice_determina varchar(255),
        codice_determina_rda varchar(255),
        codice_determina_rinuncia varchar(255),
        cognome_cittadino_egov varchar(50),
        contatore_richieste_integ int4,
        data_creazione timestamp not null,
        data_emissione_determina date,
        data_emissione_determina_rda date,
        data_emissione_determina_rin date,
        data_inserimento timestamp,
        data_modifica timestamp,
        data_scadenza_pagamento timestamp,
        data_scadenza_pratica timestamp,
        data_scadenza_preav_diniego timestamp,
        data_scadenza_rigetto timestamp,
        flg_procedura_diniego boolean,
        flg_verifica_formale boolean,
        id_pratica_originaria int8,
        id_proroga_precedente int8,
        motivazione_richiesta varchar(512),
        nome_cittadino_egov varchar(50),
        nota_al_cittadino_rda varchar(512),
        id_dati_richiesta int8 not null,
        id_richiedente_destinatario int8,
        id_richiedente_firmatario int8 not null,
        id_municipio int4,
        id_stato_pratica int4 not null,
        id_tipo_processo int4 not null,
        id_utente_assegnatario int8,
        id_utente_creazione int8 not null,
        id_utente_modifica int8,
        id_utente_presa_in_carico int8,
        primary key (id)
    );

    create table t_pratica_aud (
       id int8 not null,
        rev int4 not null,
        revtype int2,
        cf_cittadino_egov varchar(16),
        codice_determina varchar(255),
        codice_determina_rda varchar(255),
        codice_determina_rinuncia varchar(255),
        cognome_cittadino_egov varchar(255),
        contatore_richieste_integ int4,
        data_creazione timestamp,
        data_emissione_determina date,
        data_emissione_determina_rda date,
        data_emissione_determina_rin date,
        data_inserimento timestamp,
        data_modifica timestamp,
        data_scadenza_pagamento timestamp,
        data_scadenza_pratica timestamp,
        data_scadenza_preav_diniego timestamp,
        data_scadenza_rigetto timestamp,
        flg_procedura_diniego boolean,
        flg_verifica_formale boolean,
        id_pratica_originaria int8,
        id_proroga_precedente int8,
        motivazione_richiesta varchar(255),
        nome_cittadino_egov varchar(255),
        nota_al_cittadino_rda varchar(255),
        id_dati_richiesta int8,
        id_richiedente_destinatario int8,
        id_richiedente_firmatario int8,
        id_municipio int4,
        id_stato_pratica int4,
        id_tipo_processo int4,
        id_utente_assegnatario int8,
        id_utente_creazione int8,
        id_utente_modifica int8,
        id_utente_presa_in_carico int8,
        primary key (id, rev)
    );

    create table t_protocollo (
       id int8 not null,
        codice_determina_rettifica varchar(255),
        codice_protocollo varchar(100),
        data_emissione_determina_ret date,
        data_protocollo timestamp,
        tipo_operazione varchar(255),
        id_pratica int8 not null,
        id_stato_pratica int4 not null,
        primary key (id)
    );

    create table t_protocollo_aud (
       id int8 not null,
        rev int4 not null,
        revtype int2,
        codice_determina_rettifica varchar(255),
        codice_protocollo varchar(255),
        data_emissione_determina_ret date,
        data_protocollo timestamp,
        tipo_operazione varchar(255),
        id_pratica int8,
        id_stato_pratica int4,
        primary key (id, rev)
    );

    create table t_richiedente (
       id int8 not null,
        amm_doc_allegato varchar(50),
        cap varchar(5),
        citta varchar(50),
        civico varchar(10),
        codice_fiscale_p_iva varchar(16),
        cognome varchar(50),
        comune_di_nascita varchar(50),
        data_di_nascita date,
        denominazione varchar(50),
        email varchar(50),
        flg_destinatario boolean not null,
        flg_firmatario boolean not null,
        indirizzo varchar(100),
        nazionalita varchar(50),
        nome varchar(50),
        numero_doc_allegato varchar(50),
        provincia varchar(50),
        provincia_di_nascita varchar(50),
        recapito_telefonico varchar(15),
        id_tipo_documento_allegato int4,
        id_tipo_ruolo_richiedente int4 not null,
        primary key (id)
    );

    create table t_richiesta_integrazione (
       id int8 not null,
        codice_protocollo varchar(100),
        data_inserimento timestamp not null,
        data_protocollo timestamp,
        data_scadenza timestamp not null,
        flg_inserita_risposta boolean default false not null,
        motivo_richiesta varchar(512),
        tipo_richiesta varchar(255),
        id_pratica int8 not null,
        id_stato_pratica int4 not null,
        id_utente_richiedente int8 not null,
        primary key (id)
    );

    create table t_richiesta_integrazione_aud (
       id int8 not null,
        rev int4 not null,
        revtype int2,
        codice_protocollo varchar(255),
        data_inserimento timestamp,
        data_protocollo timestamp,
        data_scadenza timestamp,
        flg_inserita_risposta boolean default false,
        motivo_richiesta varchar(255),
        tipo_richiesta varchar(255),
        id_pratica int8,
        id_stato_pratica int4,
        id_utente_richiedente int8,
        primary key (id, rev)
    );

    create table t_richiesta_parere (
       id int8 not null,
        codice_protocollo varchar(100),
        data_inserimento timestamp not null,
        data_protocollo timestamp,
        flg_inserita_risposta boolean default false not null,
        nota_richiesta_parere varchar(512),
        id_gruppo_dest_parere int4 not null,
        id_pratica int8 not null,
        id_stato_pratica int4 not null,
        id_utente_richiedente int8 not null,
        primary key (id)
    );

    create table t_richiesta_parere_aud (
       id int8 not null,
        rev int4 not null,
        revtype int2,
        codice_protocollo varchar(255),
        data_inserimento timestamp,
        data_protocollo timestamp,
        flg_inserita_risposta boolean default false,
        nota_richiesta_parere varchar(255),
        id_gruppo_dest_parere int4,
        id_pratica int8,
        id_stato_pratica int4,
        id_utente_richiedente int8,
        primary key (id, rev)
    );

    create table t_template (
       id int4 not null,
        data_inserimento timestamp not null,
        data_modifica timestamp,
        file_template bytea not null,
        mime_type varchar(255) not null,
        nome_file varchar(255) not null,
        id_tipo_template int4 not null,
        id_utente_modifica int8,
        primary key (id)
    );

    create table t_utente (
       id int8 not null,
        codice_fiscale varchar(16) not null,
        cognome varchar(50),
        data_di_nascita date,
        data_eliminazione timestamp,
        date_created timestamp not null,
        email varchar(50),
        enabled boolean default true not null,
        flg_eliminato boolean default false not null,
        indirizzo varchar(255),
        last_login timestamp,
        luogo_di_nascita varchar(50),
        nome varchar(50),
        num_tel varchar(15),
        password_ varchar(120) not null,
        provincia_di_nascita varchar(50),
        ragione_sociale varchar(100),
        sesso varchar(1),
        uo_id varchar(255),
        username varchar(30) not null,
        id_gruppo int4 not null,
        primary key (id)
    );

    create table tp_stato_pratica (
       id int4 not null,
        descrizione varchar(50) not null,
        primary key (id)
    );

    create table tp_tipo_allegato (
       id int4 not null,
        descrizione varchar(100) not null,
        descrizione_estesa varchar(255),
        primary key (id)
    );

    create table tp_tipo_attivita_da_svolgere (
       id int4 not null,
        descrizione varchar(50) not null,
        flg_testo_libero boolean default false not null,
        primary key (id)
    );

    create table tp_tipo_doc_allegato (
       id int4 not null,
        descrizione varchar(50) not null,
        primary key (id)
    );

    create table tp_tipo_manufatto (
       id int4 not null,
        descrizione varchar(50) not null,
        flg_testo_libero boolean default false not null,
        primary key (id)
    );

    create table tp_tipo_notifica_scadenzario (
       id int4 not null,
        descrizione varchar(50) not null,
        primary key (id)
    );

    create table tp_tipo_processo (
       id int4 not null,
        descrizione varchar(50) not null,
        primary key (id)
    );

    create table tp_tipo_ruolo_richiedente (
       id int4 not null,
        descrizione varchar(50) not null,
        primary key (id)
    );

    create table tp_tipo_template (
       id int4 not null,
        descrizione varchar(100) not null,
        primary key (id)
    );

    create table tp_tipologia_titolo_edilizio (
       id int4 not null,
        descrizione varchar(50) not null,
        flg_testo_libero boolean default false not null,
        primary key (id)
    );

--nuova
	create table T_MARCA_BOLLO_PRATICA (
       id int8 not null,
        IUV varchar(255),
        IMPRONTA_FILE varchar(255),
        IMPORTO_PAGATO numeric,
        CAUSALE_PAGAMENTO varchar(255),
        ID_RICHIESTA varchar(255),
        DATA_OPERAZIONE date,
        primary key (id)
    );
--nuova
	create table T_MARCA_BOLLO_DETERMINA (
       id int8 not null,
        IUV varchar(255),
        IMPRONTA_FILE varchar(255),
        IMPORTO_PAGATO numeric,
        CAUSALE_PAGAMENTO varchar(255),
        ID_RICHIESTA varchar(255),
        DATA_OPERAZIONE date,
        primary key (id)
    );

    alter table a_tipo_allegato_grup_stat_proc 
       add constraint UQ_A_TIPO_ALLEGATO unique (id_tipo_allegato, id_stato_pratica, id_tipo_processo, id_gruppo);

    alter table t_gruppo 
       add constraint UQ_GRUPPO_DESCRIZIONE unique (descrizione);

    alter table t_municipio 
       add constraint UQ_MUNICIPIO_DESCRIZIONE unique (descrizione);

    alter table t_pratica 
       add constraint UQ_PRATICA_DATI_RICHIESTA unique (id_dati_richiesta);

    alter table t_template 
       add constraint UQ_TEMPLATE_TIPO unique (id_tipo_template);

    alter table tp_stato_pratica 
       add constraint UQ_STATO_PRATICA_DESCRIZIONE unique (descrizione);

    alter table tp_tipo_allegato 
       add constraint UQ_TIPO_ALLEGATO unique (descrizione);

    alter table tp_tipo_attivita_da_svolgere 
       add constraint UQ_TIPO_ATT_DA_SVOL_DESC unique (descrizione);

    alter table tp_tipo_doc_allegato 
       add constraint UQ_TIPO_DOC_ALLEGATO_DESC unique (descrizione);

    alter table tp_tipo_manufatto 
       add constraint UQ_TIPO_MANUFATTO_DESC unique (descrizione);

    alter table tp_tipo_notifica_scadenzario 
       add constraint UQ_TIPO_NOTIFICA_SCAD_DESC unique (descrizione);

    alter table tp_tipo_processo 
       add constraint UQ_TIPO_PROCESSO_DESC unique (descrizione);

    alter table tp_tipo_ruolo_richiedente 
       add constraint UQ_TIPO_RUOLO_RICH_DESCRIZIONE unique (descrizione);

    alter table tp_tipo_template 
       add constraint UQ_TIPO_TEMPLATE_DESCRIZIONE unique (descrizione);

    alter table tp_tipologia_titolo_edilizio 
       add constraint UQ_TIP_TITOLO_ED_DESC unique (descrizione);

    alter table a_tipo_allegato_grup_stat_proc 
       add constraint FK_A_TIPO_ALL_GRUPPO 
       foreign key (id_gruppo) 
       references t_gruppo;

    alter table a_tipo_allegato_grup_stat_proc 
       add constraint FK_A_TIPO_ALL_STATO_PRATICA 
       foreign key (id_stato_pratica) 
       references tp_stato_pratica;

    alter table a_tipo_allegato_grup_stat_proc 
       add constraint FK_A_TIPO_ALL 
       foreign key (id_tipo_allegato) 
       references tp_tipo_allegato;

    alter table a_tipo_allegato_grup_stat_proc 
       add constraint FK_A_TIPO_ALL_TIPO_PROCESSO 
       foreign key (id_tipo_processo) 
       references tp_tipo_processo;

    alter table a_utente_municipio 
       add constraint FK_A_UTENTE_MUNICIPIO_MUN 
       foreign key (id_municipio) 
       references t_municipio;

    alter table a_utente_municipio 
       add constraint FK_A_UTENTE_MUNICIPIO_UT 
       foreign key (id_utente) 
       references t_utente;

    alter table t_allegato 
       add constraint FK_ALLEGATO_INTEGRAZIONE 
       foreign key (id_integrazione) 
       references t_integrazione;

    alter table t_allegato 
       add constraint FK_ALLEGATO_PARERE 
       foreign key (id_parere) 
       references t_parere;

    alter table t_allegato 
       add constraint FK_ALLEGATO_PRATICA 
       foreign key (id_pratica) 
       references t_pratica;

    alter table t_allegato 
       add constraint FK_ALLEGATO_TIPO_ALLEGATO 
       foreign key (id_tipo_allegato) 
       references tp_tipo_allegato;

    alter table t_comunicazione_mail 
       add constraint FK_COM_MAIL_PRATICA 
       foreign key (id_pratica) 
       references t_pratica;

    alter table t_comunicazione_mail 
       add constraint FK_COM_MAIL_RICH_INT 
       foreign key (id_rich_integrazione) 
       references t_richiesta_integrazione;

    alter table t_comunicazione_mail 
       add constraint FK_COM_MAIL_RICH_PARERE 
       foreign key (id_rich_parere) 
       references t_richiesta_parere;

    alter table t_dati_richiesta 
       add constraint FK_DATI_RICHIESTA_TIPO_ATT 
       foreign key (id_tipo_attivita_da_svolgere) 
       references tp_tipo_attivita_da_svolgere;

    alter table t_dati_richiesta 
       add constraint FK_DATI_RICHIESTA_TIPO_MAN 
       foreign key (id_tipo_manufatto) 
       references tp_tipo_manufatto;

    alter table t_dati_richiesta 
       add constraint FK_DATI_RICHIESTA_TIP_TITOLO_ED 
       foreign key (id_tipologia_titolo_edilizio) 
       references tp_tipologia_titolo_edilizio;

    alter table t_dati_richiesta_aud 
       add constraint FKny7nqta00f21ss031ga09wv8 
       foreign key (rev) 
       references revinfo;

    alter table t_integrazione 
       add constraint FK_INTEGRAZIONE_RICH_INT 
       foreign key (id_richiesta_integrazione) 
       references t_richiesta_integrazione;

    alter table t_integrazione 
       add constraint FK_INTEGRAZIONE_UTENTE 
       foreign key (id_utente_integrazione) 
       references t_utente;

    alter table t_integrazione_aud 
       add constraint FKmdo8ms4yvh2ykkmhk5h6w78wq 
       foreign key (rev) 
       references revinfo;

    alter table t_notifica_scadenzario 
       add constraint FK_NOTIFICA_SCAD_PRATICA 
       foreign key (id_pratica) 
       references t_pratica;

    alter table t_notifica_scadenzario 
       add constraint FK_NOTIFICA_SCAD_TIPO 
       foreign key (id_tipo_notifica_scadenzario) 
       references tp_tipo_notifica_scadenzario;

    alter table t_notifica_scadenzario 
       add constraint FK_NOTIFICA_SCAD_UTENTE 
       foreign key (id_utente) 
       references t_utente;

    alter table t_parere 
       add constraint FK_PARERE_RICH_PARERE 
       foreign key (id_richiesta_parere) 
       references t_richiesta_parere;

    alter table t_parere 
       add constraint FK_PARERE_UTENTE 
       foreign key (id_utente_parere) 
       references t_utente;

    alter table t_parere_aud 
       add constraint FKlr5ggl5bsnxb3r0h1961h35pe 
       foreign key (rev) 
       references revinfo;

    alter table t_pratica 
       add constraint FK_PRATICA_DATI_RICHIESTA 
       foreign key (id_dati_richiesta) 
       references t_dati_richiesta;

    alter table t_pratica 
       add constraint FK_PRATICA_RICH_DESTINATARIO 
       foreign key (id_richiedente_destinatario) 
       references t_richiedente;

    alter table t_pratica 
       add constraint FK_PRATICA_RICH_FIRMATARIO 
       foreign key (id_richiedente_firmatario) 
       references t_richiedente;

    alter table t_pratica 
       add constraint FK_PRATICA_MUNICIPIO 
       foreign key (id_municipio) 
       references t_municipio;

    alter table t_pratica 
       add constraint FK_PRATICA_STATO_PRATICA 
       foreign key (id_stato_pratica) 
       references tp_stato_pratica;

    alter table t_pratica 
       add constraint FK_PRATICA_TIPO_PROCESSO 
       foreign key (id_tipo_processo) 
       references tp_tipo_processo;

    alter table t_pratica 
       add constraint FK_PRATICA_UTENTE_ASS 
       foreign key (id_utente_assegnatario) 
       references t_utente;

    alter table t_pratica 
       add constraint FK_PRATICA_UTENTE_CRE 
       foreign key (id_utente_creazione) 
       references t_utente;

    alter table t_pratica 
       add constraint FK_PRATICA_UTENTE_MOD 
       foreign key (id_utente_modifica) 
       references t_utente;

    alter table t_pratica 
       add constraint FK_PRATICA_UTENTE_PC 
       foreign key (id_utente_presa_in_carico) 
       references t_utente;

    alter table t_pratica_aud 
       add constraint FK9uk9jlp3oc12em76g8u918exu 
       foreign key (rev) 
       references revinfo;

    alter table t_protocollo 
       add constraint FK_PROTOCOLLO_PRATICA 
       foreign key (id_pratica) 
       references t_pratica;

    alter table t_protocollo 
       add constraint FK_PROTOCOLLO_STATO_PRATICA 
       foreign key (id_stato_pratica) 
       references tp_stato_pratica;

    alter table t_protocollo_aud 
       add constraint FKbwxy934njob26ujw7ww3ee06 
       foreign key (rev) 
       references revinfo;

    alter table t_richiedente 
       add constraint FK_RICHIEDENTE_TIP_DOC_ALL 
       foreign key (id_tipo_documento_allegato) 
       references tp_tipo_doc_allegato;

    alter table t_richiedente 
       add constraint FK_RICHIEDENTE_TIP_RUOLO_RICH 
       foreign key (id_tipo_ruolo_richiedente) 
       references tp_tipo_ruolo_richiedente;

    alter table t_richiesta_integrazione 
       add constraint FK_RICH_INTEGRAZIONE_PRATICA 
       foreign key (id_pratica) 
       references t_pratica;

    alter table t_richiesta_integrazione 
       add constraint FK_PRATICA_STATO_PRATICA 
       foreign key (id_stato_pratica) 
       references tp_stato_pratica;

    alter table t_richiesta_integrazione 
       add constraint FK_PRATICA_UTENTE_RICH 
       foreign key (id_utente_richiedente) 
       references t_utente;

    alter table t_richiesta_integrazione_aud 
       add constraint FKrpk5ubhjt4mvmb5mmjphxu7c9 
       foreign key (rev) 
       references revinfo;

    alter table t_richiesta_parere 
       add constraint FK_RICH_PARERE_GRUPPO 
       foreign key (id_gruppo_dest_parere) 
       references t_gruppo;

    alter table t_richiesta_parere 
       add constraint FK_RICH_PARERE_PRATICA 
       foreign key (id_pratica) 
       references t_pratica;

    alter table t_richiesta_parere 
       add constraint FK_RICH_PARERE_STATO_PRATICA 
       foreign key (id_stato_pratica) 
       references tp_stato_pratica;

    alter table t_richiesta_parere 
       add constraint FK_RICH_PARERE_UTENTE_RICH 
       foreign key (id_utente_richiedente) 
       references t_utente;

    alter table t_richiesta_parere_aud 
       add constraint FK71v0so209i8gxtks9rs84pfes 
       foreign key (rev) 
       references revinfo;

    alter table t_template 
       add constraint FK_TEMPLATE_TIPO_TEMPLATE 
       foreign key (id_tipo_template) 
       references tp_tipo_template;

    alter table t_template 
       add constraint FK_TEMPLATE_UTENTE_MOD 
       foreign key (id_utente_modifica) 
       references t_utente;

    alter table t_utente 
       add constraint FK_UTENTE_GRUPPO 
       foreign key (id_gruppo) 
       references t_gruppo;

--NUOVI
    alter table public.t_pratica add column origin_egov boolean default false;

    alter table public.t_pratica add column ID_MARCA_BOLLO_PRATICA int8;

    alter table public.t_pratica add column ID_MARCA_BOLLO_DETERMINA int8;

    alter table public.t_pratica add column FLAG_ESENZIONE_PAGAMENTO_CUP boolean default false;

    alter table public.t_pratica add column MOTIVAZIONE_ESENZIONE_PAGAMENTO_CUP varchar(255);

    alter table t_automi_protocollo
       add constraint UQ_AUTOMI_PROTOCOLLO_UO_ID unique (UO_ID);

    alter table t_automi_protocollo
       add constraint FK_AUTOMA_MUNICIPIO
       foreign key (id_municipio)
       references t_municipio;

    alter table t_pratica
       add constraint FK_MARCA_BOLLO_PRATICA
       foreign key (ID_MARCA_BOLLO_PRATICA)
       references T_MARCA_BOLLO_PRATICA;

    alter table t_pratica
       add constraint FK_MARCA_BOLLO_DETERMINA
       foreign key (ID_MARCA_BOLLO_DETERMINA)
       references T_MARCA_BOLLO_DETERMINA;

    alter table t_pratica add column INFO_PASSAGGIO_STATO varchar(512);

    alter table public.t_pratica_aud add column origin_egov boolean default false;

    alter table public.t_pratica_aud add column ID_MARCA_BOLLO_PRATICA int8;

    alter table public.t_pratica_aud add column ID_MARCA_BOLLO_DETERMINA int8;

    alter table public.t_pratica_aud add column FLAG_ESENZIONE_PAGAMENTO_CUP boolean default false;

    alter table public.t_pratica_aud add column MOTIVAZIONE_ESENZIONE_PAGAMENTO_CUP varchar(255);

    alter table public.t_pratica_aud add column INFO_PASSAGGIO_STATO varchar(512);

    alter table public.t_dati_richiesta add column FLAG_ESENZIONE_MARCA_DA_BOLLO boolean default false;

    alter table public.t_dati_richiesta add column FLAG_ESENZIONE_MARCA_DA_BOLLO_MODIFICATO boolean default false;

    alter table public.t_dati_richiesta add column MOTIVAZIONE_ESENZIONE_MARCA_BOLLO varchar(255);

    alter table public.t_dati_richiesta_aud add column FLAG_ESENZIONE_MARCA_DA_BOLLO boolean default false;

    alter table public.t_dati_richiesta_aud add column FLAG_ESENZIONE_MARCA_DA_BOLLO_MODIFICATO boolean default false;

    alter table public.t_dati_richiesta_aud add column MOTIVAZIONE_ESENZIONE_MARCA_BOLLO varchar(255);

    alter table public.t_protocollo add column NUMERO_PROTOCOLLO varchar(100);

    alter table public.t_protocollo add column ANNO varchar(4);

    alter table public.t_protocollo_aud add column NUMERO_PROTOCOLLO varchar(100);

    alter table public.t_protocollo_aud add column ANNO varchar(4);

    alter table public.t_integrazione add column NUMERO_PROTOCOLLO varchar(100);

    alter table public.t_integrazione add column ANNO varchar(4);

    alter table public.t_integrazione_aud add column NUMERO_PROTOCOLLO varchar(100);

    alter table public.t_integrazione_aud add column ANNO varchar(4);

    alter table public.tp_stato_pratica add column descrizione_estesa varchar(255);
create table if not exists public.t_automi_protocollo_aud
(
    id               integer      not null,
    rev              integer      not null,
    revtype          smallint,
    uo_id            varchar(100) not null,
    id_municipio     integer,
    denominazione    varchar(100) not null,
    label            varchar(100),
    data_inserimento timestamp    not null,
    data_modifica    timestamp,
    primary key (id, rev)
);

create table if not exists public.revinfo
(
    rev      integer not null
        primary key,
    revtstmp bigint
);

create table if not exists public.t_dati_richiesta_aud
(
    id                                       bigint  not null,
    rev                                      integer not null
        constraint fkny7nqta00f21ss031ga09wv8
            references public.revinfo,
    revtype                                  smallint,
    cod_via                                  varchar(255),
    coord_ubicazione_definitiva              geometry,
    coord_ubicazione_temporanea              geometry,
    data_inizio_occupazione                  date,
    data_scadenza_occupazione                date,
    descrizione_att_da_svolgere              varchar(255),
    descrizione_manufatto                    varchar(255),
    descrizione_titolo_edilizio              varchar(255),
    flg_accett_reg_suolo_pubblico            boolean default false,
    flg_conosc_tassa_occupazione             boolean default false,
    flg_non_modifiche_rispetto_concessione   boolean default false,
    flg_num_civico_assente                   boolean default false,
    flg_obbligo_rip_danni                    boolean default false,
    flg_risp_disp_regolamento                boolean default false,
    flg_risp_interesse_terzi                 boolean default false,
    id_municipio                             integer,
    larghezza_carr_m                         double precision,
    larghezza_m                              double precision,
    larghezza_marc_m                         double precision,
    localita                                 varchar(255),
    lunghezza_carr_m                         double precision,
    lunghezza_m                              double precision,
    lunghezza_marc_m                         double precision,
    nome_via                                 varchar(255),
    note_ubicazione                          text,
    numero_via                               varchar(255),
    ora_inizio_occupazione                   time,
    ora_scadenza_occupazione                 time,
    pres_pass_carr_div_abili                 boolean,
    pres_scivoli_div_abili                   boolean,
    riferimento_titolo_edilizio              varchar(255),
    stallo_di_sosta                          boolean,
    superficie_area_mq                       double precision,
    superficie_marc_mq                       double precision,
    tp_op_verifica_occupazione               varchar(255),
    ubicazione_occupazione                   varchar(255),
    id_tipo_attivita_da_svolgere             integer,
    id_tipo_manufatto                        integer,
    id_tipologia_titolo_edilizio             integer,
    flag_esenzione_marca_da_bollo            boolean default false,
    flag_esenzione_marca_da_bollo_modificato boolean default false,
    motivazione_esenzione_marca_bollo        varchar(255),
    primary key (id, rev)
);

create table if not exists public.t_gruppo
(
    id                             integer               not null
        primary key,
    descrizione                    varchar(50)           not null
        constraint uq_gruppo_descrizione
            unique,
    flg_attesa_pagamento           boolean default false not null,
    flg_concessioni_valide         boolean default false not null,
    flg_fascicolo                  boolean default false not null,
    flg_gest_invio_mail_determina  boolean default false not null,
    flg_gest_richiesta_parere      boolean default false not null,
    flg_gestione_profilo           boolean default false not null,
    flg_gestione_utenti            boolean default false not null,
    flg_inserisci_richiesta        boolean default false not null,
    flg_necessaria_integrazione    boolean default false not null,
    flg_pratica_inserita           boolean default false not null,
    flg_pratiche_approvate         boolean default false not null,
    flg_pratiche_archiviate        boolean default false not null,
    flg_pratiche_da_rigettare      boolean default false not null,
    flg_pratiche_preavviso_diniego boolean default false not null,
    flg_pratiche_rigettate         boolean default false not null,
    flg_pronte_rilascio            boolean default false not null,
    flg_richiesta_pareri           boolean default false not null,
    flg_verifica_pratiche          boolean default false not null,
    flg_verifica_ripristino_luoghi boolean default false not null
);

create table if not exists public.t_integrazione_aud
(
    id                        bigint  not null,
    rev                       integer not null
        constraint fkmdo8ms4yvh2ykkmhk5h6w78wq
            references public.revinfo,
    revtype                   smallint,
    cf_cittadino_egov         varchar(16),
    codice_protocollo         varchar(255),
    cognome_cittadino_egov    varchar(255),
    data_inizio_occupazione   date,
    data_inserimento          timestamp,
    data_protocollo           timestamp,
    data_scadenza_occupazione date,
    motivo_integrazione       varchar(255),
    nome_cittadino_egov       varchar(255),
    ora_inizio_occupazione    time,
    ora_scadenza_occupazione  time,
    id_richiesta_integrazione bigint,
    id_utente_integrazione    bigint,
    numero_protocollo         varchar(100),
    anno                      varchar(4),
    primary key (id, rev)
);

create table if not exists public.t_municipio
(
    id          integer     not null
        primary key,
    descrizione varchar(50) not null
        constraint uq_municipio_descrizione
            unique
);

create table if not exists public.t_automi_protocollo
(
    id               integer      not null
        primary key,
    uo_id            varchar(100) not null
        constraint uq_automi_protocollo_uo_id
            unique,
    id_municipio     integer
        constraint fk_automa_municipio
            references public.t_municipio,
    denominazione    varchar(100) not null,
    label            varchar(100),
    data_inserimento timestamp    not null,
    data_modifica    timestamp
);

create table if not exists public.t_parere_aud
(
    id                  bigint  not null,
    rev                 integer not null
        constraint fklr5ggl5bsnxb3r0h1961h35pe
            references public.revinfo,
    revtype             smallint,
    codice_protocollo   varchar(255),
    data_inserimento    timestamp,
    data_protocollo     timestamp,
    flg_esito           boolean,
    flg_competenza      boolean,
    nota                varchar(255),
    id_richiesta_parere bigint,
    id_utente_parere    bigint,
    primary key (id, rev)
);

create table if not exists public.t_pratica_aud
(
    id                                  bigint  not null,
    rev                                 integer not null
        constraint fk9uk9jlp3oc12em76g8u918exu
            references public.revinfo,
    revtype                             smallint,
    cf_cittadino_egov                   varchar(16),
    codice_determina                    varchar(255),
    codice_determina_rda                varchar(255),
    codice_determina_rinuncia           varchar(255),
    cognome_cittadino_egov              varchar(255),
    contatore_richieste_integ           integer,
    data_creazione                      timestamp,
    data_emissione_determina            date,
    data_emissione_determina_rda        date,
    data_emissione_determina_rin        date,
    data_inserimento                    timestamp,
    data_modifica                       timestamp,
    data_scadenza_pagamento             timestamp,
    data_scadenza_pratica               timestamp,
    data_scadenza_preav_diniego         timestamp,
    data_scadenza_rigetto               timestamp,
    flg_procedura_diniego               boolean,
    flg_verifica_formale                boolean,
    id_pratica_originaria               bigint,
    id_proroga_precedente               bigint,
    motivazione_richiesta               varchar(255),
    nome_cittadino_egov                 varchar(255),
    nota_al_cittadino_rda               varchar(255),
    id_dati_richiesta                   bigint,
    id_richiedente_destinatario         bigint,
    id_richiedente_firmatario           bigint,
    id_municipio                        integer,
    id_stato_pratica                    integer,
    id_tipo_processo                    integer,
    id_utente_assegnatario              bigint,
    id_utente_creazione                 bigint,
    id_utente_modifica                  bigint,
    id_utente_presa_in_carico           bigint,
    origin_egov                         boolean default false,
    id_marca_bollo_pratica              bigint,
    id_marca_bollo_determina            bigint,
    flag_esenzione_pagamento_cup        boolean default false,
    motivazione_esenzione_pagamento_cup varchar(255),
    info_passaggio_stato                varchar(512),
    primary key (id, rev)
);

create table if not exists public.t_protocollo_aud
(
    id                           bigint  not null,
    rev                          integer not null
        constraint fkbwxy934njob26ujw7ww3ee06
            references public.revinfo,
    revtype                      smallint,
    codice_determina_rettifica   varchar(255),
    codice_protocollo            varchar(255),
    data_emissione_determina_ret date,
    data_protocollo              timestamp,
    tipo_operazione              varchar(255),
    id_pratica                   bigint,
    id_stato_pratica             integer,
    numero_protocollo            varchar(100),
    anno                         varchar(4),
    primary key (id, rev)
);

create table if not exists public.t_richiesta_integrazione_aud
(
    id                    bigint  not null,
    rev                   integer not null
        constraint fkrpk5ubhjt4mvmb5mmjphxu7c9
            references public.revinfo,
    revtype               smallint,
    codice_protocollo     varchar(255),
    data_inserimento      timestamp,
    data_protocollo       timestamp,
    data_scadenza         timestamp,
    flg_inserita_risposta boolean default false,
    motivo_richiesta      varchar(255),
    tipo_richiesta        varchar(255),
    id_pratica            bigint,
    id_stato_pratica      integer,
    id_utente_richiedente bigint,
    primary key (id, rev)
);

create table if not exists public.t_richiesta_parere_aud
(
    id                    bigint  not null,
    rev                   integer not null
        constraint fk71v0so209i8gxtks9rs84pfes
            references public.revinfo,
    revtype               smallint,
    codice_protocollo     varchar(255),
    data_inserimento      timestamp,
    data_protocollo       timestamp,
    flg_inserita_risposta boolean default false,
    nota_richiesta_parere varchar(255),
    id_gruppo_dest_parere integer,
    id_pratica            bigint,
    id_stato_pratica      integer,
    id_utente_richiedente bigint,
    primary key (id, rev)
);

create table if not exists public.t_utente
(
    id                   bigint                not null
        primary key,
    codice_fiscale       varchar(16)           not null,
    cognome              varchar(50),
    data_di_nascita      date,
    data_eliminazione    timestamp,
    date_created         timestamp             not null,
    email                varchar(50),
    enabled              boolean default true  not null,
    flg_eliminato        boolean default false not null,
    indirizzo            varchar(255),
    last_login           timestamp,
    luogo_di_nascita     varchar(50),
    nome                 varchar(50),
    num_tel              varchar(15),
    password_            varchar(120)          not null,
    provincia_di_nascita varchar(50),
    ragione_sociale      varchar(100),
    sesso                varchar(1),
    uo_id                varchar(255),
    username             varchar(30)           not null,
    id_gruppo            integer               not null
        constraint fk_utente_gruppo
            references public.t_gruppo
);

create table if not exists public.a_utente_municipio
(
    id_utente    bigint  not null
        constraint fk_a_utente_municipio_ut
            references public.t_utente,
    id_municipio integer not null
        constraint fk_a_utente_municipio_mun
            references public.t_municipio,
    primary key (id_utente, id_municipio)
);

create table if not exists public.tp_stato_pratica
(
    id                 integer     not null
        primary key,
    descrizione        varchar(50) not null
        constraint uq_stato_pratica_descrizione
            unique,
    descrizione_estesa varchar(255)
);

create table if not exists public.tp_tipo_allegato
(
    id                 integer      not null
        primary key,
    descrizione        varchar(100) not null
        constraint uq_tipo_allegato
            unique,
    descrizione_estesa varchar(255)
);

create table if not exists public.tp_tipo_attivita_da_svolgere
(
    id               integer               not null
        primary key,
    descrizione      varchar(50)           not null
        constraint uq_tipo_att_da_svol_desc
            unique,
    flg_testo_libero boolean default false not null
);

create table if not exists public.tp_tipo_doc_allegato
(
    id          integer     not null
        primary key,
    descrizione varchar(50) not null
        constraint uq_tipo_doc_allegato_desc
            unique
);

create table if not exists public.tp_tipo_manufatto
(
    id               integer               not null
        primary key,
    descrizione      varchar(50)           not null
        constraint uq_tipo_manufatto_desc
            unique,
    flg_testo_libero boolean default false not null
);

create table if not exists public.tp_tipo_notifica_scadenzario
(
    id          integer     not null
        primary key,
    descrizione varchar(50) not null
        constraint uq_tipo_notifica_scad_desc
            unique
);

create table if not exists public.tp_tipo_processo
(
    id          integer     not null
        primary key,
    descrizione varchar(50) not null
        constraint uq_tipo_processo_desc
            unique
);

create table if not exists public.a_tipo_allegato_grup_stat_proc
(
    id               integer               not null
        primary key,
    flg_obbligatorio boolean default false not null,
    flg_testo_libero boolean default false not null,
    id_gruppo        integer               not null
        constraint fk_a_tipo_all_gruppo
            references public.t_gruppo,
    id_stato_pratica integer               not null
        constraint fk_a_tipo_all_stato_pratica
            references public.tp_stato_pratica,
    id_tipo_allegato integer               not null
        constraint fk_a_tipo_all
            references public.tp_tipo_allegato,
    id_tipo_processo integer               not null
        constraint fk_a_tipo_all_tipo_processo
            references public.tp_tipo_processo,
    constraint uq_a_tipo_allegato
        unique (id_tipo_allegato, id_stato_pratica, id_tipo_processo, id_gruppo)
);

create table if not exists public.tp_tipo_ruolo_richiedente
(
    id          integer     not null
        primary key,
    descrizione varchar(50) not null
        constraint uq_tipo_ruolo_rich_descrizione
            unique
);

create table if not exists public.t_richiedente
(
    id                         bigint  not null
        primary key,
    amm_doc_allegato           varchar(50),
    cap                        varchar(5),
    citta                      varchar(50),
    civico                     varchar(10),
    codice_fiscale_p_iva       varchar(16),
    cognome                    varchar(50),
    comune_di_nascita          varchar(50),
    data_di_nascita            date,
    denominazione              varchar(50),
    email                      varchar(50),
    flg_destinatario           boolean not null,
    flg_firmatario             boolean not null,
    indirizzo                  varchar(100),
    nazionalita                varchar(50),
    nome                       varchar(50),
    numero_doc_allegato        varchar(50),
    provincia                  varchar(50),
    provincia_di_nascita       varchar(50),
    recapito_telefonico        varchar(15),
    id_tipo_documento_allegato integer
        constraint fk_richiedente_tip_doc_all
            references public.tp_tipo_doc_allegato,
    id_tipo_ruolo_richiedente  integer not null
        constraint fk_richiedente_tip_ruolo_rich
            references public.tp_tipo_ruolo_richiedente,
    qualita_ruolo              varchar(50),
    descrizione_ruolo          varchar(50)
);

create table if not exists public.tp_tipo_template
(
    id          integer      not null
        primary key,
    descrizione varchar(100) not null
        constraint uq_tipo_template_descrizione
            unique
);

create table if not exists public.t_template
(
    id                 integer      not null
        primary key,
    data_inserimento   timestamp    not null,
    data_modifica      timestamp,
    file_template      bytea        not null,
    mime_type          varchar(255) not null,
    nome_file          varchar(255) not null,
    id_tipo_template   integer      not null
        constraint uq_template_tipo
            unique
        constraint fk_template_tipo_template
            references public.tp_tipo_template,
    id_utente_modifica bigint
        constraint fk_template_utente_mod
            references public.t_utente
);

create table if not exists public.tp_tipologia_titolo_edilizio
(
    id               integer               not null
        primary key,
    descrizione      varchar(50)           not null
        constraint uq_tip_titolo_ed_desc
            unique,
    flg_testo_libero boolean default false not null
);

create table if not exists public.t_dati_richiesta
(
    id                                       bigint                not null
        primary key,
    cod_via                                  varchar(20),
    coord_ubicazione_definitiva              geometry,
    coord_ubicazione_temporanea              geometry,
    data_inizio_occupazione                  date                  not null,
    data_scadenza_occupazione                date                  not null,
    descrizione_att_da_svolgere              varchar(255),
    descrizione_manufatto                    varchar(255),
    descrizione_titolo_edilizio              varchar(100),
    flg_accett_reg_suolo_pubblico            boolean default false not null,
    flg_conosc_tassa_occupazione             boolean default false not null,
    flg_non_modifiche_rispetto_concessione   boolean default false not null,
    flg_num_civico_assente                   boolean default false not null,
    flg_obbligo_rip_danni                    boolean default false not null,
    flg_risp_disp_regolamento                boolean default false not null,
    flg_risp_interesse_terzi                 boolean default false not null,
    id_municipio                             integer,
    larghezza_carr_m                         double precision,
    larghezza_m                              double precision      not null,
    larghezza_marc_m                         double precision,
    localita                                 varchar(50),
    lunghezza_carr_m                         double precision,
    lunghezza_m                              double precision      not null,
    lunghezza_marc_m                         double precision,
    nome_via                                 varchar(255),
    note_ubicazione                          text,
    numero_via                               varchar(5),
    ora_inizio_occupazione                   time,
    ora_scadenza_occupazione                 time,
    pres_pass_carr_div_abili                 boolean,
    pres_scivoli_div_abili                   boolean,
    riferimento_titolo_edilizio              varchar(100),
    stallo_di_sosta                          boolean,
    superficie_area_mq                       double precision      not null,
    superficie_marc_mq                       double precision,
    tp_op_verifica_occupazione               varchar(255),
    ubicazione_occupazione                   varchar(300)          not null,
    id_tipo_attivita_da_svolgere             integer
        constraint fk_dati_richiesta_tipo_att
            references public.tp_tipo_attivita_da_svolgere,
    id_tipo_manufatto                        integer
        constraint fk_dati_richiesta_tipo_man
            references public.tp_tipo_manufatto,
    id_tipologia_titolo_edilizio             integer
        constraint fk_dati_richiesta_tip_titolo_ed
            references public.tp_tipologia_titolo_edilizio,
    flag_esenzione_marca_da_bollo            boolean default false,
    flag_esenzione_marca_da_bollo_modificato boolean default false,
    motivazione_esenzione_marca_bollo        varchar(255)
);

create table if not exists public.t_marca_bollo_pratica
(
    id                bigint not null
        primary key,
    iuv               varchar(255),
    impronta_file     varchar(255),
    importo_pagato    numeric,
    causale_pagamento varchar(255),
    id_richiesta      varchar(255),
    data_operazione   date
);

create table if not exists public.t_marca_bollo_determina
(
    id                bigint not null
        primary key,
    iuv               varchar(255),
    impronta_file     varchar(255),
    importo_pagato    numeric,
    causale_pagamento varchar(255),
    id_richiesta      varchar(255),
    data_operazione   date
);

create table if not exists public.t_pratica
(
    id                                  bigint    not null
        primary key,
    cf_cittadino_egov                   varchar(16),
    codice_determina                    varchar(255),
    codice_determina_rda                varchar(255),
    codice_determina_rinuncia           varchar(255),
    cognome_cittadino_egov              varchar(50),
    contatore_richieste_integ           integer,
    data_creazione                      timestamp not null,
    data_emissione_determina            date,
    data_emissione_determina_rda        date,
    data_emissione_determina_rin        date,
    data_inserimento                    timestamp,
    data_modifica                       timestamp,
    data_scadenza_pagamento             timestamp,
    data_scadenza_pratica               timestamp,
    data_scadenza_preav_diniego         timestamp,
    data_scadenza_rigetto               timestamp,
    flg_procedura_diniego               boolean,
    flg_verifica_formale                boolean,
    id_pratica_originaria               bigint,
    id_proroga_precedente               bigint,
    motivazione_richiesta               varchar(512),
    nome_cittadino_egov                 varchar(50),
    nota_al_cittadino_rda               varchar(512),
    id_dati_richiesta                   bigint    not null
        constraint uq_pratica_dati_richiesta
            unique
        constraint fk_pratica_dati_richiesta
            references public.t_dati_richiesta,
    id_richiedente_destinatario         bigint
        constraint fk_pratica_rich_destinatario
            references public.t_richiedente,
    id_richiedente_firmatario           bigint    not null
        constraint fk_pratica_rich_firmatario
            references public.t_richiedente,
    id_municipio                        integer
        constraint fk_pratica_municipio
            references public.t_municipio,
    id_stato_pratica                    integer   not null
        constraint fk_pratica_stato_pratica
            references public.tp_stato_pratica,
    id_tipo_processo                    integer   not null
        constraint fk_pratica_tipo_processo
            references public.tp_tipo_processo,
    id_utente_assegnatario              bigint
        constraint fk_pratica_utente_ass
            references public.t_utente,
    id_utente_creazione                 bigint    not null
        constraint fk_pratica_utente_cre
            references public.t_utente,
    id_utente_modifica                  bigint
        constraint fk_pratica_utente_mod
            references public.t_utente,
    id_utente_presa_in_carico           bigint
        constraint fk_pratica_utente_pc
            references public.t_utente,
    origin_egov                         boolean default false,
    id_marca_bollo_pratica              bigint
        constraint fk_marca_bollo_pratica
            references public.t_marca_bollo_pratica,
    id_marca_bollo_determina            bigint
        constraint fk_marca_bollo_determina
            references public.t_marca_bollo_determina,
    flag_esenzione_pagamento_cup        boolean default false,
    motivazione_esenzione_pagamento_cup varchar(255),
    info_passaggio_stato                varchar(512)
);

create table if not exists public.t_notifica_scadenzario
(
    id                           bigint  not null
        primary key,
    data_notifica                timestamp,
    id_pratica                   bigint  not null
        constraint fk_notifica_scad_pratica
            references public.t_pratica,
    id_tipo_notifica_scadenzario integer not null
        constraint fk_notifica_scad_tipo
            references public.tp_tipo_notifica_scadenzario,
    id_utente                    bigint  not null
        constraint fk_notifica_scad_utente
            references public.t_utente
);

create table if not exists public.t_protocollo
(
    id                           bigint  not null
        primary key,
    codice_determina_rettifica   varchar(255),
    codice_protocollo            varchar(100),
    data_emissione_determina_ret date,
    data_protocollo              timestamp,
    tipo_operazione              varchar(255),
    id_pratica                   bigint  not null
        constraint fk_protocollo_pratica
            references public.t_pratica,
    id_stato_pratica             integer not null
        constraint fk_protocollo_stato_pratica
            references public.tp_stato_pratica,
    numero_protocollo            varchar(100),
    anno                         varchar(4)
);

create table if not exists public.t_richiesta_integrazione
(
    id                    bigint                not null
        primary key,
    codice_protocollo     varchar(100),
    data_inserimento      timestamp             not null,
    data_protocollo       timestamp,
    data_scadenza         timestamp             not null,
    flg_inserita_risposta boolean default false not null,
    motivo_richiesta      varchar(512),
    tipo_richiesta        varchar(255),
    id_pratica            bigint                not null
        constraint fk_rich_integrazione_pratica
            references public.t_pratica,
    id_stato_pratica      integer               not null
        constraint fk_pratica_stato_pratica
            references public.tp_stato_pratica,
    id_utente_richiedente bigint                not null
        constraint fk_pratica_utente_rich
            references public.t_utente
);

create table if not exists public.t_integrazione
(
    id                        bigint    not null
        primary key,
    cf_cittadino_egov         varchar(16),
    codice_protocollo         varchar(255),
    cognome_cittadino_egov    varchar(50),
    data_inizio_occupazione   date,
    data_inserimento          timestamp not null,
    data_protocollo           timestamp,
    data_scadenza_occupazione date,
    motivo_integrazione       varchar(512),
    nome_cittadino_egov       varchar(50),
    ora_inizio_occupazione    time,
    ora_scadenza_occupazione  time,
    id_richiesta_integrazione bigint    not null
        constraint fk_integrazione_rich_int
            references public.t_richiesta_integrazione,
    id_utente_integrazione    bigint    not null
        constraint fk_integrazione_utente
            references public.t_utente,
    numero_protocollo         varchar(100),
    anno                      varchar(4)
);

create table if not exists public.t_richiesta_parere
(
    id                    bigint                not null
        primary key,
    codice_protocollo     varchar(100),
    data_inserimento      timestamp             not null,
    data_protocollo       timestamp,
    flg_inserita_risposta boolean default false not null,
    nota_richiesta_parere varchar(512),
    id_gruppo_dest_parere integer               not null
        constraint fk_rich_parere_gruppo
            references public.t_gruppo,
    id_pratica            bigint                not null
        constraint fk_rich_parere_pratica
            references public.t_pratica,
    id_stato_pratica      integer               not null
        constraint fk_rich_parere_stato_pratica
            references public.tp_stato_pratica,
    id_utente_richiedente bigint                not null
        constraint fk_rich_parere_utente_rich
            references public.t_utente
);

create table if not exists public.t_comunicazione_mail
(
    id                      bigint                not null
        primary key,
    date_inserimento        timestamp             not null,
    date_invio              timestamp,
    destinatari             varchar(500)          not null,
    destinatari_cc          varchar(500),
    file_allegato           bytea,
    flg_inviata             boolean default false not null,
    flg_pec                 boolean default false not null,
    mime_type_file_allegato varchar(255),
    nome_file_allegato      varchar(255),
    numero_tentativi_invio  integer,
    oggetto                 varchar(998)          not null,
    testo                   varchar(2000),
    id_pratica              bigint
        constraint fk_com_mail_pratica
            references public.t_pratica,
    id_rich_integrazione    bigint
        constraint fk_com_mail_rich_int
            references public.t_richiesta_integrazione,
    id_rich_parere          bigint
        constraint fk_com_mail_rich_parere
            references public.t_richiesta_parere
);

create table if not exists public.t_parere
(
    id                  bigint    not null
        primary key,
    codice_protocollo   varchar(255),
    data_inserimento    timestamp not null,
    data_protocollo     timestamp,
    flg_esito           boolean,
    flg_competenza      boolean,
    nota                varchar(255),
    id_richiesta_parere bigint    not null
        constraint fk_parere_rich_parere
            references public.t_richiesta_parere,
    id_utente_parere    bigint    not null
        constraint fk_parere_utente
            references public.t_utente
);

create table if not exists public.t_allegato
(
    id                        bigint    not null
        primary key,
    codice_protocollo         varchar(100),
    data_inserimento          timestamp not null,
    file_allegato             bytea,
    id_richiesta_integrazione bigint,
    id_richiesta_parere       bigint,
    mime_type                 varchar(255),
    nome_file                 varchar(255),
    nota                      varchar(255),
    revisione                 integer
        constraint t_allegato_revisione_check
            check (revisione >= 1),
    id_integrazione           bigint
        constraint fk_allegato_integrazione
            references public.t_integrazione,
    id_parere                 bigint
        constraint fk_allegato_parere
            references public.t_parere,
    id_pratica                bigint
        constraint fk_allegato_pratica
            references public.t_pratica,
    id_tipo_allegato          integer
        constraint fk_allegato_tipo_allegato
            references public.tp_tipo_allegato
);

create sequence public.hibernate_sequence;

create sequence public.seq_allegato;

create sequence public.seq_com_email;

create sequence public.seq_dati_richiesta;

create sequence public.seq_integrazione;

create sequence public.seq_municipio;

create sequence public.seq_notifica_scadenzario;

create sequence public.seq_parere;

create sequence public.seq_pratica;

create sequence public.seq_protocollo;

create sequence public.seq_rich_integrazione;

create sequence public.seq_rich_parere;

create sequence public.seq_richiedente;

create sequence public.seq_template;

create sequence public.seq_utente;

create sequence public.seq_marca_bollo_pratica;

create sequence public.seq_marca_bollo_determina;

create sequence public.seq_automi_protocollo;
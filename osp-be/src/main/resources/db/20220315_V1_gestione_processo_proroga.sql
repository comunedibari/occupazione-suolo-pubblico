ALTER TABLE t_dati_richiesta ADD flg_non_modifiche_rispetto_concessione bool NOT NULL DEFAULT false;
ALTER TABLE t_dati_richiesta_aud ADD flg_non_modifiche_rispetto_concessione bool NOT NULL DEFAULT false;

ALTER TABLE t_pratica ADD id_proroga_precedente int8;
ALTER TABLE t_pratica_aud ADD id_proroga_precedente int8;

-- inserimento nuove tipologie allegato
INSERT INTO tp_tipo_allegato
(id, descrizione, descrizione_estesa)
VALUES(22, 'Motivazioni tecniche proroga', 'Motivazioni tecniche per la richiesta di proroga da parte della direzione dei lavori per i cantieri edili e stradali o tecnico abilitato');
INSERT INTO tp_tipo_allegato
(id, descrizione, descrizione_estesa)
VALUES(23, 'Attestazione pagamento CUP concessione', 'Attestazione di pagamento Canone Unico Patrimoniale relativa alla precedente occupazione');
INSERT INTO tp_tipo_allegato
(id, descrizione, descrizione_estesa)
VALUES(24, 'Attestazione pagamento TARSU concessione', 'Attestazione di pagamento TARSU relativa alla precedente occupazione');

--inserimento configurazioni allegati
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(109, true, false, 2, 0, 22, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(110, true, false, 2, 0, 5, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(111, true, false, 2, 0, 6, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(112, true, false, 2, 0, 23, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(113, false, false, 2, 0, 10, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(114, false, false, 2, 0, 24, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(115, false, true, 2, 0, 12, 2);

INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(116, true, false, 4, 0, 22, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(117, true, false, 4, 0, 5, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(118, true, false, 4, 0, 6, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(119, true, false, 4, 0, 23, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(120, false, false, 4, 0, 10, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(121, false, false, 4, 0, 24, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(122, false, true, 4, 0, 12, 2);

INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(123, true, false, 20, 0, 22, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(124, true, false, 20, 0, 5, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(125, true, false, 20, 0, 6, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(126, true, false, 20, 0, 23, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(127, false, false, 20, 0, 10, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(128, false, false, 20, 0, 24, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(129, false, true, 20, 0, 12, 2);

INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(130, true, false, 5, 3, 13, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(131, false, false, 5, 3, 15, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(132, true, false, 7, 3, 14, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(133, false, false, 7, 3, 15, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(134, true, false, 18, 3, 14, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(135, false, false, 18, 3, 15, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(136, true, false, 15, 3, 14, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(137, false, false, 15, 3, 15, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(138, true, false, 16, 3, 14, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(139, false, false, 16, 3, 15, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(140, true, false, 14, 3, 14, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(141, false, false, 14, 3, 15, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(142, true, false, 17, 3, 14, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(143, false, false, 17, 3, 15, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(144, true, false, 4, 6, 16, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(145, true, false, 2, 8, 17, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(146, true, false, 4, 8, 17, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(147, true, false, 2, 8, 18, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(148, true, false, 4, 8, 18, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(149, true, false, 4, 5, 19, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(150, false, false, 2, 4, 1, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(151, false, false, 2, 4, 2, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(152, false, false, 2, 4, 3, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(153, false, false, 2, 4, 4, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(154, false, false, 2, 4, 5, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(155, false, false, 2, 4, 6, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(156, false, false, 2, 4, 7, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(157, false, false, 2, 4, 8, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(158, false, false, 2, 4, 9, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(159, false, false, 2, 4, 10, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(160, false, false, 2, 4, 11, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(161, false, true, 2, 4, 12, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(162, false, false, 4, 4, 1, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(163, false, false, 4, 4, 2, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(164, false, false, 4, 4, 3, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(165, false, false, 4, 4, 4, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(166, false, false, 4, 4, 5, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(167, false, false, 4, 4, 6, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(168, false, false, 4, 4, 7, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(169, false, false, 4, 4, 8, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(170, false, false, 4, 4, 9, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(171, false, false, 4, 4, 10, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(172, false, false, 4, 4, 11, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(173, false, true, 4, 4, 12, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(174, false, false, 2, 7, 1, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(175, false, false, 2, 7, 2, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(176, false, false, 2, 7, 3, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(177, false, false, 2, 7, 4, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(178, false, false, 2, 7, 5, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(179, false, false, 2, 7, 6, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(180, false, false, 2, 7, 7, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(181, false, false, 2, 7, 8, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(182, false, false, 2, 7, 9, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(183, false, false, 2, 7, 10, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(184, false, false, 2, 7, 11, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(185, false, true, 2, 7, 12, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(186, false, false, 4, 7, 1, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(187, false, false, 4, 7, 2, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(188, false, false, 4, 7, 3, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(189, false, false, 4, 7, 4, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(190, false, false, 4, 7, 5, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(191, false, false, 4, 7, 6, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(192, false, false, 4, 7, 7, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(193, false, false, 4, 7, 8, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(194, false, false, 4, 7, 9, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(195, false, false, 4, 7, 10, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(196, false, false, 4, 7, 11, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(197, false, true, 4, 7, 12, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(198, false, false, 2, 8, 20, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(199, false, false, 4, 8, 20, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(200, false, false, 2, 8, 21, 2);
INSERT INTO a_tipo_allegato_grup_stat_proc
(id, flg_obbligatorio, flg_testo_libero, id_gruppo, id_stato_pratica, id_tipo_allegato, id_tipo_processo)
VALUES(201, false, false, 4, 8, 21, 2);

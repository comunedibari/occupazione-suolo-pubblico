UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaConcessione.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 1;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaProroga.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 2;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaRigetto.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 3;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaRinuncia.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 4;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaRettifica.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 5;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaRevoca.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 6;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaDecadenza.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 7;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaAnnullamento.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 8;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateOrdinanza.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 10;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateRelazioneServizio.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 11;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateIstruttoriaIVOOPP-UrbanizzazioniPrimarie.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 12;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateIstruttoriaIVOOPP-Giardini.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 13;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateIstruttoriaIVOOPP-InterventiTerritorio.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 14;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateIstruttoriaIVOOPP-InfrastruttureRete.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 15;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateIstruttoriaUrbanistica.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 16;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateIstruttoriaPatrimonio.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 17;

--nuovi
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaProrogaEsenteBollo.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 18;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaProrogaEsenteBolloCUP.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 19;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaProrogaEsenteCUP.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 20;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaConcessioneEsenteBollo.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 21;e
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaConcessioneEsenteBolloCUP.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 22;
UPDATE t_template SET file_template = pg_read_binary_file('/var/lib/postgresql/data/TemplateDeterminaConcessioneEsenteCUP.docx'),
	data_modifica = CURRENT_TIMESTAMP WHERE id = 23;
--end nuovi

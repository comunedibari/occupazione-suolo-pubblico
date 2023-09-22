package it.fincons.osp.payload.civiliario.response;

import lombok.Data;

@Data
public class CiviliarioResponse {

	private Long id;
	private String numero;
	private String cod_via;
	private String nome_via;
	private String esponente;
	private String estensione;
	private String localita;
	private String municipio;
	private String data_fine;
	private String x;
	private String y;

	// campi popolati nell'eleborazione della response successiva
	private Integer numeroCivico;
	private Double lat;
	private Double lon;

	public CiviliarioResponse() {

	}

	public CiviliarioResponse(CiviliarioResponse object) {
		this.id = object.id;
		this.numero = object.numero;
		this.cod_via = object.cod_via;
		this.nome_via = object.nome_via;
		this.esponente = object.esponente;
		this.estensione = object.estensione;
		this.localita = object.localita;
		this.municipio = object.municipio;
		this.data_fine = object.data_fine;
		this.x = object.x;
		this.y = object.y;
		this.numeroCivico = object.numeroCivico;
		this.lat = object.lat;
		this.lon = object.lon;
	}
}

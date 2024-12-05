package model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Episodio {
	private Integer temporada;
	private String titulo;
	private Integer numero;
	private Double avaliacao;
	private LocalDate dataLancamento;
	
	
	public Episodio(Integer numeroTemporada, DadosEpisodio dadosEpisodio) {
		this.temporada = numeroTemporada;
		this.titulo = dadosEpisodio.titulo();
		this.numero = dadosEpisodio.numero();
		
		try {
			this.avaliacao = Double.valueOf(dadosEpisodio.avaliacao());
		}catch (NumberFormatException ex) {
			this.avaliacao = 0.0;
		}
		
		try {
			this.dataLancamento = LocalDate.parse(dadosEpisodio.dataLancamento());
		}catch(DateTimeParseException ex) {
			this.dataLancamento = null;
		}
		
		
	}
		
	public Integer getTemporada() {
		return temporada;
	}
	public void setTemporada(Integer temporada) {
		this.temporada = temporada;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public Integer getNumero() {
		return numero;
	}
	public void setNumero(Integer numero) {
		this.numero = numero;
	}
	public Double getAvaliacao() {
		return avaliacao;
	}
	public void setAvaliacao(Double avaliacao) {
		this.avaliacao = avaliacao;
	}
	public LocalDate getDataLancamento() {
		return dataLancamento;
	}
	public void setDataLancamento(LocalDate dataLancamento) {
		this.dataLancamento = dataLancamento;
	}

	@Override
	public String toString() {
		return "temporada=" + temporada + 
				", titulo=" + titulo + 
				", numero=" + numero + 
				", avaliacao="+ avaliacao + 
				", dataLancamento=" + dataLancamento;
	}
	 
	
}

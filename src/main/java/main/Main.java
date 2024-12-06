package main;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


import model.DadosSerie;
import model.DadosTemporada;
import model.Episodio;
import service.ConsumoApi;
import service.ConverteDados;

public class Main {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private ConverteDados conversor = new ConverteDados();

    private List<Episodio> episodios = new ArrayList<>();

    public static void main(String[] args) {
        Main app = new Main();
        app.menuPrincipal();
    }

    public void menuPrincipal() {
        int opcao;
        do {
            System.out.println("\n--- Menu Principal ---");
            System.out.println("1. Buscar série");
            System.out.println("2. Listar episódios por temporada");
            System.out.println("3. Exibir top 10 episódios");
            System.out.println("4. Pesquisar episódio por título");
            System.out.println("5. Filtrar episódios por data");
            System.out.println("6. Exibir estatísticas de avaliações");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");
            
            try {
                opcao = leitura.nextInt();
                leitura.nextLine(); // Limpar buffer do scanner
                
                switch (opcao) {
                    case 1 -> buscarSerie();
                    case 2 -> listarEpisodios();
                    case 3 -> exibirTop10Episodios();
                    case 4 -> pesquisarEpisodioPorTitulo();
                    case 5 -> filtrarEpisodiosPorData();
                    case 6 -> exibirEstatisticas();
                    case 0 -> System.out.println("Saindo do sistema...");
                    default -> System.out.println("Opção inválida. Tente novamente.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erro: Entrada inválida. Por favor, insira um número.");
                leitura.nextLine(); // Limpar entrada inválida
                opcao = -1; // Reiniciar o loop
            }
        } while (opcao != 0);
    }

    private void buscarSerie() {
        try {
            System.out.print("Digite o nome da série para a busca: ");
            var nomeSerie = leitura.nextLine();
            var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
            DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

            if (dados == null) {
                System.out.println("Erro: Série não encontrada.");
                return;
            }

            System.out.println(dados);

            List<DadosTemporada> temporadas = new ArrayList<>();
            for (int i = 1; i <= dados.totalTemporadas(); i++) {
                json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }

            episodios = temporadas.stream()
                    .flatMap(t -> t.episodios().stream().map(e -> new Episodio(t.numero(), e)))
                    .collect(Collectors.toList());

            System.out.println("Série e episódios carregados com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao buscar série: " + e.getMessage());
        }
    }

    private void listarEpisodios() {
        if (episodios.isEmpty()) {
            System.out.println("Nenhum episódio encontrado. Realize a busca de uma série primeiro.");
            return;
        }

        episodios.forEach(e -> System.out.println("Temporada: " + e.getTemporada() + " Episódio: " + e.getTitulo()));
    }

    private void exibirTop10Episodios() {
        if (episodios.isEmpty()) {
            System.out.println("Nenhum episódio encontrado. Realize a busca de uma série primeiro.");
            return;
        }

        System.out.println("\nTop 10 Episódios:");
        episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .sorted(Comparator.comparingDouble(Episodio::getAvaliacao).reversed())
                .limit(10)
                .forEach(e -> System.out.println("Temporada: " + e.getTemporada() + " Título: " + e.getTitulo()
                        + " Avaliação: " + e.getAvaliacao()));
    }

    private void pesquisarEpisodioPorTitulo() {
        if (episodios.isEmpty()) {
            System.out.println("Nenhum episódio encontrado. Realize a busca de uma série primeiro.");
            return;
        }

        System.out.print("Digite o trecho do título a ser pesquisado: ");
        var trechoTitulo = leitura.nextLine();

        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();

        if (episodioBuscado.isPresent()) {
            Episodio e = episodioBuscado.get();
            System.out.println("Episódio encontrado!");
            System.out.println("Temporada: " + e.getTemporada() + " Título: " + e.getTitulo());
        } else {
            System.out.println("Episódio não encontrado.");
        }
    }

    private void filtrarEpisodiosPorData() {
        if (episodios.isEmpty()) {
            System.out.println("Nenhum episódio encontrado. Realize a busca de uma série primeiro.");
            return;
        }

        try {
            System.out.print("A partir de que ano você deseja ver os episódios? ");
            var ano = leitura.nextInt();
            leitura.nextLine();

            LocalDate dataBusca = LocalDate.of(ano, 1, 1);

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            episodios.stream()
                    .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                    .forEach(e -> System.out.println("Temporada: " + e.getTemporada() + " Episódio: " + e.getTitulo()
                            + " Data lançamento: " + e.getDataLancamento().format(df)));
        } catch (Exception e) {
            System.out.println("Erro ao filtrar episódios por data: " + e.getMessage());
        }
    }

    private void exibirEstatisticas() {
        if (episodios.isEmpty()) {
            System.out.println("Nenhum episódio encontrado. Realize a busca de uma série primeiro.");
            return;
        }

        DoubleSummaryStatistics estatisticas = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("\n--- Estatísticas ---");
        System.out.println("Média: " + estatisticas.getAverage());
        System.out.println("Melhor Avaliação: " + estatisticas.getMax());
        System.out.println("Pior Avaliação: " + estatisticas.getMin());
        System.out.println("Quantidade de Episódios Avaliados: " + estatisticas.getCount());
    }
}

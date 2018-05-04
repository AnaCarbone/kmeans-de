import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class KMeansMaisInt {
	/**
	 * matriz de dados que o k-means receberá, cada linha um documento, cada
	 * coluna uma palavra
	 */
	private int[][] dados;

	/**
	 * quantidade k de prototipos passada
	 */
	private int k;
	
	/**
	 * matriz com os protótipos int[k][numeroDimensoes];
	 */
	private int[][] prototipos;

	/**
	 * matriz com a distância euclidiana entre os documentos e os protótipos
	 */
	private double[][] distanciasEuclidianas;

	/**
	 * matriz binaria de partição int[k][numeroLinhas]
	 */
	private int[][] matrizParticao;
	
	/**
	 * quantização do erro de agrupamento
	 */
	private double jcm;

	/**
	 * equivale ao numero de documentos
	 */
	private int numeroLinhas;
	
	/**
	 * equivale ao numero de palavras analisadas nos documentos
	 */
	private int numeroDimensoes;

	
	public KMeansMaisInt(String arquivo, int k, int linhas, int colunas) {
		try {
			BufferedReader leitor = new BufferedReader(new FileReader(arquivo));

			/*
			 * Para popular a matriz dados é necessário saber o número de linhas
			 * e o número de dimensões do corpus recebido
			 */
			numeroLinhas = linhas;
			numeroDimensoes = colunas;
			jcm = 0;
			this.k = k;


			/*
			 * Inicialização das matrizes
			 */
			dados = new int[numeroLinhas][numeroDimensoes];
			prototipos = new int[k][numeroDimensoes];
			distanciasEuclidianas = new double[numeroLinhas][k];
			matrizParticao = new int[k][numeroLinhas];

			/*
			 *  Leitura do arquivo e população da matriz de dados
			 */
			String linha = null;
			String[] numColunas = null;
			int i = 0;
			int j = 0;
			/*
			 * Lê a linha que contém o nome das palavras
			 */
			leitor.readLine();
			
			
			while ((linha = leitor.readLine()) != null) {
				numColunas = linha.split(",");
				for(int co = 1; co <= colunas; co++) {
					dados[i][j] = Integer.parseInt(numColunas[co]);
					j++;
				}
				j = 0;
				i++;
			}
			
			leitor.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * inicializa os protótipos
	 * @return
	 */
	public int[][] inicializarPrototipos() {
		
		Random rand = new Random();
		/*
		 * probabilidade dist²/soma-dist²
		 */
		double [] probabilidade = new double[numeroLinhas];
		/*
		 *  distâncias em relação ao centroide do momento
		 */
		double [] distancias = new double[numeroLinhas];
		double somaDistancias = 0;
		
		double soma = 0;
		int novoCentroide = 0;
		double maiorProbabilidade = 0;
		
		probabilidade = inicializarProbabilidades(probabilidade);
		int primeiroCentroide = rand.nextInt(numeroLinhas + 1);
		int cluster = 0;
		for(int palavra = 0; palavra<numeroDimensoes; palavra++){
			prototipos[cluster][palavra] = dados[primeiroCentroide][palavra];
		}
		cluster++;
		/*
		 * o documento ao qual o primeiro centroide se associou 
		 * terá sua probabilidade zerada para a escolha dos demais
		 */
		probabilidade[primeiroCentroide] = 0;
		novoCentroide = primeiroCentroide;
		while(cluster<k){
			// percorrer documentos
			for(int documentos = 0; documentos<numeroLinhas; documentos++){
				// percorrer palavras (dimensoes)
				for(int palavras = 0; palavras<numeroDimensoes; palavras++){
					// calcular distância euclidiana
					if(documentos != novoCentroide){
						soma = soma + (dados[novoCentroide][palavras] - dados[documentos][palavras])*
							(dados[novoCentroide][palavras] - dados[documentos][palavras]);
					}
					else{
						probabilidade[novoCentroide] = 0;
						distancias[documentos] = 0;
						// evitar a comparação do documento centroide com ele mesmo
						continue;
					}
				}
				distancias[documentos] = Math.sqrt(soma);
				somaDistancias = somaDistancias + (distancias[documentos]*distancias[documentos]);
			}
			// calcular as probabilidades
			for(int j = 0; j<distancias.length; j++){
				if(probabilidade[j] != 0){
					probabilidade[j] = (distancias[j]*distancias[j])/somaDistancias;
					if(j == 0){
						maiorProbabilidade = probabilidade[j];
						novoCentroide = j;
					}
					else if(probabilidade[j]>maiorProbabilidade){
						maiorProbabilidade = probabilidade[j];
						novoCentroide = j;
					}
				}
			}
			for(int dimensao = 0; dimensao<numeroDimensoes; dimensao++){
				prototipos[cluster][dimensao] = dados[novoCentroide][dimensao];
			}
			cluster++;
			probabilidade = inicializarProbabilidades(probabilidade);
		}

		return prototipos;
	}
	
	public double [] inicializarProbabilidades(double [] probabilidade){
		for(int i = 0; i<probabilidade.length; i++){
			if(probabilidade[i] != 0){
				probabilidade[i] = -1;
			}
		}
		return probabilidade;
	}
	
	/**
	 * Define a distância euclidiana entre todos os documentos com cada protótipo
	 */
	public void definirDistanciasEuclidianas() {
		double soma = 0;
		double diferencaQuadrado = 0;
		double valorDado = 0;
		double valorPrototipo = 0;
		/*
		 *  percorrendo prototipos
		 */
		for (int prototipo = 0; prototipo < k; prototipo++) {
			/*
			 *  percorrendo todos documentos para cada prototipo
			 */
			for (int documento = 0; documento < numeroLinhas; documento++) {
				/*
				 *  cálculo da distância para um documento
				 */
				for(int palavra = 0; palavra < numeroDimensoes; palavra++){
					valorDado = dados[documento][palavra];
					valorPrototipo = prototipos[prototipo][palavra];
					diferencaQuadrado = (valorDado - valorPrototipo) * (valorDado - valorPrototipo);
					soma = soma + diferencaQuadrado;
				}
				distanciasEuclidianas[documento][prototipo] = Math.sqrt(soma);
				soma = 0;
			}
		}
	}
	
	
	public void clustering() {
		inicializarMatrizParticao();
		double menorDistancia = 0;
		double atual = 0;
		int cluster = 0;
		for (int documento = 0; documento < numeroLinhas; documento++) {
			/*
			 *  Calcula qual o prototipo com menor distancia e assim define um
			 *  cluster para o dado
			 */
			for (int prototipo = 0; prototipo < k; prototipo++) {
				atual = distanciasEuclidianas[documento][prototipo];
				if (prototipo == 0) {
					menorDistancia = atual;
					cluster = prototipo;
				}
				if (atual < menorDistancia) {
					menorDistancia = atual;
					cluster = prototipo;
				}
			}
			/*
			 *  adiciona o valor um a matriz de particao no local marcando o
			 *  cluster a qual pertence
			 */
			matrizParticao[cluster][documento] = 1;
		}

	}
	
	/**
	 * zera matriz de partição
	 */
	private void inicializarMatrizParticao() {

		for (int i = 0; i < k; i++) {
			for (int j = 0; j < numeroLinhas; j++) {
				matrizParticao[i][j] = 0;
			}
		}
	}

	public double calcularJCM() {
		double jcmAtual = 0;
		for (int prototipo = 0; prototipo < k; prototipo++) {
			for (int documento = 0; documento < numeroLinhas; documento++) {

				jcmAtual = jcmAtual
						+ (matrizParticao[prototipo][documento] * (distanciasEuclidianas[documento][prototipo] * distanciasEuclidianas[documento][prototipo]));
			}
		}
		return jcmAtual;
	}
	
	public int [][] redefinirPrototipos() {
		int integrantes = 0; 
		for (int prototipo = 0; prototipo < k; prototipo++) {
			for (int documento = 0; documento < numeroLinhas; documento++) {
				if (matrizParticao[prototipo][documento] == 1) {
					if (integrantes == 0){
						inicializarPrototipo(prototipo);
					}
					integrantes++;
					for(int palavra = 0; palavra < numeroDimensoes; palavra++){
						prototipos[prototipo][palavra] = prototipos[prototipo][palavra] + dados[documento][palavra];
					}
					
				}
				
			}
			/*
			 *  evitar erro aritimético de divisão por 0				
			 */
			if(integrantes>0){
				for (int palavra = 0; palavra < numeroDimensoes; palavra++) {
					prototipos[prototipo][palavra] = (prototipos[prototipo][palavra]/integrantes);
				}
			}
			integrantes = 0;
		}
		return prototipos;
	}

	/** 
	 * zera matriz de prototipos
	 * @param prototipo
	 */
	private void inicializarPrototipo(int prototipo) {
		for (int j = 0; j < numeroDimensoes; j++) {
			prototipos[prototipo][j] = 0;
		}
	}
	
	/** 
	 * distância euclidiana entre os prototipos da iteração anterior com
	 * os da iteração atual para saber a movimentação que ocorreu
	 * @param prototiposAnterior
	 * @return
	 */
	public double diferencaPrototipos(int [][] prototiposAnterior) {
		double resposta = 0;
		double soma = 0;
		for(int prototipo = 0; prototipo<k; prototipo++){
			for(int palavra = 0; palavra<numeroDimensoes; palavra++){
				soma = soma + (prototipos[prototipo][palavra] - prototiposAnterior[prototipo][palavra]) * (prototipos[prototipo][palavra] - prototiposAnterior[prototipo][palavra]);
			}
			resposta = resposta + Math.sqrt(Math.abs(soma));
			
			soma = 0;
		}
		resposta = resposta/k;
		return resposta;
	}


	public int[][] getPrototipos() {
		return this.prototipos;
	}


	public int[][] getMatrizParticao() {
		return matrizParticao;
	}

}

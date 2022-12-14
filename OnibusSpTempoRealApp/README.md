# Teste Android     
## Requisitos   

Esses requisitos são obrigatórios e devem ser desenvolvidos para a entrega do teste

* **Posições dos veículos**: Exibir no mapa onde os veículos estavam na sua última atualização.( Sim, disponibilizei no mapa todos os ônibus de são paulo e a possibilidade de ver só da linha selecionada)

* **Linhas**: Exibir informações sobre as linhas de ônibus. (Sim)

* **Paradas**: Exibir os pontos de parada da cidade no mapa. (Não, acabei ficando atento a muito detalhes e acabou não dando tempo de fazer a parte das paradas de ônibus)

* **Previsão de chegada**: Dado uma parada informar a previsão de chegada de cada veículo que passe pela parada selecionada. (Não, mesmo motivo de cima)

* **Pesquisa e Filtros**: Permitir que o usuário pesquise e filtre esses dados, interagindo com a interface. (Sim, a interface esta dinâmica e o usuário tem a possibilidade de filtra os ônibus no mapa)


## Vídeo do Aplicativo desenvolvido em execução   


Para ver o vídeo é só clicar na imagem que abre o vídeo no youtube   


<div align="center">
  <a href="https://www.youtube.com/watch?v=VijbqPqyz6c&ab_channel=gabrielconti"><img src="https://i9.ytimg.com/vi_webp/VijbqPqyz6c/mq1.webp?sqp=CMjA55gG&rs=AOn4CLDv-RY0vO3rtE4V8z2Dq_5HtIgVdg" alt="IMAGE ALT TEXT"></a>
</div>


## Documentação 


Para desenvolver o aplicativo proposto no desafio, primeiro analisei a API, para entender melhor os dados que seriam necessários consumir, tive um pouco de dificuldades para lidar com a autenticação da API.    
Julguei necessário utilizar apenas 4 requisições da API para as funcionalidades do aplicativo, o (POST) da autenticação para conseguir o cookie de certificação, (GET) de todos os ônibus em circulação, (GET) de uma linha específica que o usuário pesquisar   
e (GET) de paradas de uma determinada linha. E para desenvolver o aplicativo utilizei Android nativo com Kotlin.  

Fiz a descrição de algumas funcionalidades do aplicativo:

**Como pesquisar uma linha especifica ?**     
Para pesquisar uma linha, é disponibilizado duas opções, clicar na barra de pesquisa na parte superior da tela, ou no botão flutuante de linhas favoritas com um ícone de estrela, embaixo da barra de pesquisa.   
* Barra de pesquisa -> Clicar em qualquer parte da barra de pesquisa, abre a tela para pesquisar a linha, na tela aberta, ao clicar na seta apontando a esquerda na parte superior a esquerda,   
	você volta para a tela principal. Para pesquisar é só digitar na caixa de texto na parte superior da tela, abaixo da caixa de texto vai ser carregado o resultado da sua pesquisa, com as linhas encontradas.     
* Botão flutuante de linhas favoritas -> Abre um popup menu abaixo do botão com o letreiro completo das linhas que você já salvou, para selecionar alguma é só clicar   
  


**Como dar refresh para ver a ultima atualização dos ônibus?**    
* Botão flutuante de refresh -> Na parte inferior da tela no canto direito, o botão verde com maior destaque, ao clicar atualiza a posição de todos os ônibus de são paulo     
* Chip da linha atual selecionada -> Quando tem algum linha específica selecionada esse chip fica visivel, e caso clique nele da um zoom no primeiro ônibus da lista de ônibus do estado da UI, e faz um refresh dos ônibus dessa linha para saber qual a posição atual deles   ]  
	Caso clique no icone de fechar do chip, é feito a remoção da seleção da linha atual, e volta a aparecer todos os ônibus de são paulo, inves da linha que estava selecionada  


**Como dar Zoom no mapa?**  
* Botão flutuante localizaçao atual -> Caso clicar no botão no canto inferior direito, acima do botão verde de refresh, ele sinaliza a posição atual do usuário no mapa caso ele tenha permitido o app ter acesso a sua localização atual,  
	caso usuario tenha recusado essa função não esta disponível.    
* Chip da quantidade de ônibus em circulação -> Na parte inferior da tela no canto esquerdo, o botão com um icone de ônibus e um valor a sua esquerda que representa a quantidade de ônibus na cidade.   
	Ao clicar nele vai da zoom no primeiro ônibus da lista de ônibus que está no estado da UI  


**Como salvar uma linha no favorito?**   
* Botão com o icone de estrela -> Na tela de pesquisa de linha, quando realizar a busca vai ter um icone de estrela ao final de cada linha, ao clicar nesse botão a linha é salva como favorita  

## Estruturação do projeto  

Para desenvolver o projeto optei por utilizar a arquitetura MVVM recomendada pelo google, a separação de pasta:   

data -> A fonte de dados do aplicativo, remoto e local com seus models.
di -> Para injeção de dependências necessárias no aplicativo com o Dagger Hilt
domain -> Repositórios remoto e local, para receber os pedidos do view Model e buscar os dados localmente ou na API, pelo projeto ser muito pequeno optei por não fazer interface paras os repositórios.
ui -> Tem todas as Atividades (activity) e Fragmentos (fragments), cada um em suas pastas, uma pasta para os Adaptadores(adapters) utilizados na ui, e a pasta do viewModel, como o projeto era bem simples e tinha só 2 fragments, optei por utilizar apenas um viewModel instanciado na viewModelStore da main activity e compartilhado com os outros dois fragmentos, para torna mais fácil a dinâmica do aplicativo.
util -> Todas as classes de utilidades dentro do projeto.

Retrofit : Utilizei o retrofit para consumir a API 
Room: Para salvar localmente as linhas favoritas 
Dagger Hilt: Para implementação de dependências
Google Map: Utilizei o sdk do maps para ter acesso ao maps do google
LifeCycle: Para utilizar os componentes do lifeCycle do android como por exemplo, viewModels, StateFlow, liveData, coroutines, etc.   



  
## Como rodar no seu computador  

Para rodar a aplicação é necessário ter o android studio no computador, após isso é só criar um clone desse projeto no seu pc  

E na hora de rodar o aplicativo, você vai precisar fazer duas mudanças  

* No arquivo local.properties do projeto, você vai colocar o valor da sua API key do google maps como exemplificado abaixo.      
MAPS_API_KEY=(SUA API KEY AQUI DENTRO)	

* No arquivo gradle.properties do projeto, você vai colocar a sua API key da API olho vivo como exemplificado abaixo.   
API_KEY=("SUA KEY AQUI LEMBRE QUE AQUI É NECESSÁRIO COLOCAR SUA API DENTRO DESSAS ASPAS")

Após essas duas mudanças o aplicativo está pronto para rodar no seu computador  

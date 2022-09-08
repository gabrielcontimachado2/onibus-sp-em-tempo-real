# Ônibus SP em tempo real   

  Aplicativo para Android, que disponibiliza os ônibus de São Paulo em tempo real no mapa, consumindo a API do OLHO VIVO, o aplicativo ainda não está finalizado,       
então com o tempo irei atualizando o projeto nesse repositório com as novas funcionalidades.   

## Linguagem, arquitetura e bibliotecas utilizadas para desenvolver o projeto    
• Kotlin    
• Android Jetpack     
• Dagger Hilt    
• Retrofit     
• Room     
• Firebase (Analytics and Crashlytics)    
• Arquitetura MVVM    
• Material Design  
• Google Maps    
 
## Gifs do projeto finalizado
Tela principal sem filtros ativo | Filtro ativo | Favoritar uma linha | Posição atual  
--- | --- | --- | ---   
![Alt Text](https://media2.giphy.com/media/Z0RVn0j9qcft2dLSJu/giphy.gif?cid=790b7611c8a62ea7a7720b2348be924831765e4ed853243f&rid=giphy.gif&ct=g) | ![Alt Text](https://media3.giphy.com/media/6a4PlPZUFaczpXT42j/giphy.gif?cid=790b761143504bb5dbb4cb2c8106e3ed57a7d92b5f3a14ae&rid=giphy.gif&ct=g) | ![Alt Text](https://media1.giphy.com/media/qvGVp7Ffq1Ak8GFGaO/giphy.gif?cid=790b76112528bb0b84a05337f43541398b2bec13996ed813&rid=giphy.gif&ct=g) | ![Alt Text](https://media4.giphy.com/media/QzEqs3hIUg9gLN2aTc/giphy.gif?cid=790b76118ab70fcdebd8f54bc607668d63f08f045a938e9e&rid=giphy.gif&ct=g)




## Implementações futuras   
• Ao pesquisar as linhas, além de mostrar os ônibus dessa linha também vai ser disponibilizado no mapa o ícone das paradas que essa linha passa.     
• Para complementar a funcionalidade acima, ao clicar no ícone da parada, vai ser aberto uma nova tela, com algumas informações da linha, e previsão dos próximos ônibus que irão passar naquela parada.      
• Adicionar a possibilidade de criar uma rota do ponto que você está no mapa, até uma parada de ônibus que você deseja chegar, ou calcular sozinho a rota com a parada de ônibus mais próxima a você.     
• Ao pesquisar uma linha, em conjunto com os ônibus dessa linha, traçar também uma rota no mapa, da origem ao destino, para você saber certinho o caminho daquela linha.     
• Colocar algumas opções de configurações de preferencias para o usuário, não pensei em todas ainda, vai ser coisas simples, como tema do app, estilo do mapa etc.  Além do manual de como usar o aplicativo, com legendas sobre as funcionalidades.    
• Melhorar a visualização da posição atual, adicionar um círculo em volta do ícone do usuário.    
• Adicionar uma Splash Screen e o ícone, para aplicativo ficar mais elegante.     

## Problemas conhecidos até o momento:      
• A quantidade total de ônibus apresentada no chip no canto inferior a esquerda, não está correta quando a busca está sem filtro de linha, caso tenha ativo um filtro, é aprestado o valor correto.       
• Mesmo utilizando o cluster de marker do google maps, as vezes o app ainda tem problemas de otimização, demora um pouco para agrupar os ônibus nos círculos.  






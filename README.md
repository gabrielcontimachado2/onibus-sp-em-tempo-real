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

Home (Filters) | Edit Task Screen | Create and Delete Task | Categories Manager
--- | --- | --- | ---
![Alt Text](https://media2.giphy.com/media/ooK7DOydVl68DisaWb/giphy.gif?cid=790b7611cfca9da8fc008b59848108583e4b59a6d5442f9b&rid=giphy.gif&ct=g) | ![Alt Text](https://media4.giphy.com/media/JYCgOpLU21wbbdRId1/giphy.gif?cid=790b761111984e7640ef2221e844740900fb644b328e483d&rid=giphy.gif&ct=g) | ![Alt Text](https://media2.giphy.com/media/d5uBPsDa3jwNgEYa8j/giphy.gif?cid=790b7611a3a74adb3b1a7432d3e0a3fb803863622159c4df&rid=giphy.gif&ct=g) | ![Alt Text](https://media2.giphy.com/media/KVHy2SmwTm8fuSTyOV/giphy.gif?cid=790b7611f0986431d25eb14ca39893fc7fb553722953b253&rid=giphy.gif&ct=g)


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




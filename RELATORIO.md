# Projeto CMUApp

**Aluno:** Vasco André Pinto Oliveira Coelho  
**Nºmecanográfico:** 8210188  
**Curso:** Licenciatura em Engenharia Informática  
**Disciplina:** Computação Móvel e Ubíqua  
**Data de entrega:** 07/09/2025  
---

# 1. Descrição do Projeto

O presente projeto foi desenvolvido no âmbito da unidade curricular **Computação Móvel e Ubíqua**, com o objetivo de consolidar os conhecimentos adquiridos em **Android Studio**, **Kotlin** e **Jetpack Compose**.  

O principal objetivo consistiu na criação de uma aplicação móvel capaz de **fornecer informações sobre estabelecimentos do tipo café e pastelaria**, permitindo aos utilizadores interagirem de forma intuitiva e segura. A aplicação oferece as seguintes funcionalidades principais:  

## Funcionalidades

1. **Registo de utilizadores** – possibilita a criação de contas para que cada utilizador possa aceder às funcionalidades da aplicação.  
2. **Localização de estabelecimentos próximos** – permite consultar os cafés e pastelarias mais próximos da posição atual do utilizador.  
3. **Avaliação de estabelecimentos** – os utilizadores podem submeter avaliações apenas se se encontrarem a **menos de 50 metros** do estabelecimento e não tiverem realizado outra avaliação ao mesmo local nos últimos **30 minutos**, garantindo a autenticidade e a precisão das avaliações.  
4. **Leaderboard de estabelecimentos** – apresenta uma classificação dos estabelecimentos com base nas avaliações dos utilizadores.  
5. **Histórico de visitas** – mantém um registo dos estabelecimentos já visitados pelo utilizador.  
6. **Consultas às avaliações de outros utilizadores** – cada utilizador pode consultar as reviews submetidas por outros, promovendo a partilha de experiências.  
7. **Notificações geolocalizadas** – caso o utilizador passe a menos de 50 metros de um estabelecimento, a aplicação envia notificações entre as **16h e as 18h**, com a opção de ativar ou desativar esta funcionalidade.  
8. **Página de detalhes do estabelecimento** – apresenta a imagem, nome, morada e rating do estabelecimento, assim como um **componente de mapa** que mostra a sua localização exata.  

A aplicação foi projetada de forma a proporcionar uma **experiência de utilização prática, segura e interativa**, ao mesmo tempo que permite explorar funcionalidades avançadas do ecossistema Android e da linguagem Kotlin, consolidando competências em desenvolvimento de aplicações móveis modernas.

# 2. Funcionalidades Implementadas

A aplicação implementa as seguintes funcionalidades:  

## 1. Registo de utilizadores
Permite a criação de contas de utilizador para que cada pessoa possa aceder às funcionalidades da aplicação de forma personalizada e segura.  

## 2. Localização de estabelecimentos próximos
Mostra os cafés e pastelarias mais próximos da localização atual do utilizador, utilizando serviços de geolocalização para apresentar resultados precisos.  

## 3. Avaliação de estabelecimentos
Os utilizadores podem submeter avaliações apenas se se encontrarem a menos de **50 metros** do estabelecimento e não tiverem avaliado o mesmo estabelecimento nos últimos **30 minutos**, garantindo avaliações confiáveis e atualizadas. As avaliações devem conter obrigatoriamente os campos "Descrição" e "Rating" e podem conter opcionalmente os campos "imagem", "áudio", e "nome da doçaria".

## 4. Leaderboard de estabelecimentos
Exibe uma classificação dos estabelecimentos com base nas avaliações recebidas dos utilizadores, permitindo identificar os mais bem avaliados. Tem também a opção de visualizar a leaderboard com base nos ratings da google.

## 5. Histórico de visitas
Regista os estabelecimentos já visitados pelo utilizador, possibilitando o acesso rápido ao seu histórico de interações e avaliações.  

## 6. Consultas às avaliações de outros utilizadores
Permite que cada utilizador veja as reviews de outros utilizadores a um determinado establishment.

## 7. Histórico de avaliações
Permite que cada utilizador veja o seu histórico de reviews.

## 8. Notificações geolocalizadas
Envia notificações ao utilizador entre as **16h e as 18h** sempre que este passe a menos de 50 metros de um estabelecimento, com a opção de ativar ou desativar esta funcionalidade. Caso o utilizador passe perto do mesmo estabelecimento mais do que uma vez entre as 16h e as 18h, a aplicação não voltará a enviar a notificação. 

## 9. Página de detalhes do estabelecimento
Apresenta informações detalhadas sobre cada estabelecimento, incluindo **imagem, nome, morada, rating** e um **mapa interativo** que indica a localização exata do estabelecimento.

# 3. Decisões de Implementação

Para o desenvolvimento desta aplicação foram tomadas as seguintes decisões de implementação:

## 1. Room
Foi utilizado o **Room** como base de dados local para permitir que os dados fossem armazenados em cache quando o utilizador se encontrava **offline**. Isto garante que a aplicação continue funcional mesmo sem acesso à internet.

## 2. Firebase Firestore e Storage
O **Firebase Firestore** foi escolhido para armazenar os dados da aplicação de forma escalável e segura. O **Firebase Storage** foi utilizado para guardar imagens e áudios associados aos estabelecimentos e às avaliações, facilitando o acesso e a gestão de ficheiros multimédia.

## 3. Jetpack Compose
A interface da aplicação foi desenvolvida com **Jetpack Compose**, permitindo a criação de UI de forma declarativa, mais simples e com menor quantidade de código, além de melhorar a manutenção e a escalabilidade da interface.

## 4. Hilt
O **Hilt** foi usado para a **injeção de dependências**, facilitando a gestão de objetos e serviços em toda a aplicação. Esta abordagem permite um código mais modular, testável e desacoplado, reduzindo a complexidade e evitando a criação manual de instâncias de objetos em diferentes partes do código.

## 5. Retrofit
O **Retrofit** foi utilizado para realizar chamadas HTTP à API do Google Places, permitindo a obtenção de dados de estabelecimentos próximos de forma eficiente e segura.

## 6. Arquitetura MVVM
A aplicação segue a arquitetura **MVVM (Model-View-ViewModel)**, separando a lógica de apresentação, lógica de negócios e dados. Esta abordagem melhora a organização do código, facilita a manutenção e permite testes mais eficientes.

## 7. API do Google Places
Foi integrada a **API do Google Places** para localizar estabelecimentos próximos ao utilizador, fornecendo dados precisos e atualizados sobre cafés e pastelarias na área.

## 8. Geofence
O recurso **Geofence** foi utilizado para implementar notificações e restrições baseadas na localização do utilizador, como a submissão de avaliações apenas quando o utilizador se encontra a menos de 50 metros do estabelecimento.


# 4. Resultados Obtidos

Este capítulo apresenta os resultados da aplicação, incluindo funcionalidades implementadas e possíveis limitações observadas.

## 4.1 Funcionalidades Implementadas

As seguintes funcionalidades foram implementadas na aplicação:

- **Registo de Utilizadores:** Criação de conta e autenticação.
- **Consulta de Estabelecimentos Próximos:** Retorna cafés e pastelarias próximos usando a Google Places API, podendo ser visualizado em componentes do tipo Mapa e em componentes do tipo Lista.
- **Avaliação de Estabelecimentos:** Submissão de avaliações restrita a utilizadores a menos de 50 metros do estabelecimento e sem avaliações recentes no mesmo local.
- **Leaderboard:** Exibe os estabelecimentos com melhores avaliações.
- **Histórico de Estabelecimentos Visitados:** Mostra todos os estabelecimentos visitados pelo utilizador.
- **Visualização de Reviews:** Permite consultar avaliações de outros utilizadores.
- **Notificações Geofence:** Notificações enviadas quando o utilizador passa a menos de 50 metros de um estabelecimento entre as 16h e 18h.
- **Página de Detalhes do Estabelecimento:** Exibe imagem, nome, morada, rating e localização no mapa.
- **Cache Offline com Room:** Permite aceder a dados mesmo sem ligação à internet.
- **Gravação de Áudio nas Reviews:** Permite ao utilizador gravar um áudio na sua avaliação.
- **Adaptação a pouca bateria/modo poupança de energia:** Caso o utilizador se encontre com pouca bateria ou em modo de poupança de energia, as notificações não serão enviadas.

## 4.2 Possíveis Limitações ou Bugs Observados

- Carregamento de imagens pode ser lento dependendo da conexão à internet.
- Em alguns dispositivos, a detecção de localização pode apresentar atrasos, afetando a submissão de avaliações via Geofence.

# 5. Conclusão

Este projeto permitiu consolidar diversos conhecimentos adquiridos na unidade curricular de Computação Móvel e Ubíqua, assim como ganhar experiência prática no desenvolvimento de aplicações Android modernas.

## 5.1 Aprendizagens Adquiridas

- Desenvolvimento de aplicações mobile usando **Kotlin** e **Jetpack Compose**.
- Implementação da arquitetura **MVVM**, promovendo separação de responsabilidades e organização do código.
- Integração com serviços externos como **Firebase Firestore**, **Firebase Storage** e **Google Places API**.
- Uso de **Hilt** para gestão de dependências e injeção de dependências em Android.
- Gestão de cache offline usando **Room**, garantindo que a aplicação funcione mesmo sem ligação à internet.
- Implementação de **Geofences** e notificações contextuais.
- Consumo de APIs externas com **Retrofit**.

## 5.2 Pontos Fortes

- Aplicação funcional, com várias funcionalidades implementadas conforme o enunciado.
- Integração com serviços externos e uso de cache offline.
- Estrutura de código organizada com MVVM e injeção de dependências.

# 6. Documentação

Toda a documentação do projeto foi gerada usando **Dokka**, uma ferramenta de documentação para projetos Kotlin/Android.  

Para gerar a documentação localmente, basta executar o seguinte comando no terminal, na pasta raiz do projeto:

```bash
./gradlew dokkaHtml


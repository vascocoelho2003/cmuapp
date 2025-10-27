# CMUApp

Aplicação móvel desenvolvida no âmbito da unidade curricular **Computação Móvel e Ubíqua** (CMU), com o objetivo de fornecer informações sobre estabelecimentos do tipo café e pastelaria, permitindo aos utilizadores interagir com avaliações, leaderboard, histórico de visitas e notificações geolocalizadas.

---

## Funcionalidades

- **Registo de utilizadores:** Criação de contas e autenticação segura.  
- **Localização de estabelecimentos próximos:** Consulta cafés e pastelarias próximos do utilizador usando a **Google Places API**.  
- **Avaliação de estabelecimentos:** Submissão restrita a utilizadores a menos de 50 metros do estabelecimento e sem avaliações recentes no mesmo local. Permite adicionar texto, rating, imagem e áudio.  
- **Leaderboard:** Exibe os estabelecimentos com melhores avaliações.  
- **Histórico de visitas:** Mantém o registo dos estabelecimentos já visitados pelo utilizador.  
- **Consultas às avaliações de outros utilizadores:** Visualiza reviews de outros utilizadores.  
- **Notificações geolocalizadas:** Envia alertas quando o utilizador passa perto de um estabelecimento entre as 16h e 18h (opcional).  
- **Página de detalhes do estabelecimento:** Mostra imagem, nome, morada, rating e localização no mapa.  
- **Cache offline com Room:** Acesso a dados mesmo sem ligação à internet.  

---

## Tecnologias Utilizadas

- **Kotlin**  
- **Jetpack Compose**  
- **MVVM** (Model-View-ViewModel)  
- **Room** (base de dados local)  
- **Firebase Firestore** (armazenamento de dados)  
- **Firebase Storage** (armazenamento de imagens e áudios)  
- **Hilt** (injeção de dependências)  
- **Retrofit** (chamadas HTTP à Google Places API)  
- **Google Places API**  
- **Geofence** (notificações baseadas na localização)

---

## Como Executar
  
1. Clonar o repositório:  
```bash
git clone https://gitlab.com/vascotzandre2003/cmuapp.git
``` 
cd cmuapp  <br>
2. Abrir o projeto no Android Studio  
3. Executar a aplicação num emulador ou dispositivo Android  

## Dependências Externas

- Firebase (Auth, Firestore e Storage)
- Google Places API
- Hilt
- Retrofit

## Documentação
A documentação do projeto foi gerada com Dokka.
Para gerar localmente, executar:

```
./gradlew dokkaHtml
```

A documentação resultante estará disponível em app/build/dokka/html.

## Conclusão

Este projeto permitiu consolidar conhecimentos em **Android Studio**, **Kotlin** e **Jetpack Compose**, assim como integrar serviços externos e implementar funcionalidades avançadas de geolocalização, cache offline e notificações contextuais.

---

## Autor

**Vasco André Pinto Oliveira Coelho**  
**Nº Mecanográfico:** 8210188  
**Curso:** Licenciatura em Engenharia Informática

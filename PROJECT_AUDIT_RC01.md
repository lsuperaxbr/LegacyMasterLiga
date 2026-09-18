# PROJECT AUDIT RC-01 — Estabilização da base

Data da auditoria: 2026-08-03  
Escopo: inspeção estática da base atual, dos relatórios de regressão e dos resultados de testes disponíveis.  
Regra desta etapa: nenhuma funcionalidade ou correção foi implementada.

## 1. Parecer executivo

**A base atual ainda não pode ser classificada como Release Candidate.**

O projeto contém uma aplicação local ampla e uma primeira camada de sincronização online, mas ainda não existe evidência reproduzível de que o fluxo crítico da Copa, a autenticação online, a migração completa do banco e a sincronização entre aparelhos funcionem juntos sem regressão. A recuperação posterior à reprovação da ONLINE-006 está presente no código, porém permanece como **candidata não aprovada**: o teste físico offline, o login com conta real, a suíte completa e a geração de um novo APK não foram concluídos.

Bloqueadores principais:

1. regressões funcionais graves reportadas em aparelho e ainda sem revalidação física completa;
2. dois fluxos concorrentes de autenticação online, com semânticas diferentes;
3. ciclo de vida incompleto dos listeners e da retomada de sincronização após reinício;
4. divergência de versão do Room entre banco, backup e diagnóstico Firebase;
5. regras/configuração Firebase dependentes de publicação e validação externas;
6. cobertura incompleta de migrações e de integração;
7. responsabilidades do motor de competição divididas entre `Schedule` e `Results`;
8. nenhuma evidência atual de build RC completo com APK correspondente ao código auditado.

## 2. Método e limites

Foram auditados:

- 1 módulo Gradle (`:app`);
- 223 arquivos Kotlin de produção, aproximadamente 18.671 linhas;
- 15 arquivos Kotlin de teste;
- banco Room versão 19, suas migrations e schemas exportados;
- repositories, ViewModels, DAOs, entidades, use cases e módulos Hilt;
- regras Firestore e caminhos de Auth/Firestore;
- listeners Firebase e observadores Room;
- relatório de recuperação da ONLINE-006 e resultados de teste existentes.

Não foram executados nesta auditoria:

- teste físico em emulador/aparelho;
- login com conta Firebase real;
- inspeção do Console Firebase;
- publicação de regras;
- alteração de código;
- remoção de código morto.

Consequentemente, achados externos como “provedor E-mail/Senha ativado” e “regras publicadas” permanecem **não verificáveis somente pelo repositório**.

---

# FASE 1 — INVENTÁRIO

## 3. Módulos e organização

### 3.1 Módulo Gradle

- `:app` — único módulo compilável.

Não existem módulos Gradle separados para domínio, dados, Firebase ou features. A separação é apenas por pacotes dentro de `:app`, portanto o compilador não impõe os limites da Clean Architecture.

### 3.2 Pacotes estruturais

- `core`: cache, database, DI, error, model, navigation, network, performance, security, session, storage e UI compartilhada;
- `data`: implementações Room dos repositories gerais;
- `domain`: modelos, repositories gerais e use cases;
- `ui`: tema e componentes de aplicação;
- `feature`: funcionalidades da aplicação.

### 3.3 Features existentes

`audit`, `backup`, `betaguide`, `closure`, `clubprofile`, `clubs`, `common`, `competitions`, `dashboard`, `finance`, `halloffame`, `history`, `login`, `market`, `news`, `notifications`, `online`, `participants`, `performance`, `reports`, `results`, `schedule`, `settings`, `statistics` e `users`.

## 4. Injeção de dependências

Módulos Hilt:

- `CloudModule` — fornece `FirebaseAuth` e `FirebaseFirestore` singleton;
- `DatabaseModule` — fornece `AppDatabase`, DAOs e migrations;
- `RepositoryModule` — vincula interfaces a implementações Room/Firestore e `AuditLogger`.

## 5. Repositories

Foram identificados 24 contratos e 24 implementações principais.

| Domínio/feature | Contrato | Implementação |
|---|---|---|
| Autenticação local | `AuthRepository` | `RoomAuthRepository` |
| Usuários | `UserRepository` | `RoomUserRepository` |
| Ligas | `LeagueRepository` | `RoomLeagueRepository` |
| Clubes | `ClubRepository` | `RoomClubRepository` |
| Auditoria | `AuditRepository` | `RoomAuditRepository` |
| Backup | `BackupRepository` | `RoomBackupRepository` |
| Encerramento | `SeasonClosureRepository` | `RoomSeasonClosureRepository` |
| Perfil de clube | `ClubProfileRepository` | `RoomClubProfileRepository` |
| Competições | `CompetitionRepository` | `RoomCompetitionRepository` |
| Dashboard | `DashboardRepository` | `RoomDashboardRepository` |
| Finanças | `FinanceRepository` | `RoomFinanceRepository` |
| Hall da Fama | `HallOfFameRepository` | `RoomHallOfFameRepository` |
| Histórico | `HistoryRepository` | `RoomHistoryRepository` |
| Notícias | `NewsRepository` | `RoomNewsRepository` |
| Notificações | `NotificationRepository` | `RoomNotificationRepository` |
| Participantes | `ParticipantRepository` | `RoomParticipantRepository` |
| Relatórios | `ReportRepository` | `RoomReportRepository` |
| Resultados | `ResultsRepository` | `RoomResultsRepository` |
| Calendário | `ScheduleRepository` | `RoomScheduleRepository` |
| Configurações | `SettingsRepository` | `RoomSettingsRepository` |
| Estatísticas | `StatisticsRepository` | `RoomStatisticsRepository` |
| Auth online | `CloudAuthRepository` | `FirestoreCloudAuthRepository` |
| Liga online | `CloudLeagueRepository` | `FirestoreCloudLeagueRepository` |
| Sincronização esportiva | sem contrato | `OnlineSportsSyncManager` |

Observação: `OnlineSportsSyncManager` é uma implementação concreta singleton, sem interface, e concentra banco, fila, conflito, mapeamento, upload, download e listeners.

## 6. ViewModels

Foram identificadas 26 ViewModels:

- `RootViewModel`;
- `AppThemeViewModel`;
- `AuditViewModel`;
- `BackupViewModel`;
- `SeasonClosureViewModel`;
- `ClubProfileViewModel`;
- `ClubsViewModel`;
- `CompetitionsViewModel`;
- `DashboardViewModel`;
- `FinanceViewModel`;
- `HallOfFameViewModel`;
- `HistoryViewModel`;
- `LoginViewModel`;
- `MarketViewModel`;
- `NewsViewModel`;
- `NotificationsViewModel`;
- `OnlineAuthViewModel`;
- `CloudLeagueViewModel`;
- `ParticipantsViewModel`;
- `PerformanceViewModel`;
- `ReportsViewModel`;
- `StandingsViewModel`;
- `ScheduleViewModel`;
- `SettingsViewModel`;
- `StatisticsViewModel`;
- `UsersViewModel`.

## 7. Entidades e tabelas Room

O `AppDatabase` está na versão **19**, com 24 entidades/tabelas:

| Entidade | Tabela |
|---|---|
| `UserEntity` | `users` |
| `SessionEntity` | `sessions` |
| `LeagueEntity` | `leagues` |
| `ClubEntity` | `clubs` |
| `CompetitionEntity` | `competitions` |
| `CompetitionParticipantEntity` | `competition_participants` |
| `SeasonEntity` | `seasons` |
| `RoundEntity` | `rounds` |
| `MatchEntity` | `matches` |
| `StandingEntity` | `standings` |
| `FinancialTransactionEntity` | `financial_transactions` |
| `TransferEntity` | `transfers` |
| `NewsEntity` | `news` |
| `AuditLogEntity` | `audit_logs` |
| `AppSettingsEntity` | `app_settings` |
| `CompetitionSettingsEntity` | `competition_settings` |
| `SeasonClosureEntity` | `season_closures` |
| `FinalStandingEntity` | `final_standings` |
| `CompetitionPrizeEntity` | `competition_prizes` |
| `PrizeHistoryEntity` | `prize_history` |
| `NotificationEntity` | `notifications` |
| `NotificationReadEntity` | `notification_reads` |
| `OnlineSyncRecordEntity` | `online_sync_records` |
| `OnlineSyncQueueEntity` | `online_sync_queue` |

## 8. DAOs

Foram identificados 24 DAOs:

`UserDao`, `SessionDao`, `LeagueDao`, `ClubDao`, `CompetitionDao`, `CompetitionParticipantDao`, `SeasonDao`, `RoundDao`, `MatchDao`, `StandingDao`, `FinancialDao`, `TransferDao`, `NewsDao`, `AuditLogDao`, `SettingsDao`, `HistoryDao`, `HallOfFameDao`, `SeasonClosureDao`, `PrizeDao`, `NotificationDao`, `ReportDao`, `StatisticsDao`, `DashboardDao` e `OnlineSyncDao`.

## 9. Use cases

Existem somente quatro use cases explícitos:

- `InitializeDefaultDataUseCase`;
- `InitializeDemoDataUseCase`;
- `LoginUseCase`;
- `LogoutUseCase`.

A maior parte das ViewModels acessa repositories diretamente. Algumas também acessam serviços de sessão ou DAOs, o que torna a aplicação da camada de domínio inconsistente.

## 10. Migrations Room

Há uma cadeia declarada contínua de `1→2` até `18→19`:

| Migration | Alteração principal |
|---|---|
| 1→2 | cria finanças, transferências e notícias |
| 2→3 | cria participantes de competição |
| 3→4 | cria rodadas e partidas, incluindo unicidade de rodada e confronto/perna |
| 4→5 | cria classificação |
| 5→6 | amplia transações financeiras |
| 6→7 | recria notícias com vínculos e chave de deduplicação |
| 7→8 | cria logs de auditoria |
| 8→9 | cria configurações do app e de competição |
| 9→10 | cria encerramento de temporada e classificação final |
| 10→11 | cria premiações e histórico de prêmios |
| 11→12 | cria notificações e leituras |
| 12→13 | amplia transferências |
| 13→14 | adiciona grupos, pernas e identificação inicial de fases |
| 14→15 | adiciona chaveamento, pênaltis, vencedor e rótulo de fase |
| 15→16 | adiciona `firebaseUid` ao usuário |
| 16→17 | adiciona origem de login à sessão |
| 17→18 | adiciona vínculo de Liga com a nuvem |
| 18→19 | cria mapeamentos e fila durável da ONLINE-006 |

Schemas exportados presentes: versões 6, 7 e 12 a 19.  
Schemas ausentes no repositório: versões 1 a 5 e 8 a 11.

Isso impede testar de forma padronizada todas as rotas históricas de upgrade com `MigrationTestHelper` e reduz a confiança em instalações antigas.

### Divergência crítica de versão

- `AppDatabase`: versão 19;
- `RoomBackupRepository.DATABASE_VERSION`: versão 14;
- `FirebaseConnectionChecker`: registra versão 15 como “atual estável”.

O backup pode rotular incorretamente pacotes atuais e executar decisões de compatibilidade usando uma versão cinco níveis atrasada. O diagnóstico Firebase também publica metadado incorreto.

## 11. Firebase e Firestore

### 11.1 Componentes

- Firebase Auth fornecido por `CloudModule`;
- Firestore fornecido por `CloudModule`;
- `FirestoreCloudAuthRepository`;
- `FirestoreCloudLeagueRepository`;
- `OnlineSportsSyncManager`;
- `FirebaseErrorMapper`;
- `FirebaseConnectionChecker`;
- regras em `firestore.rules`.

### 11.2 Coleções conhecidas

Top-level:

- `user_profiles/{uid}`;
- `leagues/{leagueId}`;
- `invites/{code}`;
- `system_status/{document}`.

Subcoleções de Liga:

- `leagues/{leagueId}/members/{uid}`;
- `competitions/{cloudId}`;
- `seasons/{cloudId}`;
- `participants/{cloudId}`;
- `rounds/{cloudId}`;
- `matches/{cloudId}`;
- `standings/{cloudId}`.

Não existe coleção nem mapeamento de sincronização de finanças. Finanças continuam exclusivamente locais.

## 12. Listeners e observadores ativos

### 12.1 Firebase Auth/perfil

`FirestoreCloudAuthRepository.observeOnlineProfile()` registra:

- um `FirebaseAuth.AuthStateListener`;
- um listener de documento em `user_profiles/{uid}` quando autenticado.

O listener anterior do perfil é removido na troca de usuário; ambos são removidos em `awaitClose`.

### 12.2 Liga online

`FirestoreCloudLeagueRepository` registra:

- um listener do documento da Liga;
- um listener da subcoleção `members`.

Cada Flow remove seu listener em `awaitClose`. Porém `CloudLeagueViewModel.startObserving()` pode iniciar novas coletas repetidamente sem cancelar as anteriores.

### 12.3 Sincronização esportiva

`OnlineSportsSyncManager` registra, após `startAuthenticatedSession()`:

- um Auth state listener singleton;
- um callback `addSnapshotsInSyncListener`;
- observador Room de Ligas online;
- observador Room de candidatos de sincronização;
- até seis listeners Firestore por Liga habilitada: competições, temporadas, participantes, rodadas, partidas e classificação.

Os seis listeners por Liga são guardados e removidos ao desabilitar a sessão. O Auth listener e o callback global de snapshots não têm seus registros guardados para remoção; sobrevivem durante a vida do singleton/processo.

### 12.4 Observadores Room

DAOs expõem `Flow` para sessão, Ligas, competições, calendário, classificação, finanças, notícias, notificações, fila online e demais telas. Em geral, as coletas estão ligadas a `viewModelScope` e `stateIn`, portanto são encerradas com a ViewModel. A exceção arquitetural é o escopo próprio e permanente do singleton `OnlineSportsSyncManager`.

---

# FASE 2 — MAPA DE DEPENDÊNCIAS

## 13. Cadeia funcional principal

```text
LoginScreen
  ↓
LoginViewModel
  ├─ LOCAL → LoginUseCase → AuthRepository → RoomAuthRepository
  │                                      ↓
  │                               RoomSessionManager
  │                                      ↓
  │                              sessions + users (Room)
  │
  └─ ONLINE → CloudAuthRepository → FirebaseAuth
                                      ↓
                             user_profiles (Firestore)
                                      ↓
                              vínculo UserEntity
                                      ↓
                           sessão ONLINE no Room
                                      ↓
                         OnlineSportsSyncManager
```

Dependência de dados esportivos:

```text
Sessão válida
  ↓
Liga (leagues)
  ↓
Competição (competitions)
  ↓
Temporada (seasons)
  ├─ Participantes (competition_participants)
  ↓
Rodadas (rounds)
  ↓
Partidas (matches)
  ↓
Classificação (standings)
  ↓
Encerramento / histórico / prêmios / notícias / finanças
```

Essa ordem é conceitual. No código, repositories consultam múltiplos DAOs diretamente e não há uma camada de orquestração única que imponha a sequência.

## 14. Cadeia Firebase real

O mapa solicitado não é uma linha única no código; existem dois ramos:

```text
Firebase SDK
  ├─ FirebaseAuth
  │      ↓
  │  CloudAuthRepository
  │      ↓
  │  FirestoreCloudAuthRepository
  │      ├─ FirebaseAuth
  │      ├─ user_profiles (Firestore)
  │      └─ UserDao (Room)
  │
  └─ FirebaseFirestore
         ├─ CloudLeagueRepository → FirestoreCloudLeagueRepository
         │      ├─ leagues / members / invites
         │      └─ LeagueDao (Room)
         │
         └─ OnlineSportsSyncManager
                ├─ seis coleções esportivas
                ├─ nove DAOs esportivos/online
                └─ fila + resolução de conflitos
```

`CloudLeagueRepository` não está tecnicamente abaixo de `CloudAuthRepository`; ambos dependem diretamente dos SDKs Firebase e misturam persistência remota com espelhamento local.

## 15. Ciclos e dependências cruzadas

### 15.1 Ciclos de injeção

Não foi identificado ciclo direto de construtores Hilt que impeça a criação do grafo.

### 15.2 Ciclo lógico Schedule ↔ Results

- `RoomScheduleRepository` injeta `ResultsRepository` para reconstruir classificação após gerar calendário;
- `RoomCompetitionRepository` também injeta `ResultsRepository`;
- `RoomResultsRepository` contém a geração das próximas rodadas de mata-mata e da Final;
- `ScheduleViewModel` e `StandingsViewModel` dependem simultaneamente de `ScheduleRepository` e `ResultsRepository`.

Não é ciclo de DI, mas é um ciclo de responsabilidade: calendário gera resultados derivados e resultados geram calendário futuro. Isso dificulta garantir idempotência e favorece regressões na Copa.

### 15.3 Sessão acoplada ao Firebase

O gerenciador de sessão local também executa logout Firebase. O núcleo de sessão deixa de ser estritamente local e passa a conhecer infraestrutura cloud.

### 15.4 Repositories cloud acoplados ao Room

Os repositories Firestore gravam diretamente DAOs locais. Isso mistura autenticação/transportes remotos, espelhamento e domínio em uma única classe.

### 15.5 ViewModels atravessando camadas

Exemplos:

- `LoginViewModel` usa use case, repository cloud, `UserDao`, `SessionManager` e o gerenciador de sync;
- `OnlineAuthViewModel` usa diretamente `SessionManager` e repository cloud;
- `CloudLeagueViewModel` usa repository cloud e gerenciador concreto de sync;
- outras ViewModels usam dois repositories de features diferentes.

### 15.6 Objeto central excessivamente acoplado

`OnlineSportsSyncManager` depende de `AppDatabase`, Firebase Auth, Firestore e nove DAOs. Ele concentra política de ativação, listeners, serialização, fila offline, revisão, resolução de conflito e aplicação remota. Uma falha nessa classe pode afetar toda a cadeia esportiva.

---

# FASE 3 — REGRESSÕES E ESTADO QUEBRADO

## 16. Regressões funcionais reportadas em aparelho

### Copa

- Final não era criada;
- botão “Acessar Chaveamento Completo” retornava ao Dashboard;
- tela da Copa mostrava tabela de Liga;
- já houve regressão de unicidade de rodada de volta.

O código atual contém uma **tentativa de recuperação** para esses itens, incluindo rota parametrizada e correção de vencedor em partida única. Entretanto, os testes Room da Copa não chegaram às asserções por bloqueio de dependência do Robolectric, e não há teste físico posterior. Para RC, esses itens continuam **abertos até validação**, não podem ser declarados corrigidos.

### Online

- login físico reportado como indisponível;
- causa externa real ainda não confirmada no Console/conta válida;
- publicação das regras Firestore não comprovada;
- existência e estado de `user_profiles/{uid}` não comprovados;
- conteúdo real do Firestore entre dois aparelhos não validado;
- finanças não são sincronizadas.

### UX

- o erro de login já foi agrupado em mensagem genérica, escondendo causas diferentes;
- existem dois pontos de entrada para “conectar online”, com comportamentos diferentes;
- erros de listeners de Liga/perfil podem virar `null` ou lista vazia, indistinguíveis de ausência legítima de dados;
- mensagens e estados de recuperação não possuem uma fonte única de verdade.

## 17. Defeitos confirmados por inspeção na base atual

### 17.1 Versão de backup incorreta

`RoomBackupRepository` usa versão 14 enquanto o banco é versão 19. É um defeito funcional de metadados e compatibilidade de restauração.

### 17.2 Diagnóstico Firebase inevitavelmente falho

`LegacyMasterLigaApp.onCreate()` sempre chama `FirebaseConnectionChecker`. O checker tenta escrever em `system_status/connection_test`, mas `firestore.rules` contém `allow write: if false` para essa coleção. Portanto o próprio teste de conexão foi desenhado para falhar nas regras atuais, produz tráfego/log de erro em toda inicialização e envia `ANDROID_ID` e versão de banco desatualizada na tentativa.

### 17.3 Sincronização não retoma após restauração de sessão

`startAuthenticatedSession(profile)` é chamado somente pelo sucesso de login em `LoginViewModel`. Se o processo reiniciar com sessão Room `ONLINE` e Firebase Auth ainda autenticado, não há bootstrap que recarregue o perfil e reinicie o sync. O app pode aparentar sessão online enquanto a sincronização esportiva permanece desligada.

### 17.4 Dois fluxos de autenticação incompatíveis

`LoginViewModel` autentica, exige perfil, cria sessão ONLINE e inicia sync. `OnlineAuthViewModel`, por outro lado:

- tenta registrar uma conta após **qualquer** falha de login, inclusive senha inválida ou indisponibilidade;
- vincula UID, mas não cria a mesma sessão ONLINE nem inicia o gerenciador de sync;
- ao desconectar, chama apenas logout cloud, podendo deixar a sessão Room divergente.

### 17.5 Regra de membro não exige status ativo

Nas regras, `isMember()` verifica apenas a existência do documento. Um membro inativo que ainda possua documento pode ler/escrever coleções esportivas. O cliente exige `status == ACTIVE`, mas a autorização do servidor não exige. É divergência de segurança e de regra de negócio.

### 17.6 Acúmulo possível de listeners de Liga

Cada `promote()` ou `join()` bem-sucedido chama `CloudLeagueViewModel.startObserving()` e cria duas novas coletas. Não há `Job` armazenado/cancelado antes de iniciar outra observação.

### 17.7 Callback global não removível

O registro retornado por `addSnapshotsInSyncListener` não é armazenado. O listener de Auth do gerenciador também não tem referência de remoção. Como o componente é singleton, isso não multiplica na mesma execução devido ao `AtomicBoolean`, mas impede encerramento/reinicialização limpa e torna testes isolados mais difíceis.

### 17.8 Estado do build não fecha uma RC

- `LegacyDestinationTest`: aprovado;
- testes de integração da Copa: bloqueados antes do cenário pelo download do Robolectric;
- `:app:testDebugUnitTest`: sem aprovação completa atual;
- `:app:assembleDebug`: não concluído para o código recuperado;
- APK novo correspondente: inexistente;
- teste físico offline da Copa: pendente;
- login físico com conta válida: pendente.

Os “21 testes aprovados” pertencem a um estado anterior e não aprovam automaticamente a base auditada atual.

## 18. Lacunas de cobertura

- não há teste automatizado completo de navegação Compose; existe teste da string da rota;
- não há teste aprovado atual do fluxo real semifinal → Final → campeão/vice;
- não há teste do bootstrap online após reinício do processo;
- não há teste de sessão Room divergente de Firebase Auth;
- não há teste das regras Firestore contra membro inativo;
- não há teste de integração com Emulator Suite ou projeto Firebase de teste;
- não há testes de todas as migrations históricas;
- não há teste de backup/restauração usando o schema v19;
- não há teste entre dois dispositivos.

---

# FASE 4 — CÓDIGO MORTO, DUPLICADO E LEGADO

## 19. Classes e APIs sem uso de produção confirmado

Não remover nesta etapa.

- `AccessPolicy`: usada pelos próprios testes, mas nenhuma referência no código de produção;
- `InitializeDemoDataUseCase`: injetada em `LegacyMasterLigaApp`, porém nunca chamada;
- `CloudAuthRepository.currentFirebaseUid()`: declarada e implementada, sem consumidor;
- dependências `SessionManager` e `LeagueRepository` de `CloudLeagueViewModel`: injetadas, mas não utilizadas;
- `FirebaseConnectionChecker`: é chamado, porém seu protocolo é incompatível com as regras e representa código diagnóstico legado, não um health check válido.

## 20. Repositories e ViewModels sobrepostos

Não foi encontrada uma dupla de implementações com o mesmo contrato Room. A duplicação relevante é de **responsabilidade**:

- `LoginViewModel` e `OnlineAuthViewModel` implementam fluxos concorrentes de login/conexão online;
- `ScheduleRepository` e `ResultsRepository` compartilham geração/avanço de competição;
- `RoomCompetitionRepository` também chama lógica de `ResultsRepository`;
- `OnlineSportsSyncManager` repete responsabilidades de coordenação que não estão formalizadas em use case ou repository.

## 21. Listeners redundantes ou sem dono explícito

- coletas repetíveis de Liga/membros em `CloudLeagueViewModel`;
- Auth listener e `addSnapshotsInSyncListener` sem handle de remoção em `OnlineSportsSyncManager`;
- fluxo de perfil em `OnlineAuthViewModel` paralelo ao fluxo autenticado de `LoginViewModel`;
- `FirebaseConnectionChecker` faz uma chamada de startup que não representa o estado efetivo de Auth/sincronização.

## 22. Código legado e inconsistências

- comentário “Simplificação para o teste RC1” ainda controla a decisão de registrar conta após falha de login;
- constantes de banco 14 e 15 sobreviveram à versão 19;
- pacote raiz `domain/data` convive com `feature/*/domain/data`, sem regra uniforme;
- apenas quatro use cases para 26 ViewModels;
- `OnlineSportsSyncManager` não possui contrato e é consumido diretamente;
- o projeto ainda contém `ExampleUnitTest`, teste-modelo sem valor de regressão;
- `RoomJvmTest` e testes auxiliares precisam ser avaliados quanto à finalidade e força das asserções antes da RC.

## 23. Imports provavelmente inúteis

Varredura estática apontou candidatos, a confirmar pelo compilador/IDE antes de remover:

- `LegacyMotion` em `LegacyModuleCard.kt`;
- `ContentScale` em `LegacyStateComponents.kt`;
- `TextButton` em `BackupScreen.kt` e `CompetitionsScreen.kt`;
- `AndroidView`, `Box`, `CircleShape`, `ContentScale`, `ImageView`, `Surface`, `Uri` e `UserRole` em `ClubsScreen.kt`;
- `height` em `DashboardScreen.kt`;
- `background` e `EmojiEvents` em `HistoryScreen.kt`;
- `DocumentSnapshot` em `OnlineSportsSyncManager.kt`;
- `RoundStatus` em `RoomResultsRepository.kt`;
- `Box` em `StandingsScreen.kt`;
- `Date`, `DateFormat` e `EmojiEvents` em `ScheduleScreen.kt`.

Esta lista é de candidatos, não autorização de remoção.

---

# FASE 5 — AVALIAÇÃO ARQUITETURAL

## 24. O que impede o Legacy Master Liga de ser Release Candidate?

Tecnicamente, uma RC precisa ter escopo congelado, build reproduzível, caminhos críticos aprovados, migração segura, configuração externa conhecida e falhas observáveis. A base atual não cumpre esses critérios:

### 24.1 Não há evidência de estabilidade ponta a ponta

O motor local da Copa sofreu regressões em criação da Final e navegação. Mesmo com correções candidatas presentes, falta a prova funcional offline obrigatória. Código aparentemente correto não substitui teste em banco real e aparelho.

### 24.2 A autenticação não tem uma única máquina de estados

Há duas ViewModels e caminhos diferentes para autenticar/vincular/desconectar. Sessão Room, Firebase Auth, perfil Firestore e ativação do sync podem divergir. Uma RC precisa de um único coordenador de sessão com estados explícitos e retomada determinística.

### 24.3 A sincronização não possui ciclo de vida completo

O gate de segurança local melhorou, mas a retomada após reinício não existe, alguns listeners não têm ownership removível e o tratamento de erro dos listeners perde informação. Isso impede afirmar consistência em tempo real.

### 24.4 O contrato do banco está inconsistente

Banco v19, backup v14 e diagnóstico v15 são um bloqueador direto. Além disso, schemas históricos ausentes impedem uma matriz confiável de upgrade a partir de versões antigas.

### 24.5 O backend implantado não é conhecido

O arquivo de regras no projeto não comprova que ele foi publicado. Provedor Auth, projeto Firebase efetivo, perfis e dados reais precisam de checklist de ambiente. Sem isso, um APK idêntico pode funcionar em um aparelho e falhar em outro.

### 24.6 Segurança cliente e servidor divergem

O cliente exige membership `ACTIVE`; as regras aceitam qualquer documento de membro existente. Uma RC online não pode depender do cliente para impor autorização.

### 24.7 O motor esportivo não tem fronteira arquitetural clara

Calendário, resultados, competição e classificação se chamam mutuamente. A lógica de avanço de fase fica dentro de repository de resultados, enquanto a criação inicial fica em schedule. Essa divisão elevou o risco de idempotência, fase e número de rodada quebrarem de forma independente.

### 24.8 A suíte não é uma barreira de release confiável

Não há execução verde atual de todos os testes e do build, nem APK novo. Testes críticos de integração, migrations, backup e Firebase estão ausentes ou não executados. Logo não existe um gate objetivo que impeça outra regressão.

## 25. Critério mínimo recomendado para declarar RC-01

Sem implementar novas funcionalidades, a RC-01 só deve ser promovida quando:

1. o fluxo Copa de 4 clubes passar offline, em partida única e ida/volta;
2. semifinal, uma única Final, campeão, vice, histórico e preservação de resultados forem verificados;
3. navegação para `Schedule/Bracket` preservar Liga, competição e temporada;
4. Liga continuar mostrando classificação e Copa não mostrar tabela indevida;
5. existir um único fluxo de autenticação e uma retomada de sessão testada;
6. login real diferenciar Auth, perfil, permissão, rede e timeout;
7. regras publicadas e membership ativa forem validadas no servidor;
8. versões de banco/backup/diagnóstico forem coerentes e migrations suportadas forem testadas;
9. listeners tiverem ownership e não se acumularem;
10. `:app:testDebugUnitTest` e `:app:assembleDebug` terminarem com sucesso em ambiente limpo;
11. o APK gerado for o mesmo artefato submetido ao roteiro físico;
12. o relatório de aceite registrar evidências, versão, hash e caminho do APK.

## 26. Conclusão

O Legacy Master Liga possui uma base funcional relevante, mas seu estado atual é de **estabilização pré-RC**, não de Release Candidate. O maior risco não é a ausência de novas funcionalidades: é a falta de uma fronteira única entre sessão local, Auth, perfil cloud e sincronização, somada à divisão do motor da Copa e à ausência de um gate de testes reproduzível.

Recomendação de governança: manter o congelamento de funcionalidades e transformar cada bloqueador acima em item verificável de estabilização. Nenhum achado desta auditoria autoriza alteração ou remoção automática de código.

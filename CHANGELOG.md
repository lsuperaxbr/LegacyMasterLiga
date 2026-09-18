# Changelog

## 1.0.0-rc01 — Beta Stabilization 03 + 04

- Reconstruídas otimizações seguras de Performance sobre a Stabilization 02.
- Reduzidas emissões e recomposições desnecessárias no Dashboard.
- Adicionada variante RC minificada, reduzida e instalável separadamente.
- Habilitados R8 e shrinkResources para RC e Release.
- Revisadas regras ProGuard para Room, Hilt, WorkManager e modelos do banco.
- Adicionados scripts de geração da RC1 e da Release oficial.
- Adicionadas documentação de assinatura e checklist de distribuição.
- Preservadas todas as funcionalidades e Room versão 12.

## 1.0.0-beta09 — Beta Stabilization 02

- Configurado Android Lint com relatórios HTML, XML e SARIF.
- Adicionados scripts para executar `lintBeta` e salvar o log completo.
- Adicionado verificador estático offline para Manifest, segurança, strings e código temporário.
- Declarado `pt-BR` como idioma atual do aplicativo.
- Removido atributo redundante de orientação da Activity principal.
- Internacionalizados os textos da tela de Login e dos estados visuais compartilhados.
- Mantidos Room 12 e todas as funcionalidades existentes.

## 1.0.0-beta08 — Beta Stabilization 01

- Confirmado que a primeira compilação real terminou com `BUILD SUCCESSFUL`.
- Ativada execução paralela do Gradle.
- KSP incremental explicitamente habilitado.
- Jetifier desativado por o projeto utilizar somente AndroidX.
- Classes `R` não transitivas e IDs não finais ativados para melhorar builds incrementais.
- Adicionado script local de build com `--profile`, Configuration Cache e log completo.
- Adicionada documentação do Build Analyzer e política de congelamento de dependências até a RC1.
- Nenhuma funcionalidade ou migração Room adicionada.

## 1.0.0-beta07 — Sprint 036

- Preparação final para a primeira compilação e instalação reais.
- Hilt atualizado para 2.59.2 por compatibilidade com AGP 9.
- Novo script único de diagnóstico, testes e geração do APK Beta.
- Novos guias de abertura, instalação e correção direta de erros.
- Verificação estática ampliada.
- Nenhuma funcionalidade adicionada ou removida.

## 1.0.0-beta05 — Sprint 034

- Preparada a primeira abertura guiada no Android Studio.
- Adicionado roteiro direto para sincronizar o Gradle e gerar o APK Beta.
- Adicionados scripts Windows sem dependência obrigatória de Python.
- Primeiro build configurado com `--no-configuration-cache` para reduzir riscos de cache antigo.
- Verificador estático atualizado para a numeração atual da Beta.
- Nenhuma funcionalidade existente foi alterada ou removida.

## 1.0.0-beta02 — Sprint 031

- Padroniza o compileSdk em API 36 para simplificar a instalação do SDK.
- Amplia o timeout do Gradle Wrapper para conexões mais lentas.
- Ativa cache local do Gradle e confirma AndroidX.
- Adiciona guia passo a passo para Android Studio Quail.
- Adiciona documentação de erros de sincronização e build.
- Adiciona scripts do Windows para sincronizar, validar e gerar o APK Beta.
- Atualiza a verificação estática para a Beta 2.

## 0.29.0-alpha29 — Sprint 029

- Revisão final de Room, Hilt, navegação, permissões, telas e regras de negócio.
- Correção de valores `Long` nos fluxos de Financeiro, Mercado e Premiações.
- Desativação do Android Auto Backup em favor do backup oficial `.lmlbackup`.
- Bloqueio de tráfego HTTP sem criptografia.
- Inclusão de verificador estático local para preparação da Beta.
- Remoção de configurações de template incompatíveis com a estratégia oficial de backup.

## 0.28.0-alpha28 — Sprint 028

### Adicionado
- Testes unitários para autenticação, permissões, calendário, notícias e cache.
- Testes instrumentados do Room com banco em memória e regras de integridade.
- Testes de fumaça das rotas de navegação.
- Relatório técnico `SPRINT_028_REPORT.md`.

### Qualidade
- Cobertura das regras críticas de CR, sessões, acesso por perfil e geração de confrontos.
- Comandos documentados para testes locais, instrumentados e lint.

## 0.27.0-alpha27 — Sprint 027

### Adicionado
- Monitor local de desempenho com métricas agregadas por operação.
- Cache em memória LRU com expiração e limite de entradas.
- Registro central de erros recuperáveis, sem envio externo.
- Tela administrativa de Desempenho com métricas, cache e erros.

### Otimizado
- Dashboard com fluxos `distinctUntilChanged` e `conflate`.
- Atalhos do Dashboard sem grade preguiçosa aninhada.
- Lista de módulos estabilizada com `remember` e chaves estáveis.
- Opções dos relatórios reutilizadas por cache local.
- Geração e cópia de relatórios monitoradas e protegidas por tratamento de erros.

### Banco de dados
- Nenhuma migração; Room permanece na versão 12.

## 0.26.0-alpha26 — Sprint 026
- Adicionado Design System refinado com tokens de espaçamento, formas e movimento.
- Tipografia completa e consistente em todo o Material 3.
- Adicionados estados reutilizáveis de carregamento, vazio e erro.
- Adicionadas transições suaves entre destinos da navegação.
- Melhorado o feedback de toque, elevação e acessibilidade dos cartões de módulos.
- Padronizadas as telas de acesso restrito e recursos futuros.
- Nenhuma alteração no banco Room; versão permanece 12.


## 0.25.0-alpha25 — Sprint 025
- Adicionada Central de Relatórios com filtros por liga, competição e temporada.
- Exportação consolidada em PDF e CSV.
- Salvamento via seletor de arquivos e compartilhamento seguro via FileProvider.
- Relatórios incluem classificação, financeiro em CR, mercado, resultados, histórico e estatísticas.
- Exportações registradas na Auditoria.
- Dashboard e navegação integrados ao novo módulo.

## 0.24.0-alpha24 — Sprint 024

### Adicionado
- Central interna de notificações persistente e reativa.
- Alertas para resultados pendentes, transferências, liderança, encerramentos, backups e ações administrativas.
- Filtros por liga, categoria e leitura.
- Estado de leitura individual por usuário e contador no Dashboard.
- Rotas contextuais e controle de audiência conforme perfil.

### Banco de dados
- Novas tabelas `notifications` e `notification_reads`.
- Room atualizado da versão 11 para 12.

### Alterado
- Dashboard com atalho e contador de notificações não lidas.
- Backup compatível com banco na versão 12.
- Versão atualizada para `0.24.0-alpha24` (`versionCode 25`).


## 0.23.0-alpha23 — Sprint 023

### Adicionado
- Dashboard avançado e reativo com contexto da liga, competição e temporada ativa.
- Cartões de líder, próxima rodada, saldo em CR e atividade da liga.
- Últimos resultados, transferências recentes e notícias mais novas.
- Saldo contextual do clube para usuários Presidente.
- Atalhos exibidos conforme as permissões de Administrador, Presidente e Visitante.
- DAO e modelos dedicados ao Dashboard, sem nova migração do Room.

### Alterado
- `DashboardRepository`, `DashboardViewModel` e `DashboardScreen` foram ampliados.
- `AppDatabase` e `DatabaseModule` agora expõem `DashboardDao`.
- Versão atualizada para `0.23.0-alpha23`.

### Banco de dados
- Nenhuma alteração de esquema; Room permanece na versão 11.
# Changelog

## 0.22.1-alpha22-p21 — Sprint 021 restaurada sobre a Sprint 022

### Adicionado
- Premiações configuráveis por competição para campeão, vice-campeão e participação.
- Aplicação automática das premiações durante o encerramento oficial da temporada.
- Crédito nos clubes e débito correspondente no Banco da Liga, sempre em CR.
- Histórico permanente das premiações por liga, competição, temporada, clube e tipo.
- Tela de encerramento ampliada para configurar valores e consultar o histórico.
- Auditoria das alterações nas regras de premiação.

### Banco de dados
- Novas tabelas `competition_prizes` e `prize_history`.
- Campo `participationPrizeCr` adicionado a `season_closures`.
- Room atualizado da versão 10 para 11.

### Preservado
- Centro de Estatísticas da Sprint 022 mantido integralmente.

## 0.22.0-alpha22 — Sprint 022

### Adicionado
- Centro de Estatísticas da Liga com filtros por liga, competição e temporada.
- Resumo de partidas, gols, médias, clubes ativos, temporadas e movimentação em CR.
- Ranking histórico dos clubes e integração com perfil, Dashboard, Room, Hilt e Navigation Compose.

### Observação
- A Sprint 022 foi aplicada sobre o último ZIP real disponível, a Sprint 020. A Sprint 021 permanece pendente de implementação real.

## 0.20.0-alpha20 — Sprint 020
- Encerramento oficial e manual de temporadas.
- Campeão e vice definidos pela classificação final.
- Snapshot imutável da classificação final.
- Premiações de campeão e vice em CR, debitadas do Banco da Liga.
- Notícia automática de campeão e log de auditoria.
- Bloqueio de novos resultados após encerramento.
- Opção de encerrar também a competição.
- Room 9 → 10.


## 0.18.0-alpha18 — Sprint 018

### Adicionado
- Histórico completo por liga, competição e temporada.
- Campeão, vice-campeão e classificação final derivados dos dados preservados.
- Estatísticas históricas de temporadas, competições, partidas e gols.
- Tela Compose com filtros e acesso ao perfil dos clubes.
- Repository Room, DAO histórico e integração Hilt/Navigation/Dashboard.

### Banco de dados
- Nenhuma migração necessária; Room permanece na versão 9.
# Changelog

## 0.17.0-alpha17 — Sprint 017
- Configurações avançadas da liga e do aplicativo.
- Regras de pontuação por competição.
- Critérios de desempate persistidos por competição.
- Preferências visuais com tema escuro, claro ou do sistema.
- Tema atualizado reativamente.
- Migração Room 8 → 9.
- Integração com Dashboard, Hilt, Navigation e Auditoria.

## 0.16.0-alpha16 — Sprint 016

### Adicionado
- Sistema completo de Backup e Restauração com pacote `.lmlbackup`.
- Backup manual e automático diário/semanal por WorkManager.
- Manifesto com versões, data e SHA-256 para validação de integridade.
- Exportação e importação pelo seletor de arquivos do Android.
- Cópia de segurança obrigatória antes de qualquer restauração.
- Tela administrativa de backups, frequência automática e reinício seguro.
- Registros de backup e restauração integrados à Auditoria.

### Alterado
- Dashboard administrativo com atalho para Backup.
- Inicialização do aplicativo sincroniza o agendamento automático salvo.
- Versão atualizada para `0.16.0-alpha16` (`versionCode 16`).

## 0.15.0-alpha15 — Sprint 015

### Adicionado
- Sistema permanente de Logs e Auditoria com Room.
- Pesquisa e filtros por liga, categoria e ação.
- Tela administrativa reativa em Jetpack Compose.
- Registro automático de login, logout, usuários, clubes, competições, temporadas, resultados, recalculo da classificação, finanças e transferências.
- Migração Room 7 → 8 e índices para consultas de auditoria.

### Alterado
- Dashboard administrativo com atalho para Auditoria.
- Repositories principais integrados ao `AuditLogger`.
- Versão atualizada para `0.15.0-alpha15` (`versionCode 15`).

## [0.14.0-alpha14] - Sprint 014

### Adicionado
- Motor automático de notícias orientado a eventos e totalmente determinístico, sem uso de IA.
- Catálogo variado de manchetes e textos para transferências, resultados, goleadas, mudanças de liderança e conquistas.
- Chaves únicas por evento para impedir notícias duplicadas no banco.
- Atualização da notícia existente quando um placar é corrigido.
- Vínculos opcionais de notícia com competição e temporada, mantendo sempre a liga de origem.
- Tela Compose de notícias com seleção de liga, filtros por categoria, contexto esportivo e data de publicação.
- Testes unitários do catálogo de templates, incluindo determinismo, variedade e classificação de goleadas.

### Alterado
- Transferências publicam a notícia dentro da mesma transação que movimenta os clubes e o financeiro.
- Resultados recalculam a classificação e publicam as notícias relacionadas na mesma transação Room.
- Mudanças de liderança são detectadas pelos mesmos critérios de desempate da tabela oficial.
- Temporadas são encerradas automaticamente quando todas as partidas terminam, gerando a notícia do campeão.
- Card de Notícias do Dashboard exibe a quantidade gerada automaticamente e abre o feed real.
- Room atualizado da versão 6 para 7 com migração preservando notícias existentes.
- Versão atualizada para `0.14.0-alpha14` (`versionCode 14`).

## [0.13.0-alpha13] - Sprint 013

### Adicionado
- Módulo Financeiro reativo com saldos em CR por clube e Banco da Liga.
- Créditos e débitos administrativos com lançamentos espelhados e descrição obrigatória.
- Extrato completo por liga ou clube, com contraparte, origem, data e vínculo da transferência.
- Módulo Mercado com transferências pelo nome do jogador, sem cadastro prévio de elenco.
- Histórico de negociações com clubes vendedor e comprador, valor e data.
- Validação de saldo do comprador antes de confirmar a transferência.

### Alterado
- Transferências atualizam automaticamente e de forma atômica os saldos dos dois clubes.
- Presidentes podem consultar apenas o financeiro do clube associado e comprar para o próprio clube.
- Dashboard passou a calcular o total de CR dos clubes sem contabilizar o saldo técnico do Banco da Liga.
- Room atualizado para a versão 6 com migração preservando os lançamentos existentes.
- Rotas Financeiro e Mercado substituíram as telas provisórias por módulos Compose reais.
- Dependências AndroidX alinhadas ao `compileSdk 36.1`, eliminando a exigência indevida de API 37.
- Versão atualizada para `0.13.0-alpha13` (`versionCode 13`).

## [0.12.0-alpha12] - Sprint 012

### Adicionado
- Tela de gestão de usuários, perfis e permissões.
- Criação de Administradores, Presidentes e Visitantes.
- Associação de Presidentes aos clubes.
- Política central de permissões e tela de acesso negado.
- Atalho administrativo de Usuários no Dashboard.

### Alterado
- Dashboard agora exibe módulos conforme o perfil autenticado.
- Navegação de Competições, Inscrições, Rodadas e Usuários protegida para Administradores.
- Clubes entram em modo somente leitura para perfis sem permissão administrativa.
- Sessões de contas desativadas ou bloqueadas são invalidadas.
- Repositórios de usuários e clubes ampliados para manutenção de perfis e associações.
- Versão atualizada para `0.12.0-alpha12`.


## [0.11.0-alpha11] - Sprint 011

### Adicionado
- Tela completa de perfil e estatísticas dos clubes em Jetpack Compose.
- Cabeçalho do clube com escudo, presidente e status ativo/desativado.
- Seleção de competição e temporada no perfil.
- Exibição de posição, jogos, vitórias, empates, derrotas, pontos, gols pró, gols contra, saldo de gols e aproveitamento.
- Lista dos cinco resultados mais recentes com adversário, placar e indicador de vitória, empate ou derrota.
- Histórico consolidado por competição e temporada, incluindo a posição final calculada pelos critérios oficiais.
- Acesso ao perfil pela tela de Clubes e ao tocar em um time na Classificação.
- Repository reativo dedicado ao perfil do clube, integrado a Room, Flow, StateFlow e Hilt.

### Alterado
- `ClubDao`, `MatchDao` e `StandingDao` receberam consultas reativas para cabeçalho, resultados recentes e histórico.
- Navegação recebeu rota protegida e parametrizada para o perfil do clube.
- Versão do aplicativo atualizada para `0.11.0-alpha11`.

### Banco de dados
- Nenhuma nova tabela ou coluna foi necessária. A versão do Room permanece 5.

### Validação
- Estrutura, imports, rotas, projeções Room, bindings Hilt e integridade do ZIP verificados estaticamente.
- A compilação Gradle foi iniciada, mas o ambiente não conseguiu acessar `services.gradle.org` para baixar o Wrapper.

## 0.1.0-alpha01 — Sprint 001

### Adicionado

- Configuração inicial de Hilt, Room, KSP e Navigation Compose.
- `LegacyMasterLigaApp`.
- Grafo de navegação inicial.
- Dashboard Alpha.
- Componente reutilizável `LegacyModuleCard`.
- Tema escuro oficial inicial.
- Relatório técnico do projeto recebido.

### Alterado

- Java 11 para Java 17.
- Dependências AndroidX atualizadas para versões estáveis.
- `MainActivity` convertida de tela de exemplo para ponto de entrada do aplicativo.
- Versão do aplicativo para `0.1.0-alpha01`.

### Removido

- Tela padrão `Hello Android!`.
- Paleta roxa padrão do template.

## 0.2.0-alpha02 — Sprint 002

### Added
- Room database infrastructure (`AppDatabase`) with schema export enabled.
- Hilt database module with providers for the database and initial DAOs.
- Shared Room type converters for user, league, competition and season enums.
- Initial entities: users, sessions, leagues, clubs, competitions and seasons.
- Foreign keys, unique constraints and query indexes for the initial data model.
- Initial DAOs with coroutine and `Flow` support.
- Dynamic club crest URI support and the invisible Banco da Liga flag.
- Official league currency default set to `CR`.

### Validation
- Static source validation completed.
- Gradle compilation was attempted, but the execution environment could not resolve `services.gradle.org`; final compilation must be confirmed in Android Studio with internet access.

## 0.3.0-alpha03 — Sprint 003

### Adicionado

- Interfaces de domínio para usuários, ligas e clubes.
- Modelos de domínio desacoplados das entidades Room.
- Repositórios Room com `Flow` e operações suspensas.
- Mapeadores entre entidades de persistência e modelos de domínio.
- `InitializeDefaultDataUseCase` com transação atômica.
- Criação idempotente do primeiro Administrador, da primeira Liga e do Banco da Liga.
- Moeda oficial inicial configurada como `CR`.
- Hash de senha com PBKDF2, salt aleatório e comparação em tempo constante.
- Módulo Hilt de bindings para repositories e segurança.
- Inicialização automática e segura dos dados padrão ao abrir o aplicativo.

### Dados iniciais da Alpha

- Usuário: `admin`
- Senha temporária: `admin123`
- Liga: `Liga M L Amigos`
- Clube invisível: `Banco da Liga`

> A senha temporária deverá ser alterada na Sprint de autenticação.

### Validação

- Validação estática de pacotes, imports, bindings Hilt e dependências Room concluída.
- A compilação Gradle foi tentada, mas o ambiente não conseguiu acessar `services.gradle.org` para baixar o Gradle Wrapper. A confirmação final deverá ser realizada no Android Studio conectado à internet.

## 0.5.0-alpha05 — Sprint 004 + Sprint 005
- Autenticação local real com PBKDF2, sessão Room persistente e logout.
- Login protegido com Administrador padrão da Sprint 003.
- Navegação protegida entre Login e Dashboard.
- Dashboard reativo conectado ao Room por Flow/StateFlow.
- Resumos de Competições, Clubes, Financeiro, Mercado e Notícias.
- Estruturas iniciais de Financeiro, Transferências e Notícias no banco.
- Migração Room 1 → 2.

## 0.6.0-alpha06 — Sprint 006

### Adicionado

- Módulo Competições com arquitetura `data/domain/presentation`.
- Cadastro reativo de múltiplas ligas, sempre utilizando a moeda oficial CR.
- Cadastro de competições dos tipos Liga e Copa.
- Formatos configuráveis: somente ida, ida e volta e jogo único para copas.
- Criação atômica da competição com a primeira temporada.
- Criação de novas temporadas, encerrando e preservando automaticamente a temporada anterior.
- Listagem reativa de competições e temporadas por liga usando Room, Flow e StateFlow.
- Tela `CompetitionsScreen` em Jetpack Compose com formulários, validações e mensagens de retorno.
- Integração real da rota Competições com o Dashboard e o grafo de navegação.
- Repository Room dedicado e binding Hilt.
- Consultas e índices existentes reutilizados sem alteração de schema.

### Validação

- Validação estática de imports, rotas, DAOs, repository, Hilt e fluxos reativos concluída.
- A compilação Gradle foi iniciada, mas não pôde baixar o Gradle Wrapper porque o ambiente não acessou `services.gradle.org`.

## 0.7.0-alpha07 — Sprint 007

### Adicionado

- Módulo Clubes com arquitetura reativa em `presentation`, integrado aos repositories de domínio.
- Cadastro e edição de clubes vinculados à liga selecionada.
- Associação opcional de presidente usando usuários já cadastrados.
- Seleção de escudo pela galeria com persistência de permissão de leitura da URI.
- Pré-visualização do escudo no formulário e nos cartões dos clubes.
- Ativação e desativação sem excluir o histórico do clube.
- Listagem reativa por liga usando Room, `Flow` e `StateFlow`.
- Validação de nome e tratamento de duplicidade dentro da mesma liga.
- Integração real da rota Clubes com o Dashboard e o grafo de navegação.

### Alterado

- `ClubDao` ampliado com consulta administrativa, busca por ID e atualização de status.
- `ClubRepository` e `RoomClubRepository` ampliados com criação, edição e ativação/desativação.
- Versão do aplicativo atualizada para `0.7.0-alpha07`.

### Validação

- Verificação estática de imports, rotas, queries Room, repository e fluxos reativos realizada.
- A compilação Gradle continua dependente do download do Wrapper no Android Studio conectado à internet.

## 0.8.0-alpha08 — Sprint 008
- Adicionado módulo de inscrições e participantes por competição/temporada.
- Criada entidade Room `CompetitionParticipantEntity` com chave única por temporada e clube.
- Adicionados DAO, Repository, ViewModel e tela Compose reativa.
- Clubes podem ser inscritos e removidos de uma temporada sem duplicidade.
- Banco da Liga permanece fora da lista de participantes.
- Adicionada migração Room 2 → 3.
- Dashboard e navegação receberam atalho para Inscrições.
- Estrutura preparada para a geração automática de rodadas na Sprint 009.

## 0.9.0-alpha09 — Sprint 009

### Adicionado
- Geração automática de rodadas e partidas por temporada.
- Formatos somente ida, ida e volta e jogo único.
- Entidades, DAOs, Repository, ViewModel e tela Compose de calendário.
- Migração Room 3 → 4.
- Proteção contra confrontos duplicados.

### Modificado
- Dashboard e navegação com acesso ao módulo Rodadas.
- AppDatabase, TypeConverters, módulos Hilt e DAOs auxiliares.

## [0.10.0-alpha10] - Sprint 010
### Adicionado
- Lançamento e correção de resultados nas partidas.
- Classificação automática por temporada.
- Estatísticas de J, V, E, D, PTS, GP, GC, SG e aproveitamento.
- Destaque amarelo para o líder.
- Migração Room 4 para 5 com a tabela `standings`.
- Tela reativa de classificação integrada ao Dashboard.

### Alterado
- Rodadas agora mudam automaticamente de status conforme os resultados.
- Correções de placar reconstroem toda a classificação para impedir contagem duplicada.

## 0.19.0-alpha19 — Sprint 019

### Adicionado

- Hall da Fama com seleção por liga e atualização reativa.
- Recordes de maior campeão, mais vitórias, melhor ataque e melhor defesa.
- Maior goleada com placar e contexto da competição.
- Maior sequência invicta calculada por temporada.
- Maior movimentação acumulada em CR por clube.
- Acesso ao perfil dos clubes pelos cartões de recordes.
- DAO e Repository dedicados, integrados ao Room e Hilt.
- Atalho Hall da Fama no Dashboard.

### Alterado

- Navegação ampliada com a rota `hall_of_fame`.
- Versão do aplicativo atualizada para `0.19.0-alpha19`.

### Banco de dados

- Nenhuma migração necessária; Room permanece na versão 9.

## 1.0.0-beta01 — Sprint 030

### Adicionado

- Variante de build `beta` para testes reais e instalação paralela.
- Configuração opcional de assinatura da variante Release por `keystore.properties`.
- Arquivo `app/proguard-rules.pro`, corrigindo a referência ausente no build de Release.
- Documentação completa de instalação, testes, geração e assinatura de APKs.
- Scripts `build_beta.bat` e `build_beta.sh`.
- Checklist oficial da primeira Beta.

### Alterado

- `versionCode` atualizado para 31.
- `versionName` atualizado para `1.0.0-beta01`.
- `.gitignore` ampliado para proteger keystores e artefatos de build.
- Release mantido sem minificação durante a fase Beta para maior previsibilidade.

### Preservado

- Todas as funcionalidades e migrações das Sprints 001 a 029.
- Banco Room na versão 12.

## 1.0.0-beta03 — Sprint 032

### Adicionado
- Dados de demonstração da Liga M L Amigos nas variantes Debug/Beta.
- Torino, Fluminense, Bournemouth e Girona com presidentes e saldo inicial de 500 CR.
- Liga Principal em formato ida e volta e Temporada 1 ativa.
- Inscrição automática e idempotente dos quatro clubes.
- Usuários Presidente de demonstração com senha temporária `demo123`.
- Roteiro de teste Beta dentro do aplicativo.

### Preservado
- Moeda oficial CR e todas as regras funcionais anteriores.
- Room na versão 12, sem nova migração.
- Variante Release sem dados fictícios.

## 1.0.0-beta04 — Sprint 033

- Preparada a primeira compilação real da Beta com scripts diagnósticos.
- Adicionado log persistente do build em `build-reports/sprint033-build.log`.
- Criados guias de sincronização e correção de Gradle, KSP, Room, Hilt e Compose.
- Removido o plugin Foojay não utilizado para simplificar a sincronização.
- Nenhuma funcionalidade foi adicionada ou removida.

## 1.0.0-beta06 — Sprint 035

### Adicionado
- Guia de instalação e primeiro teste no celular.
- Checklist funcional da Beta por módulo.
- Documento com contas de teste.
- Modelo padronizado de relatório de erros.
- Scripts para empacotar o APK Beta com documentação e SHA-256.

### Alterado
- `versionCode` atualizado para 36.
- `versionName` atualizado para `1.0.0-beta06`.
- Verificação estática ampliada para a preparação do teste no celular.

### Preservado
- Todas as funcionalidades existentes.
- Room na versão 12, sem nova migração.

## Validação final da RC1

- Adicionados scripts separados para `testDebugUnitTest`, `lintRc` e `assembleRc`.
- Adicionado relatório transparente da validação final.
- Nenhuma funcionalidade ou regra de negócio foi alterada.
- Nenhum erro de código foi registrado sem um resultado real do Gradle.

# Sprint 011 — Perfil e Estatísticas dos Clubes

## Objetivo

Entregar uma tela reativa de perfil do clube com estatísticas da temporada selecionada, resultados recentes e histórico por temporada, integrada às telas de Clubes e Classificação.

## Implementação realizada

### Camada de banco de dados

Foram adicionadas projeções Room para:

- cabeçalho do clube com presidente;
- últimos cinco jogos finalizados de um clube em uma temporada;
- histórico de classificação por competição e temporada.

Os critérios usados para calcular a posição histórica são os mesmos da classificação atual:

1. pontos;
2. vitórias;
3. saldo de gols;
4. gols pró;
5. nome do clube.

Nenhuma migração foi necessária porque a Sprint utiliza as tabelas existentes.

### Camadas data e domain

Foi criado o `ClubProfileRepository`, com implementação Room reativa baseada em `Flow`.

O módulo converte os placares para a perspectiva do clube selecionado, identificando automaticamente vitória, empate ou derrota.

### Apresentação

A nova tela mostra:

- escudo;
- nome e presidente;
- status ativo/desativado;
- seletor de competição e temporada;
- posição;
- J, V, E, D;
- GP, GC e SG;
- pontos e aproveitamento;
- cinco resultados recentes;
- histórico completo por temporada.

O líder continua destacado em amarelo.

### Navegação

O perfil pode ser aberto:

- pelo botão de informações na tela de Clubes;
- tocando em um clube na tabela de Classificação.

A temporada selecionada na Classificação é transportada para o perfil.

## Arquivos criados

- `core/database/model/ClubProfileHeaderRow.kt`
- `core/database/model/ClubRecentMatchRow.kt`
- `core/database/model/ClubSeasonHistoryRow.kt`
- `feature/clubprofile/domain/ClubProfileModels.kt`
- `feature/clubprofile/domain/ClubProfileRepository.kt`
- `feature/clubprofile/data/RoomClubProfileRepository.kt`
- `feature/clubprofile/presentation/ClubProfileViewModel.kt`
- `feature/clubprofile/presentation/ClubProfileScreen.kt`
- `SPRINT_011_REPORT.md`

## Arquivos modificados

- `core/database/dao/ClubDao.kt`
- `core/database/dao/MatchDao.kt`
- `core/database/dao/StandingDao.kt`
- `core/di/RepositoryModule.kt`
- `core/navigation/LegacyDestination.kt`
- `core/navigation/LegacyNavGraph.kt`
- `feature/clubs/presentation/ClubsScreen.kt`
- `feature/results/presentation/StandingsScreen.kt`
- `app/build.gradle.kts`
- `CHANGELOG.md`

## Validação

- Contagem de delimitadores Kotlin e inspeção das referências realizadas.
- Projeções SQL revisadas contra o schema Room atual.
- ZIP final validado.
- A compilação automática não avançou porque este ambiente não conseguiu resolver `services.gradle.org`.

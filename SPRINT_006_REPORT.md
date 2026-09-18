# Sprint 006 — Competições

## Objetivo

Implementar o módulo de Competições sobre o projeto existente, permitindo cadastrar e consultar múltiplas ligas, ligas esportivas, copas e temporadas sem misturar os respectivos dados.

## Entregas

- Cadastro de ligas com moeda fixa em `CR`.
- Seleção da liga atualmente exibida.
- Cadastro de Liga ou Copa.
- Formatos `SINGLE_ROUND`, `HOME_AND_AWAY` e `SINGLE_MATCH`.
- Primeira temporada criada na mesma transação da competição.
- Próxima temporada criada sem apagar a anterior.
- Listagem reativa usando Room `Flow` e ViewModel `StateFlow`.
- Interface em Jetpack Compose integrada à navegação e ao Dashboard.
- Regras contra nomes duplicados na mesma liga.
- Dados de cada liga, competição e temporada mantidos isolados por chave estrangeira.

## Arquivos principais

- `feature/competitions/domain/CompetitionModels.kt`
- `feature/competitions/domain/CompetitionRepository.kt`
- `feature/competitions/data/RoomCompetitionRepository.kt`
- `feature/competitions/presentation/CompetitionsViewModel.kt`
- `feature/competitions/presentation/CompetitionsScreen.kt`

## Arquivos atualizados

- `CompetitionDao.kt`
- `SeasonDao.kt`
- `RoomLeagueRepository.kt`
- `RepositoryModule.kt`
- `LegacyNavGraph.kt`
- `app/build.gradle.kts`
- `CHANGELOG.md`

## Banco de dados

A Sprint reutiliza as tabelas `leagues`, `competitions` e `seasons` criadas anteriormente. Não houve mudança de schema, portanto a versão Room permanece em 2 e nenhuma migração nova foi necessária.

## Validação

O comando `:app:compileDebugKotlin` foi executado, mas o Gradle Wrapper não pôde ser baixado por indisponibilidade de acesso a `services.gradle.org` no ambiente. A validação final deve ser feita no Android Studio conectado à internet.

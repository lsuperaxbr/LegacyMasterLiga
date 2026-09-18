# Sprint 007 — Módulo Clubes

## Objetivo

Implementar o gerenciamento real de clubes sobre a base existente, sem recriar o projeto.

## Entregas

- Cadastro e edição de clubes.
- Associação à liga selecionada.
- Associação opcional a um usuário/presidente.
- Escolha de escudo por documento de imagem da galeria.
- Ativação e desativação preservando o histórico.
- Listagem reativa com Room, Flow e StateFlow.
- Integração com Dashboard e Navigation Compose.

## Arquivos criados

- `feature/clubs/presentation/ClubsScreen.kt`
- `feature/clubs/presentation/ClubsViewModel.kt`
- `SPRINT_007_REPORT.md`

## Arquivos modificados

- `core/database/dao/ClubDao.kt`
- `domain/repository/ClubRepository.kt`
- `data/repository/RoomClubRepository.kt`
- `core/navigation/LegacyNavGraph.kt`
- `app/build.gradle.kts`
- `CHANGELOG.md`

## Banco de dados

Nenhuma migração foi necessária. A entidade `ClubEntity` já possuía os campos `crestUri`, `presidentUserId`, `isActive` e `leagueId`.

## Observação de compilação

A validação final deve ser feita no Android Studio com internet na primeira sincronização, para que o Gradle Wrapper e as dependências sejam baixados e armazenados em cache.

# Task List - Importação em Lote de Atributos por Time (ImportTeamAttributes)

- [x] Commit e push inicial (antes das alterações de código)
- [x] Criar `ImportTeamAttributesParser.kt` (parser de blocos chave-valor com suporte a camelCase, underscore e maiúsculas/minúsculas)
- [x] Criar `ImportTeamAttributesUseCase.kt` (caso de uso para busca e atualização de atletas e seus 26 atributos no banco Room)
- [x] Criar `ImportTeamAttributesViewModel.kt` e `ImportTeamAttributesScreen.kt` (interface Compose com seletor de clube, campo multilinha e resumo de resultado)
- [x] Integrar rota em `LegacyDestination.kt`, `LegacyNavGraph.kt`, `ClubProfileScreen.kt` e `SettingsScreen.kt`
- [x] Criar testes unitários em `ImportTeamAttributesUseCaseTest.kt` (2 jogadores válidos, jogador não encontrado, Foot/Height vazios e campo desconhecido ignorado)
- [x] Compilar e validar a aplicação (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [x] Criar walkthrough e executar commit e push final no GitHub ("feat: importação em lote de atributos por time (TXT) e parser de atleta")

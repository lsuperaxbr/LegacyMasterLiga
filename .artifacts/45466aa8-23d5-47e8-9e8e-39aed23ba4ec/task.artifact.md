# Task List - Sistema Vetorial de Escudos (`ClubBadge`)

- [x] Commit e push inicial (antes das alterações de código)
- [x] Criar `ClubBadgeConfig.kt` (modelo de dados com `toJson`/`fromJson`, extração de inicial e valores padrão)
- [x] Criar `BadgeShapeDrawer.kt` (desenhador de formas: `shield`, `circle`, `diamond`, `hexagon`, `crest`)
- [x] Criar `BadgePatternDrawer.kt` (desenhador de padrões: `solid`, `vertical_stripes`, `horizontal_stripes`, `diagonal_stripes`, `half`, `cross`, `central_circle`)
- [x] Criar `BadgeSymbolDrawer.kt` (desenhador de símbolos: `initial` [1ª letra], `star`, `crown`, `lightning`, `ball`, `tree`, `none`)
- [x] Criar `ClubBadge.kt` (Composable principal do escudo vetorial)
- [x] Atualizar `ClubEntity.kt`, `Club.kt`, `ClubDao.kt`, `DomainMappers.kt` para adicionar a nova coluna `badgeConfigJson`
- [x] Incrementar `DatabaseContract.VERSION` para `32` em `AppDatabase.kt` e adicionar `migration31To32` em `DatabaseModule.kt`
- [x] Atualizar `NativeCrest.kt` para delegar ao novo `ClubBadge` mantendo compatibilidade
- [x] Atualizar o editor de clube em `ClubsScreen.kt` (`ClubEditorDialog`) com o novo editor visual e preview em tempo real
- [x] Aplicar `ClubBadge` com dimensões padronizadas nas telas (Lista: 40.dp, Perfil: 80.dp, Jogos: 32.dp, Classificação: 24.dp)
- [x] Criar testes unitários em `ClubBadgeConfigTest.kt`
- [x] Compilar e validar a aplicação (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [x] Criar walkthrough e executar commit e push final no GitHub ("feat: sistema vetorial de escudos ClubBadge, coluna badgeConfigJson, migração v32 e editor visual")

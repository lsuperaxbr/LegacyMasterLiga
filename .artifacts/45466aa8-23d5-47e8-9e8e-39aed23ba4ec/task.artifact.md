# Task List - Sistema Vetorial de Escudos (`ClubBadge`)

- [/] Commit e push inicial (antes das alterações de código)
- [ ] Criar `ClubBadgeConfig.kt` (modelo de dados, valores padrão e utilitários JSON/string)
- [ ] Criar `BadgeShapeDrawer.kt` (desenhador de formas: shield, circle, diamond, hexagon, crest)
- [ ] Criar `BadgePatternDrawer.kt` (desenhador de padrões: solid, vertical_stripes, horizontal_stripes, diagonal_stripes, half, cross, central_circle)
- [ ] Criar `BadgeSymbolDrawer.kt` (desenhador de símbolos: initial [1ª letra], star, crown, lightning, ball, tree, none)
- [ ] Criar `ClubBadge.kt` (Composable principal do escudo vetorial)
- [ ] Atualizar `ClubEntity.kt`, `Club.kt`, `ClubDao.kt`, `DomainMappers.kt` para adicionar a nova coluna `badgeConfigJson`
- [ ] Incrementar `DatabaseContract.VERSION` para `32` em `AppDatabase.kt` e adicionar `migration31To32` em `DatabaseModule.kt`
- [ ] Atualizar `NativeCrest.kt` para delegar ao novo `ClubBadge` mantendo compatibilidade
- [ ] Atualizar o editor de clube em `ClubsScreen.kt` (`ClubEditorDialog`) com o novo editor visual e preview em tempo real
- [ ] Aplicar `ClubBadge` com dimensões padronizadas (Lista: 40.dp, Perfil: 80.dp, Jogos: 32.dp, Classificação: 24.dp)
- [ ] Criar testes unitários em `ClubBadgeConfigTest.kt`
- [ ] Compilar e validar a aplicação (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [ ] Criar walkthrough e executar commit e push final no GitHub ("feat: sistema vetorial de escudos ClubBadge, coluna badgeConfigJson, migração v32 e editor visual")

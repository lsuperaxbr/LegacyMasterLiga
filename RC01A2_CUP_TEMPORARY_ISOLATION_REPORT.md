# RC-01A2 — Isolamento temporário da Copa

## Resultado

A Copa foi removida da experiência esportiva ativa sem apagar dados nem remover seu motor. O botão **Continuar Copa** permanece no Dashboard e abre exclusivamente a tela **Copa — Em breve**, com o texto aprovado:

> O modo Copa está sendo preparado para uma atualização futura.

A tela possui identidade Material 3 do aplicativo, ícone de troféu e ação Voltar. Ela não instancia ViewModel, repository, DAO ou serviço Firebase.

## Comportamento ativo

- Dashboard → **Continuar Copa** → `CupHub` → `CupComingSoonScreen`.
- O hub da Copa não contém criação, chaveamento, rodadas, resultados, estatísticas, premiação ou encerramento.
- Uma rota antiga de `Schedule` que contenha uma `competitionId` do tipo `CUP` é identificada antes da leitura das rodadas e redirecionada ao `CupHub`.
- Uma rota genérica de `Schedule` expõe somente temporadas do tipo `LEAGUE`.
- A Classificação expõe somente temporadas do tipo `LEAGUE`.
- A criação ativa de competições oferece somente Liga; pedidos de criação do tipo `CUP` também são recusados pelo ViewModel.
- Os seletores de Estatísticas, Premiação e Encerramento acessados pelo módulo Liga mostram somente competições do tipo `LEAGUE`.

## Rotas bloqueadas ou isoladas

| Entrada anterior | Comportamento RC-01A2 |
|---|---|
| `cup?leagueId=...&competitionId=...&seasonId=...` | Exibe somente **Copa — Em breve** |
| `schedule?...competitionId=<CUP>...&tab=matches` | Redireciona para `CupHub`; não observa nem cria rodadas |
| `schedule?...competitionId=<CUP>...&tab=bracket` | Redireciona para `CupHub`; não abre chaveamento |
| `standings` | Lista somente temporadas de Liga |
| `competitions` | Lista e cria somente competições de Liga |

A rota `LeagueHub → Classificação` foi preservada e continua apontando para `standings`.

## Preservação dos dados

Não foram apagados ou removidos:

- entidades Room;
- DAOs;
- migrations;
- repositories do motor da Copa;
- `CupEngine` e `ScheduleGenerator`;
- Copas históricas;
- rodadas e partidas existentes;
- campeões, vice-campeões e histórico;
- configurações ou registros de premiação existentes.

Não houve alteração de schema, versão do banco, migration ou operação destrutiva. O isolamento filtra apenas as opções apresentadas nas telas ativas. Os dados históricos continuam disponíveis para os módulos históricos.

## Classificação da Liga

A fórmula, desempates, pontuação, ordenação e persistência da classificação da Liga não foram modificados. A única alteração foi filtrar o catálogo de temporadas antes da seleção, permitindo apenas `CompetitionType.LEAGUE`.

Foram removidos da experiência ativa da Classificação:

- pódio de Copa;
- botão de chaveamento;
- mensagem de competição eliminatória;
- temporadas de Copa no seletor.

## Arquivos alterados

### Navegação e tela temporária

- `app/src/main/java/com/example/legacymasterliga/core/navigation/CupIsolationPolicy.kt` — novo.
- `app/src/main/java/com/example/legacymasterliga/core/navigation/LegacyNavigationMenu.kt`.
- `app/src/main/java/com/example/legacymasterliga/core/navigation/LegacyNavGraph.kt`.
- `app/src/main/java/com/example/legacymasterliga/feature/cup/presentation/CupComingSoonScreen.kt` — novo.
- `app/src/main/java/com/example/legacymasterliga/feature/hubs/presentation/NavigationHubScreens.kt`.

### Isolamento da experiência ativa

- `app/src/main/java/com/example/legacymasterliga/feature/schedule/domain/ScheduleModels.kt`.
- `app/src/main/java/com/example/legacymasterliga/feature/schedule/data/RoomScheduleRepository.kt` — somente expõe o tipo já persistido da competição; geração não foi alterada.
- `app/src/main/java/com/example/legacymasterliga/feature/schedule/presentation/ScheduleViewModel.kt`.
- `app/src/main/java/com/example/legacymasterliga/feature/schedule/presentation/ScheduleScreen.kt`.
- `app/src/main/java/com/example/legacymasterliga/feature/results/presentation/StandingsViewModel.kt`.
- `app/src/main/java/com/example/legacymasterliga/feature/results/presentation/StandingsScreen.kt`.
- `app/src/main/java/com/example/legacymasterliga/feature/competitions/presentation/CompetitionsViewModel.kt`.
- `app/src/main/java/com/example/legacymasterliga/feature/competitions/presentation/CompetitionsScreen.kt`.
- `app/src/main/java/com/example/legacymasterliga/feature/statistics/presentation/StatisticsViewModel.kt`.
- `app/src/main/java/com/example/legacymasterliga/feature/closure/presentation/SeasonClosureViewModel.kt`.

### Testes

- `app/src/test/java/com/example/legacymasterliga/core/navigation/CupIsolationPolicyTest.kt` — novo.
- `app/src/test/java/com/example/legacymasterliga/core/navigation/LegacyNavigationMenuTest.kt`.
- `app/src/test/java/com/example/legacymasterliga/feature/schedule/presentation/ScheduleViewModelCupIsolationTest.kt` — novo.

## Testes executados

### Testes específicos de isolamento

**14 testes executados; 14 aprovados; 0 falhos.**

Eles confirmam:

- o botão Copa aponta para o `CupHub`;
- o `CupHub` não expõe Classificação ou Schedule;
- a Classificação oficial permanece dentro da Liga;
- temporadas de Copa são removidas dos seletores ativos sem alterar a coleção de origem;
- rotas antigas com `competitionId` de Copa são bloqueadas;
- a rota antiga de Copa não chama `observeSchedule`;
- a rota antiga de Copa não chama `generateSchedule` e, portanto, não cria `RoundEntity`;
- rotas e hubs restantes permanecem únicos.

### Suíte completa

Tarefa Gradle executada:

```text
:app:testDebugUnitTest
```

Resultado: **56 testes executados; 55 aprovados; 1 falho.**

Falha única:

`BackupRestoreIntegrationTest > backup header uses official version and restore reopens clean data`

Essa falha já existia antes da RC-01A2, pertence ao sistema de backup e não foi alterada nesta Sprint. Nenhuma mudança em banco/backup foi feita para mascarar o resultado.

## Build

- `clean`: **BUILD SUCCESSFUL**.
- `:app:compileDebugKotlin`: **BUILD SUCCESSFUL**.
- `:app:assembleDebug`: **BUILD SUCCESSFUL**.
- Warning de empacotamento: `libandroidx.graphics.path.so` foi incluída sem strip; não bloqueia o APK.

O Windows recusou a execução direta do arquivo `gradlew.bat` neste ambiente. As mesmas tarefas do wrapper foram executadas diretamente por `gradle-wrapper.jar`, usando o JBR do Android Studio e o mesmo projeto/cache Gradle.

## APK

Arquivo entregue:

`outputs/LegacyMasterLiga-RC01A2-debug.apk`

- Tamanho: 26.186.659 bytes (24,97 MB).
- SHA-256: `3A7CD83F4077900ACCC4EF3F05E64D382E50E6971009DAD66114FD2DEE763D7C`.

Arquivo original do Gradle:

`app/build/outputs/apk/debug/app-debug.apk`

## Firebase e Login

Nenhum arquivo de Firebase, Firestore ou autenticação foi alterado. Permaneceram intocados:

- `LoginScreen`;
- `LoginViewModel`;
- `OnlineAuthViewModel`;
- `CloudAuthRepository`;
- `FirebaseConnectionChecker`;
- regras e configuração Firestore.

O erro de login será auditado exclusivamente na RC-01B, conforme decisão de produto.

## Encerramento da Sprint

A RC-01A2 termina no isolamento temporário da Copa. Não foi iniciado trabalho de RC-01B, UX-002B ou nova tentativa de correção do motor da Copa.

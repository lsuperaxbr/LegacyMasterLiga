# RC-01A — Relatório técnico de recuperação da Copa

## Estado da entrega

A recuperação do motor e os testes específicos da Copa foram concluídos. A validação oficial completa e o APK ainda não podem ser declarados concluídos porque o sandbox bloqueia o `javac` ao abrir um JAR no cache externo do Gradle. Esse bloqueio não é erro Kotlin nem regressão do motor.

## Escopo respeitado

- Nenhuma funcionalidade nova foi iniciada.
- Nenhuma interface, UX, Dashboard, Login, Firebase, Mercado ou Financeiro foi alterado.
- Nenhuma migration ou versão do Room foi alterada.
- A navegação foi auditada, mas não modificada: a rota aprovada da UX-002A já preserva `leagueId`, `competitionId` e `seasonId` e abre `tab=bracket`.
- Copas novas são exclusivamente mata-mata. Copas antigas com grupos permanecem apenas para histórico e não podem iniciar uma temporada nova nesse formato legado.

## Auditoria do fluxo anterior

Não existiam classes próprias chamadas `CompetitionEngine`, `RoundRepository` ou `MatchRepository`. As responsabilidades do mata-mata estavam fragmentadas entre:

- `CompetitionsViewModel` e `RoomCompetitionRepository`: criação e configuração da Copa;
- `ScheduleViewModel`, `ScheduleGenerator` e `RoomScheduleRepository`: geração inicial;
- `RoomResultsRepository`: gravação do placar, cálculo do vencedor e criação da próxima fase;
- `RoundDao` e `MatchDao`: persistência de rodadas e partidas;
- `RoomSeasonClosureRepository`: campeão, vice, histórico, premiação e encerramento;
- `StandingsViewModel`, `ScheduleScreen`, `StandingsScreen`, `LegacyDestination` e `LegacyNavGraph`: apresentação e navegação para o chaveamento.

A RC-01A criou `CupEngine` como fonte determinística das regras puras do mata-mata, mantendo persistência e telas nas camadas já existentes.

## Causas raiz

1. **Byes reaplicados:** o avanço antigo usava `currentRoundCount <= 2`. Em Copas de jogo único, essa condição continuava verdadeira depois da primeira fase e recolocava clubes já eliminados. Em 8 clubes isso contaminava a semifinal; em 10 clubes quebrava progressivamente o tamanho das fases.
2. **Fase inferida pela partida errada:** o código procurava a última partida global finalizada, em vez de usar a rodada da partida cujo resultado acabara de ser salvo. Resultados lançados fora de ordem podiam bloquear ou avançar a fase errada.
3. **Desempate da volta ignorado:** o vencedor escolhido no segundo jogo era persistido, mas os cálculos posteriores retornavam apenas `winnerClubId` da ida. O clube correto não avançava em empate agregado.
4. **Primeira fase não determinística:** os participantes eram embaralhados e a fase inicial recebia rótulos genéricos. Uma Copa de 2 clubes, por exemplo, não nascia formalmente como Final.
5. **Geração não idempotente:** a presença de rodadas ou partidas fazia a geração lançar erro, em vez de reutilizar o calendário existente. Caminhos concorrentes podiam disputar o mesmo `(seasonId, number)`.
6. **Herança da Liga/grupos:** o fluxo de resultados ainda podia disparar a transição de grupos em Copa. Isso conflitava com a regra oficial de mata-mata exclusivo.

## Correção aplicada

- `CupEngine` calcula fases, nomes, byes, confrontos e vencedor agregado de forma determinística.
- Para 10 clubes, somente 4 disputam a preliminar e 6 recebem bye uma única vez; os 8 classificados seguem para Quartas.
- A rodada disparadora vem diretamente da partida salva.
- O avanço aguarda todas as partidas da fase e agrupa corretamente ida e volta pelo confronto.
- Rodadas são localizadas e reutilizadas por fase/nome; inserções usam `insertIfAbsent` e busca por número já existente.
- Partidas são reutilizadas por rodada, posição de chaveamento e perna, com chave estável por fase.
- A Final é terminal. Depois do resultado, o fechamento automático reutiliza o fluxo existente de campeão, vice, histórico, premiação e encerramento.
- A geração inicial retorna zero criações quando o calendário já existe, preservando resultados.
- Novas Copas são normalizadas para `KNOCKOUT`; o mínimo permitido passou a 2 clubes.

## Matriz validada

| Clubes | Jogo único | Ida e volta | Caminho esperado |
|---:|:---:|:---:|---|
| 2 | Passou | Passou | Final |
| 4 | Passou | Passou | Semifinal → Final |
| 8 | Passou | Passou | Quartas → Semifinal → Final |
| 10 | Passou | Passou | Preliminar → Quartas → Semifinal → Final |
| 16 | Passou | Passou | Oitavas → Quartas → Semifinal → Final |

Também passaram: preservação dos resultados da fase anterior, uma única Final, avanço automático, desempate registrado na volta, campeão, vice, pódio histórico, dois registros de premiação, encerramento único e ausência de números de rodada duplicados.

## Testes criados/fortalecidos

- `CupEngineTest`: 5 testes unitários das regras puras.
- `CupFlowIntegrationTest`: 4 testes de integração, incluindo a matriz completa de 10 configurações.
- O teste de integração executa o Room real em memória e valida `rounds`, `matches`, `season_closures`, `final_standings` e o histórico de premiações.

### Resultados observados

- Testes específicos do `CupEngine`: **5/5 aprovados**.
- Testes específicos do fluxo completo da Copa: **4/4 aprovados**, incluindo as 10 configurações da matriz.
- Suíte completa anterior à limpeza final: **52 executados, 51 aprovados, 1 falho**.
- Única falha: `BackupRestoreIntegrationTest > backup header uses official version and restore reopens clean data`, já existente e fora do escopo da RC-01A.
- `:app:compileDebugKotlin`: **BUILD SUCCESSFUL**.
- `clean`: **BUILD SUCCESSFUL**.
- Nova tentativa de `:app:testDebugUnitTest`: bloqueada em `compileDebugJavaWithJavac` por `AccessDeniedException` no cache externo.
- `:app:assembleDebug`: bloqueado pela mesma causa no `navigationevent-api.jar`; a assinatura debug foi validada com a chave Android local.

O erro de `javac` produz mensagens secundárias de símbolos Kotlin ausentes, mas a causa primária registrada pelo Gradle é:

`AccessDeniedException: C:\Users\luizh\.gradle\caches\9.5.0\transforms\...\navigationevent-api.jar`

## Arquivos modificados

### Motor e persistência

- `app/src/main/java/com/example/legacymasterliga/feature/schedule/domain/CupEngine.kt` (novo)
- `app/src/main/java/com/example/legacymasterliga/feature/schedule/domain/ScheduleGenerator.kt`
- `app/src/main/java/com/example/legacymasterliga/feature/schedule/data/RoomScheduleRepository.kt`
- `app/src/main/java/com/example/legacymasterliga/feature/results/data/RoomResultsRepository.kt`
- `app/src/main/java/com/example/legacymasterliga/feature/competitions/data/RoomCompetitionRepository.kt`
- `app/src/main/java/com/example/legacymasterliga/feature/competitions/presentation/CompetitionsViewModel.kt`
- `app/src/main/java/com/example/legacymasterliga/core/database/dao/RoundDao.kt`
- `app/src/main/java/com/example/legacymasterliga/core/database/dao/MatchDao.kt`
- `app/src/main/java/com/example/legacymasterliga/feature/closure/domain/SeasonClosureRepository.kt`
- `app/src/main/java/com/example/legacymasterliga/feature/closure/data/RoomSeasonClosureRepository.kt`

### Testes

- `app/src/test/java/com/example/legacymasterliga/feature/schedule/domain/CupEngineTest.kt` (novo)
- `app/src/test/java/com/example/legacymasterliga/feature/results/CupFlowIntegrationTest.kt`

## Navegação auditada

O botão **Acessar Chaveamento Completo** usa:

`LegacyDestination.Schedule.createRoute(leagueId, competitionId, seasonId, openBracket = true)`

e gera:

`schedule?leagueId=...&competitionId=...&seasonId=...&tab=bracket`

Portanto não existe redirecionamento para Dashboard e o contexto da competição/temporada não é perdido. Nenhum arquivo de navegação ou tela foi alterado nesta Sprint.

## Warnings e limitações restantes

- Warning Room já existente em `HistoryDao`: `StandingRow.groupIndex` não retornado por uma consulta.
- Warnings existentes de APIs Compose depreciadas e alvos de anotações Kotlin.
- A falha do teste de backup permanece fora do escopo.
- **APK:** não gerado; não há caminho de APK válido para informar enquanto o `assembleDebug` não for executado fora do sandbox ou o cache externo puder ser lido pelo `javac`.

## Validação local ainda necessária para fechar a entrega

Na raiz do projeto, executar no ambiente local já validado:

```powershell
.\gradlew.bat clean
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Se a falha de backup continuar sendo a única falha, ela deve ser tratada em Sprint própria; não deve ser corrigida dentro da RC-01A. O APK esperado, após montagem bem-sucedida, é `app/build/outputs/apk/debug/app-debug.apk`.


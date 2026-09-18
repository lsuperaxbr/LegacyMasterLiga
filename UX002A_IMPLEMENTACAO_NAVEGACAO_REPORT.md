# UX-002A — Relatório de implementação e validação

## Situação da entrega

A reorganização de navegação da UX-002A está implementada e os testes específicos da nova navegação foram aprovados. A Release não deve, porém, ser declarada integralmente validada neste ambiente: a suíte completa terminou com 44 de 45 testes aprovados, e o `assembleDebug` foi interrompido por uma restrição de acesso do sandbox durante o compilador Java. Nenhum APK novo da UX-002A foi gerado ou apresentado como válido.

O `gradlew clean` executado por Luiz fora do sandbox terminou com `BUILD SUCCESSFUL`, confirmando que o bloqueio anterior do `clean` não era causado pelo projeto. Esse resultado não substitui a execução obrigatória de `testDebugUnitTest` e `assembleDebug`.

## Escopo implementado

O Dashboard foi reorganizado como Central de Jogo e passou a expor somente os módulos previstos pela Sprint, respeitando o perfil do usuário:

- Continuar Liga;
- Continuar Copa;
- Mercado;
- Clubes;
- Financeiro;
- Central;
- História;
- Mais.

Foram criados hubs de navegação que reutilizam as telas existentes. Nenhuma tela antiga ou rota anterior foi removida.

## Rotas novas

- `league?leagueId={leagueId}&competitionId={competitionId}&seasonId={seasonId}`;
- `cup?leagueId={leagueId}&competitionId={competitionId}&seasonId={seasonId}`;
- `central`;
- `history_hub`;
- `more`.

Liga e Copa preservam `leagueId`, `competitionId` e `seasonId` quando o contexto está disponível. O chaveamento usa a rota existente de agenda com `tab=bracket`; as rodadas usam `tab=matches`. Quando o contexto da Copa está incompleto, o hub abre a seleção de partidas sem inventar IDs.

Todas as rotas antigas permanecem registradas e acessíveis. Os novos hubs usam `navigateUp()` no retorno, preservando o histórico normal de navegação.

## Botões retirados do Dashboard

Os seguintes acessos deixaram de ser exibidos diretamente na página inicial:

- Classificação;
- Rodadas;
- Encerramento e premiação;
- Inscrições/participantes;
- Usuários;
- Configurações;
- Backup;
- Auditoria;
- Desempenho;
- Exportação/relatórios;
- Guia Beta;
- notícias, avisos e histórico como atalhos duplicados;
- resultados, transferências e demais blocos que repetiam módulos.

As respectivas telas não foram excluídas.

## Hubs e destinos reutilizados

### Liga

- Nova Temporada → Competições;
- Rodadas → Agenda/partidas;
- Classificação → Classificação existente;
- Estatísticas → Estatísticas existentes;
- Premiação e encerramento → Encerramento de temporada existente.

### Copa

- Nova Copa → Competições;
- Chaveamento → Agenda, aba `bracket`;
- Rodadas → Agenda, aba `matches`;
- Estatísticas → Estatísticas existentes;
- Premiação e encerramento → Encerramento existente.

O hub da Copa não oferece Classificação de Liga.

### Central

- Notícias;
- Avisos/notificações.

### História

- Temporadas e campeões;
- Recordes/Hall da Fama.

### Mais

Sistema:

- Backup;
- Auditoria;
- Desempenho;
- Exportação;
- Guia Beta.

Gestão:

- Usuários;
- Configurações.

O hub Mais permanece exclusivo do administrador. Mercado e Financeiro não aparecem para visitante. A agenda e as ações de criação/encerramento continuam respeitando as proteções de perfil já existentes.

## Arquivos de aplicação alterados ou criados

- `app/src/main/java/com/example/legacymasterliga/core/navigation/LegacyDestination.kt`;
- `app/src/main/java/com/example/legacymasterliga/core/navigation/LegacyNavGraph.kt`;
- `app/src/main/java/com/example/legacymasterliga/core/navigation/LegacyNavigationMenu.kt`;
- `app/src/main/java/com/example/legacymasterliga/core/ui/components/LegacyNavigationGrid.kt`;
- `app/src/main/java/com/example/legacymasterliga/feature/dashboard/presentation/DashboardScreen.kt`;
- `app/src/main/java/com/example/legacymasterliga/feature/hubs/presentation/NavigationHubScreens.kt`.

## Testes criados ou ampliados

- `app/src/test/java/com/example/legacymasterliga/core/navigation/LegacyDestinationTest.kt`;
- `app/src/test/java/com/example/legacymasterliga/core/navigation/LegacyNavigationMenuTest.kt`;
- `app/src/androidTest/java/com/example/legacymasterliga/navigation/NavigationSmokeTest.kt`;
- `app/src/androidTest/java/com/example/legacymasterliga/navigation/NavigationHubScreensTest.kt`.

## Resultado da validação

### Testes específicos da UX-002A

Comando filtrado sobre as duas classes de navegação:

- 10 testes executados;
- 10 aprovados;
- 0 falhas;
- 0 ignorados;
- resultado: `BUILD SUCCESSFUL`.

Foram validados por teste unitário:

- construção das cinco rotas novas;
- preservação dos três IDs de contexto;
- seleção da aba de chaveamento e da aba de partidas;
- composição do Dashboard por perfil;
- proteção do menu Mais;
- ausência de Classificação no hub da Copa;
- comportamento seguro quando não existe contexto completo.

### Suíte completa `:app:testDebugUnitTest`

- 45 testes executados;
- 44 aprovados;
- 1 falhou;
- falha: `BackupRestoreIntegrationTest.backup header uses official version and restore reopens clean data`, na validação de que “Liga preservada” existe após reabrir o banco restaurado;
- resultado do comando: `FAILED`.

A falha pertence ao módulo de Backup/Restore e não foi causada nem corrigida nesta Sprint, conforme a proibição de alterar regras e módulos fora da navegação.

### `:app:assembleDebug`

O Kotlin da aplicação, incluindo os arquivos da UX-002A, compilou sem erro real de código. A etapa Java foi interrompida ao fechar uma dependência do cache:

`AccessDeniedException: C:\Users\luizh\.gradle\caches\9.5.0\transforms\...\navigationevent-api.jar`

O mesmo compilador, sob o sandbox, também deixou de resolver as classes Kotlin já geradas. Como isso não corresponde a uma correção de fonte e o build externo já demonstrou que o ambiente local funciona, nenhum arquivo Kotlin ou configuração oficial do projeto foi alterado para contornar o sandbox.

Resultado do comando: `FAILED` no sandbox.

## APK

Não foi gerado APK novo da UX-002A. O arquivo atualmente presente em `app/build/outputs/apk/debug/app-debug.apk` tem data de 02/08/2026 e hash SHA-256 `873ADC7B2687E06A24D5D992E34E1CF5A253807A36F320C865F3C4D2499B7C76`; ele pertence à build anterior e não deve ser entregue como APK desta Sprint.

## Confirmação de isolamento

Não foram modificados nesta Sprint:

- Room, entidades, DAOs ou migrations;
- Firebase, Firestore ou autenticação;
- Competition Engine ou Cup Engine;
- geração e avanço de rodadas;
- Mercado ou Financeiro;
- ViewModels;
- Repositories;
- sincronização online;
- regras de negócio.

A alteração ficou restrita à organização de menu, rotas, hubs de navegação e seus testes.

## Pendências para aprovação final

Em um terminal local sem sandbox, executar:

```text
gradlew :app:testDebugUnitTest
gradlew :app:assembleDebug
```

A UX-002A somente poderá ser marcada como concluída com ambos os comandos aprovados e com um APK novo, cuja data, tamanho, hash e caminho deverão ser registrados neste relatório. A falha preexistente de Backup/Restore precisa ser tratada em escopo próprio ou formalmente aceita antes de considerar a suíte completa verde.

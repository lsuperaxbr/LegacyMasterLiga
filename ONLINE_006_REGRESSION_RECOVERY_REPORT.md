# ONLINE-006 — Relatório de recuperação de regressões

## Status

**CANDIDATO DE RECUPERAÇÃO — NÃO APROVADO PARA ENTREGA FUNCIONAL.**

As correções de código foram aplicadas após comparação por diff, mas a validação obrigatória completa e a geração de um novo APK não puderam ser concluídas neste ambiente. A ONLINE-006 não deve ser considerada corrigida até os testes locais da Copa e o login online serem repetidos no Android Studio/emulador.

## Segurança e comparação obrigatória

- Backup imutável do estado reprovado: `LegacyMasterLiga-ONLINE-006-REPROVADA-BACKUP.zip`.
- Base estável comparada: `LegacyMasterLiga-corrigido.zip`.
- O diff mostrou que a ONLINE-006 não havia alterado diretamente o motor da Copa, a tela de classificação ou as rotas. Ela acrescentou e alterou DAOs, entidades e o gerenciador de sincronização.
- A configuração original de build (`build.gradle.kts`, `app/build.gradle.kts` e `gradle/libs.versions.toml`) foi restaurada byte a byte após as tentativas isoladas de validação.

## Causa da Copa não gerar a Final

Foram identificados dois problemas independentes:

1. **Defeito local preexistente não coberto pelos 21 testes anteriores:** `RoomResultsRepository.saveResult` interpretava toda partida de mata-mata com `leg == 1` como jogo de ida. Partidas únicas também usam `leg == 1`; por isso o vencedor era apagado (`winnerClubId = null`), `advanceKnockout` recebia zero vencedores e não criava a Final.
2. **Regressão de isolamento introduzida pela ONLINE-006:** `LegacyMasterLigaApp` iniciava `OnlineSportsSyncManager` em toda abertura do aplicativo. O gerenciador aceitava usuário Firebase em cache como condição suficiente, registrava listeners para todas as Ligas online locais e aplicava snapshots sem exigir `user_profile`, associação ativa, origem de servidor, revisão válida ou ausência de alteração local pendente. Isso permitia snapshot remoto interferir em rodadas e partidas válidas.

Correções aplicadas:

- jogo único agora define vencedor pelo placar ou desempate explícito;
- ida somente deixa vencedor vazio quando `knockoutLegs == 2`;
- avanço considera exclusivamente a fase atual por `stageLabel`/nome, sem misturar fases antigas ou futuras;
- geração continua idempotente e reutiliza rodadas e partidas existentes;
- nenhuma parte do motor local depende do Firebase.

## Rota incorreta e classificação da Copa

Causa da rota: os botões **Acessar Chaveamento Completo** chamavam `onBack`. O `popBackStack` retornava ao Dashboard e a rota genérica `schedule` não transportava competição, temporada nem aba.

Correções aplicadas:

- rota parametrizada: `schedule?leagueId=...&competitionId=...&seasonId=...&tab=bracket`;
- `competitionId` e `seasonId` são entregues ao `ScheduleViewModel` por `SavedStateHandle`;
- a tela abre diretamente a aba de chaveamento;
- Copa de formato `KNOCKOUT` ou `SINGLE_MATCH` não mostra tabela de Liga como conteúdo principal;
- classificação das Ligas não foi alterada.

Validação executada: `LegacyDestinationTest` passou e confirmou a rota exata com Liga, competição, temporada e `tab=bracket`.

## Login online

Causa observável do erro informado: o fluxo chegava ao Firebase Auth e falhava na leitura de `user_profiles/{uid}` com indisponibilidade do Firestore; o mapeador antigo convertia esse e outros erros diferentes na mesma mensagem genérica **Servidor temporariamente indisponível**. Não há evidência de que senha inválida tenha sido a causa desse caso.

Correções aplicadas:

- validação explícita do projeto Firebase esperado (`legacy-master-liga`);
- timeout separado para autenticação e carregamento do perfil;
- leitura do perfil obrigatoriamente no servidor;
- classificação e registro interno separados para credencial inválida, Auth indisponível, falta de internet, `PERMISSION_DENIED`, perfil ausente, projeto incorreto, timeout, Firestore indisponível e erro inesperado;
- logout imediato quando o perfil não existe, está inativo ou não pode ser carregado;
- sincronização esportiva só começa depois de Auth e perfil ativo concluídos.

Verificações locais:

- `google-services.json` é idêntico ao projeto estável;
- `project_id`: `legacy-master-liga`;
- `mobilesdk_app_id`: `1:1005402262877:android:48c0e8ed4e42ebf3af6cb4`.

Verificações ainda pendentes no Console Firebase/conta válida:

- provedor E-mail/Senha ativado;
- regras Firestore efetivamente publicadas;
- `user_profiles/{uid}` existente e ativo;
- login físico com conta válida.

## Isolamento da ONLINE-006

A sincronização esportiva não inicia mais no `Application.onCreate`.

Condição explícita aplicada por Liga:

`syncEnabled = Auth atual corresponde ao perfil ativo + cloudLeagueId não vazio + membership ACTIVE confirmada no servidor`

Sem as três condições:

- nenhum listener esportivo é registrado;
- nenhum snapshot é aplicado;
- nenhuma operação é enviada;
- Room permanece como fonte local integral.

Proteções adicionais:

- snapshot de cache é ignorado;
- documento remoto exige `schemaVersion`, `cloudId` coerente e `revision >= 1`;
- alteração local pendente ou conflito impede sobrescrita remota;
- partida local finalizada não pode regredir para estado não finalizado;
- rodada de mata-mata remota exige número válido e identificação de fase;
- consulta posterior de documento usa somente servidor e revalida o gate.

## Arquivos corrigidos

- `LegacyMasterLigaApp.kt`
- `RoomResultsRepository.kt`
- `LegacyDestination.kt`
- `LegacyNavGraph.kt`
- `ScheduleViewModel.kt`
- `ScheduleScreen.kt`
- `StandingsScreen.kt`
- `FirestoreCloudAuthRepository.kt`
- `FirebaseErrorMapper.kt`
- `LoginViewModel.kt`
- `OnlineSportsSyncManager.kt`

Testes adicionados:

- `CupFlowIntegrationTest.kt`
- `LegacyDestinationTest.kt`

Arquivos revertidos: nenhum arquivo funcional foi revertido integralmente para a base estável. A inicialização automática de sincronização adicionada pela ONLINE-006 foi removida de `LegacyMasterLigaApp.kt`. As mudanças temporárias usadas para contornar o ambiente de build foram totalmente revertidas.

## Resultado da validação

- Compilação Kotlin completa do aplicativo: concluída sem erro de código durante a validação isolada.
- Teste da rota do chaveamento: aprovado.
- Testes Room da Copa com 4 clubes, jogo único e ida/volta: criados e iniciados, mas bloqueados antes do cenário pela tentativa do Robolectric de baixar `android-all-instrumented`; rede indisponível no sandbox.
- `:app:testDebugUnitTest`: **não aprovado neste ambiente**.
- `:app:assembleDebug`: **não concluído neste ambiente**. O JDK 21.0.10 falhou ao fechar arquivos ZIP/JAR com `AccessDeniedException` sob o sandbox, inclusive `core-for-system-modules.jar` do SDK Android.
- Teste físico offline da Copa: **pendente**.
- Teste físico do login: **pendente**.

## APK

**Nenhum APK novo foi gerado.** Não reutilizar o APK da ONLINE-006 reprovada como se contivesse estas correções.

## Comandos obrigatórios para encerrar a aprovação

No Android Studio ou terminal normal do usuário, com acesso à internet na primeira execução do Robolectric:

```text
gradlew.bat :app:testDebugUnitTest
gradlew.bat :app:assembleDebug
```

Depois, executar separadamente o roteiro físico offline da Copa e o login com uma conta válida. Somente após os dois blocos passarem este relatório pode mudar para **APROVADO** e registrar o caminho do novo APK.

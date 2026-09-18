# RC-01A3 — Consolidação da Base Oficial

Data da consolidação: 05/08/2026  
Base funcional: `work/LegacyMasterLiga`  
Base visual de origem: `C:\Users\luizh\Downloads\Telegram Desktop\LegacyMasterLiga`  
Base oficial de destino: `C:\Users\luizh\Downloads\Telegram Desktop\LegacyMasterLiga`

## 1. Resultado executivo

A base foi consolidada usando a RC-01A2/UX-002A como autoridade funcional e a cópia do Android Studio/Gemini como autoridade exclusivamente visual.

Foram preservados:

- Dashboard e hubs definidos pela UX-002A;
- tela `Copa — Em breve`;
- redirecionamento seguro das rotas antigas da Copa;
- Classificação exclusiva da Liga;
- bloqueio da geração de rodadas de Copa pela interface ativa;
- dados, entidades, DAOs, repositórios, migrations e motor histórico da Copa;
- contratos de estado, callbacks, ViewModels e permissões por perfil;
- implementação existente de Login, Firebase Auth e Firestore, sem tentativa de correção funcional.

Foram incorporados:

- tema azul/ciano;
- paleta, tipografia e superfícies visuais atuais;
- placas em grade e HUD retrô;
- aparência Retro Championship do Login;
- bordas, gradientes, brilhos e animações puramente visuais.

Nenhuma regra de negócio foi modificada nesta Sprint.

## 2. Backup obrigatório

O backup da base visual foi criado e validado antes da primeira alteração:

`outputs/LegacyMasterLiga-visual-pre-RC01A3-backup-20260805.zip`

- tamanho: 624.867 bytes;
- conteúdo: 571 entradas;
- SHA-256: `30CF3C936F6E5AC45A88571156F28869FA60BD7BBDF3667CFFA799DB4B0D0B0B`;
- exclusões verificadas: `.gradle`, `.idea`, `.kotlin`, caches, diretórios `build`, APKs, ZIPs aninhados, temporários e locks.

## 3. Auditoria por diff anterior à alteração

O inventário normalizado, excluindo binários e caches, encontrou:

- 344 arquivos idênticos;
- 49 arquivos existentes nas duas bases com conteúdo diferente;
- 36 arquivos exclusivos da base do Work;
- 5 arquivos exclusivos da base visual;
- 434 arquivos comparados no total.

Os cinco arquivos exclusivos da base visual eram o componente visual `LegacyArcadeComponents.kt` e quatro relatórios antigos (`HOTFIX_RC1_006_REPORT.md`, `HOTFIX_RC1_006B_REPORT.md`, `HOTFIX_RC1_006C_REPORT.md` e `HOTFIX_RC1_006D_REPORT.md`). Os relatórios antigos não foram usados como fonte funcional.

### Classificação das diferenças

| Categoria | Decisão aplicada |
|---|---|
| Somente visual | Importar seletivamente da base visual |
| Navegação UX-002A | Preservar a base do Work |
| Isolamento da Copa RC-01A2 | Preservar a base do Work |
| Lógica de negócio | Preservar a base do Work |
| Firebase/Login | Preservar a implementação funcional do Work; incorporar apenas a composição visual do Login |
| Build e configuração | Preservar a base do Work |

## 4. Arquivos trazidos da base visual

Arquivos visuais copiados integralmente:

- `app/src/main/java/com/example/legacymasterliga/ui/theme/Color.kt`;
- `app/src/main/java/com/example/legacymasterliga/ui/theme/Theme.kt`;
- `app/src/main/java/com/example/legacymasterliga/ui/theme/Type.kt`;
- `app/src/main/java/com/example/legacymasterliga/core/ui/components/LegacyModuleCard.kt`;
- `app/src/main/java/com/example/legacymasterliga/core/ui/components/LegacyArcadeComponents.kt`.

Arquivos consolidados manualmente, sem copiar a lógica antiga da base visual:

- `app/src/main/java/com/example/legacymasterliga/core/ui/components/LegacyNavigationGrid.kt` — passou a renderizar as placas retrô, mantendo `LegacyMenuItem`, rotas e callbacks da UX-002A;
- `app/src/main/java/com/example/legacymasterliga/feature/dashboard/presentation/DashboardScreen.kt` — recebeu HUD, cores e composição atuais, mas continua obtendo os módulos exclusivamente de `LegacyNavigationMenu.dashboard(role)`;
- `app/src/main/java/com/example/legacymasterliga/feature/login/presentation/LoginScreen.kt` — recebeu a aparência Retro Championship, preservando exatamente o estado e os callbacks funcionais do Work.

## 5. Arquivos funcionais preservados da RC-01A2/UX-002A

Entre os arquivos preservados como autoridade funcional estão:

- `core/navigation/LegacyDestination.kt`;
- `core/navigation/LegacyNavigationMenu.kt`;
- `core/navigation/LegacyNavGraph.kt`;
- `core/navigation/CupIsolationPolicy.kt`;
- `feature/cup/presentation/CupComingSoonScreen.kt`;
- `feature/hubs/presentation/NavigationHubScreens.kt`;
- `feature/schedule/presentation/ScheduleScreen.kt`;
- `feature/schedule/presentation/ScheduleViewModel.kt`;
- telas e estado da Classificação da Liga;
- `AppDatabase`, entidades, DAOs, migrations e schemas;
- repositórios de competições, rodadas, partidas, resultados, backup e sincronização;
- `LoginViewModel`, `OnlineAuthViewModel`, `CloudAuthRepository` e implementações Firebase/Firestore;
- regras Firestore e arquivos Gradle da base do Work;
- testes da RC-01A2 e UX-002A.

O motor da Copa foi preservado para histórico, mas não foi reativado na interface.

## 6. Conflitos encontrados e decisão aplicada

### Dashboard

A base visual possuía um menu antigo com acessos diretos e duplicados para competições, inscrições, rodadas, classificação, usuários, auditoria e backup. Copiar esse arquivo integralmente quebraria a UX-002A. Foram mantidos os oito destinos oficiais da Central de Jogo e aplicada somente a aparência azul/ciano, o HUD e as placas em grade.

### Login

A base visual incluía decisões funcionais diferentes para cadastro e autenticação. Esses trechos não foram copiados. O Login consolidado usa o contrato do Work (`LoginUiState` e callbacks existentes) e apenas reproduz a aparência visual atual. `LoginViewModel`, `OnlineAuthViewModel`, Firebase Auth, Firestore e tratamento de erros não foram alterados.

### Copa, Schedule e Classificação

A base visual ainda possuía navegação e comportamentos anteriores ao isolamento. Prevaleceu integralmente a RC-01A2: o botão Copa abre somente `Copa — Em breve`; rotas antigas são desviadas sem criar rodadas; Schedule de Copa não é iniciado; e Classificação permanece exclusiva da Liga.

### Firebase e configuração

As regras Firestore e o diagnóstico Firebase da base visual eram divergentes e não foram importados. Também não foi importada a diferença de versão do Android Gradle Plugin. A configuração funcional e de build permaneceu a do Work.

## 7. Validações funcionais e visuais

- Dashboard: composição azul/ciano, HUD e placas atuais presentes; destinos vêm somente do menu UX-002A.
- Login: composição Retro Championship presente; nenhum fluxo novo de cadastro, ViewModel ou chamada Firebase foi introduzido.
- Copa: destino oficial aponta para `CupComingSoonScreen`.
- Rotas antigas da Copa: política de isolamento preservada.
- Liga: hub e destino de Classificação preservados.
- Classificação: acesso de Copa removido da experiência ativa; cálculo da Liga não foi modificado.
- Firebase/Login: arquivos funcionais não foram modificados.

A validação visual desta entrega foi feita por inspeção estrutural do Compose e por compilação completa. A aceitação visual final em tela física depende da instalação do APK no aparelho/emulador.

## 8. Testes executados

### Limpeza

Equivalente Gradle Wrapper de `gradlew.bat clean`:

- resultado: `BUILD SUCCESSFUL`;
- duração: 27 s;
- tarefas: 1 executada.

### Testes unitários

Equivalente Gradle Wrapper de `gradlew.bat :app:testDebugUnitTest`:

- total: 56;
- aprovados: 55;
- falhos: 1;
- resultado global: falhou devido a uma limitação preexistente da base RC-01A2.

Falha preexistente e fora do escopo:

- `BackupRestoreIntegrationTest.backup header uses official version and restore reopens clean data`;
- linha: `BackupRestoreIntegrationTest.kt:81`;
- ponto: após reabrir o banco no Robolectric, o teste não encontra `Liga preservada`;
- não houve alteração em banco, backup ou restore nesta Sprint.

Testes diretamente relacionados à consolidação funcional:

- `CupIsolationPolicyTest`: 3/3;
- `LegacyDestinationTest`: 4/4;
- `LegacyNavigationMenuTest`: 6/6;
- `ScheduleViewModelCupIsolationTest`: 1/1;
- total de navegação/isolamento: 14/14 aprovados.

### APK

Equivalente Gradle Wrapper de `gradlew.bat :app:assembleDebug`:

- resultado: `BUILD SUCCESSFUL`;
- duração: 1 min 24 s;
- tarefas: 43;
- warning: `libandroidx.graphics.path.so` não pôde ser stripped e foi empacotada sem alteração;
- o warning não impediu a geração do APK.

O `.bat` direto continuou sujeito às restrições de execução do ambiente; os comandos foram executados pela classe oficial do mesmo Gradle Wrapper (`gradle/wrapper/gradle-wrapper.jar`), usando o JBR do Android Studio.

## 9. Artefatos

### APK consolidado

`outputs/LegacyMasterLiga-RC01A3-debug.apk`

- tamanho: 26.203.043 bytes;
- SHA-256: `66D864B8A58F741DACB4D2B76C7C4B13AD2CA44F58812599023CA6993E4C97AC`.

### Projeto consolidado

`outputs/LegacyMasterLiga-RC01A3-consolidated.zip`

- tamanho: 715.869 bytes;
- entradas: 650;
- SHA-256: `73AFD2ED7E09E50CD4E5780B423A504CE4E69BD4EDD5DAD7C4333CD16DED1625`;
- entradas proibidas encontradas na validação: 0.

O ZIP não inclui caches, diretórios de build, APKs aninhados, arquivos temporários, configurações locais do Android Studio nem este relatório externo de entrega.

## 10. Limitações restantes

- A falha preexistente do teste de restauração de backup permanece registrada e não foi corrigida por estar fora do escopo da RC-01A3.
- O Login Online continua com o comportamento anterior; sua auditoria funcional pertence à RC-01B.
- A Copa continua temporariamente isolada; seu motor não foi corrigido nem reativado.
- Nenhuma etapa da RC-01B ou UX-002B foi iniciada.

## 11. Encerramento

A publicação seletiva na pasta oficial do Android Studio foi concluída após o backup:

- destino: `C:\Users\luizh\Downloads\Telegram Desktop\LegacyMasterLiga`;
- arquivos comparados após a publicação: 442;
- arquivos ausentes no destino: 0;
- arquivos com hash diferente: 0;
- caches, builds, APKs, ZIPs, temporários e `local.properties` não foram copiados.

A RC-01A3 termina na consolidação, publicação e empacotamento desta base. Não foram iniciadas novas funcionalidades nem alterações de regra de negócio.

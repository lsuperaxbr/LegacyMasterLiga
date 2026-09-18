# Beta Stabilization 01 — Build Analyzer

## Estado confirmado

A primeira compilação real executada no Android Studio terminou com `BUILD SUCCESSFUL` em aproximadamente 18 segundos. O erro observado depois do build foi o encerramento do AVD Pixel 6, portanto não era uma falha do código nem do Gradle.

## Ajustes aplicados

- Configuration Cache preservado e já reutilizado pelo build real.
- Build Cache preservado.
- Execução paralela ativada para tarefas independentes.
- KSP incremental explicitamente ativado.
- Jetifier desativado, pois o projeto utiliza somente bibliotecas AndroidX.
- `R` não transitivo e IDs de recursos não finais ativados para reduzir recompilações.
- Foojay já havia sido removido na Sprint 033, evitando resolução externa desnecessária.
- Gradle, AGP, Kotlin, KSP, Room, Hilt e Compose foram mantidos, pois o build real comprovou compatibilidade.

## Como gerar um relatório local

No Windows, execute:

```bat
scripts\beta_build_profile.bat
```

O comando gera:

- log completo em `build-reports\beta-stabilization-01.log`;
- perfil HTML do Gradle em `build\reports\profile`;
- APK Beta em `app\build\outputs\apk\beta\app-beta.apk`.

## Avisos que não bloqueiam o aplicativo

- “New Minor Gradle Version Available” é apenas informativo. Não atualize o Gradle durante a estabilização.
- “Build Analyzer detected new build performance issues” não significa falha. O detalhe precisa ser aberto no próprio Android Studio para identificar a tarefa apontada.
- O encerramento do AVD Pixel 6 é um problema do emulador, não da compilação.

## Política até a RC1

Durante a estabilização, versões de dependências não devem ser atualizadas sem um erro real reproduzível. O objetivo é reduzir mudanças e preservar o build já confirmado como bem-sucedido.

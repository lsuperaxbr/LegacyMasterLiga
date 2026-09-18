# Beta Stabilization 02 — Lint

## Base

Projeto derivado de `LegacyMasterLiga_BetaStabilization01.zip`.

## Alterações

- Android Lint configurado para as variantes do aplicativo, com relatórios HTML, XML e SARIF.
- Verificação de dependências habilitada.
- Alertas puramente informativos de atualização de Gradle/dependências desativados durante a estabilização.
- Idioma `pt-BR` declarado por `localeConfig`.
- Atributo redundante `screenOrientation="unspecified"` removido do Manifest.
- Textos da tela de Login e dos estados compartilhados movidos para `strings.xml`.
- Descrições acessíveis do botão de mostrar/ocultar senha preservadas por recursos.
- Scripts Windows/Linux adicionados para executar `lintBeta` e salvar o log.
- Verificador estático offline adicionado para ambientes sem acesso ao Gradle.

## Execução

A tentativa de executar `./gradlew lintBeta` neste ambiente não concluiu porque `services.gradle.org` não pôde ser resolvido. A revisão estática local foi executada com sucesso e não encontrou erros estruturais; os avisos remanescentes foram documentados para o Lint real no Android Studio.

## Banco de dados

Room permanece na versão 12. Nenhuma migração foi necessária.

## Versão

- `versionCode 39`
- `versionName 1.0.0-beta09`

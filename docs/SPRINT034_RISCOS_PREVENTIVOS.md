# Sprint 034 — riscos preventivos revisados

## Gradle e Java

- O projeto usa o Gradle Wrapper presente no ZIP.
- A compilação deve usar o JDK incorporado do Android Studio.
- Os scripts de compilação usam `--no-configuration-cache` na primeira geração do APK para reduzir interferências de cache antigo.

## Pasta do projeto

- A pasta aberta no Android Studio deve conter `settings.gradle.kts`.
- Caminhos curtos evitam problemas do Windows com arquivos profundos.
- `local.properties` não é distribuído; o Android Studio o cria automaticamente com o caminho do SDK.

## KSP, Room e Hilt

- O Room Gradle Plugin possui `schemaDirectory` configurado.
- O schema Room 12 será exportado pelo KSP na primeira compilação completa.
- Hilt e KSP já estão configurados no módulo `app`.

## APK Beta

- A variante Beta usa um identificador separado e pode coexistir com Debug/Release.
- O primeiro APK Beta não precisa de keystore de lançamento.
- A assinatura oficial será exigida somente para a futura variante Release.

## Quando pedir correção

Envie o arquivo `build-reports/sprint034-beta-build.log` completo. A primeira seção `What went wrong` costuma indicar o arquivo e a linha que precisam ser corrigidos.

# Validação final da RC1

## Escopo

Revisão final da variante `rc` sem inclusão de funcionalidades.

Comandos previstos:

```bash
./gradlew testDebugUnitTest lintRc assembleRc --stacktrace --warning-mode all
```

## Resultado neste ambiente

A execução foi iniciada, mas o Gradle Wrapper não pôde baixar `gradle-9.4.1-bin.zip` porque este ambiente não resolve `services.gradle.org` (`UnknownHostException`). Portanto, não houve resultado real de `testDebugUnitTest`, `lintRc` ou `assembleRc` neste ambiente e nenhum erro de código foi inventado.

## Verificações concluídas

- verificação estática do projeto: aprovada;
- 23 rotas de navegação únicas;
- Room na versão 12, com aviso conhecido de schema ainda não exportado;
- Manifest válido;
- variante `rc` configurada com R8 e redução de recursos;
- assinatura RC usa a chave privada quando configurada e, na ausência dela, a chave debug para testes;
- FileProvider não exportado;
- backup nativo do Android desativado;
- tráfego HTTP não criptografado bloqueado;
- nenhum `TODO()` ou `NotImplementedError` encontrado;
- ZIP sem APKs, keystores ou senhas privadas.

## Avisos conhecidos

- 247 textos literais em Compose ainda aguardam internacionalização gradual;
- 18 ícones usam `contentDescription = null` e devem permanecer assim somente quando forem decorativos;
- o schema Room 12 será exportado pelo KSP na primeira compilação completa.

Esses avisos não foram tratados como erros de compilação porque ainda não existe um relatório real do Android Lint da variante RC.

## Validação no Windows

Execute:

```bat
scripts\validate_rc1_final.bat
```

O script executa separadamente testes, Lint e build RC, preservando um log específico para cada etapa. Quando aprovado, copia o APK para:

```text
rc-package\LegacyMasterLiga-1.0.0-rc01.apk
```

## Critério de aprovação

A RC1 estará aprovada para instalação quando as três etapas terminarem com sucesso:

1. `testDebugUnitTest`;
2. `lintRc`;
3. `assembleRc`.

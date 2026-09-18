# Sprint 036 — abrir, compilar e instalar a Beta

## 1. Extraia o ZIP

No Windows, clique com o botão direito no ZIP e escolha **Extrair tudo**.

Abra no Android Studio a pasta que contém estes arquivos:

- `settings.gradle.kts`
- `build.gradle.kts`
- `gradlew.bat`
- pasta `app`

Não abra a pasta `app` sozinha.

## 2. Aguarde o Android Studio

Na primeira abertura, permita que o Android Studio:

1. crie o `local.properties`;
2. localize o Android SDK;
3. baixe o Gradle 9.4.1;
4. baixe as dependências do projeto;
5. execute o Gradle Sync.

Use o **JDK 17** em:

`File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK`

Selecione `jbr-17`, `Embedded JDK 17` ou outro JDK 17 instalado.

## 3. Gere o APK pelo jeito mais simples

Na pasta do projeto, execute:

```text
scripts\sprint036_diagnostico_e_build.bat
```

O script:

- verifica o Java;
- executa os testes unitários;
- compila a variante Beta;
- salva o log completo;
- copia o APK para uma pasta fácil de encontrar.

## 4. Onde ficará o APK

```text
beta-package\LegacyMasterLiga-1.0.0-beta07.apk
```

## 5. Instale no celular

1. Envie o APK para o celular por cabo, Drive, WhatsApp ou Telegram.
2. Toque no arquivo.
3. Autorize temporariamente **Instalar apps desconhecidos** para o aplicativo usado para abrir o APK.
4. Instale.

## 6. Primeiro login

```text
Usuário: admin
Senha: admin123
```

## 7. Quando aparecer um erro

Não tente mudar vários arquivos.

Envie ao Legacy este arquivo:

```text
build-reports\sprint036-build.log
```

Também envie uma captura da primeira mensagem vermelha exibida pelo Android Studio.

## Erros que não são defeitos do código

- falha de internet ao baixar o Gradle;
- SDK 36 ainda não instalado;
- JDK diferente do 17;
- antivírus ou firewall bloqueando o Gradle;
- projeto aberto pela pasta errada.

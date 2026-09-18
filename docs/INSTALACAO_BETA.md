# Instalação da Beta — Legacy Master Liga

## Requisitos

- Windows 10 ou superior.
- Android Studio com JDK 17 configurado.
- Conexão com a internet na primeira sincronização do Gradle.
- Celular Android 7.0 (API 24) ou superior.

## Abrir o projeto

1. Extraia o ZIP em uma pasta simples, por exemplo `C:\Projetos\LegacyMasterLiga`.
2. No Android Studio, selecione **Open**.
3. Escolha a pasta `LegacyMasterLiga` que contém `settings.gradle.kts`.
4. Aguarde a mensagem **Gradle Sync Finished**.
5. Não abra uma subpasta como `app` isoladamente.

## Gerar APK de teste

### Pelo Android Studio

1. Abra o menu **Build**.
2. Escolha **Build App Bundle(s) / APK(s)** e depois **Build APK(s)**. Em versões onde essa opção mudou, abra a janela Gradle e execute `app > Tasks > build > assembleBeta`.
3. O APK Beta ficará em:

```text
app/build/outputs/apk/beta/app-beta.apk
```

O APK de depuração pode ser gerado com `assembleDebug` e ficará em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Pelo terminal do Windows

```bat
gradlew.bat clean assembleBeta
```

## Instalar no celular

1. Copie o APK para o celular.
2. Abra o arquivo.
3. Autorize a instalação de aplicativos dessa fonte, caso o Android solicite.
4. A versão Beta usa o identificador `.beta`, podendo coexistir com a futura versão oficial.

## Primeiro acesso

```text
Usuário: admin
Senha: admin123
```

Troque a senha assim que o aplicativo oferecer essa opção.

## Problemas comuns

- **Gradle não baixa:** confira a internet, proxy e firewall.
- **SDK 36 ausente:** use o SDK Manager para instalar o Android SDK exigido pelo projeto.
- **Celular não aparece:** gere o APK e instale manualmente; conexão USB não é obrigatória.
- **Erro de Java:** configure o Gradle JDK para o JDK 17 ou o JDK incorporado compatível do Android Studio.

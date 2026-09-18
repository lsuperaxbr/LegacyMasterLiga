# Guia simples — abrir, sincronizar e gerar o APK Beta

Este guia foi preparado para **Android Studio Quail 1 | 2026.1.1** no Windows.

## 1. Extrair o projeto

1. Clique com o botão direito no ZIP da Sprint 031.
2. Escolha **Extrair tudo**.
3. Coloque a pasta em um caminho curto, por exemplo:

```text
C:\LegacyMasterLiga
```

Evite abrir o projeto diretamente dentro do ZIP.

## 2. Abrir no Android Studio

1. Abra o Android Studio.
2. Clique em **Open**.
3. Selecione a pasta que contém `settings.gradle.kts`.
4. Clique em **Trust Project**, caso apareça essa pergunta.

## 3. Aguardar a sincronização

Na primeira abertura, o Android Studio poderá baixar:

- Gradle 9.4.1;
- Android Gradle Plugin 9.2.1;
- dependências do Compose, Room e Hilt;
- Android SDK 36, se ainda não estiver instalado.

O primeiro download pode demorar. Não feche o Android Studio enquanto a barra inferior indicar sincronização.

## 4. Sincronizar manualmente

Use uma destas opções:

- clique no ícone do elefante/Gradle na barra superior; ou
- abra **File > Sync Project with Gradle Files**.

Quando terminar, procure a mensagem **BUILD SUCCESSFUL** ou **Gradle sync finished**.

## 5. Verificação simples pelo Terminal

No Android Studio, abra **View > Tool Windows > Terminal** e execute:

```bat
python scripts\verify_project.py
```

Depois execute:

```bat
gradlew.bat testDebugUnitTest assembleDebug assembleBeta
```

## 6. Local dos APKs

Após o build:

```text
app\build\outputs\apk\debug\app-debug.apk
app\build\outputs\apk\beta\app-beta.apk
```

Use `app-beta.apk` para os primeiros testes da liga.

## 7. Login inicial

```text
Usuário: admin
Senha: admin123
```

Troque essa senha assim que o aplicativo oferecer essa opção.

## 8. Quando aparecer um erro

Não tente alterar vários arquivos ao mesmo tempo.

1. Copie a primeira mensagem vermelha completa do painel **Build**.
2. Tire uma captura de tela, se for mais fácil.
3. Envie o erro junto com o ZIP atual do projeto.

A primeira mensagem costuma ser a causa; as mensagens seguintes podem ser apenas consequências.

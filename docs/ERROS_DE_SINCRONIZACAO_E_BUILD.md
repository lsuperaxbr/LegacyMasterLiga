# Erros comuns de sincronização e build

## Gradle não consegue baixar arquivos

Confirme que o computador possui internet e que antivírus, proxy ou firewall não estão bloqueando:

```text
services.gradle.org
plugins.gradle.org
dl.google.com
repo.maven.apache.org
```

Depois use **File > Sync Project with Gradle Files**.

## Android SDK 36 não instalado

Abra:

```text
Tools > SDK Manager > SDK Platforms
```

Marque **Android API 36** e instale.

## Java incorreto

Abra:

```text
File > Settings > Build, Execution, Deployment > Build Tools > Gradle
```

Em **Gradle JDK**, selecione o JDK incorporado do Android Studio, que deve ser Java 17 ou superior.

## Cache do Gradle inconsistente

Tente nesta ordem:

1. **File > Sync Project with Gradle Files**.
2. Feche e abra o Android Studio.
3. Execute no terminal:

```bat
gradlew.bat --stop
gradlew.bat clean
```

Evite apagar pastas do projeto antes de registrar a mensagem de erro.

## Falha em uma migração do Room

Não desinstale o aplicativo imediatamente se houver dados importantes.

Envie:

- a mensagem completa do erro;
- a versão instalada anteriormente;
- o ZIP usado para gerar o APK.

## O APK não aparece

Execute:

```bat
gradlew.bat assembleBeta
```

O arquivo esperado é:

```text
app\build\outputs\apk\beta\app-beta.apk
```

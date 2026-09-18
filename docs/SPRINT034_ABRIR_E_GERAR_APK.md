# Sprint 034 — abrir o projeto e gerar o primeiro APK Beta

Este é o roteiro mais simples possível. Não é necessário copiar código nem alterar arquivos.

## 1. Extrair o ZIP

Extraia `LegacyMasterLiga_Sprint034.zip` para uma pasta curta, por exemplo:

```text
C:\LegacyMasterLiga
```

Evite abrir o projeto diretamente de dentro do ZIP.

## 2. Abrir a pasta correta

No Android Studio:

1. Clique em **Open**.
2. Abra a pasta `LegacyMasterLiga` que contém `settings.gradle.kts`.
3. Clique em **Trust Project**, caso apareça.

Não selecione apenas a pasta `app`.

## 3. Escolher o JDK do Android Studio

Abra:

```text
File > Settings > Build, Execution, Deployment > Build Tools > Gradle
```

Em **Gradle JDK**, escolha o JDK incorporado do Android Studio. O projeto está configurado para Java 17.

## 4. Aguardar o Gradle Sync

Na primeira abertura, o Android Studio precisa baixar o Gradle e as dependências. Isso pode demorar alguns minutos.

Se aparecer uma barra com **Sync Now**, clique nela. Caso contrário, use:

```text
File > Sync Project with Gradle Files
```

Só avance depois de aparecer uma mensagem de sincronização concluída.

## 5. Gerar o APK Beta

Abra o Terminal dentro do Android Studio e execute:

```bat
scripts\generate_first_beta_apk.bat
```

O APK será criado em:

```text
app\build\outputs\apk\beta\app-beta.apk
```

## 6. Em caso de erro

O script salva o erro completo em:

```text
build-reports\sprint034-beta-build.log
```

Envie esse arquivo na conversa. Não tente alterar versões ou apagar arquivos aleatoriamente.

## Login da Beta

```text
Usuário: admin
Senha: admin123
```

## Observações

- A primeira sincronização exige internet.
- Depois de baixar as dependências, o Gradle reutiliza o cache local.
- O aviso do schema Room 12 deve desaparecer depois que o KSP concluir a primeira compilação.

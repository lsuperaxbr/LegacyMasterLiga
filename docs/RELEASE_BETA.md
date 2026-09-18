# Preparação de APKs — Beta e Release

## Variantes disponíveis

- `debug`: desenvolvimento e diagnóstico.
- `beta`: testes reais, instalável paralelamente à versão oficial.
- `release`: pacote final, que deve ser assinado com uma chave privada.

## Comandos

### Windows

```bat
gradlew.bat clean testDebugUnitTest assembleDebug assembleBeta
```

### Linux/macOS

```bash
./gradlew clean testDebugUnitTest assembleDebug assembleBeta
```

## Assinatura de Release

1. Crie uma chave no Android Studio em **Build > Generate Signed App Bundle or APK**.
2. Guarde o arquivo `.jks` em local seguro e faça uma cópia externa.
3. Copie `keystore.properties.example` para `keystore.properties`.
4. Preencha o caminho e as senhas.
5. Execute:

```bat
gradlew.bat clean assembleRelease
```

Sem `keystore.properties`, o projeto continua gerando Debug e Beta normalmente, mas o Release não será assinado automaticamente.

## Saídas esperadas

```text
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/beta/app-beta.apk
app/build/outputs/apk/release/app-release.apk
```

## Política da Beta

- Não apagar dados de testes sem backup.
- Criar um backup antes de atualizar para outra Beta.
- Não distribuir a chave de assinatura.
- Relatar erros antes de usar a Beta em uma temporada oficial.

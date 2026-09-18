# RC1 — assinatura e geração

## APK RC para teste

Sem criar uma chave de lançamento, execute no Windows:

```bat
scripts\build_rc1.bat
```

A variante `rc` usa a assinatura de depuração apenas quando `keystore.properties` não existe. Ela é própria para teste e pode coexistir com outros APKs pelo sufixo `.rc`.

Saída esperada:

```text
app\build\outputs\apk\rc\app-rc.apk
```

## APK Release oficial

1. Gere e guarde uma chave privada `.jks` em local seguro.
2. Copie `keystore.properties.example` para `keystore.properties`.
3. Preencha os quatro campos reais.
4. Execute:

```bat
scripts\build_release.bat
```

Saída esperada:

```text
app\build\outputs\apk\release\app-release.apk
```

Nunca envie a chave, as senhas ou o arquivo `keystore.properties` em ZIPs do projeto.

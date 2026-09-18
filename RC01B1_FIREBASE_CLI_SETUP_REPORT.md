# RC-01B1 — Preparação Oficial do Firebase CLI

Data: 05/08/2026  
Projeto Firebase: `legacy-master-liga`  
Escopo: somente configuração de publicação das regras Firestore

## Resultado

A base oficial está preparada para executar:

```text
firebase deploy --only firestore:rules
```

Nenhum arquivo Kotlin, regra de negócio, tela, autenticação, navegação ou módulo do aplicativo foi alterado.

## Arquivos de configuração

### `firebase.json`

Define o arquivo que o Firebase CLI deve publicar:

```json
{
  "firestore": {
    "rules": "firestore.rules"
  }
}
```

- tamanho: 56 bytes;
- SHA-256: `E87125F3EC6439A59BA44D80A6DCC46378A27A0644B0CDA30ABEAFD2EFB67E20`.

### `.firebaserc`

Define o projeto padrão:

```json
{
  "projects": {
    "default": "legacy-master-liga"
  }
}
```

- tamanho: 60 bytes;
- SHA-256: `EA14FD5B5BB01F7C306093F1CD112BC6AE688CDBEDCA64F810A6D5BAE4DD0E80`.

Observação: `.firebaserc` é um arquivo oculto no Windows. Para exibi-lo no PowerShell, use `Get-ChildItem -Force`.

### `firestore.rules`

As regras atuais foram preservadas sem nenhuma modificação.

- tamanho: 5.870 bytes;
- SHA-256 antes e depois da RC-01B1: `B754C7478B330C95218A5488CC275CBD802E8BE09FC6F971241A26088651E202`.

## `firestore.indexes.json`

Não foi criado.

Esse arquivo não é necessário para `firebase deploy --only firestore:rules`. Criar um arquivo vazio sem uma necessidade real poderia substituir ou confundir a configuração de índices em uma publicação futura. Os índices devem ser exportados ou declarados somente quando uma Sprint específica de consultas Firestore exigir isso.

## Validações executadas

- `firebase.json` convertido e validado como JSON;
- `.firebaserc` convertido e validado como JSON;
- referência `firestore.rules` resolvida para um arquivo existente;
- projeto padrão confirmado como `legacy-master-liga`;
- Firebase CLI 15.25.1 executado sem modificar o projeto remoto;
- configuração presente na base Work e na base oficial;
- hashes dos três arquivos idênticos entre as duas bases;
- pacote verificado com exatamente três entradas e nenhum arquivo inesperado.

O Firebase CLI não possui sessão autenticada neste computador. A validação remota parou antes de qualquer publicação com a mensagem `Failed to authenticate, have you run firebase login?`.

## Como publicar

Abra um terminal na pasta oficial:

```text
C:\Users\luizh\Downloads\Telegram Desktop\LegacyMasterLiga
```

Autentique o Firebase CLI:

```text
firebase login
```

Confirme o projeto selecionado:

```text
firebase use legacy-master-liga
```

Publique somente as regras:

```text
firebase deploy --only firestore:rules
```

Também é possível informar o projeto explicitamente:

```text
firebase deploy --only firestore:rules --project legacy-master-liga
```

O comando não publica APK, Auth, Hosting, Functions ou índices. Ele envia somente `firestore.rules`.

## Verificação após a publicação

O terminal deve informar a compilação e a liberação das regras sem erros. Depois, no Console Firebase:

1. abrir o projeto `legacy-master-liga`;
2. acessar Firestore Database;
3. abrir a aba Regras;
4. confirmar que a publicação possui a data/hora atual;
5. testar o Login Online separadamente com uma conta válida.

## Pacote entregue

`outputs/RC01B1_FIREBASE_CLI_CONFIG.zip`

- conteúdo: `firebase.json`, `.firebaserc` e `firestore.rules`;
- entradas: 3;
- arquivos ausentes: 0;
- arquivos inesperados: 0;
- tamanho: 1.723 bytes;
- SHA-256: `4B365F4A8FF76B9DEDFC970DE3F68BD538A134AE6C2D3BE2B52860D7F3A626FC`.

## Confirmação de escopo

Não foram alterados Login, Firebase Auth, código Firestore, Liga, Copa, Mercado, Financeiro, UX, Compose, navegação, banco ou qualquer funcionalidade do aplicativo.

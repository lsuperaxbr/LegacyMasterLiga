# Plano de Correção: Permissões do Firestore e Robustez do Sync

Corrigiremos a falha de sincronização causada por `PERMISSION_DENIED` na consulta de `collectionGroup`, garantindo que o sistema de descoberta de ligas seja resiliente a falhas parciais e que as regras de segurança do Firebase permitam a consulta necessária.

## User Review Required

> [!IMPORTANT]
> Esta correção exige a atualização das **Regras de Segurança do Firestore** no Console do Firebase após o deploy do código. Sem isso, a consulta global continuará falhando (embora o app agora vá ignorar o erro e seguir com as ligas verificadas diretamente).

## Proposed Changes

### [Firebase Configuration]

#### [MODIFY] [firestore.rules](file:///C:/Users/luizh/Downloads/Telegram Desktop/LegacyMasterLiga/firestore.rules)
- Adicionar regra de recursão para a coleção `members`, permitindo consultas do tipo `collectionGroup` para usuários autenticados.

```rules
match /{path=**}/members/{memberId} {
  allow read, write: if signedIn();
}
```

### [Online Sync Component]

#### [MODIFY] [OnlineSportsSyncManager.kt](file:///C:/Users/luizh/Downloads/Telegram Desktop/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/online/sync/OnlineSportsSyncManager.kt)

1.  **Isolamento da Consulta Global**: Envolver o bloco que executa `firestore.collectionGroup("members")` em um `try-catch` específico.
2.  **Continuidade do Fluxo**: Se a consulta global falhar, logar um aviso (`syncLog`), mas permitir que o código prossiga para a comparação de `verifiedRoles` com o cache atual.
3.  **Preservação do Progresso**: Garantir que as ligas encontradas via Perfil do Usuário sejam aplicadas mesmo que o Scan Global falhe.

## Verification Plan

### Manual Verification
1.  **Aplicar Regras**: Copiar o conteúdo de `firestore.rules` para o Console do Firebase e publicar.
2.  **Limpeza**: No app, abrir o "Diagnóstico de Sync" e verificar se o erro de permissão desapareceu (após o refresh automático).
3.  **Teste de Sync**: Realizar uma transferência ou alteração financeira.
4.  **Confirmar Envio**:
    - Verificar na tela de Diagnóstico se a seção "Fila PENDING" é esvaziada.
    - Confirmar no Firestore Console se os documentos de `financial_transactions` ou `clubs` foram atualizados.

### Instruções para o Usuário
Após eu concluir as alterações nos arquivos locais:
1. Abra o arquivo `firestore.rules` no seu editor.
2. Copie todo o conteúdo.
3. Vá ao **Firebase Console** > **Firestore Database** > Aba **Rules**.
4. Cole o conteúdo e clique em **Publish**.

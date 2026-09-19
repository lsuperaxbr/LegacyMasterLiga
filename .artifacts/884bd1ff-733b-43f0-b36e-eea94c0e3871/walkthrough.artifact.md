# Walkthrough: Resiliência de Sincronismo e Permissões Firestore

Corrigimos a falha de sincronização causada por erros de permissão (`PERMISSION_DENIED`) no Firestore e tornamos o processo de descoberta de ligas mais resiliente.

## Mudanças Realizadas

### [Configuração do Firebase]

#### [firestore.rules](file:///C:/Users/luizh/Downloads/Telegram Desktop/LegacyMasterLiga/firestore.rules)
- **Suporte a Collection Group**: Adicionada uma regra recursiva que permite consultas globais na coleção `members`. Isso é essencial para que o app descubra em quais ligas o usuário está inscrito sem precisar saber os IDs das ligas antecipadamente.

### [Componente de Sincronismo]

#### [OnlineSportsSyncManager.kt](file:///C:/Users/luizh/Downloads/Telegram Desktop/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/online/sync/OnlineSportsSyncManager.kt)
- **Isolamento de Erros**: A consulta de Scan Global (que disparava o erro de permissão) agora está isolada em seu próprio bloco `try-catch`.
- **Resiliência**: Se o Scan Global falhar (por rede ou permissão), o app não interrompe mais o processo de sincronismo. Ele prossegue utilizando as ligas já confirmadas através do perfil do usuário, garantindo que o sincronismo funcione mesmo em cenários de erro parcial.
- **Continuidade**: Após o refresh, o sistema dispara automaticamente o envio dos itens pendentes na fila.

## Instruções de Publicação (Ação Necessária)

> [!IMPORTANT]
> Você deve atualizar as regras no console do Firebase para que a correção tenha efeito completo:
> 1. Abra o arquivo [firestore.rules](file:///C:/Users/luizh/Downloads/Telegram Desktop/LegacyMasterLiga/firestore.rules) no Android Studio.
> 2. Copie todo o texto.
> 3. Vá ao [Firebase Console](https://console.firebase.google.com/).
> 4. Acesse **Firestore Database** > aba **Rules**.
> 5. Cole o código e clique em **Publish**.

## Como Verificar a Correção

1. **Abra o Diagnóstico**: Vá em **Configurações** > **Diagnóstico de Sync**.
2. **Observe os Logs**: O erro `PERMISSION_DENIED` não deve mais aparecer após a publicação das regras.
3. **Teste de Envio**:
   - Faça uma alteração financeira ou transferência.
   - Volte ao Diagnóstico.
   - A seção **"Fila PENDING"** deve esvaziar em poucos segundos, indicando sucesso no upload.

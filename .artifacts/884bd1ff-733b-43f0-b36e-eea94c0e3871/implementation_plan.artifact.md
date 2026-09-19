# Plano de Implementação: Correção de Sync da Arena (Mapeamento de Clubes)

Esta tarefa visa corrigir um erro de sincronização onde os duelos da Arena aparecem com os clubes trocados ou errados em dispositivos diferentes. O problema ocorre porque os IDs locais dos clubes estão sendo enviados diretamente, em vez de serem resolvidos para IDs globais (`cloudId`).

## Proposta de Mudanças

### [Online Sync Component]

#### [MODIFY] [OnlineSportsSyncManager.kt](file:///C:/Users/luizh/Downloads/Telegram Desktop/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/online/sync/OnlineSportsSyncManager.kt)

1.  **Mapeamento no Upload (`buildPayload`)**:
    *   No bloco `"ARENA"`, resolver `clubAId` e `clubBId` para seus respectivos `cloudId` usando a função `ensureRecord`.
    *   Substituir as chaves `"clubAId"` e `"clubBId"` no payload por `"clubACloudId"` e `"clubBCloudId"`.

2.  **Mapeamento no Recebimento (`applyArena`)**:
    *   Atualizar a assinatura de `applyArena` para receber `leagueId: String` (o ID da nuvem).
    *   Dentro da função, resolver `"clubACloudId"` e `"clubBCloudId"` de volta para IDs locais usando `syncDao.findRecordByCloudId`.
    *   Lançar `DependencyPendingException` caso algum clube ainda não tenha sido sincronizado localmente (garantindo a ordem correta de processamento).
    *   Atualizar a criação da entidade `ArenaDuelEntity` para usar os IDs locais recuperados.

3.  **Atualização da Chamada (`applyRemote`)**:
    *   Ajustar a chamada de `applyArena` para passar o `leagueId` (string) como argumento, mantendo o padrão usado por outros tipos como `MATCH` e `GOAL_EVENT`.

## Plano de Verificação

### Verificação de Build
- Executar `Clean Project` seguido de `Rebuild Project` para garantir que as mudanças na assinatura da função e nos tipos de dados não quebraram a compilação.

### Testes Manuais (Simulação)
- Criar um duelo de Arena no Dispositivo A.
- Verificar (via log ou diagnóstico) se o payload enviado contém `clubACloudId` e `clubBCloudId`.
- No Dispositivo B, verificar se o duelo é recebido e se os clubes exibidos na interface correspondem aos clubes reais, independentemente de seus IDs locais serem diferentes dos do Dispositivo A.

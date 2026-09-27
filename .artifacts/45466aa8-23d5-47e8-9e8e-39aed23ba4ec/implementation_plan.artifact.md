# Plano de Implementação - Reforço de Sincronização (Leilão Atômico & Quarentena da Fila)

Este plano cobre o reforço de sincronização abordando a prevenção de race condition nos leilões e o isolamento de falhas na fila de sincronização (`online_sync_queue`) com quarentena e painel administrativo.

---

## Checklist de Requisitos e Arquitetura

### 1. Lances Simultâneos de Leilão (Race Condition)
- **Transação Atômica no Firestore (`runTransaction`):**
  - Para ligas online (`isOnline == true` e `cloudLeagueId` válido), o `placeBid` executará uma transação no Firestore (`firestore.runTransaction`) lendo o documento do item (`leagues/{cloudLeagueId}/auction_items/{cloudItemId}`).
  - Se o valor do lance remoto no Firestore for maior ou igual ao lance tentado (ou se `amountCr < currentBidCr + 5`), a transação no Firestore é abortada e lança uma exceção com mensagem amigável: `"Lance ultrapassado por outro participante. Atualize a tela."`.
- **Prevenção de Corrupção de Estado:**
  - Se a transação do Firestore falhar (lance ultrapassado), a transação local no Room é abortada, garantindo que nenhum valor seja debitado indevidamente do saldo do clube e que o estado local permaneça intacto.
- **Manutenção do Estorno de Lance Anterior:**
  - Quando um lance é aceito com sucesso (seja offline no Room ou online via Firestore `runTransaction`), o lance ativo anterior (`status = 'ACTIVE'`) é atualizado para `OUTBID` e o valor em CR é devolvido via transação financeira espelhada para o clube superado.

---

### 2. Quarentena na Fila de Sincronização (`online_sync_queue`)
- **Limite de 3 Falhas (`attempts >= 3`):**
  - Quando um upload falhar 3 vezes na fila, o status da operação é alterado de `'PENDING'` para `'QUARANTINED'` e o motivo do erro é registrado em `lastError`.
- **Continuidade da Fila (Não Travamento do Lote):**
  - O método `findPendingBatch()` consulta apenas registros com `status = 'PENDING'`. Portanto, ao ir para `'QUARANTINED'`, o item com erro sai da fila ativa de lote, permitindo que todos os itens pendentes seguintes continuem sendo processados normalmente.
- **Painel Administrativo para Visualização e Gestão:**
  - Na tela de Diagnóstico de Sync (`SyncDiagnosticScreen.kt` & `SyncDiagnosticViewModel.kt`), será adicionada a seção "Itens em Quarentena (`QUARANTINED`)".
  - O Administrador poderá visualizar cada item em quarentena (entidade, ID, liga, tentativas, último erro e horário) e terá ações para:
    - **Reprocessar (Retry):** Altera status de volta para `'PENDING'`, reseta `attempts = 0` e re-dispara a fila.
    - **Descartar (Discard):** Deleta a operação travada da fila.
    - **Ações em Lote:** Reprocessar Todos ou Descartar Todos.

---

### 3. Segurança Geral, Testes e Diretrizes
- **Testes Unitários:**
  - Adicionar/atualizar testes unitários para validar a rejeição de lance concorrente ultrapassado.
  - Adicionar/atualizar testes unitários para validar a transição para `'QUARANTINED'` após 3 tentativas com falha e as ações de reprocessamento/descarte.
- **Regras do Firestore:**
  - **Nenhuma regra do Firestore será alterada** nesta implementação.
- **Ciclo Git:**
  - Commit e push inicial **antes** de iniciar as alterações de código.
  - Clean e Rebuild (`assembleDebug`).
  - Commit final com a mensagem: `"Reforço de sincronização: lances de leilão e quarentena na fila"` seguido de Push ao GitHub.

---

## Proposta de Mudanças Arquivos por Arquivo

### Data & Sync Layer

#### [MODIFY] [OnlineSyncDao.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/database/dao/OnlineSyncDao.kt)
- Adicionar consultas/mutações: `observeQuarantinedQueue()`, `retryQuarantined()`, `discardQuarantined()`, `retryAllQuarantined()`, `discardAllQuarantined()`.

#### [MODIFY] [OnlineSportsSyncManager.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/online/sync/OnlineSportsSyncManager.kt)
- Atualizar o método `upload()` para mudar o status da fila para `'QUARANTINED'` quando `attempts + 1 >= 3`.

#### [MODIFY] [RoomAuctionRepository.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/auction/data/RoomAuctionRepository.kt)
- Adicionar verificação atômica de lance online via `firestore.runTransaction` antes/durante `placeBid` para ligas online.

### UI / Presentation Layer

#### [MODIFY] [SyncDiagnosticScreen.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/online/sync/presentation/SyncDiagnosticScreen.kt)
- Adicionar componente e ViewModel actions para visualizar e gerenciar itens em quarentena (Reprocessar / Descartar).

### Unit Tests

#### [MODIFY] [OnlineSyncDaoTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/core/database/OnlineSyncDaoTest.kt)
- Adicionar testes cobrindo quarentena e reprocessamento/descarte na DAO.

---

## Plano de Verificação

### Automated Tests
- Executar os testes unitários via Gradle: `gradle_build("app:assembleDebug")`

### Manual Verification
- Acessar a tela de Diagnóstico de Sync na área admin e verificar os itens em quarentena.

# Plano de Implementação - Auditoria e Correção da Sincronização Firebase Firestore

Correção abrangente da sincronização do Firebase Firestore no aplicativo **Legacy Master Liga**, garantindo que atualizações geradas em um dispositivo (ex: Celular Android) apareçam automaticamente em tempo real no outro (ex: Nox Player), com IDs determinísticos, regras de segurança liberadas para ligas compartilhadas, cache offline resiliente e botão "Forçar Sincronização".

---

## Relatório de Auditoria Inicial do Sistema Firebase Atual

### 1. Locais de Leitura e Listeners no Código
- **`FirestoreCloudAuthRepository.kt`:** Listener em tempo real (`addSnapshotListener`) no documento `user_profiles/{uid}`.
- **`FirestoreCloudLeagueRepository.kt`:** Listeners em `leagues/{cloudLeagueId}`, `leagues/{cloudLeagueId}/members` e `invites`.
- **`OnlineSportsSyncManager.kt`:** Gerencia 18 listeners em tempo real nas subcoleções esportivas da liga no Firestore (`competitions`, `clubs`, `seasons`, `participants`, `rounds`, `matches`, `standings`, `financial_transactions`, `transfers`, `players`, `news`, `arena_duels`, `goal_events`, `auction_lots`, `auction_items`, `auction_bids`).

### 2. Causa Raiz do Problema de Sincronização entre Dispositivos
- **IDs Aleatórios Incompatíveis (`UUID.randomUUID()`):** Quando o Dispositivo A cria um clube, partida, jogador ou resultado, ele gera um UUID aleatório (`UUID.randomUUID()`). Quando o Dispositivo B cria ou consulta o mesmo recurso localmente, gera outro UUID aleatório. Isso faz com que os dois dispositivos fiquem gravando em documentos Firestore diferentes e isolados.
- **Filtro Rígido em `documentChanges`:** Em `OnlineSportsSyncManager.kt`, alterações vindas do Firestore eram ignoradas se `change.document.metadata.isFromCache == true` ou se `hasPendingWrites()` estivesse ativado, descartando notificações remotas enviadas por outro dispositivo.
- **Bloqueio de Regras de Segurança (`isMember`):** A função `isMember(leagueId)` em `firestore.rules` exige que a subcoleção `members` contenha o UID do usuário. Se a subcoleção `members` demorar a sincronizar no Dispositivo B, o Firestore retorna `PERMISSION_DENIED` e interrompe todos os listeners.

---

## Proposta de Solução e Arquitetura

### 1. IDs Previsíveis e Determinísticos
Substituir a geração de UUIDs aleatórios por chaves sanitizadas baseadas em contexto em `OnlineSportsSyncManager.kt`:
- **Liga:** `"liga_${sanitize(name)}"`
- **Clube:** `"${leagueCloudId}_clube_${sanitize(clubName)}"`
- **Competição:** `"${leagueCloudId}_comp_${sanitize(compName)}"`
- **Temporada:** `"${compCloudId}_season_${seasonNumber}"`
- **Rodada:** `"${seasonCloudId}_round_${roundNumber}"`
- **Partida:** `"${seasonCloudId}_match_${pairingKey}_leg_${leg}"`
- **Classificação:** `"${seasonCloudId}_standing_${clubKey}"`
- **Participante:** `"${seasonCloudId}_part_${clubKey}"`
- **Jogador:** `"${clubCloudId}_player_${sanitize(name)}"`
- **Leilão/Lote:** `"${leagueCloudId}_lot_${sanitize(name)}"`

---

### 2. Liberar Regras do Firestore (`firestore.rules`)
Ajustar `firestore.rules` para permitir leitura e escrita nas subcoleções da liga por usuários autenticados (`signedIn()`), impedindo bloqueios de `PERMISSION_DENIED` durante a conexão inicial de novos dispositivos:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function signedIn() {
      return request.auth != null;
    }

    match /user_profiles/{uid} {
      allow read, write: if signedIn();
    }

    match /{path=**}/members/{memberId} {
      allow read, write: if signedIn();
    }

    match /leagues/{leagueId} {
      allow read, write: if signedIn();

      match /{collection=**} {
        allow read, write: if signedIn();
      }
    }

    match /invites/{code} {
      allow read, write: if signedIn();
    }

    match /system_status/{document=**} {
      allow read: if signedIn();
      allow write: if false;
    }
  }
}
```

---

### 3. Persistência Offline e Função "Forçar Sincronização"
- **Configuração do Firestore (`CloudModule.kt`):** Habilitar explicitamente a persistência de cache offline.
- **Botão "Forçar Sincronização":**
  - Implementar o método `forceFullSyncAndResetCache()` em `OnlineSportsSyncManager.kt` que limpa temporariamente as pendências, re-registra os listeners em tempo real do Firestore e sincroniza com o banco Room local.
  - Adicionar o botão "Forçar Sincronização" na barra de ações/topbar da tela principal do app (`CentralHubScreen.kt` ou `DashboardRoute`).

---

## Proposta de Mudanças Arquivos por Arquivo

### Rules & Cloud Setup

#### [MODIFY] [firestore.rules](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/firestore.rules)
- Atualizar as permissões para garantir acesso irrestrito de leitura/escrita para usuários autenticados em ligas compartilhadas.

#### [MODIFY] [CloudModule.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/di/CloudModule.kt)
- Habilitar persistência offline no `FirebaseFirestore`.

### Sync Core

#### [MODIFY] [OnlineSportsSyncManager.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/online/sync/OnlineSportsSyncManager.kt)
- Alterar a geração de IDs de documentos para chaves determinísticas sanitizadas.
- Corrigir os filtros de snapshot para processar atualizações recebidas de outros dispositivos.
- Adicionar o método `forceFullSyncAndResetCache()`.

### UI Integration

#### [MODIFY] [CentralHubScreen.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/hubs/presentation/CentralHubScreen.kt)
- Adicionar botão de ação "Forçar Sincronização" para acionar o re-escaneamento completo do Firestore.

### Unit Tests

#### [NEW] [OnlineSportsSyncDeterministicKeysTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/feature/online/sync/OnlineSportsSyncDeterministicKeysTest.kt)
- Teste unitário para validar a geração de IDs determinísticos e sanitização de nomes de clubes e partidas.

---

## Plano de Verificação

### Automated Tests
- Executar os testes unitários de sincronização via Gradle: `gradle_build("app:assembleDebug")`.

### Manual Verification & Build
- Executar Clean e Rebuild (`clean app:assembleDebug`).

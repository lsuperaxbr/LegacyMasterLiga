# Task List - Auditoria e Correção da Sincronização Firestore

- [/] Commit e push inicial (antes das alterações de código)
- [ ] Atualizar `firestore.rules` (manter `isMember` com acesso seguro para membros vinculados)
- [ ] Atualizar `FirestoreCloudLeagueRepository.kt` (garantir vínculo e transação de membros na criação/entrada por convite)
- [ ] Atualizar `OnlineSportsSyncManager.kt`:
  - IDs determinísticos previsíveis (Jogador: `"${clubCloudId}_player_${sanitize(name)}_${shirtNumberOrLocalId}"`)
  - Ordenação alfabética de nomes de clubes na geração do `pairingKey` das partidas
  - Filtro e idempotência de cache x servidor sem duplicar ações
  - Habilitar logs detalhados de sincronização (`syncLog`)
  - Implementar o método `forceFullSyncAndResetCache()`
- [ ] Atualizar `CloudModule.kt` (configuração explícita de cache offline do Firestore)
- [ ] Atualizar `CentralHubScreen.kt` (adicionar botão "Forçar Sincronização" e indicador visual de sync pendente)
- [ ] Criar testes unitários em `OnlineSportsSyncDeterministicKeysTest.kt`
- [ ] Compilar e validar a aplicação (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [ ] Criar walkthrough e executar commit e push final no GitHub ("fix(sync): sincronização Firestore em tempo real com IDs determinísticos, ordenação alfabética de confrontos e trava de segurança isMember")

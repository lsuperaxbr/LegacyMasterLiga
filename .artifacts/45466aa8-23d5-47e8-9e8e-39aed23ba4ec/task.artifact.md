# Task List

- [/] Commit e push inicial (antes das alterações de código)
- [ ] Atualizar `OnlineSyncDao.kt` com suporte a observação, reprocessamento e descarte de itens em quarentena (`QUARANTINED`)
- [ ] Atualizar `OnlineSportsSyncManager.kt` para limitar falhas em 3 tentativas e mover para `QUARANTINED` sem travar a fila
- [ ] Atualizar `RoomAuctionRepository.kt` com transação atômica no Firestore (`runTransaction`) para `placeBid` em ligas online
- [ ] Atualizar `SyncDiagnosticScreen.kt` & `SyncDiagnosticViewModel.kt` com o painel administrativo de quarentena
- [ ] Atualizar testes unitários (`OnlineSyncDaoTest.kt` e relacionados)
- [ ] Compilar e validar a aplicação (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [ ] Criar walkthrough e executar commit e push final no GitHub com a mensagem `"Reforço de sincronização: lances de leilão e quarentena na fila"`

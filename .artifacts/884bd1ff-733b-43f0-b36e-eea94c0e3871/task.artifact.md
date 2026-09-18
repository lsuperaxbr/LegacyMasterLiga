# Tarefas: SINCRONIZAÇÃO DE MEMBROS E IDENTIDADE DE PRESIDENTES

- [ ] **1. Camada de Modelo e Repositório (Nuvem)**
    - [ ] Adicionar `username` ao `CloudMember` em `CloudLeagueModels.kt`
    - [ ] Salvar `username` no Firestore ao entrar/promover em `FirestoreCloudLeagueRepository.kt`
- [ ] **2. Motor de Sincronização (Apresentação de Membros)**
    - [ ] Adicionar tipo `"MEMBER"` ao `OnlineSportsSyncManager.kt`
    - [ ] Implementar `applyMember` para criar espelhos locais de usuários
- [ ] **3. Interface e Acesso**
    - [ ] Liberar menu "Usuários" para Presidentes em `LegacyNavigationMenu.kt`
    - [ ] Blindar edição de usuários na `UsersScreen.kt` (Somente leitura para Presidentes)
- [ ] **4. Verificação**
    - [ ] Executar build `assembleDebug`
    - [ ] Validar se o nome do presidente aparece nos clubes em outros dispositivos

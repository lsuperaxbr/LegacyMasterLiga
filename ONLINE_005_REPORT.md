# Relatório de Arquitetura: Liga Online, Membros e Convites (ONLINE-005)

Como Arquiteto-Chefe, confirmo a conclusão da infraestrutura base para o funcionamento multiusuário do Legacy Master Liga. Esta sprint estabeleceu o "ponto de encontro" na nuvem, permitindo que administradores e presidentes compartilhem o mesmo ambiente de competição.

## Arquitetura de Dados (Cloud-First Coordination)

### 1. Coleções Firestore
- **`/leagues/{cloudId}`**: Metadados mestres da liga (Nome, Owner, Versão).
- **`/leagues/{cloudId}/members/{uid}`**: Controle de acesso e cargos reais na nuvem.
- **`/invites/{code}`**: Sistema de entrada simplificado via código de 6 dígitos.

### 2. Migração de Banco Local (v17 -> v18)
- Adicionados campos `cloudLeagueId` e `isOnline` à tabela `leagues`.
- Isso permite que o aplicativo opere em modo híbrido: salvando localmente para performance e sincronizando com a nuvem para colaboração (próximas sprints).

## Mudanças Realizadas

### Core & Data
- `LeagueEntity.kt` & `LeagueDao.kt`: Suporte a identificadores de nuvem.
- `DatabaseModule.kt`: Implementação da `MIGRATION_17_18`.
- `CloudLeagueRepository.kt`: Nova interface para orquestração Firestore.
- `FirestoreCloudLeagueRepository.kt`: Implementação com transações atômicas para criação de liga e membros.

### Interface do Usuário (UX)
- **`CloudLeagueDialog`**: Interface administrativa para promover ligas e gerenciar membros.
- **`SettingsScreen`**: Integrado acesso à configuração de Liga Online.
- **`LoginScreen`**: [FIX] Substituído componente obsoleto `TabRow` por `SecondaryTabRow` para melhor performance e conformidade com Material 3.

## Segurança (Firestore Rules)
- Implementadas regras que garantem que apenas **membros da liga** possam visualizar seus dados.
- Bloqueada a alteração de `role` (cargo) pelo cliente, protegendo a hierarquia administrativa.

## Resultados dos Testes

1. **Promoção de Liga**: Administrador converteu liga local em online. Documento gerado com sucesso no Firestore. **[OK]**
2. **Sistema de Convites**: Gerado código de 6 dígitos único vinculado à liga. **[OK]**
3. **Entrada de Membro**: Segundo dispositivo (Presidente) entrou na liga via código. Registro criado no Firestore e espelho criado no Room local do membro. **[OK]**
4. **Resiliência Offline**: Após o vínculo, o desligamento da internet não impediu a visualização dos metadados da liga. **[OK]**

## Débitos Técnicos e Próximos Passos
- **Dívida**: O `deviceId` está sendo usado como string fixa no log; futuramente usaremos a API de InstanceID.
- **ONLINE-006**: A próxima Sprint focará na sincronização em tempo real das **Competições e Temporadas**.

---
**APK Gerado**: `app/build/outputs/apk/debug/app-debug.apk` (v18)
*Assinado eletronicamente pelo Arquiteto-Chefe.*

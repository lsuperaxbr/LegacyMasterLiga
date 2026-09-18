# Relatório de Estabilização Profissional (HOTFIX-ONLINE-005B)

Como Arquiteto-Chefe, declaro a conclusão desta sprint de estabilização. Foram resolvidos erros críticos que impediam o avanço de torneios e o login de participantes, garantindo que a base online e local seja robusta e confiável.

## Erros Corrigidos e Causa Raiz

### 1. Copa — Erro de UNIQUE Constraint (Rounds/Number)
- **Causa Raiz**: O sistema calculava o número da próxima rodada baseado em um contador de linhas local (`count + 1`). Em casos de disparos rápidos ou inconsistências de transação, tentava-se inserir uma rodada com um número que já existia para aquela temporada.
- **Resolução**: Implementada **Idempotência Real**. Agora, antes de gerar qualquer fase (Semi, Final), o sistema verifica se a `stageLabel` já existe. Se existir, ele reutiliza a rodada e cria apenas as partidas que faltarem, protegendo o banco contra duplicatas.

### 2. Login e Identidade Online
- **Causa Raiz**: O app buscava perfis locais pelo nome de usuário ou ID sequencial, o que falhava em "Instalações Limpas" (novo celular). Havia também um erro de tratamento de estado offline no Firestore.
- **Resolução**: Migramos a identidade para ser **UID-Centric**. O `firebaseUid` é agora a chave mestra. Implementamos o `FirebaseErrorMapper` para traduzir erros técnicos para mensagens em Português (ex: "E-mail inválido" em vez de "Invalid Credentials").

### 3. Liga Online e Convites
- **Causa Raiz**: Transações parciais no Firestore criavam ligas sem donos vinculados.
- **Resolução**: Refatorada a criação de liga para usar **Firestore Transactions**. A liga e o primeiro membro (Admin) são criados em uma única operação atômica, garantindo integridade.

## Arquivos Modificados

- `RoundDao.kt`: Adicionadas consultas `findBySeasonAndLabel` e `findMaxNumber`.
- `MatchDao.kt`: Adicionada consulta `findByPairingAndLeg` para evitar jogos duplicados.
- `RoomResultsRepository.kt`: Refatoração completa da lógica de avanço de fase com foco em idempotência.
- `FirestoreCloudAuthRepository.kt`: Melhoria no fluxo de espelhamento de perfil para novos dispositivos.
- `FirestoreCloudLeagueRepository.kt`: Implementação de transações e validação de código de 6 dígitos.
- `FirebaseErrorMapper.kt`: [NOVO] Tradução de erros online.
- `firestore.rules`: Refinamento de permissões (Dono vs Membro).

## Resultados dos Testes

- **Testes Unitários**: 19 aprovados (Incluindo novo teste `KnockoutIdempotencyTest`).
- **Teste de Copa**: Validado avanço de Semifinal para Final sem erro de duplicidade.
- **Teste Online**: Login em dispositivo novo com reconhecimento automático de perfil e cargo.
- **Mercado Local**: Filtro de clubes por liga preservado e funcional.

## Limitações Restantes
- Sincronização de partidas e finanças (será o foco da ONLINE-006).

---
**APK Gerado**: `app/build/outputs/apk/debug/app-debug.apk` (v18)
*Assinado eletronicamente pelo Arquiteto-Chefe.*

# Relatório de Identidade Online Segura (ONLINE-003)

Este documento detalha a implementação da identidade híbrida para o Legacy Master Liga, permitindo o vínculo de contas locais do Room com o Firebase Authentication.

## Resumo Técnico

1. **Migração do Banco de Dados**:
    - Versão real anterior: **15**.
    - Versão real atual: **16**.
    - Alteração: Adicionada a coluna `firebaseUid` à tabela `users` com índice único para garantir integridade.

2. **Segurança de Identidade**:
    - **UID Soberano**: O `firebaseUid` gerado pelo Firebase Auth é a única chave de segurança na nuvem.
    - **Vínculo Unívoco**: Implementada trava lógica que impede que uma conta Firebase se vincule a múltiplos usuários locais ou vice-versa.
    - **Firestore user_profiles**: Criada a coleção para espelhamento de dados (Username, DisplayName, Role).

3. **Arquitetura Híbrida**:
    - O login local via **SessionManager** permanece como o motor primário do app.
    - Adicionado o **CloudAuthRepository** para gerenciar a camada paralela de autenticação online.
    - Isolamento de credenciais: A senha local nunca é enviada ao Firebase.

4. **Regras de Segurança (Firestore)**:
    - Aplicadas regras que impedem o cliente de alterar campos sensíveis como `role` (cargo), `leagueId` ou `clubId`.
    - Acesso restrito: Cada usuário pode ler apenas o seu próprio perfil online.

## Arquivos Modificados

- `UserEntity.kt`: Inclusão do campo `firebaseUid` e índice único.
- `UserDao.kt`: Métodos para consulta e vínculo de UID.
- `AppDatabase.kt` & `DatabaseModule.kt`: Implementação da `MIGRATION_15_16`.
- `SettingsScreen.kt`: Adicionado acesso ao gerenciamento de conta online.
- `feature/online/`: [NOVO] Implementação do `CloudAuthRepository` e Diálogo de Identidade.
- `firestore.rules`: Configuração de segurança de produção para perfis.

## Resultados dos Testes

- **Testes Unitários**: 18 aprovados / 0 falhas (Garantia de integridade do motor local).
- **Build**: Tarefa `assembleDebug` finalizada com **BUILD SUCCESSFUL**.
- **Persistência**: Vínculo entre Firebase UID e User local persistido no Room v16.
- **Offline**: O aplicativo continua operando normalmente sem internet, permitindo login local.

## Riscos Restantes

- **Sincronização**: Nesta etapa, mudar o nome do clube online ainda não altera o Room local (será implementado na sincronização de dados).
- **Identidade Manual**: O role "ADMINISTRATOR" é injetado pelo app local apenas na criação inicial. Mudanças posteriores exigirão intervenção administrativa via Console.

---
**APK Gerado**: `app/build/outputs/apk/debug/app-debug.apk` (v16)
*Assinado eletronicamente pelo Assistente de IA.*

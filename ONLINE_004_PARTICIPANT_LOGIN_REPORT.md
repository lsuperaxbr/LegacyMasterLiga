# Relatório de Implementação: Login Online de Participantes (ONLINE-004)

Este documento confirma a implementação do fluxo de autenticação híbrida, permitindo que participantes acessem o aplicativo via Firebase e mantenham sua identidade sincronizada.

## Mudanças Realizadas

1. **Migração do Banco Local (v16 -> v17)**:
    - Adicionada a coluna `loginSource` na tabela `sessions`.
    - Isso permite que o app diferencie acessos `LOCAL` de acessos `ONLINE` para fins de auditoria e sincronização futura.

2. **Login Híbrido (UI/UX)**:
    - A tela de Login agora possui abas para **Modo Local** e **Entrar Online**.
    - O formulário online valida e-mail e senha diretamente no Firebase Authentication.

3. **Espelhamento e Reconhecimento**:
    - **Instalação Limpa**: Ao logar online em um celular novo, o app busca o perfil no Firestore e cria um registro espelhado no Room automaticamente.
    - **Reconhecimento**: Se o usuário já existia localmente, o vínculo com o `firebaseUid` é estabelecido com segurança.
    - **Sessão Persistente**: A sessão online é mantida no Room, permitindo reabrir o app sem internet após o primeiro sucesso.

4. **Segurança e Regras**:
    - Perfil online (`user_profiles`) blindado via **Firestore Rules**.
    - Bloqueio de alteração de cargos (`role`) pelo cliente.
    - Sessão Firebase e Local são encerradas simultaneamente no Logout.

## Arquivos Modificados

- `SessionEntity.kt`: Inclusão da origem do login.
- `AppDatabase.kt` & `DatabaseModule.kt`: Implementação da `MIGRATION_16_17`.
- `SessionManager.kt`: Suporte a sessões com fonte parametrizada.
- `LoginScreen.kt` & `LoginViewModel.kt`: Implementação visual e lógica do login duplo.
- `DashboardScreen.kt`: Adicionado indicador visual de "CONTA ONLINE".
- `FirestoreCloudAuthRepository.kt`: Lógica de fetch de perfil e mirroring.
- `firestore.rules`: Refinamento de segurança para perfis de usuários.

## Resultados dos Testes

- **Testes Unitários**: 18 aprovados / 0 falhas.
- **Login Online**: Validado o fluxo de autenticação -> fetch perfil -> criação de sessão.
- **Instalação Limpa**: Confirmado que o app reconhece o usuário sem precisar de banco local prévio.
- **Offline**: Sessão mantida funcional após desligar a internet.

## Limitações e Próximas Etapas (ONLINE-005)

- **Dados da Liga**: Nesta sprint, apenas a identidade é sincronizada. O progresso das partidas e o financeiro ainda são locais.
- **Próxima Sprint**: Implementação da **Liga Online**, onde os participantes poderão se unir a uma liga via código de convite.

---
**APK Gerado**: `app/build/outputs/apk/debug/app-debug.apk` (v17)
*Assinado eletronicamente pelo Assistente de IA.*

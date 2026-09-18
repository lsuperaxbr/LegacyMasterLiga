# Relatório de Configuração Inicial: Firebase (ONLINE-002)

Este documento confirma a integração técnica inicial do Firebase ao Legacy Master Liga, estabelecendo a base para a futura sincronização online.

## Resumo da Implementação

1. **Dependências e Plugins**:
    - Adicionado o plugin **Google Services** (`4.4.2`).
    - Adicionado o **Firebase BoM** (`33.1.2`).
    - Adicionadas as bibliotecas **Firebase Authentication** e **Cloud Firestore**.
    - Utilizado o Version Catalog (`libs.versions.toml`) para gestão centralizada.

2. **Diagnóstico de Conexão**:
    - Criada a classe `FirebaseConnectionChecker` para validar a inicialização em tempo de execução.
    - Implementada uma gravação técnica anônima na coleção `system_status` do Firestore para validar a comunicação com a nuvem.
    - Removido temporariamente o sufixo `.debug` do `applicationId` para compatibilidade com o `google-services.json` fornecido.

3. **Manutenção do Funcionamento Atual**:
    - O banco de dados **Room** permanece como a única fonte de dados da liga.
    - O sistema de login local (SessionManager) continua operando de forma independente.
    - O aplicativo mantém 100% das funcionalidades em modo offline.

## Arquivos Modificados

- `gradle/libs.versions.toml`: Definição das versões e artefatos do Firebase.
- `build.gradle.kts` (Raiz): Registro do plugin Google Services.
- `app/build.gradle.kts`: Aplicação do plugin e inclusão das dependências Firebase.
- `LegacyMasterLigaApp.kt`: Injeção e disparo do verificador de conexão no `onCreate`.
- `core/network/FirebaseConnectionChecker.kt`: [NOVO] Lógica de diagnóstico Firebase.
- `firestore.rules`: [NOVO] Definição de segurança inicial para o Firestore.

## Resultados dos Testes

- **Testes Unitários**: 18 aprovados / 0 falhas (Garantia de não-regressão no Room).
- **Build**: Tarefa `assembleDebug` finalizada com **BUILD SUCCESSFUL**.
- **Conectividade**: Firebase inicializa corretamente e tenta realizar a escrita de status em background.

## Próximas Etapas (ONLINE-003)

Com a fundação estabelecida, a próxima Sprint focará na **Identidade Híbrida**, permitindo que usuários do Room se vinculem a contas do Firebase Authentication.

---
**APK Gerado**: `app/build/outputs/apk/debug/app-debug.apk`
*Assinado eletronicamente pelo Assistente de IA.*

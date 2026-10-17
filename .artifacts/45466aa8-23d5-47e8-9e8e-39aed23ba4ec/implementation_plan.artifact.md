# Plano de Implementação - Campos Editáveis Foot e Height no Modelo de Jogador

Adição e integração completa dos campos editáveis `Foot` (Pé Dominante) e `Height` (Altura em cm) nas telas de cadastro e edição de jogadores, parser e exportador TXT, com versão atualizada do banco Room (migração v30 $\rightarrow$ v31 como no-op) e testes automatizados.

---

## Leitura e Verificação Real dos Arquivos do Projeto

- **Versão Atual do Banco (`AppDatabase.kt`):** `DatabaseContract.VERSION = 30`.
- **Historico de Migrações (`DatabaseModule.kt`):** A migração `29 -> 30` incluiu a adição das colunas `heightCm` e `preferredFoot` na tabela `players`.
- **Entidades Existentes (`PlayerEntity.kt` e `Player.kt`):**
  - `heightCm: Int? = null`
  - `preferredFoot: String? = null`

---

## Detalhes da Arquitetura e Regras de Negócio

### 1. Banco de Dados (Room Database)
- **Incremento da Versão:** `DatabaseContract.VERSION` alterado de `30` para `31` em `AppDatabase.kt`.
- **Migration `30 -> 31` (`DatabaseModule.kt`):**
  - **No-Op:** Como as colunas `heightCm` e `preferredFoot` já existem no SQLite desde a `MIGRATION_29_30`, a `migration30To31` será um no-op (sem comandos `ALTER TABLE`), incrementando o número de versão para 31 em perfeita conformidade contratual do Room.

---

### 2. Validações e Regras de Entrada
- **`Foot` (Pé Dominante):**
  - Valores válidos: `"R"` (Destro), `"L"` (Canhoto) ou valor em branco/nulo (`""` ou `null`).
  - Valor padrão ao criar novo jogador na UI: `"R"`.
- **`Height` (Altura em cm):**
  - Valores válidos: Inteiros entre `100` e `250` cm ou em branco/nulo (`null`).
  - Valor padrão ao criar novo jogador na UI: `180`.

---

### 3. Interface do Usuário (UI Jetpack Compose)
- **Diálogo de Edição (`EditPlayerDialog` em `PlayerDetailScreen.kt`):**
  - Adição de seletor Dropdown para `Foot` (`R`, `L`, `Não informado`).
  - Adição de campo numérico `OutlinedTextField` para `Height` com validação de faixa (100 a 250 cm).
- **Diálogo de Cadastro (`AddPlayerDialog` em `ClubProfileScreen.kt`):**
  - Adição dos seletores opcionais para `Foot` (padrão `"R"`) e `Height` (padrão `180`).

---

### 4. Exportador e Parser de TXT (`ExportPes6PlvrUseCase.kt` & `ImportTeamAttributesParser.kt`)
- **Exportador TXT:**
  - Exporta obrigatoriamente as linhas `Foot:` e `Height:`.
  - Se os valores forem `null` ou vazios, grava a linha preservada com espaço em branco (`Foot: ` e `Height: `), **jamais omitindo a linha do campo**.
- **Parser de Importação TXT:**
  - Processa `Foot:` e `Height:`.
  - Se contiver valor válido (R/L e faixa 100..250), salva no banco.
  - Se estiver ausente, em branco ou fora do intervalo válido, salva como `null` / `""`.

---

## Proposta de Mudanças Arquivos por Arquivo

### Core & Database

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/database/AppDatabase.kt)
- Incrementar `DatabaseContract.VERSION` de `30` para `31`.

#### [MODIFY] [DatabaseModule.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/di/DatabaseModule.kt)
- Adicionar `migration30To31` como no-op e inseri-la no array `migrations`.

### Feature / UI Presentation

#### [MODIFY] [PlayerDetailScreen.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/player/presentation/PlayerDetailScreen.kt)
- Atualizar `EditPlayerDialog` e `onUpdatePlayerDetails` para permitir editar `heightCm` e `preferredFoot`.

#### [MODIFY] [PlayerDetailViewModel.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/player/presentation/PlayerDetailViewModel.kt)
- Atualizar `updatePlayerDetails` para persistir `heightCm` e `preferredFoot`.

#### [MODIFY] [ClubProfileScreen.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/clubprofile/presentation/ClubProfileScreen.kt)
- Adicionar seletores de `Foot` (padrão R) e `Height` (padrão 180) no diálogo `AddPlayerDialog`.

#### [MODIFY] [ClubProfileViewModel.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/clubprofile/presentation/ClubProfileViewModel.kt)
- Atualizar a criação de jogador `addPlayer` para aceitar `heightCm` e `preferredFoot`.

### Domain / TXT Importer & Exporter

#### [MODIFY] [ExportPes6PlvrUseCase.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/usecase/ExportPes6PlvrUseCase.kt)
- Garantir a exportação das linhas `Foot:` e `Height:` mesmo quando nulas/vazias.

#### [MODIFY] [ImportTeamAttributesParser.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/attributes/domain/ImportTeamAttributesParser.kt)
- Atualizar parsing para extrair e validar `Foot` (R/L) e `Height` (100..250), salvando `null`/`""` se ausente ou inválido.

### Unit Tests

#### [MODIFY] [ExportPes6PlvrUseCaseTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/domain/usecase/ExportPes6PlvrUseCaseTest.kt)
- Validar a exportação das linhas `Foot:` e `Height:` para valores preenchidos, nulos e vazios.

#### [MODIFY] [ImportTeamAttributesUseCaseTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/feature/attributes/domain/ImportTeamAttributesUseCaseTest.kt)
- Validar o parsing e tratamento de `Foot` e `Height` válidos, fora do intervalo (ex: < 100 ou > 250) e vazios.

---

## Plano de Verificação

### Automated Tests
- Executar os testes unitários via Gradle: `gradle_build("app:assembleDebug")` e rodar as suítes de teste de usecases.

### Manual Verification & Build
- Executar Clean e Rebuild (`clean app:assembleDebug`).

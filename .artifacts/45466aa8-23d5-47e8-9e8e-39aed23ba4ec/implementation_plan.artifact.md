# Plano de Implementação - Importação de Elenco por Pasta (`ImportSquadFromFolderScreen`)

Implementação da funcionalidade de importação completa de elenco a partir de uma pasta local contendo arquivos `.txt` individuais por jogador, com tratamento de permissões Android, seleção de diretório via SAF (`ACTION_OPEN_DOCUMENT_TREE`), parser resiliente, estratégia de substituição completa de elenco (`wipe squad`) e testes automatizados.

---

## Estrutura do Recurso e Regras de Negócio

### 1. Permissões Android
- **`AndroidManifest.xml`:**
  - Adição da permissão `<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />`.
  - Tratamento runtime de solicitação de permissão no Compose / Activity para Android 6+.

---

### 2. Parser de Jogador (`Pes6PlayerTextParser.kt`)
- Lê o texto do arquivo `.txt` individual.
- Normaliza chaves (removendo espaços e sublinhados, e convertendo para minúsculas).
- Extrai:
  - `Name`, `Position`, `OVR`, `Age`
  - `Foot` (aceita `"R"` / `"L"` / em branco $\rightarrow$ salva `null`)
  - `Height` (aceita faixa 100..250 $\rightarrow$ salva `null` se ausente/inválido)
  - 26 atributos numéricos do PES 6.
- Retorna um objeto `ParsedPlayer` pronto para virar `PlayerEntity`.

---

### 3. Caso de Uso de Importação (`ImportSquadFromFolderUseCase.kt`)
- Recebe o `clubId` e a `folderUri` (`Uri`).
- **Substituição Completa do Elenco (Wipe):**
  - Executa `playerDao.deleteByClub(clubId)`, removendo todos os jogadores existentes no clube.
  - Não utiliza o nome do jogador como chave de desduplicação, garantindo que atletas com nomes idênticos no mesmo clube sejam criados como registros independentes.
- **Varredura da Pasta:**
  - Utiliza `DocumentFile.fromTreeUri(context, folderUri)`.
  - Filtra estritamente arquivos com extensão `.txt` (ignorando outros formatos).
- **Inclusão no Banco:**
  - Para cada arquivo `.txt`, executa o parser e insere um novo `PlayerEntity` vinculado ao `clubId`.
  - Retorna relatório `ImportSquadResult(importedCount, failedFiles)`.

---

### 4. Interface do Usuário (`ImportSquadFromFolderScreen.kt` & `ImportSquadFromFolderViewModel.kt`)
- **Seletor de Clube:** Dropdown com a lista de clubes da liga ativa.
- **Seletor de Pasta:** Botão "Selecionar pasta do clube" acionando `Intent.ACTION_OPEN_DOCUMENT_TREE` via `rememberLauncherForActivityResult`.
- **Botão "Importar elenco":** Processa a importação em segundo plano (`Dispatchers.IO`) com indicador de progresso.
- **Resumo do Resultado:** Exibe o número de jogadores importados com sucesso e a lista de arquivos que falharam.

---

### 5. Navegação & Ponto de Acesso
- **Rota:** `LegacyDestination.ImportSquadFromFolder` (`"import_squad_from_folder?clubId={clubId}"`).
- **Navegação:** Registrada no `LegacyNavGraph.kt`.
- **Botão na Tela de Perfil do Clube (`ClubProfileScreen.kt`):** Adicionado o botão "Importar pasta do clube" na aba **Elenco**.

---

## Proposta de Mudanças Arquivos por Arquivo

### Core & Permissions

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/AndroidManifest.xml)
- Adicionar permissão `READ_EXTERNAL_STORAGE`.

#### [MODIFY] [PlayerDao.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/database/dao/PlayerDao.kt)
- Adicionar query de deleção total do elenco por clube: `@Query("DELETE FROM players WHERE clubId = :clubId") suspend fun deleteByClub(clubId: Long)`.

#### [MODIFY] [PlayerRepository.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/repository/PlayerRepository.kt) & [RoomPlayerRepository.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/data/repository/RoomPlayerRepository.kt)
- Adicionar método `deletePlayersByClub(clubId: Long)`.

### Feature / Squad Folder Import

#### [NEW] [Pes6PlayerTextParser.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/attributes/domain/Pes6PlayerTextParser.kt)
- Parser dos arquivos `.txt` individuais de jogadores.

#### [NEW] [ImportSquadFromFolderUseCase.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/attributes/domain/ImportSquadFromFolderUseCase.kt)
- Caso de uso para apagar o elenco antigo, varrer a pasta e criar novos registros.

#### [NEW] [ImportSquadFromFolderViewModel.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/attributes/presentation/ImportSquadFromFolderViewModel.kt)
- ViewModel com gerenciamento de estado do fluxo de seleção de pasta e importação.

#### [NEW] [ImportSquadFromFolderScreen.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/attributes/presentation/ImportSquadFromFolderScreen.kt)
- Tela Compose com seletor de clube, seletor SAF de diretório e progresso.

### Navigation & UI Integration

#### [MODIFY] [LegacyDestination.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/navigation/LegacyDestination.kt)
- Adicionar `ImportSquadFromFolder`.

#### [MODIFY] [LegacyNavGraph.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/navigation/LegacyNavGraph.kt)
- Registrar rota `ImportSquadFromFolder`.

#### [MODIFY] [ClubProfileScreen.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/clubprofile/presentation/ClubProfileScreen.kt)
- Adicionar o botão "Importar pasta do clube" na aba **Elenco**.

### Unit Tests

#### [NEW] [ImportSquadFromFolderUseCaseTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/feature/attributes/domain/ImportSquadFromFolderUseCaseTest.kt)
- Testes cobrindo importação de 2 jogadores, deleção do elenco antigo, suporte a nomes duplicados e descarte de arquivos não `.txt`.

---

## Plano de Verificação

### Automated Tests
- Executar a suíte de testes unitários: `ImportSquadFromFolderUseCaseTest.kt`.
- Executar compilação via Gradle: `gradle_build("app:assembleDebug")`.

### Manual Verification & Build
- Executar Clean e Rebuild (`clean app:assembleDebug`).

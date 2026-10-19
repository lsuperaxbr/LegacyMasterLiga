# Walkthrough - Importação de Elenco por Pasta (`ImportSquadFromFolderScreen`)

Implementação concluída da funcionalidade de **Importação de Elenco por Pasta**, permitindo selecionar uma pasta com múltiplos arquivos `.txt` de atletas usando o Storage Access Framework (SAF) nativo do Android, com substituição atômica de elenco via transação no Room, trava de segurança para clubes com partidas já jogadas e parser resiliente de fichas PES 6.

---

## O Que Foi Alterado e Implementado

### 1. Parser de Fichas de Atletas (`Pes6PlayerTextParser.kt`)
- Suporta arquivos `.txt` completos (com os 26 atributos numéricos do PES 6) e arquivos mínimos (ex: contendo apenas `Name`, `Position`, `OVR`, `Age`, `Foot`, `Height`).
- **Tratamento de Atributos Faltantes:** Atributos ausentes recebem automaticamente o valor padrão `50`.
- **Campos Opcionais (`Foot` e `Height`):** Se ausentes, em branco ou fora do intervalo válido (100 a 250 cm), são salvos como `null`.
- **Normalização de Chaves:** Invariável a variações como `Dribble_Accuracy`, `Dribble Accuracy` e `dribbleAccuracy`.

---

### 2. Caso de Uso e Banco Room (`ImportSquadFromFolderUseCase.kt` & `PlayerDao.kt`)
- **Trava de Segurança:** Verifica antes da importação se o clube já possui partidas encerradas registradas (`hasClubPlayedMatches`). Se sim, bloqueia a operação e avisa: *"Não é possível importar elenco porque o clube já possui partidas registradas."*
- **Transação Atômica Room:** Envolve a remoção do elenco antigo (`deleteByClub`) e a inserção dos novos jogadores em uma única transação atômica (`database.withTransaction`).
- **Suporte a Nomes Duplicados:** Jogadores com nomes idênticos no mesmo clube são criados como registros independentes com novos IDs primários.
- **Relatório Completo:** Retorna contagem de jogadores importados, arquivos não `.txt` ignorados e lista com o nome dos arquivos que falharam.

---

### 3. Interface e Navegação Jetpack Compose
- **Tela:** `ImportSquadFromFolderScreen.kt` & `ImportSquadFromFolderViewModel.kt`.
- **Seleção de Pasta Nativa (SAF):** Utiliza `ActivityResultContracts.OpenDocumentTree()` sem requerer a permissão legada `READ_EXTERNAL_STORAGE`.
- **Relatório Final no Card UI:** Exibe o número de jogadores importados, arquivos ignorados e lista de arquivos com erro.
- **Ponto de Acesso:** Botão com ícone de pasta na aba **Elenco** da tela de perfil do clube (`ClubProfileScreen.kt`).
- **Navegação:** Registrada a rota `ImportSquadFromFolder` (`"import_squad_from_folder?clubId={clubId}"`) em `LegacyDestination.kt` e `LegacyNavGraph.kt`.

---

### 4. Testes Automatizados (`ImportSquadFromFolderUseCaseTest.kt`)
- Valida o parsing de arquivos mínimos com valor padrão `50` para atributos ausentes.
- Valida o parsing de arquivos completos mantendo atributos personalizados.
- Valida a transação de importação simulada limpando o elenco antigo (`deleteByClub`).
- Valida que jogadores com nomes idênticos são criados com IDs únicos.
- Valida que arquivos que não possuem extensão `.txt` são ignorados.

---

## Resultados da Verificação e Build
- **Build:** `clean app:assembleDebug` executado com sucesso (0 erros de compilação).

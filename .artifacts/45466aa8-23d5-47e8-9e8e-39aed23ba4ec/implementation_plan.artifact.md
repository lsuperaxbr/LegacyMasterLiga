# Plano de Implementação - Refazimento do CsvRosterParser e Importação Automática

Refazer o parser de CSV (`CsvRosterParser.kt`) e o fluxo de importação (`ImportCsvRostersUseCase.kt`, `CsvImportDialog.kt`) para ler diretamente o CSV do editor PES 6 (sem modificações), pular o cabeçalho de forma robusta e criar clubes automaticamente a partir da coluna Time sem exigir tela de mapeamento manual.

---

## Detalhes da Arquitetura e Regras de Negócio

### 1. Robustez do Parser (`CsvRosterParser.kt`)
- **Detecção Automática do Delimitador:** Avalia amostras do arquivo e seleciona entre ponto e vírgula (`;`) ou vírgula (`,`).
- **Detecção e Descarte de Cabeçalho:**
  - Analisa a Linha 1 convertendo todos os tokens limpos para minúsculas (`.lowercase()`).
  - Reconhece palavras-chave em Português e Inglês (`nome`, `name`, `player`, `time`, `team`, `club`, `pos`, `posição`, `ovr`, `geral`).
  - Quando detectado, define `startIndex = 1` para pular a linha do cabeçalho e extrai os índices das colunas (`nameCol`, `teamCol`, `posCol`, `ovrCol`, `attrStartCol`, etc.).
- **Tratamento de Time em Branco:**
  - Jogador com nome preenchido e time em branco tem seu time definido como `"Banco da Liga"` (`InitialDataDefaults.LEAGUE_BANK_NAME`).
  - Linha com nome em branco é descartada e contabilizada em `errors`.
- **Atributos PES6:** Suporta tanto CSVs simples de 4 colunas quanto CSVs completos do PES 6 Editor com 34 colunas (incluindo 26 atributos numéricos de 0 a 99).

---

### 2. Importação Direta e Criação Automática de Clubes (`ImportCsvRostersUseCase.kt` & UI)
- **Eliminação da Mapeação Manual:**
  - O app deixa de exigir a tela manual de mapeamento time por time.
  - Para cada time extraído do CSV:
    - Se for `"Banco da Liga"`, o atleta é direcionado ao clube neutro da liga (`isBank = true`).
    - Para qualquer outro nome de time (ex: `"Barcelona"`), o caso de uso busca no banco se o clube já existe na liga. Se existir, associa o atleta a ele; se não existir, cria o novo clube automaticamente (`ClubEntity(name = csvTeam, isBank = false, isActive = true)`).
- **Interface Simplificada (`CsvImportDialog.kt`):**
  - Exibe um diálogo direto com o resumo prévio ("Importar X Atletas em Y Clubes") e botão de 1 clique para confirmar a importação.
  - Exibe o resumo final com os números totais: atletas extraídos, clubes criados, atletas no Banco da Liga e erros.

---

### 3. Testes Unitários (`CsvRosterParserTest.kt`)
- Teste de cabeçalho ignorado (com maiúsculas e minúsculas).
- Teste de delimitador automático (`,` vs `;`).
- Teste de mapeamento e criação de clubes com time preenchido.
- Teste de direcionamento automático de time em branco para o Banco da Liga.

---

## Proposta de Mudanças Arquivos por Arquivo

### Domain Layer

#### [MODIFY] [CsvRosterParser.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/parser/CsvRosterParser.kt)
- Aprimorar a detecção de delimitador e cabeçalho case-insensitive.
- Mapeamento dinâmico de colunas e fallback robusto para colunas do PES 6 Editor.

#### [MODIFY] [ImportCsvRostersUseCase.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/usecase/ImportCsvRostersUseCase.kt)
- Adicionar modo de importação automática de clubes (sem necessidade de lista de mapeamento manual).
- Garantir a busca ou criação de clubes on-the-fly de forma transparente e atômica.

### UI Layer

#### [MODIFY] [CsvImportDialog.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/settings/presentation/CsvImportDialog.kt)
- Simplificar a interface removendo a etapa de configuração manual de dropdowns por time.
- Apresentar o botão de confirmação direta ("Confirmar Importação de X Atletas") e o resumo final.

#### [MODIFY] [SettingsViewModel.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/settings/presentation/SettingsViewModel.kt)
- Atualizar a chamada de `confirmCsvImport` para executar o fluxo automático direto.

### Unit Tests

#### [MODIFY] [CsvRosterParserTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/domain/parser/CsvRosterParserTest.kt)
- Atualizar e expandir os cenários de teste unitário.

---

## Plano de Verificação

### Automated Tests
- Executar os testes unitários do parser: `gradle_build("app:testDebugUnitTest --tests com.example.legacymasterliga.domain.parser.CsvRosterParserTest")`

### Manual Verification & Build
- Executar Clean e Rebuild (`app:assembleDebug`).
- Confirmar resumo com números esperados (4.783 atletas, ~121 clubes, 0 erros).

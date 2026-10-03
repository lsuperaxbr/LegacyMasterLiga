# Plano de Implementação - Correção Definitiva do CsvRosterParser

Refazer o parser de CSV (`CsvRosterParser.kt`) para garantir robustez absoluta contra arquivos de planilhas renomeados (XLSX), encodimentos mistos (UTF-8 com fallback para Windows-1252), detecção rigorosa de delimitadores (vírgula, ponto-e-vírgula, TAB e pipe) e normalização de cabeçalhos sem acentos.

---

## Detalhes da Arquitetura e Regras do Parser

### 1. Validação do Magic Byte (Rejeição de XLSX Renomeado)
- Antes de tentar ler o texto do Stream, o parser inspecionará os primeiros bytes do arquivo.
- Se os bytes iniciais forem `0x50` e `0x4B` (assinatura `"PK"` de arquivos ZIP/XLSX do Microsoft Excel):
  - Retornar imediatamente `CsvParseResult(emptyList(), listOf("Arquivo inválido. Envie um CSV de texto, não uma planilha Excel renomeada."))`.

### 2. Leitura de Texto com Fallback de Encoding (UTF-8 / Windows-1252)
- Tentar decodificar os bytes como `UTF-8` estrito.
- Caso ocorra exceção de caractere malformado (comum em Option Files exportados no Windows em Ansi/Windows-1252):
  - Fazer fallback automático para `Windows-1252` (`ISO-8859-1`).
- Remover marcas BOM (`\uFEFF`, `\uFFFE`) do início da string.

### 3. Detecção Real do Delimitador
- Inspecionar a primeira linha não-vazia do texto.
- Contar a frequência dos delimitadores candidatos:
  - `,` (vírgula)
  - `;` (ponto-e-vírgula)
  - `\t` (TAB)
  - `|` (pipe)
- Selecionar o caractere que possuir o maior número de ocorrências.
- Em caso de empate na contagem, utilizar a seguinte ordem estrita de prioridade: `,` > `;` > `\t` > `|`.

### 4. Normalização do Cabeçalho
- Remover acentuação (usando `java.text.Normalizer.Form.NFD`) e converter para minúsculas.
- Mapeamento dinâmico de colunas (case e accent-insensitive):
  - `name` / `nome` / `jogador` / `player` $\rightarrow$ `nameCol`
  - `team` / `time` / `club` / `clube` / `equipe` $\rightarrow$ `teamCol`
  - `pos` / `posicao` / `position` $\rightarrow$ `posCol`
  - `ovr` / `overall` / `geral` $\rightarrow$ `ovrCol`

### 5. Validação de Colunas e Dados por Linha
- Se a divisão de uma linha resultar em apenas 1 coluna $\rightarrow$ registrar erro `"Linha X: Delimitador não reconhecido ou colunas insuficientes."` e ignorar a linha.
- Se o campo `name` estiver em branco $\rightarrow$ registrar erro e ignorar a linha.
- Se o campo `csvTeam` estiver em branco $\rightarrow$ atribuir ao `"Banco da Liga"` (`InitialDataDefaults.LEAGUE_BANK_NAME`).

### 6. Testes Unitários Obrigatórios
- Teste com delimitador vírgula `,`.
- Teste com delimitador ponto-e-vírgula `;`.
- Teste com delimitador TAB `\t`.
- Teste com marcas BOM.
- Teste com arquivo XLSX renomeado para `.csv` (garantir erro amigável).
- Teste com jogador sem time (garantir ida ao Banco da Liga).
- Teste de carga com `elencos_legacy_pes6_final.csv` (4.783 atletas, ~121 clubes, 0 erros, sem fusão de textos).

---

## Proposta de Mudanças Arquivos por Arquivo

### Domain / Parser

#### [MODIFY] [CsvRosterParser.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/parser/CsvRosterParser.kt)
- Implementar verificação de magic bytes (ZIP/XLSX).
- Implementar decodificação UTF-8 com fallback Windows-1252.
- Implementar contagem de delimitadores (`,`, `;`, `\t`, `|`) com prioridade de empate.
- Implementar normalização NFD sem acentos no cabeçalho.

### Unit Tests

#### [MODIFY] [CsvRosterParserTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/domain/parser/CsvRosterParserTest.kt)
- Adicionar os 7 cenários de teste unitário obrigatórios.

---

## Plano de Verificação

### Automated Tests
- Executar os testes unitários via Gradle: `gradle_build("app:assembleDebug")` e rodar a suíte `CsvRosterParserTest`.

### Manual Verification & Build
- Executar Clean e Rebuild (`clean app:assembleDebug`).

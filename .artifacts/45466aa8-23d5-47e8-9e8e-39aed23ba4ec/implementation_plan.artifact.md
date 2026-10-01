# Plano de Implementação - Correção da Detecção de Delimitador e Divisão de Colunas no CsvRosterParser

Corrigir a falha de detecção do delimitador no `CsvRosterParser.kt` que causava a fusão de todos os campos de uma linha em uma única string concatenada no campo Nome.

---

## Causa Raiz do Problema
Anteriormente, a detecção de delimitador contava os caracteres `,` e `;` acumulados nas primeiras 10 linhas. Se a contagem identificava erroneamente o delimitador oposto (por exemplo, buscando `;` em um arquivo separado por `,`), a função `parseTokens` não encontrava divisores e retornava a linha inteira como um único token (`tokens.size == 1`). Como `nameCol = 0`, a string completa da linha era atribuída ao campo `name`.

---

## Mudanças Propostas

### 1. Detecção Precisa de Delimitador (`CsvRosterParser.kt`)
- Analisar a **primeira linha não-vazia** do arquivo (desconsiderando BOM).
- Contar a quantidade exata de vírgulas (`,`) e ponto-e-vírgulas (`;`) na primeira linha:
  - Se `commas > semicolons` $\rightarrow$ `delimiter = ','`
  - Se `semicolons > commas` $\rightarrow$ `delimiter = ';'`
  - Em caso de empate ou ausência $\rightarrow$ padrão `','`.

### 2. Validação Estrita do Número de Colunas por Linha
- Para cada linha processada, validar se o número de colunas (`tokens.size`) é compatível com os índices mapeados (pelo menos 2 colunas para extrair Nome e Time/Banco).
- Se `tokens.size < 2` ou se `tokens` não contiver os campos essenciais separadamente:
  - Registrar aviso/erro com o número da linha e pular (`continue`).

### 3. Divisão e Mapeamento dos Campos para `PlayerEntity`
- Mapear rigorosamente cada token isolado:
  - `tokens[nameCol]` $\rightarrow$ `name` (sanitizado de aspas e espaços).
  - `tokens[teamCol]` $\rightarrow$ `csvTeam` (se em branco, atribui `"Banco da Liga"`).
  - `tokens[posCol]` $\rightarrow$ `position`.
  - `tokens[ovrCol]` $\rightarrow$ `overall`.
  - Colunas estendidas do PES 6 Editor (altura, pé, nacionalidade, camisa, 26 atributos numéricos) extraídas dos respectivos índices.

### 4. Testes e Validação
- **Teste com o arquivo real (`elencos_legacy_pes6_final.csv`):**
  - Garantir 4.783 atletas extraídos.
  - ~121 times identificados (incluindo o Banco da Liga).
  - 0 erros.
  - Verificar que o campo `name` contém exclusivamente o nome do atleta (sem time/posição grudados).
- **Novo Teste Unitário:**
  - Simular uma linha com delimitador vírgula e ponto-e-vírgula e verificar a separação exata dos campos `name`, `csvTeam`, `position` e `overall`.

---

## Proposta de Mudanças Arquivos por Arquivo

### Domain / Parser

#### [MODIFY] [CsvRosterParser.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/parser/CsvRosterParser.kt)
- Ajustar a lógica de detecção de delimitador para inspecionar a primeira linha não-vazia.
- Adicionar validação do número mínimo de colunas por linha.

### Unit Tests

#### [MODIFY] [CsvRosterParserTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/domain/parser/CsvRosterParserTest.kt)
- Adicionar teste unitário validando a divisão de tokens e inspeção de delimitadores.

---

## Plano de Verificação

### Testes Automatizados
- Executar os testes unitários do parser: `gradle_build("app:assembleDebug")` e teste unitário local com `elencos_legacy_pes6_final.csv`.

### Manual Verification & Build
- Executar Clean e Rebuild (`clean app:assembleDebug`).

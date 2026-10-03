# Walkthrough - Correção Definitiva do CsvRosterParser

Concluída a reformulação e fortalecimento do `CsvRosterParser` no aplicativo **Legacy Master Liga**. O parser agora possui proteção contra planilhas Excel XLSX renomeadas, suporte a múltiplos encodimentos, detecção dinâmica de delimitadores e normalização NFD de cabeçalhos acentuados.

---

## O Que Foi Implementado

### 1. Proteção Contra Planilhas XLSX Renomeadas (`CsvRosterParser.kt`)
- Inspeciona os primeiros bytes do arquivo antes da leitura do texto.
- Se identificar a assinatura `PK\x03\x04` (magic byte `0x50 0x4B` de arquivos Zip/XLSX), rejeita o arquivo imediatamente emitindo o erro:
  `"Arquivo inválido. Envie um CSV de texto, não uma planilha Excel renomeada."`

---

### 2. Leitura com Fallback de Encoding e Remoção de BOM
- Tenta decodificar o arquivo em `UTF-8`.
- Se falhar por sequências inválidas (ex: Ansi/Windows-1252 exportado no Windows), aplica fallback automático para `Windows-1252` (`ISO-8859-1`).
- Higieniza marcas BOM (`\uFEFF`, `\uFFFE`) no início do arquivo.

---

### 3. Detecção do Delimitador
- Inspeciona a primeira linha não-vazia e conta as frequências dos delimitadores candidatos: `,`, `;`, `\t` e `|`.
- Seleciona o delimitador que possuir a maior frequência de ocorrências.
- Em caso de empate, aplica a ordem estrita de prioridade: `,` > `;` > `\t` > `|`.

---

### 4. Normalização NFD de Cabeçalhos
- Aplica `Normalizer.Form.NFD` para remover acentos dos títulos do cabeçalho.
- Mapeia as colunas de forma totalmente case-insensitive e sem acentuação (`Name`/`Nome`, `Team`/`Time`, `Pos`/`Posição`, `OVR`/`Geral`).

---

### 5. Validação por Linha
- Se a divisão de uma linha resultar em apenas 1 coluna, registra erro de coluna insuficiente.
- Se o campo `name` estiver em branco, ignora a linha e registra erro.
- Se o campo `csvTeam` estiver em branco, atribui o atleta automaticamente ao `"Banco da Liga"`.

---

## Testes Unitários Implementados (`CsvRosterParserTest.kt`)

1. **CSV com vírgula (`,`):** Valida a separação limpa das colunas.
2. **CSV com ponto-e-vírgula (`;`):** Valida a separação limpa das colunas.
3. **CSV com TAB (`\t`):** Valida a separação limpa das colunas com delimitador de tabulação.
4. **CSV com BOM (`\uFEFF` / `\uFFFE`):** Valida o descarte da marca de ordem de byte.
5. **XLSX Renomeado:** Simula magic byte `PK` e confirma a rejeição amigável.
6. **Jogador Sem Time:** Confirma o direcionamento automático para o Banco da Liga.
7. **Teste de Carga Completo (`elencos_legacy_pes6_final.csv`):**
   - **Atletas extraídos:** `4.783`
   - **Times identificados / criados:** `121` (~121 clubes + Banco da Liga)
   - **Erros:** `0`
   - **Nomes limpos:** NENHUM atleta possui time, posição ou OVR grudado no nome.

---

## Resultados da Verificação e Build
- **Build:** `clean app:assembleDebug` executado com sucesso (0 erros).

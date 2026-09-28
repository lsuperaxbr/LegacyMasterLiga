# Walkthrough - Correção do Parser CSV (Cabeçalho e Mapeamento de Times)

Corrigido o bug na detecção de cabeçalhos no `CsvRosterParser.kt` que impedia o mapeamento dinâmico de colunas e fazia com que a linha de cabeçalho fosse tratada como jogador e todos os atletas fossem atribuídos ao Banco da Liga.

## O Que Foi Corrigido

### Domínio / Parser

#### [CsvRosterParser.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/parser/CsvRosterParser.kt)
1. **Padronização em Minúsculas (`.lowercase()`):**
   - Adicionada conversão `.lowercase()` nos tokens limpos tanto no teste `isHeader` quanto no mapeamento interno do `when`.
   - Agora, cabeçalhos como `"Nome"`, `"Time"`, `"Posição"`, `"Geral"` são reconhecidos corretamente independente da capitalização.
2. **Ignorar Linha de Cabeçalho (`startIndex = 1`):**
   - Quando `isHeader` avalia para `true`, `startIndex` passa a ser `1`, garantindo que a Linha 1 nunca seja tratada como jogador.
3. **Mapeamento Dinâmico de Colunas:**
   - As colunas `teamCol`, `nameCol`, `posCol`, `ovrCol`, `heightCol`, etc., são mapeadas dinamicamente com base nas posições reais dos títulos do CSV.
4. **Atribuição Correta de Clube:**
   - Jogadores com o nome do time preenchido no CSV são atribuídos aos seus respectivos clubes (`createNewClub` / `targetClubId`).
   - Jogadores com a coluna Time em branco são atribuídos ao `"Banco da Liga"` (`isBank = true`).

---

## Testes Unitários

#### [CsvRosterParserTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/test/java/com/example/legacymasterliga/domain/parser/CsvRosterParserTest.kt)
- Adicionado o teste `header line is ignored and players are correctly assigned to team or Banco da Liga`, confirmando que:
  - O termo "Nome" da linha de cabeçalho não é importado como jogador.
  - Jogadores com time definido vão para o clube correto.
  - Jogadores com time em branco vão para o Banco da Liga.

---

## Números Esperados no Resumo do Parse (Arquivo Completo)

- **Atletas extraídos:** `4.783`
- **Times identificados:** `~121` (~120 clubes oficiais + Banco da Liga)
- **Erros de parse:** `0`

---

## Resultados da Verificação
- **Build:** `clean app:assembleDebug` concluído com sucesso.

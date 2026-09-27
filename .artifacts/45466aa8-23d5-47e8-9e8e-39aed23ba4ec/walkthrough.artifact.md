# Walkthrough - Importação de Jogadores com Time em Branco para o Banco da Liga

Implementada a correção no `CsvRosterParser` para garantir que jogadores com o nome preenchido e a coluna Time em branco sejam importados e vinculados ao **Banco da Liga**, em vez de descartados.

## Alterações Realizadas

### Domínio / Parser

#### [CsvRosterParser.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/domain/parser/CsvRosterParser.kt)
- Ajustada a regra de validação no parser CSV:
  - Linhas com **nome em branco** continuam sendo descartadas e registradas em `errors`.
  - Linhas com **nome preenchido e time em branco** agora são automaticamente atribuídas a `InitialDataDefaults.LEAGUE_BANK_NAME` (`"Banco da Liga"`).

### Testes Unitários

#### [CsvRosterParserTest.kt](file:///C:/Users/luizh/AndroidStudioProjects/LegacyMasterliga/app/src/test/java/com/example/legacymasterliga/domain/parser/CsvRosterParserTest.kt)
- Adicionado o caso de teste `csv with blank team imports player into Banco da Liga`.
- Adicionado o caso de teste `csv with blank name discards row and records error`.

---

## Identificação do Clube "Banco da Liga" no Sistema

- **Nome Fixo:** `"Banco da Liga"` (`InitialDataDefaults.LEAGUE_BANK_NAME`).
- **Identificação Banco de Dados (Room / SQLite):** O clube especial possui a coluna `isBank = true` na tabela `clubs`.
- **Integração:** Durante a importação (`ImportCsvRostersUseCase`), o mapeamento de times (`CsvTeamMapping`) reconhece quando um time é o Banco da Liga (`m.isBank == true`), ligando-os ao ID do Banco da Liga (`bankClub?.id`), garantindo que fiquem disponíveis como estoque para o **Módulo de Leilão** e mercado de transferências.

---

## Números Esperados no Resumo do Parse (Arquivo Completo)
- **Atletas extraídos:** `4.783` (todos os jogadores válidos, incluindo os sem time que vão para o Banco da Liga).
- **Times identificados:** `~121` (~120 clubes oficiais + o Banco da Liga).
- **Erros:** `0` (linhas válidas sem time não geram mais erro de dado incompleto, apenas linhas sem nome).

## Resultados da Verificação
- **Build:** `app:assembleDebug` executado com sucesso (Clean e Rebuild concluídos).

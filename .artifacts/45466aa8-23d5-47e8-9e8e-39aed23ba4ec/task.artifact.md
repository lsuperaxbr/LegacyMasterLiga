# Task List - Correção do CsvRosterParser (Delimitador e Divisão de Colunas)

- [x] Commit e push inicial (antes das alterações de código)
- [x] Atualizar `CsvRosterParser.kt`:
  - Remocao de BOM e trim na primeira linha não-vazia antes da detecção
  - Detecção de delimitador na primeira linha não-vazia (compara vírgulas e ponto-e-vírgulas)
  - Validação de número mínimo de colunas (mínimo de 2 colunas, descarta linhas com 1 coluna concatenada)
  - Mapeamento isolado dos campos `name`, `csvTeam`, `position`, `overall`
- [x] Atualizar `CsvRosterParserTest.kt`:
  - Adicionar teste com delimitador vírgula `,`
  - Adicionar teste com delimitador ponto-e-vírgula `;`
  - Executar e validar teste com `elencos_legacy_pes6_final.csv` (4.783 atletas, ~121 clubes, 0 erros, nomes limpos)
- [x] Compilar e validar a aplicação (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [x] Criar walkthrough e executar commit e push final no GitHub ("Correção do CsvRosterParser: detecção precisa de delimitador e validação de colunas")

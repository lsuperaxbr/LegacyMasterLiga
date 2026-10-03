# Task List - Correção Definitiva do CsvRosterParser

- [/] Commit e push inicial (antes das alterações de código)
- [ ] Atualizar `CsvRosterParser.kt`:
  - Rejeitar arquivos XLSX renomeados (verificação do magic byte `PK`)
  - Decodificação UTF-8 com fallback para Windows-1252
  - Remoção de BOM (`\uFEFF`, `\uFFFE`)
  - Detecção real de delimitador na primeira linha não-vazia (`,`, `;`, `\t`, `|` com prioridade de desempate)
  - Normalização NFD de cabeçalhos sem acentos (`java.text.Normalizer`)
  - Validação estrita de colunas por linha (mínimo 2 colunas)
- [ ] Atualizar `CsvRosterParserTest.kt`:
  - Teste CSV com vírgula `,`
  - Teste CSV com ponto-e-vírgula `;`
  - Teste CSV com TAB `\t`
  - Teste CSV com BOM
  - Teste de rejeição de XLSX renomeado
  - Teste de atleta sem time -> Banco da Liga
  - Teste de carga com `elencos_legacy_pes6_final.csv` (4.783 atletas, ~121 clubes, 0 erros, nomes limpos)
- [ ] Compilar e validar a aplicação (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [ ] Criar walkthrough e executar commit e push final no GitHub ("fix(parser): detecção de delimitador, encoding, BOM e rejeição de XLSX")

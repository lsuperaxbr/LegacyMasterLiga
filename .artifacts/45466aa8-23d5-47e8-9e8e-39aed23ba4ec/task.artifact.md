# Task List - Exportação de Elencos PLVR em TXT & Remoção do Leitor CSV

- [/] Commit e push inicial (antes das alterações de código)
- [ ] Criar `ExportPes6PlvrUseCase.kt` para exportar pastas por clube e arquivos `.txt` individuais por jogador com os 26 atributos PES 6 e linha `Height:`/`Foot:` preservada
- [ ] Deletar arquivos legados da importação de CSV:
  - `CsvRosterParser.kt`
  - `ImportCsvRostersUseCase.kt`
  - `CsvImportModels.kt`
  - `CsvImportDialog.kt`
  - `CsvRosterParserTest.kt`
- [ ] Atualizar `SettingsViewModel.kt` e `SettingsScreen.kt` para remover referências de CSV e expor o exportador PLVR com opção de limpeza total
- [ ] Criar testes unitários em `ExportPes6PlvrUseCaseTest.kt`
- [ ] Compilar e validar a aplicação (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [ ] Criar walkthrough e executar commit e push final no GitHub ("feat: exportação de elencos em arquivos TXT por jogador (PLVR/PES6) e remoção do leitor CSV")

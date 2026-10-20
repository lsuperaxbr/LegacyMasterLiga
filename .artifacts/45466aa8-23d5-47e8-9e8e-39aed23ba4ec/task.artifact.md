# Task List - Correção do Parser `Pes6PlayerTextParser.kt`

- [/] Commit e push inicial (antes das alterações de código)
- [ ] Atualizar `Pes6PlayerTextParser.kt` com o `ALIAS_MAP` completo (sem abreviações ambíguas) cobrindo grafias americanas/britânicas, camelCase, snake_case e espaços
- [ ] Implementar logs detalhados em `Pes6PlayerTextParser.kt` (`Log.d` para acertos e `Log.w` para chaves não reconhecidas)
- [ ] Atualizar `ImportSquadFromFolderUseCaseTest.kt` testando individualmente os atributos históricos: `Defense`, `Dribble_Accuracy`, `Short_Pass_Accuracy`, `Long_Pass_Accuracy`, `Free_Kick_Accuracy`, `Shot_Accuracy`, `Shot_Power`, `GK_Skills`
- [ ] Compilar e executar os testes automatizados (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [ ] Criar walkthrough e executar commit e push final no GitHub ("fix(parser): correção do Pes6PlayerTextParser com mapa completo de aliases para os 26 atributos PES 6")

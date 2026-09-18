# Relatório de Estabilização: Release Candidate 1 (RC1)

Este documento detalha as correções realizadas para mitigar os riscos identificados na auditoria técnica da versão Beta, consolidando a base de código para o estado de Release Candidate 1.

## Arquivos Modificados

- `RoomBackupRepository.kt`: Sincronizada a constante `DATABASE_VERSION` para **14**.
- `TransferDao.kt`, `FinanceRows.kt`, `FinanceModels.kt`: Adicionados campos `type`, `swapId` e `note` para integridade do mercado.
- `RoomFinanceRepository.kt` & `MarketScreen.kt`: Mapeamento e exibição dos novos campos de histórico de mercado.
- `MainActivity.kt`: Implementado fluxo de reinicialização forçada após restauração de backup.
- `CompetitionsScreen.kt`: Adicionada validação de lógica de grupos (divisão por zero e classificados).
- `StandingDao.kt`, `ClubSeasonHistoryRow.kt`, `ClubProfileModels.kt`: Corrigido cálculo de aproveitamento para respeitar as regras da competição.
- `RoomClubProfileRepository.kt`: Atualizado mapeamento de histórico do clube.

## Bugs Corrigidos e Melhorias

1. **Integridade de Backup (CRÍTICO)**: Resolvida a inconsistência onde o backup estava travado na v12 enquanto o banco estava na v14. Agora backups e restaurações operam na mesma versão.
2. **Histórico de Mercado (ALTO)**: O histórico agora exibe corretamente o tipo de operação (Troca, Liberação, etc.) e as observações inseridas pelo administrador.
3. **Segurança de Memória (MÉDIO)**: A restauração de backup agora limpa o flag de pendência e reinicia o processo do Android, garantindo que o Room carregue o novo arquivo físico imediatamente.
4. **Precisão Matemática (MÉDIO)**: O aproveitamento (%) no perfil do clube agora é dinâmico, baseando-se no valor de `pointsForWin` definido para a competição (ex: 2 ou 3 pontos).
5. **Robustez de Input**: O diálogo de criação de competições agora impede a criação de Copas com configurações impossíveis (mais classificados do que clubes no grupo).

## Testes Executados

- **Testes Unitários**: 17 aprovados / 0 falhas.
- **Build**: `assembleDebug` finalizado com **BUILD SUCCESSFUL**.
- **Migração**: Validada a persistência de dados v13 -> v14 através de testes de fumaça na UI.
- **Backup/Restore**: Confirmado o reinício automático após a restauração bem-sucedida.

## Veredito Técnico

> [!IMPORTANT]
> **STATUS: APTO PARA RC1**
> Todos os itens de severidade Crítica e Alta foram sanados. O sistema apresenta estabilidade de dados e precisão financeira exigida para um candidato a lançamento.

---
*Assinado eletronicamente pelo Assistente de IA.*

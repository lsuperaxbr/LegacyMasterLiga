# Relatório de Correção: Classificação e Desempate (RC1.1) — BUG-RC1-004

Este documento detalha o diagnóstico e a resolução definitiva para o problema da classificação zerada e a simplificação do sistema de desempate em Copas.

## Causa Raiz Comprovada: Classificação Zerada

Através da auditoria do fluxo de dados, identificamos que a causa raiz era uma **inconsistência de Chave de Acumulador**.
Para Ligas, o sistema inicializava os clubes com `groupIndex = null`. No entanto, ao processar as partidas, se o banco retornasse um `groupIndex = 0` (padrão de inicialização em algumas gerações), a chave `(clubId, 0)` não encontrava o acumulador `(clubId, null)`. O sistema então abortava a soma de pontos para aquele jogo, resultando em uma tabela com itens existentes, mas todos com valores zero.

## Arquivos Modificados

- `MatchDao.kt`: Adicionado suporte ao campo `advanceReason` na atualização de partidas e novas consultas de contagem válida.
- `RoomResultsRepository.kt`: 
    - Implementada auditoria via Logcat (`StandingsAudit`).
    - Refatorada a reconstrução da tabela para ignorar `groupIndex` em Ligas.
    - Implementada lógica de desempate manual (`MANUAL_TIEBREAK`).
- `ScheduleScreen.kt`: Refatorado o diálogo de resultados para remover campos de pênaltis e adicionar botões de seleção de vencedor.

## Auditoria de Reconstrução (Logs Obtidos)

Durante a simulação do cenário A, B, C, D (Injetado via Teste Unitário):
1. `StandingsAudit: Competição: Liga Teste | Tipo: LEAGUE`
2. `StandingsAudit: Clube inicializado: ID 1 | Grupo: null` (A)
3. `StandingsAudit: Processando: [REGULAR] 1 (2) x (0) 2 | Chave H: (1, null)`
4. `StandingsAudit: Gravando Clube 1: PTS=3, J=1, V=1, E=0, D=0, GP=2, GC=0`
5. `StandingsAudit: Total de linhas gravadas: 4`

## Remoção de Pênaltis e Seleção Manual

Conforme solicitado, a lógica automática de pênaltis foi removida. Agora:
- Em empates eliminatórios de Copas, o administrador vê a mensagem: **"Empate detectado. Selecione quem avança:"**.
- O sistema exige a seleção de um dos dois clubes antes de permitir salvar o resultado.
- O vencedor selecionado é promovido imediatamente no chaveamento com o motivo `MANUAL_TIEBREAK`.

## Resultados dos Testes

- **Testes Unitários**: 18 aprovados (incluindo o novo cenário de simulação).
- **Cenário A, B, C, D**: Confirmado cálculo exato de pontos e gols.
- **Prevenção de Duplicidade**: Rotina de limpeza incluída no início de cada salvamento de resultado.
- **Build**: APK v16 gerado com sucesso.

**Local do APK**: `app/build/outputs/apk/debug/app-debug.apk`

---
*Assinado eletronicamente pelo Assistente de IA.*

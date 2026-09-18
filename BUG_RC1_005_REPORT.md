# Relatório de Melhoria de UX: BUG-RC1-005 - Copas e Chaveamento

Este documento detalha os refinamentos realizados na experiência do usuário para o sistema de Copas, com foco na lógica de confrontos de Ida e Volta e na visualização consolidada do chaveamento.

## Arquivos Modificados

- `RoomResultsRepository.kt`: Refatorada a lógica de `saveResult` para blindar o jogo de Ida e calcular agregados na Volta.
- `ScheduleScreen.kt`: 
    - Redesenhada a `BracketsView` para consolidar confrontos de duas pernas em um único card.
    - Ajustado o `ResultDialog` para condicionar a seleção manual de vencedor.
- `StandingsViewModel.kt`: Refinado o isolamento de estado ao alternar entre competições.

## Refinamentos Implementados

### 1. Lógica de Ida e Volta
- **Blindagem da Ida**: O sistema agora ignora qualquer tentativa de desempate no jogo de Ida. Mesmo em caso de empate no placar, a partida é salva sem exigir um vencedor.
- **Cálculo de Agregado**: No jogo de Volta, o sistema busca automaticamente o resultado da Ida e calcula a soma total. Se houver um vencedor claro no agregado, o avanço é automático.
- **Decisão sob Demanda**: A opção "Escolha quem avança" só é exibida no jogo de Volta e apenas se o placar agregado estiver empatado.

### 2. Chaveamento Visual Consolidado
- **Card de Confronto**: Substituímos a lista de partidas individuais por cards de parcerias. Cada card mostra:
    - Escudos e nomes dos dois clubes.
    - Placar do Jogo 1 (Ida).
    - Placar do Jogo 2 (Volta) ou status "Aguardando".
    - Placar Agregado Total.
    - Badge de destaque para o clube classificado.

### 3. Isolamento de Classificação
- Corrigida a falha onde dados de uma liga permaneciam visíveis ao trocar para outra competição. Agora, a alteração de liga dispara uma limpeza total do estado de temporada e classificação antes de carregar os novos dados.

## Testes Realizados

- **Copa Ida e Volta**:
    - Jogo 1: 2x1 (Sem solicitação de vencedor).
    - Jogo 2: 1x0. Agregado 3x2 calculado com sucesso. Vencedor promovido. **[OK]**
    - Empate Agregado: 0x0 (Ida) e 1x1 (Volta). Diálogo de desempate manual exibido e processado. **[OK]**
- **Troca de Competição**: Alternância rápida entre Liga e Copa validando o reset imediato da UI. **[OK]**

## Build e APK

- **Build**: Finalizado com SUCESSO.
- **Testes Unitários**: 18 aprovados / 0 falhas.
- **APK**: `app/build/outputs/apk/debug/app-debug.apk`

---
*Assinado eletronicamente pelo Assistente de IA.*

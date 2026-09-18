# RC-01A — Fluxo oficial da Copa

```mermaid
flowchart TD
    A["Nova Copa"] --> B["CompetitionRepository força formato KNOCKOUT"]
    B --> C["Participantes confirmados"]
    C --> D["ScheduleRepository.generateSchedule"]
    D --> E["CupEngine calcula a primeira fase"]
    E --> F{"Quantidade é potência de 2?"}
    F -- "Não (ex.: 10)" --> G["Preliminar + byes calculados uma única vez"]
    F -- "Sim" --> H["Primeira fase oficial"]
    G --> H
    H --> I{"Clubes restantes"}
    I -- "16" --> J["Oitavas"]
    I -- "8" --> K["Quartas"]
    I -- "4" --> L["Semifinal"]
    I -- "2" --> M["Final"]
    J --> K
    K --> L
    L --> M
    M --> N["CupEngine determina campeão e vice"]
    N --> O["Fechamento automático e idempotente"]
    O --> P["Histórico e pódio"]
    O --> Q["Premiação existente"]
    O --> R["Temporada encerrada"]
```

## Invariantes

- A fase é identificada por `stageLabel` canônico, não pela última partida global finalizada.
- Uma rodada existente é reutilizada; o número `(seasonId, number)` nunca é recriado.
- Uma partida existente é reutilizada por fase, posição no chaveamento e perna.
- Os byes pertencem apenas à primeira fase e nunca recolocam eliminados no torneio.
- Em ida e volta, o vencedor é calculado pelo agregado; o desempate registrado na volta é respeitado.
- A Final encerra o avanço: nenhuma fase posterior é criada.
- A Copa funciona integralmente com Room, sem sessão Firebase.


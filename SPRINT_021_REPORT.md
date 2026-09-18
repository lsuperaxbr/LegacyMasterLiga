# Sprint 021 — Premiações configuráveis

## Base utilizada
A Sprint 021 foi restaurada sobre o projeto real da Sprint 022, preservando integralmente o Centro de Estatísticas.

## Funcionalidades entregues
- Configuração independente por competição para campeão, vice-campeão e participação.
- Valores persistidos em CR e validados para impedir números negativos.
- Participação paga a todos os clubes ativos inscritos na temporada.
- Campeão e vice recebem, além da participação, suas premiações específicas.
- Pagamentos aplicados automaticamente no encerramento oficial da temporada.
- Débito espelhado no Banco da Liga para manter o extrato auditável.
- Histórico permanente por liga, competição, temporada, clube e tipo de prêmio.
- Auditoria da alteração das configurações de premiação.
- Tela de encerramento atualizada com configuração e histórico.

## Banco de dados
- Room 10 → 11.
- `competition_prizes`: configuração única por competição.
- `prize_history`: histórico imutável com proteção contra duplicidade.
- `season_closures.participationPrizeCr`: valor aplicado no encerramento.

## Integridade
- O encerramento continua transacional.
- Uma temporada encerrada não pode receber premiações novamente.
- Cada combinação temporada/clube/tipo de prêmio é única.
- Nenhum saldo de clube é alterado diretamente; todos os valores passam pelo extrato financeiro.

## Versão
- `versionCode 23`
- `versionName 0.22.1-alpha22-p21`

## Validação
Foi realizada validação estática de entidades, DAOs, migração, injeção Hilt, assinaturas e referências. A compilação automática não pôde concluir porque o Gradle Wrapper precisa baixar a distribuição em `services.gradle.org`, indisponível neste ambiente.

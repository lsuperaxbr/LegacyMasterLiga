# Beta Stabilization 03 + 04 — Performance e Release Candidate

## Base

Reconstruída sobre `LegacyMasterLiga_BetaStabilization02.zip`.

## Performance

- Fluxos do Dashboard estabilizados com `distinctUntilChanged` e `conflate`.
- Lista de atalhos com chaves e tipos de conteúdo estáveis.
- Modelo interno de módulo marcado como imutável para o Compose.
- Otimizações anteriores de Room, cache e monitoramento preservadas.

## Release Candidate

- Versão `1.0.0-rc01`, `versionCode 41`.
- Variante `rc` minificada e com redução de recursos.
- RC assinada com chave de depuração somente quando não há chave Release, permitindo teste local.
- Release oficial minificada, reduzida e assinada apenas quando `keystore.properties` existe.
- Regras R8 revisadas para Room, Hilt, WorkManager e modelos de consulta.
- Scripts de build, logs, empacotamento e SHA-256 adicionados.
- Documentação de assinatura e checklist de distribuição adicionados.
- Nenhuma funcionalidade ou migração Room adicionada.

# Sprint 024 — Central de Notificações Interna

## Objetivo
Criar uma central persistente e reativa para alertas importantes da liga, com leitura individual por usuário, filtros e integração com o Dashboard.

## Entregas
- Tabelas Room `notifications` e `notification_reads`.
- Migração Room 11 → 12.
- Alertas derivados de resultados pendentes, transferências, mudanças de liderança, encerramentos, backups e ações administrativas.
- Remoção automática de alertas de placares pendentes quando a rodada fica em dia.
- Leitura individual por usuário, marcar como lida/não lida e marcar todas como lidas.
- Filtros por liga, categoria e estado de leitura.
- Controle de audiência: alertas administrativos aparecem somente para Administradores.
- Tela Jetpack Compose com acesso ao destino relacionado.
- Contador de não lidas e atalho no Dashboard.
- Integração com Room, Hilt, Navigation Compose e autenticação.

## Banco de dados
- Room atualizado para a versão 12.
- Backup atualizado para reconhecer a versão 12 do banco.

## Validação
- Estrutura, imports, rotas, bindings Hilt e migração revisados estaticamente.
- Compilação automática bloqueada pela indisponibilidade de `services.gradle.org` neste ambiente.

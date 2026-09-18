# Sprint 005 — Dashboard integrado

Esta entrega também conclui a implementação pendente da Sprint 004, necessária para proteger o Dashboard.

## Implementado
- Login real: `admin` / `admin123`.
- Hash PBKDF2 e sessão local persistente por 30 dias.
- Perfis preservados: Administrador, Presidente e Visitante.
- Logout e redirecionamento automático ao Login.
- Dashboard com dados reativos do Room.
- Atalhos navegáveis para Competições, Clubes, Financeiro, Mercado e Notícias.
- Novas tabelas-base para movimentações financeiras, transferências e notícias.
- Migração do banco da versão 1 para a versão 2.

## Validação
A validação estática foi realizada. A compilação Gradle não pôde ser concluída neste ambiente porque o Gradle Wrapper precisa baixar a distribuição em `services.gradle.org`, domínio indisponível nesta execução.

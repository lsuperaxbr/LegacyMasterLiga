# Sprint 012 — Usuários, Perfis e Permissões

## Implementado

- Gestão reativa de usuários com perfis Administrador, Presidente e Visitante.
- Criação e edição de usuários, alteração de status e redefinição de senha com PBKDF2.
- Associação exclusiva de Presidentes a clubes por liga.
- Proteção contra o Administrador conectado remover o próprio acesso.
- Sessões de contas desativadas ou bloqueadas deixam de ser consideradas válidas.
- Dashboard adaptado ao perfil do usuário.
- Rotas administrativas protegidas e tela de acesso negado.
- Clubes em modo somente leitura para Presidentes e Visitantes.
- Financeiro e Mercado ocultos e bloqueados para Visitantes.

## Permissões v1

- Administrador: acesso completo, gestão de usuários, competições, inscrições, rodadas e clubes.
- Presidente: consulta de clubes, classificação, financeiro, mercado e notícias.
- Visitante: consulta de clubes, classificação e notícias.

## Banco de dados

Nenhuma nova tabela foi necessária. O banco permanece na versão 5. Foram adicionadas consultas de atualização e associação sobre as tabelas existentes.

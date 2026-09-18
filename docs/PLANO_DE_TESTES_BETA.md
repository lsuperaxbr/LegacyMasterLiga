# Plano de Testes da Beta 1

## Objetivo

Validar as funções principais com dados reais de uma liga antes do lançamento estável.

## Sequência recomendada

1. Entrar com o Administrador padrão.
2. Alterar a senha do Administrador.
3. Criar uma liga de teste usando CR.
4. Cadastrar pelo menos quatro clubes e seus escudos.
5. Criar usuários Administrador, Presidente e Visitante.
6. Associar Presidentes aos clubes.
7. Criar uma Liga de ida e volta.
8. Criar uma Copa de jogo único.
9. Inscrever os clubes em cada competição.
10. Gerar rodadas e conferir confrontos duplicados.
11. Lançar resultados e corrigir um placar.
12. Conferir classificação, gols, saldo, pontos e aproveitamento.
13. Confirmar o líder destacado em amarelo.
14. Registrar compras pelo Banco da Liga e entre clubes.
15. Confirmar débito, crédito, extrato e saldo em CR.
16. Conferir as notícias automáticas.
17. Encerrar uma temporada e validar campeão, vice e premiações.
18. Conferir histórico, estatísticas e Hall da Fama.
19. Criar e restaurar um backup `.lmlbackup`.
20. Exportar relatórios PDF e CSV.
21. Conferir permissões de Administrador, Presidente e Visitante.
22. Consultar Logs, Auditoria e Notificações.

## Critérios de aprovação

- Nenhum encerramento inesperado.
- Nenhum dado de uma competição aparece em outra.
- Nenhum saldo é alterado sem registro no extrato.
- Correções de placar não duplicam estatísticas.
- Usuários não acessam funções proibidas pelo perfil.
- Backup restaurado mantém todos os dados.

## Como registrar um problema

Anote:

- tela onde ocorreu;
- ação realizada;
- resultado esperado;
- resultado observado;
- mensagem completa do erro;
- modelo do celular e versão do Android;
- captura de tela, quando possível.

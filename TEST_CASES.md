# Suíte de Casos de Teste — Legacy Master Liga (QA-001)

Este documento contém a suíte oficial de casos de teste para validação do sistema Legacy Master Liga, baseada no Plano de Testes Funcionais.

---

## 1. Login

**ID:** LGN-001
**Título:** Login com credenciais válidas (Administrador)
**Prioridade:** Alta
**Pré-requisitos:** Aplicativo instalado, banco inicializado com dados padrão.
**Passos:**
1. Abrir o aplicativo.
2. Inserir usuário "admin".
3. Inserir senha "admin123".
4. Tocar no botão de login.
**Resultado esperado:** Usuário autenticado com sucesso e redirecionado para o Dashboard.
**Critério de aprovação:** Navegação para a rota "dashboard" concluída.
**Status:** Não Executado

**ID:** LGN-002
**Título:** Login com senha incorreta
**Prioridade:** Alta
**Pré-requisitos:** Aplicativo na tela de login.
**Passos:**
1. Inserir usuário "admin".
2. Inserir senha "errada123".
3. Tocar no botão de login.
**Resultado esperado:** Mensagem de erro informando usuário ou senha inválidos.
**Critério de aprovação:** Acesso negado e Snackbar/Texto de erro visível.
**Status:** Não Executado

**ID:** LGN-003
**Título:** Invalidação de sessão após bloqueio
**Prioridade:** Alta
**Pré-requisitos:** Usuário logado em uma sessão ativa.
**Passos:**
1. Administrador (em outro local ou após troca de conta) desativa ou bloqueia a conta do usuário logado.
2. O usuário logado tenta realizar qualquer ação reativa (ex: salvar clube).
**Resultado esperado:** O aplicativo deve invalidar a sessão e redirecionar para a tela de login.
**Critério de aprovação:** Sessão expirada detectada pelo `AuthRepository`.
**Status:** Não Executado

---

## 2. Usuários

**ID:** USR-001
**Título:** Criação de novo usuário Presidente
**Prioridade:** Média
**Pré-requisitos:** Logado como Administrador.
**Passos:**
1. Acessar menu "Usuários".
2. Tocar no botão "+".
3. Preencher nome, usuário, senha e selecionar perfil "PRESIDENT".
4. Salvar.
**Resultado esperado:** Usuário listado com sucesso na tela de gestão.
**Critério de aprovação:** Registro persistido no banco com Role = PRESIDENT.
**Status:** Não Executado

**ID:** USR-002
**Título:** Impedir duplicidade de Username
**Prioridade:** Alta
**Passos:**
1. Tentar criar um novo usuário com o mesmo "username" de um já existente.
**Resultado esperado:** Mensagem de erro informando que o usuário já existe.
**Critério de aprovação:** Restrição UNIQUE do Room disparada e tratada.
**Status:** Não Executado

---

## 3. Dashboard

**ID:** DSH-001
**Título:** Reatividade do Dashboard (Líder)
**Prioridade:** Alta
**Pré-requisitos:** Temporada ativa com resultados lançados.
**Passos:**
1. Visualizar o card de "Líder" no Dashboard.
2. Lançar um resultado que altere a liderança.
3. Voltar ao Dashboard.
**Resultado esperado:** O card de líder deve atualizar automaticamente para o novo clube.
**Critério de aprovação:** Nome e escudo do novo líder exibidos sem refresh manual.
**Status:** Não Executado

---

## 4. Clubes

**ID:** CLB-001
**Título:** Cadastro de clube com escudo interno
**Prioridade:** Alta
**Pré-requisitos:** Liga ativa criada.
**Passos:**
1. Acessar menu "Clubes".
2. Criar novo clube selecionando uma imagem da galeria.
3. Salvar o clube.
**Resultado esperado:** Clube criado e escudo visível.
**Critério de aprovação:** URI do escudo deve apontar para o armazenamento interno (CrestStorage).
**Status:** Não Executado

**ID:** CLB-002
**Título:** Remoção de escudo órfão na troca
**Prioridade:** Média
**Pré-requisitos:** Clube existente com escudo local único.
**Passos:**
1. Editar o clube.
2. Selecionar um novo escudo.
3. Salvar.
**Resultado esperado:** O arquivo físico do escudo anterior deve ser deletado.
**Critério de aprovação:** Pasta `crests/` não deve conter o arquivo antigo se ele não for usado por outros.
**Status:** Não Executado

**ID:** CLB-003
**Título:** Preservação de escudo compartilhado
**Prioridade:** Alta
**Pré-requisitos:** Dois clubes utilizando exatamente o mesmo arquivo de escudo interno.
**Passos:**
1. Excluir um dos clubes ou trocar seu escudo.
**Resultado esperado:** O arquivo físico do escudo deve ser mantido para o outro clube.
**Critério de aprovação:** `countCrestUsages` > 0 impede a deleção física.
**Status:** Não Executado

---

## 5. Liga

**ID:** LIG-001
**Título:** Criação de liga com moeda CR
**Prioridade:** Alta
**Pré-requisitos:** Logado como Administrador.
**Passos:**
1. Acessar gestão de ligas.
2. Criar liga "Liga de Teste".
**Resultado esperado:** Liga criada com moeda "CR" definida por padrão.
**Critério de aprovação:** Campo `currencyCode` no banco deve ser obrigatoriamente "CR".
**Status:** Não Executado

---

## 6. Competições

**ID:** CPT-001
**Título:** Criação de competição tipo Liga
**Prioridade:** Alta
**Pré-requisitos:** Mínimo de 3 clubes na liga.
**Passos:**
1. Criar competição com formato "Ida e Volta".
2. Definir 3 pontos por vitória.
**Resultado esperado:** Competição e Temporada 1 criadas.
**Critério de aprovação:** Status da competição = ACTIVE.
**Status:** Não Executado

**ID:** CPT-002
**Título:** Limite mínimo de clubes (3)
**Prioridade:** Alta
**Passos:**
1. Tentar criar uma competição em uma liga que possua apenas 2 clubes cadastrados.
**Resultado esperado:** Mensagem de erro ou impedimento da ação.
**Critério de aprovação:** Regra de negócio de 3 a 20 clubes respeitada.
**Status:** Não Executado

---

## 7. Temporadas

**ID:** TMP-001
**Título:** Encerramento oficial de temporada
**Prioridade:** Alta
**Pré-requisitos:** Todas as partidas finalizadas.
**Passos:**
1. Acessar "Encerrar Temporada".
2. Confirmar encerramento e premiações.
**Resultado esperado:** Temporada muda para status FINISHED.
**Critério de aprovação:** Criação de snapshot imutável na tabela `final_standings`.
**Status:** Não Executado

---

## 8. Rodadas

**ID:** ROD-001
**Título:** Status automático da Rodada
**Prioridade:** Média
**Pré-requisitos:** Rodada com todas as partidas SCHEDULED.
**Passos:**
1. Lançar resultado em uma partida da rodada.
**Resultado esperado:** Status da rodada muda para IN_PROGRESS.
**Critério de aprovação:** UI reflete o status correto imediatamente.
**Status:** Não Executado

---

## 9. Partidas

**ID:** PAR-001
**Título:** Lançamento de resultado atômico
**Prioridade:** Alta
**Pré-requisitos:** Partida agendada.
**Passos:**
1. Lançar 2 x 1.
2. Salvar.
**Resultado esperado:** Placar salvo e classificação recalculada.
**Critério de aprovação:** Lançamento registrado na auditoria e classificação atualizada.
**Status:** Não Executado

**ID:** PAR-002
**Título:** Bloqueio de resultados em temporada encerrada
**Prioridade:** Alta
**Pré-requisitos:** Temporada com status FINISHED.
**Passos:**
1. Tentar alterar o placar de uma partida de uma temporada antiga.
**Resultado esperado:** Ação bloqueada; mensagem informando que a temporada está encerrada.
**Critério de aprovação:** Validação do `SeasonStatus` no repositório de resultados.
**Status:** Não Executado

---

## 10. Classificação

**ID:** CLS-001
**Título:** Critérios de desempate (Saldo de Gols)
**Prioridade:** Alta
**Pré-requisitos:** Dois clubes com mesmos pontos e vitórias.
**Passos:**
1. Lançar resultado que dê melhor saldo a um dos clubes.
**Resultado esperado:** O clube com melhor saldo deve aparecer acima na tabela.
**Critério de aprovação:** Ordem da lista segue a hierarquia oficial.
**Status:** Não Executado

---

## 11. Mercado

**ID:** MKR-001
**Título:** Compra de jogador entre clubes
**Prioridade:** Alta
**Pré-requisitos:** Clube comprador com saldo suficiente.
**Passos:**
1. Registrar transferência de 100 CR.
**Resultado esperado:** Débito no comprador e crédito no vendedor.
**Critério de aprovação:** Conservação de CR validada (Soma total constante).
**Status:** Não Executado

**ID:** MKR-002
**Título:** Bloqueio por saldo insuficiente
**Prioridade:** Alta
**Passos:**
1. Tentar comprar jogador de 1000 CR com saldo de 500 CR.
**Resultado esperado:** Erro informado e transação impedida.
**Critério de aprovação:** Nenhum registro financeiro criado (Rollback).
**Status:** Não Executado

**ID:** MKR-003
**Título:** Isolamento financeiro entre Ligas
**Prioridade:** Alta
**Passos:**
1. Tentar realizar uma transferência entre clubes de ligas diferentes (se a UI permitir seleção).
**Resultado esperado:** Ação bloqueada; clubes devem pertencer à mesma liga.
**Critério de aprovação:** Validação de `leagueId` idêntico no repositório.
**Status:** Não Executado

---

## 12. Financeiro

**ID:** FIN-001
**Título:** Imutabilidade do extrato
**Prioridade:** Crítica
**Passos:**
1. Verificar se existe opção de excluir lançamento na UI.
**Resultado esperado:** Não deve haver botão ou comando de exclusão.
**Critério de aprovação:** Somente inclusão de ajustes permitida.
**Status:** Não Executado

---

## 13. Banco da Liga

**ID:** BNK-001
**Título:** Ajuste de saldo via Banco
**Prioridade:** Alta
**Passos:**
1. Admin realiza ajuste positivo de 50 CR em um clube.
**Resultado esperado:** Clube ganha 50, Banco perde 50.
**Critério de aprovação:** Lançamentos espelhados com tipo BANK_ADJUSTMENT.
**Status:** Não Executado

---

## 14. Notícias

**ID:** NWS-001
**Título:** Geração automática de notícia de goleada
**Prioridade:** Média
**Passos:**
1. Lançar resultado 4 x 0.
**Resultado esperado:** Notícia na categoria "GOLEADA" publicada.
**Critério de aprovação:** Texto determinístico presente no feed.
**Status:** Não Executado

---

## 15. Hall da Fama

**ID:** HOF-001
**Título:** Recorde de maior campeão
**Prioridade:** Baixa
**Passos:**
1. Encerrar 2 temporadas onde o mesmo clube vence.
**Resultado esperado:** Clube aparece como recordista de títulos.
**Critério de aprovação:** Dados extraídos corretamente da tabela `season_closures`.
**Status:** Não Executado

---

## 16. Histórico

**ID:** HST-001
**Título:** Consulta de temporadas passadas
**Prioridade:** Média
**Passos:**
1. Selecionar filtro de temporada já encerrada.
**Resultado esperado:** Tabela de classificação da época exibida corretamente.
**Critério de aprovação:** Dados batem com o snapshot arquivado.
**Status:** Não Executado

---

## 17. Estatísticas

**ID:** EST-001
**Título:** Médias de gols da liga
**Prioridade:** Baixa
**Passos:**
1. Visualizar painel de estatísticas.
**Resultado esperado:** Cálculo de total de gols / total de partidas.
**Critério de aprovação:** Valor matemático preciso.
**Status:** Não Executado

---

## 18. Backup

**ID:** BKP-001
**Título:** Geração de pacote .lmlbackup completo
**Prioridade:** Crítica
**Passos:**
1. Gerar backup manual.
**Resultado esperado:** Arquivo ZIP contendo manifesto, banco e escudos.
**Critério de aprovação:** Checksum SHA-256 gerado e incluído no manifesto.
**Status:** Não Executado

**ID:** BKP-002
**Título:** Bloqueio de versão de banco superior
**Prioridade:** Alta
**Passos:**
1. Tentar restaurar um backup onde `databaseVersion` é maior que a versão atual do app.
**Resultado esperado:** Mensagem informando que o app precisa ser atualizado.
**Critério de aprovação:** Validação de versão impede corrupção do banco.
**Status:** Não Executado

---

## 19. Restauração

**ID:** RST-001
**Título:** Restauração com Safety Backup
**Prioridade:** Crítica
**Passos:**
1. Iniciar restauração de um backup válido.
**Resultado esperado:** O sistema deve criar um backup dos dados atuais antes de prosseguir.
**Critério de aprovação:** Arquivo "safety" visível na pasta de backups.
**Status:** Não Executado

**ID:** RST-002
**Título:** Detecção de Checksum inválido
**Prioridade:** Crítica
**Passos:**
1. Alterar manualmente o conteúdo do arquivo `.lmlbackup` (ex: via editor hexadecimal).
2. Tentar restaurar.
**Resultado esperado:** Mensagem de erro informando que o arquivo pode estar corrompido.
**Critério de aprovação:** Falha na validação SHA-256 detectada.
**Status:** Não Executado

---

## 20. Relatórios

**ID:** REP-001
**Título:** Exportação de relatório PDF
**Prioridade:** Baixa
**Passos:**
1. Gerar relatório da classificação.
**Resultado esperado:** PDF aberto ou compartilhado com sucesso.
**Critério de aprovação:** Arquivo gerado no cache via FileProvider.
**Status:** Não Executado

---

## 21. Configurações

**ID:** CFG-001
**Título:** Troca de tema visual (Escuro/Claro)
**Prioridade:** Média
**Passos:**
1. Mudar preferência de tema para Light.
**Resultado esperado:** App atualiza cores imediatamente.
**Critério de aprovação:** Persistência da preferência em `app_settings`.
**Status:** Não Executado

---

## 22. Permissões

**ID:** PRM-001
**Título:** Bloqueio de Auditoria para Presidente
**Prioridade:** Alta
**Pré-requisitos:** Logado como Presidente.
**Passos:**
1. Tentar acessar menu "Auditoria" ou rota direta.
**Resultado esperado:** Acesso negado.
**Critério de aprovação:** Componente `AdminOnly` ou `AccessDeniedScreen` ativado.
**Status:** Não Executado

**ID:** PRM-002
**Título:** Restrição de Mercado para Visitante
**Prioridade:** Alta
**Pré-requisitos:** Logado como Visitante.
**Passos:**
1. Tentar acessar menu "Mercado".
**Resultado esperado:** Menu deve estar oculto ou acesso negado com mensagem clara.
**Critério de aprovação:** Bloqueio de perfil conforme RBAC.
**Status:** Não Executado

---

## Resumo da Suíte de Testes (Atualizado QA-002)

- **Quantidade Total de Casos:** 35
- **Novos Casos Adicionados:** 9
- **Quantidade por Módulo:**
  - Login: 3 (+1)
  - Usuários: 2 (+1)
  - Dashboard: 1
  - Clubes: 3 (+1)
  - Liga: 1
  - Competições: 2 (+1)
  - Temporadas: 1
  - Rodadas: 1
  - Partidas: 2 (+1)
  - Classificação: 1
  - Mercado: 3 (+1)
  - Financeiro: 1
  - Banco da Liga: 1
  - Notícias: 1
  - Hall da Fama: 1
  - Histórico: 1
  - Estatísticas: 1
  - Backup: 2 (+1)
  - Restauração: 2 (+1)
  - Relatórios: 1
  - Configurações: 1
  - Permissões: 2 (+1)

- **Prioridade Alta/Crítica:** 23
- **Prioridade Média:** 8
- **Prioridade Baixa:** 4

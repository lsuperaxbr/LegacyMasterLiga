# Plano de Testes Funcionais — Legacy Master Liga

Este documento descreve os cenários de testes funcionais para validar a integridade das regras de negócio, fluxos de usuário e estabilidade do sistema **Legacy Master Liga**.

---

## 1. Login
**Objetivo:** Garantir que apenas usuários autorizados acessem o sistema e que a política de segurança (PBKDF2) funcione corretamente.
- **Pré-requisitos:** App instalado, banco inicializado (admin/admin123).
- **Passos:**
  1. Abrir o app e digitar usuário e senha válidos.
  2. Tentar entrar com usuário inexistente.
  3. Tentar entrar com senha incorreta.
  4. Tentar entrar com conta desativada/bloqueada.
- **Resultado Esperado:** Acesso permitido apenas com credenciais válidas e ativas.
- **Critérios de Aprovação:** O sistema deve negar acesso e informar erro claro em casos de falha.
- **Prioridade:** Alta.

---

## 2. Liga
**Objetivo:** Validar a criação e gestão de ligas, respeitando a moeda oficial CR.
- **Pré-requisitos:** Usuário Administrador logado.
- **Passos:**
  1. Acessar gestão de ligas.
  2. Criar nova liga com nome válido.
  3. Tentar criar liga com nome duplicado.
  4. Verificar se a moeda CR é atribuída automaticamente.
- **Resultado Esperado:** Liga criada e persistida no Room.
- **Critérios de Aprovação:** Nome único obrigatório e moeda CR imutável.
- **Prioridade:** Média.

---

## 3. Clubes
**Objetivo:** Validar o cadastro de clubes, incluindo a importação interna de escudos.
- **Pré-requisitos:** Liga ativa criada.
- **Passos:**
  1. Cadastrar clube com nome e escudo da galeria.
  2. Verificar se o escudo foi copiado para a pasta interna.
  3. Editar nome do clube.
  4. Trocar escudo e verificar se o antigo (não usado) foi removido.
  5. Desativar clube e verificar se ele some da lista ativa mas permanece no banco.
- **Resultado Esperado:** Clube gerenciado com sucesso e imagens persistidas localmente.
- **Critérios de Aprovação:** Limite de 3 a 20 clubes por liga respeitado.
- **Prioridade:** Alta.

---

## 4. Competições
**Objetivo:** Validar a criação de campeonatos do tipo Liga (pontos corridos) ou Copa.
- **Pré-requisitos:** Mínimo de 3 clubes cadastrados na liga.
- **Passos:**
  1. Criar nova competição escolhendo formato (Ida ou Ida/Volta).
  2. Definir regras de pontuação (V/E/D).
  3. Ativar a primeira temporada.
- **Resultado Esperado:** Competição criada com status "Ativa".
- **Critérios de Aprovação:** Estrutura de competição e temporada íntegra no Room.
- **Prioridade:** Alta.

---

## 5. Rodadas e Partidas
**Objetivo:** Validar a geração automática do calendário e o lançamento de resultados.
- **Pré-requisitos:** Competição ativa com clubes inscritos.
- **Passos:**
  1. Gerar rodadas para a temporada.
  2. Lançar placar em uma partida da primeira rodada.
  3. Corrigir um placar já lançado.
- **Resultado Esperado:** Calendário gerado sem confrontos duplicados; resultados salvos e auditados.
- **Critérios de Aprovação:** Status da rodada muda para "Em Progresso" ou "Finalizada" automaticamente.
- **Prioridade:** Alta.

---

## 6. Classificação
**Objetivo:** Garantir que a tabela de classificação reflita os resultados em tempo real.
- **Pré-requisitos:** Resultados lançados na temporada.
- **Passos:**
  1. Visualizar tabela após vitória de um clube.
  2. Verificar critérios de desempate (Pontos, Vitórias, Saldo, Gols Pró).
  3. Confirmar se o líder está destacado.
- **Resultado Esperado:** Tabela calculada matematicamente correta.
- **Critérios de Aprovação:** Classificação isolada por temporada.
- **Prioridade:** Alta.

---

## 7. Mercado
**Objetivo:** Validar transferências de jogadores entre clubes utilizando apenas dinheiro (CR).
- **Pré-requisitos:** Dois clubes ativos com saldo; usuário Presidente ou Admin.
- **Passos:**
  1. Registrar transferência informando nome do jogador e valor em CR.
  2. Tentar comprar sem saldo suficiente.
  3. Verificar se o saldo do vendedor subiu e o do comprador desceu.
- **Resultado Esperado:** Transação atômica concluída; notícia gerada automaticamente.
- **Critérios de Aprovação:** Proibição de trocas sem dinheiro ou empréstimos.
- **Prioridade:** Alta.

---

## 8. Financeiro e Banco da Liga
**Objetivo:** Garantir a integridade do fluxo de caixa e a invariância da moeda CR.
- **Pré-requisitos:** Lançamentos existentes.
- **Passos:**
  1. Realizar ajuste manual de saldo via Banco da Liga.
  2. Consultar extrato do clube.
  3. Tentar apagar um lançamento (deve ser proibido pela UI/API).
- **Resultado Esperado:** Extrato reflete débitos/créditos espelhados com o Banco.
- **Critérios de Aprovação:** Soma de todos os saldos deve permanecer constante (Conservação de CR).
- **Prioridade:** Crítica.

---

## 9. Notícias
**Objetivo:** Validar o motor determinístico de geração de manchetes.
- **Pré-requisitos:** Ações de mercado ou resultados realizados.
- **Passos:**
  1. Realizar uma goleada (margem >= 3 gols).
  2. Verificar se a notícia de goleada apareceu no feed.
  3. Alterar líder da tabela e verificar notícia de troca de liderança.
- **Resultado Esperado:** Textos variados e coerentes com os eventos.
- **Critérios de Aprovação:** Sem duplicidade para o mesmo evento (Deduplicação via hash).
- **Prioridade:** Média.

---

## 10. Dashboard
**Objetivo:** Validar o centro de comando reativo do usuário.
- **Pré-requisitos:** Usuário logado.
- **Passos:**
  1. Verificar contador de notificações.
  2. Validar se o card de "Líder" mostra o time correto.
  3. Confirmar se atalhos administrativos somem para perfil Visitante.
- **Resultado Esperado:** Informações atualizadas via Flow sem necessidade de refresh manual.
- **Critérios de Aprovação:** Performance de recomposição estável.
- **Prioridade:** Alta.

---

## 11. Backup e Restauração
**Objetivo:** Garantir portabilidade total dos dados e imagens.
- **Pré-requisitos:** Dados cadastrados e escudos importados.
- **Passos:**
  1. Gerar backup manual (.lmlbackup).
  2. Mover arquivo para outro local/dispositivo.
  3. Restaurar backup.
  4. Verificar se bancos e escudos voltaram ao estado original.
- **Resultado Esperado:** Restauração bem-sucedida após criação obrigatória de Safety Backup.
- **Critérios de Aprovação:** Checksum SHA-256 validado antes da extração.
- **Prioridade:** Crítica.

---

## 12. Histórico e Hall da Fama
**Objetivo:** Validar a preservação de dados de temporadas encerradas.
- **Pré-requisitos:** Temporada encerrada oficialmente.
- **Passos:**
  1. Acessar Histórico e filtrar por temporada antiga.
  2. Verificar recordes no Hall da Fama (maior campeão, maior goleada).
- **Resultado Esperado:** Dados imutáveis e estatísticas agregadas corretas.
- **Critérios de Aprovação:** Snapshots de classificação preservados.
- **Prioridade:** Baixa.

---

## 13. Relatórios
**Objetivo:** Validar a exportação de dados para uso externo.
- **Pré-requisitos:** Dados na liga.
- **Passos:**
  1. Gerar relatório PDF de classificação e financeiro.
  2. Gerar exportação CSV de mercado.
- **Resultado Esperado:** Arquivos gerados e compartilháveis via FileProvider.
- **Critérios de Aprovação:** Formatação legível e dados precisos.
- **Prioridade:** Baixa.

---

## 14. Usuários e Permissões
**Objetivo:** Validar a política de acesso (RBAC).
- **Pré-requisitos:** Administrador logado.
- **Passos:**
  1. Criar novo usuário Presidente e associar a um clube.
  2. Logar como esse usuário e tentar acessar menu de Auditoria (deve ser bloqueado).
  3. Tentar editar outro clube (deve ser bloqueado).
- **Resultado Esperado:** Permissões aplicadas rigorosamente conforme o perfil.
- **Critérios de Aprovação:** Bloqueio via AccessPolicy e UI condicional.
- **Prioridade:** Alta.

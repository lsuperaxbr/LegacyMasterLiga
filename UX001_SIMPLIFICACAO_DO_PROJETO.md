# UX-001 — Simplificação e reorganização do Legacy Master Liga

Data: 2026-08-04  
Status: **projeto de navegação — nenhuma implementação realizada**  
Escopo analisado: rotas, Dashboard, telas, abas, botões, seletores e responsabilidades dos módulos atuais.

## 1. Decisão de produto

O Legacy Master Liga deve se comportar como um videogame de futebol: o usuário entra em um modo de jogo, encontra tudo que pertence àquele modo e volta para uma central simples.

Regras oficiais do novo desenho:

1. cada informação terá um único local oficial;
2. cada ação pertencerá a um único módulo;
3. o Início mostrará contexto e continuidade, não cópias de telas;
4. Liga e Copa serão modos independentes;
5. seletores de Liga, competição e temporada não serão repetidos em todas as telas;
6. ações administrativas ficarão dentro do módulo que administram;
7. ferramentas técnicas não disputarão espaço com os modos de jogo;
8. nenhuma rota genérica poderá perder o contexto atual;
9. resumos não poderão virar uma segunda versão da informação oficial;
10. o vocabulário deverá ser de futebol, não de sistema administrativo.

## 2. Diagnóstico do menu atual

### 2.1 Dimensão atual

O aplicativo possui 23 destinos declarados:

`Login`, `Dashboard`, `Competitions`, `Clubs`, `ClubProfile`, `Participants`, `Schedule`, `Standings`, `Finance`, `Market`, `News`, `Users`, `Audit`, `Backup`, `Settings`, `History`, `HallOfFame`, `SeasonClosure`, `Statistics`, `Notifications`, `Reports`, `Performance` e `BetaTestGuide`.

Para o administrador, o Dashboard monta 20 cartões de acesso. Antes deles, a mesma tela ainda oferece destaques e painéis clicáveis que voltam a abrir Rodadas, Classificação, Financeiro, Mercado e Notícias.

O problema não é somente quantidade. O menu atual expõe entidades técnicas — “Competições”, “Inscrições”, “Rodadas”, “Encerramento” — como se fossem produtos independentes. Para o jogador, elas são partes de Liga ou Copa.

### 2.2 Menu atual por responsabilidade

| Acesso atual | Conteúdo atual | Problema |
|---|---|---|
| Dashboard | líder, próxima rodada, saldo, transferências, notícias, resultados e 20 cartões | repete informações e atalhos de quase todos os módulos |
| Competições | cria a Liga organizadora, Liga, Copa, temporada e participantes | mistura quatro responsabilidades e dois modos de jogo |
| Inscrições | escolhe participantes por temporada | repete a seleção já presente nos diálogos de criação |
| Rodadas | seleciona Liga/competição/temporada, gera jogos, lança resultados e mostra chaveamento | mistura Liga e Copa e repete seletores globais |
| Classificação | tabela de Liga, pódio de Copa e botão para chaveamento | mistura uma informação de Liga com navegação de Copa |
| Estatísticas | resumo geral, filtros de Liga/competição/temporada e ranking histórico | mistura estatística corrente, histórico, finanças e classificação |
| Encerramento | premiação, fechamento, pódio, histórico e prêmio manual | mistura Liga, Copa, configuração, execução e histórico |
| Configurações | conta online, Liga online, nome da Liga, Banco da Liga, aparência e regras de competição | contém operações que pertencem a outros módulos |
| Mercado | negociação e histórico de transferências | já concentra parte do fluxo, mas o Banco da Liga ficou fora |
| Financeiro | saldos, extratos e operação chamada “Banco da Liga” | sobreposição de nome e responsabilidade com Mercado/Configurações |
| Histórico | campeões e temporadas | sobreposição com Hall da Fama e Estatísticas |
| Hall da Fama | recordes | segunda porta para memória histórica |
| Notícias | notícias completas | também reproduzidas no Dashboard |
| Notificações | avisos e destinos | disputa espaço no menu com Notícias |
| Relatórios | exportação PDF/CSV | ferramenta técnica exposta como modo principal |
| Auditoria, Backup, Desempenho e Roteiro Beta | manutenção técnica | ferramentas administrativas no mesmo nível dos modos de futebol |

## 3. Duplicações encontradas

### 3.1 Classificação

A classificação aparece ou é acessada por:

- cartão “Classificação” no Dashboard;
- destaque “Líder” no Dashboard;
- `StandingsScreen`;
- resumo de posição no Perfil do Clube;
- ranking por pontos no Centro de Estatísticas;
- prévia de campeão no Encerramento.

Decisão: **o único ponto oficial da tabela será `Liga → Classificação`**. Outros módulos não manterão outra tabela nem outro botão direto para ela.

O Perfil do Clube poderá exibir dados próprios do clube, mas não uma miniatura da classificação oficial. Estatísticas poderão exibir métricas, não reconstruir a tabela por pontos.

### 3.2 Rodadas e resultados

Há acesso por:

- destaque “Próxima rodada”;
- cartão “Rodadas”;
- painel “Últimos resultados”;
- tela `Schedule`;
- perfil do clube;
- telas de fechamento e estatísticas.

Decisão: rodadas deixam de ser um módulo global. Passam a existir apenas como:

- `Liga → Rodadas`;
- `Copa → Rodadas`.

O Início poderá indicar “há jogos pendentes”, mas não mostrará calendário, placares ou botão paralelo para uma rota genérica.

### 3.3 Copa e chaveamento

Atualmente a Copa atravessa:

- criação em Competições;
- participantes em Inscrições;
- partidas em Rodadas;
- chaveamento como aba secundária de Schedule;
- pódio e botão de chaveamento em Classificação;
- encerramento e prêmio em Encerramento.

Decisão: tudo será absorvido pelo módulo Copa. O chaveamento será a entrada principal, não uma aba escondida atrás de Classificação ou Schedule.

### 3.4 Premiação e encerramento

A tela atual reúne configuração de valores, fechamento de temporada, fechamento da competição, prêmio manual e histórico de prêmios.

Decisão:

- prêmio de Liga: `Liga → Premiação`;
- encerramento de Liga: `Liga → Encerrar Temporada`;
- prêmio de Copa: `Copa → Premiação`;
- encerramento de Copa: `Copa → Encerrar Copa`;
- histórico final: `História`, somente depois de concluído.

O botão global “Encerramento” será eliminado.

### 3.5 Mercado, Banco da Liga e transferências

O histórico de transferências está no Mercado, a injeção de capital do Banco está em Configurações e operações financeiras relacionadas aparecem no Financeiro.

Decisão: toda operação do Banco da Liga relacionada ao mercado pertence ao Mercado. O Financeiro continuará sendo o livro financeiro, sem virar uma segunda tela de negociação.

### 3.6 História, Hall da Fama e Estatísticas

Os três módulos apresentam recortes de desempenho passado.

Decisão:

- `História` será o único módulo de memória permanente;
- Histórico e Hall da Fama serão unificados;
- Estatísticas correntes ficarão dentro de Liga ou Copa;
- recordes históricos ficarão em `História → Recordes`.

### 3.7 Notícias e notificações

Notícias aparecem completas no próprio módulo e novamente no Dashboard. Notificações ocupam outro cartão global.

Decisão: criar uma única `Central`, com:

- `Notícias`;
- `Avisos`.

O Início poderá mostrar apenas um indicador de novidades, sem copiar artigos ou lista de avisos.

## 4. Arquitetura de navegação proposta

### 4.1 Estrutura principal

```text
Início
├── Liga
├── Copa
├── Mercado
├── Financeiro
├── Clubes
├── Central
│   ├── Notícias
│   └── Avisos
├── História
│   ├── Temporadas
│   ├── Campeões
│   └── Recordes
└── Mais
    ├── Usuários e Presidentes       [Administrador]
    ├── Ajustes do jogo              [Administrador]
    └── Sistema                      [Administrador]
        ├── Backup
        ├── Auditoria
        ├── Desempenho
        ├── Exportar dados
        └── Roteiro de teste
```

### 4.2 Menu visível por perfil

| Perfil | Menu de jogo | Gestão |
|---|---|---|
| Administrador | Início, Liga, Copa, Mercado, Financeiro, Clubes, Central, História | Usuários, Ajustes e Sistema |
| Presidente | Início, Liga, Copa, Mercado, Financeiro, Clubes, Central, História | preferências pessoais |
| Visitante | Início, Liga, Copa, Clubes, Central, História | nenhuma operação restrita |

O usuário não verá cartões que apenas terminam em “acesso negado”. Itens sem permissão devem ficar ocultos.

### 4.3 Papel do Início

O Dashboard administrativo será substituído por um Início de videogame.

O Início deverá conter somente:

- identidade da Master Liga ativa;
- perfil do usuário;
- botão “Continuar Liga”;
- botão “Continuar Copa”;
- indicador de jogos pendentes;
- indicador de novidades;
- acesso aos módulos oficiais.

Não deverá conter:

- tabela ou líder da Liga;
- lista de rodadas;
- placares completos;
- lista de transferências;
- lista de notícias;
- saldo detalhado;
- atalhos duplicados para telas internas.

## 5. Contexto único de navegação

O modelo atual repete seletores de Liga, competição e temporada em Schedule, Classificação, Estatísticas, Encerramento e outras telas.

O desenho proposto estabelece:

```text
Master Liga ativa
    ↓
Modo escolhido: Liga ou Copa
    ↓
Competição ativa
    ↓
Temporada ativa
    ↓
Seção interna
```

A Master Liga ativa será escolhida uma vez. Liga e Copa receberão `competitionId` e `seasonId` explícitos. Trocar de contexto será uma ação consciente no cabeçalho do módulo, não uma lista repetida em cada tela.

Observação de nomenclatura: hoje `LeagueEntity` representa o universo organizador, enquanto `CompetitionType.LEAGUE` representa o campeonato de pontos. Na interface futura:

- `Master Liga` = universo/salvamento ativo;
- `Liga` = campeonato por pontos;
- `Copa` = competição mata-mata.

Essa distinção elimina a ambiguidade atual de “Liga”.

## 6. Módulo Liga

### 6.1 Estrutura oficial

```text
Liga
├── Nova Temporada
├── Rodadas
├── Classificação
├── Artilharia
├── Estatísticas
├── Premiação
└── Encerrar Temporada
```

### 6.2 Fluxo

```text
Entrar em Liga
  ↓
Existe temporada ativa?
  ├── Não → Nova Temporada → escolher clubes → confirmar regulamento
  └── Sim → Rodadas
                ↓
          lançar resultados
                ↓
          Classificação
                ↓
          Premiação
                ↓
      Encerrar Temporada
```

### 6.3 Regras de propriedade

- `Nova Temporada` absorve a criação de temporada e a inscrição de clubes;
- `Rodadas` é o único calendário da Liga;
- `Classificação` é a única tabela oficial do aplicativo;
- `Artilharia` contém somente ranking de jogadores da Liga ativa;
- `Estatísticas` não reproduz a classificação;
- `Premiação` configura e apresenta os prêmios da temporada;
- `Encerrar Temporada` mostra a prévia final e executa o fechamento;
- nenhum botão de premiação ou encerramento existirá fora deste módulo.

## 7. Módulo Copa

### 7.1 Estrutura oficial

```text
Copa
├── Nova Copa
├── Chaveamento       [tela principal]
├── Rodadas
├── Artilharia
├── Estatísticas
├── Premiação
└── Encerrar Copa
```

### 7.2 Fluxo

```text
Entrar em Copa
  ↓
Existe Copa ativa?
  ├── Não → Nova Copa → escolher clubes → jogo único ou ida/volta
  └── Sim → Chaveamento
                ↓
          Rodadas / placares
                ↓
       avanço automático de fases
                ↓
              Final
                ↓
          Encerrar Copa
```

### 7.3 Decisões definitivas

- Copa será exclusivamente mata-mata;
- fase de grupos não aparecerá na criação nem na navegação;
- `GROUPS_AND_KNOCKOUT` não será uma opção nova;
- jogo único será mata-mata com uma perna, não outro produto separado;
- ida e volta será mata-mata com duas pernas;
- participantes serão definidos no fluxo `Nova Copa`;
- chaveamento abrirá por padrão;
- Rodadas será uma aba separada;
- Copa nunca abrirá Classificação;
- campeão, vice e premiação pertencerão ao encerramento da própria Copa.

Dados históricos de Copas antigas com grupos não deverão ser apagados quando esse desenho for implementado. Eles poderão permanecer somente para consulta histórica; a proibição é para novas Copas e para a experiência ativa.

## 8. Classificação: ponto único oficial

Rota conceitual única:

```text
Início → Liga → Classificação
```

Serão eliminados:

- cartão global “Classificação”;
- clique no destaque “Líder”;
- tabela dentro de Copa;
- tabela resumida no Perfil do Clube;
- ranking por pontos que replique a tabela em Estatísticas;
- botões de chaveamento dentro de Classificação.

O clube poderá mostrar números próprios — jogos, vitórias, gols — sem informar posição oficial. A posição continuará disponível somente na tabela da Liga.

## 9. Módulo Mercado

### 9.1 Estrutura oficial

```text
Mercado
├── Negociar
│   ├── Compra
│   ├── Venda
│   └── Troca
├── Banco da Liga
│   ├── jogadores livres/liberados
│   └── operações próprias do Banco
└── Transferências
    └── histórico oficial
```

Empréstimo poderá ser acrescentado futuramente dentro de `Negociar`, nunca como novo módulo global.

### 9.2 Limite em relação ao Financeiro

- Mercado é dono da negociação e do histórico de transferências;
- Banco da Liga deixa Configurações e passa para Mercado;
- Financeiro registra o efeito em CR e o extrato;
- Financeiro não repete a ficha completa da transferência;
- Mercado pode mostrar o saldo necessário para autorizar uma operação, mas não substitui o extrato financeiro.

## 10. Módulo Financeiro

O Financeiro permanece independente:

```text
Financeiro
├── Saldos
├── Extrato
└── Lançamentos autorizados
```

Não conterá:

- criação de compra, venda ou troca;
- histórico visual de transferências;
- cadastro de jogador livre;
- configurações do Banco da Liga ligadas ao mercado.

Uma movimentação originada no Mercado aparecerá no extrato como referência financeira, sem duplicar o fluxo de negociação.

## 11. Usuários: Presidente com dois clubes

### 11.1 Regra oficial

- um Presidente poderá controlar no máximo dois clubes ativos;
- o Banco da Liga não conta como clube controlado;
- dois clubes do mesmo Presidente nunca poderão negociar entre si;
- a proibição vale para compra, venda, transferência gratuita, troca e empréstimo futuro;
- Administrador não poderá ignorar essa regra ao registrar a operação em nome dos clubes;
- transações históricas não serão apagadas.

### 11.2 Matriz de negociação

| Origem | Destino | Resultado |
|---|---|---|
| clube A do Presidente 1 | clube B do Presidente 1 | bloqueado |
| clube A do Presidente 1 | clube de outro Presidente | permitido, respeitando saldo e permissões |
| clube de outro Presidente | clube B do Presidente 1 | permitido, respeitando saldo e permissões |
| clube controlado | Banco da Liga | permitido conforme a operação |
| Banco da Liga | clube controlado | permitido conforme a operação |

### 11.3 Situação técnica atual

A tabela `clubs` já possui `presidentUserId` sem índice único, portanto consegue representar dois clubes para o mesmo usuário. Entretanto, a aplicação atual pressupõe somente um clube:

- `RoomClubRepository.assignPresident()` limpa todas as associações antes de atribuir uma;
- `ClubDao.observeByPresident()` retorna apenas um registro com `LIMIT 1`;
- `MarketContext` e `MarketUiState` possuem `ownClubId` singular;
- a tela de Usuários permite selecionar um único clube;
- Mercado filtra as opções usando um único clube próprio.

Quando essa regra for implementada, será necessário:

1. tratar clubes controlados como lista;
2. permitir selecionar até dois clubes no módulo Usuários;
3. validar o limite de dois de forma transacional;
4. aplicar a proibição no domínio/repository, não somente na interface;
5. excluir automaticamente o segundo clube próprio das opções de contraparte;
6. aplicar a mesma política a transferências e trocas;
7. reutilizar a política para empréstimos futuros;
8. cobrir tentativas do Presidente e do Administrador em testes.

Não é necessária uma nova tabela apenas para suportar dois clubes, desde que o vínculo continue sendo `clubs.presidentUserId`. Uma futura implementação deverá avaliar concorrência e sincronização antes de decidir por mudança de schema.

## 12. Telas eliminadas do menu principal

“Eliminada” abaixo significa eliminada da arquitetura-alvo. Nenhum arquivo foi removido nesta Sprint.

| Tela/acesso atual | Destino no novo desenho |
|---|---|
| Competições | dividido entre Liga e Copa |
| Inscrições | absorvido por Nova Temporada e Nova Copa |
| Rodadas global | dividido entre Liga → Rodadas e Copa → Rodadas |
| Classificação global | Liga → Classificação |
| Encerramento global | dividido entre Liga e Copa |
| Estatísticas global | dividido entre Liga/Copa; histórico vai para História |
| Hall da Fama | História → Recordes |
| Notícias global | Central → Notícias |
| Notificações global | Central → Avisos |
| Relatórios no menu principal | Mais → Sistema → Exportar dados |
| Auditoria no menu principal | Mais → Sistema → Auditoria |
| Backup no menu principal | Mais → Sistema → Backup |
| Desempenho no menu principal | Mais → Sistema → Desempenho |
| Roteiro Beta no menu principal | Mais → Sistema → Roteiro de teste |
| botão global Encerramento | removido; ações contextuais em Liga/Copa |
| botão global Classificação | removido; acesso somente dentro de Liga |

## 13. Telas unificadas

| Nova área | Origem atual |
|---|---|
| Liga | partes de Competições, Inscrições, Schedule, Standings, Estatísticas e Encerramento |
| Copa | partes de Competições, Inscrições, Schedule, Standings e Encerramento |
| Mercado | Mercado, Transferências e Banco da Liga em Configurações |
| História | Histórico e Hall da Fama, mais recordes históricos de Estatísticas |
| Central | Notícias e Notificações |
| Sistema | Backup, Auditoria, Desempenho, Relatórios e Roteiro Beta |
| Clubes | lista e Perfil do Clube como fluxo interno |
| Usuários e Presidentes | usuários, permissões e atribuição de até dois clubes |

## 14. Botões e fluxos removidos

### Botões duplicados

- “Classificação” no Dashboard;
- destaque clicável “Líder”;
- “Rodadas” global;
- destaque “Próxima rodada” como segunda rota;
- painel clicável “Últimos resultados”;
- cartões duplicados de Mercado e Notícias no corpo e nos acessos rápidos;
- “Acessar Chaveamento Completo” dentro de Classificação;
- “Encerramento” no Dashboard;
- “Salvar premiações” fora do módulo correspondente;
- “Banco da Liga” dentro de Configurações;
- criação de participantes em uma tela separada do modo de jogo.

### Fluxos desnecessários

- escolher a Master Liga novamente em cada tela;
- escolher competição e temporada em uma rota genérica;
- abrir Classificação para então abrir Chaveamento;
- criar competição e depois visitar Inscrições para completar o mesmo cadastro;
- abrir Configurações para operar o Banco da Liga;
- sair do modo Liga/Copa para premiar ou encerrar;
- navegar por ferramentas técnicas misturadas aos modos de futebol.

## 15. Contrato conceitual de rotas

Este é apenas o desenho futuro; nenhuma rota foi alterada nesta Sprint.

```text
home
league/{competitionId}/{seasonId}/{section}
cup/{competitionId}/{seasonId}/{section}
market/{section}
finance/{section}
clubs
clubs/{clubId}
central/{section}
history/{section}
management/users
management/settings
system/{tool}
```

Seções de Liga:

`rounds`, `standings`, `scorers`, `statistics`, `awards`, `closure`.

Seções de Copa:

`bracket`, `rounds`, `scorers`, `statistics`, `awards`, `closure`.

Regras:

- `competitionId` e `seasonId` nunca poderão ser descartados;
- Copa sempre cairá em `bracket` quando a seção não for informada;
- Liga nunca abrirá `bracket`;
- Classificação não terá rota global;
- rotas antigas deverão ser retiradas somente depois que seus consumidores forem migrados e testados.

## 16. Impacto técnico futuro

### 16.1 Navegação e estado

- substituir destinos globais por hubs contextuais de Liga e Copa;
- criar uma fonte única para Master Liga, competição e temporada ativas;
- preservar argumentos no processo e em links internos;
- impedir ViewModels diferentes de escolherem contextos divergentes;
- criar testes de rota e retorno para cada seção.

### 16.2 Apresentação

- reduzir o Dashboard de 20 cartões para modos de jogo e uma área “Mais”;
- transformar Schedule, Standings e Closure em componentes internos reutilizáveis;
- separar visualmente chaveamento e rodadas da Copa;
- esconder opções sem permissão em vez de encaminhar para acesso negado;
- padronizar títulos, voltar e seleção de temporada.

### 16.3 Domínio

- separar a orquestração de Liga da orquestração de Copa;
- impedir que Results e Schedule definam sozinhos a navegação do campeonato;
- criar política única de posse e negociação entre clubes;
- manter Financeiro como registro contábil e Mercado como dono da negociação;
- separar estatística corrente de histórico permanente.

### 16.4 Banco de dados

Esta Sprint não altera banco. Para a implementação futura:

- não é necessário apagar campos de grupos para retirar a opção da interface;
- Copas antigas com grupos devem ser preservadas para histórico;
- o vínculo atual `clubs.presidentUserId` já admite mais de um clube por Presidente;
- a mudança para dois clubes exige consultas e regras novas, mas não obrigatoriamente migration;
- Artilharia não pode ser produzida com segurança apenas a partir do placar dos clubes.

### 16.5 Artilharia

Hoje não existem entidade, DAO ou fluxo de lançamento de gols por jogador. A presença de “nome do jogador” no Mercado não constitui cadastro esportivo nem evento de gol.

Portanto, `Liga → Artilharia` e `Copa → Artilharia` fazem parte da arquitetura oficial, mas sua implementação exigirá uma Sprint funcional separada para definir:

- identidade do jogador;
- vínculo jogador/clube/temporada;
- evento de gol por partida;
- correção de súmula;
- gols contra e eventuais critérios;
- agregação por Liga e Copa;
- preservação histórica.

Não se deve criar uma artilharia fictícia ou manual apenas para preencher a aba.

### 16.6 Compatibilidade

- rotas antigas poderão precisar de redirecionamento temporário;
- notificações deverão apontar para o novo módulo proprietário;
- relatórios deverão ler os mesmos dados sem se tornar segunda tela oficial;
- permissões devem ser reaplicadas nos hubs;
- nenhuma reorganização deverá mudar IDs ou resultados existentes.

## 17. Vantagens do novo desenho

- aparência de modo carreira/videogame;
- redução drástica do menu principal;
- fim da mistura entre Liga e Copa;
- chaveamento imediatamente visível;
- classificação em um único local;
- menos seletores e menos perda de contexto;
- menor chance de abrir a competição ou temporada errada;
- premiação e encerramento no momento correto do fluxo;
- Mercado completo sem invadir o Financeiro;
- ferramentas técnicas fora da experiência esportiva;
- regras de Presidente mais claras;
- arquitetura mais testável, pois cada módulo terá entradas e responsabilidades definidas.

## 18. Riscos que o desenho evita

- rota genérica retornando ao Dashboard;
- Copa exibindo tabela de Liga;
- botão de chaveamento sem `competitionId` ou `seasonId`;
- usuário encerrando a temporada errada;
- premiação aplicada fora da competição ativa;
- contextos divergentes entre Schedule, Standings e Estatísticas;
- negociação entre dois clubes do mesmo Presidente;
- manutenção técnica confundida com atividade do jogo.

## 19. Ordem recomendada para uma implementação futura

Esta ordem não foi iniciada nesta Sprint:

1. criar contrato de contexto e rotas, com testes;
2. simplificar o Início e mover ferramentas para Mais/Sistema;
3. criar o hub Liga e absorver Rodadas/Classificação;
4. criar o hub Copa com Chaveamento como entrada;
5. mover Premiação e Encerramento para os hubs;
6. unificar Mercado, Banco e Transferências;
7. unificar História/Hall da Fama e Central;
8. implementar a regra de dois clubes e bloqueio de negociação;
9. retirar as rotas antigas depois dos testes de regressão;
10. planejar Artilharia como funcionalidade separada.

Cada passo deverá manter o modo offline e ter testes de navegação, permissão e preservação de contexto.

## 20. Critérios de aceite do redesenho

O futuro UX-001 estará concluído somente quando:

- Liga e Copa abrirem como módulos independentes;
- Copa abrir diretamente no chaveamento;
- fase de grupos não puder ser criada em uma nova Copa;
- Classificação tiver apenas um acesso oficial;
- não existir botão global de Premiação ou Encerramento;
- participantes forem escolhidos dentro de Nova Temporada/Nova Copa;
- Mercado contiver Banco da Liga e Transferências;
- Financeiro permanecer separado e sem duplicar negociação;
- Histórico e Hall da Fama estiverem unificados;
- ferramentas técnicas estiverem fora do menu esportivo;
- um Presidente puder controlar até dois clubes;
- clubes do mesmo Presidente não conseguirem negociar por nenhum caminho;
- todas as rotas preservarem competição e temporada;
- dados antigos, inclusive Copas históricas com grupos, permanecerem íntegros.

## 21. Conclusão

O menu atual expõe a estrutura interna do software. O menu proposto expõe modos de futebol.

A mudança central é deixar de navegar por tabelas técnicas — Competições, Inscrições, Schedule, Standings e Closure — e passar a navegar por intenções claras: jogar a Liga, jogar a Copa, negociar, acompanhar finanças e consultar a história.

Esta Sprint produziu somente este projeto documental. Nenhum arquivo Kotlin, Compose, Room, Firebase ou APK foi alterado ou gerado.

# ONLINE-006 — sincronização esportiva Firestore ↔ Room

Data: 02/08/2026

## Resultado

A ONLINE-006 foi implementada sobre o projeto corrigido e validado da entrega anterior. Ligas locais continuam usando apenas Room. Ligas marcadas como online passam a espelhar competições, temporadas, participantes, rodadas, partidas e classificação no Firestore.

O banco foi evoluído de v18 para v19 sem recriar tabelas esportivas, apagar temporadas ou alterar os IDs locais já usados por Copa, histórico e finanças.

## Arquitetura

- Room continua sendo a fonte lida pela interface, mantendo o aplicativo funcional sem rede.
- Firestore é a origem compartilhada das ligas online.
- online_sync_records relaciona cada ID local a um UUID global estável e guarda revisão, timestamps e estado.
- online_sync_queue mantém operações offline com ID idempotente, revisão-base, tentativas e erro.
- O sincronizador observa mudanças nas tabelas esportivas somente de ligas online.
- Ao reconectar ou receber um snapshot, a fila é retomada.
- Listeners independentes acompanham competitions, seasons, participants, rounds, matches e standings.
- Snapshots remotos são aplicados dentro de transação Room e usam o mapeamento global para não duplicar registros.
- Clubes referenciados são identificados de forma estável pelo nome único dentro da liga; em uma instalação nova, o espelho mínimo do clube é criado antes da partida, participante ou classificação.

## Conflitos

- Cada documento possui schemaVersion, revision, authorUid, clientUpdatedAt e updatedAt do servidor.
- Escritas usam transação Firestore e só avançam quando a revisão-base ainda corresponde à revisão remota.
- Metadados com revisão remota mais nova adotam a versão da nuvem.
- Placar ou pênaltis concorrentes não são sobrescritos silenciosamente: o item entra em CONFLICT.
- A tela Liga Online mostra fila pendente e conflitos. A resolução segura disponível aceita explicitamente os placares atuais da nuvem.

## Segurança

firestore.rules ganhou regras para as seis subcoleções esportivas:

- somente membros podem ler e escrever;
- cloudId e schemaVersion não podem mudar;
- a revisão deve avançar exatamente em uma unidade;
- authorUid deve ser o usuário autenticado;
- exclusão direta permanece bloqueada.

As regras precisam ser publicadas no projeto Firebase antes do teste entre celulares.

## Modo offline e finanças

- Uma liga local não cria registros de sincronização.
- Uma liga online sem autenticação ou rede continua gravando e lendo Room.
- Operações pendentes sobrevivem ao reinício do aplicativo.
- Finanças não foram enviadas ao Firestore. Permanecem locais até existir uma operação atômica segura no servidor, conforme a decisão de escopo.

## Validação

- 27 testes unitários/Robolectric aprovados.
- 0 falhas, 0 erros e 0 ignorados.
- Teste específico da migração 18 → 19 confirmou preservação de dados antigos e criação das tabelas de sincronização.
- Compilação Kotlin, Room/KSP e Hilt aprovada.
- assembleDebug aprovado.
- APK: 26.417.268 bytes.
- SHA-256 do APK: 873ADC7B2687E06A24D5D992E34E1CF5A253807A36F320C865F3C4D2499B7C76.

O processo do Windows retorna código 1 depois do sucesso por duas entradas inválidas preexistentes no PATH do Python; o Gradle registrou BUILD SUCCESSFUL e os artefatos foram gerados normalmente.

## Pendências operacionais

1. Publicar o novo firestore.rules no projeto Firebase correto.
2. Testar em dois dispositivos/instâncias: criar resultado offline no primeiro, reconectar e confirmar atualização no segundo.
3. Manter finanças fora da sincronização até existir transação segura no servidor.

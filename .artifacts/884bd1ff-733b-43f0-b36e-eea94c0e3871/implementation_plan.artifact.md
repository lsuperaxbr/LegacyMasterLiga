# Plano de Implementação: SINCRONIZAÇÃO DE MEMBROS E VISIBILIDADE DE USUÁRIOS

Este plano visa resolver a falta de sincronização dos presidentes dos clubes entre aparelhos e permitir que todos os membros da liga visualizem a lista de participantes.

## Problema Identificado
Atualmente, o aplicativo sincroniza os dados da liga (partidas, clubes, etc.), mas não sincroniza a lista de **Usuários (Membros)**. Quando um usuário escolhe um time (ex: Rafael escolhe o Pisa), o sistema grava o UID do Rafael no documento do clube na nuvem. No entanto, o celular do Anderson não "conhece" o Rafael (ele não existe na tabela local `users` do Anderson), por isso o time do Pisa aparece como "Sem presidente" para ele.

## Objetivos
- **Sincronização de Membros**: Garantir que todos os membros da liga sejam espelhados na tabela local `users` de cada aparelho conectado.
- **Visibilidade de Presidentes**: Resolver o problema de "Pisa sem presidente" fazendo com que o app identifique o usuário correto pelo UID sincronizado.
- **Lista de Participantes**: Permitir que Presidentes visualizem a lista de usuários da liga no menu "Mais", promovendo a transparência.

## Proposed Changes

### 1. Camada de Modelo (Nuvem)

#### [MODIFY] [CloudMember.kt](file:///C:/Users/luizh/Downloads/Telegram%20Desktop/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/online/domain/CloudLeagueModels.kt)
- Adicionar o campo `username: String` ao modelo `CloudMember`. Isso é necessário para criar o registro local do usuário no celular dos outros membros.

#### [MODIFY] [FirestoreCloudLeagueRepository.kt](file:///C:/Users/luizh/Downloads/Telegram%20Desktop/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/online/data/FirestoreCloudLeagueRepository.kt)
- Atualizar a gravação de membros para incluir o `username` vindo do banco de dados local durante o "Join" ou "Promote".

### 2. Motor de Sincronização

#### [MODIFY] [OnlineSportsSyncManager.kt](file:///C:/Users/luizh/Downloads/Telegram%20Desktop/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/online/sync/OnlineSportsSyncManager.kt)
- Adicionar o tipo `"MEMBER"` à lista de sincronização.
- Implementar `applyMember`: sempre que um novo membro entrar na liga na nuvem, o app criará (ou atualizará) um registro na tabela local `users` com o `firebaseUid` correspondente.
- Isso permitirá que a função `applyClub` encontre o `userId` local correto ao processar o `presidentFirebaseUid`.

### 3. Interface e Navegação

#### [MODIFY] [LegacyNavigationMenu.kt](file:///C:/Users/luizh/Downloads/Telegram%20Desktop/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/core/navigation/LegacyNavigationMenu.kt)
- Liberar o item de menu "Usuários" para o cargo `PRESIDENT`.

#### [MODIFY] [UsersScreen.kt](file:///C:/Users/luizh/Downloads/Telegram%20Desktop/LegacyMasterLiga/app/src/main/java/com/example/legacymasterliga/feature/users/presentation/UsersScreen.kt)
- Ocultar o botão flutuante de "Adicionar Usuário" e o ícone de "Editar" para quem não é Administrador. Os Presidentes poderão ver a lista, mas não alterá-la.

## Verification Plan

### Manual Verification
1. **Teste de Espelhamento**: Rafael entra na liga no Celular A -> Anderson abre a lista de Usuários no Celular B -> Anderson deve ver o nome e apelido do Rafael na lista.
2. **Teste de Presidência**: Rafael assume o Pisa no Celular A -> Anderson abre o Dashboard no Celular B -> O time do Pisa deve mostrar "Presidente: Rafael" em vez de "Sem presidente".
3. **Segurança**: Confirmar que o Rafael (Presidente) não consegue editar os dados do Anderson através da lista de usuários.

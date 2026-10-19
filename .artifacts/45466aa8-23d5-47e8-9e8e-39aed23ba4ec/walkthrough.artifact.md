# Walkthrough - Correção da Persistência do Escudo Vetorial (`ClubBadge`)

Correção completa da persistência do escudo vetorial (`ClubBadge`) no aplicativo **Legacy Master Liga**, solucionando a causa raiz que fazia o escudo reverter para o padrão cinza ao salvar as alterações do clube.

---

## Causa Raiz Solucionada

1. **Repasse da ViewModel (`ClubsViewModel.kt`):** A função `saveClub()` ao chamar `clubRepository.create()` e `clubRepository.update()` passou a enviar o parâmetro nomeado `badgeConfigJson = crestUri` com a string JSON gerada pelo editor.
2. **Fallback Inteligente no Repositório (`RoomClubRepository.kt`):** A função `update()` do repositório resolve o JSON usando `resolvedBadgeJson = badgeConfigJson ?: (if (crestUri?.startsWith("{") == true) crestUri else current.badgeConfigJson)`, garantindo que o JSON do escudo nunca seja sobrescrito com `null` no banco.
3. **Leitura na Tela de Perfil (`ClubDao.kt` & `ClubProfileScreen.kt`):**
   - A query `observeProfileHeader` passou a selecionar a coluna `clubs.badgeConfigJson AS badgeConfigJson`.
   - `ClubProfileHeaderRow`, `ClubProfileHeader` e `ClubProfileScreen` repassam o `badgeConfigJson` diretamente para o `NativeCrest`, permitindo que o perfil do clube exiba instantaneamente o escudo vetorial customizado sem depender do fallback genérico.
4. **Sincronização Online (`OnlineSportsSyncManager.kt`):**
   - O campo `badgeConfigJson` é enviado e recebido nas operações de sincronização com o Firestore.

---

## Resultados da Verificação e Build
- **Build:** `clean app:assembleDebug` executado com sucesso (0 erros de compilação).

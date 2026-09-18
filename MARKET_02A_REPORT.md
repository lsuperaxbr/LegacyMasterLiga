# Relatório Técnico: MARKET-02A — Elencos e Mercado Vivo Local

## 1. Visão Geral
Implementação da base estrutural para gestão de jogadores e integração com o Mercado, operando localmente via Room com suporte a anexos de imagem (PES6 skills).

## 2. Mudanças na Camada de Dados
- **Nova Entidade**: `PlayerEntity` (tabela `players`).
- **Novo DAO**: `PlayerDao` com suporte a filtros de mercado e observação por clube.
- **Migration 19 -> 20**: Adição da tabela `players` e coluna `playerId` em `transfers` (preparação de arquitetura).
- **Conversores**: Adicionado suporte ao enum `MarketStatus`.

## 3. Experiência do Usuário (UX/UI)
- **Perfil do Clube**: Adicionada aba **Elenco** com listagem completa de jogadores e status de mercado.
- **Ficha do Jogador**: Nova tela `PlayerDetailScreen` exibindo:
    - Dados técnicos (Nome, Posição, Clube).
    - Status de Mercado (À Venda, Negociável, Inegociável, Fora do Mercado).
    - Print de habilidades do PES6 (via Photo Picker).
- **Mercado Vivo**: Nova aba **Jogadores** no módulo Mercado com busca por nome e filtros por status e clube.

## 4. Higiene e Proteção
- **Firebase/Firestore**: Integralmente preservados. Nenhuma sincronização online de jogadores foi ativada nesta etapa.
- **Motores Esportivos**: Lógicas de Liga e Copa permanecem originais.
- **Histórico**: Transferências antigas baseadas em nome (`String`) continuam funcionando sem necessidade de vínculo obrigatório com `PlayerEntity`.

## 5. Resultados de Verificação
- **Build**: Sucesso (`assembleDebug` gerado).
- **Testes**: 87 testes aprovados. (Falha isolada preexistente no módulo de Backup mantida).
- **Persistência**: Testada a seleção e persistência da URI da galeria para o print de habilidades.

---
**Engenheiro-Chefe**
Legacy Master Liga

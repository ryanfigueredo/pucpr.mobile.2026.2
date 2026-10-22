# CookBook

Trabalho acadêmico — app Android nativo de receitas culinárias. O usuário explora receitas, vê ingredientes e modo de preparo, favorita as que mais gosta e monta uma lista de compras automaticamente a partir dos ingredientes.

Pacote: `br.pucpr.cookbook`

## Descrição

O CookBook tem 5 telas principais, uma por integrante da equipe:

1. **Início** — busca, categorias, destaques e receitas recentes.
2. **Explorar Receitas** — listagem completa com busca e filtros, cadastro de novas receitas.
3. **Detalhes da Receita** — ingredientes, modo de preparo, ajuste de porções, favoritar, editar/excluir (receitas próprias) e enviar ingredientes para a lista de compras.
4. **Favoritos** — receitas marcadas como favoritas, com desfazer.
5. **Lista de Compras** — itens agrupados por receita de origem, com checkbox de comprado, item manual e limpeza da lista.

Todos os dados (receitas, ingredientes, lista de compras) são persistidos localmente com Room — não há backend nem dependência de internet. Na primeira execução, o banco é populado com 12 receitas reais em português (2 por categoria).

## Stack

| Camada | Tecnologia | Versão |
|---|---|---|
| Linguagem | Kotlin | 2.2.10 |
| UI | Jetpack Compose + Material 3 | Compose BOM 2026.02.01 |
| Navegação | Navigation Compose | 2.9.7 |
| Persistência | Room (KSP) | 2.8.5 |
| Injeção de dependência | Hilt (+ KSP) | 2.60.1 |
| Hilt + Compose | androidx.hilt:hilt-lifecycle-viewmodel-compose | 1.4.0 |
| Assincronismo | Kotlin Coroutines + Flow/StateFlow | via Kotlin 2.2.10 |
| Build | Gradle Kotlin DSL + Version Catalog | AGP 9.3.1 / Gradle 9.5.0 |
| compileSdk / minSdk | 37 / 29 | |

## Como rodar

1. Abra o projeto no Android Studio (ou use o wrapper `./gradlew` direto no terminal).
2. Confirme que o `local.properties` aponta para o seu Android SDK (`sdk.dir=...`).
3. Compile e rode:
   ```bash
   ./gradlew assembleDebug     # compila
   ./gradlew installDebug      # instala num emulador/dispositivo conectado
   ```
4. Na primeira abertura, o app detecta o banco Room vazio e insere automaticamente 12 receitas de exemplo (ver `data/seed/RecipeSeedData.kt` e `data/seed/DatabaseSeeder.kt`).

Não é necessário nenhum serviço externo, chave de API ou arquivo de configuração adicional — tudo roda localmente.

## Arquitetura — View → ViewModel → Repository → DAO → Room

```
┌───────────────────┐      StateFlow<UiState>      ┌────────────────────┐
│   Composable        │ <───────────────────────────  │     ViewModel        │
│   (Screen.kt)        │ ────────────────────────────> │   @HiltViewModel      │
│   stateless,          │     eventos (onClick, ...)    │   @Inject constructor  │
│   collectAsState...   │                                │   expõe StateFlow      │
└───────────────────┘                                └──────────┬─────────┘
                                                                   │ chama métodos suspend / Flow
                                                                   ▼
                                                        ┌────────────────────┐
                                                        │    Repository         │
                                                        │  interface + Impl     │
                                                        │  (ex.: RecipeRepository)│
                                                        └──────────┬─────────┘
                                                                   │ delega ao
                                                                   ▼
                                                        ┌────────────────────┐
                                                        │        DAO            │
                                                        │  @Dao interface       │
                                                        │  retorna Flow<List<T>>│
                                                        └──────────┬─────────┘
                                                                   │
                                                                   ▼
                                                        ┌────────────────────┐
                                                        │   Room (SQLite)       │
                                                        │   AppDatabase          │
                                                        └────────────────────┘
```

Regras seguidas no projeto:
- Composables **nunca** acessam DAO ou Repository diretamente — só falam com o ViewModel.
- ViewModels são `@HiltViewModel` com construtor `@Inject`, expõem `StateFlow<UiState>` e funções de evento (`onX()`).
- Repositories são interface + implementação, recebendo o DAO injetado pelo Hilt.
- Hilt: `CookBookApplication` é `@HiltAndroidApp`; `MainActivity` é `@AndroidEntryPoint`; `di/DatabaseModule.kt` e `di/RepositoryModule.kt` usam `@Module @InstallIn(SingletonComponent::class)` com `@Provides` explícito para `AppDatabase`, DAOs e cada Repository.
- Toda tela trata estado de `loading`, `erro` (Snackbar) e lista vazia (`EmptyState`), e mantém o estado na rotação de tela (o `StateFlow` do ViewModel sobrevive a mudanças de configuração).

## Estrutura do banco (Room)

- **`recipes`** (`RecipeEntity`): id, nome, descrição, categoria (enum), tempoPreparoMin, dificuldade (enum), porções, modoPreparo (lista de passos via `TypeConverter`), imagemRes, favorita, criadaPeloUsuario, criadaEm.
- **`ingredients`** (`IngredientEntity`): id, recipeId (FK → recipes.id, `CASCADE`), nome, quantidade, unidade.
- **`shopping_items`** (`ShoppingItemEntity`): id, nome, quantidade, unidade, comprado, recipeId (opcional).
- **`RecipeWithIngredients`** (`@Relation`): junta uma receita com seus ingredientes, usada na tela de Detalhes.
- `Converters.kt` traduz `List<String>` (passos) e os enums `RecipeCategory`/`RecipeDifficulty` para `String` e volta.
- `exportSchema = true`, schema histórico salvo em `app/schemas/`.

## Divisão da equipe

| Integrante | Tela | Conceito principal | Arquivos |
|---|---|---|---|
| Integrante 1 | Início + identidade visual | Compose/Material 3, tema | `ui/screens/home/*`, `ui/theme/*` |
| Integrante 2 | Explorar, busca e filtros, cadastro de receita | Navigation Compose (argumentos opcionais), formulário com validação | `ui/screens/explore/*`, `ui/components/RecipeFormSheet.kt`, `ui/components/RecipeFormState.kt` |
| Integrante 3 | Detalhes e ingredientes | Navigation (argumento obrigatório `recipeId`), `@Relation` do Room | `ui/screens/details/*` |
| Integrante 4 | Favoritos e persistência com Room | Entity, DAO, Database, CRUD, Flow reativo | `ui/screens/favorites/*`, `data/local/*` |
| Integrante 5 | Lista de compras, integração e Hilt | MVVM/StateFlow, `@Module`/`@Provides`/`@HiltViewModel` | `ui/screens/shopping/*`, `di/*`, `data/repository/*`, `data/seed/*`, `ui/navigation/*`, `MainActivity.kt`, `CookBookApplication.kt` |

*(nomes dos integrantes a preencher pela equipe)*

## Documentos complementares

- [`APRESENTACAO.md`](APRESENTACAO.md) — roteiro de apresentação por integrante, mapeado aos critérios R1–R5.
- [`TESTE_MANUAL.md`](TESTE_MANUAL.md) — checklist de validação manual no emulador.
# pucpr.mobile.2026.2

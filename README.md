# CookBook

Trabalho acadêmico — app Android nativo de receitas culinárias. O usuário explora receitas, vê ingredientes e modo de preparo, favorita as que mais gosta e monta uma lista de compras automaticamente a partir dos ingredientes.

Pacote: `br.pucpr.cookbook`

## Equipe

- Pedro Henrique Junqueira Colantonio
- João Vitor Simão Ribas
- Arthur Krauze de Paula
- Lucas Cicarello Girata
- Ryan D'Oliveira Lopes Figueredo

## Descrição

O CookBook tem 7 telas. As cinco originais continuam com a divisão da equipe:

1. **Início** — busca, categorias, destaques e receitas recentes.
2. **Explorar Receitas** — listagem completa com busca e filtros, cadastro de novas receitas.
3. **Detalhes da Receita** — ingredientes, modo de preparo, ajuste de porções, favoritar, editar/excluir (receitas próprias) e enviar ingredientes para a lista de compras.
4. **Favoritos** — receitas marcadas como favoritas, com desfazer.
5. **Lista de Compras** — itens agrupados por receita de origem, com checkbox de comprado, item manual e limpeza da lista. Itens comprados podem preencher o formulário da despensa.
6. **Planejamento semanal** — CRUD de refeições com receita, data, tipo e porções; navegação entre semanas, acesso à receita e envio de ingredientes para compras com as quantidades ajustadas.
7. **Minha despensa** — CRUD de alimentos com nome, quantidade, unidade e validade opcional; busca e filtro de vencidos ou que vencem nos próximos 7 dias.

As novas telas são acessadas pelos atalhos em **Início → Minha cozinha**, mantendo a barra inferior atual. Cadastro e edição usam painéis (`ModalBottomSheet`), seguindo o formulário de receitas. Excluir uma receita remove também suas refeições planejadas. Os alimentos da despensa são independentes das receitas.

Ao adicionar um item comprado à despensa, o app abre um formulário preenchido para conferir quantidade e validade. Nada é salvo até tocar em **Salvar**, e o item continua na lista de compras. Alimentos com o mesmo nome podem ter registros separados para representar lotes com validades diferentes. O planejamento envia os ingredientes da refeição selecionada; não desconta automaticamente o estoque.

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

1. Use o JDK 25 e o Android SDK 37.0. Abra o projeto no Android Studio ou use o wrapper `./gradlew` direto no terminal (`.\gradlew.bat` no PowerShell).
2. Confirme que o `local.properties` aponta para o seu Android SDK (`sdk.dir=...`).
3. Compile e rode:
   ```bash
   ./gradlew assembleDebug     # compila
   ./gradlew installDebug      # instala num emulador/dispositivo conectado
   ```
4. Na primeira abertura, o app detecta o banco Room vazio e insere automaticamente 12 receitas de exemplo (ver `data/seed/RecipeSeedData.kt` e `data/seed/DatabaseSeeder.kt`).

O primeiro build precisa de internet para baixar o Gradle e as dependências. Os dados do app (receitas, compras, despensa) continuam locais no Room; apenas o **login usa o Firebase Authentication** (ver abaixo).

## Autenticação (Firebase)

A tela de **Login** usa Firebase Authentication (e-mail/senha). É possível criar conta, entrar e sair; a sessão fica salva entre aberturas do app. Ao abrir, se já houver usuário logado o app vai direto para a Início.

O arquivo `app/google-services.json` **não está no repositório** (contém as chaves do projeto Firebase e está no `.gitignore`). Para compilar, cada integrante precisa baixar o seu:

1. Firebase Console → projeto **av2pucpr-mobile** → ⚙️ Configurações do projeto.
2. Em "Seus apps", selecione o app Android `br.pucpr.cookbook` e baixe o `google-services.json`.
3. Coloque o arquivo em `app/google-services.json`.

O provedor **E-mail/senha** já está habilitado no console (Authentication → Método de login).

### Validação pelo terminal

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Os testes locais usam JUnit, Robolectric e Coroutines Test, sem emulador. Cobrem validação dos formulários, datas e semanas, CRUDs com Room, persistência após reabrir o banco, migração da versão 1 para a 2, filtros da despensa, importação de item comprado e ajuste de ingredientes por porções no planejamento. O APK de debug é gerado em `app/build/outputs/apk/debug/app-debug.apk`.

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
- **`planned_meals`** (`PlannedMealEntity`): id, recipeId (FK → recipes.id, `CASCADE`), dia, tipo de refeição e porções. `PlannedMealWithRecipe` relaciona o planejamento com a receita.
- **`pantry_items`** (`PantryItemEntity`): id, nome, quantidade, unidade e validade opcional. Datas são armazenadas como dias desde 01/01/1970 (`LocalDate.toEpochDay`), sem horário ou fuso.
- **Versão 2:** `MIGRATION_1_2` adiciona somente as duas tabelas novas e seus índices, preservando receitas, ingredientes, favoritos e compras existentes. Não há recriação destrutiva do banco.
- `Converters.kt` traduz `List<String>` (passos) e os enums `RecipeCategory`/`RecipeDifficulty`/`MealType` para `String` e volta.
- `exportSchema = true`, schema histórico salvo em `app/schemas/`.

## Divisão da equipe

| Integrante | Tela | Conceito principal | Arquivos |
|---|---|---|---|
| Pedro Henrique Junqueira Colantonio | Início + identidade visual | Compose/Material 3, tema | `ui/screens/home/*`, `ui/theme/*` |
| João Vitor Simão Ribas | Explorar, busca e filtros, cadastro de receita | Navigation Compose (argumentos opcionais), formulário com validação | `ui/screens/explore/*`, `ui/components/RecipeFormSheet.kt`, `ui/components/RecipeFormState.kt` |
| Arthur Krauze de Paula | Detalhes e ingredientes | Navigation (argumento obrigatório `recipeId`), `@Relation` do Room | `ui/screens/details/*` |
| Lucas Cicarello Girata | Favoritos e persistência com Room | Entity, DAO, Database, CRUD, Flow reativo | `ui/screens/favorites/*`, `data/local/*` |
| Ryan D'Oliveira Lopes Figueredo | Lista de compras, integração e Hilt | MVVM/StateFlow, `@Module`/`@Provides`/`@HiltViewModel` | `ui/screens/shopping/*`, `di/*`, `data/repository/*`, `data/seed/*`, `ui/navigation/*`, `MainActivity.kt`, `CookBookApplication.kt` |

## Documentos complementares

- [`APRESENTACAO.md`](APRESENTACAO.md) — roteiro de apresentação por integrante, mapeado aos critérios R1–R5.
- [`TESTE_MANUAL.md`](TESTE_MANUAL.md) — checklist de validação manual no emulador.
# pucpr.mobile.2026.2

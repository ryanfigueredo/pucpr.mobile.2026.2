package br.pucpr.cookbook.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeDifficulty
import br.pucpr.cookbook.ui.theme.CookBookTheme

/**
 * BottomSheet de cadastro/edição de receita. É um Composable sem estado próprio de
 * domínio: recebe o [RecipeFormState] atual e devolve a cópia atualizada via [onChange],
 * usado tanto pelo FAB da tela Explorar quanto pelo botão Editar da tela Detalhes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFormSheet(
    formState: RecipeFormState,
    isEditing: Boolean,
    onChange: (RecipeFormState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (isEditing) "Editar receita" else "Nova receita",
                style = MaterialTheme.typography.titleLarge,
            )

            OutlinedTextField(
                value = formState.nome,
                onValueChange = { onChange(formState.copy(nome = it)) },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = formState.descricao,
                onValueChange = { onChange(formState.copy(descricao = it)) },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Categoria", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RecipeCategory.entries) { categoria ->
                    CategoryFilterChip(
                        categoria = categoria,
                        selected = formState.categoria == categoria,
                        onClick = { onChange(formState.copy(categoria = categoria)) },
                    )
                }
            }

            Text("Dificuldade", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecipeDifficulty.entries.forEach { dificuldade ->
                    androidx.compose.material3.FilterChip(
                        selected = formState.dificuldade == dificuldade,
                        onClick = { onChange(formState.copy(dificuldade = dificuldade)) },
                        label = { Text(dificuldade.label) },
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formState.tempoPreparoMin,
                    onValueChange = { onChange(formState.copy(tempoPreparoMin = it.filter(Char::isDigit))) },
                    label = { Text("Tempo (min)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = formState.porcoes,
                    onValueChange = { onChange(formState.copy(porcoes = it.filter(Char::isDigit))) },
                    label = { Text("Porções") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }

            Text("Ingredientes", style = MaterialTheme.typography.titleMedium)
            formState.ingredientes.forEachIndexed { index, ingrediente ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = ingrediente.nome,
                        onValueChange = { novo ->
                            onChange(formState.replaceIngredient(index, ingrediente.copy(nome = novo)))
                        },
                        label = { Text("Ingrediente") },
                        singleLine = true,
                        modifier = Modifier.weight(2f),
                    )
                    OutlinedTextField(
                        value = ingrediente.quantidade,
                        onValueChange = { novo ->
                            val filtrado = novo.filter { c -> c.isDigit() || c == ',' || c == '.' }
                            onChange(formState.replaceIngredient(index, ingrediente.copy(quantidade = filtrado)))
                        },
                        label = { Text("Qtd.") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    UnidadeDropdownField(
                        value = ingrediente.unidade,
                        onValueChange = { novo ->
                            onChange(formState.replaceIngredient(index, ingrediente.copy(unidade = novo)))
                        },
                        modifier = Modifier.weight(1.2f),
                    )
                    IconButton(onClick = { onChange(formState.removeIngredient(index)) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remover ingrediente")
                    }
                }
            }
            OutlinedButton(onClick = { onChange(formState.addIngredient()) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Adicionar ingrediente")
            }

            Text("Modo de preparo", style = MaterialTheme.typography.titleMedium)
            formState.passos.forEachIndexed { index, passo ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = passo,
                        onValueChange = { novo -> onChange(formState.replacePasso(index, novo)) },
                        label = { Text("Passo ${index + 1}") },
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onChange(formState.removePasso(index)) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remover passo")
                    }
                }
            }
            OutlinedButton(onClick = { onChange(formState.addPasso()) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Adicionar passo")
            }

            formState.errorMessage?.let { erro ->
                Text(erro, color = MaterialTheme.colorScheme.error)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Salvar") }
            }
        }
    }
}

private fun RecipeFormState.replaceIngredient(index: Int, novo: IngredientDraft): RecipeFormState =
    copy(ingredientes = ingredientes.toMutableList().also { it[index] = novo })

private fun RecipeFormState.removeIngredient(index: Int): RecipeFormState =
    copy(ingredientes = ingredientes.toMutableList().also { it.removeAt(index) }.ifEmpty { listOf(IngredientDraft()) })

private fun RecipeFormState.addIngredient(): RecipeFormState =
    copy(ingredientes = ingredientes + IngredientDraft())

private fun RecipeFormState.replacePasso(index: Int, novo: String): RecipeFormState =
    copy(passos = passos.toMutableList().also { it[index] = novo })

private fun RecipeFormState.removePasso(index: Int): RecipeFormState =
    copy(passos = passos.toMutableList().also { it.removeAt(index) }.ifEmpty { listOf("") })

private fun RecipeFormState.addPasso(): RecipeFormState = copy(passos = passos + "")

@Preview(showBackground = true)
@Composable
private fun RecipeFormSheetPreview() {
    CookBookTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Preview estático (ModalBottomSheet requer runtime)")
        }
    }
}

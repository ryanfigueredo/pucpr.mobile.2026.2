package br.pucpr.cookbook.ui.screens.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.pucpr.cookbook.data.local.entity.MealType
import br.pucpr.cookbook.data.local.entity.RecipeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealFormSheet(
    formState: MealFormState,
    recipes: List<RecipeEntity>,
    isSaving: Boolean,
    onChange: (MealFormState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().imePadding()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(if (formState.id == 0L) "Nova refeição" else "Editar refeição", style = MaterialTheme.typography.titleLarge)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (!isSaving) expanded = it },
            ) {
                OutlinedTextField(
                    value = recipes.firstOrNull { it.id == formState.recipeId }?.nome.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isSaving,
                    label = { Text("Receita") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    recipes.forEach { recipe ->
                        DropdownMenuItem(
                            text = { Text(recipe.nome) },
                            onClick = {
                                onChange(formState.copy(recipeId = recipe.id, porcoes = recipe.porcoes.toString()))
                                expanded = false
                            },
                        )
                    }
                }
            }
            if (recipes.isEmpty()) Text("Cadastre uma receita em Explorar para planejar suas refeições.")
            OutlinedTextField(
                value = formState.data,
                onValueChange = { onChange(formState.copy(data = it)) },
                label = { Text("Data (dd/mm/aaaa)") },
                singleLine = true,
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Refeição", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MealType.entries) { tipo ->
                    FilterChip(
                        selected = formState.tipo == tipo,
                        onClick = { onChange(formState.copy(tipo = tipo)) },
                        label = { Text(tipo.label) },
                        enabled = !isSaving,
                    )
                }
            }
            OutlinedTextField(
                value = formState.porcoes,
                onValueChange = { onChange(formState.copy(porcoes = it)) },
                label = { Text("Porções") },
                singleLine = true,
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            formState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(onClick = onSave, enabled = !isSaving && recipes.isNotEmpty(), modifier = Modifier.weight(1f)) {
                    Text(if (isSaving) "Salvando…" else "Salvar")
                }
            }
        }
    }
}

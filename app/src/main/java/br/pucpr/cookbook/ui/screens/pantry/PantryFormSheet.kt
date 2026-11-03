package br.pucpr.cookbook.ui.screens.pantry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.pucpr.cookbook.ui.components.UnidadeDropdownField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryFormSheet(
    formState: PantryFormState,
    isSaving: Boolean,
    onChange: (PantryFormState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().imePadding()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(if (formState.id == 0L) "Novo alimento" else "Editar alimento", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = formState.nome,
                onValueChange = { onChange(formState.copy(nome = it)) },
                label = { Text("Alimento") },
                singleLine = true,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = formState.quantidade,
                onValueChange = { onChange(formState.copy(quantidade = it)) },
                label = { Text("Quantidade") },
                singleLine = true,
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Unidade", style = MaterialTheme.typography.labelLarge)
            UnidadeDropdownField(
                value = formState.unidade,
                onValueChange = { onChange(formState.copy(unidade = it)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = formState.validade,
                onValueChange = { onChange(formState.copy(validade = it)) },
                label = { Text("Validade (dd/mm/aaaa)") },
                supportingText = { Text("Opcional. Deixe em branco se não souber.") },
                singleLine = true,
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                modifier = Modifier.fillMaxWidth(),
            )
            formState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(onClick = onSave, enabled = !isSaving, modifier = Modifier.weight(1f)) {
                    Text(if (isSaving) "Salvando…" else "Salvar")
                }
            }
        }
    }
}

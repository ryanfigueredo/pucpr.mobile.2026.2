package br.pucpr.cookbook.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import br.pucpr.cookbook.data.model.UnidadeMedida

/**
 * Seletor de unidade de medida (lista fechada em [UnidadeMedida]), reaproveitado pelo
 * formulário de receitas e pelo formulário de item manual da lista de compras.
 *
 * `ExposedDropdownMenu` e `Modifier.menuAnchor` são funções-membro do escopo
 * [androidx.compose.material3.ExposedDropdownMenuBoxScope] (não têm import próprio) —
 * por isso só podem ser chamadas dentro do bloco de conteúdo do [ExposedDropdownMenuBox].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnidadeDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            isError = isError,
            // Sem label: a setinha do trailingIcon já deixa claro que é um seletor.
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            UnidadeMedida.entries.forEach { unidade ->
                DropdownMenuItem(
                    text = { Text(unidade.label) },
                    onClick = {
                        onValueChange(unidade.label)
                        expanded = false
                    },
                )
            }
        }
    }
}

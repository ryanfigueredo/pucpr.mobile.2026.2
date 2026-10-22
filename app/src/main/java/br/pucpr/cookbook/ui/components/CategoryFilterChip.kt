package br.pucpr.cookbook.ui.components

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.pucpr.cookbook.data.local.entity.RecipeCategory

@Composable
fun CategoryFilterChip(
    categoria: RecipeCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(categoria.label) },
        modifier = modifier,
    )
}

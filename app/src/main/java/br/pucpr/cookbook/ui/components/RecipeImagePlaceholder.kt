package br.pucpr.cookbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.pucpr.cookbook.data.local.entity.RecipeCategory

private data class CategoryVisual(val icon: ImageVector, val color: Color)

private fun visualFor(categoria: RecipeCategory): CategoryVisual = when (categoria) {
    RecipeCategory.MASSAS -> CategoryVisual(Icons.Filled.DinnerDining, Color(0xFFC2521E))
    RecipeCategory.CARNES -> CategoryVisual(Icons.Filled.Restaurant, Color(0xFF8C3A2B))
    RecipeCategory.AVES -> CategoryVisual(Icons.Filled.Egg, Color(0xFFBF8A3D))
    RecipeCategory.VEGETARIANO -> CategoryVisual(Icons.Filled.Eco, Color(0xFF5E6E2E))
    RecipeCategory.SOBREMESAS -> CategoryVisual(Icons.Filled.Cake, Color(0xFFC4578B))
    RecipeCategory.BEBIDAS -> CategoryVisual(Icons.Filled.LocalCafe, Color(0xFF8C6D00))
}

/** Cor de destaque da categoria, reaproveitada em badges, chips selecionados e no hero dos Detalhes. */
fun categoryColor(categoria: RecipeCategory): Color = visualFor(categoria).color

// Placeholder visual (ícone + cor por categoria) no lugar de imagem real:
// o app não depende de internet nem de um drawable por receita.
@Composable
fun RecipeImagePlaceholder(
    categoria: RecipeCategory,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    cornerRadius: Dp = 18.dp,
) {
    val visual = visualFor(categoria)
    Box(
        modifier = modifier
            .size(size)
            .background(
                Brush.linearGradient(
                    listOf(visual.color.copy(alpha = 0.28f), visual.color.copy(alpha = 0.12f)),
                ),
                RoundedCornerShape(cornerRadius),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = visual.icon,
            contentDescription = categoria.label,
            tint = visual.color,
            modifier = Modifier.size(size / 2),
        )
    }
}

package br.pucpr.cookbook.ui.components

import br.pucpr.cookbook.data.model.UnidadeMedida
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Formata uma quantidade no padrão brasileiro: vírgula decimal, no máximo 2 casas e sem
 * ".0"/",00" desnecessário. Ex.: 300.0 -> "300", 7.5 -> "7,5", 6.75 -> "6,75".
 */
fun formatQuantidade(valor: Double): String {
    val arredondado = (valor * 100).roundToLong() / 100.0
    if (arredondado == arredondado.toLong().toDouble()) return arredondado.toLong().toString()
    val comDuasCasas = String.format(Locale.forLanguageTag("pt-BR"), "%.2f", arredondado)
    return comDuasCasas.trimEnd('0').trimEnd(',')
}

/** Quantidade formatada + unidade no singular/plural correto, ex.: "7,5 dentes". */
fun formatQuantidadeComUnidade(quantidade: Double, unidadeLabel: String): String =
    "${formatQuantidade(quantidade)} ${UnidadeMedida.displayLabel(unidadeLabel, quantidade)}"

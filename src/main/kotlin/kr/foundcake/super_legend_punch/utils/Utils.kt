package kr.foundcake.super_legend_punch.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

object Utils {

    private val plain = PlainTextComponentSerializer.plainText()

    fun plainText(component: Component) : String = plain.serialize(component)

}
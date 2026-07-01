package kr.foundcake.super_legend_punch

import kr.foundcake.super_legend_punch.extension.cachedSafeMatches
import kr.foundcake.super_legend_punch.utils.Utils
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.damage.DamageType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.PrepareAnvilEvent

class EventListener: Listener {

    private val blockItemName = Regex("^(((S|SS|SSS)+)+Punch!\\b|S+Punch!)$").toPattern()
    private val punchSuffix = "Punch!"

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAttack(event: EntityDamageByEntityEvent) {
        if (
            event.entity.type === EntityType.PLAYER ||
            event.damager.type !== EntityType.PLAYER||
            event.damageSource.damageType !== DamageType.PLAYER_ATTACK
            ) return
        val player = event.damager as Player
        val item = player.inventory.itemInMainHand
        val itemName = Utils.plainText(item.effectiveName())
        val count = punchPower(itemName)
        if(item.type === Material.ARMADILLO_SCUTE && count != null) {
            event.damage = count * 20.0
            if(event.isCancelled) {
                event.isCancelled = false
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        if(
            event.result === null ||
            event.result!!.type !== Material.ARMADILLO_SCUTE ||
            event.view.player.gameMode === GameMode.CREATIVE
        ) return
        val name = Utils.plainText(event.result!!.effectiveName())
        if(blockItemName.cachedSafeMatches(name) == true){
            event.result = null
        }
    }

    private fun punchPower(itemName: String): Int? {
        if (!itemName.endsWith(punchSuffix)) return null

        val sCount = itemName.length - punchSuffix.length
        if (sCount <= 0) return null

        for (index in 0..<sCount) {
            if (itemName[index] != 'S') return null
        }

        return sCount
    }
}

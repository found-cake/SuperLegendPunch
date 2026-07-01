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

    private val blockItemName = Regex("^(S+)+Punch!$").toPattern()

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
        if(
            item.type === Material.ARMADILLO_SCUTE &&
            itemName != "" &&
            itemName.first() == 'S' &&
            itemName.endsWith("Punch!")
            ) {
            var count = 0
            for(c in itemName) {
                if(c == 'S') {
                    count++
                } else {
                    break
                }
            }
            event.damage = count * 10.0
            if(event.isCancelled) {
                event.isCancelled = false
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        if(
            event.result === null ||
            event.view.player.gameMode !== GameMode.CREATIVE ||
            event.result!!.type !== Material.ARMADILLO_SCUTE
        ) return
        val name = Utils.plainText(event.result!!.effectiveName())
        if(blockItemName.cachedSafeMatches(name) == true){
            event.result = null
        }
    }
}
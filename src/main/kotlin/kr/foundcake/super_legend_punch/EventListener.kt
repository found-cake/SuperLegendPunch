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
import org.bukkit.inventory.ItemStack
import java.util.regex.Pattern

class EventListener : Listener {

    private val blockedItemName = Regex("^(((S|SS|SSS)+)+Punch!\\b|S+Punch!)$").toPattern()
    private val punchSuffix = "Punch!"
    private val targetMaterial = Material.ARMADILLO_SCUTE

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAttack(event: EntityDamageByEntityEvent) = event {
        +rule { entity.type !== EntityType.PLAYER }
        +rule { damager.type === EntityType.PLAYER }
        +rule { damageSource.damageType === DamageType.PLAYER_ATTACK }

        player {
            hand(targetMaterial) {
                plainName.legendPunch then { event punch it }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPrepareAnvil(event: PrepareAnvilEvent) = event {
        +rule { view.player.gameMode !== GameMode.CREATIVE }

        target(targetMaterial) {
            (plainName cursedBy blockedItemName) then {
                event.result = null
            }
        }
    }

    private inline operator fun EntityDamageByEntityEvent.invoke(
        script: AttackScript.() -> Unit
    ) {
        AttackScript(this).script()
    }

    private inline operator fun PrepareAnvilEvent.invoke(
        script: AnvilScript.() -> Unit
    ) {
        AnvilScript(this).script()
    }

    private class AttackScript(
        private val event: EntityDamageByEntityEvent
    ) {
        private var alive = true

        fun rule(block: EntityDamageByEntityEvent.() -> Boolean): EntityDamageByEntityEvent.() -> Boolean =
            block

        operator fun (EntityDamageByEntityEvent.() -> Boolean).unaryPlus() {
            val gate = this
            alive = alive && gate(event)
        }

        fun player(block: Player.() -> Unit) {
            if (alive) {
                (event.damager as? Player)?.block()
            }
        }
    }

    private class AnvilScript(
        private val event: PrepareAnvilEvent
    ) {
        private var alive = true

        fun rule(block: PrepareAnvilEvent.() -> Boolean): PrepareAnvilEvent.() -> Boolean =
            block

        operator fun (PrepareAnvilEvent.() -> Boolean).unaryPlus() {
            val gate = this
            alive = alive && gate(event)
        }

        fun target(material: Material, block: ItemStack.() -> Unit) {
            if (alive) {
                event.result
                    ?.takeIf { it.type === material }
                    ?.block()
            }
        }
    }

    private infix fun EntityDamageByEntityEvent.punch(power: Int) {
        damage = 9999.9 / power
        takeIf { isCancelled }?.let {
            isCancelled = false
        }
    }

    private inline fun Player.hand(
        material: Material,
        block: ItemStack.() -> Unit
    ) {
        inventory.itemInMainHand
            .takeIf { it.type === material }
            ?.block()
    }

    private inline infix fun <T> T?.then(block: (T) -> Unit) {
        this?.let(block)
    }

    private inline infix fun Boolean?.then(block: () -> Unit) {
        if (this == true) {
            block()
        }
    }

    private infix fun String.cursedBy(pattern: Pattern): Boolean? =
        pattern.cachedSafeMatches(this)

    private val ItemStack.plainName: String
        get() = Utils.plainText(effectiveName())

    private val String.legendPunch: Int?
        get() = takeIf { endsWith(punchSuffix) }
            ?.removeSuffix(punchSuffix)
            ?.takeIf { it.isNotEmpty() }
            ?.takeIf { it.all { ch -> ch == 'S' } }
            ?.length
}

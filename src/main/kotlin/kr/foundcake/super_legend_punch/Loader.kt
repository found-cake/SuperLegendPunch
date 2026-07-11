package kr.foundcake.super_legend_punch

import org.bukkit.plugin.java.JavaPlugin

class Loader: JavaPlugin() {

    override fun onEnable() {
        this.server.pluginManager.registerEvents(EventListener(), this)
    }
}
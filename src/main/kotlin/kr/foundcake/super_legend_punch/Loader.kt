package kr.foundcake.super_legend_punch

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Loader: JavaPlugin() {

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(EventListener(), this)
    }
}
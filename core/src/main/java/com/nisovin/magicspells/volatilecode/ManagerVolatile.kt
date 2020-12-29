package com.nisovin.magicspells.volatilecode

import org.bukkit.Bukkit

import com.nisovin.magicspells.MagicSpells

object ManagerVolatile {

    fun constructVolatileCodeHandler(): VolatileCodeHandle {
        try {
            val nmsPackage = Bukkit.getServer().javaClass.getPackage().name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3]
            val volatileCode = Class.forName("com.nisovin.magicspells.volatilecode.$nmsPackage.VolatileCode${nmsPackage.replace("v", "")}")

            MagicSpells.log("Found volatile code handler for $nmsPackage.")
            var volatileCodeHandle = volatileCode.newInstance() as VolatileCodeHandle;
            return volatileCodeHandle
        } catch (ex: Exception) {
            // No volatile code handler found
        }

        return VolatileCodeDisabled()
    }

}

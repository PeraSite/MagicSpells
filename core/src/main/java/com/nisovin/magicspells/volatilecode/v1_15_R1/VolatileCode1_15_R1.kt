package com.nisovin.magicspells.volatilecode.v1_15_R1

import java.lang.reflect.Field

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.util.Vector
import org.bukkit.inventory.*
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.craftbukkit.v1_15_R1.entity.*
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.craftbukkit.v1_15_R1.CraftServer
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack

import com.nisovin.magicspells.util.*
import com.nisovin.magicspells.MagicSpells
import com.nisovin.magicspells.util.compat.EventUtil
import com.nisovin.magicspells.volatilecode.VolatileCodeDisabled
import com.nisovin.magicspells.volatilecode.VolatileCodeHandle

import net.minecraft.server.v1_15_R1.*

private typealias nmsItemStack = net.minecraft.server.v1_15_R1.ItemStack

class VolatileCode1_15_R1: VolatileCodeHandle {

    private var fallback = VolatileCodeDisabled()

    private var entityFallingBlockFallHurtAmountField: Field? = null
    private var entityFallingBlockFallHurtMaxField: Field? = null
    private var craftMetaSkullClass: Class<*>? = null
    private var craftMetaSkullProfileField: Field? = null
    private var entityLivingPotionEffectColor: DataWatcherObject<Int>? = null

    init {
        try {
            this.entityFallingBlockFallHurtAmountField = EntityFallingBlock::class.java.getDeclaredField("fallHurtAmount")
            this.entityFallingBlockFallHurtAmountField!!.isAccessible = true

            this.entityFallingBlockFallHurtMaxField = EntityFallingBlock::class.java.getDeclaredField("fallHurtMax")
            this.entityFallingBlockFallHurtMaxField!!.isAccessible = true

            this.craftMetaSkullClass = Class.forName("org.bukkit.craftbukkit.v1_15_R1.inventory.CraftMetaSkull")
            this.craftMetaSkullProfileField = this.craftMetaSkullClass!!.getDeclaredField("profile")
            this.craftMetaSkullProfileField!!.isAccessible = true

            val entityLivingPotionEffectColorField = EntityLiving::class.java.getDeclaredField("e")
            entityLivingPotionEffectColorField.isAccessible = true;
            this.entityLivingPotionEffectColor = entityLivingPotionEffectColorField.get(null) as DataWatcherObject<Int>
        } catch (e: Exception) {
            MagicSpells.error("THIS OCCURRED WHEN CREATING THE VOLATILE CODE HANDLE FOR 1.15, THE FOLLOWING ERROR IS MOST LIKELY USEFUL IF YOU'RE RUNNING THE LATEST VERSION OF MAGICSPELLS.")
            e.printStackTrace()
        }
    }

    override fun addPotionGraphicalEffect(entity: LivingEntity, color: Int, duration: Int) {
        val livingEntity = (entity as CraftLivingEntity).handle;
        val dataWatcher = livingEntity.dataWatcher;
        dataWatcher.set(entityLivingPotionEffectColor, color)
        if (duration > 0) {
            MagicSpells.scheduleDelayedTask({
                var c = 0
                if (livingEntity.effects.isNotEmpty()) {
                    c = PotionUtil.a(livingEntity.effects.values)
                }
                dataWatcher.set(entityLivingPotionEffectColor, c)
            }, duration)
        }
    }

    override fun sendFakeSlotUpdate(player: Player, slot: Int, item: ItemStack?) {
        val nmsItem: nmsItemStack?
        if (item != null) {
            nmsItem = CraftItemStack.asNMSCopy(item)
        } else {
            nmsItem = null
        }
        val packet = PacketPlayOutSetSlot(0, slot.toShort() + 36, nmsItem!!)
        (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
    }

    override fun simulateTnt(target: Location, source: LivingEntity, explosionSize: Float, fire: Boolean): Boolean {
        val e = EntityTNTPrimed((target.world as CraftWorld).handle, target.x, target.y, target.z, (source as CraftLivingEntity).handle)
        val c = CraftTNTPrimed(Bukkit.getServer() as CraftServer, e)
        val event = ExplosionPrimeEvent(c, explosionSize, fire)
        EventUtil.call(event)
        return event.isCancelled
    }

    override fun setExperienceBar(player: Player, level: Int, percent: Float) {
        val packet = PacketPlayOutExperience(percent, player.totalExperience, level)
        (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
    }

    override fun setFallingBlockHurtEntities(block: FallingBlock, damage: Float, max: Int) {
        val efb = (block as CraftFallingBlock).handle
        try {
            block.setHurtEntities(true)
            this.entityFallingBlockFallHurtAmountField!!.setFloat(efb, damage)
            this.entityFallingBlockFallHurtMaxField!!.setInt(efb, max)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun playDragonDeathEffect(location: Location) {
        val dragon = EntityEnderDragon(EntityTypes.ENDER_DRAGON, (location.world as CraftWorld).handle)
        dragon.setPositionRotation(location.x, location.y, location.z, location.yaw, 0f)

        val packet24 = PacketPlayOutSpawnEntityLiving(dragon)
        val packet38 = PacketPlayOutEntityStatus(dragon, 3.toByte())
        val packet29 = PacketPlayOutEntityDestroy(dragon.bukkitEntity.entityId)

        val box = BoundingBox(location, 64.0)
        val players = ArrayList<Player>()
        for (player in location.world!!.players) {
            if (!box.contains(player)) continue
            players.add(player)
            (player as CraftPlayer).handle.playerConnection.sendPacket(packet24)
            player.handle.playerConnection.sendPacket(packet38)
        }

        MagicSpells.scheduleDelayedTask({
            for (player in players) {
                if (player.isValid) {
                    (player as CraftPlayer).handle.playerConnection.sendPacket(packet29)
                }
            }
        }, 250)
    }

    override fun setClientVelocity(player: Player, velocity: Vector) {
        val packet = PacketPlayOutEntityVelocity(player.entityId, Vec3D(velocity.x, velocity.y, velocity.z))
        (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
    }

    override fun colorize(message: String?): String {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message.toString())
    }

    private fun getNBTTag(item: ItemStack): NBTTagCompound {
        val itemNms: nmsItemStack = CraftItemStack.asNMSCopy(item)
        return (if (itemNms.hasTag()) itemNms.tag else NBTTagCompound()) as NBTTagCompound
    }

    override fun setNBTString(item: ItemStack, key: String, value: String): ItemStack {
        val tag = getNBTTag(item)
        tag.setString(key, value)
        val itemNms: nmsItemStack = CraftItemStack.asNMSCopy(item)
        itemNms.tag = tag
        return CraftItemStack.asBukkitCopy(itemNms)
    }

    override fun getNBTString(item: ItemStack, key: String): String {
        return getNBTTag(item).getString(key)
    }

    override fun setInventoryTitle(player: Player, title: String) {
        val entityPlayer = (player as CraftPlayer).handle
        val container = entityPlayer.activeContainer
        val packet = PacketPlayOutOpenWindow(container.windowId, container.type, ChatMessage(title))
        entityPlayer.playerConnection.sendPacket(packet)
        entityPlayer.updateInventory(container)
    }

    override fun createSmithingRecipe(namespaceKey: NamespacedKey, result: ItemStack, base: Material, addition: Material): Recipe? {
        return null
    }

}

package io.github.gabehowe.magikbucket

import net.minecraft.server.v1_16_R3.NBTTagCompound
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_16_R3.persistence.CraftPersistentDataContainer
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class BucketEvents(private val magikBucket: MagikBucket) : Listener {
    @EventHandler
    fun onBucketEmptyEvent(event: PlayerBucketEmptyEvent) {
        if (event.itemStack == null) {
            return
        }
        val bucket = event.itemStack!!
        if (!event.player.persistentDataContainer.has(magikBucket.justBucketedKey, PersistentDataType.INTEGER)) {
            return
        }
        if(event.player.persistentDataContainer.get(magikBucket.justBucketedKey, PersistentDataType.INTEGER) == 1) {
            return
        }
        event.player.sendMessage("hi")
        event.isCancelled = true
        event.player.persistentDataContainer.set(magikBucket.justBucketedKey, PersistentDataType.INTEGER, 0)
    }
    @EventHandler
    fun onBucketEmptyRightClickEvent(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }
        if (event.item == null) {
            return
        }
        if (event.clickedBlock == null) {
            return
        }
        if (!event.player.persistentDataContainer.has(magikBucket.bucketCooldownKey, PersistentDataType.INTEGER)) {
            return
        }
        if (event.player.persistentDataContainer.get(magikBucket.bucketCooldownKey, PersistentDataType.INTEGER) == 1) {
            return
        }
        val bucket = event.item!!
        if (!bucket.itemMeta.persistentDataContainer.has(magikBucket.isBucketKey, PersistentDataType.INTEGER)) {
            return
        }
        var loc = event.clickedBlock!!.location.clone()
        loc.add(event.blockFace.direction.multiply(1))
        loc = loc.block.location.toCenterLocation()
        loc.y = loc.blockY.toDouble()
        val ent = event.player.world.spawnEntity(loc, EntityType.valueOf(bucket.itemMeta.persistentDataContainer.get(magikBucket.key, PersistentDataType.STRING)!!))
        ((ent as CraftEntity).handle).load((bucket.itemMeta.persistentDataContainer as CraftPersistentDataContainer).raw["entMeta"]!! as NBTTagCompound)
        ent.teleport(loc)
        ent.isPersistent = true
        event.player.persistentDataContainer.set(magikBucket.justBucketedKey, PersistentDataType.INTEGER, 1)
        event.player.inventory.setItem(event.hand!!, ItemStack(Material.BUCKET))
    }
    @EventHandler
    fun onBucketEntityEvent(event: PlayerInteractEntityEvent){
        val item = event.player.inventory.getItem(event.hand) ?: return
        if (item.type != Material.BUCKET) {
            return
        }
        if (event.rightClicked.type == EntityType.UNKNOWN) {
            return
        }
        if (event.rightClicked.isInvulnerable) {
            return
        }
        if (event.rightClicked.type == EntityType.PLAYER) {
            return
        }
        val list = magikBucket.whitelist.getList("bucketable-entities")
        if (list == null) {
            Bukkit.getServer().logger.severe("whitelist.yml missing or invalid!")
        }
        if (!list!!.contains(event.rightClicked.type.toString())) {
            return
        }
        if (!event.player.inventory.containsAtLeast(ItemStack(Material.BUCKET), 1)) {
            return
        }
        for (i in event.player.inventory.contents) {
            i ?: continue
            if (i.type != Material.BUCKET) {
                continue
            }
            i.amount -= 1
            break
        }
        event.player.inventory.addItem(magikBucket.giveBucket(event.rightClicked))
        event.rightClicked.remove()
        event.player.persistentDataContainer.set(magikBucket.bucketCooldownKey, PersistentDataType.INTEGER, 1)
        event.player.persistentDataContainer.set(magikBucket.justBucketedKey, PersistentDataType.INTEGER, 1)
        Bukkit.getServer().scheduler.runTaskLater(magikBucket, Runnable {
            event.player.persistentDataContainer.set(magikBucket.bucketCooldownKey, PersistentDataType.INTEGER, 0)
        }, 5L)
    }
    @EventHandler
    fun onPlayerDrinkEntityEvent(event: PlayerItemConsumeEvent) {
        if (!event.item.itemMeta.persistentDataContainer.has(magikBucket.isBucketKey, PersistentDataType.INTEGER)) {
            return
        }
        event.isCancelled = true

    }
}
package io.github.gabehowe.magikbucket

import net.minecraft.server.v1_16_R3.NBTTagCompound
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_16_R3.persistence.CraftPersistentDataContainer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Chicken
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.Paths

class MagikBucket : JavaPlugin() {
    val bucket = ItemStack(Material.MILK_BUCKET)
    val bucketMeta = bucket.itemMeta
    public lateinit var key: NamespacedKey
    lateinit var isBucketKey: NamespacedKey
    lateinit var justBucketedKey: NamespacedKey
    lateinit var bucketCooldownKey: NamespacedKey
    lateinit var metaKey: NamespacedKey
    val whiteListPath: File = Paths.get(dataFolder.path, "whitelist.yml").toFile()
    val whitelist = YamlConfiguration.loadConfiguration(whiteListPath)
    override fun onEnable() {
        if (!whiteListPath.exists()) {
            saveResource("whitelist.yml", false)
        }
        metaKey = NamespacedKey(this, "entityMeta")
        isBucketKey = NamespacedKey(this, "isBucket")
        key = NamespacedKey(this, "entityType")
        justBucketedKey = NamespacedKey(this, "justBucketed")
        bucketCooldownKey = NamespacedKey(this, "bucketCooldown")
        server.pluginManager.registerEvents(BucketEvents(this), this)

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    fun giveBucket(entity: Entity): ItemStack {
        val item = bucket.clone()
        item.itemMeta = bucketMeta
        val itemMeta = item.itemMeta
        itemMeta.addEnchant(Enchantment.DURABILITY, 0, true)
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        itemMeta.persistentDataContainer.set(key, PersistentDataType.STRING, "")
        val nmsEnt = (entity as CraftEntity).handle
        (itemMeta.persistentDataContainer as CraftPersistentDataContainer).put("entMeta", nmsEnt.save(NBTTagCompound()))
        itemMeta.persistentDataContainer.set(isBucketKey, PersistentDataType.INTEGER, 1)
        itemMeta.persistentDataContainer.set(key, PersistentDataType.STRING, entity.type.toString())
        itemMeta.setDisplayName("§r§bBucket of ${entity.name}")
        item.itemMeta = itemMeta
        return item

    }
}

package me.m4nst3in.m4plugins.pets.mounts;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import me.m4nst3in.m4plugins.pets.PetType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class MarePet extends MountPet {
    
    public MarePet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.MARE, variant);
    }

    @Override
    protected Entity spawnEntityType(Location location) {
        Entity entity = location.getWorld().spawnEntity(location, EntityType.HORSE);
        return entity;
    }

    @Override
    protected void customizeEntity() {
        if (entity instanceof Horse) {
            Horse horse = (Horse) entity;
            
            updateEntityName();
            
            horse.setAdult();
            horse.setAI(false);
            horse.setTamed(true);
            
            Player owner = plugin.getServer().getPlayer(ownerId);
            if (owner != null) {
                horse.setOwner(owner);
            }
            
            horse.setVariant(Horse.Variant.HORSE);
            horse.setColor(Horse.Color.WHITE);
            horse.setStyle(Horse.Style.NONE);
            
            if (horse.getAttribute(Attribute.MAX_HEALTH) != null) {
                horse.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                horse.setHealth(health);
            }
            
            if (horse.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                horse.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
            }
            
            ItemStack saddle = new ItemStack(Material.SADDLE);
            ItemMeta meta = saddle.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("Sela da " + petName);
                saddle.setItemMeta(meta);
            }
            horse.getInventory().setSaddle(saddle);
        }
    }

    @Override
    protected void performLevel5AbilityWhileMounted(Player player) {
        if (entity instanceof Horse && hasLevel5Ability()) {
            if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false));
            }
        }
    }
}
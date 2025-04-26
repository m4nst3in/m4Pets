package me.m4nst3in.m4plugins.pets.mounts;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class DonkeyPet extends MountPet {

    public DonkeyPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.DONKEY, variant);
    }

    @Override
    protected Entity spawnEntityType(Location location) {
        Entity entity = location.getWorld().spawnEntity(location, EntityType.DONKEY);
        return entity;
    }

    @Override
    protected void customizeEntity() {
        if (entity instanceof Donkey) {
            Donkey donkey = (Donkey) entity;
            
            // Usar o método da classe pai para definir o nome customizado
            updateEntityName();
            
            // Tornar o burro manso
            donkey.setTamed(true);
            donkey.setAdult();
            
            // Desativar a IA do burro
            donkey.setAI(false);
            
            // Adicionar sela automaticamente
            donkey.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            
            // Definir o dono do burro
            Player owner = plugin.getServer().getPlayer(ownerId);
            if (owner != null) {
                donkey.setOwner(owner);
            }
            
            // Definir atributos
            if (donkey.getAttribute(Attribute.MAX_HEALTH) != null) {
                donkey.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                donkey.setHealth(health);
            }
            
            if (donkey.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                donkey.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
            }
        }
    }

    @Override
    protected void performLevel5AbilityWhileMounted(Player player) {
        // Habilidade de nível 5: Resistência a dano
        if (entity instanceof Donkey && hasLevel5Ability()) {
            if (!player.hasPotionEffect(PotionEffectType.RESISTANCE)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0, false, false));
            }
        }
    }
}
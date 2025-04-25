package me.m4nst3in.m4plugins.pets.mounts;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class HorsePet extends MountPet {

    private Horse.Color horseColor;

    public HorsePet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.HORSE, variant);
        
        // Definir a cor do cavalo com base na variante
        try {
            this.horseColor = Horse.Color.valueOf(variant);
        } catch (IllegalArgumentException e) {
            this.horseColor = Horse.Color.WHITE; // Padrão
        }
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
            
            // Definir nome customizado
            horse.setCustomName(TextUtil.color("&a" + petName));
            horse.setCustomNameVisible(true);
            
            // Definir cor do cavalo
            horse.setColor(horseColor);
            
            // Remover sela por padrão
            horse.getInventory().setSaddle(null);
            
            // Tornar o cavalo manso
            horse.setTamed(true);
            horse.setAdult();
            
            // Definir atributos
            if (horse.getAttribute(Attribute.MAX_HEALTH) != null) {
                horse.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                horse.setHealth(health);
            }
            
            if (horse.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                horse.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
            }
            
            // Impedir que o cavalo seja montado por outros jogadores
            Player owner = plugin.getServer().getPlayer(ownerId);
            if (owner != null) {
                horse.setOwner(owner);
            }
        }
    }

    @Override
    protected void performLevel5AbilityWhileMounted(Player player) {
        // Habilidade de nível 5: Pulo aumentado
        if (entity instanceof Horse) {
            Horse horse = (Horse) entity;
            
            // Aplicar efeito de pulo aumentado temporariamente ao cavalo
            if (!horse.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
                horse.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 1, false, false));
            }
        }
    }
    
    @Override
    public void setVariant(String variant) {
        super.setVariant(variant);
        
        try {
            this.horseColor = Horse.Color.valueOf(variant);
        } catch (IllegalArgumentException e) {
            this.horseColor = Horse.Color.WHITE; // Padrão
        }
        
        // Atualizar cor do cavalo se estiver spawned
        if (spawned && entity instanceof Horse) {
            ((Horse) entity).setColor(horseColor);
        }
    }
}
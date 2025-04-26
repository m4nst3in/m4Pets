package me.m4nst3in.m4plugins.pets.mounts;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
            
            // Usar o método da classe pai para definir o nome customizado
            updateEntityName();
            
            // Definir cor do cavalo
            horse.setColor(horseColor);
            
            // Tornar o cavalo manso e adulto
            horse.setTamed(true);
            horse.setAdult();
            
            // Desativar a IA do cavalo
            horse.setAI(false);
            
            // Adicionar sela automaticamente
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            
            // Definir o dono do cavalo
            Player owner = plugin.getServer().getPlayer(ownerId);
            if (owner != null) {
                horse.setOwner(owner);
            }
            
            // Definir atributos
            if (horse.getAttribute(Attribute.MAX_HEALTH) != null) {
                horse.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                horse.setHealth(health);
            }
            
            if (horse.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                horse.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
            }
        }
    }

    @Override
    protected void performLevel5AbilityWhileMounted(Player player) {
        // Habilidade de nível 5: Pulo aumentado
        if (entity instanceof Horse && hasLevel5Ability()) {
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
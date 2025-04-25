package me.m4nst3in.m4plugins.pets.mounts;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class SheepPet extends MountPet {

    private DyeColor color;

    public SheepPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.SHEEP, variant);
        
        // Definir cor da ovelha com base na variante
        try {
            this.color = DyeColor.valueOf(variant);
        } catch (IllegalArgumentException e) {
            this.color = DyeColor.WHITE; // Padrão
        }
    }

    @Override
    protected Entity spawnEntityType(Location location) {
        Entity entity = location.getWorld().spawnEntity(location, EntityType.SHEEP);
        return entity;
    }

    @Override
    protected void customizeEntity() {
        if (entity instanceof Sheep) {
            Sheep sheep = (Sheep) entity;
            
            // Definir nome customizado
            sheep.setCustomName(TextUtil.color("&a" + petName));
            sheep.setCustomNameVisible(true);
            
            // Definir cor
            sheep.setColor(color);
            
            // Tornar adulta
            sheep.setAdult();
            sheep.setSheared(false); // Certificar-se de que a ovelha tem lã
            
            // Definir atributos
            if (sheep.getAttribute(Attribute.MAX_HEALTH) != null) {
                sheep.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                sheep.setHealth(health);
            }
            
            if (sheep.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                sheep.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
            }
        }
    }

    @Override
    protected void performLevel5AbilityWhileMounted(Player player) {
        // Habilidade de nível 5: Amortece quedas
        if (entity instanceof Sheep && hasLevel5Ability()) {
            // Dar efeito de queda lenta ao jogador
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW_FALLING, 
                40, // duração curta (2 segundos) para ser renovada constantemente
                0,  // nível de amplificação (I)
                false, // sem partículas
                false  // sem ícone de poção
            ));
        }
    }
    
    @Override
    public void setVariant(String variant) {
        super.setVariant(variant);
        
        try {
            this.color = DyeColor.valueOf(variant);
        } catch (IllegalArgumentException e) {
            this.color = DyeColor.WHITE; // Padrão
        }
        
        // Atualizar cor da ovelha se estiver spawned
        if (spawned && entity instanceof Sheep) {
            ((Sheep) entity).setColor(color);
        }
    }
}
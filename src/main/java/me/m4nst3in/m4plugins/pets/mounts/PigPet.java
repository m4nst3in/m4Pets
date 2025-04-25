package me.m4nst3in.m4plugins.pets.mounts;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PigPet extends MountPet {

    public PigPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.PIG, variant);
    }

    @Override
    protected Entity spawnEntityType(Location location) {
        Entity entity = location.getWorld().spawnEntity(location, EntityType.PIG);
        return entity;
    }

    @Override
    protected void customizeEntity() {
        if (entity instanceof Pig) {
            Pig pig = (Pig) entity;
            
            // Definir nome customizado
            pig.setCustomName(TextUtil.color("&a" + petName));
            pig.setCustomNameVisible(true);
            
            // Tornar o porco manso
            pig.setAdult();
            pig.setSaddle(true); // Porcos precisam de sela para serem montados
            
            // Definir atributos
            if (pig instanceof org.bukkit.entity.LivingEntity) {
                org.bukkit.entity.LivingEntity livingPig = (org.bukkit.entity.LivingEntity) pig;
                if (livingPig.getAttribute(Attribute.MAX_HEALTH) != null) {
                    livingPig.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                    livingPig.setHealth(health);
                }
                
                if (livingPig.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                    livingPig.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
                }
            }
        }
    }

    @Override
    protected void performLevel5AbilityWhileMounted(Player player) {
        // Habilidade de nível 5: Não sofre dano de queda quando montado
        if (entity instanceof Pig && hasLevel5Ability()) {
            player.setFallDistance(0f);
            
            // Dar resistência ao jogador temporariamente enquanto montado
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE, 
                40, // duração curta (2 segundos) para ser renovada constantemente
                1,  // nível de amplificação (II)
                false, // sem partículas
                false  // sem ícone de poção
            ));
        }
    }
}
package me.m4nst3in.m4plugins.pets.decoratives;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Pet decorativo: Coelho
 * Concede salto ao jogador quando estiver por perto.
 */
public class RabbitPet extends DecorativePet {
    private final double effectRadius;

    public RabbitPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.RABBIT, variant);
        this.effectRadius = getPetConfigSection().getDouble("proximity-radius", 5.0);
    }

    @Override
    protected Entity spawnEntityType(Location location) {
        return location.getWorld().spawnEntity(location, EntityType.RABBIT);
    }

    @Override
    protected void customizeEntity() {
        // Podemos customizar tipo de coelho se quisermos
        Rabbit rabbit = (Rabbit) getEntity();
        // variações: BROWN, WHITE, BLACK, BLACK_AND_WHITE, GOLD, etc.
        try {
            Rabbit.Type type = Rabbit.Type.valueOf(getVariant().toUpperCase());
            rabbit.setRabbitType(type);
        } catch (IllegalArgumentException e) {
            rabbit.setRabbitType(Rabbit.Type.BROWN);
        }
    }

    @Override
    public void applyProximityEffect() {
        Player owner = Bukkit.getPlayer(getOwnerId());
        if (owner != null && owner.isOnline() && isSpawned() && getEntity() != null && !getEntity().isDead()) {
            if (owner.getLocation().distance(getEntity().getLocation()) <= effectRadius) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 0, true, false));
            }
        }
    }
}

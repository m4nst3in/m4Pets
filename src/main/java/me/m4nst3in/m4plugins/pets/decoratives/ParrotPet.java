package me.m4nst3in.m4plugins.pets.decoratives;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Pet decorativo: Papagaio
 * Concede efeitos de velocidade ao jogador quando estiver por perto.
 */
public class ParrotPet extends DecorativePet {
    private final double effectRadius;

    public ParrotPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.PARROT, variant);
        this.effectRadius = getPetConfigSection().getDouble("proximity-radius", 5.0);
    }

    @Override
    protected Entity spawnEntityType(Location location) {
        return location.getWorld().spawnEntity(location, EntityType.PARROT);
    }

    @Override
    protected void customizeEntity() {
        Parrot parrot = (Parrot) getEntity();
        try {
            Parrot.Variant var = Parrot.Variant.valueOf(getVariant().toUpperCase());
            parrot.setVariant(var);
        } catch (IllegalArgumentException e) {
            parrot.setVariant(Parrot.Variant.RED);
        }
    }

    @Override
    public void applyProximityEffect() {
        Player owner = Bukkit.getPlayer(getOwnerId());
        if (owner != null && owner.isOnline() && isSpawned() && getEntity() != null && !getEntity().isDead()) {
            if (owner.getLocation().distance(getEntity().getLocation()) <= effectRadius) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, true, false));
            }
        }
    }
}

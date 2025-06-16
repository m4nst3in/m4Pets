package me.m4nst3in.m4plugins.pets.decoratives;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Pet decorativo: Gato
 * Concede sorte ao jogador quando estiver por perto.
 */
public class CatPet extends DecorativePet {
    private final double effectRadius;

    public CatPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.CAT, variant);
        this.effectRadius = getPetConfigSection().getDouble("proximity-radius", 5.0);
    }

    @Override
    protected Entity spawnEntityType(Location location) {
        return location.getWorld().spawnEntity(location, EntityType.CAT);
    }

    @Override
    protected void customizeEntity() {
        Cat cat = (Cat) getEntity();
        try {
            Cat.Type type = Cat.Type.valueOf(getVariant().toUpperCase());
            cat.setCatType(type);
        } catch (IllegalArgumentException e) {
            cat.setCatType(Cat.Type.TABBY);
        }
    }

    @Override
    public void applyProximityEffect() {
        Player owner = Bukkit.getPlayer(getOwnerId());
        if (owner != null && owner.isOnline() && isSpawned() && getEntity() != null && !getEntity().isDead()) {
            if (owner.getLocation().distance(getEntity().getLocation()) <= effectRadius) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 60, 0, true, false));
            }
        }
    }
}

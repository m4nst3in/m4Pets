package me.m4nst3in.m4plugins.pets.decoratives;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Pet decorativo: Axolote
 * Concede regeneração ao jogador quando estiver por perto.
 */
public class AxolotlPet extends DecorativePet {
    private final double effectRadius;

    public AxolotlPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.AXOLOTL, variant);
        this.effectRadius = getPetConfigSection().getDouble("proximity-radius", 5.0);
    }

    @Override
    protected Entity spawnEntityType(Location location) {
        return location.getWorld().spawnEntity(location, EntityType.AXOLOTL);
    }

    @Override
    protected void customizeEntity() {
        Axolotl ax = (Axolotl) getEntity();
        try {
            Axolotl.Variant var = Axolotl.Variant.valueOf(getVariant().toUpperCase());
            ax.setVariant(var);
        } catch (IllegalArgumentException e) {
            ax.setVariant(Axolotl.Variant.LUCY);
        }
    }

    @Override
    public void applyProximityEffect() {
        Player owner = Bukkit.getPlayer(getOwnerId());
        if (owner != null && owner.isOnline() && isSpawned() && getEntity() != null && !getEntity().isDead()) {
            if (owner.getLocation().distance(getEntity().getLocation()) <= effectRadius) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, false));
            }
        }
    }
}

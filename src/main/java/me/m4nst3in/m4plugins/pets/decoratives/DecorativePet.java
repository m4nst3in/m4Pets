package me.m4nst3in.m4plugins.pets.decoratives;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.pets.PetType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.UUID;

/**
 * Base class para pets decorativos que não são montáveis nem combativos.
 * Permite alternar IA e aplicar efeitos de proximidade.
 */
public abstract class DecorativePet extends AbstractPet {
    private boolean aiActive;

    public DecorativePet(M4Pets plugin, UUID ownerId, String petName, PetType type, String variant) {
        super(plugin, ownerId, petName, type, variant);
        this.aiActive = true;
    }

    @Override
    public boolean spawn(Player player) {
        boolean ok = super.spawn(player);
        if (ok && getEntity() instanceof LivingEntity) {
            ((LivingEntity) getEntity()).setAI(aiActive);
        }
        return ok;
    }

    /**
     * Alterna a IA do pet ligado/desligado em runtime.
     */
    public void toggleAI() {
        aiActive = !aiActive;
        if (spawned && getEntity() instanceof LivingEntity) {
            ((LivingEntity) getEntity()).setAI(aiActive);
        }
    }

    public boolean isAIActive() {
        return aiActive;
    }

    /**
     * Aplica efeitos de proximidade ao dono. Chamado periodicamente.
     */
    public void applyProximityEffect() {
        // Implementado nas subclasses
    }
}

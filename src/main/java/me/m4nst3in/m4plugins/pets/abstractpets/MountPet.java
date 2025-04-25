package me.m4nst3in.m4plugins.pets.abstractpets;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import eu.decentsoftware.holograms.api.DHAPI;

import java.util.Map;
import java.util.UUID;

public abstract class MountPet extends AbstractPet {
    
    protected double speed;
    protected boolean isMounted;
    
    public MountPet(M4Pets plugin, UUID ownerId, String petName, PetType type, String variant) {
        super(plugin, ownerId, petName, type, variant);
        
        // Inicializar velocidade com valor base da configuração
        ConfigurationSection petConfig = getPetConfigSection();
        if (petConfig != null) {
            this.speed = petConfig.getDouble("base-speed", 0.2);
        } else {
            this.speed = 0.2;
        }
        
        this.isMounted = false;
    }
    
    @Override
    protected void updateAttributes() {
        super.updateAttributes();
        
        ConfigurationSection petConfig = getPetConfigSection();
        if (petConfig == null) return;
        
        // Obter valores base
        double baseSpeed = petConfig.getDouble("base-speed", 0.2);
        double speedIncrease = petConfig.getDouble("speed-increase-per-level", 0.05);
        
        // Calcular nova velocidade baseada no nível
        this.speed = baseSpeed + (speedIncrease * (level - 1));
        
        // Atualizar atributo de velocidade da entidade se estiver spawned
        if (spawned && entity != null && entity instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) entity;
            if (livingEntity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                livingEntity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
            }
        }
    }
    
    /**
     * Montar o pet
     * @param player Jogador que vai montar o pet
     * @return true se conseguiu montar
     */
    public boolean mount(Player player) {
        if (!spawned || entity == null || entity.isDead() || dead) return false;
        
        // Tentar montar o pet
        entity.addPassenger(player);
        
        // Verificar se montou com sucesso
        if (entity.getPassengers().contains(player)) {
            isMounted = true;
            
            // Esconder o holograma temporariamente
            if (hologram != null && plugin.isDecentHologramsEnabled()) {
                // Em vez de usar setVisible, vamos remover e recriar depois
                DHAPI.removeHologram(hologram.getName());
                hologram = null;
            }
            
            // Iniciar monitoramento de montaria
            startMountMonitoring(player);
            return true;
        }
        
        return false;
    }
    
    /**
     * Desmontar do pet
     * @param player Jogador que vai desmontar
     */
    public void dismount(Player player) {
        if (!spawned || entity == null || !isMounted) return;
        
        entity.removePassenger(player);
        isMounted = false;
        
        // Mostrar o holograma novamente
        if (plugin.isDecentHologramsEnabled() && hologram == null) {
            createHologram();
        }
    }
    
    /**
     * Iniciar monitoramento da montaria
     * @param player Jogador montado no pet
     */
    private void startMountMonitoring(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Verificar se o pet ainda existe e está spawned
                if (entity == null || entity.isDead() || !spawned) {
                    isMounted = false;
                    cancel();
                    return;
                }
                
                // Verificar se o jogador ainda está montado
                if (!entity.getPassengers().contains(player)) {
                    isMounted = false;
                    
                    // Recriar o holograma novamente
                    if (plugin.isDecentHologramsEnabled() && hologram == null) {
                        createHologram();
                    }
                    
                    cancel();
                    return;
                }
                
                // Executar habilidades especiais enquanto montado
                if (hasLevel5Ability()) {
                    performLevel5AbilityWhileMounted(player);
                }
            }
        }.runTaskTimer(plugin, 1L, 5L); // Verificar a cada 5 ticks (1/4 de segundo)
    }
    
    /**
     * Executa a habilidade especial de nível 5 enquanto montado
     * Implementado diferentemente para cada tipo de pet de montaria
     * @param player Jogador montado no pet
     */
    protected abstract void performLevel5AbilityWhileMounted(Player player);
    
    @Override
    public void despawn() {
        // Desmontar jogadores antes de despawnar
        if (spawned && entity != null && !entity.getPassengers().isEmpty()) {
            entity.getPassengers().forEach(entity::removePassenger);
            isMounted = false;
        }
        
        super.despawn();
    }
    
    // Getters específicos
    
    public double getSpeed() {
        return speed;
    }
    
    public boolean isMounted() {
        return isMounted;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = super.serialize();
        data.put("speed", speed);
        return data;
    }
}
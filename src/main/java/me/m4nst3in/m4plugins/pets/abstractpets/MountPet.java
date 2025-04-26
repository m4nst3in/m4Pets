package me.m4nst3in.m4plugins.pets.abstractpets;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public abstract class MountPet extends AbstractPet {
    
    protected double speed;
    protected boolean isMounted;
    protected UUID riderId;
    
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
        this.riderId = null;
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
        
        // Verificar se já está montado
        if (isMounted) {
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.pet-already-mounted")));
            return false;
        }
        
        // Verificar se é o dono
        if (!player.getUniqueId().equals(ownerId)) {
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.not-owner")));
            return false;
        }
        
        // Tentar montar o pet
        if (!entity.getPassengers().contains(player)) {
            entity.addPassenger(player);
        }
        
        // O VehicleEnterEvent será disparado e o listener atualizará o estado
        // através da chamada ao registerMountedState
        
        return true;
    }
    
    /**
     * Registra o estado de montaria sem tentar montar novamente
     * Este método deve ser chamado APENAS pelo listener de VehicleEnterEvent
     * @param player Jogador que montou o pet
     */
    public void registerMountedState(Player player) {
        if (player.getUniqueId().equals(ownerId) && entity != null && entity.getPassengers().contains(player)) {
            this.isMounted = true;
            this.riderId = player.getUniqueId();
            
            // Importante: Não desativar a IA, apenas configurar para ser controlável
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                ensureMountable(); // Garantir que a entidade seja controlável
            }
            
            startMountMonitoring(player);
        }
    }
    
    /**
     * Desmontar do pet
     * @param player Jogador que vai desmontar
     */
    public void dismount(Player player) {
        if (!spawned || entity == null || !isMounted) return;
        
        entity.removePassenger(player);
        isMounted = false;
        riderId = null;
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
                    riderId = null;
                    cancel();
                    return;
                }
                
                // Verificar se o jogador ainda está montado
                if (!entity.getPassengers().contains(player)) {
                    isMounted = false;
                    riderId = null;
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
            riderId = null;
        }
        
        super.despawn();
    }
    
    /**
     * Garante que o pet seja domável e tenha sela se necessário
     */
    protected void ensureMountable() {
        // Configuração básica para entidades montáveis
        if (entity instanceof org.bukkit.entity.Horse) {
            org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) entity;
            horse.setTamed(true);
            horse.setOwner(plugin.getServer().getPlayer(ownerId));
        } else if (entity instanceof org.bukkit.entity.Pig) {
            org.bukkit.entity.Pig pig = (org.bukkit.entity.Pig) entity;
            pig.setSaddle(true);
        } else if (entity instanceof org.bukkit.entity.Strider) {
            org.bukkit.entity.Strider strider = (org.bukkit.entity.Strider) entity;
            strider.setSaddle(true);
        } else if (entity instanceof org.bukkit.entity.Cow) {
            org.bukkit.entity.Cow cow = (org.bukkit.entity.Cow) entity;
            cow.setAdult();
        } else if (entity instanceof org.bukkit.entity.Sniffer) {
            org.bukkit.entity.Sniffer sniffer = (org.bukkit.entity.Sniffer) entity;
            sniffer.setAdult();
        }
        
        // Desativar IA para permitir controle total pelo jogador
        if (entity instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) entity;
            livingEntity.setAI(false);
        }
    }
    
    // Getters específicos
    
    public double getSpeed() {
        return speed;
    }
    
    public boolean isMounted() {
        return isMounted;
    }
    
    public UUID getRiderId() {
        return riderId;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = super.serialize();
        data.put("speed", speed);
        return data;
    }
}
package me.m4nst3in.m4plugins.pets.abstractpets;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;

/**
 * Classe abstrata para pets guerreiros
 * Pets que podem atacar e defender o jogador
 */
public abstract class WarriorPet extends AbstractPet {
    
    protected double attackDamage;
    protected double attackSpeed;
    protected double defenseRadius;
    protected double attackRadius;
    protected boolean aiEnabled;
    protected LivingEntity currentTarget;
    protected String targetPlayerName;
    protected EntityType targetMobType;
    protected BukkitTask combatTask;
    protected BukkitTask targetScanTask;
    
    public WarriorPet(M4Pets plugin, UUID ownerId, String petName, PetType type, String variant) {
        super(plugin, ownerId, petName, type, variant);
        
        // Inicializar atributos de combate com valores base da configuração
        ConfigurationSection petConfig = getPetConfigSection();
        if (petConfig != null) {
            this.attackDamage = petConfig.getDouble("base-attack", 4.0);
            this.attackSpeed = petConfig.getDouble("attack-speed", 1.0);
            this.defenseRadius = petConfig.getDouble("defense-radius", 10.0);
            this.attackRadius = petConfig.getDouble("attack-radius", 30.0);
        } else {
            this.attackDamage = 4.0;
            this.attackSpeed = 1.0;
            this.defenseRadius = 10.0;
            this.attackRadius = 30.0;
        }
        
        this.aiEnabled = true;
        this.currentTarget = null;
        this.targetPlayerName = null;
        this.targetMobType = null;
    }
    
    @Override
    public boolean spawn(Player player) {
        boolean success = super.spawn(player);
        if (success && aiEnabled) {
            startCombatAI();
        }
        return success;
    }
    
    @Override
    public void despawn() {
        stopCombatAI();
        super.despawn();
    }
    
    @Override
    protected void updateAttributes() {
        super.updateAttributes();
        
        ConfigurationSection petConfig = getPetConfigSection();
        if (petConfig == null) return;
        
        // Obter valores base
        double baseAttack = petConfig.getDouble("base-attack", 4.0);
        double attackIncrease = petConfig.getDouble("attack-increase-per-level", 1.0);
        double baseSpeed = petConfig.getDouble("attack-speed", 1.0);
        double speedIncrease = petConfig.getDouble("speed-increase-per-level", 0.1);
        
        // Calcular novos valores baseados no nível
        this.attackDamage = baseAttack + (attackIncrease * (level - 1));
        this.attackSpeed = baseSpeed + (speedIncrease * (level - 1));
        
        // Atualizar atributos da entidade se estiver spawned
        if (spawned && entity != null && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            
            if (livingEntity.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
                livingEntity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(attackDamage);
            }
            
            if (livingEntity.getAttribute(Attribute.ATTACK_SPEED) != null) {
                livingEntity.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(attackSpeed);
            }
        }
    }
    
    /**
     * Ativa/desativa a IA de combate do pet
     * @param enabled true para ativar, false para desativar
     */
    public void setAIEnabled(boolean enabled) {
        this.aiEnabled = enabled;
        
        if (spawned && entity != null && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.setAI(enabled);
            
            if (enabled) {
                startCombatAI();
            } else {
                stopCombatAI();
                currentTarget = null;
            }
        }
    }
    
    /**
     * Define um jogador como alvo do pet
     * @param targetName Nome do jogador alvo (null para limpar)
     */
    public void setTargetPlayer(String targetName) {
        this.targetPlayerName = targetName;
        this.targetMobType = null; // Limpar alvo de mob quando definir jogador
        
        // Se está tentando atacar o próprio dono, cancelar
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null && owner.getName().equalsIgnoreCase(targetName)) {
            this.targetPlayerName = null;
            if (owner.isOnline()) {
                owner.sendMessage(plugin.formatMessage("&cSeu pet não pode atacar você mesmo!"));
            }
        }
    }
    
    /**
     * Define um tipo de mob como alvo do pet
     * @param mobType Tipo de mob alvo (null para limpar)
     */
    public void setTargetMobType(EntityType mobType) {
        this.targetMobType = mobType;
        this.targetPlayerName = null; // Limpar alvo de jogador quando definir mob
    }
    
    /**
     * Inicia a IA de combate do pet
     */
    protected void startCombatAI() {
        if (!aiEnabled || !spawned || entity == null) return;
        
        stopCombatAI(); // Parar tarefas anteriores
        
        // Task para escanear alvos
        targetScanTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!spawned || entity == null || entity.isDead()) {
                stopCombatAI();
                return;
            }
            
            scanForTargets();
            
            // Verificação extra de segurança - cancelar se tentando atacar o dono
            if (entity instanceof org.bukkit.entity.Mob) {
                org.bukkit.entity.Mob mob = (org.bukkit.entity.Mob) entity;
                org.bukkit.entity.LivingEntity target = mob.getTarget();
                if (target instanceof Player) {
                    Player owner = Bukkit.getPlayer(ownerId);
                    if (owner != null && target.getUniqueId().equals(owner.getUniqueId())) {
                        mob.setTarget(null);
                        currentTarget = null;
                    }
                }
            }
        }, 0L, 20L); // A cada segundo
        
        // Task para atacar
        combatTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!spawned || entity == null || entity.isDead()) {
                stopCombatAI();
                return;
            }
            
            performCombatAction();
        }, 0L, (long) (20 / attackSpeed)); // Baseado na velocidade de ataque
    }
    
    /**
     * Para a IA de combate do pet
     */
    protected void stopCombatAI() {
        if (targetScanTask != null) {
            targetScanTask.cancel();
            targetScanTask = null;
        }
        
        if (combatTask != null) {
            combatTask.cancel();
            combatTask = null;
        }
    }
    
    /**
     * Escaneia por alvos na área
     */
    protected void scanForTargets() {
        if (!(entity instanceof LivingEntity)) return;
        
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner == null || !owner.isOnline()) return;
        
        // Primeiro, verificar se há um jogador alvo específico
        if (targetPlayerName != null) {
            Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
            if (targetPlayer != null && targetPlayer.isOnline() && 
                targetPlayer.getLocation().distance(entity.getLocation()) <= attackRadius) {
                
                currentTarget = targetPlayer;
                return;
            }
        }
        
        // Se há um tipo de mob específico definido, buscar apenas esse tipo
        if (targetMobType != null) {
            LivingEntity closestMob = null;
            double closestDistance = Double.MAX_VALUE;
            
            for (Entity nearbyEntity : owner.getNearbyEntities(attackRadius, attackRadius, attackRadius)) {
                if (nearbyEntity.getType() == targetMobType && nearbyEntity instanceof LivingEntity) {
                    LivingEntity mob = (LivingEntity) nearbyEntity;
                    double distance = mob.getLocation().distance(entity.getLocation());
                    
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestMob = mob;
                    }
                }
            }
            
            currentTarget = closestMob;
            return;
        }
        
        // Se não há alvo específico, defender o dono contra hostis
        LivingEntity closestThreat = null;
        double closestDistance = Double.MAX_VALUE;
        
        // Buscar por entidades hostis próximas ao dono
        for (Entity nearbyEntity : owner.getNearbyEntities(defenseRadius, defenseRadius, defenseRadius)) {
            if (nearbyEntity instanceof LivingEntity && isHostileEntity(nearbyEntity)) {
                LivingEntity livingTarget = (LivingEntity) nearbyEntity;
                double distance = livingTarget.getLocation().distance(owner.getLocation());
                
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestThreat = livingTarget;
                }
            }
        }
        
        currentTarget = closestThreat;
    }
    
    /**
     * Verifica se uma entidade é hostil
     */
    protected boolean isHostileEntity(Entity entity) {
        // Nunca atacar o próprio dono
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null && entity.equals(owner)) {
            return false;
        }
        
        // Verificar se é um pet (nunca atacar outros pets)
        for (AbstractPet pet : plugin.getPetManager().getAllActivePets()) {
            if (pet.getEntity() != null && pet.getEntity().equals(entity)) {
                return false; // Nunca atacar outros pets
            }
        }
        
        if (entity instanceof Player) {
            Player player = (Player) entity;
            // Nunca atacar o dono, mesmo que o UUID seja diferente por algum motivo
            if (owner != null && (player.getUniqueId().equals(owner.getUniqueId()) || 
                                  player.getName().equalsIgnoreCase(owner.getName()))) {
                return false;
            }
            return true; // Outros jogadores podem ser atacados se definidos como alvo
        }
        
        return entity instanceof Monster || 
               entity instanceof Slime || 
               entity instanceof Phantom ||
               entity instanceof Shulker;
    }
    
    /**
     * Executa ação de combate
     */
    protected void performCombatAction() {
        if (!(entity instanceof LivingEntity) || currentTarget == null || currentTarget.isDead()) {
            currentTarget = null;
            return;
        }
        
        // VERIFICAÇÃO CRÍTICA: nunca atacar o dono
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null) {
            // Múltiplas verificações para garantir que não ataque o dono
            if (currentTarget.equals(owner) || 
                currentTarget.getUniqueId().equals(owner.getUniqueId()) ||
                (currentTarget instanceof Player && ((Player) currentTarget).getName().equalsIgnoreCase(owner.getName()))) {
                
                // Se tentou atacar o dono, cancelar completamente
                currentTarget = null;
                if (entity instanceof org.bukkit.entity.Mob) {
                    ((org.bukkit.entity.Mob) entity).setTarget(null);
                }
                return;
            }
        }
        
        LivingEntity livingEntity = (LivingEntity) entity;
        double distance = livingEntity.getLocation().distance(currentTarget.getLocation());
        
        // Se o alvo está muito longe, mover em direção a ele
        if (distance > 2.0) {
            if (livingEntity instanceof Mob) {
                ((Mob) livingEntity).setTarget(currentTarget);
            }
        } else {
            // Atacar o alvo
            performAttack(currentTarget);
        }
    }
    
    /**
     * Executa ataque ao alvo
     * Implementado diferentemente para cada tipo de pet guerreiro
     * @param target Alvo do ataque
     */
    protected abstract void performAttack(LivingEntity target);
    
    /**
     * Executa habilidade especial de nível 5
     * Implementado diferentemente para cada tipo de pet guerreiro
     */
    protected abstract void performLevel5Ability();
    
    /**
     * Executa habilidade especial de nível 5 (método público para GUI)
     */
    public void useLevel5Ability() {
        if (hasLevel5Ability()) {
            performLevel5Ability();
        }
    }
    
    // Getters específicos para pets guerreiros
    public double getAttackDamage() {
        return attackDamage;
    }
    
    public double getAttackSpeed() {
        return attackSpeed;
    }
    
    public double getDefenseRadius() {
        return defenseRadius;
    }
    
    public double getAttackRadius() {
        return attackRadius;
    }
    
    public boolean isAIEnabled() {
        return aiEnabled;
    }
    
    public LivingEntity getCurrentTarget() {
        return currentTarget;
    }
    
    public String getTargetPlayerName() {
        return targetPlayerName;
    }
    
    public EntityType getTargetMobType() {
        return targetMobType;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = super.serialize();
        data.put("attackDamage", attackDamage);
        data.put("attackSpeed", attackSpeed);
        data.put("aiEnabled", aiEnabled);
        if (targetPlayerName != null) {
            data.put("targetPlayer", targetPlayerName);
        }
        if (targetMobType != null) {
            data.put("targetMobType", targetMobType.name());
        }
        return data;
    }
}

package me.m4nst3in.m4plugins.pets.warriors;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.pets.abstractpets.WarriorPet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Pet Guerreiro Vindicator
 * Características: Guerreiro agressivo de combate corpo a corpo com alta mobilidade
 */
public class VindicatorPet extends WarriorPet {
    
    private long lastChargeTime = 0;
    private static final long CHARGE_COOLDOWN = 8000; // 8 segundos
    
    public VindicatorPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.valueOf("VINDICATOR"), variant);
    }
    
    @Override
    protected Entity spawnEntityType(Location location) {
        return location.getWorld().spawnEntity(location, EntityType.VINDICATOR);
    }
    
    @Override
    protected void customizeEntity() {
        if (entity instanceof Vindicator) {
            Vindicator vindicator = (Vindicator) entity;
            
            // Usar o método da classe pai para definir o nome customizado
            updateEntityName();
            
            // Configurar comportamento
            vindicator.setCanJoinRaid(false);
            vindicator.setPatrolLeader(false);
            
            // Equipar com machado de ferro ou diamante baseado na variante
            Material axeMaterial = "diamond".equals(variant) ? Material.DIAMOND_AXE : Material.IRON_AXE;
            vindicator.getEquipment().setItemInMainHand(new ItemStack(axeMaterial));
            vindicator.getEquipment().setItemInMainHandDropChance(0.0f);
            
            // Configurar atributos baseados no nível
            if (vindicator.getAttribute(Attribute.MAX_HEALTH) != null) {
                vindicator.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                vindicator.setHealth(health);
            }
            
            if (vindicator.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
                vindicator.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(attackDamage);
            }
            
            if (vindicator.getAttribute(Attribute.ATTACK_SPEED) != null) {
                vindicator.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(attackSpeed);
            }
            
            if (vindicator.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                vindicator.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.32); // Mais rápido que outros pets
            }
            
            // Definir como não agressivo por padrão (controlado pela IA)
            vindicator.setTarget(null);
        }
    }
    
    @Override
    protected void performAttack(LivingEntity target) {
        if (!(entity instanceof Vindicator)) return;
        
        Vindicator vindicator = (Vindicator) entity;
        
        // Verificar se o alvo é válido e não é um pet
        if (target.isDead() || target.equals(vindicator)) return;
        
        // Verificação de segurança adicional: nunca atacar o dono
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null && target.equals(owner)) {
            return;
        }
        
        // Verificar se o alvo não é outro pet
        for (AbstractPet pet : plugin.getPetManager().getAllActivePets()) {
            if (pet.getEntity() != null && pet.getEntity().equals(target)) {
                return; // Nunca atacar outros pets
            }
        }
        
        double distance = vindicator.getLocation().distance(target.getLocation());
        
        // Se está distante e o charge está disponível, usar charge attack
        if (distance > 5.0 && canUseCharge()) {
            performChargeAttack(target);
        } else {
            // Ataque corpo a corpo normal
            performMeleeAttack(target);
        }
        
        // Habilidade especial de nível 5
        if (hasLevel5Ability() && Math.random() < 0.18) {
            performLevel5Ability();
        }
    }
    
    /**
     * Executa ataque corpo a corpo normal
     */
    private void performMeleeAttack(LivingEntity target) {
        if (!(entity instanceof Vindicator)) return;
        
        // Ataque básico com dano aumentado - usar damage simples sem especificar o atacante para evitar loops
        target.damage(attackDamage);
        
        // Efeito visual e sonoro
        target.getWorld().spawnParticle(Particle.CRIT, 
            target.getLocation().add(0, 1, 0), 
            8, 0.5, 0.5, 0.5, 0.1);
        
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.1f);
        
        // Chance de aplicar efeito de atordoamento (stun simulado com lentidão + cegueira)
        if (Math.random() < 0.25) {
            if (target instanceof LivingEntity) {
                ((LivingEntity) target).addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOWNESS, 40, 2, false, false));
                ((LivingEntity) target).addPotionEffect(
                    new PotionEffect(PotionEffectType.BLINDNESS, 20, 0, false, false));
            }
        }
    }
    
    /**
     * Executa ataque de investida
     */
    private void performChargeAttack(LivingEntity target) {
        if (!(entity instanceof Vindicator)) return;
        
        Vindicator vindicator = (Vindicator) entity;
        lastChargeTime = System.currentTimeMillis();
        
        // Calcular direção da investida
        Vector direction = target.getLocation().subtract(vindicator.getLocation()).toVector().normalize();
        direction.multiply(1.5).setY(0.3); // Dar um impulso também vertical
        
        // Aplicar velocidade de investida
        vindicator.setVelocity(direction);
        
        // Efeito visual da investida
        Location start = vindicator.getLocation();
        vindicator.getWorld().spawnParticle(Particle.CLOUD, start, 10, 0.5, 0.5, 0.5, 0.1);
        vindicator.getWorld().playSound(start, Sound.ENTITY_RAVAGER_ROAR, 1.0f, 1.5f);
        
        // Dar efeito de força temporário para o ataque
        vindicator.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1, false, false));
        
        // Notificar o dono
        Player owner = plugin.getServer().getPlayer(ownerId);
        if (owner != null && owner.isOnline()) {
            owner.sendMessage(plugin.formatMessage("&c" + petName + " &eusou &cInvestida Brutal&e!"));
        }
    }
    
    @Override
    protected void performLevel5Ability() {
        if (!(entity instanceof Vindicator)) return;
        
        Vindicator vindicator = (Vindicator) entity;
        
        // "Fúria Berserker" - Aumenta drasticamente velocidade, ataque e resistência
        vindicator.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2, false, false));
        vindicator.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2, false, false));
        vindicator.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1, false, false));
        vindicator.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, false));
        
        // Durante a fúria, ataques têm chance de causar knockback
        performBerserkerRampage();
        
        // Efeito visual especial
        Location loc = vindicator.getLocation().add(0, 1, 0);
        vindicator.getWorld().spawnParticle(Particle.FLAME, loc, 25, 1, 1, 1, 0.1);
        vindicator.getWorld().spawnParticle(Particle.LAVA, loc, 10, 0.5, 0.5, 0.5, 0.1);
        vindicator.getWorld().playSound(loc, Sound.ENTITY_VINDICATOR_CELEBRATE, 1.0f, 0.8f);
        
        // Notificar o dono
        Player owner = plugin.getServer().getPlayer(ownerId);
        if (owner != null && owner.isOnline()) {
            owner.sendMessage(plugin.formatMessage("&4" + petName + " &eentrou em &4Fúria Berserker&e!"));
        }
    }
    
    /**
     * Executa rampage berserker - ataque em área
     */
    private void performBerserkerRampage() {
        if (!(entity instanceof Vindicator)) return;
        
        Vindicator vindicator = (Vindicator) entity;
        Location center = vindicator.getLocation();
        
        // Atacar todas as entidades hostis em um raio de 5 blocos
        for (Entity nearby : vindicator.getNearbyEntities(5, 5, 5)) {
            if (nearby instanceof LivingEntity && isHostileEntity(nearby)) {
                LivingEntity target = (LivingEntity) nearby;
                target.damage(attackDamage * 2.0, vindicator);
                
                // Knockback forte
                Vector knockback = target.getLocation().subtract(center).toVector().normalize().multiply(1.2);
                knockback.setY(0.5);
                target.setVelocity(knockback);
                
                // Efeito visual por alvo atingido
                target.getWorld().spawnParticle(Particle.EXPLOSION, 
                    target.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.1);
            }
        }
    }
    
    /**
     * Verifica se pode usar investida
     */
    private boolean canUseCharge() {
        return System.currentTimeMillis() - lastChargeTime >= CHARGE_COOLDOWN;
    }
    
    /**
     * Força o uso da investida (para comandos administrativos)
     */
    public void forceCharge() {
        if (currentTarget != null) {
            performChargeAttack(currentTarget);
        }
    }
    
    /**
     * Ativa manualmente a fúria berserker
     */
    public void activateBerserkerFury() {
        if (hasLevel5Ability()) {
            performLevel5Ability();
        }
    }
    
    @Override
    public String getLevel5AbilityDescription() {
        return "Fúria Berserker: Aumenta dramaticamente poder de combate e causa ataques em área";
    }
    
    /**
     * Retorna o tempo restante para próxima investida
     */
    public long getChargeTimeRemaining() {
        long elapsed = System.currentTimeMillis() - lastChargeTime;
        return Math.max(0, CHARGE_COOLDOWN - elapsed);
    }
}

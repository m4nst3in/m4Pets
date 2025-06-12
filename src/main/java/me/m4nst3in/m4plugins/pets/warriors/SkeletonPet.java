package me.m4nst3in.m4plugins.pets.warriors;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.pets.abstractpets.WarriorPet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Pet Guerreiro Esqueleto
 * Características: Arqueiro de longa distância com espada para combate próximo
 */
public class SkeletonPet extends WarriorPet {
    
    private boolean isRangedMode = true;
    
    public SkeletonPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.SKELETON, variant);
    }
    
    @Override
    protected Entity spawnEntityType(Location location) {
        return location.getWorld().spawnEntity(location, EntityType.SKELETON);
    }
    
    @Override
    protected void customizeEntity() {
        if (entity instanceof Skeleton) {
            Skeleton skeleton = (Skeleton) entity;
            
            // Usar o método da classe pai para definir o nome customizado
            updateEntityName();
            
            // Configurar equipamentos baseado na variante
            if ("sword".equals(variant)) {
                // Equipar com espada de ferro para combate corpo a corpo
                skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                skeleton.getEquipment().setItemInMainHandDropChance(0.0f);
                isRangedMode = false;
            } else {
                // Equipar com arco para combate à distância
                skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
                skeleton.getEquipment().setItemInMainHandDropChance(0.0f);
                isRangedMode = true;
            }
            
            // Equipar armadura leve
            skeleton.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
            skeleton.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            skeleton.getEquipment().setHelmetDropChance(0.0f);
            skeleton.getEquipment().setChestplateDropChance(0.0f);
            
            // Configurar atributos baseados no nível
            if (skeleton.getAttribute(Attribute.MAX_HEALTH) != null) {
                skeleton.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                skeleton.setHealth(health);
            }
            
            if (skeleton.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
                skeleton.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(attackDamage);
            }
            
            if (skeleton.getAttribute(Attribute.ATTACK_SPEED) != null) {
                skeleton.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(attackSpeed);
            }
            
            if (skeleton.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                skeleton.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.27);
            }
            
            // Definir como não agressivo por padrão (controlado pela IA)
            skeleton.setTarget(null);
        }
    }
    
    @Override
    protected void performAttack(LivingEntity target) {
        if (!(entity instanceof Skeleton)) return;
        
        Skeleton skeleton = (Skeleton) entity;
        
        // Verificar se o alvo é válido e não é um pet
        if (target.isDead() || target.equals(skeleton)) return;
        
        // Verificar se o alvo não é outro pet
        for (AbstractPet pet : plugin.getPetManager().getAllActivePets()) {
            if (pet.getEntity() != null && pet.getEntity().equals(target)) {
                return; // Nunca atacar outros pets
            }
        }
        
        double distance = skeleton.getLocation().distance(target.getLocation());
        
        if (isRangedMode && distance > 3.0) {
            // Ataque à distância com arco
            performRangedAttack(target);
        } else {
            // Ataque corpo a corpo
            performMeleeAttack(target);
        }
        
        // Habilidade especial de nível 5
        if (hasLevel5Ability() && Math.random() < 0.12) {
            performLevel5Ability();
        }
    }
    
    /**
     * Executa ataque à distância
     */
    private void performRangedAttack(LivingEntity target) {
        if (!(entity instanceof Skeleton)) return;
        
        Skeleton skeleton = (Skeleton) entity;
        
        // Criar e disparar flecha
        Location eyeLocation = skeleton.getEyeLocation();
        Vector direction = target.getEyeLocation().subtract(eyeLocation).toVector().normalize();
        
        Arrow arrow = skeleton.getWorld().spawnArrow(eyeLocation, direction, 1.5f, 2.0f);
        arrow.setShooter((ProjectileSource) skeleton);
        arrow.setDamage(attackDamage * 0.8); // Dano um pouco menor para ataques à distância
        arrow.setCritical(Math.random() < 0.15); // 15% de chance de crítico
        
        // Efeito sonoro
        skeleton.getWorld().playSound(skeleton.getLocation(), Sound.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f);
        
        // Partículas na ponta da flecha
        arrow.getWorld().spawnParticle(Particle.CRIT, arrow.getLocation(), 3, 0.1, 0.1, 0.1, 0.01);
    }
    
    /**
     * Executa ataque corpo a corpo
     */
    private void performMeleeAttack(LivingEntity target) {
        if (!(entity instanceof Skeleton)) return;
        
        Skeleton skeleton = (Skeleton) entity;
        
        // Ataque básico - usar damage simples sem especificar o atacante para evitar loops
        target.damage(attackDamage);
        
        // Efeito visual e sonoro
        target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, 
            target.getLocation().add(0, 1, 0), 
            1, 0.5, 0.5, 0.5, 0.1);
        
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);
        
        // Chance de aplicar efeito de fraqueza no alvo (15% de chance)
        if (Math.random() < 0.15) {
            if (target instanceof LivingEntity) {
                ((LivingEntity) target).addPotionEffect(
                    new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false));
            }
        }
    }
    
    @Override
    protected void performLevel5Ability() {
        if (!(entity instanceof Skeleton)) return;
        
        Skeleton skeleton = (Skeleton) entity;
        
        // "Rajada Mortal" - Disparo múltiplo ou ataque giratório
        if (isRangedMode) {
            // Modo arco: Dispara 5 flechas em direções ligeiramente diferentes
            performMultiArrowAttack();
        } else {
            // Modo espada: Ataque em área ao redor
            performWhirlwindAttack();
        }
        
        // Efeito visual especial
        Location loc = skeleton.getLocation().add(0, 1, 0);
        skeleton.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 30, 2, 2, 2, 0.1);
        skeleton.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 0.8f, 1.5f);
        
        // Notificar o dono
        Player owner = plugin.getServer().getPlayer(ownerId);
        if (owner != null && owner.isOnline()) {
            String abilityName = isRangedMode ? "Rajada Mortal" : "Ataque Giratório";
            owner.sendMessage(plugin.formatMessage("&6" + petName + " &eativou sua habilidade &6" + abilityName + "&e!"));
        }
    }
    
    /**
     * Executa ataque de múltiplas flechas
     */
    private void performMultiArrowAttack() {
        if (!(entity instanceof Skeleton) || currentTarget == null) return;
        
        Skeleton skeleton = (Skeleton) entity;
        Location eyeLocation = skeleton.getEyeLocation();
        Vector baseDirection = currentTarget.getEyeLocation().subtract(eyeLocation).toVector().normalize();
        
        // Disparar 5 flechas com pequenas variações de direção
        for (int i = 0; i < 5; i++) {
            Vector direction = baseDirection.clone();
            direction.add(new Vector(
                (Math.random() - 0.5) * 0.3,
                (Math.random() - 0.5) * 0.2,
                (Math.random() - 0.5) * 0.3
            )).normalize();
            
            Arrow arrow = skeleton.getWorld().spawnArrow(eyeLocation, direction, 1.8f, 1.0f);
            arrow.setShooter((ProjectileSource) skeleton);
            arrow.setDamage(attackDamage * 1.2);
            arrow.setCritical(true);
            
            // Delay pequeno entre os disparos
            if (i > 0) {
                arrow.setVelocity(arrow.getVelocity().multiply(1.0 + (i * 0.1)));
            }
        }
    }
    
    /**
     * Executa ataque giratório em área
     */
    private void performWhirlwindAttack() {
        if (!(entity instanceof Skeleton)) return;
        
        Skeleton skeleton = (Skeleton) entity;
        Location center = skeleton.getLocation();
        
        // Atacar todas as entidades hostis em um raio de 4 blocos
        for (Entity nearby : skeleton.getNearbyEntities(4, 4, 4)) {
            if (nearby instanceof LivingEntity && isHostileEntity(nearby)) {
                LivingEntity target = (LivingEntity) nearby;
                target.damage(attackDamage * 1.5, skeleton);
                
                // Empurrar levemente para trás
                Vector knockback = target.getLocation().subtract(center).toVector().normalize().multiply(0.5);
                knockback.setY(0.2);
                target.setVelocity(knockback);
            }
        }
        
        // Efeito visual do ataque giratório
        for (int i = 0; i < 360; i += 30) {
            double radians = Math.toRadians(i);
            Location particleLoc = center.clone().add(
                Math.cos(radians) * 3,
                1,
                Math.sin(radians) * 3
            );
            center.getWorld().spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 1);
        }
    }
    
    @Override
    public String getLevel5AbilityDescription() {
        if (isRangedMode) {
            return "Rajada Mortal: Dispara múltiplas flechas certeiras";
        } else {
            return "Ataque Giratório: Ataca todos os inimigos ao redor";
        }
    }
    
    /**
     * Alterna entre modo à distância e corpo a corpo
     */
    public void toggleCombatMode() {
        if (entity instanceof Skeleton) {
            Skeleton skeleton = (Skeleton) entity;
            isRangedMode = !isRangedMode;
            
            if (isRangedMode) {
                skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
            } else {
                skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            }
            
            Player owner = plugin.getServer().getPlayer(ownerId);
            if (owner != null && owner.isOnline()) {
                String mode = isRangedMode ? "Arqueiro" : "Guerreiro";
                owner.sendMessage(plugin.formatMessage("&6" + petName + " &eentrou no modo &6" + mode + "&e!"));
            }
        }
    }
    
    public boolean isRangedMode() {
        return isRangedMode;
    }
}

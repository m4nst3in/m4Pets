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

import java.util.UUID;

/**
 * Pet Guerreiro Zumbi
 * Características: Tanque com alta resistência e regeneração
 */
public class ZombiePet extends WarriorPet {
    
    public ZombiePet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.ZOMBIE, variant);
    }
    
    @Override
    protected Entity spawnEntityType(Location location) {
        return location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
    }
    
    @Override
    protected void customizeEntity() {
        if (entity instanceof Zombie) {
            Zombie zombie = (Zombie) entity;
            
            // Usar o método da classe pai para definir o nome customizado
            updateEntityName();
            
            // Configurar como pet domesticado
            zombie.setAdult();
            zombie.setCanBreakDoors(false);
            zombie.setCanPickupItems(false);
            
            // Proteger contra fogo solar - equipar helmet invisível
            zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
            zombie.getEquipment().setHelmetDropChance(0.0f);
            
            // Equipar com espada de ferro baseado na variante
            if ("armed".equals(variant)) {
                zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                zombie.getEquipment().setItemInMainHandDropChance(0.0f);
            }
            
            // Configurar atributos baseados no nível
            if (zombie.getAttribute(Attribute.MAX_HEALTH) != null) {
                zombie.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                zombie.setHealth(health);
            }
            
            if (zombie.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
                zombie.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(attackDamage);
            }
            
            if (zombie.getAttribute(Attribute.ATTACK_SPEED) != null) {
                zombie.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(attackSpeed);
            }
            
            if (zombie.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                zombie.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.25);
            }
            
            // Definir como não agressivo por padrão (controlado pela IA)
            zombie.setTarget(null);
            
            // Configurações adicionais de segurança para evitar comportamento hostil
            zombie.setRemoveWhenFarAway(false);
            zombie.setPersistent(true);
            
            // Garantir que não ataque o dono configurando como não hostil
            Player owner = Bukkit.getPlayer(ownerId);
            if (owner != null) {
                // Remover o dono da lista de alvos possíveis
                zombie.setAI(false); // Temporariamente desabilitar IA padrão
                zombie.setAI(true);  // Reabilitar IA personalizada
            }
        }
    }
    
    @Override
    protected void performAttack(LivingEntity target) {
        if (!(entity instanceof Zombie)) return;
        
        Zombie zombie = (Zombie) entity;
        
        // Verificar se o alvo é válido e não é um pet
        if (target.isDead() || target.equals(zombie)) return;
        
        // VERIFICAÇÃO CRÍTICA: nunca atacar o dono
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null) {
            // Múltiplas verificações para garantir que não ataque o dono
            if (target.equals(owner) || 
                target.getUniqueId().equals(owner.getUniqueId()) ||
                (target instanceof Player && ((Player) target).getName().equalsIgnoreCase(owner.getName()))) {
                
                // Se tentou atacar o dono, cancelar completamente
                zombie.setTarget(null);
                currentTarget = null;
                return;
            }
        }
        
        // Verificar se o alvo não é outro pet
        for (AbstractPet pet : plugin.getPetManager().getAllActivePets()) {
            if (pet.getEntity() != null && pet.getEntity().equals(target)) {
                return; // Nunca atacar outros pets
            }
        }
        
        // Ataque básico - usar damage simples sem especificar o atacante para evitar loops
        target.damage(attackDamage);
        
        // Efeito visual e sonoro
        target.getWorld().spawnParticle(Particle.BLOCK, 
            target.getLocation().add(0, 1, 0), 
            10, 0.5, 0.5, 0.5, 0.1, 
            Material.REDSTONE_BLOCK.createBlockData());
        
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
        
        // Chance de aplicar efeito de lentidão no alvo (20% de chance)
        if (Math.random() < 0.2) {
            if (target instanceof LivingEntity) {
                ((LivingEntity) target).addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOWNESS, 60, 0, false, false));
            }
        }
        
        // Habilidade especial de nível 5
        if (hasLevel5Ability() && Math.random() < 0.15) {
            performLevel5Ability();
        }
    }
    
    @Override
    protected void performLevel5Ability() {
        if (!(entity instanceof Zombie)) return;
        
        Zombie zombie = (Zombie) entity;
        
        // "Sede de Sangue" - Regeneração e aumento temporário de dano
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, false));
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0, false, false));
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0, false, false));
        
        // Efeito visual especial
        Location loc = zombie.getLocation().add(0, 1, 0);
        zombie.getWorld().spawnParticle(Particle.DUST, loc, 20, 1, 1, 1, 0.1);
        zombie.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 0.8f);
        
        // Notificar o dono
        Player owner = plugin.getServer().getPlayer(ownerId);
        if (owner != null && owner.isOnline()) {
            owner.sendMessage(plugin.formatMessage("&c" + petName + " &eativou sua habilidade &cSede de Sangue&e!"));
        }
    }
    
    @Override
    public String getLevel5AbilityDescription() {
        return "Sede de Sangue: Regenera vida e aumenta dano temporariamente";
    }
}

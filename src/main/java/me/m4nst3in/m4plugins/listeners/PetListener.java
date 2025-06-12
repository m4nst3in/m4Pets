package me.m4nst3in.m4plugins.listeners;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import me.m4nst3in.m4plugins.pets.abstractpets.WarriorPet;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.UUID;

public class PetListener implements Listener {
    
    private final M4Pets plugin;
    
    public PetListener(M4Pets plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Lidar com dano aos pets
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        
        // Verificar se é um pet ativo
        for (AbstractPet pet : plugin.getPetManager().getAllActivePets()) {
            if (pet.getEntity() != null && pet.getEntity().equals(entity)) {
                // Cancelar dano do ambiente (configurável)
                if (plugin.getConfigManager().getMainConfig().getBoolean("pets.global.invulnerable-to-environment", false)) {
                    if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                        event.setCancelled(true);
                        return;
                    }
                }
                
                // Aplicar dano ao pet
                double damage = event.getDamage();
                boolean died = pet.damage(damage);
                
                if (died) {
                    // Notificar o dono que o pet morreu
                    Player owner = plugin.getServer().getPlayer(pet.getOwnerId());
                    if (owner != null) {
                        owner.sendMessage(plugin.formatMessage("&c&lSeu pet " + pet.getPetName() + " morreu! Use /pets para ressuscitá-lo."));
                    }
                }
                
                return;
            }
        }
    }
    
    /**
     * Lidar com a morte de um pet
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        
        // Verificar se é um pet ativo
        for (AbstractPet pet : plugin.getPetManager().getAllActivePets()) {
            if (pet.getEntity() != null && pet.getEntity().equals(entity)) {
                // Marcar como morto e remover do mundo
                pet.damage(pet.getHealth()); // Isso vai definir a saúde para 0 e marcar como morto
                
                // Remover drops
                event.getDrops().clear();
                event.setDroppedExp(0);
                
                return;
            }
        }
    }
    
    /**
     * Remover pets quando o jogador sai
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Verificar se deve despawnar pets ao desconectar
        if (plugin.getConfigManager().getMainConfig().getBoolean("pets.global.despawn-on-disconnect", true)) {
            UUID playerUUID = player.getUniqueId();
            
            // Despawnar todos os pets do jogador
            for (AbstractPet pet : plugin.getPetManager().getPlayerPets(playerUUID)) {
                if (pet.isSpawned()) {
                    pet.despawn();
                }
            }
        }
        
        // Limpar callbacks do GUI Manager
        plugin.getGuiManager().clearCallbacks(player);
    }
    
    /**
     * Lidar com mudanças de mundo
     */
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        // Verificar se deve despawnar pets ao mudar de mundo
        if (plugin.getConfigManager().getMainConfig().getBoolean("pets.global.despawn-on-world-change", false)) {
            UUID playerUUID = player.getUniqueId();
            
            // Despawnar todos os pets do jogador
            for (AbstractPet pet : plugin.getPetManager().getPlayerPets(playerUUID)) {
                if (pet.isSpawned()) {
                    pet.despawn();
                    player.sendMessage(plugin.formatMessage("&eSeu pet foi removido ao mudar de mundo."));
                }
            }
        }
    }
    
    /**
     * Interação com pets (para montar)
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        
        // Verificar se o jogador está interagindo com um pet
        for (AbstractPet pet : plugin.getPetManager().getAllActivePets()) {
            if (pet.getEntity() != null && pet.getEntity().equals(entity)) {
                // Verificar se o jogador é o dono do pet
                if (pet.getOwnerId().equals(player.getUniqueId())) {
                    // Se for um pet de montaria, tentar montar
                    if (pet instanceof MountPet) {
                        event.setCancelled(true);
                        MountPet mountPet = (MountPet) pet;
                        
                        if (mountPet.isMounted() && entity.getPassengers().contains(player)) {
                            // Se já estiver montado, desmontar
                            mountPet.dismount(player);
                            player.sendMessage(plugin.formatMessage("&eVocê desmontou do seu pet."));
                        } else {
                            // Tentar montar
                            boolean mounted = mountPet.mount(player);
                            if (mounted) {
                                player.sendMessage(plugin.formatMessage("&aVocê montou no seu pet."));
                            } else {
                                player.sendMessage(plugin.formatMessage("&cNão foi possível montar no seu pet."));
                            }
                        }
                    }
                } else {
                    player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.not-owner")));
                    event.setCancelled(true);
                }
                
                return;
            }
        }
    }
    
    /**
     * Registrar estado de montaria quando um jogador monta em um pet
     * IMPORTANTE: Este listener APENAS registra o estado, não tenta montar novamente para evitar loops
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntered();
        Entity vehicle = event.getVehicle();
        
        // Verificar se a entidade sendo montada é um pet
        for (AbstractPet pet : plugin.getPetManager().getAllActivePets()) {
            if (pet.getEntity() != null && pet.getEntity().equals(vehicle)) {
                // Verificar se o jogador é o dono do pet
                if (!player.getUniqueId().equals(pet.getOwnerId())) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.not-owner")));
                    return;
                }
                
                // Se for uma montaria, apenas atualizar o status interno
                // NÃO CHAMAR mount() aqui para evitar loop infinito
                if (pet instanceof MountPet) {
                    MountPet mountPet = (MountPet) pet;
                    // Atualizar apenas variáveis internas
                    mountPet.registerMountedState(player);
                }
                
                return;
            }
        }
    }
    
    /**
     * Prevenir que pets ataquem seus donos
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        
        // Verificar se o causador do dano é um pet atacando seu dono
        for (AbstractPet pet : plugin.getPetManager().getAllActivePets()) {
            if (pet.getEntity() != null && pet.getEntity().equals(damager)) {
                // Verificar se a vítima é o dono do pet
                if (victim instanceof Player) {
                    Player player = (Player) victim;
                    if (pet.getOwnerId().equals(player.getUniqueId())) {
                        // CANCELAR DANO IMEDIATAMENTE
                        event.setCancelled(true);
                        
                        // Limpar o alvo do pet se for um warrior pet
                        if (pet instanceof WarriorPet) {
                            WarriorPet warriorPet = (WarriorPet) pet;
                            warriorPet.setTargetPlayer(null);
                            if (pet.getEntity() instanceof org.bukkit.entity.Mob) {
                                ((org.bukkit.entity.Mob) pet.getEntity()).setTarget(null);
                            }
                        }
                        
                        plugin.getLogger().warning("BLOQUEADO: Pet " + pet.getPetName() + " tentou atacar seu dono " + player.getName());
                        return;
                    }
                }
            }
        }
        
        // Verificar se um jogador está atacando um pet (lógica original)
        if (damager instanceof Player) {
            Player player = (Player) damager;
            
            // Verificar se o pet é do jogador
            for (AbstractPet pet : plugin.getPetManager().getAllActivePets()) {
                if (pet.getOwnerId().equals(player.getUniqueId()) && pet.getEntity() != null && pet.getEntity().equals(victim)) {
                    // Cancelar dano
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
    
    /**
     * Prevenir que pets tenham seus alvos definidos como seus donos
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        Entity entity = event.getEntity();
        
        // Verificar se o alvo é um jogador
        if (target instanceof Player) {
            Player player = (Player) target;
            
            // Verificar se o pet pertence ao jogador
            for (AbstractPet pet : plugin.getPetManager().getAllActivePets()) {
                if (pet.getOwnerId().equals(player.getUniqueId()) && 
                    pet.getEntity() != null && pet.getEntity().equals(entity)) {
                    // CANCELAR O TARGETING IMEDIATAMENTE
                    event.setCancelled(true);
                    plugin.getLogger().warning("BLOQUEADO: Pet " + pet.getPetName() + " tentou targetar seu dono " + player.getName());
                    
                    // Se for um warrior pet, limpar o target também
                    if (pet instanceof WarriorPet) {
                        WarriorPet warriorPet = (WarriorPet) pet;
                        warriorPet.setTargetPlayer(null);
                    }
                    return;
                }
            }
        }
    }
}
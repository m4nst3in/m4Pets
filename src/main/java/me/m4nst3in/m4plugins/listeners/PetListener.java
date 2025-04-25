package me.m4nst3in.m4plugins.listeners;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
                        ((MountPet) pet).mount(player);
                    }
                }
                
                return;
            }
        }
    }
}
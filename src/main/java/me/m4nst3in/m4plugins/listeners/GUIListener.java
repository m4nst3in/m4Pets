package me.m4nst3in.m4plugins.listeners;

import me.m4nst3in.m4plugins.M4Pets;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class GUIListener implements Listener {
    
    private final M4Pets plugin;
    
    public GUIListener(M4Pets plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Lidar com cliques em inventários
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Verificar se é um inventário do nosso plugin pelos títulos conhecidos
        if (title.contains("M4Pets") || 
            title.contains("Pets Guerreiros") || 
            title.contains("Controle:")) {
            
            // Delegar para o GUIManager
            plugin.getGuiManager().handleInventoryClick(event);
        }
    }
    
    /**
     * Lidar com fechamento de inventários
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        // Não é necessário implementar nada aqui por enquanto
        // Pode ser usado no futuro para persistir estados
    }
    
    /**
     * Lidar com mensagens de chat para renomeação de pets
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Verificar se o jogador está renomeando um pet
        boolean handled = plugin.getGuiManager().handleChatForRename(player, message);
        
        // Se a mensagem foi usada para renomear um pet, cancelar o evento (não enviar no chat)
        if (handled) {
            event.setCancelled(true);
        }
    }
}
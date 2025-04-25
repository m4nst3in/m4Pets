package me.m4nst3in.m4plugins.gui;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {
    
    private final M4Pets plugin;
    private final MainGUI mainGUI;
    private final PetStoreGUI petStoreGUI;
    private final MyPetsGUI myPetsGUI;
    private final PetInfoGUI petInfoGUI;
    
    // Mapas para rastrear callbacks e estados
    private final Map<UUID, AbstractPet> petRenameCallbacks; // Para renomear pets
    private final Map<UUID, AbstractPet> petCosmeticsCallbacks; // Para cosméticos
    private final Map<UUID, AbstractPet> petVariantCallbacks; // Para variantes
    
    public GUIManager(M4Pets plugin) {
        this.plugin = plugin;
        this.mainGUI = new MainGUI(plugin);
        this.petStoreGUI = new PetStoreGUI(plugin);
        this.myPetsGUI = new MyPetsGUI(plugin);
        this.petInfoGUI = new PetInfoGUI(plugin);
        
        this.petRenameCallbacks = new HashMap<>();
        this.petCosmeticsCallbacks = new HashMap<>();
        this.petVariantCallbacks = new HashMap<>();
    }
    
    /**
     * Abre o menu principal para o jogador
     */
    public void openMainMenu(Player player) {
        mainGUI.openMainMenu(player);
    }
    
    /**
     * Processa cliques em inventários
     */
    public void handleInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        
        // Menu Principal
        if (title.equals(TextUtil.color("&9&lM4Pets &8| &7Menu Principal"))) {
            event.setCancelled(true);
            
            if (slot == 11) { // Loja de Pets
                petStoreGUI.openMainStore(player);
            } else if (slot == 15) { // Meus Pets
                myPetsGUI.openPetsList(player);
            } else if (slot == 22) { // Informações
                petInfoGUI.openInfoMenu(player);
            }
        }
        // Menu Loja
        else if (title.equals(TextUtil.color("&9&lM4Pets &8| &eLoja de Pets"))) {
            event.setCancelled(true);
            
            if (slot == 10) { // Guerreiros
                // Em desenvolvimento
                player.sendMessage(plugin.formatMessage("&cEsta categoria está em desenvolvimento!"));
            } else if (slot == 12) { // Montarias
                petStoreGUI.openCategory(player, "mounts");
            } else if (slot == 14) { // Trabalhadores
                // Em desenvolvimento
                player.sendMessage(plugin.formatMessage("&cEsta categoria está em desenvolvimento!"));
            } else if (slot == 16) { // Decorativos
                // Em desenvolvimento
                player.sendMessage(plugin.formatMessage("&cEsta categoria está em desenvolvimento!"));
            } else if (slot == 26) { // Voltar
                mainGUI.openMainMenu(player);
            }
        }
        // Menu Categoria - Montarias
        else if (title.equals(TextUtil.color("&9&lM4Pets &8| &eMounts"))) {
            event.setCancelled(true);
            
            // Se for um slot de item de pet e não um filler ou voltar
            if (slot >= 10 && slot <= 44 && event.getCurrentItem() != null && 
                !event.getCurrentItem().getType().name().endsWith("GLASS_PANE") && 
                slot != 49) {
                
                // Determinar qual pet foi clicado com base no slot
                String petKey = getPetKeyFromSlot(slot);
                if (petKey != null) {
                    petStoreGUI.processPetPurchase(player, "mounts", petKey);
                }
            } else if (slot == 49) { // Voltar
                petStoreGUI.openMainStore(player);
            }
        }
        // Menu Meus Pets
        else if (title.equals(TextUtil.color("&9&lM4Pets &8| &aMeus Pets"))) {
            event.setCancelled(true);
            
            // Se for um slot de pet e não um filler ou voltar
            if (slot >= 10 && slot <= 44 && event.getCurrentItem() != null && 
                !event.getCurrentItem().getType().name().endsWith("GLASS_PANE")) {
                
                // Determinar qual pet foi clicado com base no slot
                AbstractPet pet = getPetFromSlot(player, slot);
                if (pet != null) {
                    myPetsGUI.openPetManagement(player, pet);
                }
            } else if (slot == 49) { // Voltar
                mainGUI.openMainMenu(player);
            }
        }
        // Menu de Gerenciamento de Pet
        else if (title.startsWith(TextUtil.color("&9&lM4Pets &8| &a")) && 
                 !title.equals(TextUtil.color("&9&lM4Pets &8| &aMeus Pets"))) {
            event.setCancelled(true);
            
            // Encontrar o pet sendo gerenciado
            AbstractPet pet = findPetByName(player, title.substring(TextUtil.color("&9&lM4Pets &8| &a").length()));
            if (pet != null) {
                myPetsGUI.handlePetManagementAction(player, pet, slot);
            }
        }
        // Menu de Cosméticos
        else if (title.equals(TextUtil.color("&9&lM4Pets &8| &dCosméticos"))) {
            event.setCancelled(true);
            
            AbstractPet pet = petCosmeticsCallbacks.get(player.getUniqueId());
            if (pet != null) {
                myPetsGUI.handleCosmeticPurchase(player, pet, slot);
            }
        }
        // Menu de Variantes
        else if (title.equals(TextUtil.color("&9&lM4Pets &8| &6Aparência"))) {
            event.setCancelled(true);
            
            AbstractPet pet = petVariantCallbacks.get(player.getUniqueId());
            if (pet != null) {
                myPetsGUI.handleVariantSelection(player, pet, slot);
            }
        }
        // Menu de Informações
        else if (title.equals(TextUtil.color("&9&lM4Pets &8| &bInformações"))) {
            event.setCancelled(true);
            
            if (slot == 26) { // Voltar
                mainGUI.openMainMenu(player);
            }
        }
    }
    
    /**
     * Obtém a chave do pet com base no slot clicado na loja
     */
    private String getPetKeyFromSlot(int slot) {
        // Mapear slots para nomes de pets (mounts)
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        String[] keys = {"pig", "horse", "donkey", "sheep", "cow", "sniffer"};
        
        int index = -1;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) {
                index = i;
                break;
            }
        }
        
        if (index >= 0 && index < keys.length) {
            return keys[index];
        }
        
        return null;
    }
    
    /**
     * Obtém o pet com base no slot clicado na lista de pets
     */
    private AbstractPet getPetFromSlot(Player player, int slot) {
        // Mapear slots para índices de pets
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        
        int index = -1;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) {
                index = i;
                break;
            }
        }
        
        if (index >= 0) {
            // Obter a lista de pets do jogador
            java.util.List<AbstractPet> pets = new java.util.ArrayList<>(
                plugin.getPetManager().getPlayerPets(player.getUniqueId())
            );
            
            if (index < pets.size()) {
                return pets.get(index);
            }
        }
        
        return null;
    }
    
    /**
     * Encontra um pet pelo nome
     */
    private AbstractPet findPetByName(Player player, String name) {
        for (AbstractPet pet : plugin.getPetManager().getPlayerPets(player.getUniqueId())) {
            if (pet.getPetName().equals(name)) {
                return pet;
            }
        }
        return null;
    }
    
    /**
     * Registra um callback para renomear pet
     */
    public void registerRenamePetCallback(Player player, AbstractPet pet) {
        petRenameCallbacks.put(player.getUniqueId(), pet);
    }
    
    /**
     * Processa uma mensagem de chat para renomear um pet
     */
    public boolean handleChatForRename(Player player, String message) {
        AbstractPet pet = petRenameCallbacks.get(player.getUniqueId());
        if (pet == null) return false;
        
        // Remover o callback
        petRenameCallbacks.remove(player.getUniqueId());
        
        // Renomear o pet
        pet.rename(message);
        plugin.getPetManager().savePet(pet);
        
        player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.pet-renamed")
                .replace("%pet_name%", message)));
        
        // Reabrir a interface de gerenciamento
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            myPetsGUI.openPetManagement(player, pet);
        });
        
        return true;
    }
    
    /**
     * Registra um pet para o menu de cosméticos
     */
    public void registerPetForCosmetics(Player player, AbstractPet pet) {
        petCosmeticsCallbacks.put(player.getUniqueId(), pet);
    }
    
    /**
     * Registra um pet para o menu de variantes
     */
    public void registerPetForVariant(Player player, AbstractPet pet) {
        petVariantCallbacks.put(player.getUniqueId(), pet);
    }
    
    /**
     * Limpa os callbacks para um jogador quando ele sai do servidor
     */
    public void clearCallbacks(Player player) {
        UUID playerUUID = player.getUniqueId();
        petRenameCallbacks.remove(playerUUID);
        petCosmeticsCallbacks.remove(playerUUID);
        petVariantCallbacks.remove(playerUUID);
    }
}
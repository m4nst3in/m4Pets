package me.m4nst3in.m4plugins.gui;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.pets.abstractpets.WarriorPet;
import me.m4nst3in.m4plugins.pets.warriors.SkeletonPet;
import me.m4nst3in.m4plugins.pets.warriors.VindicatorPet;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {
    
    private final M4Pets plugin;
    private final MainGUI mainGUI;
    private final PetStoreGUI petStoreGUI;
    private final MyPetsGUI myPetsGUI;
    private final PetInfoGUI petInfoGUI;
    private final WarriorGUI warriorGUI;
    
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
        this.warriorGUI = new WarriorGUI(plugin);
        
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
        // Adicionado para garantir que o clicker é um jogador
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        int slot = event.getRawSlot();
        
        // Menu Principal  ✨ M4Pets ✨
        if (title.equals(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &6&l⭐ Menu Principal ⭐"))) {
            event.setCancelled(true);
            
            if (slot == 11) { // Loja de Pets 🛒
                petStoreGUI.openMainStore(player);
            } else if (slot == 15) { // Meus Pets 🐾
                myPetsGUI.openPetsList(player);
            } else if (slot == 22) { // Informações ℹ
                petInfoGUI.openInfoMenu(player);
            }
        }
        // Menu Loja 🛒
        else if (title.equals(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &e&l🛒 Loja de Pets"))) {
            event.setCancelled(true);
            
            if (slot == 10) { // Guerreiros ⚔️
                petStoreGUI.openCategory(player, "warriors");
            } else if (slot == 12) { // Montarias 🐎
                petStoreGUI.openCategory(player, "mounts");
            } else if (slot == 14) { // Trabalhadores 🔨
                player.sendMessage(plugin.formatMessage("&e&l✨ &fEsta categoria ainda está em desenvolvimento! &e&l✨"));
            } else if (slot == 16) { // Decorativos 🌸
                player.sendMessage(plugin.formatMessage("&e&l✨ &fEsta categoria ainda está em desenvolvimento! &e&l✨"));
            } else if (slot == 26) { // Voltar ◀
                mainGUI.openMainMenu(player);
            }
        }
        // Menu Categoria - Guerreiros ⚔️
        else if (title.equals(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &c&l⚔ Guerreiros &c&l⚔"))) {
            event.setCancelled(true);
            
            // Se for um slot de item de pet e não um filler ou voltar
            if (slot >= 10 && slot <= 44 && event.getCurrentItem() != null && 
                !event.getCurrentItem().getType().name().endsWith("GLASS_PANE") && 
                slot != 49) {
                
                // Determinar qual pet guerreiro foi clicado com base no slot
                String petKey = getWarriorPetKeyFromSlot(slot);
                if (petKey != null) {
                    petStoreGUI.processPetPurchase(player, "warriors", petKey);
                }
            } else if (slot == 49) { // Voltar ◀
                petStoreGUI.openMainStore(player);
            }
        }
        // Menu Categoria - Montarias 🐎
        else if (title.equals(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &6&l🐎 Montarias &6&l🐎"))) {
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
            } else if (slot == 49) { // Voltar ◀
                petStoreGUI.openMainStore(player);
            }
        }
        // Menu Meus Pets 🐾
        else if (title.equals(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &a&l🐾 Meus Pets 🐾"))) {
            event.setCancelled(true);
            
            // Se for um slot de pet e não um filler ou voltar
            if (slot >= 10 && slot <= 44 && event.getCurrentItem() != null && 
                !event.getCurrentItem().getType().name().endsWith("GLASS_PANE")) {
                
                // Determinar qual pet foi clicado com base no slot
                AbstractPet pet = getPetFromSlot(player, slot);
                if (pet != null) {
                    myPetsGUI.openPetManagement(player, pet);
                }
            } else if (slot == 49) { // Voltar ◀
                mainGUI.openMainMenu(player);
            }
        }
        // Menu de Gerenciamento de Pet 🛠️
        else if (title.startsWith(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &a&l")) && 
                 !title.equals(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &a&l🐾 Meus Pets 🐾"))) {
            event.setCancelled(true);
            
            // Encontrar o pet sendo gerenciado
            AbstractPet pet = findPetByName(player, title.substring(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &a&l").length()));
            if (pet != null) {
                myPetsGUI.handlePetManagementAction(player, pet, slot);
            }
        }
        // Menu de Cosméticos ✨
        else if (title.equals(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &d&l💎 Cosméticos 💎"))) {
            event.setCancelled(true);
            
            AbstractPet pet = petCosmeticsCallbacks.get(player.getUniqueId());
            if (pet != null) {
                myPetsGUI.handleCosmeticPurchase(player, pet, slot);
            }
        }
        // Menu de Variantes 🎨
        else if (title.equals(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &6&l🎨 Aparência 🎨"))) {
            event.setCancelled(true);
            
            AbstractPet pet = petVariantCallbacks.get(player.getUniqueId());
            if (pet != null) {
                myPetsGUI.handleVariantSelection(player, pet, slot);
            }
        }
        // Menu de Informações ℹ️
        else if (title.equals(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &b&lℹ Informações ℹ"))) { // Updated title
            event.setCancelled(true);
            
            if (slot == 26) { // Voltar ◀
                mainGUI.openMainMenu(player);
            }
        }
        // Menu Pets Guerreiros ⚔️
        else if (title.equals(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &c&l⚔ Guerreiros Ativos ⚔"))) { // Updated title
            event.setCancelled(true);
            
            if (slot >= 10 && slot <= 16 && event.getCurrentItem() != null && 
                !event.getCurrentItem().getType().name().endsWith("GLASS_PANE")) {
                
                // Encontrar qual pet guerreiro foi clicado
                WarriorPet warriorPet = getWarriorPetFromSlot(player, slot);
                if (warriorPet != null) {
                    warriorGUI.openWarriorControlMenu(player, warriorPet);
                }
            } else if (slot == 49) { // Voltar ◀
                mainGUI.openMainMenu(player);
            }
        }
        // Menu Controle de Pet Guerreiro 🎮
        else if (title.startsWith(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &7&l🎮 Controle: "))) { // Updated title
            event.setCancelled(true);
            
            String petName = title.substring(TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &7&l🎮 Controle: ").length()); // Updated title
            WarriorPet warriorPet = getPlayerWarriorPetByName(player, petName);
            
            if (warriorPet != null) {
                handleWarriorControlClick(player, warriorPet, slot);
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
    
    public WarriorGUI getWarriorGUI() {
        return warriorGUI;
    }
    
    /**
     * Abre o menu de pets guerreiros
     */
    public void openWarriorMenu(Player player) {
        warriorGUI.openWarriorMenu(player);
    }
    
    /**
     * Obtém um pet guerreiro a partir do slot clicado
     */
    private WarriorPet getWarriorPetFromSlot(Player player, int slot) {
        Collection<AbstractPet> pets = plugin.getPetManager().getPlayerPets(player.getUniqueId());
        java.util.List<WarriorPet> warriorPets = new java.util.ArrayList<>();
        
        for (AbstractPet pet : pets) {
            if (pet instanceof WarriorPet) {
                warriorPets.add((WarriorPet) pet);
            }
        }
        
        int index = slot - 10;
        if (index >= 0 && index < warriorPets.size()) {
            return warriorPets.get(index);
        }
        return null;
    }
    
    /**
     * Obtém um pet guerreiro pelo nome
     */
    private WarriorPet getPlayerWarriorPetByName(Player player, String name) {
        Collection<AbstractPet> pets = plugin.getPetManager().getPlayerPets(player.getUniqueId());
        
        for (AbstractPet pet : pets) {
            if (pet instanceof WarriorPet && pet.getPetName().equals(name)) {
                return (WarriorPet) pet;
            }
        }
        return null;
    }
    
    /**
     * Manipula cliques no menu de controle de pet guerreiro
     */
    private void handleWarriorControlClick(Player player, WarriorPet warriorPet, int slot) {
        switch (slot) {
            case 19: // Controle de IA
                warriorPet.setAIEnabled(!warriorPet.isAIEnabled());
                warriorGUI.openWarriorControlMenu(player, warriorPet); // Refresh
                break;
                
            case 21: // Limpar alvo
                warriorPet.setTargetPlayer(null);
                if (warriorPet.getEntity() instanceof org.bukkit.entity.Mob) {
                    ((org.bukkit.entity.Mob) warriorPet.getEntity()).setTarget(null);
                }
                player.sendMessage(plugin.formatMessage("&a" + warriorPet.getPetName() + " &enão tem mais alvos específicos."));
                break;
                
            case 23: // Habilidade especial
                if (warriorPet.hasLevel5Ability()) {
                    if (warriorPet instanceof VindicatorPet) {
                        ((VindicatorPet) warriorPet).activateBerserkerFury();
                    } else {
                        warriorPet.useLevel5Ability();
                    }
                    player.sendMessage(plugin.formatMessage("&a" + warriorPet.getPetName() + " &eusou sua habilidade especial!"));
                }
                break;
                
            case 25: // Modo de combate (esqueleto)
                if (warriorPet instanceof SkeletonPet) {
                    ((SkeletonPet) warriorPet).toggleCombatMode();
                    warriorGUI.openWarriorControlMenu(player, warriorPet); // Refresh
                }
                break;
                
            case 40: // Voltar
                warriorGUI.openWarriorMenu(player);
                break;
        }
    }
    
    /**
     * Obtém a chave do pet guerreiro com base no slot clicado na loja
     */
    private String getWarriorPetKeyFromSlot(int slot) {
        // Mapear slots para nomes de pets (warriors)
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        String[] keys = {"zombie", "skeleton", "vindicator"};
        
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
}
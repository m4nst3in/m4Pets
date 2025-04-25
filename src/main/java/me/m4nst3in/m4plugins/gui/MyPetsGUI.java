package me.m4nst3in.m4plugins.gui;

import me.m4nst3in.m4plugins.M4Pets;
import org.bukkit.configuration.ConfigurationSection;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MyPetsGUI {
    
    private final M4Pets plugin;
    private final Map<UUID, Map<UUID, Inventory>> petManagementInventories; // playerUUID -> petUUID -> Inventory
    
    public MyPetsGUI(M4Pets plugin) {
        this.plugin = plugin;
        this.petManagementInventories = new HashMap<>();
    }
    
    /**
     * Abre a GUI de lista de pets do jogador
     */
    public void openPetsList(Player player) {
        Collection<AbstractPet> pets = plugin.getPetManager().getPlayerPets(player.getUniqueId());
        
        String title = TextUtil.color("&9&lM4Pets &8| &aMeus Pets");
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        
        if (pets.isEmpty()) {
            // Mostrar mensagem de que o jogador não tem pets
            ItemStack noPetsItem = new ItemStack(Material.BARRIER);
            ItemMeta noPetsMeta = noPetsItem.getItemMeta();
            if (noPetsMeta != null) {
                noPetsMeta.setDisplayName(TextUtil.color("&cVocê não possui pets!"));
                List<String> lore = new ArrayList<>();
                lore.add(TextUtil.color("&7Visite a loja de pets para adquirir um."));
                noPetsMeta.setLore(lore);
                noPetsItem.setItemMeta(noPetsMeta);
            }
            inventory.setItem(22, noPetsItem);
        } else {
            // Adicionar cada pet à interface
            int slot = 10;
            
            for (AbstractPet pet : pets) {
                Material icon = Material.valueOf(
                    plugin.getConfigManager().getMainConfig().getString(
                        "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase() + ".icon", 
                        "NAME_TAG"
                    )
                );
                
                ItemStack item = new ItemStack(icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(TextUtil.color("&a&l" + pet.getPetName()));
                    
                    List<String> lore = new ArrayList<>();
                    lore.add(TextUtil.color("&7Tipo: &f" + plugin.getConfigManager().getMainConfig().getString(
                        "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase() + ".name", 
                        pet.getType().name()
                    )));
                    lore.add(TextUtil.color("&7Nível: &e" + pet.getLevel()));
                    lore.add(TextUtil.color("&7Vida: &c" + (int)pet.getHealth() + "&7/&c" + (int)pet.getMaxHealth()));
                    
                    if (pet.isDead()) {
                        lore.add(TextUtil.color("&c&lMORTO"));
                    }
                    
                    lore.add("");
                    lore.add(TextUtil.color("&aClique para gerenciar"));
                    
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                
                inventory.setItem(slot, item);
                
                // Avançar para o próximo slot
                slot++;
                if (slot % 9 == 8) {
                    slot += 2;
                }
                
                // Limitar a 28 pets por página
                if (slot >= 45) break;
            }
        }
        
        // Adicionar item de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&lVoltar"));
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(49, backItem);
        
        // Preencher espaços vazios com painéis de vidro
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
        
        player.openInventory(inventory);
    }
    
    /**
     * Abre a GUI de gerenciamento de um pet específico
     */
    public void openPetManagement(Player player, AbstractPet pet) {
        UUID playerUUID = player.getUniqueId();
        
        // Criar ou recuperar o mapa de inventários para este jogador
        petManagementInventories.putIfAbsent(playerUUID, new HashMap<>());
        Map<UUID, Inventory> playerInventories = petManagementInventories.get(playerUUID);
        
        // Criar um novo inventário para este pet
        String title = TextUtil.color("&9&lM4Pets &8| &a" + pet.getPetName());
        Inventory inventory = Bukkit.createInventory(null, 36, title);
        
        // Summon/Despawn
        ItemStack summonItem = new ItemStack(pet.isSpawned() ? Material.RED_DYE : Material.LIME_DYE);
        ItemMeta summonMeta = summonItem.getItemMeta();
        if (summonMeta != null) {
            summonMeta.setDisplayName(TextUtil.color(pet.isSpawned() ? "&c&lRemover Pet" : "&a&lInvocar Pet"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color(pet.isSpawned() 
                ? "&7Clique para remover seu pet do mundo" 
                : "&7Clique para invocar seu pet"));
            
            summonMeta.setLore(lore);
            summonItem.setItemMeta(summonMeta);
        }
        inventory.setItem(10, summonItem);
        
        // Renomear pet
        ItemStack renameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta renameMeta = renameItem.getItemMeta();
        if (renameMeta != null) {
            renameMeta.setDisplayName(TextUtil.color("&e&lRenomear Pet"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&7Clique para dar um novo nome ao seu pet"));
            
            renameMeta.setLore(lore);
            renameItem.setItemMeta(renameMeta);
        }
        inventory.setItem(12, renameItem);
        
        // Atualizar Pet (level up)
        ItemStack upgradeItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta upgradeMeta = upgradeItem.getItemMeta();
        if (upgradeMeta != null) {
            int maxLevel = plugin.getConfigManager().getMainConfig().getInt("pets.global.max-level", 5);
            boolean maxLevelReached = pet.getLevel() >= maxLevel;
            
            upgradeMeta.setDisplayName(TextUtil.color(maxLevelReached 
                ? "&7&lNível Máximo" 
                : "&b&lMelhorar Pet"));
            
            List<String> lore = new ArrayList<>();
            if (maxLevelReached) {
                lore.add(TextUtil.color("&7Seu pet já está no nível máximo!"));
            } else {
                double petCost = plugin.getConfigManager().getMainConfig().getDouble(
                    "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase() + ".cost", 
                    1000
                );
                double upgradeCost = petCost * 0.25 * pet.getLevel();
                
                lore.add(TextUtil.color("&7Nível atual: &e" + pet.getLevel()));
                lore.add(TextUtil.color("&7Próximo nível: &e" + (pet.getLevel() + 1)));
                lore.add("");
                lore.add(TextUtil.color("&7Custo: &e" + String.format("%.2f", upgradeCost)));
                lore.add("");
                lore.add(TextUtil.color("&7Clique para melhorar seu pet"));
            }
            
            upgradeMeta.setLore(lore);
            upgradeItem.setItemMeta(upgradeMeta);
        }
        inventory.setItem(14, upgradeItem);
        
        // Ressuscitar Pet (se estiver morto)
        ItemStack resurrectItem = new ItemStack(pet.isDead() ? Material.TOTEM_OF_UNDYING : Material.LIGHT_GRAY_DYE);
        ItemMeta resurrectMeta = resurrectItem.getItemMeta();
        if (resurrectMeta != null) {
            resurrectMeta.setDisplayName(TextUtil.color(pet.isDead() 
                ? "&d&lRessuscitar Pet" 
                : "&8&lRessuscitar Pet"));
            
            List<String> lore = new ArrayList<>();
            if (pet.isDead()) {
                double petCost = plugin.getConfigManager().getMainConfig().getDouble(
                    "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase() + ".cost", 
                    1000
                );
                int resurrectPercent = plugin.getConfigManager().getMainConfig().getInt("economy.resurrect-cost-percent", 25);
                double resurrectCost = petCost * (resurrectPercent / 100.0);
                
                lore.add(TextUtil.color("&7Seu pet está morto!"));
                lore.add(TextUtil.color("&7Custo para ressuscitar: &e" + String.format("%.2f", resurrectCost)));
                lore.add("");
                lore.add(TextUtil.color("&7Clique para ressuscitar seu pet"));
            } else {
                lore.add(TextUtil.color("&7Seu pet está vivo"));
            }
            
            resurrectMeta.setLore(lore);
            resurrectItem.setItemMeta(resurrectMeta);
        }
        inventory.setItem(16, resurrectItem);
        
        // Cosméticos
        ItemStack cosmeticsItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta cosmeticsMeta = cosmeticsItem.getItemMeta();
        if (cosmeticsMeta != null) {
            cosmeticsMeta.setDisplayName(TextUtil.color("&d&lCosméticos"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&7Customize a aparência do seu pet"));
            lore.add("");
            lore.add(TextUtil.color("&7Clique para ver os cosméticos disponíveis"));
            
            cosmeticsMeta.setLore(lore);
            cosmeticsItem.setItemMeta(cosmeticsMeta);
        }
        inventory.setItem(30, cosmeticsItem);
        
        // Mudança de variante (cor, etc)
        ItemStack variantItem = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        ItemMeta variantMeta = variantItem.getItemMeta();
        if (variantMeta != null) {
            variantMeta.setDisplayName(TextUtil.color("&6&lAparência"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&7Mude a aparência do seu pet"));
            lore.add(TextUtil.color("&7Variante atual: &f" + pet.getVariant()));
            lore.add("");
            lore.add(TextUtil.color("&7Clique para alterar a aparência"));
            
            variantMeta.setLore(lore);
            variantItem.setItemMeta(variantMeta);
        }
        inventory.setItem(32, variantItem);
        
        // Informações do pet
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(TextUtil.color("&b&lInformações do Pet"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&7Nome: &f" + pet.getPetName()));
            lore.add(TextUtil.color("&7Tipo: &f" + plugin.getConfigManager().getMainConfig().getString(
                "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase() + ".name", 
                pet.getType().name()
            )));
            lore.add(TextUtil.color("&7Nível: &e" + pet.getLevel()));
            lore.add(TextUtil.color("&7Vida: &c" + (int)pet.getHealth() + "&7/&c" + (int)pet.getMaxHealth()));
            
            if (pet.getLevel() >= 5) {
                lore.add("");
                lore.add(TextUtil.color("&a&lHabilidade de Nível 5:"));
                lore.add(TextUtil.color("&f" + pet.getLevel5AbilityDescription()));
            }
            
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(22, infoItem);
        
        // Item de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&lVoltar"));
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(27, backItem);
        
        // Preencher espaços vazios com painéis de vidro
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
        
        // Salvar o inventário e abrir para o jogador
        playerInventories.put(pet.getPetId(), inventory);
        player.openInventory(inventory);
    }
    
    /**
     * Processa uma ação na GUI de gerenciamento de pet
     */
    public void handlePetManagementAction(Player player, AbstractPet pet, int slot) {
        switch(slot) {
            case 10: // Summon/Despawn
                if (pet.isSpawned()) {
                    pet.despawn();
                    player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.pet-despawned")));
                } else {
                    if (plugin.getPetManager().spawnPet(player, pet)) {
                        // Mensagem já é enviada pelo método spawn do pet
                    }
                }
                // Atualizar a GUI
                openPetManagement(player, pet);
                break;
                
            case 12: // Rename
                player.closeInventory();
                player.sendMessage(plugin.formatMessage("&aDigite o novo nome para o seu pet no chat:"));
                
                // Registrar callback para próxima mensagem no chat
                plugin.getGuiManager().registerRenamePetCallback(player, pet);
                break;
                
            case 14: // Upgrade
                if (pet.upgrade(player)) {
                    // Mensagem já é enviada pelo método upgrade do pet
                    // Atualizar a GUI
                    openPetManagement(player, pet);
                }
                break;
                
            case 16: // Resurrect
                if (pet.isDead() && pet.resurrect(player)) {
                    // Mensagem já é enviada pelo método resurrect do pet
                    // Atualizar a GUI
                    openPetManagement(player, pet);
                }
                break;
                
            case 30: // Cosmetics
                openCosmeticsMenu(player, pet);
                break;
                
            case 32: // Variant
                openVariantMenu(player, pet);
                break;
                
            case 27: // Back
                openPetsList(player);
                break;
        }
    }
    
    /**
     * Abre o menu de cosméticos para o pet
     */
    public void openCosmeticsMenu(Player player, AbstractPet pet) {
        String title = TextUtil.color("&9&lM4Pets &8| &dCosméticos");
        Inventory inventory = Bukkit.createInventory(null, 36, title);
        
        // Obter configuração de cosméticos
        ConfigurationSection cosmeticsConfig = plugin.getConfigManager().getMainConfig().getConfigurationSection("cosmetics.particles");
        if (cosmeticsConfig != null) {
            int slot = 10;
            
            for (String cosmeticKey : cosmeticsConfig.getKeys(false)) {
                String name = cosmeticsConfig.getString(cosmeticKey + ".name", "Cosmético");
                Material icon = Material.valueOf(cosmeticsConfig.getString(cosmeticKey + ".icon", "BARRIER"));
                int cost = cosmeticsConfig.getInt(cosmeticKey + ".cost", 500);
                String particleType = cosmeticsConfig.getString(cosmeticKey + ".particle-type", "HEART");
                
                ItemStack item = new ItemStack(icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(TextUtil.color("&d&l" + name));
                    
                    List<String> lore = new ArrayList<>();
                    
                    boolean hasCosmetic = particleType.equals(pet.getCosmeticParticle());
                    
                    if (hasCosmetic) {
                        lore.add(TextUtil.color("&aEste cosmético está ativado"));
                        lore.add("");
                        lore.add(TextUtil.color("&eClique para desativar"));
                    } else {
                        lore.add(TextUtil.color("&7Preço: &e" + cost));
                        lore.add("");
                        lore.add(TextUtil.color("&aClique para comprar"));
                    }
                    
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                
                inventory.setItem(slot, item);
                
                // Avançar para o próximo slot
                slot++;
                if (slot % 9 == 8) {
                    slot += 2;
                }
            }
        }
        
        // Item de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&lVoltar"));
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(27, backItem);
        
        // Preencher espaços vazios com painéis de vidro
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
        
        // Registrar o pet para callback
        plugin.getGuiManager().registerPetForCosmetics(player, pet);
        
        player.openInventory(inventory);
    }
    
    /**
     * Processa a compra de um cosmético
     */
    public void handleCosmeticPurchase(Player player, AbstractPet pet, int slot) {
        // Obter configuração de cosméticos
        ConfigurationSection cosmeticsConfig = plugin.getConfigManager().getMainConfig().getConfigurationSection("cosmetics.particles");
        if (cosmeticsConfig == null) return;
        
        // Mapear slot para key de cosmético
        int currentSlot = 10;
        String selectedCosmeticKey = null;
        
        for (String cosmeticKey : cosmeticsConfig.getKeys(false)) {
            if (currentSlot == slot) {
                selectedCosmeticKey = cosmeticKey;
                break;
            }
            
            currentSlot++;
            if (currentSlot % 9 == 8) {
                currentSlot += 2;
            }
        }
        
        if (selectedCosmeticKey == null) {
            // Se for o botão de voltar
            if (slot == 27) {
                openPetManagement(player, pet);
            }
            return;
        }
        
        String particleType = cosmeticsConfig.getString(selectedCosmeticKey + ".particle-type");
        String cosmeticName = cosmeticsConfig.getString(selectedCosmeticKey + ".name");
        int cost = cosmeticsConfig.getInt(selectedCosmeticKey + ".cost", 500);
        
        // Verificar se o jogador já tem o cosmético
        if (particleType != null && particleType.equals(pet.getCosmeticParticle())) {
            // Desativar o cosmético
            pet.setParticleEffect(null);
            plugin.getPetManager().savePet(pet);
            
            player.sendMessage(plugin.formatMessage("&aCosmético desativado!"));
            openCosmeticsMenu(player, pet);
            return;
        }
        
        // Verificar se o jogador tem dinheiro suficiente
        if (plugin.getEconomy().getBalance(player) < cost) {
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.not-enough-money")
                    .replace("%price%", String.valueOf(cost))));
            return;
        }
        
        // Cobrar o jogador
        plugin.getEconomy().withdrawPlayer(player, cost);
        
        // Aplicar o cosmético
        if (particleType != null) {
            pet.setParticleEffect(particleType);
            plugin.getPetManager().savePet(pet);
            
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.cosmetic-purchased")
                    .replace("%cosmetic_name%", cosmeticName)
                    .replace("%pet_name%", pet.getPetName())));
            
            // Atualizar o menu
            openCosmeticsMenu(player, pet);
        }
    }
    
    /**
     * Abre o menu de variantes para o pet
     */
    public void openVariantMenu(Player player, AbstractPet pet) {
        String title = TextUtil.color("&9&lM4Pets &8| &6Aparência");
        Inventory inventory = Bukkit.createInventory(null, 36, title);
        
        // Obter lista de variantes para este tipo de pet
        ConfigurationSection petConfig = plugin.getConfigManager().getMainConfig().getConfigurationSection(
            "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase()
        );
        
        if (petConfig != null) {
            List<String> variants = petConfig.getStringList("variants");
            int slot = 10;
            
            for (String variant : variants) {
                Material icon = Material.valueOf(petConfig.getString("icon", "BARRIER"));
                
                ItemStack item = new ItemStack(icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(TextUtil.color("&6&l" + variant));
                    
                    List<String> lore = new ArrayList<>();
                    
                    if (variant.equalsIgnoreCase(pet.getVariant())) {
                        lore.add(TextUtil.color("&aSelecionado atualmente"));
                    } else {
                        lore.add(TextUtil.color("&7Clique para selecionar esta variante"));
                    }
                    
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                
                inventory.setItem(slot, item);
                
                // Avançar para o próximo slot
                slot++;
                if (slot % 9 == 8) {
                    slot += 2;
                }
            }
        }
        
        // Item de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&lVoltar"));
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(27, backItem);
        
        // Preencher espaços vazios com painéis de vidro
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
        
        // Registrar o pet para callback
        plugin.getGuiManager().registerPetForVariant(player, pet);
        
        player.openInventory(inventory);
    }
    
    /**
     * Processa a seleção de uma variante
     */
    public void handleVariantSelection(Player player, AbstractPet pet, int slot) {
        // Obter lista de variantes para este tipo de pet
        ConfigurationSection petConfig = plugin.getConfigManager().getMainConfig().getConfigurationSection(
            "pets." + pet.getType().getConfigCategory() + "." + pet.getType().name().toLowerCase()
        );
        
        if (petConfig != null) {
            List<String> variants = petConfig.getStringList("variants");
            int currentSlot = 10;
            
            for (String variant : variants) {
                if (currentSlot == slot) {
                    // Aplicar a variante selecionada
                    pet.setVariant(variant);
                    plugin.getPetManager().savePet(pet);
                    
                    player.sendMessage(plugin.formatMessage("&aAparência do pet alterada para: &e" + variant));
                    openPetManagement(player, pet);
                    return;
                }
                
                currentSlot++;
                if (currentSlot % 9 == 8) {
                    currentSlot += 2;
                }
            }
        }
        
        // Se for o botão de voltar
        if (slot == 27) {
            openPetManagement(player, pet);
        }
    }
}
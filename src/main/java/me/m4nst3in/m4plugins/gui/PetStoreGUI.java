package me.m4nst3in.m4plugins.gui;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.util.HeadUtil;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetStoreGUI {
    
    private final M4Pets plugin;
    private final Map<String, Inventory> categoryInventories;
    private final Map<String, Map<Integer, String>> slotMappings; // category -> (slot -> petKey)
    private Inventory mainStoreInventory;
    
    public PetStoreGUI(M4Pets plugin) {
        this.plugin = plugin;
        this.categoryInventories = new HashMap<>();
        this.slotMappings = new HashMap<>();
        createMainStoreInventory();
    }
    
    /**
     * Cria o inventário principal da loja
     */
    private void createMainStoreInventory() {
        ConfigurationSection guiConfig = plugin.getConfigManager().getMainConfig().getConfigurationSection("gui.store");
        if (guiConfig == null) return;
        
        String title = TextUtil.color(guiConfig.getString("title", "&5&l✨ &9&lM4Pets &8&l| &e&l🛒 Loja de Pets"));
        mainStoreInventory = Bukkit.createInventory(null, 27, title);
        
        // Adicionar itens de categoria
        ConfigurationSection categoriesConfig = guiConfig.getConfigurationSection("categories");
        if (categoriesConfig != null) {
            for (String category : categoriesConfig.getKeys(false)) {
                if (!categoriesConfig.getBoolean(category + ".enabled", true)) continue;
                
                ItemStack item;
                String materialStr = categoriesConfig.getString(category + ".material");
                
                // Verificar se é uma custom head
                if (materialStr != null && materialStr.startsWith("HEAD:")) {
                    String textureValue = materialStr.substring(5);
                    item = HeadUtil.getCustomHead(textureValue);
                } else {
                    Material material = Material.valueOf(categoriesConfig.getString(category + ".material", "BARRIER"));
                    item = new ItemStack(material);
                }
                
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(TextUtil.color(categoriesConfig.getString(category + ".name", "&fCategoria")));
                    
                    List<String> lore = new ArrayList<>();
                    for (String line : categoriesConfig.getStringList(category + ".lore")) {
                        lore.add(TextUtil.color(line));
                    }
                    meta.setLore(lore);
                    
                    item.setItemMeta(meta);
                }
                
                int slot = categoriesConfig.getInt(category + ".slot", 0);
                mainStoreInventory.setItem(slot, item);
                
                // Criar inventário para esta categoria se implementada
                if (categoriesConfig.getBoolean(category + ".implemented", false)) {
                    createCategoryInventory(category);
                }
            }
        }
        
        // Adicionar item de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&lVoltar"));
            backItem.setItemMeta(backMeta);
        }
        mainStoreInventory.setItem(26, backItem);
        
        // Preencher espaços vazios com painéis de vidro
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < mainStoreInventory.getSize(); i++) {
            if (mainStoreInventory.getItem(i) == null) {
                mainStoreInventory.setItem(i, filler);
            }
        }
        
        // Garantir criação do inventário de montarias com sufixo correto
        createCategoryInventory("mounts");
    }
    
    /**
     * Cria um inventário para uma categoria específica
     */
    private void createCategoryInventory(String category) {
        String titleSuffix = "";
        if (category.equals("warriors")) {
            titleSuffix = "&c&l⚔ Guerreiros &c&l⚔";
        } else if (category.equals("mounts")) {
            titleSuffix = "&6&l🐎 Montarias &6&l🐎";
        } else if (category.equals("decorative")) {
            titleSuffix = "&d&l🌸 Decorativos 🌸";
        } else {
            titleSuffix = "&e" + category.substring(0, 1).toUpperCase() + category.substring(1);
        }
        
        String title = TextUtil.color("&5&l✨ &9&lM4Pets &8&l| " + titleSuffix);
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        // prepare slot mapping
        Map<Integer, String> mapping = new HashMap<>();

        // Adicionar pets dessa categoria
        // MODIFICAR ESTA LINHA PARA LIDAR COM "mounts" DE FORMA ESPECIAL
        String configCategory = category.equals("mounts") ? "mount" : category;
        ConfigurationSection petsConfig = plugin.getConfigManager().getMainConfig().getConfigurationSection("pets." + configCategory);
        
        if (petsConfig != null) {
            int slot = 10;

            for (String petKey : petsConfig.getKeys(false)) {
                ConfigurationSection petConfig = petsConfig.getConfigurationSection(petKey);
                if (petConfig == null) continue;
                
                String petName = petConfig.getString("name", "Pet");
                Material icon = Material.valueOf(petConfig.getString("icon", "BARRIER"));
                int cost = petConfig.getInt("cost", 1000);
                double baseSpeed = petConfig.getDouble("base-speed", 0.2);
                double baseHealth = petConfig.getDouble("base-health", 20);
                
                ItemStack item = new ItemStack(icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(TextUtil.color("&a&l🐾 " + petName));
                    
                    List<String> lore = new ArrayList<>();
                    lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                    lore.add(TextUtil.color("&7💰 &fPreço: &e&l$" + cost));
                    lore.add(TextUtil.color("&7❤ &fVida base: &c" + (int)baseHealth));
                    
                    if (configCategory.equals("mount")) {
                        lore.add(TextUtil.color("&7⚡ &fVelocidade base: &b" + String.format("%.1f", baseSpeed)));
                    }
                    
                    lore.add("");
                    lore.add(TextUtil.color("&7🌟 &fHabilidade especial (Nível 5):"));
                    String ability = petConfig.getString("level5-ability", "Nenhuma");
                    lore.add(TextUtil.color("&e✨ " + ability));
                    lore.add("");
                    lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
                    lore.add(TextUtil.color("&a&l💳 &6Clique para comprar!"));
                    
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                
                inventory.setItem(slot, item);
                // track slot mapping
                mapping.put(slot, petKey);
                
                // Avançar para o próximo slot
                slot++;
                if (slot % 9 == 8) {
                    slot += 2;
                }
                
                // Limitar a 28 pets por página
                if (slot >= 45) break;
            }
            slotMappings.put(category, mapping);
        } else {
            // ADICIONAR MENSAGEM DE DEBUG
            plugin.getLogger().warning("Não foi encontrada configuração para a categoria: pets." + configCategory);
        }
        
        // Adicionar item de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&l◀ &fVoltar à Loja Principal"));
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&7Retornar ao menu principal"));
            lore.add(TextUtil.color("&7da loja de pets"));
            lore.add("");
            lore.add(TextUtil.color("&e&l👆 &6Clique para voltar"));
            backMeta.setLore(lore);
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
        
        categoryInventories.put(category, inventory);
    }
    
    /**
     * Abre a GUI principal da loja para um jogador
     */
    public void openMainStore(Player player) {
        player.openInventory(mainStoreInventory);
    }
    
    /**
     * Abre uma categoria específica da loja para um jogador
     */
    public void openCategory(Player player, String category) {
        Inventory inventory = categoryInventories.get(category);
        if (inventory != null) {
            player.openInventory(inventory);
        } else {
            // ADICIONAR MENSAGEM DE DEBUG
            plugin.getLogger().warning("Tentativa de abrir categoria não existente: " + category);
            // Tentar criar a categoria se ainda não existe
            createCategoryInventory(category);
            inventory = categoryInventories.get(category);
            if (inventory != null) {
                player.openInventory(inventory);
            } else {
                player.sendMessage(plugin.formatMessage("&cEsta categoria de pets não está disponível no momento."));
            }
        }
    }
    
    /**
     * Processa a compra de um pet
     */
    public void processPetPurchase(Player player, String category, String petKey) {
        // MODIFICAR ESTA LINHA PARA LIDAR COM "mounts" DE FORMA ESPECIAL
        String configCategory = category.equals("mounts") ? "mount" : category;
        ConfigurationSection petConfig = plugin.getConfigManager().getMainConfig()
                .getConfigurationSection("pets." + configCategory + "." + petKey);
        
        if (petConfig == null) {
            player.sendMessage(plugin.formatMessage("&cEste pet não está disponível para compra."));
            return;
        }
        
        int cost = petConfig.getInt("cost", 1000);
        String petName = petConfig.getString("name", "Pet");
        
        // Verificar se o jogador já tem este pet
        try {
            PetType petType = PetType.valueOf(petKey.toUpperCase());
            if (plugin.getPetManager().playerHasPetType(player.getUniqueId(), petType)) {
                player.sendMessage(plugin.formatMessage("&cVocê já possui este pet!"));
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
            
            // Criar o pet para o jogador
            String defaultVariant = petConfig.getStringList("variants").get(0);
            plugin.getPetManager().createPet(player, petType, defaultVariant);
            
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.purchase-successful")
                    .replace("%pet_name%", petName)
                    .replace("%price%", String.valueOf(cost))));
            
            // Fechar a GUI
            player.closeInventory();
            
        } catch (IllegalArgumentException e) {
            player.sendMessage(plugin.formatMessage("&cEste tipo de pet não existe."));
        }
    }
    
    /**
     * Retorna a chave do pet para um slot e categoria
     */
    public String getPetKeyForSlot(String category, int slot) {
        Map<Integer, String> mapping = slotMappings.get(category);
        return mapping != null ? mapping.get(slot) : null;
    }
}
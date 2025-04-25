package me.m4nst3in.m4plugins.gui;

import me.m4nst3in.m4plugins.M4Pets;
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
import java.util.List;

public class MainGUI {
    
    private final M4Pets plugin;
    private Inventory mainInventory;
    
    public MainGUI(M4Pets plugin) {
        this.plugin = plugin;
        createMainInventory();
    }
    
    /**
     * Cria o inventário do menu principal
     */
    private void createMainInventory() {
        ConfigurationSection guiConfig = plugin.getConfigManager().getMainConfig().getConfigurationSection("gui");
        if (guiConfig == null) return;
        
        String title = TextUtil.color(guiConfig.getString("main-title", "&9&lM4Pets &8| &7Menu Principal"));
        mainInventory = Bukkit.createInventory(null, 27, title);
        
        ConfigurationSection itemsConfig = guiConfig.getConfigurationSection("main-items");
        if (itemsConfig != null) {
            // Item da loja de pets
            ConfigurationSection storeConfig = itemsConfig.getConfigurationSection("pet-store");
            if (storeConfig != null) {
                ItemStack storeItem;
                
                // Verificar se usa custom head
                String headTexture = plugin.getConfigManager().getTexturesConfig().getString("gui.main.shop");
                if (headTexture != null) {
                    storeItem = HeadUtil.getCustomHead(headTexture);
                } else {
                    storeItem = new ItemStack(Material.valueOf(storeConfig.getString("material", "CHEST")));
                }
                
                ItemMeta storeMeta = storeItem.getItemMeta();
                if (storeMeta != null) {
                    storeMeta.setDisplayName(TextUtil.color(storeConfig.getString("name", "&e&lLoja de Pets")));
                    
                    List<String> lore = new ArrayList<>();
                    for (String line : storeConfig.getStringList("lore")) {
                        lore.add(TextUtil.color(line));
                    }
                    storeMeta.setLore(lore);
                    
                    storeItem.setItemMeta(storeMeta);
                }
                
                mainInventory.setItem(storeConfig.getInt("slot", 11), storeItem);
            }
            
            // Item de meus pets
            ConfigurationSection myPetsConfig = itemsConfig.getConfigurationSection("my-pets");
            if (myPetsConfig != null) {
                ItemStack myPetsItem;
                
                // Verificar se usa custom head
                String headTexture = plugin.getConfigManager().getTexturesConfig().getString("gui.main.my-pets");
                if (headTexture != null) {
                    myPetsItem = HeadUtil.getCustomHead(headTexture);
                } else {
                    myPetsItem = new ItemStack(Material.valueOf(myPetsConfig.getString("material", "NAME_TAG")));
                }
                
                ItemMeta myPetsMeta = myPetsItem.getItemMeta();
                if (myPetsMeta != null) {
                    myPetsMeta.setDisplayName(TextUtil.color(myPetsConfig.getString("name", "&a&lMeus Pets")));
                    
                    List<String> lore = new ArrayList<>();
                    for (String line : myPetsConfig.getStringList("lore")) {
                        lore.add(TextUtil.color(line));
                    }
                    myPetsMeta.setLore(lore);
                    
                    myPetsItem.setItemMeta(myPetsMeta);
                }
                
                mainInventory.setItem(myPetsConfig.getInt("slot", 15), myPetsItem);
            }
            
            // Item de informações
            ConfigurationSection infoConfig = itemsConfig.getConfigurationSection("info");
            if (infoConfig != null) {
                ItemStack infoItem;
                
                // Verificar se usa custom head
                String headTexture = plugin.getConfigManager().getTexturesConfig().getString("gui.main.info");
                if (headTexture != null) {
                    infoItem = HeadUtil.getCustomHead(headTexture);
                } else {
                    infoItem = new ItemStack(Material.valueOf(infoConfig.getString("material", "BOOK")));
                }
                
                ItemMeta infoMeta = infoItem.getItemMeta();
                if (infoMeta != null) {
                    infoMeta.setDisplayName(TextUtil.color(infoConfig.getString("name", "&b&lInformações")));
                    
                    List<String> lore = new ArrayList<>();
                    for (String line : infoConfig.getStringList("lore")) {
                        lore.add(TextUtil.color(line));
                    }
                    infoMeta.setLore(lore);
                    
                    infoItem.setItemMeta(infoMeta);
                }
                
                mainInventory.setItem(infoConfig.getInt("slot", 22), infoItem);
            }
        }
        
        // Preencher espaços vazios com painéis de vidro
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < mainInventory.getSize(); i++) {
            if (mainInventory.getItem(i) == null) {
                mainInventory.setItem(i, filler);
            }
        }
    }
    
    /**
     * Abre o menu principal para um jogador
     */
    public void openMainMenu(Player player) {
        player.openInventory(mainInventory);
    }
}
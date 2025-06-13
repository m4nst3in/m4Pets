package me.m4nst3in.m4plugins.gui;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PetInfoGUI {
    
    private final M4Pets plugin;
    private Inventory infoInventory;
    
    public PetInfoGUI(M4Pets plugin) {
        this.plugin = plugin;
        createInfoInventory();
    }
    
    /**
     * Cria o inventário de informações
     */
    private void createInfoInventory() {
        String title = TextUtil.color("&5&l✨ &9&lM4Pets &8&l| &b&lℹ Informações ℹ&b&l");
        infoInventory = Bukkit.createInventory(null, 36, title);
        
        // Item para informações gerais
        ItemStack generalItem = new ItemStack(Material.BOOK);
        ItemMeta generalMeta = generalItem.getItemMeta();
        if (generalMeta != null) {
            generalMeta.setDisplayName(TextUtil.color("&b&l📚 Informações Gerais"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(TextUtil.color("&7🎮 &fM4Pets é um sistema completo"));
            lore.add(TextUtil.color("&7   &fde pets para Minecraft!"));
            lore.add("");
            lore.add(TextUtil.color("&7✨ &fRecursos principais:"));
            lore.add(TextUtil.color("&a  🐾 &fPets únicos e personalizáveis"));
            lore.add(TextUtil.color("&a  ⭐ &fSistema de níveis e upgrades"));
            lore.add(TextUtil.color("&a  💎 &fCosméticos e aparências"));
            lore.add(TextUtil.color("&a  ⚔️ &fPets guerreiros de combate"));
            lore.add("");
            lore.add(TextUtil.color("&7👨‍💻 &fDesenvolvido por: &b&lm4nst3in"));
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            
            generalMeta.setLore(lore);
            generalItem.setItemMeta(generalMeta);
        }
        infoInventory.setItem(10, generalItem);
        
        // Item para comandos
        ItemStack commandsItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta commandsMeta = commandsItem.getItemMeta();
        if (commandsMeta != null) {
            commandsMeta.setDisplayName(TextUtil.color("&e&l💻 Comandos Disponíveis"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(TextUtil.color("&7📋 &fLista de comandos do M4Pets:"));
            lore.add("");
            lore.add(TextUtil.color("&e&l/pets"));
            lore.add(TextUtil.color("&7  📂 &fAbre o menu principal"));
            lore.add("");
            lore.add(TextUtil.color("&e&l/pets summon &8<nome>"));
            lore.add(TextUtil.color("&7  🎯 &fInvoca um pet específico"));
            lore.add("");
            lore.add(TextUtil.color("&e&l/pets list"));
            lore.add(TextUtil.color("&7  📜 &fLista todos os seus pets"));
            lore.add("");
            lore.add(TextUtil.color("&e&l/pets reload &8(admin)"));
            lore.add(TextUtil.color("&7  🔄 &fRecarrega a configuração"));
            lore.add("");
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            
            commandsMeta.setLore(lore);
            commandsItem.setItemMeta(commandsMeta);
        }
        infoInventory.setItem(12, commandsItem);
        
        // Item para tipos de pets
        ItemStack typesItem = new ItemStack(Material.SADDLE);
        ItemMeta typesMeta = typesItem.getItemMeta();
        if (typesMeta != null) {
            typesMeta.setDisplayName(TextUtil.color("&6&l🏷️ Tipos de Pets"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(TextUtil.color("&7🎯 &fCategorias disponíveis:"));
            lore.add("");
            lore.add(TextUtil.color("&6&l🐎 Montarias"));
            lore.add(TextUtil.color("&7  🚀 &fPets que você pode montar"));
            lore.add(TextUtil.color("&7  ⚡ &fAumentam sua velocidade"));
            lore.add("");
            lore.add(TextUtil.color("&c&l⚔️ Guerreiros"));
            lore.add(TextUtil.color("&7  🛡️ &fPets que lutam por você"));
            lore.add(TextUtil.color("&7  💪 &fProtegem e defendem"));
            lore.add("");
            lore.add(TextUtil.color("&e&l🔨 Trabalhadores"));
            lore.add(TextUtil.color("&7  ⭐ &f(Em desenvolvimento)"));
            lore.add("");
            lore.add(TextUtil.color("&d&l🌸 Decorativos"));
            lore.add(TextUtil.color("&7  ⭐ &f(Em desenvolvimento)"));
            lore.add("");
            lore.add(TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            
            typesMeta.setLore(lore);
            typesItem.setItemMeta(typesMeta);
        }
        infoInventory.setItem(14, typesItem);
        
        // Item para níveis
        ItemStack levelsItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta levelsMeta = levelsItem.getItemMeta();
        if (levelsMeta != null) {
            levelsMeta.setDisplayName(TextUtil.color("&a&lSistema de Níveis"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&7Cada pet pode ser melhorado até o nível 5"));
            lore.add(TextUtil.color("&7A cada nível, seu pet ficará mais forte:"));
            lore.add("");
            lore.add(TextUtil.color("&7- Mais vida"));
            lore.add(TextUtil.color("&7- Mais velocidade (montarias)"));
            lore.add("");
            lore.add(TextUtil.color("&7No nível 5, seu pet desbloqueia uma"));
            lore.add(TextUtil.color("&7habilidade especial única!"));
            
            levelsMeta.setLore(lore);
            levelsItem.setItemMeta(levelsMeta);
        }
        infoInventory.setItem(16, levelsItem);
        
        // Item para cosméticos
        ItemStack cosmeticsItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta cosmeticsMeta = cosmeticsItem.getItemMeta();
        if (cosmeticsMeta != null) {
            cosmeticsMeta.setDisplayName(TextUtil.color("&d&lCosméticos"));
            
            List<String> lore = new ArrayList<>();
            lore.add(TextUtil.color("&7Personalize seus pets com efeitos"));
            lore.add(TextUtil.color("&7visuais incríveis!"));
            lore.add("");
            lore.add(TextUtil.color("&7- Partículas"));
            lore.add(TextUtil.color("&7- Variantes de cor"));
            lore.add("");
            lore.add(TextUtil.color("&7Compre cosméticos na loja de cada pet"));
            
            cosmeticsMeta.setLore(lore);
            cosmeticsItem.setItemMeta(cosmeticsMeta);
        }
        infoInventory.setItem(22, cosmeticsItem);
        
        // Item de voltar
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&lVoltar"));
            backItem.setItemMeta(backMeta);
        }
        infoInventory.setItem(26, backItem);
        
        // Preencher espaços vazios com painéis de vidro
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < infoInventory.getSize(); i++) {
            if (infoInventory.getItem(i) == null) {
                infoInventory.setItem(i, filler);
            }
        }
    }
    
    /**
     * Abre o menu de informações para um jogador
     */
    public void openInfoMenu(Player player) {
        player.openInventory(infoInventory);
    }
}
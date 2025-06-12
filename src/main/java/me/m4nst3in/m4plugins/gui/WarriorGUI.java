package me.m4nst3in.m4plugins.gui;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.pets.abstractpets.WarriorPet;
import me.m4nst3in.m4plugins.pets.warriors.SkeletonPet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collection;

/**
 * GUI para gerenciar pets guerreiros
 */
public class WarriorGUI {
    
    private final M4Pets plugin;
    
    public WarriorGUI(M4Pets plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Abre o menu principal de pets guerreiros
     */
    public void openWarriorMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Pets Guerreiros");
        
        // Preencher bordas
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            border.setItemMeta(borderMeta);
        }
        
        // Bordas superior e inferior
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(45 + i, border);
        }
        
        // Bordas laterais
        for (int i = 9; i < 45; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }
        
        // Seus pets guerreiros
        Collection<AbstractPet> pets = plugin.getPetManager().getPlayerPets(player.getUniqueId());
        int slot = 10;
        
        for (AbstractPet pet : pets) {
            if (pet instanceof WarriorPet && slot <= 16) {
                WarriorPet warriorPet = (WarriorPet) pet;
                ItemStack petItem = createWarriorPetItem(warriorPet);
                inv.setItem(slot, petItem);
                slot++;
            }
        }
        
        // Botão de voltar
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§cVoltar");
            backMeta.setLore(Arrays.asList("§7Voltar ao menu principal"));
            backButton.setItemMeta(backMeta);
        }
        inv.setItem(49, backButton);
        
        player.openInventory(inv);
    }
    
    /**
     * Abre o menu de controle específico de um pet guerreiro
     */
    public void openWarriorControlMenu(Player player, WarriorPet warriorPet) {
        Inventory inv = Bukkit.createInventory(null, 45, "§8Controle: " + warriorPet.getPetName());
        
        // Preencher bordas
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            border.setItemMeta(borderMeta);
        }
        
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(36 + i, border);
        }
        for (int i = 9; i < 36; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }
        
        // Status do pet
        ItemStack statusItem = createStatusItem(warriorPet);
        inv.setItem(13, statusItem);
        
        // Controle de IA
        ItemStack aiItem = createAIControlItem(warriorPet);
        inv.setItem(19, aiItem);
        
        // Limpar alvo
        ItemStack clearTargetItem = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearTargetItem.getItemMeta();
        if (clearMeta != null) {
            clearMeta.setDisplayName("§cLimpar Alvo");
            clearMeta.setLore(Arrays.asList(
                "§7Remove o alvo atual do pet",
                "§7",
                "§eClique para executar"
            ));
            clearTargetItem.setItemMeta(clearMeta);
        }
        inv.setItem(21, clearTargetItem);
        
        // Habilidade especial (se disponível)
        if (warriorPet.hasLevel5Ability()) {
            ItemStack abilityItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta abilityMeta = abilityItem.getItemMeta();
            if (abilityMeta != null) {
                abilityMeta.setDisplayName("§6Habilidade Especial");
                abilityMeta.setLore(Arrays.asList(
                    "§7" + warriorPet.getLevel5AbilityDescription(),
                    "§7",
                    "§eClique para usar"
                ));
                abilityItem.setItemMeta(abilityMeta);
            }
            inv.setItem(23, abilityItem);
        }
        
        // Modo de combate (apenas para esqueleto)
        if (warriorPet instanceof SkeletonPet) {
            SkeletonPet skeleton = (SkeletonPet) warriorPet;
            ItemStack modeItem = new ItemStack(skeleton.isRangedMode() ? Material.BOW : Material.IRON_SWORD);
            ItemMeta modeMeta = modeItem.getItemMeta();
            if (modeMeta != null) {
                String currentMode = skeleton.isRangedMode() ? "Arqueiro" : "Guerreiro";
                String nextMode = skeleton.isRangedMode() ? "Guerreiro" : "Arqueiro";
                
                modeMeta.setDisplayName("§eModo de Combate");
                modeMeta.setLore(Arrays.asList(
                    "§7Modo atual: §f" + currentMode,
                    "§7",
                    "§eClique para trocar para §f" + nextMode
                ));
                modeItem.setItemMeta(modeMeta);
            }
            inv.setItem(25, modeItem);
        }
        
        // Botão de voltar
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§cVoltar");
            backMeta.setLore(Arrays.asList("§7Voltar ao menu de guerreiros"));
            backButton.setItemMeta(backMeta);
        }
        inv.setItem(40, backButton);
        
        player.openInventory(inv);
    }
    
    /**
     * Cria item representando um pet guerreiro
     */
    private ItemStack createWarriorPetItem(WarriorPet warriorPet) {
        ItemStack item;
        
        // Cabeça baseada no tipo do pet
        switch (warriorPet.getType()) {
            case ZOMBIE:
                item = new ItemStack(Material.ZOMBIE_HEAD);
                break;
            case SKELETON:
                item = new ItemStack(Material.SKELETON_SKULL);
                break;
            case VINDICATOR:
                item = new ItemStack(Material.IRON_AXE);
                break;
            default:
                item = new ItemStack(Material.IRON_SWORD);
                break;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + warriorPet.getPetName());
            
            String status = warriorPet.isDead() ? "§c§lMORTO" : 
                           warriorPet.isSpawned() ? "§a§lATIVO" : "§7§lINATIVO";
            
            String ai = warriorPet.isAIEnabled() ? "§aAtivada" : "§cDesativada";
            String target = warriorPet.getTargetPlayerName() != null ? 
                           "§c" + warriorPet.getTargetPlayerName() : "§7Nenhum";
            
            meta.setLore(Arrays.asList(
                "§7Tipo: §f" + warriorPet.getType().name(),
                "§7Status: " + status,
                "§7Nível: §f" + warriorPet.getLevel(),
                "§7Vida: §c" + (int)warriorPet.getHealth() + "§f/§c" + (int)warriorPet.getMaxHealth(),
                "§7Dano: §f" + warriorPet.getAttackDamage(),
                "§7IA: " + ai,
                "§7Alvo: " + target,
                "§7",
                "§eClique para controlar"
            ));
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Cria item de status do pet
     */
    private ItemStack createStatusItem(WarriorPet warriorPet) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§eStatus do Pet");
            
            String status = warriorPet.isDead() ? "§c§lMORTO" : 
                           warriorPet.isSpawned() ? "§a§lATIVO" : "§7§lINATIVO";
            
            meta.setLore(Arrays.asList(
                "§7Nome: §f" + warriorPet.getPetName(),
                "§7Tipo: §f" + warriorPet.getType().name(),
                "§7Status: " + status,
                "§7Nível: §f" + warriorPet.getLevel(),
                "§7Vida: §c" + (int)warriorPet.getHealth() + "§f/§c" + (int)warriorPet.getMaxHealth(),
                "§7Dano de Ataque: §f" + warriorPet.getAttackDamage(),
                "§7Velocidade de Ataque: §f" + warriorPet.getAttackSpeed(),
                "§7Raio de Defesa: §f" + (int)warriorPet.getDefenseRadius() + " blocos",
                "§7Raio de Ataque: §f" + (int)warriorPet.getAttackRadius() + " blocos"
            ));
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Cria item de controle de IA
     */
    private ItemStack createAIControlItem(WarriorPet warriorPet) {
        Material material = warriorPet.isAIEnabled() ? Material.REDSTONE_TORCH : Material.TORCH;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String status = warriorPet.isAIEnabled() ? "§aAtivada" : "§cDesativada";
            String action = warriorPet.isAIEnabled() ? "Desativar" : "Ativar";
            
            meta.setDisplayName("§eControle de IA");
            meta.setLore(Arrays.asList(
                "§7Status atual: " + status,
                "§7",
                "§7Com IA ativada, o pet irá:",
                "§7• Defender você automaticamente",
                "§7• Atacar alvos definidos",
                "§7• Usar habilidades especiais",
                "§7",
                "§eClique para " + action.toLowerCase()
            ));
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
}

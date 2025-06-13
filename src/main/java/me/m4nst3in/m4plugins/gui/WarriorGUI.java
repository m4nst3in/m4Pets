package me.m4nst3in.m4plugins.gui;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.pets.abstractpets.WarriorPet;
import me.m4nst3in.m4plugins.pets.warriors.SkeletonPet;
import me.m4nst3in.m4plugins.util.TextUtil;

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
        Inventory inv = Bukkit.createInventory(null, 54, TextUtil.color("&8&l⚔️ &c&lPets Guerreiros &8&l⚔️"));
        
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
            backMeta.setDisplayName(TextUtil.color("&c&l◀ &fVoltar ao Menu Principal"));
            backMeta.setLore(Arrays.asList(
                TextUtil.color("&7Retornar ao menu principal"),
                TextUtil.color("&7do M4Pets"),
                "",
                TextUtil.color("&e&l👆 &6Clique para voltar")
            ));
            backButton.setItemMeta(backMeta);
        }
        inv.setItem(49, backButton);
        
        player.openInventory(inv);
    }
    
    /**
     * Abre o menu de controle específico de um pet guerreiro
     */
    public void openWarriorControlMenu(Player player, WarriorPet warriorPet) {
        Inventory inv = Bukkit.createInventory(null, 45, TextUtil.color("&8&l🎮 &f&lControle: " + warriorPet.getPetName()));
        
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
            clearMeta.setDisplayName(TextUtil.color("&c&l❌ Limpar Alvo"));
            clearMeta.setLore(Arrays.asList(
                TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                TextUtil.color("&7🎯 &fRemove o alvo atual do pet"),
                TextUtil.color("&7🧭 &fO pet parará de atacar"),
                TextUtil.color("&7   &fespecificamente alguém"),
                "",
                TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                TextUtil.color("&e&l👆 &6Clique para executar")
            ));
            clearTargetItem.setItemMeta(clearMeta);
        }
        inv.setItem(21, clearTargetItem);
        
        // Habilidade especial (se disponível)
        if (warriorPet.hasLevel5Ability()) {
            ItemStack abilityItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta abilityMeta = abilityItem.getItemMeta();
            if (abilityMeta != null) {
                abilityMeta.setDisplayName(TextUtil.color("&6&l⭐ Habilidade Especial"));
                abilityMeta.setLore(Arrays.asList(
                    TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                    TextUtil.color("&7✨ &fDescrição:"),
                    TextUtil.color("&e" + warriorPet.getLevel5AbilityDescription()),
                    "",
                    TextUtil.color("&7🔥 &fHabilidade de nível 5"),
                    TextUtil.color("&7💫 &fPoder especial único!"),
                    "",
                    TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                    TextUtil.color("&e&l👆 &6Clique para usar")
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
                String currentModeIcon = skeleton.isRangedMode() ? "🏹" : "⚔️";
                String currentMode = skeleton.isRangedMode() ? "&e&lArqueiro" : "&c&lGuerreiro";
                String nextMode = skeleton.isRangedMode() ? "&c&lGuerreiro" : "&e&lArqueiro";
                
                modeMeta.setDisplayName(TextUtil.color("&6&l🔄 &f&lModo de Combate"));
                modeMeta.setLore(Arrays.asList(
                    TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                    TextUtil.color("&7" + currentModeIcon + " &fModo atual: " + currentMode),
                    "",
                    TextUtil.color("&7🏹 &fModo Arqueiro:"),
                    TextUtil.color("&7   &a✓ &fAtaque à distância"),
                    TextUtil.color("&7   &a✓ &fMaior alcance"),
                    TextUtil.color("&7   &c✗ &fMenor dano por tiro"),
                    "",
                    TextUtil.color("&7⚔️ &fModo Guerreiro:"),
                    TextUtil.color("&7   &a✓ &fAtaque corpo a corpo"),
                    TextUtil.color("&7   &a✓ &fMaior dano por hit"),
                    TextUtil.color("&7   &c✗ &fMenor alcance"),
                    "",
                    TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                    TextUtil.color("&e&l👆 &6Clique para trocar para " + nextMode)
                ));
                modeItem.setItemMeta(modeMeta);
            }
            inv.setItem(25, modeItem);
        }
        
        // Botão de voltar
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TextUtil.color("&c&l◀ &fVoltar aos Guerreiros"));
            backMeta.setLore(Arrays.asList(
                TextUtil.color("&7Retornar ao menu principal"),
                TextUtil.color("&7dos pets guerreiros"),
                "",
                TextUtil.color("&e&l👆 &6Clique para voltar")
            ));
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
            meta.setDisplayName(TextUtil.color("&6&l⚔️ &e&l" + warriorPet.getPetName()));
            
            String statusIcon = warriorPet.isDead() ? "💀" : 
                               warriorPet.isSpawned() ? "✅" : "❌";
            String status = warriorPet.isDead() ? "&c&lMORTO" : 
                           warriorPet.isSpawned() ? "&a&lATIVO" : "&7&lINATIVO";
            
            String aiIcon = warriorPet.isAIEnabled() ? "🤖" : "🔴";
            String ai = warriorPet.isAIEnabled() ? "&a&lAtivada" : "&c&lDesativada";
            String targetIcon = warriorPet.getTargetPlayerName() != null ? "🎯" : "🚫";
            String target = warriorPet.getTargetPlayerName() != null ? 
                           "&c&l" + warriorPet.getTargetPlayerName() : "&7&lNenhum";
            
            meta.setLore(Arrays.asList(
                TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                TextUtil.color("&7🏷️ &fTipo: &e&l" + warriorPet.getType().name()),
                TextUtil.color("&7" + statusIcon + " &fStatus: " + status),
                TextUtil.color("&7⭐ &fNível: &b&l" + warriorPet.getLevel()),
                TextUtil.color("&7❤️ &fVida: &c&l" + (int)warriorPet.getHealth() + "&f/&c&l" + (int)warriorPet.getMaxHealth()),
                TextUtil.color("&7⚔️ &fDano: &e&l" + warriorPet.getAttackDamage()),
                TextUtil.color("&7" + aiIcon + " &fIA: " + ai),
                TextUtil.color("&7" + targetIcon + " &fAlvo: " + target),
                "",
                TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                TextUtil.color("&e&l👆 &6Clique para controlar")
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
            meta.setDisplayName(TextUtil.color("&b&l📊 &f&lStatus do Pet"));
            
            String statusIcon = warriorPet.isDead() ? "💀" : 
                               warriorPet.isSpawned() ? "✅" : "❌";
            String status = warriorPet.isDead() ? "&c&lMORTO" : 
                           warriorPet.isSpawned() ? "&a&lATIVO" : "&7&lINATIVO";
            
            meta.setLore(Arrays.asList(
                TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                TextUtil.color("&7🏷️ &fNome: &e&l" + warriorPet.getPetName()),
                TextUtil.color("&7🏷️ &fTipo: &b&l" + warriorPet.getType().name()),
                TextUtil.color("&7" + statusIcon + " &fStatus: " + status),
                TextUtil.color("&7⭐ &fNível: &6&l" + warriorPet.getLevel()),
                "",
                TextUtil.color("&c❤️ &fVida: &c&l" + (int)warriorPet.getHealth() + "&f/&c&l" + (int)warriorPet.getMaxHealth()),
                TextUtil.color("&e⚔️ &fDano de Ataque: &e&l" + warriorPet.getAttackDamage()),
                TextUtil.color("&a⚡ &fVelocidade de Ataque: &a&l" + warriorPet.getAttackSpeed()),
                TextUtil.color("&b🛡️ &fRaio de Defesa: &b&l" + (int)warriorPet.getDefenseRadius() + " &fblocos"),
                TextUtil.color("&d🎯 &fRaio de Ataque: &d&l" + (int)warriorPet.getAttackRadius() + " &fblocos"),
                "",
                TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
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
            String statusIcon = warriorPet.isAIEnabled() ? "🤖" : "🔴";
            String status = warriorPet.isAIEnabled() ? "&a&lAtivada" : "&c&lDesativada";
            String action = warriorPet.isAIEnabled() ? "&c&lDesativar" : "&a&lAtivar";
            
            meta.setDisplayName(TextUtil.color("&6&l🤖 &f&lControle de IA"));
            meta.setLore(Arrays.asList(
                TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                TextUtil.color("&7" + statusIcon + " &fStatus atual: " + status),
                "",
                TextUtil.color("&7🔧 &fCom IA ativada, o pet irá:"),
                TextUtil.color("&7   &a⚡ &fDefender você automaticamente"),
                TextUtil.color("&7   &a🎯 &fAtacar alvos definidos"),
                TextUtil.color("&7   &a✨ &fUsar habilidades especiais"),
                TextUtil.color("&7   &a🧠 &fTomar decisões inteligentes"),
                "",
                TextUtil.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
                TextUtil.color("&e&l👆 &6Clique para " + action.toLowerCase())
            ));
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
}

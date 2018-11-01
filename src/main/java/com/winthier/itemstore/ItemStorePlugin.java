package com.winthier.itemstore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemStorePlugin extends JavaPlugin {
    private YamlConfiguration storedItems;

    public ItemStack getStoredItem(final String s) {
        final ItemStack itemStack = this.getStoredItems(true).getItemStack(s);
        if (itemStack == null) {
            return new ItemStack(Material.DIRT);
        }
        return itemStack.clone();
    }

    private YamlConfiguration getStoredItems(final boolean b) {
        if (b || this.storedItems == null) {
            this.getDataFolder().mkdirs();
            this.storedItems = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "items.yml"));
        }
        return this.storedItems;
    }

    private void saveStoredItems() {
        this.getStoredItems(false);
        final File file = new File(this.getDataFolder(), "items.yml");
        try {
            this.storedItems.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CommandException("I/O error. See console for more details.");
        }
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] array) {
        final Player player = (commandSender instanceof Player) ? (Player)commandSender : null;
        try {
            if (array.length == 0) {
                return false;
            }
            if ("StoreItem".equalsIgnoreCase(s) && array.length == 1) {
                if (player == null) {
                    throw new CommandException("Player expected");
                }
                final String s2 = array[0];
                if (!s2.matches("[a-zA-Z0-9-_]+")) {
                    throw new CommandException("Invalid item name: " + s2);
                }
                final ItemStack itemInHand = player.getItemInHand();
                if (itemInHand.getType() == Material.AIR) {
                    throw new CommandException("No item in your hand");
                }
                this.getStoredItems(true).set(s2, (Object)itemInHand.clone());
                this.saveStoredItems();
                player.sendMessage("" + ChatColor.YELLOW + "Item stored");
            } else if ("LoadItem".equalsIgnoreCase(s) && array.length == 1) {
                if (player == null) {
                    throw new CommandException("Player expected");
                }
                final String s3 = array[0];
                if (!s3.matches("[a-zA-Z0-9-_]+")) {
                    throw new CommandException("Invalid item name: " + s3);
                }
                final ItemStack itemStack = this.getStoredItems(true).getItemStack(s3);
                if (itemStack == null) {
                    throw new CommandException("Item not found: " + s3);
                }
                player.getInventory().addItem(new ItemStack[] {itemStack.clone()});
                player.sendMessage("" + ChatColor.YELLOW + "Item given");
            } else if ("FixBook".equalsIgnoreCase(s)) {
                final ItemStack itemInHand2 = player.getItemInHand();
                final BookMeta itemMeta = (BookMeta)itemInHand2.getItemMeta();
                final List pages = itemMeta.getPages();
                final ArrayList pages2 = new ArrayList<Object>(pages.size() / 2);
                for (int i = 0; i < pages.size() / 2; ++i) {
                    pages2.add(pages.get(i));
                }
                itemMeta.setPages((List)pages2);
                itemInHand2.setItemMeta((ItemMeta)itemMeta);
                commandSender.sendMessage("Book fixed");
            } else {
                if (!"ListItems".equalsIgnoreCase(s) || array.length != 0) {
                    return false;
                }
                final StringBuilder sb = new StringBuilder("Stored items:");
                final Iterator<String> iterator = this.getStoredItems(true).getKeys(false).iterator();
                while (iterator.hasNext()) {
                    sb.append(" ").append(iterator.next());
                }
                commandSender.sendMessage("" + ChatColor.YELLOW + sb.toString());
            }
        } catch (CommandException ex) {
            commandSender.sendMessage("" + ChatColor.RED + ex.getMessage());
        }
        return true;
    }
}

package com.winthier.itemstore;

import java.io.File;
import java.io.IOException;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

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
        final Player player = (commandSender instanceof Player) ? (Player) commandSender : null;
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
                final ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.getType() == Material.AIR) {
                    throw new CommandException("No item in your hand");
                }
                this.getStoredItems(true).set(s2, itemInHand.clone());
                this.saveStoredItems();
                player.sendMessage(text("Item stored", YELLOW));
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
                player.sendMessage(text("Item given", YELLOW));
            } else {
                return false;
            }
        } catch (CommandException ex) {
            commandSender.sendMessage(text(ex.getMessage(), RED));
        }
        return true;
    }
}

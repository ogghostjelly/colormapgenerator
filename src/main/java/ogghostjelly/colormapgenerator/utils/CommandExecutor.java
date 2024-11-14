package ogghostjelly.colormapgenerator.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayDeque;
import java.util.function.Consumer;

public class CommandExecutor {
    private final ArrayDeque<Consumer<MinecraftClient>> queue = new ArrayDeque<>();
    private long cooldown = 0;

    public void cancelAll() {
        this.queue.clear();
        this.cooldown = 0;
    }

    public void add(Consumer<MinecraftClient> command) {
        this.queue.add(command);
    }

    public CommandExecutor(int cooldownTime) {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> this.cancelAll());

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            long time = System.currentTimeMillis();
            if ((time - cooldown) < cooldownTime) {
                return;
            }
            cooldown = time;

            if (queue.isEmpty()) {
                return;
            }

            Consumer<MinecraftClient> command = queue.pop();
            command.accept(client);
        });
    }
}

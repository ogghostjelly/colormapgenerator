package ogghostjelly.colormapgenerator.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayDeque;
import java.util.function.BiConsumer;

// TODO: command executor can just execute string commands

public class CommandExecutor<T> {
    private final ArrayDeque<T> queue = new ArrayDeque<>();
    private long cooldown = 0;

    public void cancelAll() {
        this.queue.clear();
        this.cooldown = 0;
    }

    public void add(T args) {
        this.queue.add(args);
    }

    public CommandExecutor(int cooldownTime, BiConsumer<MinecraftClient, T> consumer) {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            this.cancelAll();
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            long time = System.currentTimeMillis();
            if ((time - cooldown) < cooldownTime) {
                return;
            }
            cooldown = time;

            if (queue.isEmpty()) {
                return;
            }

            T args = queue.pop();
            consumer.accept(client, args);
        });
    }
}

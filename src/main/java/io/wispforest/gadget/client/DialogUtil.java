package io.wispforest.gadget.client;

import io.wispforest.gadget.Gadget;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLProperties;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class DialogUtil {
    // SDL retains the callback and filter pointers, but does not keep their Java objects alive.
    private static final Set<PendingDialog> PENDING_DIALOGS = new HashSet<>();

    private DialogUtil() {

    }

    public static CompletableFuture<List<String>> openFileDialog(String title, @Nullable String defaultPath, @Nullable List<String> patterns, @Nullable String filterDesc, boolean allowMultipleSelections) {
        return showFileDialog(SDLDialog.SDL_FILEDIALOG_OPENFILE, title, defaultPath, patterns, filterDesc, allowMultipleSelections);
    }

    public static CompletableFuture<String> saveFileDialog(String title, @Nullable String defaultPath, @Nullable List<String> patterns, @Nullable String filterDesc) {
        return showFileDialog(SDLDialog.SDL_FILEDIALOG_SAVEFILE, title, defaultPath, patterns, filterDesc, false)
            .thenApply(paths -> paths.isEmpty() ? null : paths.getFirst());
    }

    private static CompletableFuture<List<String>> showFileDialog(int type, String title, @Nullable String defaultPath, @Nullable List<String> patterns, @Nullable String filterDesc, boolean allowMultipleSelections) {
        Minecraft client = Minecraft.getInstance();
        PendingDialog dialog = new PendingDialog();

        List<String> filters = patterns == null ? List.of() : List.copyOf(patterns);
        client.execute(() -> {
            PENDING_DIALOGS.add(dialog);
            try {
                dialog.properties = SDLProperties.SDL_CreateProperties();
                if (dialog.properties == 0) throw new IllegalStateException(SDLError.SDL_GetError());

                checkProperty(SDLProperties.SDL_SetStringProperty(dialog.properties, SDLDialog.SDL_PROP_FILE_DIALOG_TITLE_STRING, title));
                checkProperty(SDLProperties.SDL_SetPointerProperty(dialog.properties, SDLDialog.SDL_PROP_FILE_DIALOG_WINDOW_POINTER, client.getWindow().handle()));
                checkProperty(SDLProperties.SDL_SetBooleanProperty(dialog.properties, SDLDialog.SDL_PROP_FILE_DIALOG_MANY_BOOLEAN, allowMultipleSelections));

                if (defaultPath != null) {
                    checkProperty(SDLProperties.SDL_SetStringProperty(dialog.properties, SDLDialog.SDL_PROP_FILE_DIALOG_LOCATION_STRING, defaultPath));
                }

                if (!filters.isEmpty()) {
                    // SDL accepts extensions separated by semicolons instead of TinyFD's *.ext globs.
                    String pattern = String.join(";", filters.stream().map(value -> value.startsWith("*.") ? value.substring(2) : value).toList());
                    dialog.filterName = MemoryUtil.memUTF8(filterDesc == null ? String.join(", ", filters) : filterDesc);
                    dialog.filterPattern = MemoryUtil.memUTF8(pattern);
                    dialog.filter = SDL_DialogFileFilter.calloc(1);
                    dialog.filter.get(0).name(dialog.filterName).pattern(dialog.filterPattern);
                    checkProperty(SDLProperties.SDL_SetPointerProperty(dialog.properties, SDLDialog.SDL_PROP_FILE_DIALOG_FILTERS_POINTER, dialog.filter.address()));
                    checkProperty(SDLProperties.SDL_SetNumberProperty(dialog.properties, SDLDialog.SDL_PROP_FILE_DIALOG_NFILTERS_NUMBER, 1));
                }

                dialog.callback = SDL_DialogFileCallback.create((_, fileList, _) -> {
                    List<String> paths = new ArrayList<>();
                    RuntimeException error = null;
                    try {
                        if (fileList == 0) throw new IllegalStateException(SDLError.SDL_GetError());

                        // SDL frees these strings when the callback returns, possibly on another thread.
                        for (long pointer = fileList; ; pointer += Pointer.POINTER_SIZE) {
                            long path = MemoryUtil.memGetAddress(pointer);
                            if (path == 0) break;

                            paths.add(MemoryUtil.memUTF8(path));
                        }
                    } catch (RuntimeException exception) {
                        error = exception;
                    }
                    RuntimeException failure = error;
                    // execute may run inline inside the native callback; schedule always queues cleanup.
                    client.schedule(() -> dialog.complete(paths, failure));
                });
                SDLDialog.SDL_ShowFileDialogWithProperties(type, dialog.callback, 0, dialog.properties);
            } catch (RuntimeException exception) {
                dialog.complete(List.of(), exception);
            }
        });

        return dialog.result;
    }

    private static void checkProperty(boolean success) {
        if (!success) throw new IllegalStateException(SDLError.SDL_GetError());
    }

    private static final class PendingDialog {
        private final CompletableFuture<List<String>> result = new CompletableFuture<>();
        private int properties;
        private SDL_DialogFileCallback callback;
        private SDL_DialogFileFilter.Buffer filter;
        private ByteBuffer filterName;
        private ByteBuffer filterPattern;

        private void complete(List<String> paths, @Nullable RuntimeException error) {
            if (!PENDING_DIALOGS.remove(this)) return;

            if (callback != null) callback.free();
            if (properties != 0) SDLProperties.SDL_DestroyProperties(properties);
            if (filter != null) filter.free();

            MemoryUtil.memFree(filterName);
            MemoryUtil.memFree(filterPattern);

            if (error == null) {
                result.complete(List.copyOf(paths));
            } else {
                Gadget.LOGGER.error("File dialog failed", error);
                result.completeExceptionally(error);
            }
        }
    }
}

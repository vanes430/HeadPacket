package github.vanes430.headpacket.common;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.Path;
import java.util.ArrayList;

public class ImageProcessor {
    private final MineSkinClient client;
    private final TextureCache cache;
    private final File dataDir;
    private final java.util.function.BiFunction<String, Boolean, String> messageProvider;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final HeadPacketPlugin plugin;

    public ImageProcessor(MineSkinClient c, TextureCache t, File d, java.util.function.BiFunction<String, Boolean, String> m, HeadPacketPlugin p) {
        this.client = c; this.cache = t; this.dataDir = d; this.messageProvider = m; this.plugin = p;
    }

    public CompletableFuture<List<List<String>>> process(File file, HeadPacketSender sender, int pct) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BufferedImage img = ImageIO.read(file);
                int w = img.getWidth();
                int h = img.getHeight();
                if (w > 264 || w % 8 != 0 || (h != 8 && h != 16)) {
                    sender.sendMessage(messageProvider.apply("image-invalid-size", true)
                            .replace("{width}", String.valueOf(w))
                            .replace("{height}", String.valueOf(h)));
                    throw new RuntimeException("Invalid image dimensions");
                }
                List<List<ImageSlicer.Segment>> rows = ImageSlicer.slice(file);

                int total = rows.stream().mapToInt(r -> (int) Math.ceil(r.size() * (pct / 100.0))).sum();
                sender.sendMessage(messageProvider.apply("detection-report", true)
                        .replace("{width}", String.valueOf(img.getWidth()))
                        .replace("{height}", String.valueOf(img.getHeight())));
                sender.sendMessage(messageProvider.apply("processing-report", true)
                        .replace("{percentage}", String.valueOf(pct))
                        .replace("{total}", String.valueOf(total)));
                return processRows(rows, sender, pct, total);
            } catch (Exception e) { throw new RuntimeException(e); }
        }, executor);
    }

    private List<List<String>> processRows(List<List<ImageSlicer.Segment>> rows, HeadPacketSender s, int pct, int total) throws Exception {
        List<List<String>> rowUrls = new ArrayList<>();
        int count = 0;
        for (var segments : rows) {
            List<String> urls = new ArrayList<>();
            int limit = (int) Math.ceil(segments.size() * (pct / 100.0));
            for (int i = 0; i < limit; i++) {
                count++;
                urls.add(processSegment(segments.get(i), s, count, total));
            }
            if (!urls.isEmpty()) rowUrls.add(urls);
        }
        return rowUrls;
    }

    private String processSegment(ImageSlicer.Segment seg, HeadPacketSender s, int cur, int total) throws Exception {
        String cached = cache.get(seg.hash());
        if (cached != null) {
            s.sendMessage(messageProvider.apply("layer-cached", false).replace("{current}", String.valueOf(cur)).replace("{total}", String.valueOf(total)));
            return cached;
        }
        s.sendMessage(messageProvider.apply("layer-uploading", false).replace("{current}", String.valueOf(cur)).replace("{total}", String.valueOf(total)));
        Path temp = ImageSlicer.saveTempSegment(seg.image(), seg.hash(), dataDir);
        try {
            String url = client.upload(temp).join();
            cache.put(seg.hash(), url);
            s.sendMessage(messageProvider.apply("layer-done", false).replace("{current}", String.valueOf(cur)));
            int delay = plugin.getMineSkinDelay();
            if (delay > 0) Thread.sleep(delay);
            return url;
        } finally { temp.toFile().delete(); }
    }

    public void shutdown() { executor.shutdown(); }
}

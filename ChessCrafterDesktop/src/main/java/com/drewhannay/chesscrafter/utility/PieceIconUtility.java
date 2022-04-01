package com.drewhannay.chesscrafter.utility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class PieceIconUtility {

    private static final String TAG = "PieceIconUtility";
    private static final Map<String, Icon> ICON_CACHE = new HashMap<>();

    @Nullable
    public static Icon getPieceIcon(String internalId, Color teamColor) {
        String key = getKey(internalId, teamColor);
        if (ICON_CACHE.containsKey(key)) {
            return ICON_CACHE.get(key);
        }

        BufferedImage pieceImage = ImageUtility.getPieceImage(internalId);
        long start = System.currentTimeMillis();
        BufferedImage mask = generateMask(pieceImage, teamColor, 0.5f);
        Log.d(TAG, "GenerateMask took:" + String.valueOf(System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        BufferedImage tintedImage = tint(pieceImage, mask);
        Log.d(TAG, "Tint took:" + String.valueOf(System.currentTimeMillis() - start));
        Icon icon = new StretchIcon(tintedImage, true);
        ICON_CACHE.put(key, icon);
        return icon;
    }

    public static void invalidateCache(@NotNull String internalId) {
        Map<String, Icon> iconCache = ICON_CACHE.entrySet().stream()
                .filter(p -> !p.getKey().startsWith(internalId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        ICON_CACHE.clear();
        ICON_CACHE.putAll(iconCache);
    }

    private static String getKey(String pieceName, Color teamColor) {
        return pieceName + String.valueOf(teamColor.getRGB());
    }

    private static BufferedImage generateMask(BufferedImage imgSource, Color color, float alpha) {
        int imgWidth = imgSource.getWidth();
        int imgHeight = imgSource.getHeight();

        BufferedImage imgMask = createCompatibleImage(imgWidth, imgHeight, Transparency.TRANSLUCENT);
        Graphics2D g2 = imgMask.createGraphics();
        applyQualityRenderingHints(g2);

        g2.drawImage(imgSource, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
        g2.setColor(color);

        g2.fillRect(0, 0, imgSource.getWidth(), imgSource.getHeight());
        g2.dispose();

        return imgMask;
    }

    private static BufferedImage tint(BufferedImage master, BufferedImage tint) {
        BufferedImage tinted = createCompatibleImage(master.getWidth(), master.getHeight(), Transparency.TRANSLUCENT);
        Graphics2D g2 = tinted.createGraphics();
        applyQualityRenderingHints(g2);
        g2.drawImage(master, 0, 0, null);
        g2.drawImage(tint, 0, 0, null);
        g2.dispose();

        return tinted;
    }

    private static void applyQualityRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static BufferedImage createCompatibleImage(int width, int height, int transparency) {
        BufferedImage image = getGraphicsConfiguration().createCompatibleImage(width, height, transparency);
        image.coerceData(true);
        return image;
    }

    private static GraphicsConfiguration getGraphicsConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }
}

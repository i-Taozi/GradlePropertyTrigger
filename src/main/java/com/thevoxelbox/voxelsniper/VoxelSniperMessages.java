/*
 * This file is part of VoxelSniper, licensed under the MIT License (MIT).
 *
 * Copyright (c) The VoxelBox <http://thevoxelbox.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.thevoxelbox.voxelsniper;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class VoxelSniperMessages {

    public static Text NO_BRUSH;
    public static Template CURRENT_TOOL;
    public static Template UNDO_SUCCESSFUL;
    public static Text NOTHING_TO_UNDO;
    public static Text BRUSH_ERROR;
    public static Text SNIPE_TARGET_NOT_VISIBLE;
    public static Template BRUSH_PERMISSION_ERROR;

    public static void reload() {
        NO_BRUSH = TextSerializers.FORMATTING_CODE.deserialize(VoxelSniperConfiguration.MESSAGE_NO_BRUSH);
        CURRENT_TOOL = new Template(VoxelSniperConfiguration.MESSAGE_CURRENT_TOOL);
        UNDO_SUCCESSFUL = new Template(VoxelSniperConfiguration.MESSAGE_UNDO_SUCCESSFUL);
        NOTHING_TO_UNDO = TextSerializers.FORMATTING_CODE.deserialize(VoxelSniperConfiguration.MESSAGE_NOTHING_TO_UNDO);
        BRUSH_ERROR = TextSerializers.FORMATTING_CODE.deserialize(VoxelSniperConfiguration.MESSAGE_BRUSH_ERROR);
        SNIPE_TARGET_NOT_VISIBLE = TextSerializers.FORMATTING_CODE.deserialize(VoxelSniperConfiguration.MESSAGE_SNIPE_TARGET_NOT_VISIBLE);
        BRUSH_PERMISSION_ERROR = new Template(VoxelSniperConfiguration.MESSAGE_BRUSH_PERMISSION_ERROR);
    }

    public static class Template {
        private String msg;
        
        public Template(String msg) {
            this.msg = msg;
        }
        
        public Text create(String... args) {
            String msg = this.msg;
            for (int i = 0; i < args.length; i++) {
                msg = msg.replace("{" + i + "}", args[i]);
            }
            return TextSerializers.FORMATTING_CODE.deserialize(msg);
        }
    }
    
}

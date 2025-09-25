package com.tool.looseprince.client.screens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class CodexScreens {
    private CodexScreens() {}

    public static void openMysticTome() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        mc.setScreen(new MysticTomeScreen());
    }

    public static class MysticTomeScreen extends Screen {
        private ContentWidget content;
        private java.util.List<Text> categories;
        private java.util.List<String> categoryIds;
        private int selectedIndex;
        private int listScroll;
		private int listX, listY, listW, listH, itemH;
		private int uiX, uiY, uiW, uiH;
        protected MysticTomeScreen() {
            super(Text.translatable("screen.looseprinces-tool.codex.title"));
        }

        @Override
        public boolean shouldPause() {
            return false;
        }

        @Override
		protected void init() {
			int gap = 6;
			this.itemH = 22;
			// 限制界面最大尺寸（窗口小于该尺寸时自适应）
			int maxW = 640;
			int maxH = 360;
			this.uiW = Math.min(this.width - gap * 2, maxW);
			this.uiH = Math.min(this.height - gap * 2, maxH);
			this.uiX = (this.width - this.uiW) / 2;
			this.uiY = (this.height - this.uiH) / 2;

			this.listW = 160;
			this.listX = this.uiX + gap;
			this.listY = this.uiY + gap + 18;
			this.listH = this.uiH - gap * 2 - 18;
			int contentX = this.listX + this.listW + gap;
			int contentY = this.listY;
			int contentW = this.uiX + this.uiW - gap - contentX;
			int contentH = this.listH;
			this.content = new ContentWidget(contentX, contentY, contentW, contentH);
            buildCategories();
            if (!categories.isEmpty()) {
                selectedIndex = 0;
                content.setLines(buildContentFor(categoryIds.get(0)));
            }
        }

        @Override
		public void render(DrawContext context, int mouseX, int mouseY, float delta) {
			this.renderBackground(context, mouseX, mouseY, delta);
			// panels background within bounded UI
			context.fill(this.uiX, this.uiY, this.uiX + this.uiW, this.uiY + this.uiH, 0x66000000);
			context.fill(this.listX, this.listY - 18, this.listX + this.listW, this.listY + this.listH, 0x55000000);
			// left header: 词条目录
			int headerColor = 0xFFFFFF;
			context.drawText(this.textRenderer, Text.translatable("screen.looseprinces-tool.codex.catalog"), this.listX + 6, this.listY - 14, headerColor, false);
            int visible = this.listH / this.itemH;
            int start = Math.max(0, this.listScroll / this.itemH);
            int end = Math.min(categories.size(), start + visible + 1);
            int y = this.listY - (this.listScroll % this.itemH);
            for (int i = start; i < end; i++) {
                int bg = i == selectedIndex ? 0x5500AAFF : 0x55000000;
                context.fill(this.listX, y, this.listX + this.listW, y + this.itemH - 2, bg);
                Text label = categories.get(i);
                context.drawText(this.textRenderer, label, this.listX + 6, y + 6, 0xE0E0E0, false);
                y += this.itemH;
            }
            // right content
            if (this.content != null) this.content.render(context, mouseX, mouseY, delta);
            // title
			context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.uiX + this.uiW / 2, this.uiY + 6, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                if (mouseX >= this.listX && mouseX < this.listX + this.listW && mouseY >= this.listY && mouseY < this.listY + this.listH) {
                    int relY = (int)mouseY - this.listY + (this.listScroll % this.itemH);
                    int indexInView = relY / this.itemH;
                    int base = Math.max(0, this.listScroll / this.itemH);
                    int idx = base + indexInView;
                    if (idx >= 0 && idx < categories.size()) {
                        this.selectedIndex = idx;
                        this.content.setLines(buildContentFor(categoryIds.get(idx)));
                        return true;
                    }
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (mouseX >= this.listX && mouseX < this.listX + this.listW && mouseY >= this.listY && mouseY < this.listY + this.listH) {
                int max = Math.max(0, categories.size() * itemH - listH);
                this.listScroll = (int)Math.max(0, Math.min(max, this.listScroll - verticalAmount * 12));
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        private void buildCategories() {
            this.categories = new java.util.ArrayList<>();
            this.categoryIds = new java.util.ArrayList<>();
            // self
            this.categories.add(Text.translatable("item.looseprinces-tool.mystic_tome"));
            this.categoryIds.add("self");
            // 从玩家持久化数据读取 ids，并在注册表中查找词条定义
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null && mc.getServer() != null) {
                try {
                    var st = com.tool.looseprince.state.CodexState.get(mc.getServer().getPlayerManager().getPlayer(mc.player.getUuid()));
                    java.util.Set<String> ids = st.getUnlockedEntries();
                    for (String id : ids) {
                        var entry = com.tool.looseprince.codex.CodexRegistry.get(id);
                        if (entry != null) {
                            this.categories.add(entry.getTitle());
                            this.categoryIds.add(id);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        private java.util.List<String> buildContentFor(String id) {
            java.util.List<String> lines = new java.util.ArrayList<>();
            var entry = com.tool.looseprince.codex.CodexRegistry.get(id);
            if (entry != null) {
                lines.add(entry.getTitle().getString());
                lines.addAll(entry.getContentLines());
                return lines;
            }
            lines.add(id); return lines;
        }

        class ContentWidget {
            private final int x, y, w, h;
            private java.util.List<net.minecraft.text.OrderedText> cached = java.util.Collections.emptyList();
            private int scroll;
            ContentWidget(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
            void setLines(java.util.List<String> lines) {
                java.util.List<net.minecraft.text.OrderedText> out = new java.util.ArrayList<>();
                for (String s : lines) {
                    out.addAll(MysticTomeScreen.this.textRenderer.wrapLines(Text.literal(s), w - 10));
                }
                this.cached = out;
                this.scroll = 0;
            }
            void render(DrawContext context, int mouseX, int mouseY, float delta) {
                context.fill(this.x, this.y, this.x + this.w, this.y + this.h, 0xAA000000);
                int ty = this.y + 6 - this.scroll;
                for (net.minecraft.text.OrderedText t : this.cached) {
                    context.drawText(MysticTomeScreen.this.textRenderer, t, this.x + 6, ty, 0xFFFFFF, false);
                    ty += 10;
                    if (ty > this.y + this.h) break;
                }
            }
        }
    }
}



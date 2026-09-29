package cn.blockforge.generated.steelballspin.client;

import cn.blockforge.generated.steelballspin.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class SpinWheelScreen extends Screen {
    private int selected;
    public SpinWheelScreen() { super(Text.translatable("screen.steel_ball_spin.wheel")); }
    @Override protected void init() { selected=SpinMode.get(SpinClient.held(client)).ordinal(); }
    private int segment(double dx,double dy) {
        return Math.floorMod((int)Math.floor((Math.atan2(dy,dx)+Math.PI/2+Math.PI/4)/(Math.PI/2)),4);
    }
    @Override public void render(DrawContext draw,int mouseX,int mouseY,float delta) {
        draw.fill(0,0,width,height,0xA509100F);
        int cx=width/2,cy=height/2;
        int outer=Math.min(104,Math.min(width/2-8,height/2-30));
        outer=Math.max(48,outer);
        int inner=(int)(outer*.50);
        double dx=mouseX-cx,dy=mouseY-cy;
        double d=Math.sqrt(dx*dx+dy*dy);
        if (d>inner && d<outer+16) selected=segment(dx,dy);
        for (int y=-outer;y<=outer;y+=2) for (int x=-outer;x<=outer;x+=2) {
            double radius=Math.sqrt(x*x+y*y);
            if (radius<inner || radius>outer) continue;
            int index=segment(x,y);
            int rgb=SpinMode.from(index).color;
            int alpha=index==selected ? 0xC8000000 : 0x38000000;
            if (radius>outer-3 || radius<inner+2) alpha=0xEE000000;
            draw.fill(cx+x,cy+y,cx+x+2,cy+y+2,alpha|rgb);
        }
        for (int i=0;i<4;i++) {
            double angle=i*Math.PI/2-Math.PI/2;
            int lx=cx+(int)(Math.cos(angle)*outer*.75);
            int ly=cy+(int)(Math.sin(angle)*outer*.75);
            draw.drawCenteredTextWithShadow(textRenderer,SpinMode.from(i).title(),lx,ly-4,i==selected ? 0xFFFFFF : 0xB7C6BD);
            draw.drawCenteredTextWithShadow(textRenderer,Integer.toString(i+1),lx,ly+8,SpinMode.from(i).color);
        }
        draw.drawCenteredTextWithShadow(textRenderer,Text.translatable("screen.steel_ball_spin.center"),cx,cy-12,0xFFD35E);
        draw.drawCenteredTextWithShadow(textRenderer,SpinMode.from(selected).title(),cx,cy+4,SpinMode.from(selected).color);
        draw.drawCenteredTextWithShadow(textRenderer,title,cx,cy-outer-22,0xF4E8B3);
        draw.drawCenteredTextWithShadow(textRenderer,Text.translatable("screen.steel_ball_spin.controls"),cx,cy+outer+14,0x95A49C);
    }
    private void choose(int index) {
        if (client.player!=null && !SpinClient.held(client).isEmpty() && ClientPlayNetworking.canSend(ModePayload.ID))
            ClientPlayNetworking.send(new ModePayload(index));
        close();
    }
    @Override public boolean mouseClicked(double x,double y,int button) {
        if (button==0) { choose(selected); return true; }
        if (button==1) { close(); return true; }
        return super.mouseClicked(x,y,button);
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers) {
        if (key>=GLFW.GLFW_KEY_1 && key<=GLFW.GLFW_KEY_4) { choose(key-GLFW.GLFW_KEY_1); return true; }
        if (key==GLFW.GLFW_KEY_ENTER || SpinClient.wheelKey.matchesKey(key,scan)) { choose(selected); return true; }
        return super.keyPressed(key,scan,modifiers);
    }
    @Override public boolean shouldPause() { return false; }
}

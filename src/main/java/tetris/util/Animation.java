package tetris.util;

import java.awt.*;
import javax.swing.*;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


public class Animation extends JPanel {
    final int delay = 16;
    
    public float alpha = 0.f;

    public float scaleX = 1.f;
    public float scaleY = 1.f;

    public float rotate = 0.f;

    public float offsetX = 0.f;
    public float offsetY = 0.f;

    public boolean bVisible = false;
    
    public int borderRadius = 0;
    public int borderThickness = 0;
    public float[] borderHSB = new float[3];
    public float[] backgroundHSB = new float[3];

    public Animation() { 
        setOpaque(false);
        counter.add(this);
    }

    public void release() {
        if(animTimers != null) {
            for(AnimTimer animTimer: animTimers) {
                animTimer.timer.stop();
                animTimer.timer = null;
            }
            animTimers.clear();
            animTimers = null;
        }
        counter.remove(this);
    }

    static public void clear() {
        System.out.println("미해제 Animation 객체 " + counter.size() + "개 해제");
        if (counter.isEmpty()) return;
        for(Animation anim : counter) {
            anim.release();
        }
        counter.clear();
    }
    static List<Animation> counter = new CopyOnWriteArrayList<>();

    class AnimTimer {
        Timer timer;
        long startTime = -1L;
        long getElapsedNanos() {
            return System.nanoTime() - startTime;
        }
    }
    List<AnimTimer> animTimers = new ArrayList<>();
    AnimTimer addAnimTimer() {
        AnimTimer animTimer = new AnimTimer();
        animTimers.add(animTimer);
        return animTimer;
    }
    void deleteAnimTimer(AnimTimer animTimer) {
        animTimer.timer.stop();
        animTimers.remove(animTimer);
    }

    public void stop() {
        if(animTimers != null) {
            for(AnimTimer animTimer: animTimers) {
                animTimer.timer.stop();
                animTimer.timer = null;
            }
            animTimers.clear();
            //animTimers = null;
        }
    }

    public void hueBackground(float duration, boolean bLoop) {
        alpha = 1f;
        bVisible = true;
        scaleX = 1f;
        scaleY = 1f;

        backgroundHSB[1] = .5f;
        backgroundHSB[2] = .5f;

        final long durationNanos = secToNanos(duration);

        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();
        animTimer.timer = new Timer(delay, e -> {
            long elapsed = animTimer.getElapsedNanos();
            float tp = getTimeProgress(elapsed, durationNanos);

            backgroundHSB[0] = tp % 1f;

            if(!bLoop && tp >= 1f) {
                ((Timer)e.getSource()).stop();
            }
            repaint();
        });
        animTimer.timer.start();
    }


    public void hueBorder(float duration, boolean bLoop) {
        alpha = 1f;
        bVisible = true;
        scaleX = 1f;
        scaleY = 1f;

        borderHSB[1] = .8f;
        borderHSB[2] = .8f;

        final long durationNanos = secToNanos(duration);
        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();
        animTimer.timer = new Timer(delay, e -> {

            long elapsed = animTimer.getElapsedNanos();
            float tp = getTimeProgress(elapsed, durationNanos);

            borderHSB[0] = tp % 1f;

            if(!bLoop && tp >= 1f) {
                ((Timer)e.getSource()).stop();
            }

            repaint();
        });
        animTimer.timer.start();
    }

    public void saturateBorder(float duration, boolean bLoop) {
        alpha = 1f;
        bVisible = true;
        scaleX = 1f;
        scaleY = 1f;
    
        final long durationNanos = secToNanos(duration);
        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();
        animTimer.timer = new Timer(delay, e -> {
    
            long elapsed = animTimer.getElapsedNanos();
            float tp = getTimeProgress(elapsed, durationNanos);
    
            if (bLoop) {
                // 순환: 0 -> 1 -> 0 -> 1 반복
                borderHSB[1] = 0.5f + 0.5f * (float)Math.sin(tp * Math.PI * 2);
            } else {
                borderHSB[1] = tp;
                if (tp >= 1f) {
                    ((Timer)e.getSource()).stop();
                }
            }
    
            repaint();
        });
        animTimer.timer.start();
    }
    

    public void blink(float vis, float nonVis) {
        setVisible(true);
        final long visNanos = (long)(vis * 1_000_000_000L);
        final long nonVisNanos = (long)(nonVis * 1000000000L);
        final long durationNanos = visNanos + nonVisNanos;
        alpha = 1f;
        scaleX = 1f;
        scaleY = 1f;
        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();
        animTimer.timer = new Timer(delay, e -> {
            long elapsed = animTimer.getElapsedNanos();
            long phase   = elapsed % durationNanos;
            bVisible = phase < visNanos;
            repaint();
        });
        animTimer.timer.start();
    }

    public void popIn(float duration) {
        setVisible(true);
        alpha = 0f;
        bVisible = true;
        scaleX = 0.8f;
        scaleY = 0.8f;
        final long durationNanos = secToNanos(duration);
        final float overshoot = 2.0f;

        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();
        animTimer.timer = new Timer(delay, e -> {
            long now = System.nanoTime();
            if (animTimer.startTime < 0) animTimer.startTime = now;
            float t = Math.min(1f, (now - animTimer.startTime) / (float) durationNanos);

            float ease = 1 - (float)Math.pow(1 - t, 5); // 감속 곡선 (easeOutCubic)

            // scale: 시작 1+overshoot → 끝 1.0
            scaleX = 1.0f + overshoot * (1 - ease);
            scaleY = 1.0f + overshoot * (1 - ease);

            alpha = (float) Math.pow(t, 0.6);
            repaint();
            if (t >= 1f) ((Timer) e.getSource()).stop();
        });
        animTimer.timer.start();
    }


    public void popOut(float duration) {
        setVisible(true);
        final float overshoot = 1.4f;
        final float startScaleX = 0.6f;
        final float startScaleY = 0.6f;

        alpha = 0f;
        bVisible = true;
        scaleX = startScaleX;
        scaleY = startScaleY;

        final long durationNanos = secToNanos(duration);

        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();


        animTimer.timer = new Timer(delay, e -> {
            long now = System.nanoTime();
            float t = Math.min(1f, (now - animTimer.startTime) / (float) durationNanos);

            float s = overshoot; // overshoot 강도
            float tp = t - 1f;
            float backOut = tp * tp * ((s + 1f) * tp + s) + 1f;


            alpha = (float) Math.pow(t, 0.6);
            scaleX = startScaleX + (1f - startScaleX) * backOut;
            scaleY = startScaleY + (1f - startScaleY) * backOut;

            repaint();

            if (t >= 1f) {
                ((Timer) e.getSource()).stop();
            }
        });
        animTimer.timer.start();
    }

    public void move(float duration, int startX, int startY) {
        final float overshoot = 1.5f;
        final float endX = 0;
        final float endY = 0;
        alpha   = 1f;
        scaleX  = 1f;
        scaleY  = 1f;
        final long  durationNanos = secToNanos(duration);
        AnimTimer animTimer = addAnimTimer();
        animTimer.startTime = System.nanoTime();

        animTimer.timer = new Timer(delay, e -> {
            bVisible = true;

            long elapsed = animTimer.getElapsedNanos();
            float t = getTimeProgress(elapsed, durationNanos);

            // 위치 보간
            offsetX = interpolate(startX, endX, overshoot, t % 1f);
            offsetY = interpolate(startY, endY, overshoot, t % 1f);

            repaint();

            if(t >= 1f) {
                ((Timer)e.getSource()).stop();
                offsetX = endX;
                offsetY = endY;
            }
        });
        animTimer.timer.start();
    }

    /** 0..1로 클램프 */
    protected float clamp01(float t) {
        return t < 0f ? 0f : (t > 1f ? 1f : t);
    }

    /** Cubic ease-out */
    protected float easeOutCubic(float t) {
        t = clamp01(t);
        float u = 1f - t;
        return 1f - u*u*u;
    }

    /** Cubic ease-in */
    protected float easeInCubic(float t) {
        t = clamp01(t);
        return t*t*t;
    }

    protected float interpolate(float start, float end, float overshoot, float t) {
        t = clamp01(t);  // 0~1로 보정
        float eased;
    
        if (overshoot > 0f) {
            // Back Ease-Out (끝에서 살짝 넘어갔다가 돌아옴)
            float s = 1.70158f * overshoot; // 강도 조절
            float u = t - 1f;
            eased = 1f + (s + 1f) * u * u * u + s * u * u;
        } else {
            // 일반 Cubic Ease-Out
            eased = easeOutCubic(t);
        }
    
        return start + (end - start) * eased;
    }

    protected long secToNanos(float sec) {
        return (long)(sec * 1_000_000_000L);
    }

    protected float getTimeProgress(long cur, long total) {
        return (float)cur / (float)total;
    }

    protected Color HSBtoColor(float[] hsb) {
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }
    
    public void setBorderThickness(int thickness) {
        borderThickness = thickness;
    }
    
    public void setBorderRadius(int radius) {
        borderRadius = radius;
    }
    
    public void setBorderColor(Color border) {
        borderHSB = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), null);
    }
    
    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
        backgroundHSB = Color.RGBtoHSB(background.getRed(), background.getGreen(), background.getBlue(), null);
    }

    @Override 
    public void setVisible(boolean _bVisible) {
        //super.setVisible(aFlag);
        this.bVisible = _bVisible;
    }

    @Override
    public void paint(Graphics g) {
        if(!bVisible) return;

        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        g2.translate(offsetX, offsetY);

        g2.translate(w * 0.5, h * 0.5);
        g2.rotate(Math.toRadians(rotate));
        g2.scale(scaleX, scaleY);
        g2.translate(-w * 0.5, -h * 0.5);

        super.paint(g2);
        g2.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if(!bVisible) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(HSBtoColor(backgroundHSB));
        float o = borderThickness / 2f;
        int w = getWidth(), h = getHeight();
        g2.fillRoundRect(Math.round(o), Math.round(o),
                        Math.round(w - 1 - borderThickness),
                        Math.round(h - 1 - borderThickness),
                        borderRadius, borderRadius);
        g2.dispose();

        super.paintComponent(g); // 텍스트, 아이콘 등 그리기
    }

    @Override
    public void paintBorder(Graphics g) {
        if(!bVisible) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(borderThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(HSBtoColor(borderHSB));

        float o = borderThickness / 2f;
        int w = getWidth(), h = getHeight();
        g2.drawRoundRect(Math.round(o), Math.round(o),
                        Math.round(w - 1 - borderThickness),
                        Math.round(h - 1 - borderThickness),
                        borderRadius, borderRadius);
        g2.dispose();
    }

}
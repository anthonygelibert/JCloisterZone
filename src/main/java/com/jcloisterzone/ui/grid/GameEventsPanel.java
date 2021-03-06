package com.jcloisterzone.ui.grid;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.play.MeepleDeployed;
import com.jcloisterzone.event.play.MeepleReturned;
import com.jcloisterzone.event.play.NeutralFigureMoved;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.PlayerTurnEvent;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.event.play.TilePlacedEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.layer.EventsOverlayLayer;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.ui.resources.TileImage;

import io.vavr.collection.Queue;

public class GameEventsPanel extends JPanel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final int ICON_WIDTH = 30;
    private static Font FONT_SCORE = new Font(null, Font.PLAIN, 24);

    private EventsOverlayLayer eventsOverlayPanel;

    private GameState state;
    private ArrayList<EventItem> model = new ArrayList<>();
    private Integer mouseOverIdx;
    private int skipItems;

    protected final ResourceManager rm;

    public GameEventsPanel(GameController gc) {
        setBackground(Color.WHITE);
        rm = gc.getClient().getResourceManager();

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int idx = skipItems + e.getX() / ICON_WIDTH;
                setMouseOverIdx(idx < model.size() ? idx : null);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                setMouseOverIdx(null);
            }
        });
    }

    public void setMouseOverIdx(Integer mouseOverIdx) {
        if (this.mouseOverIdx == mouseOverIdx) {
            return;
        }

        this.mouseOverIdx = mouseOverIdx;
        if (mouseOverIdx == null) {
            eventsOverlayPanel.clearHighlight();
            return;
        }
        EventItem item = model.get(mouseOverIdx);
        PlayEvent ev = item.event;
        if (ev instanceof TilePlacedEvent) {
            eventsOverlayPanel.setHighlightedPosition(state, ((TilePlacedEvent) ev).getPosition());
        } else if (ev instanceof MeepleDeployed) {
            MeepleDeployed evt = (MeepleDeployed) ev;
            Feature feature = state.getFeature(evt.getPointer().asFeaturePointer());
            eventsOverlayPanel.setHighlightedFeature(state, feature);
        } else if (ev instanceof ScoreEvent) {
            ScoreEvent evt = (ScoreEvent) ev;
            Feature feature = state.getFeature(evt.getFeaturePointer());
            eventsOverlayPanel.setHighlightedFeature(state, feature);
        } else if (ev instanceof NeutralFigureMoved) {
            NeutralFigureMoved evt = (NeutralFigureMoved) ev;
            if (evt.getNeutralFigure() instanceof Count) {
                Feature feature = state.getFeature(evt.getTo().asFeaturePointer());
                eventsOverlayPanel.setHighlightedFeature(state, feature);
            } else {
                eventsOverlayPanel.setHighlightedPosition(state, evt.getTo().getPosition());
            }
        } else {
            eventsOverlayPanel.clearHighlight(); // should never happen
        }
    }

    public void handleGameChanged(GameChangedEvent ev) {
        state = ev.getCurrentState();
        model = prepareModel(state, ev.getCurrentState().getEvents());
        repaint();
    }

    private ArrayList<EventItem> prepareModel(GameState state, Queue<PlayEvent> events) {
        ArrayList<EventItem> model = new ArrayList<>();

        Color turnColor = Color.GRAY;
        boolean ignore = true;

        for (PlayEvent ev : events) {
            if (ev instanceof MeepleReturned) {
                continue;
            }
            if (ev instanceof PlayerTurnEvent) {
                turnColor = getMeepleColor(((PlayerTurnEvent) ev).getPlayer());
                ignore = false;
                continue;
            }
            if (ignore) {
                continue;
            }

            Integer idx = ev.getMetadata().getTriggeringPlayerIndex();
            Color triggeringColor;
            if (idx == null) {
                triggeringColor = turnColor;
            } else {
                triggeringColor = getMeepleColor(state.getPlayers().getPlayer(idx));
            }

            if (ev instanceof TilePlacedEvent) {
                TilePlacedEvent evt = (TilePlacedEvent) ev;
                TileImage img = rm.getTileImage(evt.getTile().getId(), evt.getRotation());
                model.add(new EventItem(ev, turnColor, triggeringColor, img.getImage()));
                continue;
            }
            if (ev instanceof MeepleDeployed) {
                // TOOD draw with opacity if returned
                MeepleDeployed evt = (MeepleDeployed) ev;
                Image img = rm.getLayeredImage(new LayeredImageDescriptor(evt.getMeeple().getClass(), triggeringColor));
                EventItem item = new EventItem(ev, turnColor, triggeringColor, img);
                item.imageMargin = 2;
                model.add(item);
                continue;
            }
            if (ev instanceof ScoreEvent) {
                ScoreEvent evt = (ScoreEvent) ev;
                model.add(new EventItem(ev, turnColor, triggeringColor, null));
                continue;
            }
            if (ev instanceof NeutralFigureMoved) {
                NeutralFigureMoved evt = (NeutralFigureMoved) ev;
                Image img = rm.getImage("neutral/count");
                model.add(new EventItem(ev, turnColor, triggeringColor, img));
                continue;
            }
            logger.warn("Unhandled event {}", ev.getClass());
        }
        return model;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        int size = model.size();
        skipItems = Math.max(0, size - getWidth() / ICON_WIDTH);

        Graphics2D g2 = (Graphics2D) g;
        int x = 0;

        g2.setFont(FONT_SCORE);
        for (int i = skipItems; i < size; i++) {
            EventItem item = model.get(i);
            int y = 0;
            g2.setColor(item.turnColor);
            g2.fillRect(x * ICON_WIDTH, y, ICON_WIDTH, 2);
            y += 2;
            g2.setColor(item.color);
            g2.fillRect(x * ICON_WIDTH, y, ICON_WIDTH, 2);
            y += 3; // 1px space

            if (item.image != null) {
                int m = item.imageMargin;
                g2.drawImage(item.image, x * ICON_WIDTH + m, y + m, ICON_WIDTH - 2 * m, ICON_WIDTH - 2 * m, null);
            } else if (item.event instanceof ScoreEvent) {
                ScoreEvent evt = (ScoreEvent) item.event;
                Color color = evt.getReceiver().getColors().getFontColor();
                g2.setColor(color);
                int offset = evt.getPoints() > 9 ? 0 : 8;
                g2.drawString("" + evt.getPoints(), x * ICON_WIDTH + offset, y + 22);
            } else {
                logger.error("Should never happen");
            }
            x++;
        }
    }

    private Color getMeepleColor(Player player) {
        return player.getColors().getMeepleColor();
    }

    public EventsOverlayLayer getEventsOverlayPanel() {
        return eventsOverlayPanel;
    }

    public void setEventsOverlayPanel(EventsOverlayLayer eventsOverlayPanel) {
        this.eventsOverlayPanel = eventsOverlayPanel;
    }

    static class EventItem {
        PlayEvent event;
        Color turnColor;
        Color color;
        Image image;
        int imageMargin = 0;

        public EventItem(PlayEvent event, Color turnColor, Color color, Image image) {
            this.event = event;
            this.turnColor = turnColor;
            this.color = color;
            this.image = image;
        }
    }
}

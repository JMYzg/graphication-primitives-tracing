package com.graphication;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class App {
    enum PrimitiveType {
        POINT, LINE, CIRCLE, RECTANGLE, OVAL, POLYGON, HEART
    }

    private static final int POINT_SIZE = 6;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::createAndShowUi);
    }

    private static void createAndShowUi() {
        JFrame frame = new JFrame("Graphication Primitives Tracing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DrawingPanel drawingPanel = new DrawingPanel();
        drawingPanel.setPreferredSize(new Dimension(900, 600));

        JPanel controls = new JPanel();
        JComboBox<PrimitiveType> primitiveSelector = new JComboBox<>(PrimitiveType.values());
        primitiveSelector.addActionListener(event -> drawingPanel.setCurrentPrimitive(
            (PrimitiveType) primitiveSelector.getSelectedItem()));
        controls.add(new JLabel("Primitive:"));
        controls.add(primitiveSelector);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(event -> drawingPanel.clear());
        controls.add(clearButton);

        frame.add(controls, BorderLayout.NORTH);
        frame.add(drawingPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static Shape buildShape(PrimitiveType primitiveType, Point start, Point end) {
        int minX = Math.min(start.x, end.x);
        int minY = Math.min(start.y, end.y);
        int width = Math.abs(end.x - start.x);
        int height = Math.abs(end.y - start.y);

        switch (primitiveType) {
            case POINT:
                return new Ellipse2D.Double(start.x - (POINT_SIZE / 2.0), start.y - (POINT_SIZE / 2.0),
                    POINT_SIZE, POINT_SIZE);
            case LINE:
                return new Line2D.Double(start, end);
            case CIRCLE:
                int size = Math.max(width, height);
                int circleX = end.x >= start.x ? start.x : start.x - size;
                int circleY = end.y >= start.y ? start.y : start.y - size;
                return new Ellipse2D.Double(circleX, circleY, size, size);
            case RECTANGLE:
                return new Rectangle2D.Double(minX, minY, width, height);
            case OVAL:
                return new Ellipse2D.Double(minX, minY, width, height);
            case POLYGON:
                return buildRegularPolygon(minX, minY, width, height, 5);
            case HEART:
                return buildHeart(minX, minY, width, height);
            default:
                throw new IllegalArgumentException("Unsupported primitive type: " + primitiveType);
        }
    }

    private static Shape buildRegularPolygon(int x, int y, int width, int height, int sides) {
        double centerX = x + (width / 2.0);
        double centerY = y + (height / 2.0);
        double radius = Math.min(width, height) / 2.0;
        Path2D path = new Path2D.Double();
        for (int i = 0; i < sides; i++) {
            double angle = (-Math.PI / 2.0) + (2.0 * Math.PI * i / sides);
            double px = centerX + radius * Math.cos(angle);
            double py = centerY + radius * Math.sin(angle);
            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }
        path.closePath();
        return path;
    }

    private static Shape buildHeart(int x, int y, int width, int height) {
        Path2D heart = new Path2D.Double();
        double top = y + height * 0.25;
        double left = x;
        double right = x + width;
        double bottom = y + height;
        double middleX = x + (width / 2.0);

        heart.moveTo(middleX, bottom);
        heart.curveTo(right, y + height * 0.75, right, top, middleX, y + height * 0.4);
        heart.curveTo(left, top, left, y + height * 0.75, middleX, bottom);
        heart.closePath();
        return heart;
    }

    private static final class DrawingPanel extends JPanel {
        private final List<TracedPrimitive> tracedPrimitives = new ArrayList<TracedPrimitive>();
        private PrimitiveType currentPrimitive = PrimitiveType.POINT;
        private Point start;
        private Point end;
        private boolean dragging;

        private DrawingPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    start = e.getPoint();
                    end = e.getPoint();
                    dragging = true;
                    if (currentPrimitive == PrimitiveType.POINT) {
                        tracedPrimitives.add(new TracedPrimitive(currentPrimitive, start, start));
                        dragging = false;
                        repaint();
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    end = e.getPoint();
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!dragging || currentPrimitive == PrimitiveType.POINT) {
                        return;
                    }
                    end = e.getPoint();
                    tracedPrimitives.add(new TracedPrimitive(currentPrimitive, start, end));
                    dragging = false;
                    repaint();
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        private void setCurrentPrimitive(PrimitiveType primitiveType) {
            currentPrimitive = primitiveType;
        }

        private void clear() {
            tracedPrimitives.clear();
            start = null;
            end = null;
            dragging = false;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2f));

            g2.setColor(Color.BLACK);
            for (TracedPrimitive primitive : tracedPrimitives) {
                g2.draw(buildShape(primitive.type, primitive.start, primitive.end));
            }

            if (dragging && start != null && end != null && currentPrimitive != PrimitiveType.POINT) {
                g2.setColor(new Color(30, 120, 220));
                g2.draw(buildShape(currentPrimitive, start, end));
            }
            g2.dispose();
        }
    }

    private static final class TracedPrimitive {
        private final PrimitiveType type;
        private final Point start;
        private final Point end;

        private TracedPrimitive(PrimitiveType type, Point start, Point end) {
            this.type = type;
            this.start = new Point(start);
            this.end = new Point(end);
        }
    }
}

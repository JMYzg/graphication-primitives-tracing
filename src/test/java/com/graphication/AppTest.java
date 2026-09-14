package com.graphication;

import junit.framework.TestCase;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public class AppTest extends TestCase {
    public void testCircleAlwaysHasEqualWidthAndHeight() {
        Shape circle = App.buildShape(App.PrimitiveType.CIRCLE, new Point(10, 10), new Point(50, 30));
        Rectangle2D bounds = circle.getBounds2D();
        assertEquals(bounds.getWidth(), bounds.getHeight());
    }

    public void testPointUsesSmallMarkerSize() {
        Shape point = App.buildShape(App.PrimitiveType.POINT, new Point(40, 60), new Point(40, 60));
        Rectangle2D bounds = point.getBounds2D();
        assertEquals(6.0, bounds.getWidth());
        assertEquals(6.0, bounds.getHeight());
    }

    public void testHeartAndPolygonProducePathShapes() {
        Shape heart = App.buildShape(App.PrimitiveType.HEART, new Point(10, 10), new Point(90, 100));
        Shape polygon = App.buildShape(App.PrimitiveType.POLYGON, new Point(10, 10), new Point(90, 100));
        assertTrue(heart instanceof Path2D);
        assertTrue(polygon instanceof Path2D);
    }

    public void testOvalAndRectangleBoundsMatchDragArea() {
        Shape rectangle = App.buildShape(App.PrimitiveType.RECTANGLE, new Point(80, 40), new Point(20, 10));
        Shape oval = App.buildShape(App.PrimitiveType.OVAL, new Point(80, 40), new Point(20, 10));
        Rectangle2D rectBounds = rectangle.getBounds2D();
        Rectangle2D ovalBounds = oval.getBounds2D();
        assertEquals(20.0, rectBounds.getX());
        assertEquals(10.0, rectBounds.getY());
        assertEquals(60.0, rectBounds.getWidth());
        assertEquals(30.0, rectBounds.getHeight());
        assertEquals(rectBounds, ovalBounds);
    }

    public void testCircleIsEllipseShape() {
        Shape circle = App.buildShape(App.PrimitiveType.CIRCLE, new Point(10, 10), new Point(50, 30));
        assertTrue(circle instanceof Ellipse2D);
    }
}

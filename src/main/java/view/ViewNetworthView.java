package view;

import interface_adapter.ViewNetworth.ViewNetworthViewModel;
import interface_adapter.ViewNetworth.ViewNetworthViewModelListener;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class ViewNetworthView extends JPanel implements ViewNetworthViewModelListener {
        private Map<LocalTime, BigDecimal> networthHistory;
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        public ViewNetworthView(ViewNetworthViewModel viewNetworthViewModel) {
            setPreferredSize(new Dimension(700, 450));
            setBackground(Color.WHITE);
            viewNetworthViewModel.addListener(this);
        }

        @Override
        public void notifyPropertyChanged(Map<LocalTime, BigDecimal> networthHistory) {
            this.networthHistory = networthHistory;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 90; // Space around the graph for axis titles & tick labels

            // 1. Draw Chart Title
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("Net Worth Over Time", width / 2 - 80, 30);

            // 2. Draw X and Y Axes
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(padding, height - padding, width - padding, height - padding); // X-axis
            g2.drawLine(padding, padding, padding, height - padding);                  // Y-axis

            // 3. Draw Axis Titles
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.drawString("Time", width / 2 - 15, height - 15); // X-axis label

            // Rotated Y-axis label ("Net Worth ($)")
            AffineTransform originalTransform = g2.getTransform();
            g2.rotate(-Math.PI / 2);
            g2.drawString("Net Worth ($)", -(height / 2 + 40), 20);
            g2.setTransform(originalTransform);

            // Handle empty state
            if (networthHistory == null || networthHistory.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
                g2.drawString("No net worth data available.", width / 2 - 90, height / 2);
                return;
            }

            // 4. Find Min and Max Net Worth for Y-axis scaling
            List<Map.Entry<LocalTime, BigDecimal>> entries = new ArrayList<>(new TreeMap<>(networthHistory).entrySet());
            BigDecimal minVal = entries.get(0).getValue();
            BigDecimal maxVal = entries.get(0).getValue();
            for (Map.Entry<LocalTime, BigDecimal> entry : entries) {
                if (entry.getValue().compareTo(minVal) < 0) minVal = entry.getValue();
                if (entry.getValue().compareTo(maxVal) > 0) maxVal = entry.getValue();
            }
            // Prevent division by zero if all values are identical
            if (minVal.compareTo(maxVal) == 0) {
                minVal = minVal.subtract(BigDecimal.TEN);
                maxVal = maxVal.add(BigDecimal.TEN);
            }

            int plotWidth = width - 2 * padding;
            int plotHeight = height - 2 * padding;

            // 5. Draw Y-Axis Tick Marks & Dollar Labels (5 intervals)
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            int numYTicks = 5;
            for (int i = 0; i <= numYTicks; i++) {
                int y = height - padding - (i * plotHeight / numYTicks);
                BigDecimal tickValue = minVal.add(
                        maxVal.subtract(minVal)
                                .multiply(BigDecimal.valueOf(i))
                                .divide(BigDecimal.valueOf(numYTicks), 2, RoundingMode.HALF_UP)
                );

                // Light horizontal grid line
                g2.setColor(new Color(230, 230, 230));
                g2.drawLine(padding + 1, y, width - padding, y);

                // Tick mark and value
                g2.setColor(Color.BLACK);
                g2.drawLine(padding - 5, y, padding, y);
                String label = "$" + tickValue.toPlainString();
                int labelWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, padding - 8 - labelWidth, y + 4);
            }

            // 6. Plot Points, Connecting Lines, and X-Axis Time Labels
            int n = entries.size();
            int[] xPoints = new int[n];
            int[] yPoints = new int[n];

            for (int i = 0; i < n; i++) {
                LocalTime time = entries.get(i).getKey();
                BigDecimal val = entries.get(i).getValue();

                // Calculate X coordinate (centered if only 1 point)
                xPoints[i] = (n == 1)
                        ? padding + plotWidth / 2
                        : padding + (i * plotWidth) / (n - 1);

                // Calculate Y coordinate scaled between minVal and maxVal
                double ratio = val.subtract(minVal).doubleValue() / maxVal.subtract(minVal).doubleValue();
                yPoints[i] = height - padding - (int) (ratio * plotHeight);

                // X-axis tick mark and timestamp label
                g2.setColor(Color.BLACK);
                g2.drawLine(xPoints[i], height - padding, xPoints[i], height - padding + 5);
                String timeLabel = time.format(timeFormatter);
                g2.drawString(timeLabel, xPoints[i] - 22, height - padding + 20);
            }

            // Draw connecting line segments
            g2.setColor(new Color(33, 150, 243)); // Clean blue line
            g2.setStroke(new BasicStroke(2.5f));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
            }

            // Draw data point dots
            g2.setColor(new Color(13, 71, 161));
            for (int i = 0; i < n; i++) {
                g2.fillOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
            }
        }
    }

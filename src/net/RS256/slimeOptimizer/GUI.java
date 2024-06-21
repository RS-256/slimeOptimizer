package net.RS256.slimeOptimizer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class GUI implements ActionListener {
    JPanel panel = new JPanel();
    JFrame frame = new JFrame();
    JPanel chunkGridPanel = new JPanel();
    JTextField seed;
    JTextField cX;
    JTextField cZ;
    JLabel seedLabel;
    JLabel cXLabel;
    JLabel cZLabel;
    JLabel outputLabel;
    JTextArea output;
    JButton runButton;
    JCheckBox debugInfoCheckBox;
    JTextField debugLogOutput;
    JTextField slimeChunkCount;
//    JComboBox<String> versionComboBox;
//    JLabel versionLabel;

    private static final int CHUNK_GRID_SIZE = 19;
    private  static final int CHUNK_CELL_SIZE = 30;
    private static int SLIME_CHUNK_COUNT_SQUARE = 0;
    private static int SLIME_CHUNK_COUNT_CIRCLE = 0;
    private static final boolean[][] slimeChunkCache = new boolean[CHUNK_GRID_SIZE][CHUNK_GRID_SIZE];
    private long executionTime;


    public static void main(String[] args) {
        new GUI();
    }

    public GUI() {
        this.panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        this.panel.setLayout(null);

        Font MonospaceFont = new Font("consolas", Font.PLAIN, 14);
        UIManager.put("TextField.font", MonospaceFont);
        UIManager.put("TextArea.font", MonospaceFont);
        Font smallMonospaceFont = new Font("consolas", Font.PLAIN, 10);

        Border EmptyBorder = new EmptyBorder(0, 0, 0, 0);
        Border GrayColoredBorder = new LineBorder(Color.GRAY, 1);
        UIManager.put("Button.border", GrayColoredBorder);
        UIManager.put("TextField.border", GrayColoredBorder);
        UIManager.put("TextArea.border", EmptyBorder);

        // seed の表示

        this.seedLabel = new JLabel("seed: ");
        this.seedLabel.setBounds(30, 0, 330, 30);
        this.panel.add(this.seedLabel);
        this.seed = new JTextField();
        this.seed.setBounds(30, 30, 330, 30);
        this.panel.add(this.seed);

        // cX の表示

        this.cXLabel = new JLabel("cX");
        this.cXLabel.setBounds(30, 60, 90, 30);
        this.panel.add(this.cXLabel);
        this.cX = new JTextField();
        this.cX.setBounds(30, 90, 90, 30);
        this.panel.add(this.cX);
        this.cX.setText("0");

        // cZ の表示

        this.cZLabel = new JLabel("cZ");
        this.cZLabel.setBounds(150, 60, 90, 30);
        this.panel.add(this.cZLabel);
        this.cZ = new JTextField();
        this.cZ.setBounds(150, 90, 90, 30);
        this.panel.add(this.cZ);
        this.cZ.setText("0");

        // run の表示

        this.runButton = new JButton("run");
        this.runButton.setBounds(270, 90, 90, 30);
        this.runButton.addActionListener(this);
        this.panel.add(this.runButton);

        // Debug Info の表示

        this.debugInfoCheckBox = new JCheckBox("Debug Info");
        this.debugInfoCheckBox.setBounds(270, 120, 90, 30);
        this.panel.add(this.debugInfoCheckBox);

        // debug log の表示

        this.debugLogOutput = new JTextField();
        this.debugLogOutput.setBounds(30, 600, 925, 30);
        this.debugLogOutput.setEditable(false);
        this.debugLogOutput.setOpaque(false);
        this.debugLogOutput.setBorder(BorderFactory.createEmptyBorder());
        this.debugLogOutput.setHorizontalAlignment(JTextField.RIGHT);
        this.debugLogOutput.setFont(smallMonospaceFont);
        this.panel.add(this.debugLogOutput);

        // slimeChunkCount の表示

        this.slimeChunkCount = new JTextField();
        this.slimeChunkCount.setBounds(30, 600, 330, 30);
        this.slimeChunkCount.setEditable(false);
        this.slimeChunkCount.setOpaque(false);
        this.slimeChunkCount.setBorder(BorderFactory.createEmptyBorder());
        this.slimeChunkCount.setFont(smallMonospaceFont);
        this.panel.add(this.slimeChunkCount);

        // output の表示
        this.outputLabel = new JLabel("output");
        this.outputLabel.setBounds(30, 120, 330, 30);
        this.panel.add(this.outputLabel);
        this.output = new JTextArea();
        this.output.setEditable(false);
        this.output.setBounds(30, 150, 330, 450);

        JScrollPane scrollPane = new JScrollPane(this.output);
        scrollPane.setBounds(30, 150, 330, 450);
        this.panel.add(scrollPane);
        this.frame.add(this.panel);



        this.chunkGridPanel.setLayout(new GridLayout(CHUNK_GRID_SIZE, CHUNK_GRID_SIZE, 0, 0));
        this.chunkGridPanel.setBounds(385, 30, CHUNK_GRID_SIZE * CHUNK_CELL_SIZE, CHUNK_GRID_SIZE * CHUNK_CELL_SIZE);

        for (int i = 0; i < CHUNK_GRID_SIZE * CHUNK_GRID_SIZE; i++) {
            JPanel cell = new JPanel();
            cell.setBorder(BorderFactory.createLineBorder(Color.lightGray));
            cell.setBackground(Color.WHITE);
            this.chunkGridPanel.add(cell);
        }

        this.panel.add(this.chunkGridPanel);

        this.frame.add(this.panel);

        // タイトルとウィンドウサイズの指定

        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setTitle("slimeOptimizer");
        this.frame.pack();
        this.frame.setResizable(false);
        this.frame.setSize(1000, 665);
        this.frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {

        long seed;
        int cX;
        int cZ;

        // 無効な数字の検出

        try {
            seed = Long.parseLong(this.seed.getText());
            cX = Integer.parseInt(this.cX.getText());
            cZ = Integer.parseInt(this.cZ.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this.frame, "Invalid input. Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(this.debugInfoCheckBox.isSelected() == true){
            measureExecutionTime(() -> {
                updateChunkGrid(seed, cX, cZ);
                String[] outputText = outputCalculate(seed, cX, cZ);
                this.output.setText(outputText[0]);
                this.output.setCaretPosition(0);
                this.slimeChunkCount.setText("#: " + SLIME_CHUNK_COUNT_SQUARE + ", O: " + SLIME_CHUNK_COUNT_CIRCLE);
            });

            this.debugLogOutput.setText(executionTime + "ms");
        } else {
            updateChunkGrid(seed, cX, cZ);
            String[] outputText = outputCalculate(seed, cX, cZ);
            this.output.setText(outputText[0]);
            this.output.setCaretPosition(0);
            this.slimeChunkCount.setText("");
            this.debugLogOutput.setText("");
        }
    }

    private void initializeSlimeChunkCache(long seed, int centerX, int centerZ) {
        for (int i = 0; i < CHUNK_GRID_SIZE; i++) {
            for (int j = 0; j < CHUNK_GRID_SIZE; j++) {
                int offsetX = i - CHUNK_GRID_SIZE / 2;
                int offsetZ = j - CHUNK_GRID_SIZE / 2;
                slimeChunkCache[i][j] = isSlimeChunk(seed, centerX + offsetX, centerZ + offsetZ);

                if (slimeChunkCache[i][j] == true && this.debugInfoCheckBox.isSelected() == true) {
                    if (Math.abs(offsetX) <= 8 == true && Math.abs(offsetZ) <= 8 == true) {
                        SLIME_CHUNK_COUNT_SQUARE++;
                    }
                    if ((offsetX ^ 2 + offsetZ ^ 2) < Math.pow(8, 2) == true && (offsetX ^ 2 + offsetZ ^ 2) > Math.pow(1.5, 2) == true) {
                        SLIME_CHUNK_COUNT_CIRCLE++;
                    }
                }
            }
        }
    }

    private void updateChunkGrid(Long seed, int cX, int cZ) {

        this.chunkGridPanel.removeAll();
        this.chunkGridPanel.setLayout(new GridLayout(CHUNK_GRID_SIZE, CHUNK_GRID_SIZE));

        this.chunkGridPanel.repaint();

        initializeSlimeChunkCache(seed, cX, cZ);

        for (int i = 0; i < CHUNK_GRID_SIZE * CHUNK_GRID_SIZE; i++) {

            int offsetX = i % CHUNK_GRID_SIZE - CHUNK_GRID_SIZE / 2;
            int offsetZ = i / CHUNK_GRID_SIZE - CHUNK_GRID_SIZE / 2;

            JLabel label = new JLabel();
            label.setOpaque(true);

            if (offsetX == 0 && offsetZ == 0) {
                label.setBorder(BorderFactory.createLineBorder(new Color(128, 0, 128)));
            } else {
                label.setBorder(BorderFactory.createLineBorder(Color.lightGray));
            }

            if (slimeChunkCache[offsetX + CHUNK_GRID_SIZE / 2][offsetZ + CHUNK_GRID_SIZE / 2] == true) {
                label.setBackground(new Color(144, 238, 144));
            } else {
                label.setBackground(Color.WHITE);
            }
            this.chunkGridPanel.add(label);
        }

        this.chunkGridPanel.revalidate();
        this.chunkGridPanel.repaint();
    }

    private void measureExecutionTime(Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();
        executionTime = endTime - startTime;
    }

    public static String[] outputCalculate(long seed, int cX, int cZ) {

        Map<String, Integer> result = new HashMap<>();
        StringBuilder output = new StringBuilder();

        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {

                int currentCX = cX + offsetX;
                int currentCZ = cZ + offsetZ;

                for (int bX = 0; bX < 16; bX++) {
                    for (int bZ = 0; bZ < 16; bZ++) {
                        int pX = currentCX * 16 + bX;
                        int pZ = currentCZ * 16 + bZ;

                        int slimeChunkBlockCount = countSlimeChunkInCache(seed, pX, pZ, cX, cZ);

                        String key = "[" + offsetX + ", " + offsetZ + "] (" + pX + ", " + pZ + ")";
                        result.put(key, slimeChunkBlockCount);
                    }
                }
            }
        }
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(result.entrySet());
        sortedList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (Map.Entry<String, Integer> entry : sortedList) {
            output.append(entry.getKey())
                    .append(" = ")
                    .append(entry.getValue())
                    .append("\n");
        }

        int index = 0;
        String firstLine;
        if (sortedList.get(index) == null){
            return new String[]{output.toString(),"0","0"};
        } else {
            firstLine = sortedList.get(index).toString();
            int[] maxCoordinate = maxCoord(firstLine);
            return new String[]{output.toString(), String.valueOf(maxCoordinate[0]), String.valueOf(maxCoordinate[1])};
        }
    }

    public static int[] maxCoord(String firstLine) {

        Pattern pattern = Pattern.compile("\\((-?\\d+), (-?\\d+)\\)");
        Matcher matcher = pattern.matcher(firstLine);

        if (matcher.find()){
            int maxX = Integer.parseInt(matcher.group(1));
            int maxZ = Integer.parseInt(matcher.group(2));
//            System.out.println(maxX);
//            System.out.println(maxZ);
            return new int[]{maxX, maxZ};
        } else {
            return new int[]{0,0};
        }
    }

    public static int countSlimeChunkInCache(long seed, int pX, int pZ, int centerCX, int centerCZ) {
        int slimeChunkBlockCount = 0;

        for (int X = pX - 128; X <= pX + 128; X++) {
            for (int Z = pZ - 128; Z <= pZ + 128; Z++) {
                int cX = X / 16;
                int cZ = Z / 16;
                int distance = (pX - X) * (pX - X) + (pZ - Z) * (pZ - Z);

                if (distance >= 24 * 24 &&
                        distance <= 128 * 128 &&
                        isSlimeChunkInCache(seed, cX, cZ, centerCX, centerCZ) == true) {
                    slimeChunkBlockCount++;
                }
            }
        }
        return slimeChunkBlockCount;
    }

    public static boolean isSlimeChunkInCache(long seed, int cX, int cZ, int centerCX, int centerCZ) {

        int offsetX = cX - centerCX + CHUNK_GRID_SIZE / 2;
        int offsetZ = cZ - centerCZ + CHUNK_GRID_SIZE / 2;

        if (offsetX >= 0 && offsetX < CHUNK_GRID_SIZE && offsetZ >= 0 && offsetZ < CHUNK_GRID_SIZE) {
            return slimeChunkCache[offsetX][offsetZ];
        } else {
            // キャッシュ範囲外の場合は直接計算
            return isSlimeChunk(seed, cX, cZ);
        }
    }

    public static boolean isSlimeChunk(long seed, int cX, int cZ) {
        Random random = createSlimeRandom(seed, cX, cZ);
        return random.nextInt(10) == 0;
    }


    public static Random createSlimeRandom(long seed, int chunkX, int chunkZ) {

        return new Random(
                seed +
                        (int) (chunkX * chunkX * 0x4c1906) +
                        (int) (chunkX * 0x5ac0db) +
                        (int) (chunkZ * chunkZ) * 0x4307a7L +
                        (int) (chunkZ * 0x5f24f) ^ 0x3ad8025fL
        );
    }
}
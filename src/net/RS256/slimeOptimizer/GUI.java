package net.RS256.slimeOptimizer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class GUI implements ActionListener {
    JPanel panel = new JPanel();
    JFrame frame = new JFrame();
    JTextField seed;
    JTextField cX;
    JTextField cZ;
    JLabel seedLabel;
    JLabel cXLabel;
    JLabel cZLabel;
    JLabel outputLabel;
    JTextArea output;
    JButton runButton;

    public static void main(String[] args) {
        new GUI();
    }

    public GUI() {
        this.panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        this.panel.setLayout(null);
        Font MonospaceFont = new Font("consolas", Font.PLAIN, 14);

        UIManager.put("TextField.font", MonospaceFont);
        UIManager.put("TextArea.font", MonospaceFont);

        // seed の表示

        this.seedLabel = new JLabel("seed : ");
        this.seedLabel.setBounds(30, 0, 330, 30);
        this.panel.add(this.seedLabel);
        this.seed = new JTextField();
        this.seed.setBounds(30, 30, 330, 30);
        this.seed.setFont(MonospaceFont);
        this.panel.add(this.seed);

        // min の表示

        this.cXLabel = new JLabel("cX");
        this.cXLabel.setBounds(30, 60, 90, 30);
        this.panel.add(this.cXLabel);
        this.cX = new JTextField();
        this.cX.setBounds(30, 90, 90, 30);
        this.panel.add(this.cX);
        this.cX.setText("0");

        // max の表示

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

        // output の表示
        this.outputLabel = new JLabel("output");
        this.outputLabel.setBounds(30, 120, 330, 30);
        this.panel.add(this.outputLabel);
        this.output = new JTextArea();
        this.output.setEditable(false);
        this.output.setBounds(30, 150, 330, 240);
        this.output.setBorder(BorderFactory.createEtchedBorder());

        // Wrap the JTextArea in a JScrollPane
        JScrollPane scrollPane = new JScrollPane(this.output);
        scrollPane.setBounds(30, 150, 330, 240);
        this.panel.add(scrollPane);
        this.frame.add(this.panel);

        // タイトルとウィンドウサイズの指定

        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setTitle("slimeOptimizer");
        this.frame.pack();
        this.frame.setResizable(false);
        this.frame.setSize(405, 460);
        this.frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {

        //変数の定義

        long seed;
        int cX;
        int cZ;

        // 無効な数字の検出

        try {
            seed = Long.parseLong(this.seed.getText());
            cX = Integer.parseInt(this.cX.getText());
            cZ = Integer.parseInt(this.cZ.getText());
        }
        catch (Exception var11) {
            this.output.setText("Illegal argument");
            return;
        }

        this.output.setText(outputCalculate(seed, cX, cZ));
    }

    public static String outputCalculate(long seed, int cX, int cZ) {

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

                        int slimeChunkBlockCount = countSlimeChunks(seed, pX, pZ);

                        String key = "[" + offsetX + ", " + offsetZ + "] (" + pX + ", " + pZ + ")";
                        result.put(key, slimeChunkBlockCount);
                    }
                }
            }
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(result.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (Map.Entry<String, Integer> entry : list) {
            output.append(entry.getKey())
                    .append(" = ")
                    .append(entry.getValue())
                    .append("\n");
        }

        return output.toString();
    }

    public static int countSlimeChunks(long seed, int pX, int pZ) {
        int slimeChunkBlockCount = 0;

        for (int X = pX - 128; X <= pX + 128; X++) {
            for (int Z = pZ - 128; Z <= pZ + 128; Z++) {
                int cX = X / 16;
                int cZ = Z / 16;
                int distance = (pX - X) * (pX - X) + (pZ - Z) * (pZ - Z);

                if (distance >= 24 * 24 &&
                        distance <= 128 * 128 &&
                        isSlimeChunk(seed, cX, cZ) == true) {
                    slimeChunkBlockCount++;
                }
            }
        }
        return slimeChunkBlockCount;
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


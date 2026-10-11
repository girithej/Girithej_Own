import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Calculator extends JFrame {
    private final JTextField display;
    private final JLabel expressionDisplay;
    private String currentInput = "";
    private String storedValue = "";
    private String pendingOperator = "";
    private boolean startNewNumber = false;
    private boolean hasError = false;

    public Calculator() {
        super("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        mainPanel.setBackground(new Color(15, 23, 42));

        JPanel displayPanel = new JPanel(new BorderLayout(8, 8));
        displayPanel.setBackground(new Color(17, 24, 39));
        displayPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        expressionDisplay = new JLabel(" ");
        expressionDisplay.setHorizontalAlignment(SwingConstants.RIGHT);
        expressionDisplay.setFont(new Font("SansSerif", Font.PLAIN, 16));
        expressionDisplay.setForeground(new Color(148, 163, 184));

        display = new JTextField("0");
        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(new Font("SansSerif", Font.BOLD, 38));
        display.setBackground(new Color(17, 24, 39));
        display.setForeground(new Color(248, 250, 252));
        display.setBorder(BorderFactory.createEmptyBorder());
        display.setCaretColor(new Color(248, 250, 252));
        displayPanel.add(expressionDisplay, BorderLayout.NORTH);
        displayPanel.add(display, BorderLayout.CENTER);
        mainPanel.add(displayPanel, BorderLayout.NORTH);

        JPanel buttonGrid = new JPanel(new GridLayout(5, 4, 12, 12));
        buttonGrid.setBackground(new Color(15, 23, 42));

        String[] labels = {
            "C", "⌫", "/", "*",
            "7", "8", "9", "-",
            "4", "5", "6", "+",
            "1", "2", "3", "=",
            "0", "."
        };

        for (String label : labels) {
            JButton button = new JButton(label);
            button.setFocusPainted(false);
            button.setFont(new Font("SansSerif", Font.BOLD, 24));
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setBackground(getButtonColor(label));
            button.setForeground(Color.WHITE);
            button.addActionListener(new CalculatorButtonListener(label));
            buttonGrid.add(button);
        }

        mainPanel.add(buttonGrid, BorderLayout.CENTER);
        setContentPane(mainPanel);
        updateDisplay();
    }

    private Color getButtonColor(String label) {
        if ("C".equals(label) || "⌫".equals(label)) {
            return new Color(71, 85, 105);
        }
        if ("/".equals(label) || "*".equals(label) || "-".equals(label) || "+".equals(label) || "=".equals(label)) {
            return new Color(99, 102, 241);
        }
        return new Color(51, 65, 85);
    }

    private void updateDisplay() {
        if (hasError) {
            expressionDisplay.setText("Cannot divide by zero");
            display.setText("Error");
            return;
        }

        if (!storedValue.isEmpty() && !pendingOperator.isEmpty()) {
            String operatorSymbol = pendingOperator;
            if ("*".equals(operatorSymbol)) {
                operatorSymbol = "×";
            } else if ("/".equals(operatorSymbol)) {
                operatorSymbol = "÷";
            } else if ("-".equals(operatorSymbol)) {
                operatorSymbol = "−";
            }
            String expression = storedValue + " " + operatorSymbol;
            if (!startNewNumber && !currentInput.isEmpty()) {
                expression += " " + currentInput;
            }
            expressionDisplay.setText(expression);
        } else {
            expressionDisplay.setText(" ");
        }

        display.setText(currentInput.isEmpty() ? "0" : currentInput);
    }

    private void clearAll() {
        currentInput = "";
        storedValue = "";
        pendingOperator = "";
        startNewNumber = false;
        hasError = false;
        updateDisplay();
    }

    private void deleteLast() {
        if (hasError) {
            clearAll();
            return;
        }

        if (currentInput.length() <= 1) {
            currentInput = "";
        } else {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        }
        updateDisplay();
    }

    private void inputDigit(String digit) {
        if (hasError) {
            clearAll();
        }

        if (startNewNumber) {
            currentInput = digit;
            startNewNumber = false;
        } else {
            currentInput = currentInput.equals("0") ? digit : currentInput + digit;
        }
        updateDisplay();
    }

    private void inputDecimal() {
        if (hasError) {
            clearAll();
        }

        if (startNewNumber) {
            currentInput = "0.";
            startNewNumber = false;
        } else if (!currentInput.contains(".")) {
            currentInput = currentInput.isEmpty() ? "0." : currentInput + ".";
        }
        updateDisplay();
    }

    private void handleOperator(String operator) {
        if (hasError) {
            return;
        }

        if (currentInput.isEmpty()) {
            return;
        }

        if (!storedValue.isEmpty() && !pendingOperator.isEmpty()) {
            evaluate(false);
        }

        storedValue = currentInput;
        pendingOperator = operator;
        startNewNumber = true;
        currentInput = "";
        updateDisplay();
    }

    private void evaluate(boolean fromEqualsButton) {
        if (hasError) {
            return;
        }

        if (storedValue.isEmpty() || currentInput.isEmpty() || pendingOperator.isEmpty()) {
            if (fromEqualsButton) {
                return;
            }
            return;
        }

        double left = Double.parseDouble(storedValue);
        double right = Double.parseDouble(currentInput);
        double result;

        switch (pendingOperator) {
            case "+":
                result = left + right;
                break;
            case "-":
                result = left - right;
                break;
            case "*":
                result = left * right;
                break;
            case "/":
                if (right == 0) {
                    hasError = true;
                    currentInput = "";
                    storedValue = "";
                    pendingOperator = "";
                    updateDisplay();
                    return;
                }
                result = left / right;
                break;
            default:
                result = right;
                break;
        }

        String formattedResult = formatResult(result);
        currentInput = formattedResult;
        storedValue = "";
        pendingOperator = "";

        if (fromEqualsButton) {
            startNewNumber = true;
        }

        updateDisplay();
    }

    private String formatResult(double result) {
        if (Double.isInfinite(result) || Double.isNaN(result)) {
            hasError = true;
            currentInput = "";
            storedValue = "";
            pendingOperator = "";
            return "Error";
        }

        if (result == Math.rint(result)) {
            return String.valueOf((long) result);
        }

        return String.valueOf(result);
    }

    private class CalculatorButtonListener implements ActionListener {
        private final String label;

        public CalculatorButtonListener(String label) {
            this.label = label;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (label) {
                case "C":
                    clearAll();
                    break;
                case "⌫":
                    deleteLast();
                    break;
                case "/":
                case "*":
                case "+":
                case "-":
                    handleOperator(label);
                    break;
                case "=":
                    if (!storedValue.isEmpty() && !pendingOperator.isEmpty() && !currentInput.isEmpty()) {
                        evaluate(true);
                    }
                    break;
                case ".":
                    inputDecimal();
                    break;
                default:
                    if (label.matches("[0-9]")) {
                        inputDigit(label);
                    }
                    break;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Calculator calculator = new Calculator();
            calculator.setVisible(true);
        });
    }
}

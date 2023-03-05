package com.cekmitl.pdpacameracensor;

import java.io.Serializable;

public class Question implements Serializable {
    private int[] operands;
    private int[] choices;
    private int userAnswerIndex;

    public Question(int[] operands, int[] choices) {
        this.operands = operands;
        this.choices = choices;
        this.userAnswerIndex = -1;
    }

    public int[] getChoices() {
        return choices;
    }

    public void setChoices(int[] choices) {
        this.choices = choices;
    }

    public int[] getOperands() {
        return operands;
    }

    public void setOperands(int[] operands) {
        this.operands = operands;
    }

    public int getUserAnswerIndex() {
        return userAnswerIndex;
    }

    public void setUserAnswerIndex(int userAnswerIndex) {
        this.userAnswerIndex = userAnswerIndex;
    }

    public int getAnswer() {
        int answer = 0;
        for (int operand : operands) {
            answer += operand;
        }
        return answer;
    }

    public boolean isCorrect() {
        return getAnswer() == choices[this.userAnswerIndex];
    }

    public boolean hasAnswered() {
        return userAnswerIndex != -1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        // Question
        builder.append("Question: ");
        for(int operand : operands) {
            builder.append(String.format("%d ", operand));
        }
        builder.append(System.getProperty("line.separator"));

        // Choices
        int answer = getAnswer();
        for (int choice : choices) {
            if (choice == answer) {
                builder.append(String.format("%d (A) ", choice));
            } else {
                builder.append(String.format("%d ", choice));
            }
        }
        return builder.toString();
    }
}
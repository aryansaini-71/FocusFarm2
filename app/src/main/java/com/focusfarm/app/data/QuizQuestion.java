package com.focusfarm.app.data;

import androidx.annotation.StringRes;

/** One multiple-choice reading quiz shown in the intervention overlay. */
public final class QuizQuestion {

    @StringRes
    public final int paragraphRes;
    @StringRes
    public final int questionRes;
    @StringRes
    public final int[] answerRes;
    /** Index into {@link #answerRes} for the correct option (0–2). */
    public final int correctIndex;

    public QuizQuestion(
            @StringRes int paragraphRes,
            @StringRes int questionRes,
            @StringRes int[] answerRes,
            int correctIndex) {
        if (answerRes == null || answerRes.length != 3) {
            throw new IllegalArgumentException("Quiz must have exactly 3 answers");
        }
        if (correctIndex < 0 || correctIndex > 2) {
            throw new IllegalArgumentException("correctIndex must be 0, 1, or 2");
        }
        this.paragraphRes = paragraphRes;
        this.questionRes = questionRes;
        this.answerRes = answerRes;
        this.correctIndex = correctIndex;
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctIndex;
    }
}

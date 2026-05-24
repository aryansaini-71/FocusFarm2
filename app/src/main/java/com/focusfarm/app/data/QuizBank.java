package com.focusfarm.app.data;

import com.focusfarm.app.R;

import java.util.Random;

/** All intervention quizzes; one is picked at random per session. */
public final class QuizBank {

    private static final QuizQuestion[] QUESTIONS = {
            new QuizQuestion(
                    R.string.quiz_1_paragraph,
                    R.string.quiz_1_question,
                    new int[]{
                            R.string.quiz_1_answer_a,
                            R.string.quiz_1_answer_b,
                            R.string.quiz_1_answer_c
                    },
                    0),
            new QuizQuestion(
                    R.string.quiz_2_paragraph,
                    R.string.quiz_2_question,
                    new int[]{
                            R.string.quiz_2_answer_a,
                            R.string.quiz_2_answer_b,
                            R.string.quiz_2_answer_c
                    },
                    1),
            new QuizQuestion(
                    R.string.quiz_3_paragraph,
                    R.string.quiz_3_question,
                    new int[]{
                            R.string.quiz_3_answer_a,
                            R.string.quiz_3_answer_b,
                            R.string.quiz_3_answer_c
                    },
                    2),
            new QuizQuestion(
                    R.string.quiz_4_paragraph,
                    R.string.quiz_4_question,
                    new int[]{
                            R.string.quiz_4_answer_a,
                            R.string.quiz_4_answer_b,
                            R.string.quiz_4_answer_c
                    },
                    0),
    };

    private QuizBank() {
    }

    public static QuizQuestion getRandomQuestion(Random random) {
        return QUESTIONS[random.nextInt(QUESTIONS.length)];
    }

    public static int getQuestionCount() {
        return QUESTIONS.length;
    }
}

package pl.rvyk.instapp.enums;

import android.content.Context;
import pl.rvyk.instapp.R;

public enum Grades {
    CORRECT(R.string.grades_good),
    SYNONYM(R.string.grades_synonym),
    WRONG_CASE(R.string.grades_letter_case),
    TYPO(R.string.grades_typo),
    WRONG(R.string.grades_incorrect);

    private final int labelResId;

    Grades(int labelResId) {
        this.labelResId = labelResId;
    }

    public String getLabel(Context context) {
        return context.getString(labelResId);
    }

    public static Grades fromGrade(int grade) {
        switch (grade) {
            case 1:
                return CORRECT;
            case 2:
                return SYNONYM;
            case 3:
                return WRONG_CASE;
            case 4:
                return TYPO;
            default:
                return WRONG;
        }
    }
}

package br.pucminas.stundentproject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luket on 03-Sep-16.
 */
public class ResultStudents {
    private List<Student> results = new ArrayList<Student>();

    /**
     * @return The results
     */
    public List<Student> getResults() {
        return results;
    }

    /**
     * @param results The results
     */
    public void setResults(List<Student> results) {
        this.results = results;
    }
}

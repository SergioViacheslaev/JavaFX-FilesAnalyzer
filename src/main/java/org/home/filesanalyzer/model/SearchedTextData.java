package org.home.filesanalyzer.model;

import lombok.Data;

/**
 * Stores search meta-data for each Tab.
 */
@Data
public class SearchedTextData {
    private String text;
    private Integer position;
}

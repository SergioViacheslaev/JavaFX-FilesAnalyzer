package org.home.filesanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores search meta-data for each Tab.
 * @author Sergei Viacheslaev
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchedTextData {
    private String text;
    private Integer position;
}

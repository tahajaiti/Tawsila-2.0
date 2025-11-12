package com.kyojin.tawsila.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchInput {
    private String key;
    private String operation;
    private Object value;
}
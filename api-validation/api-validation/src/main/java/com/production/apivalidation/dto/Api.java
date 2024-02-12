package com.production.apivalidation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
Class : Api
Type : DTO
Desc : DTO class to display/save API details
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Api
{
    private String url;
    private String method;
    private List<Object> header;
    private Object body;
}

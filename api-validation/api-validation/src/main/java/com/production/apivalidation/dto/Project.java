package com.production.apivalidation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
Class : Project
Type : DTO
Desc : DTO class to display/save Project details
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project
{
    private String project;
    private List<Api> api;
}
